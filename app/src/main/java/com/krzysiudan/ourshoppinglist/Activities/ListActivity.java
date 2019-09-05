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
    public static final String TAG = "ListActivityLog";

    private FirebaseFirestore mFirestore;
    private RecyclerView  mRecyclerView;
    private FloatingActionButton newListButton;
    private DatabaseReference mDatabaseReference;
    private String userUid;
    private RecyclerAdapterShoppingList mRecyclerAdapter;
    private Toolbar mToolbar;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private boolean fabShouldBeShown;
    private FirebaseUser user;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG,"ON CREATE");
        setContentView(R.layout.activity_list_rv);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirestore = FirebaseFirestore.getInstance();

        setupFirebaseListener();



        newListButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        mRecyclerView = (RecyclerView) findViewById(R.id.ShoppingListrv);

        mToolbar = (Toolbar) findViewById(R.id.include2);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView mTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(R.string.Title);



    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.items_menu,menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG,"ON START");
        mRecyclerAdapter = new RecyclerAdapterShoppingList(this,mFirestore,"elo",this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setClickable(true);

        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);



        final String userDisplayName ;

        if(user !=null){
            userUid = user.getUid();
            if(userUid.isEmpty()){
                for(UserInfo profile :user.getProviderData()){
                    userUid = profile.getUid();
                }
            }
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
                    }
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }


        newListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addList();
            }
        });


        /*setting display name for users from google signin
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if(acct!=null){
            Log.e("OurShoppingList","User display name:"+ acct.getDisplayName());

            String actualName = acct.getDisplayName();
            if(actualName.equals(null)){
               showSettingDisplayNameDialog();
            }

        }*/

        //setting display name for users from normal signin
        if(user!=null){
            Log.e("OurShoppingList","User display name:"+ user.getDisplayName());

            String actualName = user.getDisplayName();

            if(actualName.equals("")){
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
                    Log.e(TAG,"User profile updated");
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    Log.e(TAG,"User display name:"+ user.getDisplayName());
                }
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

                            ShoppingList mShoppingList = new ShoppingList();
                            mShoppingList.setList_name(list_name);
                            mShoppingList.setOwner_id(userUid);
                            Log.e(TAG, "User Uid:" +userUid);

                            mFirestore.collection("ShoppingLists")
                                    .add(mShoppingList)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.e(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                           Map<String,Object> userMap = new HashMap<>();
                                           userMap.put("user_ID",userUid);
                                            documentReference.collection("users_allowed").add(userMap)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            Log.e(TAG,"User added to shoppingList");
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.e(TAG, "Failure in adding user to shoppingList");
                                                        }
                                                    });

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

    private void setupFirebaseListener(){
        Log.e(TAG, "setupFirebaseListener: setting up the auth state listener");
        mAuthStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = firebaseAuth.getCurrentUser();
                if(mFirebaseUser !=null){
                    userUid = mFirebaseUser.getUid();
                    user = mFirebaseUser;
                    Log.e(TAG,"User UID: "+ userUid);
                }else{
                    Log.e(TAG,"We don't get a user :(");

                }
            }
        };

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG,"ON STOP");
        if(mAuthStateListener!=null){
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener);
        }
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
                                Log.e(TAG,"SIGNED OUT");
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


}

