package com.example.bassa.bloodpressuresynchronizer;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import static com.example.bassa.bloodpressuresynchronizer.DatabaseContract.BPEntry.TABLE_NAME;

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
                TABLE_NAME,
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
        db.delete(TABLE_NAME, selection, selectionArgs);
    }

    public void deleteAll(SQLiteDatabase db) {
        db.delete(TABLE_NAME, null, null); // delete from bp_entry;
        String[] selectionArgs = { TABLE_NAME };
        db.delete("sqlite_sequence", "name=?", selectionArgs); // delete from sqlite_sequence where name=
    }

}

final class DatabaseContract {

    private static final String TEXT = " TEXT";
    private static final String INTEGER = " INTEGER";
    private static final String COMMA = ",";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public DatabaseContract() {}

    /* Inner class that defines the table contents */
    public static abstract class BPEntry implements BaseColumns {

        public static final String TABLE_NAME = "bp_entry";
        public static final String _ID = "_id";
        public static final String COLUMN_NAME_SYS = "sys";
        public static final String COLUMN_NAME_DIA = "dia";
        public static final String COLUMN_NAME_PULSE = "pulse";
        public static final String COLUMN_NAME_DATE = "date";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " ( " +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_NAME_SYS + INTEGER + COMMA +
                        COLUMN_NAME_DIA + INTEGER + COMMA +
                        COLUMN_NAME_PULSE + INTEGER + COMMA +
                        COLUMN_NAME_DATE + TEXT +
                        " )";

        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    }

}
