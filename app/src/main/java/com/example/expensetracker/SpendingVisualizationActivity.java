package com.example.expensetracker;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpendingVisualizationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupActionBar();
        setupPieChart();
        loadTransactions();
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
