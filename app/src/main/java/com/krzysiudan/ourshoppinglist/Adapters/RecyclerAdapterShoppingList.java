package com.krzysiudan.ourshoppinglist.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.krzysiudan.ourshoppinglist.Activities.ActivityMainItems;
import com.krzysiudan.ourshoppinglist.DatabaseItems.ShoppingList;
import com.krzysiudan.ourshoppinglist.Interfaces.RecyclerViewClickListener;
import com.krzysiudan.ourshoppinglist.R;

import org.w3c.dom.Text;

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
    private ArrayList<DocumentSnapshot> mSnapshotList;
    private Context context;
    private static RecyclerViewClickListener itemListener;

    public RecyclerAdapterShoppingList(Activity activity, FirebaseFirestore mFirestore, String name, final Context context) {
        mActivity = activity;
        mDisplayName = name;
        this.mFirestore = mFirestore;

        mSnapshotList = new ArrayList<>();
        this.context = context;
        collectionReference = mFirestore.collection("ShoppingLists");



        collectionReference.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
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
            }
        });



    }




    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public EditText nameTextView;
        public ImageButton mImageButton;
        Context context;
        ArrayList<DocumentSnapshot> mSnapshotList;

        public ViewHolder(View itemView, Context context, ArrayList<DocumentSnapshot> msnapshotlist) {
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

        return new ViewHolder(shoppingView, context,mSnapshotList);

    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerAdapterShoppingList.ViewHolder viewHolder,  int i) {
        DocumentSnapshot snapshot= mSnapshotList.get(viewHolder.getAdapterPosition());
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
                Log.e(TAG, "Options item clicked:  position : " +viewHolder.getLayoutPosition());
                final PopupMenu popup = new PopupMenu(mActivity, imageButton);
                popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int option = menuItem.getItemId();

                        switch (option) {
                            case R.id.menu_rename:
                                changeName(viewHolder.getLayoutPosition());

                                Log.e(TAG, "Options item clicked: rename, position clicked : " +viewHolder.getLayoutPosition());
                                return true;
                            case R.id.menu_remove:
                                removeList(viewHolder.getLayoutPosition());
                                Log.e(TAG, "Options item clicked: remove, position clicked : " +viewHolder.getLayoutPosition());
                                return true;
                            case R.id.menu_add_user:

                            default:
                                return false;
                        }
                    }
                });
                popup.show();


            }
        });
    }



            public void cleanUp() {
                //TODO detach listener
            }



    public int getItemCount() {
        return mSnapshotList.size();
    }

    public ShoppingList getItem(int position) {
        return mSnapshotList.get(position).toObject(ShoppingList.class);
    }

    public void removeList(final int position){
        final String key = mSnapshotList.get(position).getId();
        DocumentReference docRef = FirebaseFirestore.getInstance().document("ShoppingLists/"+key);
        docRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e(TAG,"List with key: " + key + "has been removed on position : " +position);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"Failure removing list with key: " + key);

                    }
                });
                mSnapshotList.remove(position);
                notifyItemRemoved(position);
    }

    private void changeName(final int position) {
        Log.e(TAG,"changeName method");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        final View alertView = inflater.inflate(R.layout.dialog_custom_add_list,null);
        TextView mTextView = alertView.findViewById(R.id.alert_textView);
        mTextView.setText(R.string.TextViewChangingListNameAlert);

//TODO add refreshing name after changing in database, Zmienić dane w arraylist mSnapshotlist aby działało


        builder.setView(alertView)
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText alert_editText = (EditText) alertView.findViewById(R.id.alert_editText);
                        final String list_name = alert_editText.getText().toString();

                        if(!list_name.equals("")){
                            final String key = mSnapshotList.get(position).getId();
                            final DocumentReference mDocumentReference = FirebaseFirestore.getInstance().collection("ShoppingLists").document(key);
                            mDocumentReference.update("list_name",list_name)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.e(TAG,"List name changed to: " + list_name +" , list key: "+key);
                                            mDocumentReference
                                                    .get()
                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if(task.isSuccessful()){
                                                                DocumentSnapshot doc = task.getResult();
                                                                if(doc.exists()){
                                                                    Log.e(TAG, "changeName method : Document Snapshot exist ");
                                                                    mSnapshotList.set(position,doc);
                                                                    notifyItemChanged(position);
                                                                } else {
                                                                    Log.e(TAG, "changeName method : Document Snapshot not exist ");
                                                                }
                                                            } else {
                                                                Log.e(TAG, "changeName method : get failed with : " + task.getException());
                                                            }
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG,"List name not changed, list key: "+key);

                                        }
                                    });
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create()
                .show();


    }

}
