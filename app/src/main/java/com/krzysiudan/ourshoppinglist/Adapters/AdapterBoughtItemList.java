package com.krzysiudan.ourshoppinglist.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.krzysiudan.ourshoppinglist.R;
import com.krzysiudan.ourshoppinglist.DatabaseItems.SingleItem;

import java.util.ArrayList;

public class AdapterBoughtItemList extends BaseAdapter {
    private Activity mActivity;

    private DatabaseReference mDatabaseReferenceItems;
    private String mDisplayName;
    private ArrayList<DataSnapshot> mSnapshotList;
    private String mMotherList;
    private String key;
    private TextView rowEditText;
    private int position;

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

    public AdapterBoughtItemList(Activity activity, DatabaseReference ref, String motherList,
                            String name){
        mActivity = activity;
        mDisplayName = name;
        mMotherList = motherList;
        mDatabaseReferenceItems = ref.child("ShoppingLists").child(motherList).child("BoughtItems");
        mDatabaseReferenceItems.addChildEventListener(mListener);


        mSnapshotList = new ArrayList<>();
    }

    static class ViewHolder{
        TextView body;

    }

    @Override
    public int getCount() {
        return mSnapshotList.size();
    }

    @Override
    public SingleItem getItem(int i) {
        DataSnapshot snapshot = mSnapshotList.get(i);
        return snapshot.getValue(SingleItem.class);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view==null){
            LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.bought_items_row, viewGroup,false);
            final AdapterBoughtItemList.ViewHolder holder = new AdapterBoughtItemList.ViewHolder();
            holder.body = (TextView) view.findViewById(R.id.single_list);
            rowEditText= holder.body;
            view.setTag(holder);
        }

        final SingleItem singleItem = getItem(i);
        final AdapterBoughtItemList.ViewHolder holder =
                (AdapterBoughtItemList.ViewHolder) view.getTag();
        final String itemName = singleItem.getName();
        holder.body.setText(itemName);
        position = i;

        return view;
    }

    public void removeItem (int position){
        key= mSnapshotList.get(position).getKey();

        mDatabaseReferenceItems.child(key).removeValue();
        mSnapshotList.remove(position);
        notifyDataSetChanged();

    }

    public void removeAllItems(){
        mDatabaseReferenceItems.removeValue();
        mSnapshotList.clear();
        notifyDataSetChanged();
    }

    public void cleanUp(){
        mDatabaseReferenceItems.removeEventListener(mListener);
    }

}


