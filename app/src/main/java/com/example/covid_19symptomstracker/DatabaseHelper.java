package com.example.covid_19symptomstracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper sInstance;

    private static final String TAG = "DatabaseHelper";

    private static final String TABLE_NAME = "covidDB";

    public static synchronized DatabaseHelper getInstance(Context context){
        if(sInstance == null){
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }

        return sInstance;
    }

    public DatabaseHelper(@Nullable Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (Name STRING DEFAULT 'Fatma Mokhtar', HRrate REAL DEFAULT 0.0, RRrate REAL DEFAULT 0.0, " +
                "nausea REAL DEFAULT 0.0, headache REAL DEFAULT 0.0, diarrhea REAL DEFAULT 0.0, soarThroat REAL DEFAULT 0.0, " +
                " fever REAL DEFAULT 0.0, muscleAche REAL DEFAULT 0.0, LST REAL DEFAULT 0.0, cough REAL DEFAULT 0.0, shortnessBreath REAL DEFAULT 0.0, feelingTired REAL DEFAULT 0.0)";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        String dropTable = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(dropTable);
        onCreate(db);
    }

    //Method used for debugging
    public void clearDatabase(){
        String query = "DELETE FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
    }

    //Method used for debugging
    public void deleteTable() {
        String query = "DROP TABLE " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
    }

    public boolean updateData(String column, float data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(column, data);

        Log.i(TAG, "Update: " + data + " in " + column + " to " + TABLE_NAME);

        int result = db.update(TABLE_NAME, contentValues, null, null);

        if(result == 0){
            return false;
        } else {
            return true;
        }
    }

    public boolean insertData(String column, float data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(column, data);

        Log.i(TAG, "insert: " + data + " in " + column + " to " + TABLE_NAME);

        long result = db.insert(TABLE_NAME, null, contentValues);

        if(result == -1){
            return false;
        } else {
            return true;
        }
    }

    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }
}
