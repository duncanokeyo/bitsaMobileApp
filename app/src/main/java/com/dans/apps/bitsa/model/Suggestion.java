package com.dans.apps.bitsa.model;

public class Suggestion extends Entity{
    String title;
    String body;
    String from;
    String senderEmail;

    public Suggestion() {
    }

    public Suggestion(String title, String body, String from,String senderEmail) {
        this.title=title;
        this.body = body;
        this.from = from;
        this.senderEmail = senderEmail;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return "Suggestion{" +
                "title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", from='" + from + '\'' +
                '}';
    }
}
