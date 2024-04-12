/*
 Author: fy916
 This is the activity that shows the history record list
 */

package com.fy916.android_fitness_tracker;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class WorkoutHistoryActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    // database attributes and list tools
    private DBHelper mDbHelper;
    private SQLiteDatabase mDb;
    private ListView workoutListView;
    SimpleCursorAdapter mDataAdapter;


    // orderby string for query usage (used when sorting)
    private String orderBy = "Session_ID";
    private String orderSequence = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // hide the title
        //https://developer.android.com/reference/androidx/appcompat/app/ActionBar#hide()
        getSupportActionBar().hide(); // hide the title bar

        // set view
        setContentView(R.layout.activity_list);
        workoutListView = findViewById(R.id.workoutListView);

        // get database
        mDbHelper = new DBHelper(this);
        mDb = mDbHelper.getWritableDatabase();

        // reference https://developer.android.com/reference/android/widget/ArrayAdapter
        // https://developer.android.com/develop/ui/views/components/spinner
        // create an ArrayAdapter using the string array and a default spinner layout
        // used for selecting the field for sorting
        Spinner spinner = findViewById(R.id.categorySpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sortCategories, android.R.layout.simple_spinner_item);
        // specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    public void backToMain(View view) {
        // when back button is clicked (in the view), back to main page
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    public void ascending(View v) {
        // when user wants to sort based on ascending order, the sql query is default by ascending order
        // so there is no need to add additional info after the field of sorting
        orderSequence = "";
        // update the list based on query
        renderList();
    }

    public void descending(View v) {
        // when the user wants to sort based on descending order, add "DESC" to the query sortby
        orderSequence = " DESC";
        // update the list based on query
        renderList();
    }


    public void renderList() {
        // update the list
        // get a cursor of the data
        Cursor cursor = readSummaries(orderBy + orderSequence);

        // reference: Lab008
        // get strings of field in database
        String[] columns = new String[]{
                "Distance",
                "AVGSPEED",
                "Duration",
                "Date",
                "Type",
                "Score"
        };


        // get mapping element in layout
        int[] uiMapping = new int[]{
                R.id.ListDistance,
                R.id.ListAvgSpeed,
                R.id.ListDuration,
                R.id.ListDate,
                R.id.ListType,
                R.id.ListScore,
        };


        // map data
        mDataAdapter = new SimpleCursorAdapter(
                this, R.layout.db_item_view,
                cursor,
                columns,
                uiMapping,
                0);


        // we need to format the info before showing
        // reference https://developer.android.com/reference/android/widget/SimpleCursorAdapter.ViewBinder
        // https://developer.android.com/reference/android/widget/SimpleCursorAdapter.ViewBinder#setViewValue(android.view.View,%20android.database.Cursor,%20int)
        mDataAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                // format some fields
                TextView textView = (TextView) view; // reference of the text we store the values
                if (columnIndex == cursor.getColumnIndex("Distance")) {
                    // if the value is for distance column, format the distance before showing it
                    double distance = cursor.getDouble(columnIndex);
                    String formatedDistance = Utils.formatDistance(distance);
                    textView.setText(formatedDistance);
                    return true;
                }

                if (columnIndex == cursor.getColumnIndex("AVGSPEED")) {
                    // if the value is for average speed column, format the speed before showing it
                    double avgSpeed = cursor.getDouble(columnIndex);
                    String avgSpeedTst = Utils.formatSpeed(avgSpeed);
                    textView.setText(avgSpeedTst);
                    return true;
                }


                if (columnIndex == cursor.getColumnIndex("Duration")) {
                    // if the value is for duration column, format the duration before showing it
                    long duration = cursor.getLong(columnIndex);
                    String formatedDuration = Utils.formatElapsedTime(duration);
                    textView.setText(formatedDuration);
                    return true;
                }

                if (columnIndex == cursor.getColumnIndex("Type")) {
                    // if the value is for type column, convert the index to strings it represents
                    String type = cursor.getString(columnIndex);

                    if (type == null) { // if no value, default is Other
                        type = "Other";
                    }

                    // convert index to type labels
                    if (type.equals("1")) {
                        type = "Running";
                    } else if (type.equals("2")) {
                        type = "Hiking";
                    } else if (type.equals("3")) {
                        type = "Walking";
                    } else if (type.equals("4")) {
                        type = "Cycling";
                    } else if (type.equals("5")) {
                        type = "Swimming";
                    } else if (type.equals("6")) {
                        type = "Climbing";
                    } else {
                        type = "Others";
                    }
                    textView.setText(type);
                    return true;
                }

                if (columnIndex == cursor.getColumnIndex("Score")) {
                    // if the value is for score column, convert the index to strings it represents
                    String score = cursor.getString(columnIndex);

                    if (score == null) { // if no value, default is Other
                        score = "Other";
                    }

                    // convert index to score labels
                    if (score.equals("1")) {
                        score = "1 - Worst :(";
                    } else if (score.equals("2")) {
                        score = "2 - Bad :(";
                    } else if (score.equals("3")) {
                        score = "3 - Normal :|";
                    } else if (score.equals("4")) {
                        score = "4 - Good :)";
                    } else if (score.equals("5")) {
                        score = "5 - Best :)";
                    } else {
                        score = "Other";
                    }

                    textView.setText(score);
                    return true;
                }

                // for rest of the field which does not need format, return false directly
                return false;
            }
        });


        workoutListView.setAdapter(mDataAdapter);
        // reference https://developer.android.com/reference/android/widget/AdapterView.OnItemClickListener
        workoutListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // when the item is clicked, get the session ID of the item through the position
                // provided for us to find from the database
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                int columnIndex = cursor.getColumnIndex("Session_ID");

                // sessionIDVal is the session id value
                String sessionIDVal = "";
                if (columnIndex != -1) {
                    sessionIDVal = cursor.getString(columnIndex);
                }

                // when the user clicks the item, show the detail of this record
                Intent intent = new Intent(WorkoutHistoryActivity.this, DetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong("SessionID", Long.parseLong(sessionIDVal));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        // render list when activity starts
        renderList();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
        // this function is used when the user selects the field they want to sort from the spinner
        String item = (String) parent.getItemAtPosition(i);
        // if the user selects Date, we directly sort them based on the ID since the newer data has larger ID
        if (item.equals("Date")) {
            item = "Session_ID";
        }
        // set the orderBy field to the user selected field
        orderBy = item;

        // render the list
        renderList();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onBackPressed() {
        // when system back button is pressed, back to main page
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    public void onSummaryClicked(View view) {
        // when the summary page is clicked, go to the summary page
        Intent intent = new Intent(this, PeriodSummaryActivity.class);
        startActivity(intent);
    }


    public Cursor readSummaries(String orderBy) {
        // read the record data from the database
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
}



