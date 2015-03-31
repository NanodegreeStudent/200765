package com.daverickdunn.campusmapv2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.daverickdunn.campusmapv2.data.TimetableContract.*;

public class TimetableDbHelper extends SQLiteOpenHelper {


    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "campusmap.db";

    public TimetableDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + CourseEntry.TABLE_NAME + "(" +

                CourseEntry._ID + " INTEGER PRIMARY KEY," +
                CourseEntry.COLUMN_COURSE_SETTING + " TEXT UNIQUE NOT NULL " +
                " );";


        final String SQL_CREATE_TIMETABLE_TABLE = "CREATE TABLE " + TimetableEntry.TABLE_NAME + " (" +
                TimetableEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                TimetableEntry.COLUMN_COURSE_KEY + " INTEGER NOT NULL, " +
                TimetableEntry.COLUMN_POSITION + " INTEGER NOT NULL, " +
                TimetableEntry.COLUMN_MODULE + " TEXT NOT NULL, " +
                TimetableEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                TimetableEntry.COLUMN_ROOM + " TEXT NOT NULL," +

                TimetableEntry.COLUMN_LECT + " TEXT NOT NULL, " +
                TimetableEntry.COLUMN_LAB + " TEXT NOT NULL, " +

                TimetableEntry.COLUMN_DAY + " INTEGER NOT NULL, " +
                TimetableEntry.COLUMN_TIME + " INTEGER NOT NULL, " +
                TimetableEntry.COLUMN_COLOUR + " TEXT NOT NULL, " +

                " FOREIGN KEY (" + TimetableEntry.COLUMN_COURSE_KEY + ") REFERENCES " +
                CourseEntry.TABLE_NAME + " (" + CourseEntry._ID + "), " +

                " UNIQUE (" + TimetableEntry.COLUMN_POSITION + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TIMETABLE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CourseEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TimetableEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
