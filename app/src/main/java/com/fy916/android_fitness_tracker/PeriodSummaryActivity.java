/*
 Author: fy916
 This is the activity that shows the daily, weekly, and monthly summaries
 */

package com.fy916.android_fitness_tracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PeriodSummaryActivity extends AppCompatActivity {
    // database attributes
    private DBHelper mDbHelper;
    private SQLiteDatabase mDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // hide the title
        // reference https://developer.android.com/reference/androidx/appcompat/app/ActionBar#hide()
        getSupportActionBar().hide(); // hide the title bar

        // set view
        setContentView(R.layout.activity_summary);

        // get database
        mDbHelper = new DBHelper(this);
        mDb = mDbHelper.getWritableDatabase();

        try {
            // calc the summaries
            Cursor c = readSummaries("Date");
            calcMonthlyData(c);
            c = readSummaries("Date");
            calcWeeklyData(c);
            c = readSummaries("Date");
            calcDailyData(c);

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    public Cursor readSummaries(String orderBy) {
        // get a cursor of the data records
        // perform a sql query, reference is Lab007A
        String selection = "_id IN (SELECT MAX(_id) FROM LocationTable GROUP BY Session_ID)";
        Cursor c = mDb.query(
                "LocationTable",
                new String[]{"_id", "Session_ID", "Step_ID", "Latitude", "Longitude", "Distance", "Elevation", "Speed", "AVGSPEED", "Duration", "Date", "IsTracking", "Type", "Score", "Notes", "Weather"},
                selection,
                null,
                null,
                null,
                orderBy
        );
        return c;
    }


    public void calcMonthlyData(Cursor cursor) throws ParseException {
        // get monthly data
        double totalDistance = 0;
        long totalDuration = 0;

        // reference https://stackoverflow.com/questions/14241836/get-first-date-of-current-month-in-java
        // get the time of the first day of this month
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        cal.set(year, month, 1, 0, 0, 0);

        // get the time in mills for comparison
        long startOfCurrentMonthTime = cal.getTimeInMillis();


        // go through the data records
        while (cursor.moveToNext()) {
            // get date, distance, and duration
            @SuppressLint("Range") String dateString = cursor.getString(cursor.getColumnIndex("Date"));
            @SuppressLint("Range") double distance = cursor.getDouble(cursor.getColumnIndex("Distance"));
            @SuppressLint("Range") long duration = cursor.getLong(cursor.getColumnIndex("Duration"));

            // format the date to an interpretable date format
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
            Date currentDataDate = dateFormat.parse(dateString);

            // convert the date+time to time in mills
            long currentDataTime = currentDataDate.getTime();

            // If the date is larger than the start of this month, add the distance and duration
            if (currentDataTime >= startOfCurrentMonthTime) {
                totalDistance += distance;
                totalDuration += duration;
            }
        }

        // update ui
        TextView monthlyDistance = findViewById(R.id.ListDistanceMonthly);
        TextView monthlyDuration = findViewById(R.id.ListDurationMonthly);

        monthlyDistance.setText(Utils.formatDistance(totalDistance));
        monthlyDuration.setText(Utils.formatElapsedTime(totalDuration));
    }


    public void calcWeeklyData(Cursor cursor) throws ParseException {
        // get weekly data
        double totalDistance = 0;
        long totalDuration = 0;

        // reference https://www.javatpoint.com/post/java-calendar-getfirstdayofweek-method
        // get the time of the first day of this week
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        cal.set(year, month, 1, 0, 0, 0);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        // get the time in mills for comparison
        long startOfCurrentWeekTime = cal.getTimeInMillis();

        // go through the data records
        while (cursor.moveToNext()) {
            // get date, distance, and duration
            @SuppressLint("Range") String dateString = cursor.getString(cursor.getColumnIndex("Date"));
            @SuppressLint("Range") double distance = cursor.getDouble(cursor.getColumnIndex("Distance"));
            @SuppressLint("Range") long duration = cursor.getLong(cursor.getColumnIndex("Duration"));

            // format the date to an interpretable date format
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
            Date currentDataDate = dateFormat.parse(dateString);

            // convert the date+time to time in mills
            long currentDataTime = currentDataDate.getTime();

            // If the date is larger than the start of this week, add the distance and duration
            if (currentDataTime >= startOfCurrentWeekTime) {
                totalDistance += distance;
                totalDuration += duration;
            }
        }

        // update ui
        TextView weeklyDistance = findViewById(R.id.ListDistanceWeekly);
        TextView weeklyDuration = findViewById(R.id.ListDurationWeekly);

        weeklyDistance.setText(Utils.formatDistance(totalDistance));
        weeklyDuration.setText(Utils.formatElapsedTime(totalDuration));

    }


    public void calcDailyData(Cursor cursor) throws ParseException {
        // get daily data
        double totalDistance = 0;
        long totalDuration = 0;

        // get the time of 0:00 of today
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DATE);
        cal.set(year, month, day, 0, 0, 0);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        // get the time in mills for comparison
        long startOfCurrentWeekTime = cal.getTimeInMillis();

        // go through the data records
        while (cursor.moveToNext()) {
            // get date, distance, and duration
            @SuppressLint("Range") String dateString = cursor.getString(cursor.getColumnIndex("Date"));
            @SuppressLint("Range") double distance = cursor.getDouble(cursor.getColumnIndex("Distance"));
            @SuppressLint("Range") long duration = cursor.getLong(cursor.getColumnIndex("Duration"));

            // format the date to an interpretable date format
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
            Date currentDataDate = dateFormat.parse(dateString);

            // convert the date+time to time in mills
            long currentDataTime = currentDataDate.getTime();

            // If the date is larger than the start of today, add the distance and duration
            if (currentDataTime >= startOfCurrentWeekTime) {
                totalDistance += distance;
                totalDuration += duration;
            }
        }

        // update ui
        TextView dailyDistance = findViewById(R.id.ListDistanceDaily);
        TextView dailyDuration = findViewById(R.id.ListDurationDaily);

        dailyDistance.setText(Utils.formatDistance(totalDistance));
        dailyDuration.setText(Utils.formatElapsedTime(totalDuration));
    }


    // if the user wants to back to history page
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, WorkoutHistoryActivity.class);
        startActivity(intent);
    }

    // if the user wants to back to history page
    public void BacktoHistoryList(View view) {
        Intent intent = new Intent(this, WorkoutHistoryActivity.class);
        startActivity(intent);
    }
}

