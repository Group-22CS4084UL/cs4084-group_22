package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.cardview.widget.CardView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.expensetracker.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * MainActivity: The main dashboard of the Expense Tracker app
 * Features:
 * - Display total balance, income, and expenses
 * - Navigation to Income, Expense, and Visualization activities
 * - Theme switching (Light/Dark mode) (TBC)
 * - Data export functionality (TBC)
 * - Notification management (TBC)
 */
public class MainActivity extends AppCompatActivity {
    // UI Elements
    private TextView totalBalanceText, totalIncomeText, totalExpenseText;
    private CardView incomeCard, expenseCard, visualizeCard;

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize all UL elements
        initializeViews();

        // Set up click listeners for navigation cards
        setupClickListeners();

        // Update dashboard with latest financial data
        updateDashboard();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
            }
        });
    }
    /**
     * Updates the dashboard with latest financial data
     * Calculates and displays total income, expenses, and balance
     */
    private void updateDashboard() {
        double totalIncome = 2800.9;
        double totalExpense = 1789.3;
        double balance = totalIncome - totalExpense;

        // Format and display monetary values with 2 decimal places
        totalIncomeText.setText(String.format("$%.2f", totalIncome));
        totalExpenseText.setText(String.format("$%.2f", totalExpense));
        totalBalanceText.setText(String.format("$%.2f", balance));
    }
    /**
     * Sets up click listeners for all interactive elements
     */
    private void setupClickListeners() {
        // Navigate to Income activity
        incomeCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, IncomeActivity.class);
            startActivity(intent);
        });

        // Navigate to Expense activity
        expenseCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ExpenseActivity.class);
            startActivity(intent);
        });

        // Navigate to Visualization activity
        visualizeCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SpendingVisualizationActivity.class);
            startActivity(intent);
        });
    }
    /**
     * Initializes all UI elements from the layout
     */
    private void initializeViews() {
        totalBalanceText = findViewById(R.id.totalBalanceText);
        totalIncomeText = findViewById(R.id.totalIncomeText);
        totalExpenseText = findViewById(R.id.totalExpenseText);
        incomeCard = findViewById(R.id.incomeCard);
        expenseCard = findViewById(R.id.expenseCard);
        visualizeCard = findViewById(R.id.visualizeCard);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update dashboard whenever activity becomes visible
        updateDashboard();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
