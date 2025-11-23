package com.secureview.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Base64;

/**
 * Activity that displays intrusion alert with intruder's image.
 */
public class IntrusionAlertActivity extends AppCompatActivity {
    
    private ImageView intruderImageView;
    private TextView timestampTextView;
    private TextView detailsTextView;
    private Button lockButton;
    private Button alarmButton;
    private Button dismissButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intrusion_alert);
        
        initializeViews();
        loadIntrusionData();
    }
    
    private void initializeViews() {
        intruderImageView = findViewById(R.id.intruderImageView);
        timestampTextView = findViewById(R.id.timestampTextView);
        detailsTextView = findViewById(R.id.detailsTextView);
        lockButton = findViewById(R.id.lockButton);
        alarmButton = findViewById(R.id.alarmButton);
        dismissButton = findViewById(R.id.dismissButton);
        
        lockButton.setOnClickListener(v -> sendRemoteAction("lock"));
        alarmButton.setOnClickListener(v -> sendRemoteAction("alarm"));
        dismissButton.setOnClickListener(v -> finish());
    }
    
    private void loadIntrusionData() {
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        
        String imageBase64 = intent.getStringExtra("image");
        String timestamp = intent.getStringExtra("timestamp");
        String details = intent.getStringExtra("details");
        
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                intruderImageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                intruderImageView.setImageResource(R.drawable.ic_warning);
            }
        }
        
        if (timestamp != null) {
            timestampTextView.setText("Time: " + timestamp);
        }
        
        if (details != null) {
            detailsTextView.setText("Details: " + details);
        }
        
        // Show alert dialog
        showAlertDialog();
    }
    
    private void showAlertDialog() {
        new AlertDialog.Builder(this)
            .setTitle("🚨 Intrusion Alert")
            .setMessage("Unauthorized access attempt detected on your laptop!")
            .setPositiveButton("View Details", (dialog, which) -> {
                // Already viewing details
            })
            .setNegativeButton("Dismiss", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }
    
    private void sendRemoteAction(String action) {
        // In a full implementation, this would send a command back to the desktop app
        // via Firebase Realtime Database or Cloud Functions
        
        // For now, show a message
        new AlertDialog.Builder(this)
            .setTitle("Remote Action")
            .setMessage("Action '" + action + "' would be sent to your laptop.\n\n" +
                "Note: This requires Firebase Realtime Database or Cloud Functions integration.")
            .setPositiveButton("OK", null)
            .show();
    }
}

