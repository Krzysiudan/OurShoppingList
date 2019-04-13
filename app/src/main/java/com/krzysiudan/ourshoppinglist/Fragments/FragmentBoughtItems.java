package com.krzysiudan.ourshoppinglist.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.krzysiudan.ourshoppinglist.Activities.RegisterActivity;
import com.krzysiudan.ourshoppinglist.Adapters.AdapterBoughtItemList;
import com.krzysiudan.ourshoppinglist.R;
import com.krzysiudan.ourshoppinglist.DatabaseItems.SingleItem;

public class FragmentBoughtItems extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private ListView mListView;
    private DatabaseReference mDatabaseReference;
    private AdapterBoughtItemList mListAdapter;
    private String mUsername;
    private String motherListName;



    public static FragmentBoughtItems newInstance(String text) {
        FragmentBoughtItems f = new FragmentBoughtItems();
        Bundle args = new Bundle();
        args.putString("MOTHERLISTNAME",text);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabaseReference= FirebaseDatabase.getInstance().getReference();

        //SharedPreferences mPreferences = this.getActivity().getSharedPreferences(ListActivity.DATA, Context.MODE_PRIVATE);
        //motherListName = mPreferences.getString(ListActivity.MOTHER_NAME,null);

        SharedPreferences prefs = this.getActivity().getSharedPreferences(RegisterActivity.LIST_PREFS, Context.MODE_PRIVATE);
        mUsername = prefs.getString(RegisterActivity.DISPLAY_NAME_KEY,null);
        Log.e("OurShoppingList","DISPLAY NAME: " + mUsername);

        if(getArguments()!=null){
            Bundle b = this.getArguments();
            motherListName = b.getString("MOTHERLISTNAME");

        }

        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bought_items, container, false);

        mListView = (ListView) view.findViewById(R.id.list_view);



        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mListAdapter = new AdapterBoughtItemList(getActivity(),mDatabaseReference,motherListName,
                mUsername);
        mListView.setAdapter(mListAdapter);


        mListView.setClickable(true);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String newItem = mListAdapter.getItem(i).getName();
                DatabaseReference boughtItemRef = mDatabaseReference.child("ShoppingLists")
                        .child(motherListName).child("PlannedItems");
                boughtItemRef.push().setValue(new SingleItem(newItem,mUsername));
                mListAdapter.removeItem(i);
                Toast.makeText(getContext(), R.string.toast_when_item_clicked_bought_tab,
                        Toast.LENGTH_SHORT).show();

            }
        });


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.items_bought_menu,menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.delete_items:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.dialog_in_bought_items);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListAdapter.removeAllItems();
                        Log.e("OurShoppingList","Button to remove all clicked");

                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

}
