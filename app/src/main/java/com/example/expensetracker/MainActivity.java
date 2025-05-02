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
    private String pendingPermission = null;
    private boolean pendingExport = false;
    private String lastExportedFilePath = null;

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
                    if ("android.permission.WRITE_EXTERNAL_STORAGE".equals(pendingPermission) && pendingExport) {
                        // Permission granted for export, proceed
                        exportDataInternal();
                        pendingExport = false;
                        pendingPermission = null;
                    } else if ("android.permission.POST_NOTIFICATIONS".equals(pendingPermission)) {
                        Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
                        checkAndShowTutorial();
                        pendingPermission = null;
                    } else {
                        Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                        pendingPermission = null;
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    pendingExport = false;
                    pendingPermission = null;
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
            if ("income".equalsIgnoreCase(transaction.getType())) {
                totalIncome += transaction.getAmount();
            } else if ("expense".equalsIgnoreCase(transaction.getType())) {
                totalExpense += transaction.getAmount();
            }
        }
        
        // Format with Euro symbol
        totalBalanceText.setText(String.format("€%.2f", balance));
        totalIncomeText.setText(String.format("€%.2f", totalIncome));
        totalExpenseText.setText(String.format("€%.2f", Math.abs(totalExpense)));
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
        // For Android 6.0+ (API 23+), check WRITE_EXTERNAL_STORAGE permission at runtime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                pendingPermission = "android.permission.WRITE_EXTERNAL_STORAGE";
                pendingExport = true;
                requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                Toast.makeText(this, "Storage permission required to export data", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        exportDataInternal();
    }

    private void exportDataInternal() {
        // Use the exportToCSV() that returns a boolean, but for file path, use exportToCSV(Context)
        String exportedFilePath = dbHelper.exportToCSV(this);
        if (exportedFilePath != null) {
            lastExportedFilePath = exportedFilePath;
            Toast.makeText(this, "Data exported to: " + exportedFilePath, Toast.LENGTH_LONG).show();
            showShareDialog(exportedFilePath);
        } else {
            Toast.makeText(this, "Failed to export data", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showShareDialog(String filePath) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Export Complete")
                .setMessage("Data exported to:\n" + filePath + "\n\nWould you like to share the file?")
                .setPositiveButton("Share", (dialog, which) -> shareExportedFile(filePath))
                .setNegativeButton("Dismiss", null)
                .show();
    }

    private void shareExportedFile(String filePath) {
        java.io.File file = new java.io.File(filePath);
        android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share exported CSV"));
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