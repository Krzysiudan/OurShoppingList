package com.krzysiudan.ourshoppinglist.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.krzysiudan.ourshoppinglist.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.facebook.FacebookSdk;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivityLog";


    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;


    private static int RC_SIGN_IN = 100;

    @BindView(R.id.textView_email) AutoCompleteTextView emailText;
    @BindView(R.id.textView_password) EditText passwordText;
    @BindView(R.id.button_sign_in) Button buttonSignIn;
    @BindView(R.id.button_register) Button buttonRegister;
    @BindView(R.id.sign_in_button) SignInButton googleSignInButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("OurShoppingList","googleSignInButton clicked");
                switch (v.getId()){
                    case R.id.sign_in_button:
                        Log.e("OurShoppingList","correct case worked");
                        signIn();
                        break;
                }
            }
        });

        Log.e("OurShoppingList","Button refernces complited");

        passwordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i== R.integer.login || i==EditorInfo.IME_NULL){
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });


        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,RegisterActivity.class);
                finish();
                startActivity(i);
            }
        });

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mAuth = FirebaseAuth.getInstance();


    }



    private void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode ==RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignedResault(task);
        }
    }

    private void handleSignedResault(Task<GoogleSignInAccount> task) {
        try{
            GoogleSignInAccount account =task.getResult(ApiException.class);
            updateUI(account);
        }catch (ApiException e){
            Log.e("OurShoppingList", "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkIfThereIsEmailAndPassword();
    }


    @Override
    protected void onStart() {
        super.onStart();
        String i = getIntent().getStringExtra("logout_key");
        if(i==null){
            GoogleSignInAccount account =  GoogleSignIn.getLastSignedInAccount(this);
            updateUI(account);
        }

    }

    private void updateUI(GoogleSignInAccount account) {
        if (account==null){
            Log.e("OurShoppingList","There is no google account");

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
        String email = emailText.getText().toString();
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
                    }else{
                        Intent i = new Intent(MainActivity.this, ListActivity.class);
                        finish();
                        startActivity(i);
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


    private void checkIfThereIsEmailAndPassword(){
        String rememberedEmail ;
        String rememberedPassword;

        SharedPreferences prefer = getSharedPreferences(RegisterActivity.DATA_PREFS,MODE_PRIVATE);
        rememberedEmail = prefer.getString(RegisterActivity.EMAIL_KEY,null);
        rememberedPassword = prefer.getString(RegisterActivity.PASSWORD_KEY,null);

        Log.e("OurShoppingList","RememberedEmail value: "+rememberedEmail);
        Log.e("OurShoppingList","RememberedPassword value: "+rememberedPassword);

        if(!(rememberedEmail==null)){
            emailText.setText(rememberedEmail);
        }
        if(!(rememberedPassword==null)){
            passwordText.setText(rememberedPassword);
        }
    }
}
