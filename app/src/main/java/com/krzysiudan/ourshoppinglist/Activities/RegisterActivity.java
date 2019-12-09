package com.krzysiudan.ourshoppinglist.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.krzysiudan.ourshoppinglist.R;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    public static final String LIST_PREFS ="ListPrefs";
    public static final String DISPLAY_NAME_KEY="username";
    public static final String DATA_PREFS ="DataPrefs";
    public static final String EMAIL_KEY="EmailKey";
    public static final String PASSWORD_KEY="PasswordKey";
    public static final String TAG = "RegisterActivityLog";

    private Button registerbutton;
    private AutoCompleteTextView usernameText;
    private AutoCompleteTextView emailText;
    private EditText passwordText;
    private EditText repeatpasswordText;

    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerbutton = (Button) findViewById(R.id.register_button_regactivity);

        usernameText = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_Username);
        emailText = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_Email);
        passwordText = (EditText) findViewById(R.id.editText_Password);
        repeatpasswordText = (EditText) findViewById(R.id.editText_RepeatPassword);

        repeatpasswordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i==R.integer.register_form_finished ||i ==EditorInfo.IME_NULL){
                    attemptRegistration();
                    return true;
                }

                return false;
            }
        });
        mAuth=FirebaseAuth.getInstance();

        registerbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegistration();
            }
        });
    }

    private void attemptRegistration(){

        //reset errors
        usernameText.setError(null);
        emailText.setError(null);

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        boolean cancel = false;
        View focusView =null;

        //checking for valid password
        if(TextUtils.isEmpty(password)|| !isPasswordValid(password)){
            passwordText.setError("Password too short or");
            focusView=passwordText;
            cancel=true;
        }else{
            Log.e(TAG,"Password is valid");
        }

        //check for a valid email address
        if(TextUtils.isEmpty(email)){
            emailText.setError("This Field is required");
            focusView=emailText;
            cancel=true;
        }else if(!isEmailValid(email)){
            emailText.setError("This email address is invalid");
            focusView=emailText;
            cancel=true;
        }else{
            Log.e(TAG,"Email is Valid");
        }

        if(cancel){
            focusView.requestFocus();
        }else{
            createFirebaseUser();
        }

    }

    private boolean isEmailValid(String email){
        return email.contains("@");
    }

    private boolean isPasswordValid(String password){
        String confirmPassword = repeatpasswordText.getText().toString();
        return confirmPassword.equals(password);
    }

    private void createFirebaseUser(){
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener
                (RegisterActivity.this,
                new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.e(TAG,"create user onComplete:"+ task.isSuccessful());

                if(!task.isSuccessful()){
                    Log.e(TAG,"Create user failed"+task.getException());
                    showErrorDialog("Registration attempt failed");
                }else{
                    saveDisplayName();
                    saveEmailAndPassword();
                    FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();
                    String displayName = usernameText.getText().toString();
                    String userUid =task.getResult().getUser().getUid();
                    Map<String,Object> userMap = new HashMap<>();
                    userMap.put("display_name",displayName);

                    mFirebaseFirestore.collection("users").document(userUid).set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                    Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                    finish();
                    startActivity(i);
                }

            }
        });

    }

    private void saveDisplayName(){
        String displayName = usernameText.getText().toString();
        SharedPreferences prefs = getSharedPreferences(LIST_PREFS,0);
        prefs.edit().putString(DISPLAY_NAME_KEY,displayName).apply();
    }

    private void saveEmailAndPassword(){
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        Log.e(TAG,email+password);
        SharedPreferences prefer = getSharedPreferences(DATA_PREFS,0);
        prefer.edit().putString(EMAIL_KEY,email).apply();
        prefer.edit().putString(PASSWORD_KEY,password).apply();
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
