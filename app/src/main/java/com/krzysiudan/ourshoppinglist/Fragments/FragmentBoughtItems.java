package com.krzysiudan.ourshoppinglist.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.krzysiudan.ourshoppinglist.Activities.RegisterActivity;
import com.krzysiudan.ourshoppinglist.Adapters.RecyclerAdapterBoughtItemsList;
import com.krzysiudan.ourshoppinglist.R;

public class FragmentBoughtItems extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private RecyclerView mRecyclerView;
    private DatabaseReference mDatabaseReference;
    private RecyclerAdapterBoughtItemsList mRecyclerAdapter;
    private String mUsername;
    private String motherListKey;
    private FirebaseFirestore mFirestore;



    public static FragmentBoughtItems newInstance(String text) {
        FragmentBoughtItems f = new FragmentBoughtItems();
        Bundle args = new Bundle();
        args.putString("MOTHERLISTKEY",text);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabaseReference= FirebaseDatabase.getInstance().getReference();
        mFirestore = FirebaseFirestore.getInstance();

        //SharedPreferences mPreferences = this.getActivity().getSharedPreferences(ListActivity.DATA, Context.MODE_PRIVATE);
        //motherListName = mPreferences.getString(ListActivity.MOTHER_NAME,null);

        SharedPreferences prefs = this.getActivity().getSharedPreferences(RegisterActivity.LIST_PREFS, Context.MODE_PRIVATE);
        mUsername = prefs.getString(RegisterActivity.DISPLAY_NAME_KEY,null);
        Log.e("OurShoppingList","DISPLAY NAME: " + mUsername);

        if(getArguments()!=null){
            Bundle b = this.getArguments();
            motherListKey = b.getString("MOTHERLISTKEY");

        }

        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bought_items, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);



        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mRecyclerAdapter = new RecyclerAdapterBoughtItemsList(getActivity(),motherListKey, mFirestore);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setClickable(true);


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
                        mRecyclerAdapter.removeAllItems();
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
