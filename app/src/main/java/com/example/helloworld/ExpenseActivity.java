package com.example.expensetracker;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Date;

/**
 * ExpenseActivity: Manages expense transaction entry and display
 * Features:
 * - Add new expense transactions
 * - View expense history
 * - Delete existing expense entries
 * - Categorize expenses
 */
public class ExpenseActivity extends AppCompatActivity implements TransactionAdapter.OnTransactionDeleteListener {
    // UI Elements
    private TextInputEditText amountInput, noteInput;
    private AutoCompleteTextView categorySpinner;
    private Button addButton;
    private RecyclerView expenseList;
    
    // Database and adapter
    private DatabaseHelper dbHelper;
    private TransactionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        // Initialize UI components
        initializeViews();

        // Set up database helper
        dbHelper = new DatabaseHelper(this);

        // Configure category dropdown
        setupCategorySpinner();

        // Set up RecyclerView for expense list
        setupRecyclerView();

        // Set up click listener for add button
        addButton.setOnClickListener(v -> addExpense());

        // Load existing expense transactions
        loadExpenseTransactions();
    }

    /**
     * Initializes all UI elements from the layout
     */
    private void initializeViews() {
        amountInput = findViewById(R.id.amountInput);
        noteInput = findViewById(R.id.noteInput);
        categorySpinner = findViewById(R.id.categorySpinner);
        addButton = findViewById(R.id.addButton);
        expenseList = findViewById(R.id.expenseList);
    }

    /**
     * Sets up the category spinner with predefined expense categories
     */
    private void setupCategorySpinner() {
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.expense_categories, android.R.layout.simple_dropdown_item_1line);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setText(categoryAdapter.getItem(0).toString(), false);
    }

    /**
     * Configures the RecyclerView with adapter and layout manager
     */
    private void setupRecyclerView() {
        adapter = new TransactionAdapter(this, this);
        expenseList.setLayoutManager(new LinearLayoutManager(this));
        expenseList.setAdapter(adapter);
    }

    /**
     * Handles the addition of a new expense transaction
     * Includes input validation and database insertion
     */
    private void addExpense() {
        String amountStr = amountInput.getText().toString();
        String note = noteInput.getText().toString();
        String category = categorySpinner.getText().toString();

        // Validate amount input
        if (amountStr.isEmpty()) {
            amountInput.setError("Please enter amount");
            return;
        }

        try {
            // Parse and save the expense transaction
            double amount = Double.parseDouble(amountStr);
            long result = dbHelper.addTransaction(amount, "expense", category, note);

            if (result != -1) {
                // Success - clear inputs and refresh list
                Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show();
                clearInputs();
                loadExpenseTransactions();
            } else {
                // Database error
                Toast.makeText(this, "Error adding expense", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            // Invalid number format
            amountInput.setError("Invalid amount format");
        }
    }

    /**
     * Clears all input fields after successful transaction
     */
    private void clearInputs() {
        amountInput.setText("");
        noteInput.setText("");
        categorySpinner.setText(categorySpinner.getAdapter().getItem(0).toString(), false);
    }

    /**
     * Loads and displays all expense transactions
     */
    private void loadExpenseTransactions() {
        Cursor cursor = dbHelper.getTransactionsByType("expense");
        adapter.swapCursor(cursor);
    }

    @Override
    public void onTransactionDeleted() {
        // Refresh the expense list after deletion
        loadExpenseTransactions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up database connection
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
