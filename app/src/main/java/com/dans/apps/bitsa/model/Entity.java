package com.dans.apps.bitsa.model;

import com.google.firebase.database.Exclude;

public class Entity {

    @Exclude
    String id;

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }
}
