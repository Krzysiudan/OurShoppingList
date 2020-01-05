package com.krzysiudan.ourshoppinglist.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firestore.v1.Write;
import com.krzysiudan.ourshoppinglist.DatabaseItems.ShoppingList;
import com.krzysiudan.ourshoppinglist.R;

import java.util.HashMap;
import java.util.Map;

public class DialogChangeName extends DialogFragment {
    private static String TAG = "OurShoppingList";
    OnNameInsertedListener callback;

    public static DialogChangeName newInstance() {
        return new DialogChangeName();
    }

    public DialogChangeName(){
    }

    public void setOnNameInsertedListener(OnNameInsertedListener callback){
        this.callback = callback;
    }

    public interface OnNameInsertedListener{
        public void onNameInserted(String name,String key, int position);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Bundle mBundle = this.getArguments();
        String key = mBundle.getString("key");
        int position = mBundle.getInt("position");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View alertView = inflater.inflate(R.layout.dialog_custom_add_list,null);
        EditText alert_editText = (EditText) alertView.findViewById(R.id.alert_editText);
        TextView alertTextView =  alertView.findViewById(R.id.alert_textView);
        alertTextView.setText(R.string.TextViewChangingListNameAlert);

        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        builder.setView(alertView);

        builder.setPositiveButton(R.string.create, (dialogInterface, i) -> {
            String list_name = alert_editText.getText().toString();

            if(!list_name.equals("")){
                callback.onNameInserted(list_name, key, position);


            }
        })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                });
        return builder.create();

    }
}
