package com.krzysiudan.ourshoppinglist.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputEditText;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.krzysiudan.ourshoppinglist.Activities.RegisterActivity;
import com.krzysiudan.ourshoppinglist.Adapters.RecyclerAdapterPlannedItemList;
import com.krzysiudan.ourshoppinglist.R;
import com.krzysiudan.ourshoppinglist.DatabaseItems.SingleItem;

import java.util.HashMap;
import java.util.Map;

public class FragmentPlannedItems extends Fragment {

    private RecyclerView mRecyclerView;
    private DatabaseReference mDatabaseReference;
    private RecyclerAdapterPlannedItemList mRecyclerAdapter;
    private String mUsername;
    private TextInputEditText addItemEditText;
    private String motherListKey;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mFirebaseAuth;

    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    public static FragmentPlannedItems newInstance(String text) {
        FragmentPlannedItems f = new FragmentPlannedItems();
        Bundle args = new Bundle();
        args.putString("MOTHERLISTKEY", text);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseReference= FirebaseDatabase.getInstance().getReference();
        mFirestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        //SharedPreferences mPreferences = this.getActivity().getSharedPreferences(ListActivity.DATA, Context.MODE_PRIVATE);
        //motherListName = mPreferences.getString(ListActivity.MOTHER_NAME,null);

        SharedPreferences prefs = this.getActivity().getSharedPreferences(RegisterActivity.LIST_PREFS,Context.MODE_PRIVATE);
        mUsername = prefs.getString(RegisterActivity.DISPLAY_NAME_KEY,null);
        Log.e("OurShoppingList","DISPLAY NAME: " + mUsername);

        if(getArguments()!=null){
            Bundle b = this.getArguments();
            motherListKey = b.getString("MOTHERLISTKEY");

        }

        Log.e("OurShoppingList","MOTHER:" + motherListKey);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_planned_items, container, false);

        addItemEditText = (TextInputEditText) view.findViewById(R.id.text_input_edit);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);



    }

    @Override
    public void onStart() {
        super.onStart();
        mRecyclerAdapter = new RecyclerAdapterPlannedItemList(getActivity(),motherListKey,mFirestore);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setClickable(true);
        final CollectionReference mCollectionReferencePlanned =mFirestore.collection("ShoppingLists").document(motherListKey).collection("Planned");

        addItemEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i== EditorInfo.IME_ACTION_DONE){
                    String newItem = addItemEditText.getText().toString();
                    if(!newItem.equals("")){
                        Map<String,Object> data = new HashMap<>();
                        data.put("name",newItem);
                        data.put("author", mFirebaseAuth.getCurrentUser().getDisplayName());
                        mCollectionReferencePlanned.add(data);
                        addItemEditText.setText("");
                    }
                }
                return false;
            }
        });





    }



    }

