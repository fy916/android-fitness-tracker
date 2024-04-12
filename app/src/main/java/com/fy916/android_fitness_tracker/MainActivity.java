/*
 Author: fy916
 This is the activity that shows the main page when user opens this app
 reference of two images used in this app:
    https://www.flaticon.com/free-icon/add-image_6520324
    https://www.flaticon.com/free-icon/run_6896430
 */

package com.fy916.android_fitness_tracker;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button tutorialButton;
    private Button historyButton;
    private Button startTrackingButton;

    private NetworkReceiver networkReceiver;
    IntentFilter intentFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Main Activity", "onCreate!");

        requestWindowFeature(Window.FEATURE_NO_TITLE); // hide the title
        //https://developer.android.com/reference/androidx/appcompat/app/ActionBar#hide()
        getSupportActionBar().hide(); // hide the title bar

        setContentView(R.layout.activity_entry); //set the layout to activity_entry.xml

        // get reference of buttons
        tutorialButton = findViewById(R.id.tutorials);
        historyButton = findViewById(R.id.history);
        startTrackingButton = findViewById(R.id.startTracking);


        // create a networkReceiver to check if user is connected to Internet
        // reference Lab009
        networkReceiver = new NetworkReceiver();
        intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
    }

    // log onDestory status
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkReceiver);
        Log.d("MainActivity", "onDestroy");
    }

    // log onPause status
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity", "onPause");
    }

    // log onResume status
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkReceiver, intentFilter);
        Log.d("MainActivity", "onResume");
    }

    // log onStart status
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("MainActivity", "onStart");
    }

    // log onStop status
    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MainActivity", "onStop");
    }

    // reference: https://stackoverflow.com/questions/3141996/android-how-to-override-the-back-button-so-it-doesnt-finish-my-activity
    @Override
    public void onBackPressed() {
        // move the app to background when the back button is clicked
        moveTaskToBack(true);
    }


    // when start workout button is pressed, start tracking user movements
    public void onClickStartWorkout(View v) {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    // when tutorial button is pressed, show the tutorial page
    public void onClickShowTutorial(View v) {
        Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
        startActivity(intent);
    }

    // when workout history button is pressed, show the history page
    public void onClickShowHistory(View v) {
        Intent intent = new Intent(MainActivity.this, WorkoutHistoryActivity.class);
        startActivity(intent);
    }
}
