package com.example.expensetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expensetracker.databinding.ItemTransactionBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private List<Transaction> transactions;

    public TransactionAdapter() {
        this.transactions = new ArrayList<>();
    }

    public void setTransactions(List<Transaction> newTransactions) {
        int oldSize = this.transactions.size();
        this.transactions.clear();
        notifyItemRangeRemoved(0, oldSize);
        this.transactions.addAll(newTransactions);
        notifyItemRangeInserted(0, newTransactions.size());
    }

    public void removeTransaction(int position) {
        if (position >= 0 && position < transactions.size()) {
            transactions.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransactionBinding binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new TransactionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final ItemTransactionBinding binding;

        TransactionViewHolder(ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Transaction transaction) {
            binding.titleText.setText(transaction.getTitle());
            binding.descriptionText.setText(transaction.getDescription());
            binding.amountText.setText(String.format(Locale.getDefault(), "%.2f", transaction.getAmount()));
            binding.dateText.setText(transaction.getDate());
            
            // Set text color based on transaction type
            int color = transaction.getAmount() < 0 
                ? binding.getRoot().getContext().getColor(android.R.color.holo_red_dark)
                : binding.getRoot().getContext().getColor(android.R.color.holo_green_dark);
            binding.amountText.setTextColor(color);
        }
    }
}
