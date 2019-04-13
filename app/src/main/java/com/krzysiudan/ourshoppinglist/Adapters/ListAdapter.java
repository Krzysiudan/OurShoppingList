package com.krzysiudan.ourshoppinglist.Adapters;

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
import com.google.firebase.database.ValueEventListener;
import com.krzysiudan.ourshoppinglist.R;
import com.krzysiudan.ourshoppinglist.DatabaseItems.ShoppingList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListAdapter extends BaseAdapter {

    private Activity mActivity;
    private DatabaseReference mDatabaseReference;
    private String mDisplayName;
    private ArrayList<DataSnapshot> mSnapshotList;

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

    public ListAdapter(Activity activity, DatabaseReference ref, String name){
        mActivity = activity;
        mDisplayName = name;
        mDatabaseReference = ref.child("ShoppingLists");
        mDatabaseReference.addChildEventListener(mListener);
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
    public ShoppingList getItem(int i) {
        DataSnapshot snapshot = mSnapshotList.get(i);
        return snapshot.getValue(ShoppingList.class);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if(view==null){
            LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view =inflater.inflate(R.layout.list_row, viewGroup,false);
            final ViewHolder holder = new ViewHolder();
            holder.body = (EditText) view.findViewById(R.id.single_list);
            holder.mImageButton = (ImageButton) view.findViewById(R.id.imageButton3) ;
            view.setTag(holder);
        }
        final ShoppingList list = getItem(i);
        final ViewHolder holder = (ViewHolder) view.getTag();
        final String list_name = list.getList_name();
        holder.body.setText(list_name);

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
                                final String old_list = holder.body.getText().toString();
                                Log.e("OurShoppingList","Options item clicked: rename");
                                holder.body.setEnabled(true);
                                holder.body.setInputType(InputType.TYPE_CLASS_TEXT);

                                holder.body.setFocusableInTouchMode(true);
                                holder.body.requestFocus();
                                holder.body.setSelection(holder.body.getText().toString().length());
                                mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                InputMethodManager immm = (InputMethodManager) mActivity.getSystemService(mActivity.INPUT_METHOD_SERVICE);
                                immm.showSoftInput(holder.body,InputMethodManager.SHOW_IMPLICIT);

                                holder.body.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                                    @Override
                                    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                                            if(i==EditorInfo.IME_ACTION_DONE){
                                                Log.e("OurShoppingList","Changes 2");
                                                //what happens after input in edittext
                                                InputMethodManager imm = (InputMethodManager) mActivity.getSystemService((Context.INPUT_METHOD_SERVICE));
                                                imm.hideSoftInputFromWindow(textView.getWindowToken(),0);
                                                holder.body.setFocusable(false);
                                               holder.body.setInputType(InputType.TYPE_NULL);
                                               holder.body.setEnabled(false);
                                               holder.body.setClickable(false);

                                               final String listname = holder.body.getText().toString();

                                               changeName(old_list,listname);
                                               Log.e("OurShoppingList","Changes to listname");
                                                return true;
                                            }
                                        return false;
                                    }
                                });
                                return true;
                            case R.id.menu_remove:
                                removeList(holder.body.getText().toString());
                                mSnapshotList.remove(i);
                                notifyDataSetChanged();
                                Log.e("OurShoppingList","Options item clicked: remove");

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

    public void cleanUp(){
        mDatabaseReference.removeEventListener(mListener);
    }

    private void changeName (String oldName, String newName){
        final String new_name = newName;
        mDatabaseReference.orderByChild("list_name")
                .equalTo(oldName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot childSnapshot: dataSnapshot.getChildren()){
                            Log.e("OurShoppingList", "Changes in " + "OnDataChange");
                            String listkey = childSnapshot.getKey();
                            Map<String, Object> listUpdate = new HashMap<>();
                            listUpdate.put(listkey,new ShoppingList(new_name));
                            mDatabaseReference.updateChildren(listUpdate);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    private void removeList(String name){
        mDatabaseReference.orderByChild("list_name")
                .equalTo(name)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot
                                childSnapshot: dataSnapshot.getChildren()){
                            Log.e("OurShoppingList", "Removed");
                            String listkey = childSnapshot.getKey();
                            mDatabaseReference.child(listkey).removeValue();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }
}


