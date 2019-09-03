package com.krzysiudan.ourshoppinglist.Activities;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.krzysiudan.ourshoppinglist.Adapters.RecyclerAdapterShoppingList;
import com.krzysiudan.ourshoppinglist.R;
import com.krzysiudan.ourshoppinglist.DatabaseItems.ShoppingList;

import java.util.HashMap;
import java.util.Map;

public class ListActivity extends AppCompatActivity   {

    public static final String MOTHER_NAME="mothername";
    public static final String DATA = "data";
    public static final String TAG = "OurShoppingList";

    private FirebaseFirestore mFirestore;
    private RecyclerView  mRecyclerView;
    private FloatingActionButton newListButton;
    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;
    private RecyclerAdapterShoppingList mRecyclerAdapter;
    private Toolbar mToolbar;
    private boolean fabShouldBeShown;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_rv);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        String userUid;
        final String userDisplayName ;

        if(user!=null){
            userUid = user.getUid();
            Log.e(TAG,"User Uid :" + userUid);
            userDisplayName = user.getDisplayName();
            Log.e(TAG,"User display name :" + userDisplayName);
            final DocumentReference userDocumentReference = mFirestore.collection("users").document(userUid);

            userDocumentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                       DocumentSnapshot doc = task.getResult();
                       if(doc.exists()){
                           Log.e(TAG,"Document exist" + doc.getData());
                       } else {
                           Map<String, Object> userData = new HashMap<>();
                           if (!userDisplayName.isEmpty()) {
                               userData.put("display_name", userDisplayName);
                               userDocumentReference.set(userData);
                           }
                       }
                    } else {
                        }
                    }
                });
            }

        newListButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        mRecyclerView = (RecyclerView) findViewById(R.id.ShoppingListrv);

        mToolbar = (Toolbar) findViewById(R.id.include2);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView mTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(R.string.Title);

        newListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addList();
            }
        });


    }

    private void addList(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View alertView = inflater.inflate(R.layout.dialog_custom_add_list,null);

        builder.setView(alertView)
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText alert_editText = (EditText) alertView.findViewById(R.id.alert_editText);
                        String list_name = alert_editText.getText().toString();

                        if(!list_name.equals("")){

                            Map<String, Object> list = new HashMap<>();
                            list.put("list_name", list_name);

                            mFirestore.collection("ShoppingLists")
                                    .add(list)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.e(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error adding document",e);
                                        }
                                    });

                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create()
                .show();
        Log.e("OurShoppingList","New list added");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.items_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Logout")
                        .setMessage("Do you want to logout?")
                        .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("logout_key","Logout");
                                startActivity(intent);
                                Log.e("OurShoppingList","Dialog logout button clicked");
                            }
                        })
                        .setNegativeButton("NO", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }

    }
    @Override
    protected void onStart() {
        super.onStart();
        mRecyclerAdapter = new RecyclerAdapterShoppingList(this,mFirestore,"elo",this);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.setClickable(true);

        /*setting display name for users from google signin
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if(acct!=null){
            Log.e("OurShoppingList","User display name:"+ acct.getDisplayName());

            String actualName = acct.getDisplayName();
            if(actualName.equals(null)){
               showSettingDisplayNameDialog();
            }

        }*/

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //setting display name for users from normal signin
        if(user!=null){
            Log.e("OurShoppingList","User display name:"+ user.getDisplayName());

            String actualName = user.getDisplayName();

            if(actualName.equals(null)){
               showSettingDisplayNameDialog();
            }
        }
    }


    private void showSettingDisplayNameDialog(){
        LayoutInflater inflater = this.getLayoutInflater();
        final View alertView = inflater.inflate(R.layout.dialog_custom_add_list,null);
        final EditText alert_editText = (EditText) alertView.findViewById(R.id.alert_editText);
        alert_editText.setHint("Write your name");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertView)
                .setTitle("You are new!")
                .setMessage("Set your display name")
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = alert_editText.getText().toString();
                        updateDisplayName(name);

                    }
                })
                .show();

    }
    private void updateDisplayName(String name){
        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.e("OurShoppingList","User profile updated");
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    Log.e("OurShoppingList","User display name:"+ user.getDisplayName());
                }
            }
        });

    }


    @Override
    protected void onStop() {
        super.onStop();
    }


}

