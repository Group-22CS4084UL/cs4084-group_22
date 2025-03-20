package com.example.expensetracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {
    // UI Elements
    private TextView totalBalanceText, totalIncomeText, totalExpenseText;
    private MaterialCardView incomeCard, expenseCard, visualizeCard;
    private MaterialCardView darkModeCard, exportCard;
    
    // Database helper instance
    private DatabaseHelper dbHelper;
    
    // Constants for SharedPreferences
    private static final String PREFS_NAME = "ExpenseTrackerPrefs";
    private static final String DARK_MODE_KEY = "darkMode";

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
        visualizeCard = findViewById(R.id.visualizeCard);
        darkModeCard = findViewById(R.id.darkModeCard);
        exportCard = findViewById(R.id.exportCard);
        
        // Set up click listeners
        setupClickListeners();
        
        // Load financial data
        updateFinancialSummary();
        
        // Show tutorial if first launch
        checkAndShowTutorial();
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
        
        visualizeCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SpendingVisualizationActivity.class);
            startActivity(intent);
        });
        
        darkModeCard.setOnClickListener(v -> toggleDarkMode());
        
        exportCard.setOnClickListener(v -> exportData());
    }
    
    private void updateFinancialSummary() {
        double totalIncome = dbHelper.getTotalIncome();
        double totalExpense = dbHelper.getTotalExpense();
        double balance = totalIncome - totalExpense;
        
        totalBalanceText.setText(String.format("$%.2f", balance));
        totalIncomeText.setText(String.format("$%.2f", totalIncome));
        totalExpenseText.setText(String.format("$%.2f", totalExpense));
    }
    
    private void toggleDarkMode() {
        // Get current mode
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean(DARK_MODE_KEY, false);
        
        // Toggle mode
        isDarkMode = !isDarkMode;
        
        // Save preference
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(DARK_MODE_KEY, isDarkMode);
        editor.apply();
        
        // Apply mode
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Toast.makeText(this, "Dark mode enabled", Toast.LENGTH_SHORT).show();
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Toast.makeText(this, "Light mode enabled", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void exportData() {
        try {
            String filePath = dbHelper.exportToCSV(this);
            if (filePath != null) {
                Toast.makeText(this, "Data exported to: " + filePath, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void checkAndShowTutorial() {
        TutorialHelper tutorialHelper = new TutorialHelper(this);
        if (tutorialHelper.isFirstLaunch()) {
            Toast.makeText(this, "Welcome to Expense Tracker! Tap on cards to manage your finances.", Toast.LENGTH_LONG).show();
            tutorialHelper.setFirstLaunchComplete();
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
        
        if (id == R.id.action_view_spending) {
            Intent intent = new Intent(MainActivity.this, SpendingVisualizationActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_settings) {
            Toast.makeText(this, "Settings not implemented yet", Toast.LENGTH_SHORT).show();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
}