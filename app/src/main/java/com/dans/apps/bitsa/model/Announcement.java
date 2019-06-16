package com.dans.apps.bitsa.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Created by duncan on 12/21/17.
 */
public class Announcement extends Entity{
    String title;
    String message;
    String senderEmail;
    String from;

    Object createdAt;

    public Announcement() {
    }

    public Announcement(String title, String message, String from,String senderEmail) {
        this.title = title;
        this.message = message;
        this.from = from;
        this.senderEmail = senderEmail;
        this.createdAt = ServerValue.TIMESTAMP;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getFrom() {
        return from;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public void setFrom(String from) {
        this.from = from;
    }


    public Object getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Object createdAt) {
        this.createdAt = createdAt;
    }


    @Exclude
    public Date getDate(){
        Timestamp tsp = new Timestamp((Long)createdAt);
        return new Date(tsp.getTime());
    }
}
