package com.krzysiudan.ourshoppinglist;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdapterPlannedItemList extends BaseAdapter {


    private Activity mActivity;
    private DatabaseReference mDatabaseReferenceLists;
    private DatabaseReference mDatabaseReferenceItems;
    private String mDisplayName;
    private ArrayList<DataSnapshot> mSnapshotList;
    private String mMotherList;
    private String key;
    private EditText rowEditText;
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

    public AdapterPlannedItemList(Activity activity, DatabaseReference ref, String motherList, String name){
        mActivity = activity;
        mDisplayName = name;
        mMotherList = motherList;
        mDatabaseReferenceItems = ref.child("ShoppingLists").child(motherList).child(
                "PlannedItems");
        mDatabaseReferenceItems.addChildEventListener(mListener);


        mSnapshotList = new ArrayList<>();
    }

    static class ViewHolder{
        EditText body;
        ImageButton mImageButton;
        LinearLayout.LayoutParams params;
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
    public View getView( int i, View view, ViewGroup viewGroup) {
        if(view==null){
            LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.planned_items_row, viewGroup,false);
            final ViewHolder holder = new ViewHolder();
            holder.body = (EditText) view.findViewById(R.id.single_list);
            rowEditText= holder.body;
            holder.mImageButton = (ImageButton) view.findViewById(R.id.imageButton3) ;
            view.setTag(holder);
        }

        final SingleItem singleItem = getItem(i);
        final ViewHolder holder =(ViewHolder) view.getTag();
        final String itemName = singleItem.getName();
        holder.body.setText(itemName);
        holder.mImageButton.setTag(i);
        position = i;

        holder.mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final PopupMenu popup = new PopupMenu(mActivity,holder.mImageButton);
                popup.getMenuInflater().inflate(R.menu.popup,popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int option = menuItem.getItemId();
                        switch (option){
                            case R.id.menu_rename:

                                final String old_name = holder.body.getText().toString();

                                Log.e("OurShoppingList","Options item clicked: rename");
                                holder.body.setEnabled(true);
                                holder.body.setClickable(true);
                                holder.body.setFocusable(true);
                                holder.body.requestFocus();
                                holder.body.setSelection(holder.body.getText().toString().length());
                                mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                InputMethodManager immm = (InputMethodManager) mActivity
                                        .getSystemService(mActivity.INPUT_METHOD_SERVICE);

                                immm.showSoftInput(holder.body,InputMethodManager.SHOW_IMPLICIT);
                                holder.body.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                                    @Override
                                    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                                        if(i==EditorInfo.IME_ACTION_DONE){
                                            int position = (Integer)holder.mImageButton.getTag();
                                            Log.e("OurShoppingList","Changes 2");
                                            Log.e("OurShoppingList",
                                                    "Valuuuuue i:"+position);
                                            //what happens after input in edittext
                                            InputMethodManager imm = (InputMethodManager)
                                                    mActivity.getSystemService((Context
                                                            .INPUT_METHOD_SERVICE));
                                            imm.hideSoftInputFromWindow(textView.getWindowToken(),0);
                                            holder.body.setInputType(InputType.TYPE_NULL);
                                            holder.body.setEnabled(false);
                                            String itemName = holder.body.getText().toString();

                                            key= mSnapshotList.get(position).getKey();
                                            Log.e("OurShoppingList","Key is: "+key);

                                            Map<String, Object> listUpdate = new HashMap<>();
                                            listUpdate.put(key,new SingleItem(itemName, mDisplayName));
                                            mDatabaseReferenceItems.updateChildren(listUpdate);


                                            Log.e("OurShoppingList","Changes to listname");

                                            return true;
                                        }
                                        return false;
                                    }
                                });
                                return true;
                            case R.id.menu_remove:

                                int position = (Integer)holder.mImageButton.getTag();
                                removeItem(position);
                            default:
                                return false;
                        }
                    }
                });
                popup.show();
            }
        });


        return view;
    }

    public void removeItem (int position){
        key= mSnapshotList.get(position).getKey();

        mDatabaseReferenceItems.child(key).removeValue();
        mSnapshotList.remove(position);
        notifyDataSetChanged();

    }





    public void cleanUp(){
        mDatabaseReferenceLists.removeEventListener(mListener);
    }

}
