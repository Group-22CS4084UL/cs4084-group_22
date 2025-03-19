package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expensetracker.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private TransactionAdapter adapter;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        
        // Initialize database helper
        dbHelper = new DatabaseHelper(this);
        
        // Set up RecyclerView
        adapter = new TransactionAdapter();
        binding.contentMain.transactionList.setLayoutManager(new LinearLayoutManager(this));
        binding.contentMain.transactionList.setAdapter(adapter);
        
        // Load transactions from database
        loadTransactions();
        
        // Set up FABs for adding income and expenses
        binding.fabAddIncome.setOnClickListener(view -> {
            // TODO: Implement add income functionality
            Snackbar.make(view, "Add income clicked", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });
        
        binding.fabAddExpense.setOnClickListener(view -> {
            // TODO: Implement add expense functionality
            Snackbar.make(view, "Add expense clicked", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTransactions();
    }

    private void loadTransactions() {
        adapter.setTransactions(dbHelper.getAllTransactions());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_view_spending) {
            Intent intent = new Intent(this, SpendingVisualizationActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}