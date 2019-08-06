package com.krzysiudan.ourshoppinglist.Activities;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.krzysiudan.ourshoppinglist.Adapters.AdapterPlannedItemList;
import com.krzysiudan.ourshoppinglist.Adapters.RecyclerAdapterPlannedItemList;
import com.krzysiudan.ourshoppinglist.R;
import com.krzysiudan.ourshoppinglist.DatabaseItems.SingleItem;

public class ItemsActivity_old extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private DatabaseReference mDatabaseReference;
    private AdapterPlannedItemList mListAdapter;
    private String mMotherList;
    private String mUsername;
    private TextInputEditText addItemEditText;
    private Toolbar mToolbar;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        mDatabaseReference=FirebaseDatabase.getInstance().getReference();

        mMotherList=getIntent().getStringExtra("MotherListName");

        addItemEditText = (TextInputEditText) findViewById(R.id.text_input_edit);

        mRecyclerView = (RecyclerView) findViewById(R.id.list_view);

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

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("OurShoppingList","working2");
        Log.e("OurShoppingList",mUsername+" is my username");
        RecyclerAdapterPlannedItemList mRecyclerAdapter = new RecyclerAdapterPlannedItemList(this, mUsername, mMotherList, mDatabaseReference);
        mRecyclerView.setAdapter(mRecyclerAdapter);
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

    }
}
