package com.krzysiudan.ourshoppinglist.Fragments;

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


import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.krzysiudan.ourshoppinglist.DatabaseItems.ShoppingList;
import com.krzysiudan.ourshoppinglist.R;

import java.util.HashMap;
import java.util.Map;

public class DialogShareList extends DialogFragment {

    private final String TAG = "FragmentDialogShare";


    public static DialogShareList newInstance() {
        return new DialogShareList();
    }



    public DialogShareList(){
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle mBundle = this.getArguments();
        String key = mBundle.getString("key");
        ShoppingList mShoppingList = mBundle.getParcelable("listToShare");

        LayoutInflater mLayoutInflater = requireActivity().getLayoutInflater();
        final View mView = mLayoutInflater.inflate(R.layout.dialog_custom_share_list,null);
        EditText alertEditText = mView.findViewById(R.id.fragment_dialog_share_editText);

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        mBuilder.setView(mView);
        mBuilder.setPositiveButton(R.string.add, (dialogInterface, i) -> {
            final String friendEmail = alertEditText.getText().toString();
            if(!friendEmail.equals("")){
                final DocumentReference mDocumentReference = FirebaseFirestore.getInstance().collection("users").document(friendEmail).collection("usedLists").document(key);
                Map<String, Object> data = new HashMap<>();
                WriteBatch mWriteBatch = FirebaseFirestore.getInstance().batch();
                mWriteBatch.set(mDocumentReference,mShoppingList);
                mWriteBatch.commit()
                        .addOnCompleteListener(task ->
                                Log.d("OurShoppingList","Batch in DialogShareList completed"))
                        .addOnFailureListener(e ->
                                Log.d("OurShoppingList","Batch in DialogShareList failure"));
            }
        });
        mBuilder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
        });
        return mBuilder.create();
    }
}
