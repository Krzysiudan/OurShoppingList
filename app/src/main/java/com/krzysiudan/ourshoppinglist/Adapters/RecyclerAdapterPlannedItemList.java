package com.krzysiudan.ourshoppinglist.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.krzysiudan.ourshoppinglist.DatabaseItems.SingleItem;
import com.krzysiudan.ourshoppinglist.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecyclerAdapterPlannedItemList extends RecyclerView.Adapter<RecyclerAdapterPlannedItemList.ViewHolder> {

    private ArrayList<DataSnapshot> mSnapshotList;
    private Activity mActivity;
    private DatabaseReference mDatabaseReferenceItems;
    private DatabaseReference mDatabaseReferenceLists;
    private String mDisplayName;
    private String mMotherList;
    private String key;
    private int position;
    private Context context;

    public RecyclerAdapterPlannedItemList ( Activity activity, String name, String motherList, DatabaseReference ref){
        mSnapshotList =new ArrayList<>();
        mActivity = activity;
        mDisplayName = name;
        mMotherList = motherList;
        mDatabaseReferenceLists = ref.child("ShoppingLists");
        mDatabaseReferenceItems = ref.child("ShoppingLists").child(motherList).child(
                "PlannedItems");
        mDatabaseReferenceItems.addChildEventListener(mListener);

    }

    public RecyclerAdapterPlannedItemList(){

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public EditText editText;
        public ImageButton button;


        public ViewHolder (View itemView){
            super(itemView);

            editText = (EditText) itemView.findViewById(R.id.single_list);
            button = (ImageButton) itemView.findViewById(R.id.imageButton3);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            String newItemName = getItem(getAdapterPosition()).getName();
            DatabaseReference boughtItemRef = mDatabaseReferenceLists.child(mMotherList).child("BoughtItems");
            boughtItemRef.push().setValue(new SingleItem(newItemName,mDisplayName));
            removeItem(getAdapterPosition());

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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater  = LayoutInflater.from(context);

        View plannedItemView = inflater.inflate(R.layout.planned_items_row, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(plannedItemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapterPlannedItemList.ViewHolder viewHolder, int i) {
        DataSnapshot snapshot = mSnapshotList.get(i);
        SingleItem singleItem = snapshot.getValue(SingleItem.class);

        final EditText editText = viewHolder.editText;
        editText.setText(singleItem.getName());

        final String itemName = singleItem.getName();

        final ImageButton imageButton = viewHolder.button;
        imageButton.setTag(i);
        position = i;


        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final PopupMenu popup = new PopupMenu(mActivity,imageButton);
                popup.getMenuInflater().inflate(R.menu.popup,popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int option = menuItem.getItemId();
                        switch (option){
                            case R.id.menu_rename:

                                final String old_name =editText.getText().toString();

                                Log.e("OurShoppingList","Options item clicked: rename");
                                editText.setEnabled(true);
                                editText.setClickable(true);
                                editText.setFocusable(true);
                                editText.requestFocus();
                                editText.setSelection(editText.getText().toString().length());
                                mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                InputMethodManager immm = (InputMethodManager) mActivity
                                        .getSystemService(mActivity.INPUT_METHOD_SERVICE);

                                immm.showSoftInput(editText,InputMethodManager.SHOW_IMPLICIT);
                                editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                                    @Override
                                    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                                        if(i== EditorInfo.IME_ACTION_DONE){
                                            int position = (Integer)imageButton.getTag();
                                            Log.e("OurShoppingList","Changes 2");
                                            Log.e("OurShoppingList",
                                                    "Valuuuuue i:"+position);
                                            //what happens after input in edittext
                                            InputMethodManager imm = (InputMethodManager)
                                                    mActivity.getSystemService((Context
                                                            .INPUT_METHOD_SERVICE));
                                            imm.hideSoftInputFromWindow(textView.getWindowToken(),0);
                                            editText.setInputType(InputType.TYPE_NULL);
                                            editText.setEnabled(false);
                                            String itemName = editText.getText().toString();

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

                                int position = (Integer)imageButton.getTag();
                                removeItem(position);
                            default:
                                return false;
                        }
                    }
                });
                popup.show();
            }
        });

    }

    public void removeItem (int position){
        key= mSnapshotList.get(position).getKey();

        mDatabaseReferenceItems.child(key).removeValue();
        mSnapshotList.remove(position);
        notifyDataSetChanged();

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
