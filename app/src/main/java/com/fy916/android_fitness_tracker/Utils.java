/*
 Author: fy916
 This class contains the utilities used in this app
 */

package com.fy916.android_fitness_tracker;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class Utils {


    public static String get_current_time() {
        // get current time in format
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.getDefault());
        String currentDateTime = simpleDateFormat.format(calendar.getTime());
        return currentDateTime;
    }


    public static String formatSpeed(double speedInMeters) {
        // format speed which is in meters, to kilometers if larger than 1000 m/s
        if (speedInMeters >= 1000) {
            double speedInKM = speedInMeters / 1000.0;
            return String.format("%.2f km/s", speedInKM);
        } else {
            return String.format("%.2f m/s", speedInMeters);
        }
    }

    public static String formatElapsedTime(long elapsedTime) {
        // format time, from milliseconds to HH:MM:SS format
        long hours = TimeUnit.MILLISECONDS.toHours(elapsedTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60;

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }


    public static float calculateDistance(LatLng startLatLng, LatLng endLatLng) {
        // calc distances between two coordinates
        // use Location class to calc
        Location startLoc = new Location("startLoc");
        startLoc.setLatitude(startLatLng.latitude);
        startLoc.setLongitude(startLatLng.longitude);

        Location endLoc = new Location("endLoc");
        endLoc.setLatitude(endLatLng.latitude);
        endLoc.setLongitude(endLatLng.longitude);

        return startLoc.distanceTo(endLoc);
    }


    public static String formatAVGSpeed(long time_spent, double current_distance) {
        // calculate the average speed and then format it
        double avgSpeed;
        long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(time_spent); // get seconds

        // avoid division by 0
        if (totalSeconds == 0) {
            avgSpeed = 0;
        } else {
            avgSpeed = current_distance / totalSeconds;
        }
        return Utils.formatSpeed(avgSpeed);
    }


    public static double calcAVGSpeed(long time_spent, double current_distance) {
        // calc the average speed only
        double avgSpeed;
        long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(time_spent); // get seconds

        // avoid division by 0
        if (totalSeconds == 0) {
            avgSpeed = 0;
        } else {
            avgSpeed = current_distance / totalSeconds;
        }
        return avgSpeed;
    }


    public static String formatElevation(double currAltitude) {
        // format the altitude of the location
        return String.format("%.2f m", currAltitude);
    }


    public static String formatDistance(double current_distance) {
        // format distance, when distance in meters is larger than 1000, make the unit as km
        if (current_distance >= 1000) {
            double distanceInKM = current_distance / 1000.0;
            return String.format("%.2f km", distanceInKM);
        } else {
            return String.format("%.2f m", current_distance);
        }
    }
}
