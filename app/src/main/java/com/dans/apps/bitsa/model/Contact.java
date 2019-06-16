package com.dans.apps.bitsa.model;

import java.util.HashMap;
import java.util.Map;

public class Contact extends Entity{
    String email;
    String phone;

    public Contact() {
    }

    public Contact(String email, String phone) {
        this.email = email;
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Map<String, Object> getUpdateParams(Contact updateContact) {
        Map<String,Object>update = new HashMap<>();
        if(!updateContact.getEmail().equals(email)){
            update.put("email",updateContact.getEmail());
        }
        if(!updateContact.getPhone().equals(phone)){
            update.put("phone",updateContact.getPhone());
        }
        return update;
    }
}
