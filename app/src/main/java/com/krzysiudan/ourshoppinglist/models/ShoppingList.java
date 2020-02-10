package com.krzysiudan.ourshoppinglist.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@IgnoreExtraProperties
public class ShoppingList implements Parcelable {

    private String list_name;
    private @ServerTimestamp Date timestamp;
    private String owner_id;



    public ShoppingList(String list_name, Date timestamp, String owner_id) {
        this.list_name = list_name;
        this.timestamp = timestamp;
        this.owner_id = owner_id;
    }
    public ShoppingList(){

    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public ShoppingList(Parcel in){
        Log.d("ShoppingList","Data deparcelling");
        list_name = in.readString();
        String fromParcelTimestamp = in.readString();
        try {
            DateFormat dateFormat =  SimpleDateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.FULL, Locale.getDefault());
            Date parsedDate = dateFormat.parse(fromParcelTimestamp);
            timestamp = new java.sql.Timestamp(parsedDate.getTime());
        } catch(Exception e) {
            Log.e("OurShoppingList", "Exeption with unparcelling timestamp");
        }
        owner_id = in.readString();
    }
    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public String getList_name() {
        return list_name;
    }

    public void setList_name(String list_name) {
        this.list_name = list_name;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.d("OurShoppingList","write to parcel " + flags);
        dest.writeString(list_name);
        DateFormat dateFormat =  SimpleDateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.FULL, Locale.getDefault());
        String inStringTimestamp = dateFormat.format(timestamp);
        dest.writeString(inStringTimestamp);
        dest.writeString(owner_id);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){
        public ShoppingList createFromParcel(Parcel in ){
            return new ShoppingList(in);
        }

        public ShoppingList[] newArray(int size){
            return new ShoppingList[size];
        }
    };
}
