package com.example.expensetracker;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.expensetracker.databinding.ActivityExpenseBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class IncomeActivity extends AppCompatActivity {
    private ActivityExpenseBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Income");
        }

        // Set current date by default
        binding.dateEditText.setText(getCurrentDate());

        // Set up save button click listener
        binding.saveButton.setOnClickListener(v -> saveIncome());
    }

    private void saveIncome() {
        String title = binding.titleEditText.getText().toString().trim();
        String amountStr = binding.amountEditText.getText().toString().trim();
        String description = binding.descriptionEditText.getText().toString().trim();
        String date = binding.dateEditText.getText().toString();

        if (title.isEmpty() || amountStr.isEmpty() || date.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Math.abs(Double.parseDouble(amountStr)); // Make amount positive for income
            Transaction transaction = new Transaction(amount, description, date, title);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
