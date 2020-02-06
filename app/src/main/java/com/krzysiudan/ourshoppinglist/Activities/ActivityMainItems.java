package com.krzysiudan.ourshoppinglist.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.krzysiudan.ourshoppinglist.Adapters.FragmentPagerAdapterItems;
import com.krzysiudan.ourshoppinglist.Fragments.FragmentPlannedItems;
import com.krzysiudan.ourshoppinglist.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ActivityMainItems extends AppCompatActivity {

    public static final String TAG = "ActivityMainItemsLog";

    @BindView(R.id.include_items) Toolbar mToolbar;
    @BindView(R.id.toolbar_title) TextView toolbarTittle;

    private String motherListKey;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_items);
        ButterKnife.bind(this);
        setupFirebaseListener();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbarTittle.setText(R.string.products);

        motherListKey = getIntent().getStringExtra("MotherListName");
        Log.e("OurShoppingList","MOTHERLISTKEY: "+motherListKey);

        Bundle bundle = new Bundle();
        bundle.putString("MOTHER_LIST_KEY",motherListKey);
        FragmentPlannedItems frag = new FragmentPlannedItems();
        frag.setArguments(bundle);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        FragmentPagerAdapterItems mPagerAdapter =
                new FragmentPagerAdapterItems(getSupportFragmentManager(),ActivityMainItems.this,
                        motherListKey);
        viewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
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
                Intent intent = new Intent(this, ListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void setupFirebaseListener(){
        Log.e(TAG, "setupFirebaseListener: setting up the auth state listener");
        mAuthStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = firebaseAuth.getCurrentUser();
                if(mFirebaseUser !=null){
                    String userUid = mFirebaseUser.getUid();
                    Log.e(TAG,"User UID: "+ userUid);
                }else{
                    Log.e(TAG,"We don't get a user :(");
                }
            }
        };
    }
}
