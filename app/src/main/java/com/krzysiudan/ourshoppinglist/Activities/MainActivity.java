package com.krzysiudan.ourshoppinglist.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.krzysiudan.ourshoppinglist.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivityLog";
    private static int RC_SIGN_IN = 100;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;

    @BindView(R.id.textView_email_register) AutoCompleteTextView inputEmail;
    @BindView(R.id.textView_password_register) EditText passwordText;
    @BindView(R.id.button_sign_in) Button buttonSignIn;
    @BindView(R.id.google_sign_in) SignInButton googleSignIn;
    @BindView(R.id.button_go_to_register) Button buttonRegister;
    @BindView(R.id.activity_main_layout) ConstraintLayout parentView;
    @BindView(R.id.login_button_facebook) LoginButton facebookLogIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mCallbackManager = CallbackManager.Factory.create();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        facebookLogIn.setReadPermissions("email","public_profile");

        facebookLogIn.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>(){
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                updateUI(null);
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d(TAG, "facebook:onError", exception);
                updateUI(null);
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    @OnClick(R.id.button_go_to_register)
    public void register(View view){
        Intent i = new Intent(MainActivity.this,RegisterActivity.class);
        finish();
        startActivity(i);
    }

    @OnClick(R.id.button_sign_in)
    public void logInButton(View view){
        firebaseAuthWithEmailAndPassword();
    }

    @OnEditorAction(R.id.textView_password_register)
    public boolean logInEditor(TextView view, int i,KeyEvent keyEvent){
        if(i==R.integer.login || i == EditorInfo.IME_NULL){
            firebaseAuthWithEmailAndPassword();
            return true;
        }
        return false;
    }


    @OnClick(R.id.google_sign_in)
    public void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode ==RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignedResultGoogle(task);
        }else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignedResultGoogle(Task<GoogleSignInAccount> task) {
        try{
            GoogleSignInAccount account =task.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        }catch (ApiException e){
            Log.e("OurShoppingList", "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account){
        Log.d(TAG,"FirebaseAuthWithGoogle" + account.getId());

        AuthCredential mAuthCredential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mAuth.signInWithCredential(mAuthCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG,"signedInWithCredential:Success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        }else{
                            Log.w(TAG,"SignInWithCredential:failure", task.getException());
                            Snackbar.make(parentView, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
        super.onStart();
    }

    private void updateUI(FirebaseUser user) {
        if (user==null){
            Log.e("OurShoppingList","No user logged in");

        }else{
            //checkIfUserIsInFirestore();
            Intent i = new Intent(MainActivity.this, ListActivity.class);
            finish();
            startActivity(i);
        }
    }

    private void firebaseAuthWithEmailAndPassword(){
        String email = inputEmail.getText().toString();
        String password = passwordText.getText().toString();

        if(email.equals("")||password.equals("")) return;
        Toast.makeText(this, R.string.logginginprogress,Toast.LENGTH_SHORT).show();
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.e("OurShoppingList","SignInWithEmail onComplete: "+task.isSuccessful());

                if(!task.isSuccessful()){
                    Log.e("OurShoppingList","Problem singing in:"+task.getException());
                    showErrorDialog("There was a problem with singing in");
                    updateUI(null);
                }else{
                    updateUI(mAuth.getCurrentUser());
                }
            }
        });

    }

    private void showErrorDialog(String message){
        new AlertDialog.Builder(this)
                .setTitle("Oops")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok,null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
