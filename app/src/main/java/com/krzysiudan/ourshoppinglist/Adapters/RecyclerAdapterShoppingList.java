package com.krzysiudan.ourshoppinglist.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.krzysiudan.ourshoppinglist.Activities.ActivityMainItems;
import com.krzysiudan.ourshoppinglist.Models.ShoppingList;
import com.krzysiudan.ourshoppinglist.Fragments.Dialogs.DialogChangeName;
import com.krzysiudan.ourshoppinglist.Fragments.Dialogs.DialogShareList;
import com.krzysiudan.ourshoppinglist.R;
import java.util.ArrayList;

public class RecyclerAdapterShoppingList extends RecyclerView.Adapter<RecyclerAdapterShoppingList.ViewHolder> implements  EventListener<QuerySnapshot>, DialogChangeName.OnNameInsertedListener {

    public static final String TAG = "OurShoppingList";

    private Activity mActivity;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mAuth;
    private ArrayList<DocumentSnapshot> mSnapshotList;
    private Context context;
    private ListenerRegistration mListenerRegistration;

    public RecyclerAdapterShoppingList(Activity activity) {
        mActivity = activity;
        this.mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mSnapshotList = new ArrayList<>();
        this.context = activity.getApplicationContext();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        stopListeningToChanges();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        CollectionReference collectionReference;
        collectionReference = mFirestore.collection("users/"+mFirebaseUser.getEmail()+"/usedLists");
        mListenerRegistration = collectionReference
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(this);
    }

    @Override
    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
        if(e != null) {
            Toast.makeText(context,"Error while loading",Toast.LENGTH_SHORT).show();
            Log.w(TAG,"Listener on ShoppingLists error:",e);
            return;
        }

