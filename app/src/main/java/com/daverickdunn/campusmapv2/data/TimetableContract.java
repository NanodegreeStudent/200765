package com.daverickdunn.campusmapv2.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class TimetableContract {

    public static final String CONTENT_AUTHORITY = "com.daverickdunn.campusmapv2";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_TIMETABLE = "timetable";
    public static final String PATH_COURSE = "course";

    public static final class CourseEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_COURSE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COURSE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COURSE;

        public static Uri buildCourseUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String TABLE_NAME = "course";
        public static final String COLUMN_COURSE_SETTING = "course_setting";

    }

    public static final class TimetableEntry implements BaseColumns {

        public static final String TABLE_NAME = "timetable";

        public static final String COLUMN_COURSE_KEY = "course_id";

        public static final String COLUMN_POSITION = "pos";

        public static final String COLUMN_MODULE = "module";

        public static final String COLUMN_TITLE= "title";

        public static final String COLUMN_ROOM = "room";

        public static final String COLUMN_LECT = "lect";

        public static final String COLUMN_LAB= "lab";

        public static final String COLUMN_DAY = "day";

        public static final String COLUMN_TIME = "time";

        public static final String COLUMN_COLOUR = "colour";


        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TIMETABLE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TIMETABLE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TIMETABLE;

        public static Uri buildTimetableUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildSingleClassInfo(String course, String pos){
            return CONTENT_URI.buildUpon().appendPath(course)
                    .appendPath(pos).build();
        }

        public static Uri buildCourseTimetable(String courseSetting)
        {
            return CONTENT_URI.buildUpon().appendPath(courseSetting).build();
        }

        public static String getCourseSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static int getPositionFromUri(Uri uri){
            return Integer.parseInt(uri.getPathSegments().get(2));
        }

    }
}