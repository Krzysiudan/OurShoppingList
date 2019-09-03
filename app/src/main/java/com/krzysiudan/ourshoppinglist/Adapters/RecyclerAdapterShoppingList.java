package com.krzysiudan.ourshoppinglist.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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


import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.krzysiudan.ourshoppinglist.Activities.ActivityMainItems;
import com.krzysiudan.ourshoppinglist.DatabaseItems.ShoppingList;
import com.krzysiudan.ourshoppinglist.Interfaces.RecyclerViewClickListener;
import com.krzysiudan.ourshoppinglist.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class RecyclerAdapterShoppingList extends RecyclerView.Adapter<RecyclerAdapterShoppingList.ViewHolder> {

    public static final String TAG = "OurShoppingList";

    private Activity mActivity;
    private FirebaseFirestore mFirestore;
    private CollectionReference collectionReference;
    private String mDisplayName;
    private ArrayList<QueryDocumentSnapshot> mSnapshotList;
    private Context context;
    private static RecyclerViewClickListener itemListener;

    public RecyclerAdapterShoppingList(Activity activity, FirebaseFirestore mFirestore, String name, final Context context) {
        mActivity = activity;
        mDisplayName = name;
        this.mFirestore = mFirestore;

        mSnapshotList = new ArrayList<>();
        this.context = context;
        collectionReference = mFirestore.collection("ShoppingLists");



        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                Log.w(TAG,"SnapshotListener is working");
                if(e != null) {
                    Toast.makeText(context,"Error while loading",Toast.LENGTH_SHORT);
                    Log.w(TAG,"Listener on ShoppingLists error:",e);
                    return;
                }


                if(!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                Log.w(TAG, "New shoppinglist ADDED" + dc.getDocument().getData());
                                mSnapshotList.add(dc.getDocument());
                                notifyItemInserted(mSnapshotList.size());
                                //TODO change notifyDataSetCHanged to less global solution
                                break;
                            case MODIFIED:
                                Log.w(TAG, "Shoppinglist MODIFIED: " + dc.getDocument().getData());
                                break;
                            case REMOVED:
                                Log.w(TAG, "Shoppinglist REMOVED" + dc.getDocument().getData());
                                int position = mSnapshotList.indexOf(dc.getDocument());
                                mSnapshotList.remove(dc.getDocument());
                                notifyItemRemoved(position);
                                //TODO change notifyDataSetCHanged to less global solution
                                break;
                        }
                    }
                }
            }
        });



    }




    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public EditText nameTextView;
        public ImageButton mImageButton;
        Context context;
        ArrayList<QueryDocumentSnapshot> mSnapshotList;

        public ViewHolder(View itemView, Context context, ArrayList<QueryDocumentSnapshot> msnapshotlist) {
            super(itemView);

            nameTextView = (EditText) itemView.findViewById(R.id.single_list);
            mImageButton = (ImageButton) itemView.findViewById(R.id.imageButton3);
            this.context = context;
            this.mSnapshotList = msnapshotlist;
            itemView.setOnClickListener(this);

        }


        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context,ActivityMainItems.class);
            String motherListKey = mSnapshotList.get(getAdapterPosition()).getId();
            Log.e("OurShoppingList","Mother LIst name: "+motherListKey);
            intent.putExtra("MotherListName", motherListKey);
            //cleanUp();
            context.startActivity(intent);
        }
    }


    @NonNull
    @Override
    public RecyclerAdapterShoppingList.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View shoppingView = inflater.inflate(R.layout.list_row, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(shoppingView, context,mSnapshotList);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapterShoppingList.ViewHolder viewHolder, final int i) {
        QueryDocumentSnapshot snapshot= mSnapshotList.get(i);
        final EditText editText = viewHolder.nameTextView;
        final ImageButton imageButton = viewHolder.mImageButton;
        if(snapshot.exists()){
            ShoppingList shoppingList = snapshot.toObject(ShoppingList.class);
            Log.e("OurShoppingList", "ShoppingList name:" +shoppingList.getList_name());
            editText.setText(shoppingList.getList_name());
        }



        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final PopupMenu popup = new PopupMenu(mActivity, imageButton);
                popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int option = menuItem.getItemId();

                        switch (option) {
                            case R.id.menu_rename:
                                final String old_list = editText.getText().toString();
                                Log.e("OurShoppingList", "Options item clicked: rename");
                                editText.setEnabled(true);
                                editText.setInputType(InputType.TYPE_CLASS_TEXT);

                                editText.setFocusableInTouchMode(true);
                                editText.requestFocus();
                                editText.setSelection(editText.getText().toString().length());
                                mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                InputMethodManager immm = (InputMethodManager) mActivity.getSystemService(mActivity.INPUT_METHOD_SERVICE);
                                immm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

                                editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                                    @Override
                                    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                                        if (i == EditorInfo.IME_ACTION_DONE) {
                                            Log.e("OurShoppingList", "Changes 2");
                                            //what happens after input in edittext
                                            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService((Context.INPUT_METHOD_SERVICE));
                                            imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                                            editText.setFocusable(false);
                                            editText.setInputType(InputType.TYPE_NULL);
                                            editText.setEnabled(false);
                                            editText.setClickable(false);

                                            final String listname = editText.getText().toString();

                                           // changeName(old_list, listname);
                                            Log.e("OurShoppingList", "Changes to listname");
                                            return true;
                                        }
                                        return false;
                                    }
                                });
                                return true;
                            case R.id.menu_remove:
                               // removeList(editText.getText().toString());
                                mSnapshotList.remove(i);
                                notifyDataSetChanged();
                                Log.e("OurShoppingList", "Options item clicked: remove");

                            default:
                                return false;
                        }
                    }
                });
                popup.show();


            }

            public void cleanUp() {
                //TODO detach listener
            }

           /* private void changeName(String oldName, String newName) {
                final String new_name = newName;
                mDatabaseReference.orderByChild("list_name")
                        .equalTo(oldName)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                    Log.e("OurShoppingList", "Changes in " + "OnDataChange");
                                    String listkey = childSnapshot.getKey();
                                    Map<String, Object> listUpdate = new HashMap<>();
                                    listUpdate.put(listkey, new ShoppingList(new_name));
                                    mDatabaseReference.updateChildren(listUpdate);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
            }

            private void removeList(String name) {
                mDatabaseReference.orderByChild("list_name")
                        .equalTo(name)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot
                                        childSnapshot : dataSnapshot.getChildren()) {
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
        });


    }*/


        });
    };

    @Override
    public int getItemCount() {
        return mSnapshotList.size();
    }

    public ShoppingList getItem(int position) {
        return mSnapshotList.get(position).toObject(ShoppingList.class);
    }
}
