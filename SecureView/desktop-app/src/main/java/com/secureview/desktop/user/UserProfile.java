package com.secureview.desktop.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user profile with face data and metadata.
 */
public class UserProfile {
    private String userId;
    private String username;
    private UserRole role;
    private LocalDateTime registrationDate;
    private LocalDateTime lastLoginDate;
    private int successfulAuthentications;
    private int failedAuthentications;
    private double averageConfidence;
    private List<String> faceImagePaths;
    private boolean isActive;
    private boolean isGuest;
    
    public enum UserRole {
        ADMIN,
        STANDARD,
        GUEST
    }
    
    public UserProfile() {
        this.faceImagePaths = new ArrayList<>();
        this.isActive = true;
        this.isGuest = false;
        this.role = UserRole.STANDARD;
        this.registrationDate = LocalDateTime.now();
        this.successfulAuthentications = 0;
        this.failedAuthentications = 0;
        this.averageConfidence = 0.0;
    }
    
    public UserProfile(String userId, String username) {
        this();
        this.userId = userId;
        this.username = username;
    }
    
    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    
    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }
    
    public LocalDateTime getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(LocalDateTime lastLoginDate) { this.lastLoginDate = lastLoginDate; }
    
    public int getSuccessfulAuthentications() { return successfulAuthentications; }
    public void setSuccessfulAuthentications(int successfulAuthentications) { this.successfulAuthentications = successfulAuthentications; }
    
    public int getFailedAuthentications() { return failedAuthentications; }
    public void setFailedAuthentications(int failedAuthentications) { this.failedAuthentications = failedAuthentications; }
    
    public double getAverageConfidence() { return averageConfidence; }
    public void setAverageConfidence(double averageConfidence) { this.averageConfidence = averageConfidence; }
    
    public List<String> getFaceImagePaths() { return faceImagePaths; }
    public void setFaceImagePaths(List<String> faceImagePaths) { this.faceImagePaths = faceImagePaths; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public boolean isGuest() { return isGuest; }
    public void setGuest(boolean guest) { isGuest = guest; }
    
    /**
     * Updates confidence score with new authentication result.
     */
    public void updateConfidence(double newConfidence) {
        int total = successfulAuthentications + failedAuthentications;
        if (total == 0) {
            averageConfidence = newConfidence;
        } else {
            averageConfidence = (averageConfidence * total + newConfidence) / (total + 1);
        }
    }
    
    /**
     * Records a successful authentication.
     */
    public void recordSuccess(double confidence) {
        successfulAuthentications++;
        updateConfidence(confidence);
        lastLoginDate = LocalDateTime.now();
    }
    
    /**
     * Records a failed authentication.
     */
    public void recordFailure() {
        failedAuthentications++;
    }
}



