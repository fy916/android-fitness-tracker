/*
 Author: fy916
 This is the activity that shows the tracking page of this app
 */


package com.fy916.android_fitness_tracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

import com.fy916.android_fitness_tracker.databinding.ActivityMapsBinding;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    // database attributes and maps attributes

    private DBHelper mDbHelper;
    private SQLiteDatabase mDb;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private SupportMapFragment mapFragment;
    private boolean mLocationPermissionGranted = false;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private PolylineOptions polylineOptions;
    private Polyline polyline;
    private CountDownTimer[] timer;


    // text views and button references
    private TextView textTime;
    private TextView textSpeed;
    private TextView textLength;
    private TextView textSessionID;
    private TextView textElevation;
    private TextView textAvgDistance;


    private Button backButton;
    private Button startButton;
    private Button saveButton;


    // attributes of the status of the track and app data
    private LatLng lastRecordedLatLng = new LatLng(0, 0);
    private LatLng lastLatLng = new LatLng(0, 0);
    private double lastAltitude = 0;
    private LatLng currLatLng = new LatLng(0, 0);
    private double currAltitude = 0;
    private double current_distance = 0;
    private long time_spent = 0;
    private double current_speed = 0;
    private Marker lastKnownLocationMarker;
    private long current_sessionID = 0;
    private long current_stepID = 0;


    private boolean isLocated = false;
    private boolean isTracking = false;
    private boolean isStartPoint = true;
    private boolean last_paused = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Maps Activity", "On Create!");

        // reference https://developers.google.com/maps/documentation/android-sdk/map
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // reference Lab007A
        mDbHelper = new DBHelper(this);
        mDb = mDbHelper.getWritableDatabase();

        // set the session ID of this workout
        current_sessionID = get_new_instance_ID() + 1;
        Log.d("sessionID", String.valueOf(current_sessionID));

        // reference https://developers.google.com/maps/documentation/android-sdk/map
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // set references of view components
        textTime = findViewById(R.id.textDuration);
        textSpeed = findViewById(R.id.textspeed);
        textLength = findViewById(R.id.textDistance);
        startButton = findViewById(R.id.startandpause);
        saveButton = findViewById(R.id.save);
        textSessionID = findViewById(R.id.textID);
        textElevation = findViewById(R.id.textElevation);
        textAvgDistance = findViewById(R.id.textAvgSpeed);


        // set initial values
        textSessionID.setText("Status:\n    Waiting GPS    ");
        textSessionID.setTextColor(Color.BLUE);
        textTime.setText("      Duration:       \n00:00:00");
        textSpeed.setText("Speed: \n0.00 m/s");
        textLength.setText("Distance: \n0 m");
        textAvgDistance.setText("AVG Speed: \n0.00 m/s");
        startButton.setText("Start");
        textElevation.setText("Elevation: \n0 m");


        // Initialize the LocationManager and LocationListener
        // reference https://developer.android.com/reference/android/location/LocationManager
        // https://developer.android.com/reference/android/location/LocationListener
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Update the track and map with the new location
                Log.d("Location", location.getLatitude() + " " + location.getLongitude() + " " + location.getAltitude());
                if (isStartPoint) {
                    // if user has not clicked start yet, set the status to ready
                    textSessionID.setText("Status:\nReady");
                    textSessionID.setTextColor(Color.GREEN);
                }
                // when location is fetched, mark it as located
                isLocated = true;
                lastLatLng = currLatLng;
                lastAltitude = currAltitude;

                // update location continuously
                currLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                currAltitude = location.getAltitude();

                // update the UI
                updateUI();
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };


        // set a countdown timer which can be used to refresh time and update information
        // reference https://developer.android.com/reference/android/os/CountDownTimer
        timer = new CountDownTimer[1];
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTracking == false) {
                    // if the current state is "paused" when user presses button
                    if (isLocated) {
                        // if the location is fetched
                        if (isStartPoint) {
                            // if the location is start point (not recorded yet)
                            // add a blue marker to indicate the user's start location
                            // reference https://stackoverflow.com/questions/44069053/how-can-i-change-google-map-v2-default-red-marker-color-to-black-in-android
                            mMap.addMarker(new MarkerOptions().position(currLatLng).title("Start Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            isStartPoint = false;
                        }

                        // set the status to recording
                        textSessionID.setText("Status:\nRecording");
                        textSessionID.setTextColor(Color.RED);
                        startButton.setText("Pause");

                        // start the timer which counts every second
                        timer[0] = new CountDownTimer(Long.MAX_VALUE, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                // when 1s passes, update the duration
                                time_spent += 1000;
                                String time = Utils.formatElapsedTime(time_spent);
                                textTime.setText("Duration: \n" + time);
                            }

                            @Override
                            public void onFinish() {

                            }
                        };
                        // start the timer
                        timer[0].start();
                        isTracking = true;
                    } else {
                        // if the location is not fetched yet, tell user the record cannot start yet
                        Toast.makeText(v.getContext(), "Location is not updated yet, please wait few seconds!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // if the current state is "tracking/recording" when user presses button
                    // pause the tracking
                    textSessionID.setText("Status:\nPaused");
                    textSessionID.setTextColor(Color.BLUE);
                    startButton.setText("Continue");
                    current_speed = 0;
                    textSpeed.setText("Speed: \n0.00 m/s");
                    // Stop the timer
                    timer[0].cancel();
                    isTracking = false;
                    last_paused = true;
                    current_speed = 0;
                }
            }
        });
        getLocationPermission();
    }


    private void updateUI() {
        updateMap();
        updateTrack();

        if (time_spent != 0) { // if the record contains information, save it to database
            save_to_DB();
        }

        if (isTracking) {
            // update the ui parameters
            textLength.setText("Distance: \n" + updateDistance());
            textSpeed.setText("Speed: \n" + Utils.formatSpeed(current_speed));
            textElevation.setText("Elevation: \n" + Utils.formatElevation(currAltitude));
            textAvgDistance.setText("AVG Speed: \n" + Utils.formatAVGSpeed(time_spent, current_distance));
        }
    }


    public String updateDistance() {
        // update distance by adding new pair of distance to original distance
        if (!last_paused) {
            current_distance += Utils.calculateDistance(currLatLng, lastRecordedLatLng);
            current_speed = Utils.calculateDistance(currLatLng, lastRecordedLatLng);
        }
        lastRecordedLatLng = currLatLng;
        last_paused = false;
        return Utils.formatDistance(current_distance);
    }


    private void getLocationPermission() {
        // get the permission of location
        // reference https://developer.android.com/training/location/permissions
        // https://www.programcreek.com/java-api-examples/?class=android.support.v4.content.ContextCompat&method=checkSelfPermission
        // https://developers.google.com/maps/documentation/places/android-sdk/current-place-tutorial
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // if we have permission, start the location update
            mLocationPermissionGranted = true;
            startLocationUpdates();
        } else {
            // if we do not have location permission, request one
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // reference https://developer.android.com/training/permissions/requesting#request-permission
        // https://developers.google.com/maps/documentation/places/android-sdk/current-place-tutorial
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) { // if the request is about location request
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // if the request is successful, get the result array with info
                // we can update location with permissions now
                mLocationPermissionGranted = true;
                startLocationUpdates();
                Toast.makeText(this, "Location permission is successfully granted!", Toast.LENGTH_SHORT).show();
            } else {
                // If request is cancelled, the result arrays does not contain info
                // use a fixed, non cancelable alertdialog to tell user about location permission issue
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("Location permission is not given!");
                builder.setMessage("Please enable the location permission of this app in the settings! ");
                builder.setCancelable(false);

                // show the dialog
                AlertDialog dialog = builder.create();
                dialog.show();
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }


    private void startLocationUpdates() {
        // update the location
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // if there is problem with the permission, tell user
            Toast.makeText(this, "Location permission is not given!", Toast.LENGTH_SHORT).show();
            return;
        }
        // start a request of refreshing location every second
        // reference is the coursework issue sheet
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener, Looper.getMainLooper());
    }


    // update the track of user on the map
    private void updateTrack() {
        // reference https://developers.google.com/android/reference/com/google/android/gms/maps/model/PolylineOptions
        polylineOptions = new PolylineOptions();
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


    private void updateMap() {
        // update the marker on the map
        if (lastKnownLocationMarker != null) {
            // remove last marker of current location
            lastKnownLocationMarker.remove();
        }

        // reference https://developers.google.com/maps/documentation/android-sdk/views
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currLatLng, 15));
        // add a marker to the current location
        // reference https://stackoverflow.com/questions/44069053/how-can-i-change-google-map-v2-default-red-marker-color-to-black-in-android
        lastKnownLocationMarker = mMap.addMarker(new MarkerOptions().position(currLatLng).title("Current Location"));
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void save_to_DB() {
        // save the data to database
        // perform a sql command
        // save the information to the database
        mDb.execSQL("INSERT INTO LocationTable (Session_ID, Step_ID, Latitude, Longitude, Distance, Elevation, Speed, AVGSPEED, Duration, Date, IsTracking) "
                + "VALUES " + "('" + current_sessionID + "','" + current_stepID + "','" + currLatLng.latitude + "','" + currLatLng.longitude + "','" + current_distance + "','" + currAltitude + "','" + current_speed + "','" + Utils.calcAVGSpeed(time_spent, current_distance) + "','" + time_spent + "','" + Utils.get_current_time() + "','" + isTracking + "');");

        current_stepID += 1;
    }


    public void saveWorkout(View view) {
        // when the user clicks save button
        // stop location updating
        locationManager.removeUpdates(locationListener);
        // cancel the timer
        if (timer[0] != null) {
            timer[0].cancel();
        }

        // turn to the workout result detail page with the session id of saved record
        Intent intent = new Intent(this, DetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putLong("SessionID", Long.parseLong(String.valueOf(current_sessionID)));
        intent.putExtras(bundle);
        startActivity(intent);

    }


    // if the user wants to back to main
    public void discardAndBackToMain(View view) {
        // use alert dialog to ask if they really want to discard the info
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("Are you sure to discard this workout and back to main page?");

        // Set up the buttons
        // https://developer.android.com/reference/android/app/AlertDialog
        // https://developer.android.com/reference/android/app/AlertDialog.Builder#setPositiveButton(int,%20android.content.DialogInterface.OnClickListener)
        // https://developer.android.com/reference/android/app/AlertDialog.Builder#setNegativeButton(java.lang.CharSequence,%20android.content.DialogInterface.OnClickListener)
        // https://developer.android.com/reference/android/content/DialogInterface.OnClickListener
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // stop location updating
                locationManager.removeUpdates(locationListener);
                // cancel timer
                if (timer[0] != null) {
                    timer[0].cancel();
                }
                // remove record
                remove_history(current_sessionID);
                // back to main
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    // if the user wants to back to main
    @Override
    public void onBackPressed() {
        // use alert dialog to ask if they really want to discard the info
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("Are you sure to discard this workout and back to main page?");

        // Set up the buttons
        // https://developer.android.com/reference/android/app/AlertDialog
        // https://developer.android.com/reference/android/app/AlertDialog.Builder#setPositiveButton(int,%20android.content.DialogInterface.OnClickListener)
        // https://developer.android.com/reference/android/app/AlertDialog.Builder#setNegativeButton(java.lang.CharSequence,%20android.content.DialogInterface.OnClickListener)
        // https://developer.android.com/reference/android/content/DialogInterface.OnClickListener
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // stop location updating
                locationManager.removeUpdates(locationListener);
                // cancel timer
                if (timer[0] != null) {
                    timer[0].cancel();
                }
                // remove record
                remove_history(current_sessionID);
                // back to main
                Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public long get_new_instance_ID() {
        // get the session ID for the new record, check the largest existing one and the new one is +1
        // perform a sql query, reference is Lab007A
        String query = "SELECT MAX(Session_ID) AS Largest_SessionID FROM LocationTable";
        Cursor cursor = mDb.rawQuery(query, null);

        // get the existing largest session ID
        long largestSessionID = 0;
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex("Largest_SessionID");
            if (columnIndex != -1) {
                largestSessionID = cursor.getLong(columnIndex);
            } else { // if data is not correct, set 1 to default
                largestSessionID = 1;
            }
        }
        cursor.close();

        return largestSessionID;
    }


    public void remove_history(long sessionID) {
        // use content provider to remove the history with given session ID
        getContentResolver().delete(DataProviderContract.DataUri, "Session_ID = ?", new String[]{String.valueOf(sessionID)});
    }
}


