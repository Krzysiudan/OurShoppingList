package com.krzysiudan.ourshoppinglist.DatabaseItems;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
@IgnoreExtraProperties
public class SingleItem {

    private String name;
    private String author;
    private @ServerTimestamp
    Date timestamp;


    public SingleItem(String name, String author, Date timestamp) {
        this.name = name;
        this.author = author;
        this.timestamp = timestamp;
    }

    public SingleItem() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
