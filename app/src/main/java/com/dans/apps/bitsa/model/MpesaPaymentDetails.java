package com.dans.apps.bitsa.model;

public class MpesaPaymentDetails {
    int payBillNumber;
    long accountNumber;

    public MpesaPaymentDetails() {
    }

    public MpesaPaymentDetails(int payBillNumber, long accountNumber) {
        this.payBillNumber = payBillNumber;
        this.accountNumber = accountNumber;
    }

    public int getPayBillNumber() {
        return payBillNumber;
    }

    public void setPayBillNumber(int payBillNumber) {
        this.payBillNumber = payBillNumber;
    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
    }

    @Override
    public String toString() {
        return "MpesaPaymentDetails{" +
                "payBillNumber=" + payBillNumber +
                ", accountNumber=" + accountNumber +
                '}';
    }
}
