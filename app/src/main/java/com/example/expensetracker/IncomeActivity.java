package com.example.expensetracker;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.expensetracker.databinding.ActivityIncomeBinding;
import java.util.Locale;

public class IncomeActivity extends AppCompatActivity {
    private ActivityIncomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIncomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set current date by default
        binding.dateEditText.setText(getCurrentDate());

        // Set up save button click listener
        binding.saveButton.setOnClickListener(v -> saveIncome());
    }

    private void saveIncome() {
        String title = binding.titleEditText.getText().toString().trim();
        String description = binding.descriptionEditText.getText().toString().trim();
        String amountStr = binding.amountEditText.getText().toString().trim();
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

            Transaction transaction = new Transaction(title, description, amount, date);
            // TODO: Save transaction to database
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCurrentDate() {
        return java.time.LocalDate.now().toString();
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
