package com.krzysiudan.ourshoppinglist.Activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.krzysiudan.ourshoppinglist.Adapters.FragmentPagerAdapterItems;
import com.krzysiudan.ourshoppinglist.Fragments.FragmentPlannedItems;
import com.krzysiudan.ourshoppinglist.R;

public class ActivityMainItems extends AppCompatActivity {

    private Toolbar mToolbar;
    private String motherListKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mToolbar = (Toolbar) findViewById(R.id.include_items);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView mTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(R.string.products);


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
}
