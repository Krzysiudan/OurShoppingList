package com.krzysiudan.ourshoppinglist;

public class ShoppingList {

    private String list_name;


    public ShoppingList(String list_name) {
        this.list_name = list_name;
    }
    public ShoppingList(){

    }

    public String getList_name() {
        return list_name;
    }

    public void setList_name(String list_name) {
        this.list_name = list_name;
    }
}
