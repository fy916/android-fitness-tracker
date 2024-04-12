/*
 Author: fy916
 This is the activity that shows the detailed workout history page
 */


package com.fy916.android_fitness_tracker;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class DetailActivity extends FragmentActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {
    // database attributes and maps attributes
    private DBHelper mDbHelper;
    private SQLiteDatabase mDb;
    Cursor dataCursor;
    private GoogleMap mMap;
    private PolylineOptions polylineOptions;
    private Polyline polyline;
    private SupportMapFragment mapFragment;


    private TextView tstLatitudeVal;
    private TextView tstLongitudeVal;
    private TextView tstDistanceVal;
    private TextView tstEvaluationVal;
    private TextView tstSpeedVal;
    private TextView tstAVGSpeedVal;
    private TextView tstDurationVal;
    private TextView tstDateVal;
    private Spinner tstTypeVal;
    private Spinner tstScoreVal;
    private Spinner tstWeatherVal;
    private EditText tstNotesVal;
    private SeekBar seekBar;
    private ImageButton imageHolder;
    private Bitmap currentImage;


    // attributes of the status of the track and app data
    private double latitude;
    private double longitude;
    private double distance;
    private double elevation;
    private double speed;
    private double avgSpeed;
    private long duration;
    private String date;
    private boolean isTracking;
    private int type;
    private int score;
    private int weather;
    private String notes;
    private long historyLength;
    private long refDataId;
    private LatLng lastLatLng = new LatLng(0, 0);
    private LatLng currLatLng = new LatLng(0, 0);
    long sessionID;
    private Marker lastSelectedLocationMarker;

    public static final int PICK_IMAGE_REQUEST = 1;


    private boolean isStartPoint = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Detail Activity", "OnCreate");

        // get the session id of the workout history which the page shows
        Bundle bundle = getIntent().getExtras();
        sessionID = bundle.getLong("SessionID");

        // set views
        setContentView(R.layout.activity_details);

        // initialize the map
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

        // initialize the database tools
        mDbHelper = new DBHelper(this);
        mDb = mDbHelper.getWritableDatabase();

        // get the cursor of the specified workout history
        dataCursor = getWorkoutSessionData(sessionID);


        // get the reference of each components in the design layout
        tstLatitudeVal = findViewById(R.id.tstLatitudeVal);
        tstLongitudeVal = findViewById(R.id.tstLongitudeVal);
        tstDistanceVal = findViewById(R.id.tstDistanceVal);
        tstEvaluationVal = findViewById(R.id.tstEvaluationVal);
        tstSpeedVal = findViewById(R.id.tstSpeedVal);
        tstAVGSpeedVal = findViewById(R.id.tstAVGSpeedVal);
        tstDurationVal = findViewById(R.id.tstDurationVal);
        tstDateVal = findViewById(R.id.tstDateVal);
        tstTypeVal = findViewById(R.id.typeSpinner);
        tstScoreVal = findViewById(R.id.scoreSpinner);
        tstWeatherVal = findViewById(R.id.weatherSpinner);
        tstNotesVal = findViewById(R.id.tstNotesVal);
        seekBar = findViewById(R.id.seekBar);
        imageHolder = findViewById(R.id.imageButton);
        currentImage = BitmapFactory.decodeResource(getResources(), R.drawable.img_add);


        // reference https://developer.android.com/reference/android/widget/ArrayAdapter
        // https://developer.android.com/develop/ui/views/components/spinner
        // create an ArrayAdapter using the string array and a default spinner layout
        // used for selecting the type of the work
        ArrayAdapter<CharSequence> adapterType = ArrayAdapter.createFromResource(this,
                R.array.workoutType, android.R.layout.simple_spinner_item);
        // specify the layout to use when the list of choices appears
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // apply the adapter to the spinner
        tstTypeVal.setAdapter(adapterType);
        tstTypeVal.setOnItemSelectedListener(this);


        // reference https://developer.android.com/reference/android/widget/ArrayAdapter
        // https://developer.android.com/develop/ui/views/components/spinner
        // create an ArrayAdapter using the string array and a default spinner layout
        // used for selecting the score of the work
        ArrayAdapter<CharSequence> adapterScore = ArrayAdapter.createFromResource(this,
                R.array.scoreSelect, android.R.layout.simple_spinner_item);
        // specify the layout to use when the list of choices appears
        adapterScore.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // apply the adapter to the spinner
        tstScoreVal.setAdapter(adapterScore);
        tstScoreVal.setOnItemSelectedListener(this);


        // reference https://developer.android.com/reference/android/widget/ArrayAdapter
        // https://developer.android.com/develop/ui/views/components/spinner
        // create an ArrayAdapter using the string array and a default spinner layout
        // used for selecting the weather of the work
        ArrayAdapter<CharSequence> adapterWeather = ArrayAdapter.createFromResource(this,
                R.array.weatherSelect, android.R.layout.simple_spinner_item);
        // specify the layout to use when the list of choices appears
        adapterWeather.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // apply the adapter to the spinner
        tstWeatherVal.setAdapter(adapterWeather);
        tstWeatherVal.setOnItemSelectedListener(this);


        // reference https://developer.android.com/reference/android/widget/SeekBar
        // set the seek bar to the rightmost value as the view's default shows the end of the work
        seekBar.setProgress(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            // seek the location when user selects point to review workout history
            public void onStopTrackingTouch(SeekBar bar) {
                locateData(bar.getProgress());
            }

            public void onStartTrackingTouch(SeekBar bar) {

            }

            // seek the location when user selects point to review workout history
            public void onProgressChanged(SeekBar bar, int progress, boolean paramBoolean) {
                locateData(progress);
            }
        });


        // get the total number of recorded points for this workout
        historyLength = getStepsLengthOfSession(sessionID);
        // get the workout id from the database
        refDataId = getSessionDataID(sessionID);
        Log.d("historyLength", historyLength + "   " + refDataId);
    }


    // used to locate the data point of the workout by giving a progress in [0, 100]
    public void locateData(int progress) {
        double fraction = progress / 100.0;
        // calculate the position of index of the data point
        long position = (long) (fraction * historyLength);
        Log.d("fraction", progress + " " + fraction + " " + position);

        // check overflow
        if (position > historyLength) {
            position = historyLength;
        }


        // get the data at position index
        if (dataCursor.moveToPosition((int) position)) {
            latitude = dataCursor.getDouble(3);
            longitude = dataCursor.getDouble(4);
            distance = dataCursor.getDouble(5);
            elevation = dataCursor.getDouble(6);
            speed = dataCursor.getDouble(7);
            avgSpeed = dataCursor.getDouble(8);
            duration = dataCursor.getLong(9);
            date = dataCursor.getString(10);
            isTracking = dataCursor.getString(11).equals("true");
//            type = dataCursor.getInt(12);
//            score = dataCursor.getInt(13);
//            notes = dataCursor.getString(14);
//            weather = dataCursor.getInt(15);

            currLatLng = new LatLng(latitude, longitude);

            Log.d("Current Data", String.format("%f, %f, %f, %f, %f, %f, %d, %s, %s, %s, %s, %s\n", latitude, longitude, distance, elevation, speed, avgSpeed, duration, date, isTracking, type, score, notes, weather));


            // update the ui text
            updateTexts();

            // update the marker
            if (lastSelectedLocationMarker != null) {
                lastSelectedLocationMarker.remove();
            }

            // reference https://stackoverflow.com/questions/44069053/how-can-i-change-google-map-v2-default-red-marker-color-to-black-in-android
            lastSelectedLocationMarker = mMap.addMarker(new MarkerOptions().position(currLatLng).title("Selected Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

            // reference https://developers.google.com/maps/documentation/android-sdk/views
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currLatLng, 15));
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        // when the map is loaded, render ui and load information
        mMap = googleMap;
        renderUIWithHistory();
        Log.d("DetailActivity", "Map Rendered");
        // move to the end point of workout
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currLatLng, 15));
        // update the texts
        updateTexts();

        tstNotesVal.setText(notes);

        // update the type of work selection
        if (type < 0 || type > 6) {
            tstTypeVal.setSelection(0);
        } else {
            tstTypeVal.setSelection(type);
        }

        // update the score of work selection
        if (score < 0 || score > 5) {
            tstScoreVal.setSelection(0);
        } else {
            tstScoreVal.setSelection(score);
        }

        // update the weather of work selection
        if (weather < 0 || weather > 5) {
            tstWeatherVal.setElevation(0);
        } else {
            tstWeatherVal.setSelection(weather);
        }
    }


    public void updateTexts() {
        // format and update texts
        tstLatitudeVal.setText(String.format("%.4f", latitude));
        tstLongitudeVal.setText(String.format("%.4f", longitude));
        tstDistanceVal.setText(Utils.formatDistance(distance));
        tstEvaluationVal.setText(String.format("%.2f m", elevation));
        tstSpeedVal.setText(Utils.formatSpeed(speed));
        tstAVGSpeedVal.setText(Utils.formatSpeed(avgSpeed));
        tstDurationVal.setText(Utils.formatElapsedTime(duration));
        tstDateVal.setText(date);

    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        // when an item is selected from the spinner, update the information to database
        if (adapterView.getId() == R.id.typeSpinner) {
            updateType(refDataId, i);
        } else if (adapterView.getId() == R.id.scoreSpinner) {
            updateScore(refDataId, i);
        } else {
            updateWeather(refDataId, i);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    public void renderUIWithHistory() {
        // render the ui
        if (dataCursor.moveToFirst()) {
            do {
                // mark the last location before fetching next new location data
                lastLatLng = currLatLng;

                // get data point information from database
                latitude = dataCursor.getDouble(3);
                longitude = dataCursor.getDouble(4);
                distance = dataCursor.getDouble(5);
                elevation = dataCursor.getDouble(6);
                speed = dataCursor.getDouble(7);
                avgSpeed = dataCursor.getDouble(8);
                duration = dataCursor.getLong(9);
                date = dataCursor.getString(10);
                isTracking = dataCursor.getString(11).equals("true");
                type = dataCursor.getInt(12);
                score = dataCursor.getInt(13);
                notes = dataCursor.getString(14);
                weather = dataCursor.getInt(15);

                currLatLng = new LatLng(latitude, longitude);

                Log.d("Current Data", String.format("%f, %f, %f, %f, %f, %f, %d, %s, %s, %s, %s, %s\n", latitude, longitude, distance, elevation, speed, avgSpeed, duration, date, isTracking, type, score, notes, weather));

                updateTrack();
                isStartPoint = false;
            } while (dataCursor.moveToNext());
        }

        // load the user saved image file
        loadImageFromFile();
        renderImage();
        // add a marker of the end point
        mMap.addMarker(new MarkerOptions().position(currLatLng).title("End Location"));
    }


    // update the track of user on the map
    private void updateTrack() {
        // reference https://developers.google.com/android/reference/com/google/android/gms/maps/model/PolylineOptions
        polylineOptions = new PolylineOptions();

        if (isStartPoint) {
            // start point is marked as blue
            mMap.addMarker(new MarkerOptions().position(currLatLng).title("Start Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }

        // add tracks
        if (isTracking) {
            polylineOptions.width(8f).color(Color.RED);
        } else {
            polylineOptions.width(8f).color(Color.BLACK);
        }

        // add the track of each pair of consecutive point
        if (!isStartPoint) {
            polylineOptions.add(lastLatLng);
            polylineOptions.add(currLatLng);
        }

        // render the line to map
        polyline = mMap.addPolyline(polylineOptions);
    }


    @Override
    public void onBackPressed() {
        // when system back button is pressed, save the information by default
        updateNotes(refDataId, String.valueOf(tstNotesVal.getText()));
        Intent intent = new Intent(this, WorkoutHistoryActivity.class);
        startActivity(intent);
    }

    public void backToList(View view) {
        // when save button is clicked (in the view), save the information
        updateNotes(refDataId, String.valueOf(tstNotesVal.getText()));
        Intent intent = new Intent(this, WorkoutHistoryActivity.class);
        startActivity(intent);
    }


    public void deleteAndBack(View view) {
        // when delete button is clicked (in the view), delete the record from database

        // use a alertdialog to ask user to check if they really want to delete record
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
        builder.setTitle("Are you sure to delete this record?");

        // Set up the buttons
        // https://developer.android.com/reference/android/app/AlertDialog
        // https://developer.android.com/reference/android/app/AlertDialog.Builder#setPositiveButton(int,%20android.content.DialogInterface.OnClickListener)
        // https://developer.android.com/reference/android/app/AlertDialog.Builder#setNegativeButton(java.lang.CharSequence,%20android.content.DialogInterface.OnClickListener)
        // https://developer.android.com/reference/android/content/DialogInterface.OnClickListener
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // if they want to delete, then remove record, and back to workout history page
                remove_history(sessionID);
                deleteImage();
                Intent intent = new Intent(view.getContext(), WorkoutHistoryActivity.class);
                startActivity(intent);
            }
        });

        // if user does not want to delete, keep them in the view
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        // show the dialog if user clicks delete button
        dialog.show();
    }


    // render image with its original scale
    // reference of setting scaled image
    // https://stackoverflow.com/questions/18077325/scale-image-to-fill-imageview-width-and-keep-aspect-ratio
    public void renderImage() {
        int width = imageHolder.getMeasuredWidth();
        int diw = currentImage.getWidth();
        if (diw > 0) {
            int height = 0;
            // calculate the corresponding height and width
            height = width * currentImage.getHeight() / diw;
            // create a scaled bitmap fits the screen
            Bitmap scaled_img = Bitmap.createScaledBitmap(currentImage, width, height, false);
            Drawable imageDrawable = new BitmapDrawable(getResources(), scaled_img);
            // set the image for the image holder
            imageHolder.setBackground(imageDrawable);
            imageHolder.setScaleType(ImageView.ScaleType.CENTER);
        }
    }


    // when the image is clicked, ask user if they want to remove image or change to a new one
    public void onSelectImageClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Do you want to remove or change the image?");
        // Set up the buttons
        // https://developer.android.com/reference/android/app/AlertDialog
        // https://developer.android.com/reference/android/app/AlertDialog.Builder#setPositiveButton(int,%20android.content.DialogInterface.OnClickListener)
        // https://developer.android.com/reference/android/app/AlertDialog.Builder#setNegativeButton(java.lang.CharSequence,%20android.content.DialogInterface.OnClickListener)
        // https://developer.android.com/reference/android/content/DialogInterface.OnClickListener
        builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // if they want to remove the image, replace them with default image
                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.img_add);
                currentImage = icon;
                saveImage();
                renderImage();
            }
        });
        builder.setNegativeButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // if they want to change the image, prompt a file selector for image selection
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                //https://developer.android.com/reference/android/content/Intent#setType(java.lang.String)
                intent.setType("image/*"); // select any type of image file
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                // ask the file chooser to get an uri of image
                startActivityForResult(Intent.createChooser(intent, "Select image file"), PICK_IMAGE_REQUEST);
            }
        });
        AlertDialog dialog = builder.create();
        // show the dialog
        dialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if the user successfully picks an image from the file browser, load it to image holder and save it to database
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getData() != null) {
                // Get the image URI from the result data
                Uri uri = data.getData();
                Log.d("Detail Activity", "User picks image from uri: " + uri);

                // get the bitmap of user chosen image
                Bitmap bitmap = null;

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (bitmap != null) {
                    // if successfully get an image
                    // Convert bitmap to byte array
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    currentImage = bitmap;
                    // delete the existing image and change it to the new one
                    deleteImage();
                    renderImage();
                    saveImage();
                }
            }

        }
    }


    // load image from app's data file
    public void loadImageFromFile() {
        // reference https://developer.android.com/reference/android/content/Context#getFilesDir()
        File dataDir = getApplicationContext().getFilesDir();
        String fileName = sessionID + ".jpg";
        File file = new File(dataDir, fileName);

        // load the image from the file path
        if (file.exists()) {
            // reference https://developer.android.com/reference/android/graphics/Bitmap.Config
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            // get the image
            currentImage = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            if (currentImage == null) {
                Log.d("Detail Activity", "Failed to load image");
            }
        } else {
            Log.d("Detail Activity", "File does not exist");
        }
    }


    // save image with session ID
    public void saveImage() {
        // reference https://developer.android.com/reference/android/content/Context#getFilesDir()
        // reference https://www.geeksforgeeks.org/java-program-to-convert-byte-array-to-image/

        File dataDir = getApplicationContext().getFilesDir();
        // save the image to local data folder with the sessionID as its name
        String fileName = sessionID + ".jpg";
        // create a file for storing
        File file = new File(dataDir, fileName);
        // send the bitmap to file stream
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        currentImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageBits = stream.toByteArray();

        // write the byte to file
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(imageBits);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // delete image with session ID
    public void deleteImage() {
        // get the file
        File file = new File(getFilesDir(), sessionID + ".jpg");
        if (file.exists()) {
            // delete the file
            boolean deleted = file.delete();
            if (deleted) {
                Log.d("Detail Activity", "Image deleted successfully");
            } else {
                Log.d("Detail Activity", "Failed to delete image");
            }
        } else {
            Log.d("Detail Activity", "Image file does not exist");
        }
    }


    // remove the history from database
    public void remove_history(long sessionID) {
        // get the content provider and use it to delete rows with the session ID, reference is Lab008A
        getContentResolver().delete(DataProviderContract.DataUri, "Session_ID = ?", new String[]{String.valueOf(sessionID)});
    }


    // get the cursor of the workout data given the session ID
    public Cursor getWorkoutSessionData(long sessionID) {
        // perform a sql query, reference is Lab007A
        Cursor c = mDb.query(
                "LocationTable",
                new String[]{"_id", "Session_ID", "Step_ID", "Latitude", "Longitude", "Distance", "Elevation", "Speed", "AVGSPEED", "Duration", "Date", "IsTracking", "Type", "Score", "Notes", "Weather"},
                "Session_ID = ?",
                new String[]{String.valueOf(sessionID)}, // Pass the session ID value
                null,
                null,
                null
        );
        return c;
    }


    public long getStepsLengthOfSession(long sessionID) {
        // get the total record points of this session ID
        // perform a sql query, reference is Lab007A
        Cursor c = mDb.query(
                "LocationTable",
                new String[]{"MAX(Step_ID) as Max_Step_ID"}, // get the max step ID which counts as the total record number
                "Session_ID = ?",
                new String[]{String.valueOf(sessionID)}, // Pass the session ID value
                null,
                null,
                null
        );

        // get the max step id as the total record number
        long maxStep = 0;
        if (c.moveToFirst()) {
            int columnIndex = c.getColumnIndex("Max_Step_ID");
            maxStep = c.getLong(columnIndex);
        }
        return maxStep;
    }


    public long getSessionDataID(long sessionID) {
        // session id is the unique id of one time of workout
        // perform a sql query, reference is Lab007A
        Cursor c = mDb.query(
                "LocationTable",
                new String[]{"MAX(_id) as ID"}, // get the last row of the data with session ID, which stores complete information
                "Session_ID = ?",
                new String[]{String.valueOf(sessionID)}, // Pass the session ID value
                null,
                null,
                null
        );

        // get the unique key id that stores the complete information
        long refID = 0;
        if (c.moveToFirst()) {
            int columnIndex = c.getColumnIndex("ID");
            refID = c.getLong(columnIndex);
        }
        return refID;
    }


    // when user selects a type of work, write it to the database (last row of record with the Session ID)
    public void updateType(long globalID, int newValue) {
        // create values for writing
        ContentValues values = new ContentValues();
        values.put("Type", newValue);

        String tableName = "LocationTable";
        String whereClause = "_id = ?";
        String[] whereArgs = new String[]{String.valueOf(globalID)};

        // update the database with given id and values
        mDb.update(tableName, values, whereClause, whereArgs);
    }


    // when user selects a score of work, write it to the database (last row of record with the Session ID)
    public void updateScore(long globalID, int newValue) {
        // create values for writing
        ContentValues values = new ContentValues();
        values.put("Score", newValue);

        String tableName = "LocationTable";
        String whereClause = "_id = ?";
        String[] whereArgs = new String[]{String.valueOf(globalID)};

        // update the database with given id and values
        mDb.update(tableName, values, whereClause, whereArgs);
    }

    // when user updates the notes, write it to the database (last row of record with the Session ID)
    public void updateNotes(long globalID, String note) {
        // create values for writing
        ContentValues values = new ContentValues();
        values.put("Notes", note);

        String tableName = "LocationTable";
        String whereClause = "_id = ?";
        String[] whereArgs = new String[]{String.valueOf(globalID)};

        // update the database with given id and values
        mDb.update(tableName, values, whereClause, whereArgs);
    }

    // when user selects a weather of work, write it to the database (last row of record with the Session ID)
    public void updateWeather(long globalID, int weather) {
        // create values for writing
        ContentValues values = new ContentValues();
        values.put("Weather", weather);

        String tableName = "LocationTable";
        String whereClause = "_id = ?";
        String[] whereArgs = new String[]{String.valueOf(globalID)};

        // update the database with given id and values
        mDb.update(tableName, values, whereClause, whereArgs);
    }


}