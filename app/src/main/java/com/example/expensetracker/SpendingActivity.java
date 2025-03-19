package com.example.expensetracker;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.expensetracker.databinding.ActivitySpendingBinding;

/**
 * Activity for visualizing spending patterns and transaction history
 */
public class SpendingActivity extends AppCompatActivity {
    private ActivitySpendingBinding binding;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpendingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize database helper
        dbHelper = new DatabaseHelper(this);
        
        // Set up back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Spending Analysis");
        }
        
        // Load spending data
        loadSpendingData();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    private void loadSpendingData() {
        // This would be implemented to show charts, graphs, or statistics
        // For now, we'll just display a placeholder message
        binding.spendingMessage.setText("Spending analysis will be displayed here.");
    }
}
