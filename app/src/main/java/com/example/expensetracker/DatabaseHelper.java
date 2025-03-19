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
     * Updates an existing transaction
     * @param id ID of the transaction to update
     * @param amount New amount
     * @param type New type
     * @param category New category
     * @param note New note
     * @return Number of rows affected (should be 1 if successful)
     */
    public int updateTransaction(long id, double amount, String type, String category, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_NOTE, note);
        
        return db.update(TABLE_TRANSACTIONS, values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    /**
     * Gets all transactions from the database
     * @return List of Transaction objects
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String[] columns = {
            COLUMN_ID,
            COLUMN_AMOUNT,
            COLUMN_TYPE,
            COLUMN_CATEGORY,
            COLUMN_DATE,
            COLUMN_NOTE
        };
        
        Cursor cursor = db.query(TABLE_TRANSACTIONS, columns, null, null, null, null, COLUMN_DATE + " DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction(
                    cursor.getString(cursor.getColumnIndex(COLUMN_NOTE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT)),
                    formatDate(cursor.getLong(cursor.getColumnIndex(COLUMN_DATE)))
                );
                transaction.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                transactions.add(transaction);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return transactions;
    }

    /**
     * Gets the total balance (income - expenses)
     * @return Current balance
     */
    public double getBalance() {
        SQLiteDatabase db = this.getReadableDatabase();
        double balance = 0;
        
        String query = "SELECT SUM(CASE WHEN type = 'income' THEN amount ELSE -amount END) FROM " + TABLE_TRANSACTIONS;
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            balance = cursor.getDouble(0);
            cursor.close();
        }
        
        return balance;
    }

    /**
     * Gets transactions within a date range
     * @param startDate Start date in milliseconds
     * @param endDate End date in milliseconds
     * @return List of transactions within the range
     */
    public List<Transaction> getTransactionsByDateRange(long startDate, long endDate) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selection = COLUMN_DATE + " BETWEEN ? AND ?";
        String[] selectionArgs = {String.valueOf(startDate), String.valueOf(endDate)};
        
        Cursor cursor = db.query(TABLE_TRANSACTIONS, null, selection, selectionArgs,
                null, null, COLUMN_DATE + " DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction(
                    cursor.getString(cursor.getColumnIndex(COLUMN_NOTE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT)),
                    formatDate(cursor.getLong(cursor.getColumnIndex(COLUMN_DATE)))
                );
                transaction.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                transactions.add(transaction);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return transactions;
    }

    /**
     * Gets transactions by category
     * @param category Category to filter by
     * @return List of transactions in the category
     */
    public List<Transaction> getTransactionsByCategory(String category) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selection = COLUMN_CATEGORY + " = ?";
        String[] selectionArgs = {category};
        
        Cursor cursor = db.query(TABLE_TRANSACTIONS, null, selection, selectionArgs,
                null, null, COLUMN_DATE + " DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction(
                    cursor.getString(cursor.getColumnIndex(COLUMN_NOTE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT)),
                    formatDate(cursor.getLong(cursor.getColumnIndex(COLUMN_DATE)))
                );
                transaction.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                transactions.add(transaction);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return transactions;
    }

    /**
     * Formats a timestamp into a readable date string
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date string
     */
    private String formatDate(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }
}
