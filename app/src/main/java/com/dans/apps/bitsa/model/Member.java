package com.dans.apps.bitsa.model;

public class Member extends Entity {

    String name;
    String phoneNumber;
    String email;
    String role;

    public Member() {
    }


    public void setFields(String string){
        String [] contents = string.split(":");
        name = contents[0].trim();
        phoneNumber = contents[1].trim();
        email = contents[2].trim();
        role = contents[3].trim();
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "Member{" +
                "name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
