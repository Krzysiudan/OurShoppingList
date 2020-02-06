package com.krzysiudan.ourshoppinglist.Adapters;

import android.app.Activity;
import android.content.Context;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.krzysiudan.ourshoppinglist.Models.SingleItem;
import com.krzysiudan.ourshoppinglist.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecyclerAdapterPlannedItemList extends RecyclerView.Adapter<RecyclerAdapterPlannedItemList.ViewHolder> {

    public static final String TAG = "OurShoppingList";

    private ArrayList<QueryDocumentSnapshot> mSnapshotList;
    private Activity mActivity;
    private FirebaseFirestore mFirestore;
    private CollectionReference mCollectionReferenceBought;
    private CollectionReference mCollectionReferencePlanned;
    private String mDisplayName;
    private String mMotherListKey;
    private String key;
    private int position;
    private Context context;

    public RecyclerAdapterPlannedItemList ( Activity activity,  String motherListKey){
        mSnapshotList =new ArrayList<>();
        mActivity = activity;
        this.mFirestore = FirebaseFirestore.getInstance();
        mMotherListKey = motherListKey;
        Log.e(TAG,"MotherListKey in PlannedItems:"+mMotherListKey);

        mCollectionReferenceBought = mFirestore.collection("ShoppingLists").document(mMotherListKey).collection("Bought");
        mCollectionReferencePlanned =mFirestore.collection("ShoppingLists").document(mMotherListKey).collection("Planned");

        mCollectionReferencePlanned.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if(e != null) {
                    Log.w(TAG,"Listener on PlannedItems error:",e);
                    return;
                }

                for(DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()){
                    switch (dc.getType()){
                        case ADDED:
                            Log.e(TAG,"New sign in planned item added" + dc.getDocument().getData());
                            mSnapshotList.add(dc.getDocument());
                            notifyItemInserted(mSnapshotList.size()-1);
                            //TODO change notifyDataSetCHanged to less global solution
                            break;
                        case MODIFIED:
                            Log.e(TAG,"Shoppinglist edited " + dc.getDocument().getData());
                            break;
                        case REMOVED:
                            Log.e(TAG,"Shoppinglist removed" + dc.getDocument().getData());
                            mSnapshotList.remove(dc.getDocument());
                            notifyItemRemoved(mSnapshotList.size()-1);
                            //TODO change notifyDataSetCHanged to less global solution
                            break;
                    }
                }
            }
        });

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
            SingleItem item = getItem(getAdapterPosition());
            final String name = item.getName();
            final String key = mSnapshotList.get(getAdapterPosition()).getId();
            mCollectionReferenceBought.document(name).set(item);
            mCollectionReferencePlanned.document(name)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.e(TAG,"Item moved to bought, item in planned removed" + name);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG,"Deleting item failed" + name);
                        }
                    });
            removeItem(getAdapterPosition());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater  = LayoutInflater.from(context);

        View plannedItemView = inflater.inflate(R.layout.planned_items_row, viewGroup, false);

        return new ViewHolder(plannedItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapterPlannedItemList.ViewHolder viewHolder, int i) {

        QueryDocumentSnapshot snapshot = mSnapshotList.get(i);

        final EditText editText = viewHolder.editText;
        final ImageButton imageButton = viewHolder.button;

        if(snapshot.exists()){
            SingleItem mSingleItem = snapshot.toObject(SingleItem.class);
            Log.e("OurShoppingList", "ShoppingList name:" +mSingleItem.getName());
            final String itemName = mSingleItem.getName();
            editText.setText(mSingleItem.getName());
        }
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

                                            key= mSnapshotList.get(position).toObject(SingleItem.class).getName();
                                            Log.e("OurShoppingList","Key is: "+key);

                                            Map<String, Object> listUpdate = new HashMap<>();
                                            listUpdate.put(key,new SingleItem(itemName, mDisplayName,null));
                                           // mDatabaseReferenceItems.updateChildren(listUpdate);


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
        key= mSnapshotList.get(position).toObject(SingleItem.class).getName();
        mCollectionReferencePlanned.document(key)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
        mSnapshotList.remove(position);
        notifyItemRemoved(position);

    }
/* TODO detach listener
   public void cleanUp(){
    }
*/

    @Override
    public int getItemCount() {
        return mSnapshotList.size();
    }

    public SingleItem getItem(int i) {
        QueryDocumentSnapshot snapshot = mSnapshotList.get(i);
        return snapshot.toObject(SingleItem.class);
    }




}
