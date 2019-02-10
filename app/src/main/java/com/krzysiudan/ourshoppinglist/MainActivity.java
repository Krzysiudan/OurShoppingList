package com.krzysiudan.ourshoppinglist;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private AutoCompleteTextView emailText;
    private EditText passwordText;
    private Button button_signin;
    private Button button_register;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Log.e("OurShoppingList","Application started");

        emailText = (AutoCompleteTextView) findViewById(R.id.textView_email);
        passwordText = (EditText) findViewById(R.id.textView_password);
        button_signin = (Button)findViewById(R.id.button_input2);
        button_register =(Button)findViewById(R.id.button_input);

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


        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,RegisterActivity.class);
                finish();
                startActivity(i);
            }
        });

        button_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this,"OnResume Launched",Toast.LENGTH_SHORT).show();
        checkIfThereIsEmailAndPassword();
    }

    private void attemptLogin(){
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if(email.equals("")||password.equals("")) return;
        Toast.makeText(this,"Logging in progress",Toast.LENGTH_SHORT).show();
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.e("OurShoppingList","SignInWithEmail onComplete: "+task.isSuccessful());

                    if(!task.isSuccessful()){
                        Log.e("OurShoppingList","Problem singing in:"+task.getException());
                        showErrorDialog("There was a problem with singing in");
                    }else{
                        Intent i = new Intent(MainActivity.this, com.krzysiudan.ourshoppinglist.ListActivity.class);
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
        String rememberedEmail ="";
        String rememberedPassword="";

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
