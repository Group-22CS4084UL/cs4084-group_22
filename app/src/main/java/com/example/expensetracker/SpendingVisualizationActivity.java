package com.example.expensetracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.expensetracker.databinding.ActivitySpendingVisualizationBinding;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SpendingVisualizationActivity extends AppCompatActivity {
    private ActivitySpendingVisualizationBinding binding;
    private List<Transaction> transactions;
    private DatabaseHelper databaseHelper;
    private static final int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpendingVisualizationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        
        setupActionBar();
        setupPieChart();
        loadTransactions();
        setupExportButton();
    }

    private void setupExportButton() {
        binding.exportButton.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                exportTransactions();
            } else {
                requestStoragePermission();
            }
        });
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportTransactions();
            } else {
                Toast.makeText(this, "Storage permission is required to export data", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void exportTransactions() {
        String filePath = databaseHelper.exportToCSV(this);
        if (filePath != null) {
            Toast.makeText(this, "Transactions exported to: " + filePath, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to export transactions or no data to export", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupActionBar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Spending Visualization");
        }
    }

    private void setupPieChart() {
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setHoleColor(android.R.color.white);
        binding.pieChart.setTransparentCircleRadius(61f);
        binding.pieChart.setEntryLabelTextSize(12f);
    }

    private void loadTransactions() {
        // TODO: Load transactions from database
        transactions = new ArrayList<>(); // Temporary empty list
        updateChart();
    }

    private void updateChart() {
        if (transactions.isEmpty()) {
            binding.pieChart.setNoDataText("No spending data available");
            binding.pieChart.invalidate();
            return;
        }

        Map<String, Double> categoryTotals = transactions.stream()
                .filter(t -> t.getAmount() < 0) // Only consider expenses
                .collect(Collectors.groupingBy(
                        Transaction::getTitle,
                        Collectors.summingDouble(t -> Math.abs(t.getAmount()))
                ));

        List<PieEntry> entries = categoryTotals.entrySet().stream()
                .map(e -> new PieEntry(e.getValue().floatValue(), e.getKey()))
                .collect(Collectors.toList());

        PieDataSet dataSet = new PieDataSet(entries, "Spending Categories");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.invalidate();
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
