package com.example.expensetracker;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.expensetracker.databinding.ActivityExpenseBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExpenseActivity extends AppCompatActivity {
    private ActivityExpenseBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set current date by default
        binding.dateEditText.setText(getCurrentDate());

        // Set up save button click listener
        binding.saveButton.setOnClickListener(v -> saveExpense());
    }

    private void saveExpense() {
        String title = binding.titleEditText.getText().toString().trim();
        String amountStr = binding.amountEditText.getText().toString().trim();
        String description = binding.descriptionEditText.getText().toString().trim();
        String date = binding.dateEditText.getText().toString();

        if (title.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = -Math.abs(Double.parseDouble(amountStr)); // Make amount negative for expenses
            Transaction transaction = new Transaction(title, description, amount, date);
            // TODO: Save transaction to database
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }
}
