package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.expensetracker.databinding.ActivityMainBinding;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private TransactionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        setupRecyclerView();
        setupFABs();
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(this);
        binding.contentMain.transactionList.setLayoutManager(new LinearLayoutManager(this));
        binding.contentMain.transactionList.setAdapter(adapter);
        
        // TODO: Load transactions from database
        adapter.setTransactions(new ArrayList<>());
    }

    private void setupFABs() {
        binding.fabIncome.setOnClickListener(v -> {
            Intent intent = new Intent(this, IncomeActivity.class);
            startActivity(intent);
        });

        binding.fabExpense.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExpenseActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: Refresh transactions from database
        adapter.setTransactions(new ArrayList<>());
    }
}