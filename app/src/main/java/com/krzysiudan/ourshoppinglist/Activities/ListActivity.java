package com.krzysiudan.ourshoppinglist.Activities;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.krzysiudan.ourshoppinglist.Adapters.RecyclerAdapterShoppingList;
import com.krzysiudan.ourshoppinglist.Fragments.DialogAddList;
import com.krzysiudan.ourshoppinglist.Fragments.DialogChangeName;
import com.krzysiudan.ourshoppinglist.R;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ListActivity extends AppCompatActivity {

    public static final String MOTHER_NAME="mothername";
    public static final String DATA = "data";
    public static final String TAG = "ListActivityLog";

    private String userUid;



    private String userEmail;
    private FirebaseFirestore mFirestore;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private RecyclerAdapterShoppingList mRecyclerAdapter;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    @BindView(R.id.ShoppingListrv) RecyclerView mRecyclerView;
    @BindView(R.id.include2) Toolbar mToolbar;
    @BindView(R.id.floatingActionButton) FloatingActionButton newListButton;
    @BindView(R.id.toolbar_title) TextView mTitle;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG,"ON CREATE");
        setContentView(R.layout.activity_list_rv);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirestore = FirebaseFirestore.getInstance();

        setupFirebaseListener();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        updateUI(mAuth.getCurrentUser());
        Log.e(TAG,"ON START");
        mRecyclerAdapter = new RecyclerAdapterShoppingList(this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setClickable(true);

        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);
    }

    @OnClick(R.id.floatingActionButton)
    public void floatingActionButtonClicked(View view){
        addList();
    }

    private void updateUI(FirebaseUser user){
        if(user==null){
            Intent goBack = new Intent(ListActivity.this, MainActivity.class);
            finish();
            startActivity(goBack);
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

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction mFragmentTransaction = fm.beginTransaction();
        Fragment old = fm.findFragmentByTag("Add_List_Dialog");

        if(old instanceof DialogAddList){
            Log.d(TAG,"Fragment already exist");
            mFragmentTransaction.remove(old);
        }
        DialogAddList dialog = DialogAddList.newInstance();
        mFragmentTransaction.addToBackStack(null);
        dialog.show(mFragmentTransaction,"Add_List_Dialog");
        View view = findViewById(R.id.activity_list_coordinatorLayout);
        Snackbar.make(view,"List added",Snackbar.LENGTH_SHORT)
                .show();
    }

    private void setupFirebaseListener(){
        Log.e(TAG, "setupFirebaseListener: setting up the auth state listener");
        mAuthStateListener= firebaseAuth -> {
            FirebaseUser mFirebaseUser = firebaseAuth.getCurrentUser();
            if(mFirebaseUser !=null){
                user = mFirebaseUser;
                userUid = user.getUid();
                userEmail = user.getEmail();
                Log.e(TAG,"User UID: "+ userUid);
            }else{
                Log.e(TAG,"We don't get a user :(");
                updateUI(user);
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
               LogOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void LogOut(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout")
                .setMessage("Do you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Log.e(TAG,"SIGNED OUT");
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("logout_key","Logout");
                    startActivity(intent);
                    Log.e("OurShoppingList","Dialog logout button clicked");
                })
                .setNegativeButton("NO", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onBackPressed() {
        LogOut();
    }


}

