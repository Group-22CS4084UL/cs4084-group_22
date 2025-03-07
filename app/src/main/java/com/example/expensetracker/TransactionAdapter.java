package com.example.expensetracker;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * TransactionAdapter: Manages the display of transaction items in RecyclerView
 * Features:
 * - Custom view holder for transaction items
 * - Color-coded display (green for income, red for expenses)
 * - Date formatting
 * - Swipe-to-delete functionality
 * - Search and date filtering
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private Cursor cursor;
    private final SimpleDateFormat dateFormat;
    private final DatabaseHelper dbHelper;
    private final Context context;
    private OnTransactionDeleteListener deleteListener;

    // Database column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_NOTE = "note";

    /**
     * Interface for handling transaction deletion events
     */
    public interface OnTransactionDeleteListener {
        void onTransactionDeleted();
    }

    /**
     * Constructor for the adapter
     * @param context Application context
     * @param listener Listener for delete events
     */
    public TransactionAdapter(Context context, OnTransactionDeleteListener listener) {
        this.context = context;
        this.deleteListener = listener;
        this.dbHelper = new DatabaseHelper(context);
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the transaction item layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            try {
                // Get column indices
                int amountIndex = cursor.getColumnIndexOrThrow(COLUMN_AMOUNT);
                int categoryIndex = cursor.getColumnIndexOrThrow(COLUMN_CATEGORY);
                int noteIndex = cursor.getColumnIndexOrThrow(COLUMN_NOTE);
                int dateIndex = cursor.getColumnIndexOrThrow(COLUMN_DATE);
                int typeIndex = cursor.getColumnIndexOrThrow(COLUMN_TYPE);

                // Extract data from cursor
                double amount = cursor.getDouble(amountIndex);
                String category = cursor.getString(categoryIndex);
                String note = cursor.getString(noteIndex);
                long dateMillis = cursor.getLong(dateIndex);
                String type = cursor.getString(typeIndex);

                // Format and display data
                holder.amountText.setText(String.format(Locale.getDefault(), "$%.2f", amount));
                holder.categoryText.setText(category != null ? category : "");
                holder.noteText.setText(note != null ? note : "");
                holder.dateText.setText(dateFormat.format(new Date(dateMillis)));

                // Set text color based on transaction type (green for income, red for expense)
                int color = "income".equals(type) ? 0xFF4CAF50 : 0xFFF44336;
                holder.amountText.setTextColor(color);
            } catch (IllegalArgumentException e) {
                // Handle missing columns gracefully
                holder.amountText.setText("$0.00");
                holder.categoryText.setText("");
                holder.noteText.setText("");
                holder.dateText.setText("");
            }
        }
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    /**
     * Updates the adapter with new cursor data
     * @param newCursor New cursor containing transaction data
     */
    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();  // Close old cursor to prevent memory leaks
        }
        cursor = newCursor;
        notifyDataSetChanged();  // Refresh the RecyclerView
    }

    /**
     * Deletes a transaction at the specified position
     * @param position Position of the item to delete
     */
    public void deleteItem(int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            try {
                int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
                long id = cursor.getLong(idIndex);
                dbHelper.deleteTransaction(id);
                deleteListener.onTransactionDeleted();
            } catch (IllegalArgumentException e) {
                // Handle missing ID column gracefully
            }
        }
    }

    /**
     * Returns the context associated with this adapter
     */
    public Context getContext() {
        return context;
    }

    /**
     * Filters transactions by date range
     * @param startDate Start date in milliseconds
     * @param endDate End date in milliseconds
     */
    public void filterByDate(long startDate, long endDate) {
        Cursor filteredCursor = dbHelper.getTransactionsByDateRange(startDate, endDate);
        swapCursor(filteredCursor);
    }

    /**
     * Filters transactions by search query
     * @param query Search query string
     */
    public void filterBySearch(String query) {
        Cursor filteredCursor = dbHelper.searchTransactions(query);
        swapCursor(filteredCursor);
    }

    /**
     * ViewHolder class for transaction items
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView amountText;    // Displays transaction amount
        TextView categoryText;  // Displays transaction category
        TextView noteText;      // Displays transaction note
        TextView dateText;      // Displays transaction date

        ViewHolder(View itemView) {
            super(itemView);
            amountText = itemView.findViewById(R.id.amountText);
            categoryText = itemView.findViewById(R.id.categoryText);
            noteText = itemView.findViewById(R.id.noteText);
            dateText = itemView.findViewById(R.id.dateText);
        }
    }
}
