package com.example.expensetracker;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying transaction items in a RecyclerView
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private List<Transaction> transactions = new ArrayList<>();

    /**
     * Updates the transaction list and refreshes the view
     * @param newTransactions New list of transactions to display
     */
    public void setTransactions(List<Transaction> newTransactions) {
        if (newTransactions == null) {
            return;
        }
        int oldSize = this.transactions.size();
        this.transactions.clear();
        notifyItemRangeRemoved(0, oldSize);
        this.transactions.addAll(newTransactions);
        notifyItemRangeInserted(0, newTransactions.size());
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(itemView);
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
    
    /**
     * Removes a transaction at the specified position
     * @param position Position of the transaction to remove
     */
    public void removeItem(int position) {
        if (position >= 0 && position < transactions.size()) {
            transactions.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    /**
     * ViewHolder for transaction items
     */
    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView transactionTitle;
        private final TextView transactionDescription;
        private final TextView transactionDate;
        private final TextView transactionAmount;
        
        TransactionViewHolder(View itemView) {
            super(itemView);
            transactionTitle = itemView.findViewById(R.id.transactionTitle);
            transactionDescription = itemView.findViewById(R.id.transactionDescription);
            transactionDate = itemView.findViewById(R.id.transactionDate);
            transactionAmount = itemView.findViewById(R.id.transactionAmount);
        }
        
        /**
         * Binds transaction data to the view
         * @param transaction Transaction to display
         */
        void bind(Transaction transaction) {
            transactionTitle.setText(transaction.getCategory());
            transactionDescription.setText(transaction.getDescription());
            transactionDate.setText(transaction.getDate());
            
            // Set amount with appropriate formatting
            String amountText = String.format("%.2f", transaction.getAmount());
            if (transaction.getAmount() >= 0) {
                transactionAmount.setTextColor(Color.GREEN);
                transactionAmount.setText("+" + amountText);
            } else {
                transactionAmount.setTextColor(Color.RED);
                transactionAmount.setText(amountText);
            }
        }
    }
}
