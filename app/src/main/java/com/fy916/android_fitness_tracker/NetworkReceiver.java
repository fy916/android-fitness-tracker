// Reference: This receiver is modified based on Lab10

package com.fy916.android_fitness_tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkReceiver extends BroadcastReceiver {
    public NetworkReceiver() {
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();


        // if the user is not connected to network, show a toast info
        if (!isConnected) {
            Toast.makeText(context, "You are not connected to internet! This app requires Internet Connection to load the map! ", Toast.LENGTH_LONG).show();
        }
    }
}
