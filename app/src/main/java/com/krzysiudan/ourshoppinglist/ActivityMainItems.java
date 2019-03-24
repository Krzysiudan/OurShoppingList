package com.krzysiudan.ourshoppinglist;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ActivityMainItems extends AppCompatActivity {

    private Toolbar mToolbar;
    private String motherListName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mToolbar = (Toolbar) findViewById(R.id.include_items);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView mTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        mTitle.setText("Products");


        motherListName = getIntent().getStringExtra("MotherListName");
        Log.e("OurShoppingList","MOTHERLISTNAME: "+motherListName);

        Bundle bundle = new Bundle();
        bundle.putString("MOTHER_LIST_NAME",motherListName);
        FragmentPlannedItems frag = new FragmentPlannedItems();
        frag.setArguments(bundle);



        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        FragmentPagerAdapterItems mPagerAdapter =
                new FragmentPagerAdapterItems(getSupportFragmentManager(),ActivityMainItems.this,
                        motherListName);
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
}
