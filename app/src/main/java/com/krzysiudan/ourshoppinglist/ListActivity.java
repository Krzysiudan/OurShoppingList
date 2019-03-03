package com.krzysiudan.ourshoppinglist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ListActivity extends AppCompatActivity  {

    private ListView mListView;
    private ImageButton newList;
    private DatabaseReference mDatabaseReference;
    private ListAdapter mListAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        newList = (ImageButton) findViewById(R.id.imageButton);
        mListView = (ListView) findViewById(R.id.list_view);


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
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText alert_editText = (EditText) alertView.findViewById(R.id
                                .alert_editText);
                        String list_name = alert_editText.getText().toString();

                        if(!list_name.equals("")){
                            DatabaseReference listref = mDatabaseReference.child("ShoppingLists");

                            //listref.push().setValue(new ShoppingList(list_name));

                            Map<String, Object> shoppingListMap = new HashMap<>();
                            shoppingListMap.put(list_name, new ShoppingList(list_name));

                            listref.updateChildren(shoppingListMap);

                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()
                .show();
        Log.e("OurShoppingList","New list added");
    }



   /* public void showPopup(View v){
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener((PopupMenu.OnMenuItemClickListener) this);
        popup.inflate(R.menu.popup);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.menu_rename:
                Toast.makeText(this,"Item 1 clicked",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_remove:
                Toast.makeText(this,"Item 2 clicked",Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }

*/
    @Override
    protected void onStart() {
        super.onStart();
        mListAdapter = new ListAdapter(this,mDatabaseReference,"elo");
        mListView.setAdapter(mListAdapter);


        mListView.setClickable(true);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e("OurShoppingList","Postion on list cliked: " +i);
                Intent intent = new Intent(ListActivity.this,ItemsActivity.class);
                intent.putExtra("MotherListName", mListAdapter.getItem(i).getList_name());
                mListAdapter.cleanUp();
                finish();
                startActivity(intent);
                Log.e("OurShoppingList","Sth bad happening :(");
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}

