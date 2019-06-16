package com.dans.apps.bitsa.model;

import com.google.firebase.database.Exclude;

public class Transaction extends Entity{
    @Exclude
    String TAG = "Transaction";

    int amount;
    String referenceNumber;
    String merchantRequestID;
    long date;
    long phoneNumber;
    String email;
    String semester;
    int transactionMethod;

    int type;

    public Transaction() {}

    public Transaction(int amount,
                       String referenceNumber,
                       String merchantRequestID,
                       long date,
                       long phoneNumber,
                       String email,
                       String semester,
                       int type) {

        this.amount = amount;
        this.phoneNumber = phoneNumber;
        this.referenceNumber = referenceNumber;
        this.merchantRequestID = merchantRequestID;
        this.date = date;
        this.email = email;
        this.semester = semester;
        this.type = type;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getTransactionMethod() {
        return transactionMethod;
    }

    public void setTransactionMethod(int transactionMethod) {
        this.transactionMethod = transactionMethod;
    }

    public String getMerchantRequestID() {
        return merchantRequestID;
    }
    public void setMerchantRequestID(String merchantRequestID) {
        this.merchantRequestID = merchantRequestID;
    }
    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Exclude
    public String getPaymentDate() { //gets the date in terms of year/month/ time
        //todo convert this to a more suitable date
        return String.valueOf(date);
    }
}
