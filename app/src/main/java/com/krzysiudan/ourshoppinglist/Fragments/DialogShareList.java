package com.krzysiudan.ourshoppinglist.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.krzysiudan.ourshoppinglist.R;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DialogShareList extends DialogFragment {

    private final String TAG = "FragmentDialogShare";


    public static DialogShareList newInstance() {
        DialogShareList frag = new DialogShareList();
        return frag;
    }



    public DialogShareList(){
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle mBundle = this.getArguments();
        String key = mBundle.getString("key");
        LayoutInflater mLayoutInflater = requireActivity().getLayoutInflater();
        final View mView = mLayoutInflater.inflate(R.layout.dialog_custom_share_list,null);
        EditText alertEditText = mView.findViewById(R.id.fragment_dialog_share_editText);

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        mBuilder.setView(mView);
        mBuilder.setPositiveButton(R.string.add, (dialogInterface, i) -> {
            final String friendEmail = alertEditText.getText().toString();
            if(!friendEmail.equals("")){
                final DocumentReference mDocumentReference = FirebaseFirestore.getInstance().collection("ShoppingLists").document(key).collection("users_allowed").document(friendEmail);
                Map<String, Object> data = new HashMap<>();
                data.put(friendEmail,true);
                mDocumentReference
                        .set(data)
                        .addOnSuccessListener(aVoid ->
                                Log.d(TAG, "User added successfully :" + friendEmail))
                        .addOnFailureListener(e ->
                                Log.d(TAG, "Error adding user : " + e));
            }
        });
        mBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });

        return mBuilder.create();
    }
}
