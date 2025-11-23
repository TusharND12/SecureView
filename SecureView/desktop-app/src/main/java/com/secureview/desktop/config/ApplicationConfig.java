package com.secureview.desktop.config;

/**
 * Application configuration model.
 */
public class ApplicationConfig {
    private double faceRecognitionThreshold;
    private boolean livenessDetectionEnabled;
    private int maxFailedAttempts;
    private long lockoutDuration; // milliseconds
    private String firebaseProjectId;
    private String firebaseCredentialsPath;
    private String deviceToken;
    private String dataDirectory;
    private String logsDirectory;
    
    // Getters and Setters
    public double getFaceRecognitionThreshold() {
        return faceRecognitionThreshold;
    }
    
    public void setFaceRecognitionThreshold(double faceRecognitionThreshold) {
        this.faceRecognitionThreshold = faceRecognitionThreshold;
    }
    
    public boolean isLivenessDetectionEnabled() {
        return livenessDetectionEnabled;
    }
    
    public void setLivenessDetectionEnabled(boolean livenessDetectionEnabled) {
        this.livenessDetectionEnabled = livenessDetectionEnabled;
    }
    
    public int getMaxFailedAttempts() {
        return maxFailedAttempts;
    }
    
    public void setMaxFailedAttempts(int maxFailedAttempts) {
        this.maxFailedAttempts = maxFailedAttempts;
    }
    
    public long getLockoutDuration() {
        return lockoutDuration;
    }
    
    public void setLockoutDuration(long lockoutDuration) {
        this.lockoutDuration = lockoutDuration;
    }
    
    public String getFirebaseProjectId() {
        return firebaseProjectId;
    }
    
    public void setFirebaseProjectId(String firebaseProjectId) {
        this.firebaseProjectId = firebaseProjectId;
    }
    
    public String getFirebaseCredentialsPath() {
        return firebaseCredentialsPath;
    }
    
    public void setFirebaseCredentialsPath(String firebaseCredentialsPath) {
        this.firebaseCredentialsPath = firebaseCredentialsPath;
    }
    
    public String getDeviceToken() {
        return deviceToken;
    }
    
    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
    
    public String getDataDirectory() {
        return dataDirectory;
    }
    
    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }
    
    public String getLogsDirectory() {
        return logsDirectory;
    }
    
    public void setLogsDirectory(String logsDirectory) {
        this.logsDirectory = logsDirectory;
    }
}

