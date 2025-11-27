package com.secureview.desktop.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Manages application configuration including thresholds, paths, and settings.
 */
public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static ConfigManager instance;
    
    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".secureview";
    private static final String CONFIG_FILE = CONFIG_DIR + File.separator + "config.json";
    
    private ApplicationConfig config;
    private final Gson gson;
    
    private ConfigManager() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        config = new ApplicationConfig();
    }
    
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    public void loadConfiguration() throws IOException {
        File configFile = new File(CONFIG_FILE);
        
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                config = gson.fromJson(reader, ApplicationConfig.class);
                logger.info("Configuration loaded from: {}", CONFIG_FILE);
                
                // Upgrade old configs with low thresholds to stricter defaults
                if (config.getFaceRecognitionThreshold() < 0.7) {
                    logger.info("Upgrading face recognition threshold from {} to 0.75 for better security", 
                               config.getFaceRecognitionThreshold());
                    config.setFaceRecognitionThreshold(0.75);
                    saveConfiguration();
                }
                
                // Upgrade old configs with low max failed attempts to 15
                if (config.getMaxFailedAttempts() < 15) {
                    logger.info("Upgrading max failed attempts from {} to 15", 
                               config.getMaxFailedAttempts());
                    config.setMaxFailedAttempts(15);
                    saveConfiguration();
                }
            }
        } else {
            // Create default configuration
            createDefaultConfig();
            saveConfiguration();
            logger.info("Created default configuration");
        }
        
        // Ensure directories exist
        ensureDirectoriesExist();
    }
    
    public void saveConfiguration() throws IOException {
        File configFile = new File(CONFIG_FILE);
        configFile.getParentFile().mkdirs();
        
        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(config, writer);
            logger.info("Configuration saved to: {}", CONFIG_FILE);
        }
    }
    
    private void createDefaultConfig() {
        config = new ApplicationConfig();
        config.setFaceRecognitionThreshold(0.75); // Stricter threshold to prevent false acceptances
        config.setLivenessDetectionEnabled(true);
        config.setMaxFailedAttempts(15); // 15 attempts before sending alert email
        config.setLockoutDuration(300000); // 5 minutes
        config.setFirebaseProjectId("");
        config.setFirebaseCredentialsPath("");
        config.setDeviceToken("");
        config.setDataDirectory(CONFIG_DIR + File.separator + "data");
        config.setLogsDirectory(CONFIG_DIR + File.separator + "logs");

        // Email defaults (disabled by default; user must configure)
        config.setSmtpHost("");
        config.setSmtpPort(587);
        config.setSmtpUsername("");
        config.setSmtpPassword("");
        config.setSmtpUseTls(true);
        config.setAlertEmailFrom("");
        config.setAlertEmailTo("");
    }
    
    private void ensureDirectoriesExist() throws IOException {
        Files.createDirectories(Paths.get(config.getDataDirectory()));
        Files.createDirectories(Paths.get(config.getLogsDirectory()));
        // Create registered_faces directory
        String registeredFacesDir = config.getDataDirectory() + File.separator + "registered_faces";
        Files.createDirectories(Paths.get(registeredFacesDir));
        logger.info("Created registered_faces directory at: {}", registeredFacesDir);
    }
    
    public ApplicationConfig getConfig() {
        return config;
    }
    
    public void updateConfig(ApplicationConfig newConfig) throws IOException {
        this.config = newConfig;
        saveConfiguration();
    }
}

