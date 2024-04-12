// Reference: This provider is modified based on Lab008A

package com.fy916.android_fitness_tracker;

import android.net.Uri;

public class DataProviderContract {
    public static final String AUTHORITY = "com.fy916.android_fitness_tracker.DataProvider";

    public static final Uri DataUri = Uri.parse("content://" + AUTHORITY + "/LocationTable");
    public static final String KEY_Primary = "_id";
    public static final String KEY_SessionID = "Session_ID";
    public static final String KEY_StepID = "Step_ID";
    public static final String KEY_Lat = "Latitude";
    public static final String KEY_Long = "Longitude";
    public static final String KEY_Distance = "Distance";
    public static final String KEY_Elevation = "Elevation";
    public static final String KEY_Speed = "Speed";

    public static final String KEY_Avg_Speed = "AVGSPEED";
    public static final String KEY_Duration = "Duration";
    public static final String KEY_Date = "Date";
    public static final String KEY_IsTracking = "IsTracking";
    public static final String KEY_Type = "Type";
    public static final String KEY_Score = "Score";
    public static final String KEY_Notes = "Notes";
    public static final String KEY_Weather = "Weather";

    public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/DataProvider.data.text";
    public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/DataProvider.data.text";
}
