package com.krzysiudan.ourshoppinglist.Activities;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.krzysiudan.ourshoppinglist.Adapters.ListAdapter;
import com.krzysiudan.ourshoppinglist.R;
import com.krzysiudan.ourshoppinglist.DatabaseItems.ShoppingList;

import java.util.HashMap;
import java.util.Map;

public class ListActivity extends AppCompatActivity  {

    public static final String MOTHER_NAME="mothername";
    public static final String DATA = "data";


    private ListView mListView;
    private ImageButton newList;
    private DatabaseReference mDatabaseReference;
    private ListAdapter mListAdapter;
    private Toolbar mToolbar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        newList = (ImageButton) findViewById(R.id.floatingActionButton);
        mListView = (ListView) findViewById(R.id.list_view);

        mToolbar = (Toolbar) findViewById(R.id.include2);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView mTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(R.string.Title);


        newList.setOnClickListener(new View.OnClickListener() {
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
                            DatabaseReference listref = mDatabaseReference.child("ShoppingLists");

                            Map<String, Object> shoppingListMap = new HashMap<>();
                            shoppingListMap.put(list_name, new ShoppingList(list_name));

                            listref.updateChildren(shoppingListMap);
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
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    @Override
    protected void onStart() {
        super.onStart();
        mListAdapter = new ListAdapter(this,mDatabaseReference,"elo");
        mListView.setAdapter(mListAdapter);


        mListView.setClickable(true);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(ListActivity.this,ActivityMainItems.class);
                String motherListName = mListAdapter.getItem(i).getList_name();
                Log.e("OurShoppingList","Mother LIst name: "+mListAdapter.getItem(i).getList_name());
                intent.putExtra("MotherListName", mListAdapter.getItem(i).getList_name());
                mListAdapter.cleanUp();
                finish();
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}

