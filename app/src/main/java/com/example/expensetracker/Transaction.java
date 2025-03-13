package com.example.expensetracker;

/**
 * Transaction class to represent income and expense transactions
 */
public class Transaction {
    private String date;
    private String type;  // "INCOME" or "EXPENSE"
    private double amount;
    private String category;
    private String note;  // Changed from description to note to match database

    public Transaction(String date, String type, double amount, String category, String note) {
        this.date = date;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.note = note;
    }

    // Getters
    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {  // Keep this for backward compatibility
        return note;
    }

    public String getNote() {  // Add new getter for note
        return note;
    }

    // Setters
    public void setDate(String date) {
        this.date = date;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setDescription(String description) {  // Keep this for backward compatibility
        this.note = description;
    }
}