        try {
            handleTheViewChanges(queryDocumentSnapshots);
        }catch (NullPointerException f){
            logMessage("Query Snapshot is Empty, exception : "+f);
        }
    }

    private void handleTheViewChanges(QuerySnapshot queryDocumentSnapshots){
        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
            switch (dc.getType()) {
                case ADDED:
                    Log.w(TAG, "New shoppinglist ADDED" + dc.getDocument().getData());
                    mSnapshotList.add(dc.getDocument());
                    notifyItemInserted(mSnapshotList.size());
                    break;
                case MODIFIED:
                    Log.w(TAG, "Shoppinglist MODIFIED: " + dc.getDocument().getData());
                    notifyDataSetChanged();
                    break;
                case REMOVED:
                    int position= mSnapshotList.indexOf(dc.getDocument());
                    Log.w(TAG, "Shoppinglist REMOVED" + dc.getDocument().getData()+" Position: "+position);
                    break;
            }
        }
    }




    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,PopupMenu.OnMenuItemClickListener  {
        private EditText nameEditView;
        private ImageButton mImageButtonPopupMenu;
        private LinearLayout mLinearLayout;
        Context context;
        ArrayList<DocumentSnapshot> mSnapshotList;

        private ViewHolder( View itemView, Context context, ArrayList<DocumentSnapshot> mSnapshotList) {
            super(itemView);
            mLinearLayout = itemView.findViewById(R.id.singleMessageContainer);
            nameEditView =  itemView.findViewById(R.id.single_list);
            mImageButtonPopupMenu =  itemView.findViewById(R.id.imageButton3);
            this.context = context;
            this.mSnapshotList = mSnapshotList;
            itemView.setOnClickListener(this);
            mImageButtonPopupMenu.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.singleMessageContainer:
                    startingNewActivityAfterClickingParticularRow();
                    break;
                case R.id.imageButton3:
                    createPopupMenu(v);
                    break;

            }
        }

        private void startingNewActivityAfterClickingParticularRow(){
            Intent intentStartActivityMainItems = new Intent(context,ActivityMainItems.class);
            String motherListKey = getMotherListKey();
            intentStartActivityMainItems.putExtra("MotherListName", motherListKey);
            context.startActivity(intentStartActivityMainItems);
        }

        private String getMotherListKey(){
            String motherListKey = mSnapshotList.get(getAdapterPosition()).getId();
            logMessage("Mother list key: "+motherListKey);
            return motherListKey;

        }

        private void createPopupMenu(View view){
            PopupMenu mPopupMenu = new PopupMenu(mActivity,view);
            mPopupMenu.getMenuInflater().inflate(R.menu.popup,mPopupMenu.getMenu());
            mPopupMenu.setOnMenuItemClickListener(this);
            mPopupMenu.show();

        }

        @Override
        public boolean onMenuItemClick(MenuItem item){
            int option = item.getItemId();

            switch (option) {
                case R.id.menu_rename:
                    changeName(getLayoutPosition());
                    Log.e(TAG, "Options item clicked: rename, position clicked : " +getLayoutPosition());
                    return true;
                case R.id.menu_remove:
                    removeList(getLayoutPosition());
                    Log.e(TAG, "Options item clicked: remove, position clicked : " +getLayoutPosition());
                    return true;
                case R.id.menu_add_new_user:
                    addUser(getLayoutPosition());
                    return true;
                default:
                    return false;
            }
        }
    }


    @NonNull
    @Override
    public RecyclerAdapterShoppingList.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View shoppingView = inflater.inflate(R.layout.list_row, viewGroup, false);

        return new ViewHolder(shoppingView, context, mSnapshotList);

    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerAdapterShoppingList.ViewHolder viewHolder, int i) {
        DocumentSnapshot snapshot = mSnapshotList.get(viewHolder.getAdapterPosition());
        final EditText editTextNameOfAList = viewHolder.nameEditView;

        if (snapshot.exists()) {
            String listName = getListNameFrom(snapshot);
            editTextNameOfAList.setText(listName);
        }
    }

    private String getListNameFrom(DocumentSnapshot snapshot){
        ShoppingList mShoppingList = getShoppingListFrom(snapshot);
        return getListName(mShoppingList);
    }

    private ShoppingList getShoppingListFrom(DocumentSnapshot snapshot){
        return  snapshot.toObject(ShoppingList.class);
    }

    private String getListName(ShoppingList shoppingList){
        String listName;
        try{
            listName = shoppingList.getList_name();
        }catch (NullPointerException e ){
            listName = "none";
        }
        logMessage("ShoppingList name: "+listName);
        return listName;
    }


    public int getItemCount() {
        return mSnapshotList.size();
    }

    private ShoppingList getItem(int position) {
        return mSnapshotList.get(position).toObject(ShoppingList.class);
    }

    private void removeList(final int position){
        final String key = getListKey(position);
        DocumentReference docRef = getDocumentReference(key);
        docRef.delete()
                .addOnSuccessListener(aVoid ->
                        logMessage("List with key: " + key + "has been removed on position : " +position))
                .addOnFailureListener(e ->
                        logMessage("Failure removing list with key: " + key));
                mSnapshotList.remove(position);
                notifyItemRemoved(position);
    }

    private DocumentReference getDocumentReference(String key){
        return mFirestore.document("users/"+mFirebaseUser.getEmail()+"/usedLists/"+key);
    }

    private String getListKey(int position){
        return mSnapshotList.get(position).getId();
    }

    private void changeName(final int position) {
        String key = getListKey(position);

        Bundle mBundle = new Bundle();
        mBundle.putString("key",key);
        mBundle.putInt("position",position);

        FragmentManager fm = ((AppCompatActivity)mActivity).getSupportFragmentManager();
        FragmentTransaction mFragmentTransaction = fm.beginTransaction();
        Fragment old = fm.findFragmentByTag("changeNameDialog");

        if(old instanceof DialogChangeName){
            Log.d(TAG,"Fragment already exist");
            mFragmentTransaction.remove(old);
        }
        DialogChangeName dialog = DialogChangeName.newInstance();
        dialog.setArguments(mBundle);
        mFragmentTransaction.addToBackStack(null);
        dialog.show(mFragmentTransaction,"ChangeNameDialog");
        dialog.setOnNameInsertedListener(this);


    }

    @Override
    public void onNameInserted(String name, String key, int position) {
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference mReferenceFromUser = mFirestore.document("users/"+mUser.getEmail()+"/usedLists/"+key);
        DocumentReference mReferenceFromList = mFirestore.document("ShoppingLists/"+key);
        WriteBatch mWriteBatch = mFirestore.batch();
        mWriteBatch.update(mReferenceFromUser,"list_name",name);

        mWriteBatch.update(mReferenceFromList,"list_name",name);

        mWriteBatch.commit()
                .addOnCompleteListener(task -> {
                    Log.d("TAG", "Name succesfully changed");

                        mReferenceFromList.get()
                                .addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){
                                        DocumentSnapshot doc = task1.getResult();
                                        if(doc.exists()){
                                            Log.e(TAG, "changeName method : Document Snapshot exist ");
                                            mSnapshotList.set(position,doc);
                                            notifyItemChanged(position);
                                        } else {
                                            Log.e(TAG, "changeName method : Document Snapshot not exist ");
                                        }
                                    } else {
                                        Log.e(TAG, "changeName method : get failed with : " + task1.getException());
                                    }
                                });
                })
                .addOnFailureListener(e ->
                        Log.d(TAG,"Name changing failure"));
    }




    private void addUser(final int position){
        final String key = getListKey(position);
        ShoppingList mList = getItem(position);
       showAlertDialog(mList ,key);
    }

    private void showAlertDialog(ShoppingList list, String key){
        Bundle mBundle = new Bundle();
        mBundle.putString("key",key);
        mBundle.putParcelable("listToShare",list);

        FragmentManager fm = ((AppCompatActivity)mActivity).getSupportFragmentManager();
        FragmentTransaction mFragmentTransaction = fm.beginTransaction();
        Fragment old = fm.findFragmentByTag("Share_Dialog");

        if(old instanceof DialogShareList){
            Log.d(TAG,"Fragment already exist");
            mFragmentTransaction.remove(old);
        }
            DialogShareList dialog = DialogShareList.newInstance();
            dialog.setArguments(mBundle);
            mFragmentTransaction.addToBackStack(null);
            dialog.show(mFragmentTransaction,"Share_Dialog");

    }

    private void logMessage(String log){
        Log.d(TAG,log);
    }

    private void stopListeningToChanges(){
        mListenerRegistration.remove();
    }


}
