package com.krzysiudan.ourshoppinglist.DatabaseItems;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

@IgnoreExtraProperties
public class ShoppingList {

    private String list_name;
    private @ServerTimestamp Date timestamp;
    private String owner_id;

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public ShoppingList(String list_name, Date timestamp, String owner_id) {
        this.list_name = list_name;
        this.timestamp = timestamp;
        this.owner_id = owner_id;
    }
    public ShoppingList(){

    }

    public String getList_name() {
        return list_name;
    }

    public void setList_name(String list_name) {
        this.list_name = list_name;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
