package com.daverickdunn.campusmapv2;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.daverickdunn.campusmapv2.sync.CampusMapSyncAdapter;

public class MainActivity extends ActionBarActivity implements TimetableFragment.Callback {

    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    boolean twoPane;
    private String mCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mCourse = Utility.getPreferredCourse(this);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.timetable_detail_container) != null) {

            twoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.timetable_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }

        } else {
            twoPane = false;
        }

        CampusMapSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        String course = Utility.getPreferredCourse(this);

        if (course != null && !course.equals(mCourse)) {
            TimetableFragment tf = (TimetableFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_timetable);
            if (null != tf) {
                tf.onCourseChanged();
            }
            DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if (null != df) {
                df.onCourseChanged(course);
            }
            mCourse = course;
        }
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (twoPane) {

            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.timetable_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }
}