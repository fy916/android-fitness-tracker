/*
 Author: fy916
 This is the activity shows the tutorial page
 */

package com.fy916.android_fitness_tracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

public class TutorialActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TutorialActivity", "onCreate!");

        requestWindowFeature(Window.FEATURE_NO_TITLE); // hide the title
        //https://developer.android.com/reference/androidx/appcompat/app/ActionBar#hide()
        getSupportActionBar().hide(); // hide the title bar
        setContentView(R.layout.activity_tutorials); //set the layout to activity_tutorials.xml
    }

    // when user wants to go back to main page
    public void backToMainPage(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}


