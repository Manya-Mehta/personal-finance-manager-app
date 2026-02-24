package com.example.personalfinancialmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText amountInput, descriptionInput, budgetInput;
    private AutoCompleteTextView categorySpinner, typeSpinner, filterCategorySpinner, filterTypeSpinner;
    private MaterialButton addButton, setBudgetButton, logoutButton;
    private ListView transactionList;
    private TextView balanceText, budgetStatus;
    private TransactionAdapter adapter;
    private DatabaseHelper dbHelper;
    private LinearProgressIndicator budgetProgressBar;
    private CardView iconBackground;
    private View addIncomeBtn, addExpenseBtn, setBudgetBtn, currencyBtn;
    private double monthlyBudget = 0;

    private View transactionListCard, filterCard, transactionsHeader;

    private int currentUserId;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentUserId = getIntent().getIntExtra("USER_ID", -1);
        currentUsername = getIntent().getStringExtra("USERNAME");

        if (currentUserId == -1) {
            Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        amountInput = findViewById(R.id.amountInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        categorySpinner = findViewById(R.id.categorySpinner);
        typeSpinner = findViewById(R.id.typeSpinner);
        addButton = findViewById(R.id.addButton);
        transactionList = findViewById(R.id.transactionList);
        balanceText = findViewById(R.id.balanceText);
        budgetInput = findViewById(R.id.budgetInput);
        setBudgetButton = findViewById(R.id.setBudgetButton);
        budgetStatus = findViewById(R.id.budgetStatus);
        logoutButton = findViewById(R.id.logoutButton);
        budgetProgressBar = findViewById(R.id.budgetProgressBar);

        addIncomeBtn = findViewById(R.id.addIncomeBtn);
        addExpenseBtn = findViewById(R.id.addExpenseBtn);
        setBudgetBtn = findViewById(R.id.setBudgetBtn);
        currencyBtn = findViewById(R.id.currencyBtn);

        filterCategorySpinner = findViewById(R.id.filterCategorySpinner);
        filterTypeSpinner = findViewById(R.id.filterTypeSpinner);

        transactionListCard = findViewById(R.id.transactionListCard);
        filterCard = findViewById(R.id.filterCard);
        transactionsHeader = findViewById(R.id.transactionsHeader);

        dbHelper = new DatabaseHelper(this);
        adapter = new TransactionAdapter(this, dbHelper.getAllTransactions(currentUserId));
        transactionList.setAdapter(adapter);

        updateBalance();
        updateBudgetStatus();
        checkTransactionsVisibility();

        String[] categories = getResources().getStringArray(R.array.categories);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        categorySpinner.setAdapter(categoryAdapter);
        filterCategorySpinner.setAdapter(categoryAdapter);

        String[] types = getResources().getStringArray(R.array.transaction_types);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, types);
        typeSpinner.setAdapter(typeAdapter);
        filterTypeSpinner.setAdapter(typeAdapter);

        categorySpinner.setText(categories[1], false);
        typeSpinner.setText(types[2], false);
        filterCategorySpinner.setText(categories[0], false);
        filterTypeSpinner.setText(types[0], false);

        addButton.setOnClickListener(v -> addTransaction());

        addIncomeBtn.setOnClickListener(v -> {
            typeSpinner.setText(types[1], false);
            focusOnAmount();
        });

        addExpenseBtn.setOnClickListener(v -> {
            typeSpinner.setText(types[2], false);
            focusOnAmount();
        });

        setBudgetBtn.setOnClickListener(v -> {
            budgetInput.requestFocus();
        });

        currencyBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CurrencySettingsActivity.class);
            startActivity(intent);
        });

        transactionList.setOnItemLongClickListener((parent, view, position, id) -> {
            Transaction t = (Transaction) adapter.getItem(position);
            dbHelper.deleteTransaction(t, currentUserId);
            adapter.updateList(dbHelper.getAllTransactions(currentUserId));
            updateBalance();
            updateBudgetStatus();
            checkTransactionsVisibility();
            Toast.makeText(MainActivity.this, "Transaction Deleted", Toast.LENGTH_SHORT).show();
            return true;
        });

        filterCategorySpinner.setOnItemClickListener((parent, view, position, id) -> {
            applyFilters();
            checkTransactionsVisibility();
        });
        filterTypeSpinner.setOnItemClickListener((parent, view, position, id) -> {
            applyFilters();
            checkTransactionsVisibility();
        });

        setBudgetButton.setOnClickListener(v -> {
            String input = budgetInput.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter a budget amount", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                monthlyBudget = Double.parseDouble(input);
                updateBudgetStatus();
                Toast.makeText(MainActivity.this, "Budget set successfully!", Toast.LENGTH_SHORT).show();
                budgetInput.getText().clear();
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Invalid number", Toast.LENGTH_SHORT).show();
            }
        });

        logoutButton.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            preferences.edit().clear().apply();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_currency_settings) {
            openCurrencySettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void focusOnAmount() {
        amountInput.requestFocus();
    }

    private void applyFilters() {
        String selectedCategory = filterCategorySpinner.getText().toString();
        String selectedType = filterTypeSpinner.getText().toString();
        adapter.updateList(dbHelper.getFilteredTransactions(selectedCategory, selectedType, currentUserId));
    }

    private void addTransaction() {
        String desc = descriptionInput.getText().toString().trim();
        String amountText = amountInput.getText().toString().trim();

        if (amountText.isEmpty() || desc.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            Toast.makeText(MainActivity.this, "Invalid amount entered", Toast.LENGTH_SHORT).show();
            return;
        }

        String category = categorySpinner.getText().toString();
        String type = typeSpinner.getText().toString();

        if (type.equals("Expense")) {
            amount = -amount;
        }

        dbHelper.addTransaction(new Transaction(amount, desc, category, type), currentUserId);
        adapter.updateList(dbHelper.getAllTransactions(currentUserId));
        updateBalance();
        updateBudgetStatus();
        checkTransactionsVisibility();

        amountInput.getText().clear();
        descriptionInput.getText().clear();

        Toast.makeText(MainActivity.this, "Transaction Added!", Toast.LENGTH_SHORT).show();
    }

    private void updateBudgetStatus() {
        double totalSpent = 0;
        for (Transaction t : dbHelper.getAllTransactions(currentUserId)) {
            if (t.getType().equalsIgnoreCase("Expense")) {
                totalSpent += Math.abs(t.getAmount());
            }
        }

        budgetStatus.setText(String.format("Monthly Budget: ₹%.2f used / ₹%.2f limit", totalSpent, monthlyBudget));

        if (monthlyBudget > 0) {
            int progress = (int) ((totalSpent / monthlyBudget) * 100);
            budgetProgressBar.setProgress(progress);

            if (progress > 90) {
                budgetProgressBar.setIndicatorColor(getResources().getColor(android.R.color.holo_red_light));
            } else if (progress > 75) {
                budgetProgressBar.setIndicatorColor(getResources().getColor(android.R.color.holo_orange_light));
            } else {
                budgetProgressBar.setIndicatorColor(getResources().getColor(android.R.color.holo_green_light));
            }
        } else {
            budgetProgressBar.setProgress(0);
        }
    }

    private void updateBalance() {
        double balance = 0;
        List<Transaction> transactions = dbHelper.getAllTransactions(currentUserId);
        for (Transaction t : transactions) {
            balance += t.getAmount();
        }

        balanceText.setText(String.format("₹%.2f", balance));
    }

    private void checkTransactionsVisibility() {
        List<Transaction> transactions = adapter.getTransactionList();
        boolean hasTransactions = transactions != null && !transactions.isEmpty();

        int visibility = hasTransactions ? View.VISIBLE : View.GONE;
        transactionListCard.setVisibility(visibility);
        filterCard.setVisibility(visibility);
        transactionsHeader.setVisibility(visibility);
    }

    private void openCurrencySettings() {
        Intent intent = new Intent(MainActivity.this, CurrencySettingsActivity.class);
        startActivity(intent);
    }
}
