package com.example.expensetracker;

public class Transaction {
    private long id;
    private String title;
    private String description;
    private double amount;
    private String date;
    private String category;

    public Transaction(String title, String description, double amount, String date) {
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.category = amount >= 0 ? "Income" : "Expense";
    }

    public Transaction(String title, String description, double amount, String date, String category) {
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.category = category;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
}
