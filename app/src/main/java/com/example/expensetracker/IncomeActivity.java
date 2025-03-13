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

/**
 * IncomeActivity: Manages income transaction entry and display
 * Features:
 * - Add new income transactions
 * - View income history
 * - Delete existing income entries
 * - Categorize income sources
 * - Input validation
 */
public class IncomeActivity extends AppCompatActivity implements TransactionAdapter.OnTransactionDeleteListener {
    // UI Elements
    private TextInputEditText amountInput, noteInput;
    private AutoCompleteTextView categorySpinner;
    private Button addButton;
    private RecyclerView incomeList;
    
    // Database and adapter
    private DatabaseHelper dbHelper;
    private TransactionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        // Initialize UI components
        initializeViews();

        // Set up database helper
        dbHelper = new DatabaseHelper(this);

        // Configure category dropdown
        setupCategorySpinner();

        // Set up RecyclerView for income list
        setupRecyclerView();

        // Set up click listener for add button
        addButton.setOnClickListener(v -> addIncome());

        // Load existing income transactions
        loadIncomeTransactions();
    }

    /**
     * Initializes all UI elements from the layout
     */
    private void initializeViews() {
        amountInput = findViewById(R.id.amountInput);
        noteInput = findViewById(R.id.noteInput);
        categorySpinner = findViewById(R.id.categorySpinner);
        addButton = findViewById(R.id.addButton);
        incomeList = findViewById(R.id.incomeList);
    }

    /**
     * Sets up the category spinner with predefined income categories
     */
    private void setupCategorySpinner() {
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.income_categories, android.R.layout.simple_dropdown_item_1line);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setText(categoryAdapter.getItem(0).toString(), false);
    }

    /**
     * Configures the RecyclerView with adapter and layout manager
     */
    private void setupRecyclerView() {
        adapter = new TransactionAdapter(this, this);
        incomeList.setLayoutManager(new LinearLayoutManager(this));
        incomeList.setAdapter(adapter);
    }

    /**
     * Handles the addition of a new income transaction
     * Includes input validation and database insertion
     */
    private void addIncome() {
        String amountStr = amountInput.getText().toString();
        String note = noteInput.getText().toString();
        String category = categorySpinner.getText().toString();

        // Validate amount input
        if (amountStr.isEmpty()) {
            amountInput.setError("Please enter amount");
            return;
        }

        try {
            // Parse and save the income transaction
            double amount = Double.parseDouble(amountStr);
            long result = dbHelper.addTransaction(amount, "income", category, note);

            if (result != -1) {
                // Success - clear inputs and refresh list
                Toast.makeText(this, "Income added successfully", Toast.LENGTH_SHORT).show();
                clearInputs();
                loadIncomeTransactions();
            } else {
                // Database error
                Toast.makeText(this, "Error adding income", Toast.LENGTH_SHORT).show();
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
     * Loads and displays all income transactions
     */
    private void loadIncomeTransactions() {
        Cursor cursor = dbHelper.getTransactionsByType("income");
        adapter.swapCursor(cursor);
    }

    @Override
    public void onTransactionDeleted() {
        // Refresh the income list after deletion
        loadIncomeTransactions();
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
