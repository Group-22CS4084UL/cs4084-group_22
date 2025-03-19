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

        // Set default date
        binding.dateEditText.setText(getCurrentDate());

        binding.saveButton.setOnClickListener(v -> saveExpense());
    }

    private void saveExpense() {
        String title = binding.titleEditText.getText().toString();
        String description = binding.descriptionEditText.getText().toString();
        String amountStr = binding.amountEditText.getText().toString();
        String date = binding.dateEditText.getText().toString();

        if (title.isEmpty() || description.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create transaction with negative amount for expense
            Transaction transaction = new Transaction(title, description, -amount, date);
            // TODO: Save transaction to database
            
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }
}
