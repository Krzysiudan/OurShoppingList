package com.krzysiudan.ourshoppinglist;

import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ItemsActivity extends AppCompatActivity {

    private ListView mListView;
    private DatabaseReference mDatabaseReference;
    private AdapterItemList mListAdapter;
    private String mMotherList;
    private String mUsername;
    private TextInputEditText addItemEditText;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        mDatabaseReference=FirebaseDatabase.getInstance().getReference();

        mMotherList=getIntent().getStringExtra("MotherListName");

        addItemEditText = (TextInputEditText) findViewById(R.id.text_input_edit);

        mListView = (ListView) findViewById(R.id.list_view);

        SharedPreferences prefs = getSharedPreferences(RegisterActivity.LIST_PREFS,MODE_PRIVATE);
        mUsername = prefs.getString(RegisterActivity.DISPLAY_NAME_KEY,null);
        Log.e("OurShoppingList","working1");







    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("OurShoppingList","working2");
        Log.e("OurShoppingList",mUsername+" is my username");
        mListAdapter = new AdapterItemList(this,mDatabaseReference,mMotherList,mUsername);
        mListView.setAdapter(mListAdapter);
        Log.e("OurShoppingList","working3");
        addItemEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i==EditorInfo.IME_ACTION_DONE){
                    String newItem = addItemEditText.getText().toString();
                    if(!newItem.equals("")){
                        DatabaseReference itemRef = mDatabaseReference.child("ShoppingLists")
                                .child(mMotherList).child("Items");
                        itemRef.push().setValue(new SingleItem(newItem,mUsername));
                        addItemEditText.setText("");
                    }
                }
                return false;
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView text = (TextView) view;
                text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
        });


    }
}
