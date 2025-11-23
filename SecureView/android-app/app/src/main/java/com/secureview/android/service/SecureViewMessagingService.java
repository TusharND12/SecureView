package com.secureview.android.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.secureview.android.IntrusionAlertActivity;
import com.secureview.android.R;

import java.util.Map;

/**
 * Handles incoming Firebase Cloud Messaging notifications.
 */
public class SecureViewMessagingService extends FirebaseMessagingService {
    
    private static final String TAG = "SecureViewFCM";
    private static final String CHANNEL_ID = "secureview_intrusion_channel";
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
    
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        
        // Check if message contains data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }
        
        // Check if message contains notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotificationMessage(remoteMessage);
        }
    }
    
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        // Send token to your server if needed
    }
    
    private void handleDataMessage(Map<String, String> data) {
        String type = data.get("type");
        
        if ("intrusion".equals(type)) {
            showIntrusionNotification(data);
        } else if ("test".equals(type)) {
            showTestNotification(data);
        }
    }
    
    private void handleNotificationMessage(RemoteMessage remoteMessage) {
        // Show notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle(remoteMessage.getNotification().getTitle())
            .setContentText(remoteMessage.getNotification().getBody())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true);
        
        NotificationManager notificationManager = 
            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
    
    private void showIntrusionNotification(Map<String, String> data) {
        String timestamp = data.get("timestamp");
        String imageBase64 = data.get("image");
        String details = data.get("details");
        
        // Create intent for IntrusionAlertActivity
        Intent intent = new Intent(this, IntrusionAlertActivity.class);
        intent.putExtra("image", imageBase64);
        intent.putExtra("timestamp", timestamp);
        intent.putExtra("details", details);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        
        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("🚨 Intrusion Alert")
            .setContentText("Unauthorized access attempt detected!")
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("Time: " + timestamp + "\n" + details))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(new long[]{0, 500, 250, 500})
            .setLights(0xFF0000, 1000, 1000);
        
        NotificationManager notificationManager = 
            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }
    
    private void showTestNotification(Map<String, String> data) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("SecureView Test")
            .setContentText("Firebase Cloud Messaging is working!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true);
        
        NotificationManager notificationManager = 
            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SecureView Intrusion Alerts";
            String description = "Notifications for intrusion alerts from SecureView";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.enableLights(true);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

