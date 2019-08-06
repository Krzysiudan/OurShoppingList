package com.krzysiudan.ourshoppinglist.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.krzysiudan.ourshoppinglist.DatabaseItems.SingleItem;
import com.krzysiudan.ourshoppinglist.R;

import java.util.ArrayList;

public class RecyclerAdapterBoughtItemsList extends RecyclerView.Adapter<RecyclerAdapterBoughtItemsList.ViewHolder> {



    private ArrayList<DataSnapshot> mSnapshotList;
    private Activity mActivity;
    private DatabaseReference mDatabaseReferenceItems;
    private DatabaseReference mDatabaseReferenceLists;
    private String mDisplayName;
    private String mMotherList;
    private String key;
    private int position;
    private Context context;

    public RecyclerAdapterBoughtItemsList ( Activity activity, String name, String motherList, DatabaseReference ref){
        mSnapshotList =new ArrayList<>();
        mActivity = activity;
        mDisplayName = name;
        mMotherList = motherList;
        mDatabaseReferenceLists = ref.child("ShoppingLists");
        mDatabaseReferenceItems = ref.child("ShoppingLists").child(motherList).child(
                "BoughtItems");
        mDatabaseReferenceItems.addChildEventListener(mListener);

    }

    public RecyclerAdapterBoughtItemsList(){

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView textView;


        public ViewHolder (View itemView){
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.single_list);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            String newItem = getItem(getAdapterPosition()).getName();
            DatabaseReference boughtItemRef = mDatabaseReferenceLists.child(mMotherList).child("PlannedItems");
            boughtItemRef.push().setValue(new SingleItem(newItem,mDisplayName));
            removeItem(getAdapterPosition());
            notifyItemRemoved(getAdapterPosition());

        }
    }


    private ChildEventListener mListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            mSnapshotList.add(dataSnapshot);
            notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            mSnapshotList.remove(dataSnapshot);
            notifyDataSetChanged();
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    };

    @NonNull
    @Override
    public RecyclerAdapterBoughtItemsList.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater  = LayoutInflater.from(context);

        View boughtItemView = inflater.inflate(R.layout.bought_items_row, viewGroup, false);

        RecyclerAdapterBoughtItemsList.ViewHolder viewHolder = new RecyclerAdapterBoughtItemsList.ViewHolder(boughtItemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapterBoughtItemsList.ViewHolder viewHolder, int i) {
        DataSnapshot snapshot = mSnapshotList.get(i);
        SingleItem singleItem = snapshot.getValue(SingleItem.class);

        final TextView textView = viewHolder.textView;
        textView.setText(singleItem.getName());

        final String itemName = singleItem.getName();


    }

    public void removeItem (int position){
        key= mSnapshotList.get(position).getKey();
        mDatabaseReferenceItems.child(key).removeValue();
        mSnapshotList.remove(position);
        notifyItemRemoved(position);

    }

    public void removeAllItems(){
        int size = mSnapshotList.size();
        Log.e("OurShoppingList","Size of mSnapshotList: "+size);
        for (int i = 0; i<size; i++){
            removeItem(0);
        }
    }

    public void cleanUp(){
        mDatabaseReferenceItems.removeEventListener(mListener);
    }


    @Override
    public int getItemCount() {
        return mSnapshotList.size();
    }

    public SingleItem getItem(int i) {
        DataSnapshot snapshot = mSnapshotList.get(i);
        return snapshot.getValue(SingleItem.class);
    }


}
