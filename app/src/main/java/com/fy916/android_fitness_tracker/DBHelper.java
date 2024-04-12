// Reference: This provider is modified based on Lab008A and Lab007A


package com.fy916.android_fitness_tracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// this helper offers us the database and the helper object
public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = DBHelper.class.getSimpleName();

    private static final String KEY_Primary = "_id";
    private static final String KEY_SessionID = "Session_ID";
    private static final String KEY_StepID = "Step_ID";
    private static final String KEY_Lat = "Latitude";
    private static final String KEY_Long = "Longitude";
    private static final String KEY_Distance = "Distance";
    private static final String KEY_Elevation = "Elevation";
    private static final String KEY_Speed = "Speed";

    private static final String KEY_Avg_Speed = "AVGSPEED";
    private static final String KEY_Duration = "Duration";
    private static final String KEY_Date = "Date";
    private static final String KEY_IsTracking = "IsTracking";
    private static final String KEY_Type = "Type";
    private static final String KEY_Score = "Score";
    private static final String KEY_Notes = "Notes";
    private static final String KEY_Weather = "Weather";

    private static final Integer SQLITE_TABLE_VERSION = 1;


    // the create sql command
    private static final String SQLITE_CREATE =
            "CREATE TABLE if not exists LocationTable (" +
                    KEY_Primary + " integer PRIMARY KEY autoincrement,"
                    + KEY_SessionID + " integer,"
                    + KEY_StepID + " integer,"
                    + KEY_Lat + ","
                    + KEY_Long + ","
                    + KEY_Distance + " real,"
                    + KEY_Elevation + ","
                    + KEY_Speed + " real,"
                    + KEY_Avg_Speed + " real,"
                    + KEY_Duration + " integer,"
                    + KEY_Date + " DATE,"
                    + KEY_IsTracking + ","
                    + KEY_Type + ","
                    + KEY_Score + " integer,"
                    + KEY_Notes + ","
                    + KEY_Weather
                    + ");";

    DBHelper(Context context) {
        super(context, context.getString(R.string.app_db_name), null, SQLITE_TABLE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create the database
        Log.d(TAG, "Database Created");
        db.execSQL(SQLITE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // update the database
        Log.d(TAG, String.format("onUpgrade: Database Version updated (%d -> %d)", oldVersion,
                newVersion));

        db.execSQL("DROP TABLE IF EXISTS LocationTable;");
        onCreate(db);
    }
}