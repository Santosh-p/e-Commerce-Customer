package com.myapp.groceryshopcustomer.DBHandler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SSPL on 01-08-2017.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "GroceryCustomer.db";

    // Customer Table
    private static final String TABLE_CUSTOMER = "registration";
    private static final String KEY_PRIMARY_USER_ID = "primary_user_id";
    private static final String DEVICE_ID = "device_id";
    private static final String KEY_CUSTOMER_ID = "user_id";
    private static final String KEY_CUSTOMER_NAME = "customer_name";
    private static final String KEY_EMAIL_ID = "email_id";
    private static final String KEY_LOGIN_STATUS = "login_status";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_CUSTOMER + "("
                + KEY_PRIMARY_USER_ID + " INTEGER PRIMARY KEY,"
                + DEVICE_ID + " TEXT,"
                + KEY_CUSTOMER_ID + " INTEGER,"
                + KEY_CUSTOMER_NAME + " TEXT,"
                + KEY_EMAIL_ID + " TEXT,"
                + KEY_LOGIN_STATUS + " TEXT" + ")";

        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMER);
        onCreate(db);
    }

    // ############################# User table ###########################
    // code to add User
    public void addCustomer(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DEVICE_ID, customer.getDeviceId());
        values.put(KEY_CUSTOMER_ID, customer.getCust_id());
        values.put(KEY_CUSTOMER_NAME, customer.getCust_name());
        values.put(KEY_EMAIL_ID, customer.getEmaiid());
        values.put(KEY_LOGIN_STATUS, customer.getEmaiid());

        // Inserting Row
        db.insert(TABLE_CUSTOMER, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    // code to add User
    public int updateCustomerdetails(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DEVICE_ID, customer.getDeviceId());
        values.put(KEY_CUSTOMER_ID, customer.getCust_id());
        values.put(KEY_LOGIN_STATUS, customer.getLogin_status());
        // Inserting Row
      int result =  db.update(TABLE_CUSTOMER, values, KEY_PRIMARY_USER_ID + "=?", new String[]{String.valueOf(1)});
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
   return result;
    }

    // code to add User
    public void updateCustomer(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CUSTOMER_NAME, customer.getCust_name());
        values.put(KEY_EMAIL_ID, customer.getEmaiid());

        // Inserting Row
        db.update(TABLE_CUSTOMER, values, KEY_CUSTOMER_ID + "=?", new String[]{String.valueOf(customer.cust_id)});
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    // code to add User
    public void updateLoginStatus(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_LOGIN_STATUS, customer.getLogin_status());

        // Inserting Row
        db.update(TABLE_CUSTOMER, values, KEY_CUSTOMER_ID + "=?", new String[]{String.valueOf(customer.cust_id)});
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    public Customer getCustomerDetails() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_CUSTOMER;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {

        }
        Customer customer = new Customer(
                cursor.getString(1),
                cursor.getInt(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5));
        return customer;
    }

    // code to get all Users in a list view
    public List<Customer> getAllCustomers() {
        List<Customer> customerList = new ArrayList<Customer>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CUSTOMER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Customer customer = new Customer();
                customer.setDeviceId(cursor.getString(0));
                customer.setCust_id(cursor.getInt(1));
                customer.setCust_name(cursor.getString(2));
                customer.setEmaiid(cursor.getString(3));
                customer.setLogin_status(cursor.getString(4));
                customerList.add(customer);
            } while (cursor.moveToNext());
        }

        // return contact list
        return customerList;
    }

}
