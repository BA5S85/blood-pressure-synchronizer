package com.example.bassa.bloodpressuresynchronizer;

import android.provider.BaseColumns;

public final class DatabaseContract {

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
