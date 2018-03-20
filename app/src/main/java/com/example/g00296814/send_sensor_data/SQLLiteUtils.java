package com.example.g00296814.send_sensor_data;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.util.Log;

import java.util.HashMap;

public class SQLLiteUtils {
    private final String TAG = SQLLiteUtils.class.getSimpleName();

    private SQLiteDatabase database;

    public void openDatabase() {
        try {
            final String DATABASE_PATH = MainActivity.FILES_DIRECTORY + "/" + MainActivity.PACKAGE_NAME + ".databases";
            //final String DATABASE_NAME = DATABASE_PATH + "/" + "database.db";
            //String myPath = MainActivity.FILES_DIRECTORY.replace("files", "databases")+ File.separator + "database.db";
            String myPath = "/sdcard/database.db";
           //String myPath = MainActivity.EXTERNAL_STORAGE_DIRECTORY + "/database.db";

            Log.i(TAG, "DATABASE_PATH: " + DATABASE_PATH);
            Log.i(TAG, "DATABASE_NAME: " + myPath);

            database = SQLiteDatabase.openDatabase(myPath, null,
                    SQLiteDatabase.CREATE_IF_NECESSARY);
        } catch (SQLiteException e) {
            Log.e(TAG, "Exception: "+ e);
            Log.i(TAG, Log.getStackTraceString(e));
        }
    }

    public void insertData(String IPAddress, String port) {
        database.beginTransaction();
        try {
            database.execSQL("create table tblData ("
                    + " recID integer PRIMARY KEY autoincrement, "
                    + " IPAddress  text, port text );");

            database.setTransactionSuccessful();

        } catch (SQLException e) {
            Log.e(TAG, "Exception: "+ e);
            Log.i(TAG, Log.getStackTraceString(e));
        } finally {
            database.endTransaction();
        }

        database.beginTransaction();
        try {
            database.execSQL("insert into tblData(IPAddress, port) "
                    + " values ('" + IPAddress + "', '" + port + "' );");

            database.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e(TAG, "Exception: "+ e);
            Log.i(TAG, Log.getStackTraceString(e));
        } finally {
            database.endTransaction();
        }
    }

    public HashMap<String, String> getData() {
        HashMap<String, String> values = new HashMap<>();
        Cursor cursor = null;

        try {
            String query = "select IPAddress, port"
                    + " from tblData";
            cursor = database.rawQuery(query, null);

            cursor.moveToLast();
            int index = cursor.getColumnIndex(MainActivity.IPADDRESS);
            String IPAddress = cursor.getString(index);
            Log.i(TAG, "IPAddress from DB: " + IPAddress);

            index = cursor.getColumnIndex(MainActivity.PORT);
            String port = cursor.getString(index);
            Log.i(TAG, "Port from DB: " + IPAddress);

            values.put(MainActivity.IPADDRESS, IPAddress);
            values.put(MainActivity.PORT, port);
        } catch (SQLiteException e) {
            Log.e(TAG, "Exception: " + e);
            Log.i(TAG, Log.getStackTraceString(e));
        } finally {
            if(cursor != null)
                cursor.close();
        }

        return values;
    }
}
