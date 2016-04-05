package com.joeyturczak.drivingreference.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Copyright (C) 2016 Joey Turczak
 */
public class DrivingDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "drivingreference.db";

    public DrivingDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_MANUAL_TABLE = "CREATE TABLE " + DrivingContract.DrivingManualEntry.TABLE_NAME + " (" +
                DrivingContract.DrivingManualEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                DrivingContract.DrivingManualEntry.COLUMN_BACKEND_ID + " INTEGER NOT NULL UNIQUE ON CONFLICT REPLACE, " +
                DrivingContract.DrivingManualEntry.COLUMN_LOCATION + " TEXT NOT NULL, " +
                DrivingContract.DrivingManualEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
                DrivingContract.DrivingManualEntry.COLUMN_LANGUAGE + " TEXT NOT NULL, " +
                DrivingContract.DrivingManualEntry.COLUMN_URL + " TEXT NOT NULL, " +
                DrivingContract.DrivingManualEntry.COLUMN_DISPLAY_NAME + " TEXT NOT NULL, " +
                DrivingContract.DrivingManualEntry.COLUMN_LAST_UPDATED + " INTEGER NOT NULL, " +
                DrivingContract.DrivingManualEntry.COLUMN_DOWNLOADED + " INTEGER NOT NULL, " +
                DrivingContract.DrivingManualEntry.COLUMN_LAST_PAGE + " INTEGER NOT NULL" +
                " );";

        db.execSQL(SQL_CREATE_MANUAL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + DrivingContract.DrivingManualEntry.TABLE_NAME);
        onCreate(db);
    }
}
