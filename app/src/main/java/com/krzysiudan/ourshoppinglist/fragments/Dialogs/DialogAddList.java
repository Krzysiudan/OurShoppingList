package com.krzysiudan.ourshoppinglist.fragments.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.krzysiudan.ourshoppinglist.models.ShoppingList;
import com.krzysiudan.ourshoppinglist.R;

import java.util.HashMap;
import java.util.Map;

public class DialogAddList extends DialogFragment {

    private static String TAG = "OurShoppingList";

    public static DialogAddList newInstance() {
        return new DialogAddList();
    }

    public DialogAddList(){
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View alertView = inflater.inflate(R.layout.dialog_custom_add_list,null);
        EditText alert_editText =  alertView.findViewById(R.id.alert_editText);


        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        builder.setView(alertView);

        builder.setPositiveButton(R.string.create, (dialogInterface, i) -> {
            String list_name = alert_editText.getText().toString();

            if(!list_name.equals("")){
                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                View parentView = getActivity().findViewById(R.id.activity_list_coordinatorLayout);


                ShoppingList mShoppingList = new ShoppingList();
                mShoppingList.setList_name(list_name);
                mShoppingList.setOwner_id(mUser.getUid());


                String userEmail = mUser.getEmail();

                WriteBatch mWriteBatch = mFirestore.batch();

                DocumentReference toUser = mFirestore.collection("users/"+userEmail+"/usedLists").document();
                mWriteBatch.set(toUser,mShoppingList);

                DocumentReference toShoppingLists = mFirestore.document("ShoppingLists/"+toUser.getId());
                mWriteBatch.set(toShoppingLists,mShoppingList);


                DocumentReference addUserAllowed = mFirestore.document("ShoppingLists/"+toUser.getId()+"/users_allowed/"+userEmail);
                Map<String,Object> map = new HashMap<>();
                map.put(userEmail,true);
                mWriteBatch.set(addUserAllowed,map);

                mWriteBatch.commit().addOnCompleteListener(task -> {
                            Log.d(TAG, "Successfully added list");
                    Snackbar.make(parentView,"List added",Snackbar.LENGTH_SHORT).show();
                        }
                ).addOnFailureListener(e ->
                        Log.d(TAG,"Failed with adding list"));
            }
        })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                });
                return builder.create();

    }
}
