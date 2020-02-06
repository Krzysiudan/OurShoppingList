package com.krzysiudan.ourshoppinglist.Fragments;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.krzysiudan.ourshoppinglist.Adapters.RecyclerAdapterPlannedItemList;
import com.krzysiudan.ourshoppinglist.Fragments.Dialogs.DialogAddList;
import com.krzysiudan.ourshoppinglist.Fragments.Dialogs.DialogNotification;
import com.krzysiudan.ourshoppinglist.Fragments.Dialogs.DialogShareList;
import com.krzysiudan.ourshoppinglist.R;
import com.krzysiudan.ourshoppinglist.Models.SingleItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentPlannedItems extends Fragment {

    private RecyclerView mRecyclerView;
    private TextInputEditText addItemEditText;
    private FloatingActionButton buttonNotificate;

    private DatabaseReference mDatabaseReference;
    private RecyclerAdapterPlannedItemList mRecyclerAdapter;
    private String mUsername;
    private String motherListKey;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mFirebaseAuth;

    public static final String ARG_PAGE = "ARG_PAGE";
    public static final String TAG ="FragmentPlannedItemsLog";

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
        ButterKnife.bind((AppCompatActivity)getActivity());

        if(getArguments()!=null){
            Bundle b = this.getArguments();
            motherListKey = b.getString("MOTHERLISTKEY");

        }
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_planned_items, container, false);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        addItemEditText = view.findViewById(R.id.text_input_edit);
        buttonNotificate = view.findViewById(R.id.floatingActionButtonNotification);

        buttonNotificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification();
            }
        });

        return view;
    }

    private  void sendNotification(){
        Bundle mBundle = new Bundle();
        mBundle.putString("key",motherListKey);

        FragmentManager fm = ((AppCompatActivity)getActivity()).getSupportFragmentManager();
        FragmentTransaction mFragmentTransaction = fm.beginTransaction();
        Fragment old = fm.findFragmentByTag("Notification_Dialog");

        if(old instanceof DialogNotification){
            Log.d(TAG,"Fragment already exist");
            mFragmentTransaction.remove(old);
        }
        DialogNotification dialog = DialogNotification.newInstance();
        dialog.setArguments(mBundle);
        mFragmentTransaction.addToBackStack(null);
        dialog.show(mFragmentTransaction,"Notification_Dialog");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();
        mRecyclerAdapter = new RecyclerAdapterPlannedItemList(getActivity(),motherListKey);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setClickable(true);
        final CollectionReference mCollectionReferencePlanned =mFirestore.collection("ShoppingLists").document(motherListKey).collection("Planned");

        addItemEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i== EditorInfo.IME_ACTION_DONE){
                    final String newItem = addItemEditText.getText().toString();
                    if(!newItem.equals("")){
                        SingleItem mSingleItem = new SingleItem();
                        mSingleItem.setAuthor(mFirebaseAuth.getCurrentUser().getDisplayName());
                        mSingleItem.setName(newItem);
                        mCollectionReferencePlanned.document(newItem).set(mSingleItem)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d(TAG,"New item added: "+newItem);
                            }
                        });
                        addItemEditText.setText("");
                    }
                }
                return false;
            }
        });
    }
    }

