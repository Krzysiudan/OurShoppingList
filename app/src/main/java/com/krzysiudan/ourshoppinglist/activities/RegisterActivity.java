package com.krzysiudan.ourshoppinglist.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
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

public class RegisterActivity extends BaseActivity {

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

        SpannableStringBuilder str = new SpannableStringBuilder("Have account already? Login");
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 22, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        backToLogin.setText(str);


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
                    if(task.getException() instanceof FirebaseAuthUserCollisionException){
                        showErrorDialog("This email adress is already in use ");
                    }else {
                        showErrorDialog("Registration attempt failed");
                    }
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
            Toast.makeText(this.getApplicationContext(),"Account created happy shopping to you! :)",Toast.LENGTH_LONG).show();
            createUserModelInDatabase();
            Intent goToTheApp = new Intent(RegisterActivity.this, ListActivity.class);
            finish();
            startActivity(goToTheApp);
        } else{
            Intent getBack = new Intent(RegisterActivity.this, MainActivity.class);
            finish();
            startActivity(getBack);
        }
    }

    private void showSnackbar(String message){
        View.OnClickListener snackbarClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {

            }
        };
        View parentView = this.findViewById(R.id.layout_register_activity);
        Snackbar.make(parentView,message,Snackbar.LENGTH_INDEFINITE)
                .setAction(message,snackbarClickListener)
                .setActionTextColor(getResources().getColor(R.color.accent))
                .show();
    }


    private void createUserModelInDatabase(){
        String userEmail = mAuth.getCurrentUser().getEmail();
        String userName = mAuth.getCurrentUser().getDisplayName();
        String tokenId = mAuth.getCurrentUser().getUid();

        userModel mUserModel = new userModel(tokenId,userEmail,userName);

        FirebaseFirestore.getInstance().collection("users").document(userEmail).set(mUserModel).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    @Override
    public void onBackPressed() {
        updateUI(mAuth.getCurrentUser());

    }
}
