package com.secureview.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.secureview.android.MainActivity;

/**
 * Receives boot completed broadcast to ensure FCM token is refreshed.
 */
public class BootReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BootReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed, refreshing FCM token");
            // FCM token will be automatically refreshed when app starts
        }
    }
}

