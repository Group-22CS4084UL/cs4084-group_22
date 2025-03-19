package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import com.example.expensetracker.databinding.ActivityMainBinding;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private TransactionAdapter adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);
        setupRecyclerView();
        setupFABs();
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter();
        binding.transactionList.setLayoutManager(new LinearLayoutManager(this));
        binding.transactionList.setAdapter(adapter);
        
        // Add swipe to delete
        SwipeToDeleteCallback swipeHandler = new SwipeToDeleteCallback(adapter, this) {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Transaction transaction = adapter.getTransaction(position);
                if (transaction != null) {
                    dbHelper.deleteTransaction(transaction.getId());
                    adapter.removeTransaction(position);
                }
            }
        };
        
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeHandler);
        itemTouchHelper.attachToRecyclerView(binding.transactionList);
    }

    private void setupFABs() {
        binding.addIncomeFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, IncomeActivity.class);
            startActivity(intent);
        });

        binding.addExpenseFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExpenseActivity.class);
            startActivity(intent);
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_view_spending) {
            Intent intent = new Intent(this, SpendingVisualizationActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}