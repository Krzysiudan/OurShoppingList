package com.krzysiudan.ourshoppinglist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.krzysiudan.ourshoppinglist.models.userModel;
import com.krzysiudan.ourshoppinglist.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;

public class MainActivity extends BaseActivity {
    public static final String TAG = "MainActivityLog";
    private static int RC_SIGN_IN = 100;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;
    private FirebaseFirestore mFirebaseFirestore;

    @BindView(R.id.textView_email_register) AutoCompleteTextView inputEmail;
    @BindView(R.id.textView_password_register) EditText inputPassword;
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

        mAuth=FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
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

        SpannableStringBuilder str = new SpannableStringBuilder("Don\'t have account yet? Sign up");
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 24, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        buttonRegister.setText(str);
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
                            createUserModelInDatabase();
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
        attemptLogin();
    }

    @OnEditorAction(R.id.textView_password_register)
    public boolean logInEditor(TextView view, int i,KeyEvent keyEvent){
        if(i==R.integer.login || i == EditorInfo.IME_NULL){
            attemptLogin();
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
                            createUserModelInDatabase();
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
        updateUI(mAuth.getCurrentUser());
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

    private void attemptLogin(){

        //reset errors
        inputEmail.setError(null);
        inputPassword.setError(null);

        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        boolean cancel = false;
        View focusView =null;

        //checking for valid inputPassword
        if(TextUtils.isEmpty(password)|| !isPasswordValid(password)){
            inputPassword.setError("Password too short, must be more than 6 characters");
            focusView=inputPassword;
            cancel=true;
        }else{
            Log.e(TAG,"Password is valid");
        }

        //check for a valid inputEmail address
        if(TextUtils.isEmpty(email)){
            inputEmail.setError("This Field is required");
            focusView=inputEmail;
            cancel=true;
        }else if(!isEmailValid(email)){
            inputEmail.setError("This email address is invalid");
            focusView=inputEmail;
            cancel=true;
        }else{
            Log.e(TAG,"Email is Valid");
        }

        if(cancel){
            focusView.requestFocus();
        }else{
            firebaseAuthWithEmailAndPassword();
        }

    }

    private boolean isEmailValid(String email){
        return email.contains("@");
    }

    private boolean isPasswordValid(String password){
        return password.length()>=6;
    }

    private void firebaseAuthWithEmailAndPassword(){
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        if(email.equals("")||password.equals("")) return;
        Toast.makeText(this, R.string.logginginprogress,Toast.LENGTH_SHORT).show();
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.e("OurShoppingList","SignInWithEmail onComplete: "+task.isSuccessful());
                if(!task.isSuccessful()){
                    if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                        showErrorDialog("The password is invalid or the user does not hava a password");
                    }else{
                        showErrorDialog("There was problem singing in, check your credentials again");
                    }
                    Log.e("OurShoppingList","Problem singing in:"+task.getException());
                    updateUI(mAuth.getCurrentUser());
                }else{
                    createUserModelInDatabase();
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

    private void createUserModelInDatabase(){
        String userEmail = mAuth.getCurrentUser().getEmail();
        String userName = mAuth.getCurrentUser().getDisplayName();
        String tokenId = mAuth.getCurrentUser().getUid();

        userModel mUserModel = new userModel(tokenId,userEmail,userName);

        mFirebaseFirestore.collection("users").document(userEmail).set(mUserModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG,"User succesfully created");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG,"User creation failed");
            }
        });

        saveTokenFCM();
    }

    private void saveTokenFCM(){
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                FirebaseUser user = mAuth.getCurrentUser();
                DocumentReference userAccount = FirebaseFirestore.getInstance().collection("users").document(user.getEmail());
                userAccount.update("fcmToken",instanceIdResult.getToken());
            }
        });
    }
}
