package com.example.dell.warehouse.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.dell.warehouse.data.ProductContract.ProductEntry;
/**
 * Created by DELL on 09-02-2017.
 */

public class ProductDbHelper extends SQLiteOpenHelper {
    /*Name of the database file*/
    private static final String DATABASE_NAME = "product.db";

    /*Database version. If you change the database schema you must increment the database version.*/
    private static final int DATABASE_VERSION = 1;


    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create a string that contains the SQL statement to create the pets table.
        String SQL_CREATE_PRODUCT_TABLE = "CREATE TABLE " + ProductEntry.TABLE_NAME + " ("
                +ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                +ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                +ProductEntry.COLUMN_PRODUCT_PRICE + " TEXT, "
                +ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL, "
                +ProductEntry.COLUMN_PRODUCT_IMAGE + " TEXT);";

        //Execute the SQL statement
        db.execSQL(SQL_CREATE_PRODUCT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
