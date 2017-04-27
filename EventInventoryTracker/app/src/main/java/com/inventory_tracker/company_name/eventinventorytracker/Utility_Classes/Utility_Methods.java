package com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by brian on 4/19/2017.
 */

public class Utility_Methods {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void createToast(String message, Context context){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
