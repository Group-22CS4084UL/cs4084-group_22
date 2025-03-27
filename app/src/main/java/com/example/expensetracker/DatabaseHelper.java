package com.example.expensetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;
import java.io.FileWriter;
import java.io.IOException;
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
            + COLUMN_DATE + " TEXT,"                            // Date in dd/MM/yyyy format
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
        // Call the overloaded method with the current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
        String currentDate = dateFormat.format(new Date());
        return addTransaction(amount, type, category, note, currentDate);
    }
    
    /**
     * Adds a new transaction to the database with a specific date
     * @param amount Transaction amount
     * @param type Transaction type ('income' or 'expense')
     * @param category Transaction category
     * @param note Optional note
     * @param date Transaction date in format yyyy-MM-dd
     * @return Row ID of the newly inserted transaction, or -1 if error
     */
    public long addTransaction(double amount, String type, String category, String note, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        // Convert date format from yyyy-MM-dd to dd/MM/yyyy if needed
        String formattedDate = date;
        if (date != null && date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
                Date parsedDate = inputFormat.parse(date);
                if (parsedDate != null) {
                    formattedDate = outputFormat.format(parsedDate);
                }
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error parsing date: " + e.getMessage());
            }
        }
        
        // Prepare values for insertion
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DATE, formattedDate);
        values.put(COLUMN_NOTE, note);
        
        return db.insert(TABLE_TRANSACTIONS, null, values);
    }

    /**
     * Deletes a transaction from the database
     * @param id ID of the transaction to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteTransaction(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_TRANSACTIONS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}) > 0;
    }
    
    /**
     * Updates an existing transaction in the database
     * @param id Transaction ID
     * @param amount New amount
     * @param type New type ('income' or 'expense')
     * @param category New category
     * @param note New note
     * @param date New date
     * @return true if successful, false otherwise
     */
    public boolean updateTransaction(long id, double amount, String type, String category, String note, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        // Prepare updated values
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_NOTE, note);
        
        return db.update(TABLE_TRANSACTIONS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}) > 0;
    }
    
    /**
     * Gets a transaction by its ID
     * @param id Transaction ID
     * @return Transaction object, or null if not found
     */
    public Transaction getTransactionById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TRANSACTIONS, null, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        
        Transaction transaction = null;
        if (cursor != null && cursor.moveToFirst()) {
            transaction = cursorToTransaction(cursor);
            cursor.close();
        }
        
        return transaction;
    }
    
    /**
     * Gets all transactions from the database
     * @return List of all transactions
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Query all transactions, ordered by date (newest first)
        Cursor cursor = db.query(TABLE_TRANSACTIONS, null, null, null, null, null,
                COLUMN_DATE + " DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                transactions.add(cursorToTransaction(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return transactions;
    }
    
    /**
     * Gets transactions of a specific type ('income' or 'expense')
     * @param type Transaction type
     * @return List of transactions of the specified type
     */
    public List<Transaction> getTransactionsByType(String type) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Query transactions by type, ordered by date (newest first)
        Cursor cursor = db.query(TABLE_TRANSACTIONS, null, COLUMN_TYPE + " = ?",
                new String[]{type}, null, null, COLUMN_DATE + " DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                transactions.add(cursorToTransaction(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return transactions;
    }
    
    /**
     * Helper method to convert a cursor to a Transaction object
     * @param cursor Database cursor positioned at a transaction record
     * @return Transaction object
     */
    private Transaction cursorToTransaction(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
        double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT));
        String type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
        String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
        String note = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE));
        
        return new Transaction(id, amount, type, category, date, note);
    }
    
    /**
     * Gets the total balance (income - expenses)
     * @return Total balance
     */
    public double getTotalBalance() {
        SQLiteDatabase db = this.getReadableDatabase();
        double totalIncome = 0;
        double totalExpense = 0;
        
        // Get total income
        Cursor incomeCursor = db.rawQuery(
                "SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                        " WHERE " + COLUMN_TYPE + " = ?", new String[]{"income"});
        if (incomeCursor != null && incomeCursor.moveToFirst()) {
            totalIncome = incomeCursor.getDouble(0);
            incomeCursor.close();
        }
        
        // Get total expense
        Cursor expenseCursor = db.rawQuery(
                "SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                        " WHERE " + COLUMN_TYPE + " = ?", new String[]{"expense"});
        if (expenseCursor != null && expenseCursor.moveToFirst()) {
            totalExpense = expenseCursor.getDouble(0);
            expenseCursor.close();
        }
        
        return totalIncome - totalExpense;
    }

    /**
     * Gets the total income from all transactions
     * @return Total income amount
     */
    public double getTotalIncome() {
        SQLiteDatabase db = this.getReadableDatabase();
        double totalIncome = 0;
        
        String query = "SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS + 
                       " WHERE " + COLUMN_TYPE + " = 'income'";
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            totalIncome = cursor.getDouble(0);
            cursor.close();
        }
        
        return totalIncome;
    }

    /**
     * Gets the total expenses from all transactions
     * @return Total expense amount (as a positive value)
     */
    public double getTotalExpense() {
        SQLiteDatabase db = this.getReadableDatabase();
        double totalExpense = 0;
        
        String query = "SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS + 
                       " WHERE " + COLUMN_TYPE + " = 'expense'";
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            totalExpense = Math.abs(cursor.getDouble(0));
            cursor.close();
        }
        
        return totalExpense;
    }

    /**
     * Exports all transactions to a CSV file
     * @return true if export was successful, false otherwise
     */
    public boolean exportToCSV() {
        try {
            List<Transaction> transactions = getAllTransactions();
            if (transactions.isEmpty()) {
                return false;
            }
            
            // Get the Downloads directory
            java.io.File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            
            // Create a unique filename with timestamp
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String timestamp = dateFormat.format(new Date());
            String fileName = "expense_tracker_export_" + timestamp + ".csv";
            java.io.File exportFile = new java.io.File(downloadsDir, fileName);
            
            FileWriter fw = new FileWriter(exportFile);
            // Write CSV header
            fw.append("ID,Type,Category,Amount,Date,Note\n");
            
            // Write transaction data
            for (Transaction transaction : transactions) {
                fw.append(String.valueOf(transaction.getId())).append(",");
                fw.append(transaction.getType()).append(",");
                fw.append(transaction.getCategory()).append(",");
                fw.append(String.format(Locale.UK, "€%.2f", transaction.getAmount())).append(",");
                fw.append(transaction.getDate()).append(",");
                
                // Handle notes that might contain commas by enclosing in quotes
                String note = transaction.getDescription();
                if (note != null && !note.isEmpty()) {
                    fw.append("\"").append(note.replace("\"", "\"\"")).append("\"");
                }
                fw.append("\n");
            }
            
            fw.flush();
            fw.close();
            return true;
        } catch (IOException e) {
            Log.e("DatabaseHelper", "Error exporting data: " + e.getMessage());
            return false;
        }
    }

    /**
     * Exports all transactions to a CSV file
     * @param context The context to use for file operations
     * @return The path to the exported file, or null if export failed
     */
    public String exportToCSV(Context context) {
        try {
            List<Transaction> transactions = getAllTransactions();
            if (transactions.isEmpty()) {
                return null;
            }
            
            // Get the Downloads directory
            java.io.File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            
            // Create a unique filename with timestamp
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String timestamp = dateFormat.format(new Date());
            String fileName = "expense_tracker_export_" + timestamp + ".csv";
            java.io.File exportFile = new java.io.File(downloadsDir, fileName);
            
            FileWriter fw = new FileWriter(exportFile);
            // Write CSV header
            fw.append("ID,Type,Category,Amount,Date,Note\n");
            
            // Write transaction data
            for (Transaction transaction : transactions) {
                fw.append(String.valueOf(transaction.getId())).append(",");
                fw.append(transaction.getType()).append(",");
                fw.append(transaction.getCategory()).append(",");
                fw.append(String.format(Locale.UK, "€%.2f", transaction.getAmount())).append(",");
                fw.append(transaction.getDate()).append(",");
                
                // Handle notes that might contain commas by enclosing in quotes
                String note = transaction.getDescription();
                if (note != null && !note.isEmpty()) {
                    fw.append("\"").append(note.replace("\"", "\"\"")).append("\"");
                }
                fw.append("\n");
            }
            
            fw.flush();
            fw.close();
            
            // Notify the system about the new file so it shows up in file browsers
            MediaScannerConnection.scanFile(context, new String[]{exportFile.getAbsolutePath()}, null, null);
            
            return exportFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e("DatabaseHelper", "Error exporting data: " + e.getMessage());
            return null;
        }
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
                int idIndex = cursor.getColumnIndex(COLUMN_ID);
                int noteIndex = cursor.getColumnIndex(COLUMN_NOTE);
                int categoryIndex = cursor.getColumnIndex(COLUMN_CATEGORY);
                int amountIndex = cursor.getColumnIndex(COLUMN_AMOUNT);
                int dateIndex = cursor.getColumnIndex(COLUMN_DATE);
                
                // Only proceed if all required columns are found
                if (idIndex >= 0 && categoryIndex >= 0 && amountIndex >= 0 && dateIndex >= 0) {
                    String note = noteIndex >= 0 ? cursor.getString(noteIndex) : "";
                    Transaction transaction = new Transaction(
                        note,
                        cursor.getString(categoryIndex),
                        cursor.getDouble(amountIndex),
                        cursor.getString(dateIndex)
                    );
                    transaction.setId(cursor.getLong(idIndex));
                    transactions.add(transaction);
                }
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
                int idIndex = cursor.getColumnIndex(COLUMN_ID);
                int noteIndex = cursor.getColumnIndex(COLUMN_NOTE);
                int categoryIndex = cursor.getColumnIndex(COLUMN_CATEGORY);
                int amountIndex = cursor.getColumnIndex(COLUMN_AMOUNT);
                int dateIndex = cursor.getColumnIndex(COLUMN_DATE);
                
                // Only proceed if all required columns are found
                if (idIndex >= 0 && categoryIndex >= 0 && amountIndex >= 0 && dateIndex >= 0) {
                    String note = noteIndex >= 0 ? cursor.getString(noteIndex) : "";
                    Transaction transaction = new Transaction(
                        note,
                        cursor.getString(categoryIndex),
                        cursor.getDouble(amountIndex),
                        cursor.getString(dateIndex)
                    );
                    transaction.setId(cursor.getLong(idIndex));
                    transactions.add(transaction);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return transactions;
    }
}
