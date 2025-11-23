package com.secureview.desktop.logging;

import com.secureview.desktop.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logs authentication attempts and intrusion events.
 */
public class AttemptLogger {
    private static final Logger logger = LoggerFactory.getLogger(AttemptLogger.class);
    private static AttemptLogger instance;
    
    private ConfigManager configManager;
    private static final String LOG_FILE = "attempts.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private AttemptLogger() {
        configManager = ConfigManager.getInstance();
    }
    
    public static synchronized AttemptLogger getInstance() {
        if (instance == null) {
            instance = new AttemptLogger();
        }
        return instance;
    }
    
    /**
     * Logs a successful authentication attempt.
     */
    public void logSuccess(String userId) {
        String logEntry = String.format("[%s] SUCCESS - User: %s\n",
            LocalDateTime.now().format(FORMATTER), userId);
        writeLog(logEntry);
        logger.info("Authentication successful for user: {}", userId);
    }
    
    /**
     * Logs a failed authentication attempt.
     */
    public void logFailure(String reason, double similarity) {
        String logEntry = String.format("[%s] FAILURE - Reason: %s, Similarity: %.3f\n",
            LocalDateTime.now().format(FORMATTER), reason, similarity);
        writeLog(logEntry);
        logger.warn("Authentication failed - Reason: {}, Similarity: {}", reason, similarity);
    }
    
    /**
     * Logs an intrusion event.
     */
    public void logIntrusion(String details, String imagePath) {
        String logEntry = String.format("[%s] INTRUSION - Details: %s, Image: %s\n",
            LocalDateTime.now().format(FORMATTER), details, imagePath);
        writeLog(logEntry);
        logger.error("INTRUSION DETECTED - Details: {}", details);
    }
    
    /**
     * Logs a lockout event.
     */
    public void logLockout(int failedAttempts) {
        String logEntry = String.format("[%s] LOCKOUT - Failed attempts: %d\n",
            LocalDateTime.now().format(FORMATTER), failedAttempts);
        writeLog(logEntry);
        logger.error("System locked out after {} failed attempts", failedAttempts);
    }
    
    /**
     * Writes log entry to file.
     */
    private void writeLog(String logEntry) {
        try {
            String logsDir = configManager.getConfig().getLogsDirectory();
            Files.createDirectories(Paths.get(logsDir));
            
            File logFile = new File(logsDir, LOG_FILE);
            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.write(logEntry);
            }
        } catch (IOException e) {
            logger.error("Failed to write log entry", e);
        }
    }
    
    /**
     * Gets the count of failed attempts in the last N minutes.
     */
    public int getFailedAttemptsCount(int minutes) {
        // This is a simplified implementation
        // In production, you would parse the log file and count recent failures
        return 0;
    }
}

