package com.example.budgetkitaapp;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Expenses  implements Serializable {

    public Expenses(String expensesTransactionID, String expensesName, String  expensesCategory, String expensesTotal, String expensesDate , String invoiceUrl){
        this.expensesTransactionID = expensesTransactionID;
        this.expensesName = expensesName;
        this.expensesCategory = expensesCategory;
        this.expensesTotal = expensesTotal;
        this.expensesDate = expensesDate;
        this.invoiceUrl = invoiceUrl;
    }

    public Expenses(String expensesTransactionID, String expensesName, String expensesCategory, String expensesTotal, String expensesDate) {
        this.expensesTransactionID = expensesTransactionID;
        this.expensesName = expensesName;
        this.expensesCategory = expensesCategory;
        this.expensesTotal = expensesTotal;
        this.expensesDate = expensesDate;
    }

    // getter method for our id
    public String getId() {
        return id;
    }

    // setter method for our id
    public void setId(String id) {
        this.id = id;
    }

    @Exclude
    private String id;
    public String expensesTransactionID, expensesName, expensesCategory, expensesTotal, expensesDate;
    public String invoiceUrl;
    public Expenses(){

    }




    public void setExpensesTransactionID(String expensesTransactionID) {
        this.expensesTransactionID = expensesTransactionID;
    }

    public void setExpensesName(String expensesName) {
        this.expensesName = expensesName;
    }

    public void setExpensesCategory(String expensesCategory) {
        this.expensesCategory = expensesCategory;
    }

    public void setExpensesTotal(String expensesTotal) {
        this.expensesTotal = expensesTotal;
    }

    public void setExpensesDate(String expensesDate) {
        this.expensesDate = expensesDate;
    }

    public String getExpensesTransactionID() {
        return expensesTransactionID;
    }

    public String getExpensesName() {
        return expensesName;
    }

    public String getExpensesCategory() {
        return expensesCategory;
    }

    public String getExpensesTotal() {
        return expensesTotal;
    }

    public String getExpensesDate() {
        return expensesDate;
    }

    public String getInvoiceUrl() {
        return invoiceUrl;
    }

    public void setInvoiceUrl(String invoiceUrl) {
        this.invoiceUrl = invoiceUrl;
    }
}
