
package com.daverickdunn.campusmapv2.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class TimetableProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private TimetableDbHelper mTimetableHelper;

    static final int TIMETABLE = 100;
    static final int TIMETABLE_WITH_COURSE = 101;
    static final int TIMETABLE_WITH_COURSE_AND_CLASS = 102;
    static final int COURSE = 300;

    private static final SQLiteQueryBuilder TimetableByCourseSettingQueryBuilder;

    static{
        TimetableByCourseSettingQueryBuilder = new SQLiteQueryBuilder();

        TimetableByCourseSettingQueryBuilder.setTables(
                TimetableContract.TimetableEntry.TABLE_NAME + " INNER JOIN " +
                        TimetableContract.CourseEntry.TABLE_NAME +
                        " ON " + TimetableContract.TimetableEntry.TABLE_NAME +
                        "." + TimetableContract.TimetableEntry.COLUMN_COURSE_KEY +
                        " = " + TimetableContract.CourseEntry.TABLE_NAME +
                        "." + TimetableContract.CourseEntry._ID);
    }

    private static final String courseSettingSelection =
            TimetableContract.CourseEntry.TABLE_NAME+
                    "." + TimetableContract.CourseEntry.COLUMN_COURSE_SETTING + " = ? ";

    private static final String sLocationSettingAndDaySelection =
            TimetableContract.CourseEntry.TABLE_NAME +
                    "." + TimetableContract.CourseEntry.COLUMN_COURSE_SETTING + " = ? AND " +
                    TimetableContract.TimetableEntry.COLUMN_POSITION + " = ? ";


    private Cursor getTimetablebyCourseAndClass(Uri uri, String[] projection, String sortOrder) {

        String courseSetting = TimetableContract.TimetableEntry.getCourseSettingFromUri(uri);

        int pos = TimetableContract.TimetableEntry.getPositionFromUri(uri);

        return TimetableByCourseSettingQueryBuilder.query(mTimetableHelper.getReadableDatabase(),
                projection,
                sLocationSettingAndDaySelection,
                new String[]{courseSetting, Integer.toString(pos)},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getTimetableByCourse(Uri uri, String[] projection, String sortOrder) {

        String courseSetting = TimetableContract.TimetableEntry.getCourseSettingFromUri(uri);

        String[] selectionArgs;
        String selection;

        selection = courseSettingSelection;
        selectionArgs = new String[]{courseSetting};

        return TimetableByCourseSettingQueryBuilder.query(mTimetableHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TimetableContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, TimetableContract.PATH_TIMETABLE, TIMETABLE);
        matcher.addURI(authority, TimetableContract.PATH_TIMETABLE + "/*", TIMETABLE_WITH_COURSE);
        matcher.addURI(authority, TimetableContract.PATH_TIMETABLE + "/*/#", TIMETABLE_WITH_COURSE_AND_CLASS);
        matcher.addURI(authority, TimetableContract.PATH_COURSE, COURSE);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mTimetableHelper = new TimetableDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {

            case TIMETABLE_WITH_COURSE_AND_CLASS:
                return TimetableContract.TimetableEntry.CONTENT_ITEM_TYPE;
            case TIMETABLE_WITH_COURSE:
                return TimetableContract.TimetableEntry.CONTENT_TYPE;
            case TIMETABLE:
                return TimetableContract.TimetableEntry.CONTENT_TYPE;
            case COURSE:
                return TimetableContract.CourseEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {

            case TIMETABLE_WITH_COURSE_AND_CLASS:
            {
                retCursor = getTimetablebyCourseAndClass(uri, projection, sortOrder);
                break;
            }

            case TIMETABLE_WITH_COURSE: {
                retCursor = getTimetableByCourse(uri, projection, sortOrder);
                break;
            }

            case TIMETABLE: {
                retCursor = mTimetableHelper.getReadableDatabase().query(
                        TimetableContract.TimetableEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case COURSE: {
                retCursor = mTimetableHelper.getReadableDatabase().query(
                        TimetableContract.CourseEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mTimetableHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case TIMETABLE: {

                long _id = db.insert(TimetableContract.TimetableEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = TimetableContract.TimetableEntry.buildTimetableUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;

            }
            case COURSE: {

                long _id = db.insert(TimetableContract.CourseEntry.TABLE_NAME, null, values);

                if ( _id > 0 )
                    returnUri = TimetableContract.CourseEntry.buildCourseUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:

                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mTimetableHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if ( null == selection ) selection = "1";
        switch (match) {
            case TIMETABLE:
                rowsDeleted = db.delete(
                        TimetableContract.TimetableEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case COURSE:
                rowsDeleted = db.delete(
                        TimetableContract.CourseEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }


    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mTimetableHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case TIMETABLE:
                rowsUpdated = db.update(TimetableContract.TimetableEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case COURSE:
                rowsUpdated = db.update(TimetableContract.CourseEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mTimetableHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TIMETABLE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(TimetableContract.TimetableEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}