package com.krzysiudan.ourshoppinglist.Adapters;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

public class RecyclerAdapterBoughtItemsList extends RecyclerView.Adapter<RecyclerAdapterBoughtItemsList.ViewHolder> {

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

    public RecyclerAdapterBoughtItemsList (Activity activity, String motherListKey, FirebaseFirestore mFirestore){
        mSnapshotList =new ArrayList<>();
        mActivity = activity;
        this.mFirestore = mFirestore;
        mMotherListKey = motherListKey;
        Log.w(TAG,"MotherListKey in BoughtItems:"+mMotherListKey);

        mCollectionReferenceBought = mFirestore.collection("ShoppingLists").document(mMotherListKey).collection("Bought");
        mCollectionReferencePlanned =mFirestore.collection("ShoppingLists").document(mMotherListKey).collection("Planned");


        mCollectionReferenceBought.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if(e != null) {
                    Log.w(TAG,"Listener on ShoppingLists error:",e);
                    return;
                }

              if(!queryDocumentSnapshots.isEmpty()){
                for(DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case ADDED:
                            Log.e(TAG, "New shoppinglist added" + dc.getDocument().getData());
                            mSnapshotList.add(dc.getDocument());
                            notifyItemInserted(mSnapshotList.size()-1);
                            break;
                        case MODIFIED:
                            Log.e(TAG, "Shoppinglist edited " + dc.getDocument().getData());
                            break;
                        case REMOVED:
                            Log.e(TAG, "Shoppinglist removed" + dc.getDocument().getData());
                            mSnapshotList.remove(dc.getDocument());
                            break;
                    }
                }

                }
            }
        });


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
            SingleItem item = getItem(getAdapterPosition());
            final String name = item.getName();
            mCollectionReferencePlanned.document(item.getName()).set(item);
            mCollectionReferenceBought.document(item.getName()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.w(TAG,"Item deleted: "+name);
                }
            });
            removeItem(getAdapterPosition());

        }
    }



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
        QueryDocumentSnapshot snapshot = mSnapshotList.get(i);

        final TextView textView = viewHolder.textView;

        if(snapshot.exists()){
            SingleItem mSingleItem = snapshot.toObject(SingleItem.class);
            Log.e("OurShoppingList", "ShoppingList name:" +mSingleItem.getName());
            final String itemName = mSingleItem.getName();
            textView.setText(mSingleItem.getName());
        }


    }

    public void removeItem (int position){
        key= mSnapshotList.get(position).toObject(SingleItem.class).getName();
        mCollectionReferenceBought.document(key)
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

    public void removeAllItems(){
        int size = mSnapshotList.size();
        Log.e("OurShoppingList","Size of mSnapshotList: "+size);
        for (int i = 0; i<size; i++){
            removeItem(0);
        }
    }

    /* TODO add detach listener
    public void cleanUp(){
        ;
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
