package com.daverickdunn.campusmapv2.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.daverickdunn.campusmapv2.R;
import com.daverickdunn.campusmapv2.Utility;
import com.daverickdunn.campusmapv2.data.TimetableContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class CampusMapSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = CampusMapSyncAdapter.class.getSimpleName();

    private String[] days = {"", "Mon", "Tue", "Wed", "Thur", "Fri"};

    private String[] times = {"09:00", "10:00", "11:00", "12:00", "13:00",
            "14:00", "15:00", "16:00","17:00","18:00", "19:00"};

    public static final int SYNC_INTERVAL = 60 * 60 * 12;   // Update twice a day
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/4;


    public CampusMapSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        String courseQuery = Utility.getPreferredCourse(getContext());
        String timetableJsonStr = null;

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {

            // Construct the URL for the query
            final String TIMETABLE_BASE_URL = "http://dev-api.daverickdunn.com/?";
            final String QUERY_PARAM = "message";

            Uri builtUri = Uri.parse(TIMETABLE_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, courseQuery)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create request and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Append for debugging - not necessary
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty
                return;
            }
            timetableJsonStr = buffer.toString();
            getTimetableFromJson(timetableJsonStr, courseQuery);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the data.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return;
    }

    long addCourse(String courseSetting) {
        long CourseId;

        Cursor courseCursor = getContext().getContentResolver().query(
                TimetableContract.CourseEntry.CONTENT_URI,
                new String[]{TimetableContract.CourseEntry._ID},
                TimetableContract.CourseEntry.COLUMN_COURSE_SETTING + " = ?",
                new String[]{courseSetting},
                null);

        if (courseCursor.moveToFirst()) {
            int courseIdIndex = courseCursor.getColumnIndex(TimetableContract.CourseEntry._ID);
            CourseId = courseCursor.getLong(courseIdIndex);
        } else {

            ContentValues courseValues = new ContentValues();
            courseValues.put(TimetableContract.CourseEntry.COLUMN_COURSE_SETTING, courseSetting);

            Uri insertedUri = getContext().getContentResolver().insert(
                    TimetableContract.CourseEntry.CONTENT_URI,
                    courseValues
            );
            CourseId = ContentUris.parseId(insertedUri);
        }
        courseCursor.close();
        return CourseId;
    }

    private void getTimetableFromJson(String timetableJsonStr, String course)

            throws JSONException {

        final String CM_TIMETABLE = "timetable";
        final String CM_POSITION = "pos";
        final String CM_MODULE = "mod";
        final String CM_TITLE = "title";
        final String CM_ROOM = "room";
        final String CM_LECT = "lect";
        final String CM_LAB = "lab";
        final String CM_DAY = "day";
        final String CM_TIME = "time";
        final String CM_COLOUR = "colour";

        try {
            JSONObject timetableJson = new JSONObject(timetableJsonStr);

            JSONArray timesArray = timetableJson.getJSONArray(CM_TIMETABLE);

            long courseId = addCourse(course);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(72);

            String pos, mod, title, room, lect, colour, lab;
            Integer day, time;

            // Because I used a GridView to implement my timetable, it made more sense to
            // add a tuple to the database for each TextView, this really simplified
            // using the CursorLoader to build the timetable.

            for(int i = 0; i < 72; i++) {
                // Add days
                if (i < 6){
                    pos = "" + i;
                    mod = days[i];
                    title = "";
                    room = "";
                    lect = "";
                    lab = "";
                    day = -1;
                    time = -1;
                    // Alternate colors
                    if(i % 2 == 0) {
                        colour = "#FFCC80";
                    }else{
                        colour = "#FFE0B2";
                    }
                    // Add times
                } else if (i % 6 == 0){
                    pos = "" + i;
                    mod = times[(i-6)/6];
                    title = "";
                    room = "";
                    lect = "";
                    lab = "";
                    day = -1;
                    time = -1;
                    // Alternate colors
                    if(i % 12 == 0) {
                        colour = "#90CAF9";
                    }else{
                        colour = "#BBDEFB";
                    }
                    // Else, free class! :)
                } else {
                    pos = "" + i;
                    mod = "";
                    title = "";
                    room = "";
                    lect = "";
                    lab = "";
                    day = -1;
                    time = -1;
                    colour = "#F5F5F5";
                }

                // To ensure the correct times go into the correct
                // positions I check all JSON elements returned
                int count = 0;

                while(count < timesArray.length()){

                    JSONObject singleTime = timesArray.getJSONObject(count);

                    if (singleTime.getInt(CM_POSITION) == i){
                        pos = singleTime.getString(CM_POSITION);
                        mod = singleTime.getString(CM_MODULE);
                        title = singleTime.getString(CM_TITLE);
                        room = singleTime.getString(CM_ROOM);
                        lect = singleTime.getString(CM_LECT);

                        lab = singleTime.getString(CM_LAB);
                        day = singleTime.getInt(CM_DAY);
                        time = singleTime.getInt(CM_TIME);
                        colour = singleTime.getString(CM_COLOUR);
                        break;
                    }
                    count++;
                }

                ContentValues timetableValues = new ContentValues();

                timetableValues.put(TimetableContract.TimetableEntry.COLUMN_COURSE_KEY, courseId);
                timetableValues.put(TimetableContract.TimetableEntry.COLUMN_POSITION, pos);
                timetableValues.put(TimetableContract.TimetableEntry.COLUMN_MODULE, mod);
                timetableValues.put(TimetableContract.TimetableEntry.COLUMN_TITLE, title);
                timetableValues.put(TimetableContract.TimetableEntry.COLUMN_ROOM, room);
                timetableValues.put(TimetableContract.TimetableEntry.COLUMN_LECT, lect);
                timetableValues.put(TimetableContract.TimetableEntry.COLUMN_LAB, lab);
                timetableValues.put(TimetableContract.TimetableEntry.COLUMN_DAY, day);
                timetableValues.put(TimetableContract.TimetableEntry.COLUMN_TIME, time);
                timetableValues.put(TimetableContract.TimetableEntry.COLUMN_COLOUR, colour);

                cVVector.add(timetableValues);
            }

            // add to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(TimetableContract.TimetableEntry.CONTENT_URI, cvArray);
            }

            String sortOrder = TimetableContract.TimetableEntry.COLUMN_POSITION + " ASC";

            Uri timetableUri = TimetableContract.TimetableEntry.buildCourseTimetable(
                    course);

            Cursor cur = getContext().getContentResolver().query(timetableUri,
                    null, null, null, sortOrder);

            cVVector = new Vector<ContentValues>(cur.getCount());
            if ( cur.moveToFirst() ) {
                do {
                    ContentValues cv = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(cur, cv);
                    cVVector.add(cv);
                } while (cur.moveToNext());
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    // Syncs adapter immediately
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    // Creates dummy account to be ued with SyncAdapter
    public static Account getSyncAccount(Context context) {

        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {

        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {

        CampusMapSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        // Start the first sync
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}