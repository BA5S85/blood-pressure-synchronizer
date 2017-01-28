package com.example.bassa.bloodpressuresynchronizer;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "database.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.BPEntry.CREATE_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(DatabaseContract.BPEntry.DROP_TABLE);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public long insert(SQLiteDatabase db, int sys, int dia, int pulse, String date) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.BPEntry.COLUMN_NAME_SYS, sys);
        values.put(DatabaseContract.BPEntry.COLUMN_NAME_DIA, dia);
        values.put(DatabaseContract.BPEntry.COLUMN_NAME_PULSE, pulse);
        values.put(DatabaseContract.BPEntry.COLUMN_NAME_DATE, date);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                DatabaseContract.BPEntry.TABLE_NAME,
                null,
                values);

        return newRowId;
    }

    public void delete(SQLiteDatabase db, long id) {
        // Define 'where' part of query.
        String selection = DatabaseContract.BPEntry._ID + " = ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(id) };

        // Issue SQL statement.
        db.delete(DatabaseContract.BPEntry.TABLE_NAME, selection, selectionArgs);
    }

}
