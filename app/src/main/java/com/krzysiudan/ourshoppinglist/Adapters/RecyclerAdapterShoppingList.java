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


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
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
                                changeName(i);

                                Log.e(TAG, "Options item clicked: rename");
                                return true;
                            case R.id.menu_remove:
                                removeList(i);
                                Log.e(TAG, "Options item clicked: remove");
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

            private void changeName(final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = mActivity.getLayoutInflater();
                final View alertView = inflater.inflate(R.layout.dialog_custom_add_list,null);

//TODO add refreshing name after changing in database


                builder.setView(alertView)
                        .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                EditText alert_editText = (EditText) alertView.findViewById(R.id.alert_editText);
                                String list_name = alert_editText.getText().toString();
                                TextView mTextView = alertView.findViewById(R.id.alert_textView);
                                mTextView.setText(R.string.TextViewChangingListNameAlert);

                                if(!list_name.equals("")){
                                     final String key = mSnapshotList.get(position).getId();
                                    DocumentReference mDocumentReference = FirebaseFirestore.getInstance().collection("ShoppingLists").document(key);
                                    mDocumentReference.update("list_name",list_name)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG,"List name changed, list key: "+key);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG,"List name not changed, list key: "+key);

                                                }
                                            });
                                }
                                notifyItemChanged(position);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .create()
                        .show();
                Log.e("OurShoppingList","New list added");

            }

    public int getItemCount() {
        return mSnapshotList.size();
    }

    public ShoppingList getItem(int position) {
        return mSnapshotList.get(position).toObject(ShoppingList.class);
    }

    public void removeList(int position){
        final String key = mSnapshotList.get(position).getId();
        DocumentReference docRef = FirebaseFirestore.getInstance().document("ShoppingLists/"+key);
        docRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG,"List with key: " + key + "has been removed");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"Failure removing list with key: " + key);

                    }
                });
        mSnapshotList.remove(position);
    }
}
