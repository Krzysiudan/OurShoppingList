package com.krzysiudan.ourshoppinglist;

public class SingleItem {

    private String name;
    private String author;


    public SingleItem(String name, String author) {
        this.name = name;
        this.author = author;
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
}
