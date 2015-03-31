package com.daverickdunn.campusmapv2;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utility {

    /*
    private String[] days = {"", "Mon", "Tue", "Wed", "Thur", "Fri"};

    private String[] times = {"09:00", "10:00", "11:00", "12:00", "13:00",
            "14:00", "15:00", "16:00","17:00","18:00", "19:00"};

    */

    public static String getPreferredCourse(Context context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(
                context.getString(R.string.pref_course_key),
                context.getString(R.string.pref_course_default)
        );

    }

    public static String getDayString(String dayStr){

        int day = Integer.parseInt(dayStr);

        switch (day){
            case 1:
                return "Monday";
            case 2:
                return "Tuesday";
            case 3:
                return "Wednesday";
            case 4:
                return "Thursday";
            case 5:
                return "Friday";
            default:
                return "Unknown Day";
        }
    }




}
