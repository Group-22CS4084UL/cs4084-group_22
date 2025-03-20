package com.example.expensetracker;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.expensetracker.databinding.ActivityIncomeBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class IncomeActivity extends AppCompatActivity {
    private ActivityIncomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIncomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Set current date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        binding.dateEditText.setText(sdf.format(new Date()));
        
        // Set up save button
        binding.saveButton.setOnClickListener(v -> saveIncome());
    }

    private void saveIncome() {
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

            // Create and save transaction to database
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            long result = dbHelper.addTransaction(amount, "income", title, description);
            
            if (result != -1) {
                Toast.makeText(this, "Income saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to save income", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
