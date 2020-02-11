package com.krzysiudan.ourshoppinglist.activities;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.krzysiudan.ourshoppinglist.adapters.RecyclerAdapterShoppingList;
import com.krzysiudan.ourshoppinglist.fragments.Dialogs.DialogAddList;
import com.krzysiudan.ourshoppinglist.R;
import com.krzysiudan.ourshoppinglist.fragments.Dialogs.DialogDisplayName;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ListActivity extends BaseActivity{

    public static final String TAG = "ListActivityLog";

    private String userUid;
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

        setupFirebaseListener();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTitle.setText(R.string.activity_list_top_bar_tittle);

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
        checkIfUserFromFirebase();
        Log.e(TAG,"ON START");
        mRecyclerAdapter = new RecyclerAdapterShoppingList(this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setClickable(true);

        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);
    }


    private void checkIfUserFromFirebase(){
        Intent fromRegisterActivity = getIntent();
        if(fromRegisterActivity.getIntExtra("FirebaseUser",0)==100){
            Log.d(TAG, "User logged in for the first time(auth with firebase");
            getDisplayNameFromUser();
        } else if(mAuth.getCurrentUser().getDisplayName().equals("")){
            Log.d(TAG, "User don't have user name");
            getDisplayNameFromUser();
        }
    }

    private void getDisplayNameFromUser(){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction mFragmentTransaction = fm.beginTransaction();
        Fragment old = fm.findFragmentByTag("Display_Name_Dialog");

        if(old instanceof DialogDisplayName){
            Log.d(TAG,"Fragment already exist");
            mFragmentTransaction.remove(old);
        }
        DialogDisplayName dialog = DialogDisplayName.newInstance();
        mFragmentTransaction.addToBackStack(null);
        dialog.show(mFragmentTransaction,"Display_Name_Dialog");
    }

    private String getProvider(){
        FirebaseUser user = mAuth.getCurrentUser();
        String providerId ="";
        if(user != null){
            for(UserInfo profile : user.getProviderData()){
                providerId = profile.getProviderId();
            }
        }
        return providerId;
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

    }

    private void setupFirebaseListener(){
        Log.e(TAG, "setupFirebaseListener: setting up the auth state listener");
        mAuthStateListener= firebaseAuth -> {
            FirebaseUser mFirebaseUser = firebaseAuth.getCurrentUser();
            if(mFirebaseUser !=null){
                user = mFirebaseUser;
                userUid = user.getUid();
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

