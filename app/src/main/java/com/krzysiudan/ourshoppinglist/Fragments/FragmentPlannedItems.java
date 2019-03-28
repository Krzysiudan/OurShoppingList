package com.krzysiudan.ourshoppinglist.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.krzysiudan.ourshoppinglist.Activities.RegisterActivity;
import com.krzysiudan.ourshoppinglist.Adapters.AdapterPlannedItemList;
import com.krzysiudan.ourshoppinglist.R;
import com.krzysiudan.ourshoppinglist.DatabaseItems.SingleItem;

public class FragmentPlannedItems extends Fragment {

    private ListView mListView;
    private DatabaseReference mDatabaseReference;
    private AdapterPlannedItemList mListAdapter;
    private String mUsername;
    private TextInputEditText addItemEditText;
    private String motherListName;

    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    public static FragmentPlannedItems newInstance(String text) {
        FragmentPlannedItems f = new FragmentPlannedItems();
        Bundle args = new Bundle();
        args.putString("MOTHERLISTNAME", text);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseReference= FirebaseDatabase.getInstance().getReference();

        //SharedPreferences mPreferences = this.getActivity().getSharedPreferences(ListActivity.DATA, Context.MODE_PRIVATE);
        //motherListName = mPreferences.getString(ListActivity.MOTHER_NAME,null);

        SharedPreferences prefs = this.getActivity().getSharedPreferences(RegisterActivity.LIST_PREFS,Context.MODE_PRIVATE);
        mUsername = prefs.getString(RegisterActivity.DISPLAY_NAME_KEY,null);
        Log.e("OurShoppingList","DISPLAY NAME: " + mUsername);

        if(getArguments()!=null){
            Bundle b = this.getArguments();
            motherListName = b.getString("MOTHERLISTNAME");

        }

        Log.e("OurShoppingList","MOTHER:" + motherListName);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_planned_items, container, false);

        addItemEditText = (TextInputEditText) view.findViewById(R.id.text_input_edit);

        mListView = (ListView) view.findViewById(R.id.list_view);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mListAdapter = new AdapterPlannedItemList(getActivity(),mDatabaseReference,motherListName,mUsername);
        mListView.setAdapter(mListAdapter);
        addItemEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i== EditorInfo.IME_ACTION_DONE){
                    String newItem = addItemEditText.getText().toString();
                    if(!newItem.equals("")){
                        DatabaseReference itemRef = mDatabaseReference.child("ShoppingLists")
                                .child(motherListName).child("PlannedItems");
                        itemRef.push().setValue(new SingleItem(newItem,mUsername));
                        addItemEditText.setText("");
                    }
                }
                return false;
            }
        });


        mListView.setClickable(true);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String newItem = mListAdapter.getItem(i).getName();
                DatabaseReference boughtItemRef = mDatabaseReference.child("ShoppingLists")
                        .child(motherListName).child("BoughtItems");
                boughtItemRef.push().setValue(new SingleItem(newItem,mUsername));
                mListAdapter.removeItem(i);
                Toast.makeText(getContext(), R.string.toast_when_item_clicked_planned_tab,
                        Toast.LENGTH_SHORT).show();

            }
        });

    }






    }

