package com.example.personalfinancialmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteConstraintException;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "finance.db";
    private static final int DATABASE_VERSION = 4; // Increased for user-specific transactions

    // User table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USER_USERNAME = "username";
    private static final String COLUMN_USER_PASSWORD = "password";
    private static final String COLUMN_USER_EMAIL = "email";

    // Transaction table constants
    private static final String TABLE_TRANSACTIONS = "transactions";
    private static final String COLUMN_TRANS_ID = "id";
    private static final String COLUMN_TRANS_AMOUNT = "amount";
    private static final String COLUMN_TRANS_DESC = "description";
    private static final String COLUMN_TRANS_CATEGORY = "category";
    private static final String COLUMN_TRANS_TYPE = "type";
    private static final String COLUMN_TRANS_USER_ID = "user_id";  // New column for user ID

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + "(" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_USERNAME + " TEXT UNIQUE, " +
                COLUMN_USER_PASSWORD + " TEXT, " +
                COLUMN_USER_EMAIL + " TEXT)";
        
        // Create transactions table with user_id column
        String createTransactionsTable = "CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                COLUMN_TRANS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TRANS_AMOUNT + " REAL, " +
                COLUMN_TRANS_DESC + " TEXT, " +
                COLUMN_TRANS_CATEGORY + " TEXT, " +
                COLUMN_TRANS_TYPE + " TEXT, " +
                COLUMN_TRANS_USER_ID + " INTEGER, " +
                "FOREIGN KEY (" + COLUMN_TRANS_USER_ID + ") REFERENCES " + 
                TABLE_USERS + "(" + COLUMN_USER_ID + "))";
        
        db.execSQL(createUsersTable);
        db.execSQL(createTransactionsTable);
        
        // Add default admin user
        ContentValues adminValues = new ContentValues();
        adminValues.put(COLUMN_USER_USERNAME, "admin");
        adminValues.put(COLUMN_USER_PASSWORD, "admin123");
        adminValues.put(COLUMN_USER_EMAIL, "admin@example.com");
        db.insert(TABLE_USERS, null, adminValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            // Create users table if upgrading from older version
            String createUsersTable = "CREATE TABLE " + TABLE_USERS + "(" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_USERNAME + " TEXT UNIQUE, " +
                    COLUMN_USER_PASSWORD + " TEXT, " +
                    COLUMN_USER_EMAIL + " TEXT)";
            db.execSQL(createUsersTable);
            
            // Add default admin user
            ContentValues adminValues = new ContentValues();
            adminValues.put(COLUMN_USER_USERNAME, "admin");
            adminValues.put(COLUMN_USER_PASSWORD, "admin123");
            adminValues.put(COLUMN_USER_EMAIL, "admin@example.com");
            db.insert(TABLE_USERS, null, adminValues);
        }
        
        if (oldVersion < 4) {
            // Add user_id column to transactions table
            try {
                // Create new transactions table with user_id
                db.execSQL("CREATE TABLE temp_transactions (" +
                        COLUMN_TRANS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_TRANS_AMOUNT + " REAL, " +
                        COLUMN_TRANS_DESC + " TEXT, " +
                        COLUMN_TRANS_CATEGORY + " TEXT, " +
                        COLUMN_TRANS_TYPE + " TEXT, " +
                        COLUMN_TRANS_USER_ID + " INTEGER, " +
                        "FOREIGN KEY (" + COLUMN_TRANS_USER_ID + ") REFERENCES " + 
                        TABLE_USERS + "(" + COLUMN_USER_ID + "))");
                
                // Get admin user ID
                Cursor cursor = db.rawQuery("SELECT id FROM users WHERE username = 'admin'", null);
                int adminId = 1; // Default
                if (cursor.moveToFirst()) {
                    adminId = cursor.getInt(0);
                }
                cursor.close();
                
                // Copy data from old table to new table, assigning all transactions to admin
                db.execSQL("INSERT INTO temp_transactions (id, amount, description, category, type, user_id) " +
                        "SELECT id, amount, description, category, type, " + adminId + 
                        " FROM transactions");
                
                // Drop old table
                db.execSQL("DROP TABLE transactions");
                
                // Rename new table to transactions
                db.execSQL("ALTER TABLE temp_transactions RENAME TO transactions");
            } catch (Exception e) {
                // If migration fails, recreate the tables
                db.execSQL("DROP TABLE IF EXISTS transactions");
                db.execSQL("DROP TABLE IF EXISTS users");
                onCreate(db);
            }
        }
    }

    // User authentication methods
    public boolean addUser(String username, String password, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_USERNAME, username);
        values.put(COLUMN_USER_PASSWORD, password);
        values.put(COLUMN_USER_EMAIL, email);
        
        try {
            long result = db.insert(TABLE_USERS, null, values);
            return result != -1;
        } catch (SQLiteConstraintException e) {
            return false;
        } finally {
            db.close();
        }
    }
    
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USER_USERNAME + " = ? AND " + COLUMN_USER_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};
        
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        
        return count > 0;
    }
    
    public boolean checkUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USER_USERNAME + " = ?";
        String[] selectionArgs = {username};
        
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        
        return count > 0;
    }
    
    // Get user ID by username
    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, 
                new String[]{COLUMN_USER_ID}, 
                COLUMN_USER_USERNAME + "=?", 
                new String[]{username}, 
                null, null, null);
        
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return userId;
    }

    // Transaction methods - updated to be user-specific
    public void addTransaction(Transaction transaction, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TRANS_AMOUNT, transaction.getAmount());
        values.put(COLUMN_TRANS_DESC, transaction.getDescription());
        values.put(COLUMN_TRANS_CATEGORY, transaction.getCategory());
        values.put(COLUMN_TRANS_TYPE, transaction.getType());
        values.put(COLUMN_TRANS_USER_ID, userId);
        db.insert(TABLE_TRANSACTIONS, null, values);
        db.close();
    }

    public List<Transaction> getAllTransactions(int userId) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_TRANSACTIONS,
                new String[]{COLUMN_TRANS_AMOUNT, COLUMN_TRANS_DESC, COLUMN_TRANS_CATEGORY, COLUMN_TRANS_TYPE},
                COLUMN_TRANS_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);
                
        if (cursor.moveToFirst()) {
            do {
                double amount = cursor.getDouble(0);
                String desc = cursor.getString(1);
                String category = cursor.getString(2);
                String type = cursor.getString(3);
                transactions.add(new Transaction(amount, desc, category, type));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return transactions;
    }

    public void deleteTransaction(Transaction transaction, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANSACTIONS,
                COLUMN_TRANS_AMOUNT + "=? AND " + 
                COLUMN_TRANS_DESC + "=? AND " + 
                COLUMN_TRANS_CATEGORY + "=? AND " + 
                COLUMN_TRANS_TYPE + "=? AND " +
                COLUMN_TRANS_USER_ID + "=?",
                new String[]{
                        String.valueOf(transaction.getAmount()),
                        transaction.getDescription(),
                        transaction.getCategory(),
                        transaction.getType(),
                        String.valueOf(userId)
                });
        db.close();
    }

    public List<Transaction> getFilteredTransactions(String category, String type, int userId) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_TRANS_AMOUNT + ", " + 
                                  COLUMN_TRANS_DESC + ", " + 
                                  COLUMN_TRANS_CATEGORY + ", " + 
                                  COLUMN_TRANS_TYPE + 
                       " FROM " + TABLE_TRANSACTIONS + 
                       " WHERE " + COLUMN_TRANS_USER_ID + "=?";
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(userId));

        if (!category.equals("All")) {
            query += " AND " + COLUMN_TRANS_CATEGORY + "=?";
            args.add(category);
        }
        if (!type.equals("All")) {
            query += " AND " + COLUMN_TRANS_TYPE + "=?";
            args.add(type);
        }

        Cursor cursor = db.rawQuery(query, args.toArray(new String[0]));
        if (cursor.moveToFirst()) {
            do {
                double amount = cursor.getDouble(0);
                String desc = cursor.getString(1);
                String cat = cursor.getString(2);
                String typ = cursor.getString(3);
                transactions.add(new Transaction(amount, desc, cat, typ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return transactions;
    }
}
