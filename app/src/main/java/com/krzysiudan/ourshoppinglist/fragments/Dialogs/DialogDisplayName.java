package com.krzysiudan.ourshoppinglist.fragments.Dialogs;

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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.krzysiudan.ourshoppinglist.R;
import com.krzysiudan.ourshoppinglist.models.ShoppingList;

import java.util.HashMap;
import java.util.Map;

public class DialogDisplayName extends DialogFragment {
    private static String TAG = "OurShoppingList";

    public static DialogDisplayName newInstance(){
        return  new DialogDisplayName();
    }

    public DialogDisplayName(){
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View alertView = inflater.inflate(R.layout.dialog_custom_add_list,null);
        EditText alert_editText =  alertView.findViewById(R.id.alert_editText);
        TextView alertTextView = alertView.findViewById(R.id.alert_textView);
        alertTextView.setText(R.string.add_your_display_name);
        alert_editText.setHint(R.string.display_name);


        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        builder.setView(alertView);

        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            String displayName = alert_editText.getText().toString();

            if(!displayName.equals("")){
                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                View parentView = getActivity().findViewById(R.id.activity_list_coordinatorLayout);

                String userEmail = mUser.getEmail();
                DocumentReference userDocument = mFirestore.collection("users").document(userEmail);
                userDocument.update("userName",displayName).addOnCompleteListener(task -> {
                    Log.d(TAG,"User display name succesfully changed to: " + displayName);
                    Snackbar.make(parentView,"Happy shopping!",Snackbar.LENGTH_LONG).show();
                    updateUserProfile(displayName);

                }).addOnFailureListener(e ->
                        Log.e(TAG,"Display name changing failed"));
            }
        });
        builder.setCancelable(false);
        return builder.create();

    }

    private void updateUserProfile(String displayName){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName).build();

        user.updateProfile(profileUpdate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG,"Display name succesfully changed to: "+displayName);
                        }
                    }
                });
    }
}
