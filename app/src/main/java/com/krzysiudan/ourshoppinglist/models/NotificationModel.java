package com.krzysiudan.ourshoppinglist.models;

public class NotificationModel {
    private String notificationMessage;
    private String senderUserEmail;

    public NotificationModel(String notificationMessage, String senderUserEmail) {
        this.notificationMessage = notificationMessage;
        this.senderUserEmail = senderUserEmail;
    }

    public NotificationModel(){

    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public String getSenderUserEmail() {
        return senderUserEmail;
    }
}
