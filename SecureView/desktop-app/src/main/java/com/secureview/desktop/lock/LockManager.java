package com.secureview.desktop.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;

/**
 * Manages system locking functionality.
 * Can lock the system, trigger alarms, and perform remote actions.
 */
public class LockManager {
    private static final Logger logger = LoggerFactory.getLogger(LockManager.class);
    private static LockManager instance;
    
    private LockManager() {
    }
    
    public static synchronized LockManager getInstance() {
        if (instance == null) {
            instance = new LockManager();
        }
        return instance;
    }
    
    /**
     * Locks the system by simulating Windows lock screen shortcut.
     */
    public void lockSystem() {
        try {
            logger.info("Locking system...");
            
            // Windows: Win + L
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_WINDOWS);
            robot.keyPress(KeyEvent.VK_L);
            robot.keyRelease(KeyEvent.VK_L);
            robot.keyRelease(KeyEvent.VK_WINDOWS);
            
            logger.info("System locked successfully");
            
        } catch (AWTException e) {
            logger.error("Failed to lock system", e);
        }
    }
    
    /**
     * Triggers an alarm by playing a system sound and showing alert.
     */
    public void triggerAlarm() {
        logger.warn("ALARM TRIGGERED - Intrusion detected!");
        
        try {
            // Play system beep
            Toolkit.getDefaultToolkit().beep();
            
            // For Windows, you could also play a sound file
            // AudioSystem.getAudioInputStream(new File("alarm.wav"));
            
        } catch (Exception e) {
            logger.error("Failed to trigger alarm", e);
        }
    }
    
    /**
     * Performs a remote action based on command from mobile app.
     */
    public void performRemoteAction(String action) {
        logger.info("Performing remote action: {}", action);
        
        switch (action.toLowerCase()) {
            case "lock":
                lockSystem();
                break;
            case "alarm":
                triggerAlarm();
                break;
            case "lock_and_alarm":
                lockSystem();
                triggerAlarm();
                break;
            default:
                logger.warn("Unknown remote action: {}", action);
        }
    }
}

