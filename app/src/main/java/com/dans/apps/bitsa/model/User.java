package com.dans.apps.bitsa.model;

import android.text.TextUtils;

import com.dans.apps.bitsa.Constants;

import java.util.HashMap;
import java.util.Map;

public class User extends Entity{
    String name;
    String schoolID;
    String major;
    String phoneNumber;
    String email;

    int role = Constants.ROLES.NO_ROLE;

    int type = Constants.USER_TYPE.VISITOR;

    public User() {}

    public User(int type,
                String name,
                String schoolID,
                String major,
                String phoneNumber,
                String email,
                int role) {

        this.name = name;
        this.type= type;
        this.schoolID = schoolID;
        this.major = major;
        this.phoneNumber = phoneNumber;
        this.email =email;
        this.role = role;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchoolID() {
        return schoolID;
    }

    public void setSchoolID(String schoolID) {
        this.schoolID = schoolID;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", schoolID='" + schoolID + '\'' +
                ", major='" + major + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", type=" + type +
                '}';
    }

    //todo, finish this part
    public  Map<String,Object>setUpdateFields(User update){
        Map<String,Object>updateFields = new HashMap<>();
        if(TextUtils.isEmpty(name) || !name.equals(update.getName())){
            updateFields.put("name",update.getName());
        }
        if(TextUtils.isEmpty(email)){
            updateFields.put("email",update.getEmail());
        }
        if(TextUtils.isEmpty(phoneNumber)){
            updateFields.put("phoneNumber",update.getPhoneNumber());
        }
        if(type == Constants.USER_TYPE.VISITOR){
            updateFields.put("type",update.getType());
        }
        return updateFields;
    }
}
