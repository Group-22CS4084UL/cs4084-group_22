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
    private final List<Transaction> transactions;

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

    public Transaction getTransaction(int position) {
        if (position >= 0 && position < transactions.size()) {
            return transactions.get(position);
        }
        return null;
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

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final ItemTransactionBinding binding;

        public TransactionViewHolder(ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Transaction transaction) {
            binding.titleText.setText(transaction.getTitle());
            binding.descriptionText.setText(transaction.getDescription());
            binding.dateText.setText(transaction.getDate());
            
            double amount = transaction.getAmount();
            String amountText = String.format(Locale.getDefault(), "%.2f", Math.abs(amount));
            binding.amountText.setText(amountText);
            
            // Set text color based on transaction type
            int textColor = amount >= 0 ? 
                binding.getRoot().getContext().getColor(R.color.income_green) :
                binding.getRoot().getContext().getColor(R.color.expense_red);
            binding.amountText.setTextColor(textColor);
        }
    }
}
