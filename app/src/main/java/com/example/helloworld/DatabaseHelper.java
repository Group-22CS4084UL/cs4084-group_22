package com.example.expensetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * DatabaseHelper: Manages all database operations for the Expense Tracker app
 * Features:
 * - SQLite database creation and upgrades
 * - CRUD operations for transactions
 * - Data aggregation and filtering
 * - CSV export functionality
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    // Database metadata
    private static final String DATABASE_NAME = "ExpenseTracker.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_TRANSACTIONS = "transactions";

    // Column names for the transactions table
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_NOTE = "note";

    // SQL query to create the transactions table
    private static final String CREATE_TABLE_TRANSACTIONS = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"  // Unique identifier
            + COLUMN_AMOUNT + " REAL,"                          // Transaction amount
            + COLUMN_TYPE + " TEXT,"                            // 'income' or 'expense'
            + COLUMN_CATEGORY + " TEXT,"                        // Transaction category
            + COLUMN_DATE + " INTEGER,"                         // Timestamp in milliseconds
            + COLUMN_NOTE + " TEXT"                            // Optional note
            + ")";

    /**
     * Constructor - creates a new database helper
     * @param context The application context
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the transactions table when database is first created
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple upgrade policy - drop and recreate table
        // In a production app, you would want to migrate data
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        onCreate(db);
    }

    /**
     * Adds a new transaction to the database
     * @param amount Transaction amount
     * @param type Transaction type ('income' or 'expense')
     * @param category Transaction category
     * @param note Optional note
     * @return Row ID of the newly inserted transaction, or -1 if error
     */
    public long addTransaction(double amount, String type, String category, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        // Prepare values for insertion
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DATE, System.currentTimeMillis());  // Current timestamp
        values.put(COLUMN_NOTE, note);
        
        return db.insert(TABLE_TRANSACTIONS, null, values);
    }

    /**
     * Deletes a transaction from the database
     * @param id ID of the transaction to delete
     */
    public void deleteTransaction(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANSACTIONS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    /**
     * Calculates the total income
     * @return Sum of all income transactions
     */
    public double getTotalIncome() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COLUMN_TYPE + "=?", new String[]{"income"});
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    /**
     * Calculates the total expenses
     * @return Sum of all expense transactions
     */
    public double getTotalExpense() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COLUMN_TYPE + "=?", new String[]{"expense"});
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    /**
     * Retrieves all transactions of a specific type
     * @param type Transaction type ('income' or 'expense')
     * @return Cursor containing matching transactions
     */
    public Cursor getTransactionsByType(String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TRANSACTIONS, null,
                COLUMN_TYPE + "=?", new String[]{type},
                null, null, COLUMN_DATE + " DESC");
    }

    /**
     * Retrieves transactions within a specific date range
     * @param startDate Start timestamp in milliseconds
     * @param endDate End timestamp in milliseconds
     * @return Cursor containing matching transactions
     */
    public Cursor getTransactionsByDateRange(long startDate, long endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TRANSACTIONS, null,
                COLUMN_DATE + " BETWEEN ? AND ?",
                new String[]{String.valueOf(startDate), String.valueOf(endDate)},
                null, null, COLUMN_DATE + " ASC");
    }

    /**
     * Searches transactions by category or note
     * @param query Search query string
     * @return Cursor containing matching transactions
     */
    public Cursor searchTransactions(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String searchQuery = "%" + query + "%";
        String selection = COLUMN_CATEGORY + " LIKE ? OR " + COLUMN_NOTE + " LIKE ?";
        String[] selectionArgs = {searchQuery, searchQuery};
        return db.query(TABLE_TRANSACTIONS, null,
                selection, selectionArgs,
                null, null, COLUMN_DATE + " DESC");
    }

    /**
     * Exports all transactions to CSV format
     * @return CSV string containing all transactions
     */
    public String exportToCSV() {
        SQLiteDatabase db = this.getReadableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        StringBuilder csvData = new StringBuilder();

        // Add CSV header
        csvData.append("Date,Type,Category,Amount,Note\n");

        // Get all transactions
        Cursor cursor = db.query(TABLE_TRANSACTIONS, null, null, null,
                null, null, COLUMN_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                // Format date
                long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String date = dateFormat.format(new Date(dateMillis));

                // Get other fields
                String type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT));
                String note = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE));

                // Handle special characters in notes for CSV format
                if (note != null) {
                    note = note.replace("\"", "\"\"");  // Escape quotes
                    note = "\"" + note + "\"";          // Wrap in quotes
                } else {
                    note = "";
                }

                // Add transaction to CSV
                csvData.append(String.format("%s,%s,%s,%.2f,%s\n",
                        date, type, category, amount, note));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return csvData.toString();
    }

    /**
     * Retrieves all transactions from the database
     * @return List of all transactions
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        String[] columns = {
            COLUMN_DATE,
            COLUMN_TYPE,
            COLUMN_CATEGORY,
            COLUMN_AMOUNT,
            COLUMN_NOTE
        };

        Cursor cursor = db.query(TABLE_TRANSACTIONS, columns, null, null,
                null, null, COLUMN_DATE + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            int dateIndex = cursor.getColumnIndex(COLUMN_DATE);
            int typeIndex = cursor.getColumnIndex(COLUMN_TYPE);
            int categoryIndex = cursor.getColumnIndex(COLUMN_CATEGORY);
            int amountIndex = cursor.getColumnIndex(COLUMN_AMOUNT);
            int noteIndex = cursor.getColumnIndex(COLUMN_NOTE);

            // Only proceed if all columns are found
            if (dateIndex != -1 && typeIndex != -1 && categoryIndex != -1 && 
                amountIndex != -1 && noteIndex != -1) {
                
                do {
                    try {
                        long dateMillis = cursor.getLong(dateIndex);
                        String type = cursor.getString(typeIndex);
                        String category = cursor.getString(categoryIndex);
                        double amount = cursor.getDouble(amountIndex);
                        String note = cursor.getString(noteIndex);

                        // Format date
                        String date = dateFormat.format(new Date(dateMillis));

                        // Create and add transaction
                        Transaction transaction = new Transaction(date, type, amount, category, note);
                        transactions.add(transaction);
                    } catch (Exception e) {
                        Log.e("DatabaseHelper", "Error creating transaction: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            } else {
                Log.e("DatabaseHelper", "One or more required columns not found in cursor");
            }
            cursor.close();
        }
        return transactions;
    }
}
