package com.secureview.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Main activity for SecureView Android app.
 * Displays device token and allows configuration.
 */
public class MainActivity extends AppCompatActivity {
    
    private TextView deviceTokenTextView;
    private TextView statusTextView;
    private Button copyTokenButton;
    private SharedPreferences preferences;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        preferences = getSharedPreferences("SecureViewPrefs", MODE_PRIVATE);
        
        initializeViews();
        setupFirebaseToken();
    }
    
    private void initializeViews() {
        deviceTokenTextView = findViewById(R.id.deviceTokenTextView);
        statusTextView = findViewById(R.id.statusTextView);
        copyTokenButton = findViewById(R.id.copyTokenButton);
        
        copyTokenButton.setOnClickListener(v -> copyTokenToClipboard());
        
        // Check if app was opened from notification
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("intrusion_alert")) {
            // Open intrusion alert activity
            Intent alertIntent = new Intent(this, IntrusionAlertActivity.class);
            alertIntent.putExtras(intent.getExtras());
            startActivity(alertIntent);
        }
    }
    
    private void setupFirebaseToken() {
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    statusTextView.setText("Failed to get FCM token");
                    return;
                }
                
                String token = task.getResult();
                deviceTokenTextView.setText(token);
                
                // Save token to preferences
                preferences.edit().putString("fcm_token", token).apply();
                
                statusTextView.setText("Device registered. Copy the token to your desktop app configuration.");
                
                Toast.makeText(this, "FCM Token generated successfully!", Toast.LENGTH_SHORT).show();
            });
    }
    
    private void copyTokenToClipboard() {
        String token = deviceTokenTextView.getText().toString();
        if (token.isEmpty()) {
            Toast.makeText(this, "Token not available yet", Toast.LENGTH_SHORT).show();
            return;
        }
        
        android.content.ClipboardManager clipboard = 
            (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("FCM Token", token);
        clipboard.setPrimaryClip(clip);
        
        Toast.makeText(this, "Token copied to clipboard!", Toast.LENGTH_SHORT).show();
    }
}

