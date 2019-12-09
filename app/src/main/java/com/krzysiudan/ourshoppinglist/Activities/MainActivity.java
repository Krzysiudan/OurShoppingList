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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.krzysiudan.ourshoppinglist.R;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivityLog";


    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;


    private static int RC_SIGN_IN = 100;

    @BindView(R.id.textView_email_register)
    AutoCompleteTextView inputEmail;
    @BindView(R.id.textView_password_register) EditText passwordText;
    @BindView(R.id.button_sign_in) Button buttonSignIn;
    @BindView(R.id.google_sign_in)
    SignInButton googleSignIn;
    @BindView(R.id.button_go_to_register) Button buttonRegister;
    @BindView(R.id.activity_main_layout) ConstraintLayout parentView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

       /* ActionCodeSettings mActionCodeSettings = ActionCodeSettings.newBuilder()
                .setAndroidPackageName(getPackageName(), true,null)
                .setHandleCodeInApp(true)
                .setUrl("our-shopping-list-5f1e7.firebaseapp.com")
                .build(); */

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            updateUI(mAuth.getCurrentUser());
            Intent i = new Intent(MainActivity.this,ListActivity.class);
            finish();
            startActivity(i);
        } else{
           /* startActivityForResult(AuthUI.getInstance()
                                         .createSignInIntentBuilder()
                                         .setAvailableProviders(Arrays.asList(
                                                 new AuthUI.IdpConfig.GoogleBuilder().build(),
                                                 new AuthUI.IdpConfig.FacebookBuilder().build(),
                                                 new AuthUI.IdpConfig.EmailBuilder().enableEmailLinkSignIn()
                                                         .setActionCodeSettings(mActionCodeSettings).build()))
                                         .build(),RC_SIGN_IN);*/
        }



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
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode ==RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignedResult(task);
        }
    }

    private void handleSignedResult(Task<GoogleSignInAccount> task) {
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

    private void checkIfUserIsInFirestore(){
        final FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();
        final FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final String userUid = mFirebaseUser.getUid();
        mFirebaseFirestore.collection("users").document(userUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(!document.exists()){
                        Map<String,Object> userMap = new HashMap<>();
                        userMap.put("display_name",mFirebaseUser.getDisplayName());
                        FirebaseFirestore.getInstance().collection("users").document(userUid).set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.e(TAG,"User added to collection");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG,"Failed to add user to collection");
                            }
                        });
                    } else{
                        Log.e(TAG,"There is a document in firestore");
                    }
                }else{
                    Log.e(TAG,"Get failed with"+task.getException());
                }
            }
        });
    }

    private void attemptLogin(){
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
