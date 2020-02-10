package com.krzysiudan.ourshoppinglist.services;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.krzysiudan.ourshoppinglist.application.MyApp;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMessaging";


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());


        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Activity currentActivity =((MyApp)getApplicationContext()).getCurrentActivity();


            new Thread()
            {
                public void run()
                {
                    currentActivity.runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
                            String title = remoteMessage.getNotification().getTitle();
                            String body = remoteMessage.getNotification().getBody();
                            Toast.makeText(currentActivity.getApplicationContext(),title+" "+body,Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }.start();

        }
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }



    @Override
    public void onNewToken(@NonNull String s) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference userAccount = FirebaseFirestore.getInstance().collection("users").document(user.getEmail());
        userAccount.update("FCMToken",s);
        super.onNewToken(s);
    }
}


