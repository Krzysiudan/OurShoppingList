package com.krzysiudan.ourshoppinglist;

import android.app.ListActivity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
        mAuth = FirebaseAuth.getInstance();

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


    }

    private void attemptLogin(){
        String email = emailText.getText().toString();
        String password = emailText.getText().toString();

        if(email.equals("")||password.equals("")){
            Toast.makeText(this,"Logging in progress",Toast.LENGTH_SHORT).show();
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.e("OurShoppingList","SignInWithEmail onComplete"+task.isSuccessful());

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
