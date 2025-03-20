package com.example.expensetracker;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TransactionEditActivity extends AppCompatActivity {
    private EditText titleEditText, descriptionEditText, amountEditText, dateEditText;
    private Button saveButton, deleteButton;
    private TextView currencySymbol;
    private DatabaseHelper dbHelper;
    private long transactionId;
    private Transaction currentTransaction;
    private boolean isIncome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_edit);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Edit Transaction");

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        amountEditText = findViewById(R.id.amountEditText);
        dateEditText = findViewById(R.id.dateEditText);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        currencySymbol = findViewById(R.id.currencySymbol);

        // Set Euro currency symbol
        currencySymbol.setText("€");

        // Get transaction ID from intent
        transactionId = getIntent().getLongExtra("transaction_id", -1);
        if (transactionId == -1) {
            Toast.makeText(this, "Error: Transaction not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load transaction data
        loadTransactionData();

        // Set up save button
        saveButton.setOnClickListener(v -> saveTransaction());

        // Set up delete button
        deleteButton.setOnClickListener(v -> confirmDelete());
    }

    private void loadTransactionData() {
        // Get transaction from database
        currentTransaction = dbHelper.getTransactionById(transactionId);
        if (currentTransaction == null) {
            Toast.makeText(this, "Error: Transaction not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Determine if this is an income or expense transaction
        isIncome = currentTransaction.getAmount() >= 0;

        // Set title based on transaction type
        if (isIncome) {
            setTitle("Edit Income");
        } else {
            setTitle("Edit Expense");
        }

        // Fill form with transaction data
        titleEditText.setText(currentTransaction.getTitle());
        descriptionEditText.setText(currentTransaction.getDescription());
        
        // Format amount (remove negative sign for expenses)
        double displayAmount = Math.abs(currentTransaction.getAmount());
        amountEditText.setText(String.format(Locale.getDefault(), "%.2f", displayAmount));
        
        // Set date
        dateEditText.setText(currentTransaction.getDate());
    }

    private void saveTransaction() {
        // Validate input
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String amountStr = amountEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Parse amount
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            // Apply negative sign for expenses
            if (!isIncome) {
                amount = -amount;
            }

            // Update transaction in database
            boolean success = dbHelper.updateTransaction(
                    transactionId,
                    amount,
                    isIncome ? "income" : "expense",
                    title,
                    description,
                    date
            );

            if (success) {
                Toast.makeText(this, "Transaction updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update transaction", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (dialog, which) -> deleteTransaction())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTransaction() {
        boolean success = dbHelper.deleteTransaction(transactionId);
        if (success) {
            Toast.makeText(this, "Transaction deleted successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to delete transaction", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
