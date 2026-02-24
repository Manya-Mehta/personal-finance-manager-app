package com.example.personalfinancialmanager;

public class Transaction {
    private double amount;
    private String description;
    private String category;
    private String type;

    public Transaction(double amount, String description, String category, String type) {
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }
}
