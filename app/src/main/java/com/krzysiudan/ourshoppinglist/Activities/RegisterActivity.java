package com.krzysiudan.ourshoppinglist.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.krzysiudan.ourshoppinglist.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;

public class RegisterActivity extends AppCompatActivity {

    public static final String TAG = "RegisterActivityLog";


    private FirebaseAuth mAuth;

    @BindView(R.id.button_sign_up) Button signUp;
    @BindView(R.id.button_get_back_tologin) Button backToLogin;
    @BindView(R.id.textView_email_register) AutoCompleteTextView inputEmail;
    @BindView(R.id.textView_password_register) EditText inputPassword;
    @BindView(R.id.layout_register_activity)
    ConstraintLayout parentView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();


    }

    @OnClick(R.id.button_sign_up)
    public void signUp (View view){
        Log.d(TAG,"SignUp button clicked");
        attemptRegistration();
    }

    @OnClick(R.id.button_get_back_tologin)
    public void goBackToLoginActivity(View view){
        Log.d(TAG,"Going back to Login Activity");
        Intent goBackToLoginActivity = new Intent(RegisterActivity.this, MainActivity.class);
        finish();
        startActivity(goBackToLoginActivity);
    }

    @OnEditorAction(R.id.textView_password_register)
    public boolean register(TextView textView, int i , KeyEvent keyEvent){
        if(i==R.integer.register_form_finished ||i ==EditorInfo.IME_NULL){
            attemptRegistration();
            return true;
        }
        return false;
    }

    private void attemptRegistration(){

        //reset errors
        inputEmail.setError(null);
        inputPassword.setError(null);

        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        boolean cancel = false;
        View focusView =null;

        //checking for valid inputPassword
        if(TextUtils.isEmpty(password)|| !isPasswordValid(password)){
            inputPassword.setError("Password too short or");
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
            inputEmail.setError("This inputEmail address is invalid");
            focusView=inputEmail;
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
        return password.length()>=6;
    }

    private void createFirebaseUser(){
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

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
                    Log.e(TAG,"Create user success"+task.getException());
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);
                    Snackbar.make(parentView,"Account succesfully created", Snackbar.LENGTH_SHORT).show();
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

    private void updateUI(FirebaseUser user){
        if(user!=null) {
            Intent goToTheApp = new Intent(RegisterActivity.this, ListActivity.class);
            finish();
            startActivity(goToTheApp);
        } else{
            Intent getBack = new Intent(RegisterActivity.this, MainActivity.class);
            finish();
            startActivity(getBack);
        }
    }

    @Override
    public void onBackPressed() {
        updateUI(mAuth.getCurrentUser());

    }
}
