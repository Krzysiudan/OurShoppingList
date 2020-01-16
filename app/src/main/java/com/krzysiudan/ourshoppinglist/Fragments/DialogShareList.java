package com.krzysiudan.ourshoppinglist.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        mBuilder.setView(mView);
        mBuilder.setPositiveButton(R.string.add, (dialogInterface, i) -> {

                });
        mBuilder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
        });
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
        View parentView = getActivity().findViewById(R.id.activity_list_coordinatorLayout);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            final String friendEmail = alertEditText.getText().toString();
            if (!friendEmail.equals("") && user.getEmail() != friendEmail) {
                final DocumentReference mDocumentReference = FirebaseFirestore.getInstance().collection("users").document(friendEmail).collection("usedLists").document(key);
                DocumentReference friendDocument = FirebaseFirestore.getInstance().collection("users").document(friendEmail);
                friendDocument.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            mDocumentReference.set(mShoppingList)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.i("OurShoppingList", "List Shared with friend");
                                        Toast.makeText(getActivity().getApplicationContext(),R.string.toast_message_dialog_share_list_invitation_send,Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Log.i("OurShoppingList", "Failed sharing list with friend"));
                        } else {
                            Log.e("OurShoppingList", "No such document");
                        }
                    } else {
                        Log.e("OurShoppingList", "Get failed with: " + task.getException());
                    }
                });
            }

        });

        return dialog;
    }
}
