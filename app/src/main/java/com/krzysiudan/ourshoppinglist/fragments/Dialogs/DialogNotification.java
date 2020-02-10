package com.krzysiudan.ourshoppinglist.fragments.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.krzysiudan.ourshoppinglist.models.NotificationModel;
import com.krzysiudan.ourshoppinglist.R;

import java.util.ArrayList;

public class DialogNotification extends DialogFragment {
    public static DialogNotification newInstance() {
        return new DialogNotification();
    }


    public DialogNotification(){
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle mBundle = this.getArguments();
        String key = mBundle.getString("key");

        LayoutInflater mLayoutInflater = requireActivity().getLayoutInflater();
        final View mView = mLayoutInflater.inflate(R.layout.dialog_notification,null);
        AutoCompleteTextView mAutoCompleteTextView = mView.findViewById(R.id.autoCompleteTextViewNotification);
        String[] notifications = getResources().getStringArray(R.array.notifications);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1,notifications);
        mAutoCompleteTextView.setAdapter(adapter);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        mBuilder.setView(mView);
        mBuilder.setPositiveButton(R.string.send, (dialogInterface, i) -> {
               getFriends(key, new MyCallback() {
                @Override
                public void onCallback(ArrayList<String> ofFriends) {
                    String message = mAutoCompleteTextView.getText().toString();
                    NotificationModel notification = new NotificationModel(message,user.getEmail());

                    for(int j = 0; j<ofFriends.size(); j++){
                        db.collection("notifications").document(ofFriends.get(j)).collection("userNotifications").document()
                                .set(notification);
                        Log.d("OurShoppingList","Notification send: "+message);
                    }
                }
            });

        });
        mBuilder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
        });
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
        return dialog;
    }


    private void getFriends(String key, MyCallback myCallback){
        CollectionReference mCollectionReference = FirebaseFirestore.getInstance().collection("ShoppingLists").document(key).collection("users_allowed");
        ArrayList<String> friendEmails = new ArrayList<>();
        mCollectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("OurShoppingList", "Document dialog notification: " + document.getId());
                        if (document.getId() != FirebaseAuth.getInstance().getCurrentUser().getEmail()) {
                            friendEmails.add(document.getId());
                        }
                    }
                } else {
                    Log.e("OurShoppingList", "Error getting documents: " + task.getException());
                }
                myCallback.onCallback(friendEmails);
             }
        });
}

public interface MyCallback{
        void onCallback(ArrayList<String> ofFriends);
}


}
