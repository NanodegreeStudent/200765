package com.daverickdunn.campusmapv2;

import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.content.Loader;

import android.content.Context;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.daverickdunn.campusmapv2.data.TimetableContract;
import com.daverickdunn.campusmapv2.sync.CampusMapSyncAdapter;

public class TimetableFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private TimesAdapter adapter;

    private int mPosition = GridView.INVALID_POSITION;

    private static final String SELECTED_KEY = "selected_position";

    private static final int TIMETABLE_LOADER = 0;

    private static final String[] TIMETABLE_COLUMNS = {
            TimetableContract.TimetableEntry.TABLE_NAME + "." + TimetableContract.TimetableEntry._ID,
            TimetableContract.TimetableEntry.COLUMN_POSITION,
            TimetableContract.TimetableEntry.COLUMN_MODULE,
            TimetableContract.TimetableEntry.COLUMN_TITLE,
            TimetableContract.TimetableEntry.COLUMN_ROOM,
            TimetableContract.TimetableEntry.COLUMN_LECT,
            TimetableContract.TimetableEntry.COLUMN_LAB,
            TimetableContract.TimetableEntry.COLUMN_COLOUR,
            TimetableContract.TimetableEntry.COLUMN_DAY,
            TimetableContract.TimetableEntry.COLUMN_TIME
    };

    static final int COL_TIME_ID = 0;
    static final int COL_TIME_POS = 1;
    static final int COL_TIME_MOD = 2;
    static final int COL_TIME_TITLE = 3;
    static final int COL_TIME_ROOM = 4;
    static final int COL_TIME_LECT = 5;
    static final int COL_TIME_LAB = 6;
    static final int COL_TIME_COLOUR = 7;
    static final int COL_TIME_DAY = 8;
    static final int COL_TIME_TIME = 9;


    public interface Callback {

        public void onItemSelected(Uri dateUri);
    }

    public TimetableFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.timetablefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        adapter = new TimesAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridView);

        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);

                if (cursor != null && cursor.getInt(COL_TIME_DAY) != -1) {
                    String courseSetting = Utility.getPreferredCourse(getActivity());
                    ((Callback) getActivity())
                            .onItemSelected(TimetableContract.TimetableEntry.buildSingleClassInfo(
                                    courseSetting, cursor.getString(COL_TIME_POS)));
                }
                mPosition = position;

            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {

            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TIMETABLE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onCourseChanged( ) {
        updateTimetable();
        getLoaderManager().restartLoader(TIMETABLE_LOADER, null, this);
    }

    private void updateTimetable() {

        CampusMapSyncAdapter.syncImmediately(getActivity());

        /*
        Intent alarmIntent = new Intent(getActivity(), TimetableService.AlarmReceiver.class);
        alarmIntent.putExtra(TimetableService.COURSE_QUERY_EXTRA,
                Utility.getPreferredCourse(getActivity()));

        PendingIntent pi = PendingIntent.getBroadcast(
                getActivity(),
                0,
                alarmIntent,
                PendingIntent.FLAG_ONE_SHOT);

        AlarmManager am = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);

        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pi);
        /*
        Intent intent = new Intent(getActivity(), TimetableService.class);
        intent.putExtra(TimetableService.COURSE_QUERY_EXTRA,
                Utility.getPreferredCourse(getActivity()));
        getActivity().startService(intent);


        FetchTimetableTask timetableTask = new FetchTimetableTask(getActivity());
        String course = Utility.getPreferredCourse(getActivity());
        timetableTask.execute(course);#

        */
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        String sortOrder = TimetableContract.TimetableEntry.COLUMN_POSITION + " ASC";

        String course = Utility.getPreferredCourse(getActivity());
        Uri courseTimetableUri =
                TimetableContract.TimetableEntry.buildCourseTimetable(course);

        return new CursorLoader(getActivity(),
                courseTimetableUri,
                TIMETABLE_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }


    public class TimesAdapter extends CursorAdapter{

        public TimesAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.grid_item_timetable, parent, false);
            return view;
        }


        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            // Iterates over all elements at cursor.
            TextView tv = (TextView)view;
            tv.setBackgroundColor(Color.parseColor(cursor.getString(COL_TIME_COLOUR)));
            tv.setText(cursor.getString(COL_TIME_MOD));

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateTimetable();
    }



}
