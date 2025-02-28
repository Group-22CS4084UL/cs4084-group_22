package com.example.helloworld;

public class ExpenseList {
    private String category; // the category of the expense
    private double amount; // the amount of the expense
    private String date; // the date of the expense
    private String note; // the note of the expense

    public ExpenseList(String category, double amount, String date, String note){
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.note = note;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getNote() {
        return note;
    }
}

