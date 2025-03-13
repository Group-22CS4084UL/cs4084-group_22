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


// Adapter class: used to display expense records from the database into the RecyclerView.
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private Cursor cursor;// Result sets (cursors) returned from database queries.
    private final SimpleDateFormat dateFormat; // Date formatting tool.
    private final DatabaseHelper dbHelper;// Database operation helper classes.
    private final Context context;// Context objects
    private final OnTransactionDeleteListener deleteListener;// Remove the event listener to notify the external interface of a refresh.

    // Column names in the data table
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_NOTE = "note";

    // Delete callback interface so that the Activity can sense the delete action and trigger a refresh.
    public interface OnTransactionDeleteListener {
        void onTransactionDeleted();
    }

    // Constructor methods to initialise contexts, listeners, database tool classes and date formats
    public TransactionAdapter(Context context, OnTransactionDeleteListener listener) {
        this.context = context;
        this.deleteListener = listener;
        this.dbHelper = new DatabaseHelper(context);
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    // Create the ViewHolder, which is responsible for loading the layout of each Item.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }


    // Bind data to each ViewHolder.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the index of each column
        if (cursor != null && cursor.moveToPosition(position)) {
            int amountIndex = cursor.getColumnIndexOrThrow(COLUMN_AMOUNT);
            int categoryIndex = cursor.getColumnIndexOrThrow(COLUMN_CATEGORY);
            int noteIndex = cursor.getColumnIndexOrThrow(COLUMN_NOTE);
            int dateIndex = cursor.getColumnIndexOrThrow(COLUMN_DATE);

            // Read the data for each field
            double amount = cursor.getDouble(amountIndex);
            String category = cursor.getString(categoryIndex);
            String note = cursor.getString(noteIndex);
            long dateMillis = cursor.getLong(dateIndex);

            // Populate the interface with data
            holder.amountText.setText(String.format(Locale.getDefault(), "$%.2f", amount));
            holder.categoryText.setText(category != null ? category : "");// Avoid null pointers
            holder.noteText.setText(note != null ? note : "");// Avoid null pointers
            holder.dateText.setText(dateFormat.format(new Date(dateMillis)));// Timestamp to date

            // All records are expenditures, set the amount to red
            holder.amountText.setTextColor(0xFFF44336);
        }
    }

    // Returns the number of list items, based on the number of cursor records.
    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }


    // Update the data source (switch cursors) and refresh the interface
    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();// Close old cursors to prevent memory leaks
        }
        cursor = newCursor;// Replace the new cursor
        notifyDataSetChanged();// Notify the RecyclerView to refresh.
    }


    // Delete a record in a certain location
    public void deleteItem(int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
            long id = cursor.getLong(idIndex);
            dbHelper.deleteTransaction(id);// Call DatabaseHelper to delete records
            deleteListener.onTransactionDeleted();// Notify external refresh
        }
    }



    // Filter records by date range
    public void filterByDate(long startDate, long endDate) {
        Cursor filteredCursor = dbHelper.getExpensesByDateRange(startDate, endDate);
        swapCursor(filteredCursor);// Switch to filtered data
    }


    // Search records by keyword
    public void filterBySearch(String query) {
        Cursor filteredCursor = dbHelper.searchExpenses(query);
        swapCursor(filteredCursor);// Switch to search results
    }


    // Get context for external use
    public Context getContext() {
        return context;
    }

    // ViewHolder: responsible for caching references to controls in the Item to improve performance.
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView amountText;
        TextView categoryText;
        TextView noteText;
        TextView dateText;

        // Initialise references to each TextView.
        ViewHolder(View itemView) {
            super(itemView);
            amountText = itemView.findViewById(R.id.amountText);
            categoryText = itemView.findViewById(R.id.categoryText);
            noteText = itemView.findViewById(R.id.noteText);
            dateText = itemView.findViewById(R.id.dateText);
        }
    }
}
