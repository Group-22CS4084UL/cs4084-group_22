package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TransactionListActivity extends AppCompatActivity implements TransactionAdapter.OnTransactionClickListener {
    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private DatabaseHelper dbHelper;
    private TextView emptyView;
    private String transactionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get transaction type from intent
        transactionType = getIntent().getStringExtra("transaction_type");
        if (transactionType == null) {
            transactionType = "all"; // Default to showing all transactions
        }

        // Set title based on transaction type
        if (transactionType.equals("income")) {
            setTitle("Income History");
        } else if (transactionType.equals("expense")) {
            setTitle("Expense History");
        } else {
            setTitle("All Transactions");
        }

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter();
        adapter.setOnTransactionClickListener(this);
        recyclerView.setAdapter(adapter);

        // Load transactions
        loadTransactions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload transactions when returning to this activity
        loadTransactions();
    }

    private void loadTransactions() {
        List<Transaction> transactions;
        
        // Get transactions based on type
        if (transactionType.equals("income")) {
            transactions = dbHelper.getTransactionsByType("income");
        } else if (transactionType.equals("expense")) {
            transactions = dbHelper.getTransactionsByType("expense");
        } else {
            transactions = dbHelper.getAllTransactions();
        }

        // Update adapter and empty view visibility
        adapter.setTransactions(transactions);
        if (transactions.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            if (transactionType.equals("income")) {
                emptyView.setText("No income records found");
            } else if (transactionType.equals("expense")) {
                emptyView.setText("No expense records found");
            } else {
                emptyView.setText("No transactions found");
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTransactionClick(Transaction transaction) {
        // Open transaction edit activity
        Intent intent = new Intent(this, TransactionEditActivity.class);
        intent.putExtra("transaction_id", transaction.getId());
        startActivity(intent);
    }
}
