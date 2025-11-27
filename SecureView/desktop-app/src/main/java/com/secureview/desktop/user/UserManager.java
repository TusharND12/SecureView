package com.secureview.desktop.user;

import com.secureview.desktop.config.ConfigManager;
import com.secureview.desktop.encryption.EncryptionService;
import com.secureview.desktop.opencv.stub.Imgcodecs;
import com.secureview.desktop.opencv.stub.Mat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages multiple user profiles and user switching.
 */
public class UserManager {
    private static final Logger logger = LoggerFactory.getLogger(UserManager.class);
    private static UserManager instance;
    
    private ConfigManager configManager;
    private Map<String, UserProfile> users;
    private UserProfile currentUser;
    private static final String USERS_FILE = "users.json";
    private static final String USER_DATA_DIR = "users";
    
    private UserManager() {
        configManager = ConfigManager.getInstance();
        users = new HashMap<>();
    }
    
    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }
    
    /**
     * Initializes user manager and loads existing users.
     */
    public void initialize() throws Exception {
        logger.info("Initializing User Manager...");
        loadUsers();
        logger.info("User Manager initialized. Found {} users.", users.size());
    }
    
    /**
     * Loads users from disk.
     */
    private void loadUsers() {
        try {
            String dataDir = configManager.getConfig().getDataDirectory();
            File usersFile = new File(dataDir, USERS_FILE);
            
            if (usersFile.exists()) {
                Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(LocalDateTime.class, new com.google.gson.JsonDeserializer<LocalDateTime>() {
                        @Override
                        public LocalDateTime deserialize(com.google.gson.JsonElement json, 
                                java.lang.reflect.Type type, 
                                com.google.gson.JsonDeserializationContext context) {
                            return LocalDateTime.parse(json.getAsString());
                        }
                    })
                    .registerTypeAdapter(LocalDateTime.class, new com.google.gson.JsonSerializer<LocalDateTime>() {
                        @Override
                        public com.google.gson.JsonElement serialize(LocalDateTime src, 
                                java.lang.reflect.Type type, 
                                com.google.gson.JsonSerializationContext context) {
                            return new com.google.gson.JsonPrimitive(src.toString());
                        }
                    })
                    .create();
                
                try (FileReader reader = new FileReader(usersFile)) {
                    Type type = new TypeToken<Map<String, UserProfile>>(){}.getType();
                    users = gson.fromJson(reader, type);
                    if (users == null) {
                        users = new HashMap<>();
                    }
                }
                logger.info("Loaded {} users from disk", users.size());
            } else {
                logger.info("No users file found. Starting with empty user list.");
            }
        } catch (Exception e) {
            logger.error("Error loading users", e);
            users = new HashMap<>();
        }
    }
    
    /**
     * Saves users to disk.
     */
    private void saveUsers() {
        try {
            String dataDir = configManager.getConfig().getDataDirectory();
            Files.createDirectories(Paths.get(dataDir));
            File usersFile = new File(dataDir, USERS_FILE);
            
            Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new com.google.gson.JsonSerializer<LocalDateTime>() {
                    @Override
                    public com.google.gson.JsonElement serialize(LocalDateTime src, 
                            java.lang.reflect.Type type, 
                            com.google.gson.JsonSerializationContext context) {
                        return new com.google.gson.JsonPrimitive(src.toString());
                    }
                })
                .create();
            
            try (FileWriter writer = new FileWriter(usersFile)) {
                gson.toJson(users, writer);
            }
            logger.debug("Saved {} users to disk", users.size());
        } catch (Exception e) {
            logger.error("Error saving users", e);
        }
    }
    
    /**
     * Creates a new user profile.
     */
    public UserProfile createUser(String username, UserProfile.UserRole role) {
        String userId = UUID.randomUUID().toString();
        UserProfile user = new UserProfile(userId, username);
        user.setRole(role);
        users.put(userId, user);
        saveUsers();
        logger.info("Created new user: {} (ID: {})", username, userId);
        return user;
    }
    
    /**
     * Creates a guest user.
     */
    public UserProfile createGuestUser() {
        String guestId = "guest_" + System.currentTimeMillis();
        UserProfile guest = new UserProfile(guestId, "Guest");
        guest.setGuest(true);
        guest.setRole(UserProfile.UserRole.GUEST);
        users.put(guestId, guest);
        saveUsers();
        logger.info("Created guest user: {}", guestId);
        return guest;
    }
    
    /**
     * Registers face images for a user.
     */
    public boolean registerUserFaces(UserProfile user, List<Mat> faceImages) throws Exception {
        String userDataDir = getUserDataDirectory(user.getUserId());
        Files.createDirectories(Paths.get(userDataDir));
        
        // Clear existing images
        File[] existingFiles = new File(userDataDir).listFiles((d, name) -> 
            name.toLowerCase().endsWith(".jpg") || 
            name.toLowerCase().endsWith(".jpeg") || 
            name.toLowerCase().endsWith(".png"));
        if (existingFiles != null) {
            for (File file : existingFiles) {
                file.delete();
            }
        }
        
        // Save new images
        List<String> imagePaths = new ArrayList<>();
        for (int i = 0; i < faceImages.size(); i++) {
            String imagePath = userDataDir + File.separator + String.format("face_angle_%03d.jpg", i + 1);
            boolean saved = Imgcodecs.imwrite(imagePath, faceImages.get(i));
            if (saved) {
                imagePaths.add(imagePath);
                logger.debug("Saved face image {} for user {}", i + 1, user.getUsername());
            }
        }
        
        user.setFaceImagePaths(imagePaths);
        saveUsers();
        logger.info("Registered {} face images for user {}", imagePaths.size(), user.getUsername());
        return !imagePaths.isEmpty();
    }
    
    /**
     * Gets user data directory path.
     */
    private String getUserDataDirectory(String userId) {
        String dataDir = configManager.getConfig().getDataDirectory();
        return dataDir + File.separator + USER_DATA_DIR + File.separator + userId;
    }
    
    /**
     * Finds user by face recognition (returns best match).
     */
    public UserProfile findUserByFace(Mat faceImage, com.secureview.desktop.face.comparison.ImageComparisonService comparisonService) {
        UserProfile bestMatch = null;
        double bestScore = 0.0;
        double threshold = 0.6; // Minimum similarity threshold
        
        for (UserProfile user : users.values()) {
            if (!user.isActive() || user.getFaceImagePaths().isEmpty()) {
                continue;
            }
            
            try {
                // Load user's reference images
                List<Mat> referenceImages = new ArrayList<>();
                for (String imagePath : user.getFaceImagePaths()) {
                    Mat img = Imgcodecs.imread(imagePath);
                    if (img != null && !img.empty()) {
                        referenceImages.add(img);
                    }
                }
                
                if (!referenceImages.isEmpty()) {
                    double similarity = comparisonService.compareWithImages(faceImage, referenceImages);
                    
                    // Cleanup
                    for (Mat img : referenceImages) {
                        img.release();
                    }
                    
                    if (similarity > bestScore && similarity >= threshold) {
                        bestScore = similarity;
                        bestMatch = user;
                    }
                }
            } catch (Exception e) {
                logger.warn("Error comparing face with user {}", user.getUsername(), e);
            }
        }
        
        if (bestMatch != null) {
            logger.info("Found user match: {} (similarity: {})", bestMatch.getUsername(), bestScore);
        }
        
        return bestMatch;
    }
    
    /**
     * Sets the current active user.
     */
    public void setCurrentUser(UserProfile user) {
        this.currentUser = user;
        logger.info("Current user set to: {}", user != null ? user.getUsername() : "null");
    }
    
    /**
     * Gets the current active user.
     */
    public UserProfile getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Gets all users.
     */
    public List<UserProfile> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    /**
     * Gets active users only.
     */
    public List<UserProfile> getActiveUsers() {
        return users.values().stream()
            .filter(UserProfile::isActive)
            .collect(Collectors.toList());
    }
    
    /**
     * Deletes a user.
     */
    public boolean deleteUser(String userId) {
        UserProfile user = users.get(userId);
        if (user != null) {
            // Delete user's face images
            try {
                String userDataDir = getUserDataDirectory(userId);
                File dir = new File(userDataDir);
                if (dir.exists()) {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            file.delete();
                        }
                    }
                    dir.delete();
                }
            } catch (Exception e) {
                logger.warn("Error deleting user data directory", e);
            }
            
            users.remove(userId);
            if (currentUser != null && currentUser.getUserId().equals(userId)) {
                currentUser = null;
            }
            saveUsers();
            logger.info("Deleted user: {}", userId);
            return true;
        }
        return false;
    }
    
    /**
     * Checks if any users are registered.
     */
    public boolean hasUsers() {
        return !users.isEmpty();
    }
    
    /**
     * Gets user by ID.
     */
    public UserProfile getUserById(String userId) {
        return users.get(userId);
    }
}

