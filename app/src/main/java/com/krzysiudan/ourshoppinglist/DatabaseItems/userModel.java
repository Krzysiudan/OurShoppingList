package com.krzysiudan.ourshoppinglist.DatabaseItems;

public class userModel {
    private String idToken;
    private String email;
    private String userName;

    public userModel(String idToken, String email, String userName){
        this.idToken = idToken;
        this.email = email;
        this.userName = userName;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getEmail() {
        return email;
    }

    public String getUserName() {
        return userName;
    }
}
