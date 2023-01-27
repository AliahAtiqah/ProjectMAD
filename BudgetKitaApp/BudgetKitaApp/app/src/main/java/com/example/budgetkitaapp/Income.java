package com.example.budgetkitaapp;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Income implements Serializable {

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

    private String transactionID, incomeName, incomeCategory, totalIncome, dateIncome;

    public Income(){

    }

    public Income(String transactionID, String incomeName, String incomeCategory, String totalIncome, String dateIncome ){
        this.transactionID = transactionID;
        this.incomeName = incomeName;
        this.incomeCategory = incomeCategory;
        this.totalIncome = totalIncome;
        this.dateIncome = dateIncome;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public void setIncomeName(String incomeName) {
        this.incomeName = incomeName;
    }

    public void setIncomeCategory(String incomeCategory) {
        this.incomeCategory = incomeCategory;
    }

    public void setTotalIncome(String totalIncome) {
        this.totalIncome = totalIncome;
    }

    public void setDateIncome(String dateIncome) {
        this.dateIncome = dateIncome;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public String getIncomeName() {
        return incomeName;
    }

    public String getIncomeCategory() {
        return incomeCategory;
    }

    public String getTotalIncome() {
        return totalIncome;
    }

    public String getDateIncome() {
        return dateIncome;
    }
}
