package com.example.expensetracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    // UI Elements
    private TextView totalBalanceText, totalIncomeText, totalExpenseText;
    private MaterialCardView incomeCard, expenseCard;
    private MaterialCardView darkModeCard, exportCard;
    private MaterialCardView historyCard, incomeHistoryCard, expenseHistoryCard;
    
    // Database helper instance
    private DatabaseHelper dbHelper;
    
    // Constants for SharedPreferences
    private static final String PREFS_NAME = "ExpenseTrackerPrefs";
    private static final String DARK_MODE_KEY = "darkMode";
    
    // Permission request
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Initialize database helper
        dbHelper = new DatabaseHelper(this);
        
        // Initialize UI elements
        totalBalanceText = findViewById(R.id.totalBalanceText);
        totalIncomeText = findViewById(R.id.totalIncomeText);
        totalExpenseText = findViewById(R.id.totalExpenseText);
        
        incomeCard = findViewById(R.id.incomeCard);
        expenseCard = findViewById(R.id.expenseCard);
        darkModeCard = findViewById(R.id.darkModeCard);
        exportCard = findViewById(R.id.exportCard);
        historyCard = findViewById(R.id.historyCard);
        incomeHistoryCard = findViewById(R.id.incomeHistoryCard);
        expenseHistoryCard = findViewById(R.id.expenseHistoryCard);
        
        // Initialize permission launcher
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    // Permission granted, can show notifications
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
                    // Show tutorial after permission is granted
                    checkAndShowTutorial();
                } else {
                    // Permission denied
                    Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        // Check and request notification permission
        checkNotificationPermission();
        
        // Set up click listeners
        setupClickListeners();
        
        // Load financial data
        updateFinancialSummary();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateFinancialSummary();
    }
    
    private void setupClickListeners() {
        incomeCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, IncomeActivity.class);
            startActivity(intent);
        });
        
        expenseCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ExpenseActivity.class);
            startActivity(intent);
        });
        
        darkModeCard.setOnClickListener(v -> toggleDarkMode());
        
        exportCard.setOnClickListener(v -> exportData());
        
        // Add listeners for transaction history cards
        historyCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TransactionListActivity.class);
            intent.putExtra("transaction_type", "all");
            startActivity(intent);
        });
        
        incomeHistoryCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TransactionListActivity.class);
            intent.putExtra("transaction_type", "income");
            startActivity(intent);
        });
        
        expenseHistoryCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TransactionListActivity.class);
            intent.putExtra("transaction_type", "expense");
            startActivity(intent);
        });
    }
    
    private void updateFinancialSummary() {
        double balance = dbHelper.getTotalBalance();
        double totalIncome = 0;
        double totalExpense = 0;
        
        // Get all transactions to calculate income and expense totals
        List<Transaction> transactions = dbHelper.getAllTransactions();
        for (Transaction transaction : transactions) {
            if (transaction.getAmount() >= 0) {
                totalIncome += transaction.getAmount();
            } else {
                totalExpense += Math.abs(transaction.getAmount());
            }
        }
        
        // Format with Euro symbol
        totalBalanceText.setText(String.format("€%.2f", balance));
        totalIncomeText.setText(String.format("€%.2f", totalIncome));
        totalExpenseText.setText(String.format("€%.2f", totalExpense));
    }
    
    private void toggleDarkMode() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean(DARK_MODE_KEY, false);
        
        // Toggle dark mode
        isDarkMode = !isDarkMode;
        
        // Save preference
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(DARK_MODE_KEY, isDarkMode);
        editor.apply();
        
        // Apply theme
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        
        // Recreate activity to apply theme
        recreate();
    }
    
    private void exportData() {
        boolean success = dbHelper.exportToCSV();
        if (success) {
            Toast.makeText(this, "Data exported successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to export data", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void checkAndShowTutorial() {
        TutorialHelper tutorialHelper = new TutorialHelper(this);
        if (tutorialHelper.isFirstLaunch()) {
            tutorialHelper.startTutorial();
        }
    }
    
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            } else {
                // Permission already granted, show tutorial
                checkAndShowTutorial();
            }
        } else {
            // For Android versions below 13, no permission needed, show tutorial
            checkAndShowTutorial();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_settings) {
            // Open settings
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
}