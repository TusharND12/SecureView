package com.secureview.desktop.autostart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Manages auto-start functionality for Windows.
 * Creates a startup entry in the Windows registry or startup folder.
 */
public class AutoStartManager {
    private static final Logger logger = LoggerFactory.getLogger(AutoStartManager.class);
    
    private static final String APP_NAME = "SecureView";
    private static final String STARTUP_FOLDER = System.getProperty("user.home") + 
        File.separator + "AppData" + File.separator + "Roaming" + 
        File.separator + "Microsoft" + File.separator + "Windows" + 
        File.separator + "Start Menu" + File.separator + "Programs" + 
        File.separator + "Startup";
    
    /**
     * Enables auto-start by creating a shortcut in the Windows startup folder.
     */
    public static void enableAutoStart(String jarPath) throws IOException {
        logger.info("Enabling auto-start...");
        
        // Create VBScript to create shortcut
        String vbsScript = createVBScript(jarPath);
        File vbsFile = File.createTempFile("secureview_autostart", ".vbs");
        
        try (FileWriter writer = new FileWriter(vbsFile)) {
            writer.write(vbsScript);
        }
        
        // Execute VBScript
        try {
            Process process = Runtime.getRuntime().exec(
                "wscript.exe " + vbsFile.getAbsolutePath());
            process.waitFor();
            
            if (process.exitValue() == 0) {
                logger.info("Auto-start enabled successfully");
            } else {
                throw new IOException("Failed to create startup shortcut");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while enabling auto-start", e);
        } finally {
            vbsFile.delete();
        }
    }
    
    /**
     * Disables auto-start by removing the shortcut.
     */
    public static void disableAutoStart() throws IOException {
        logger.info("Disabling auto-start...");
        
        File shortcutFile = new File(STARTUP_FOLDER, APP_NAME + ".lnk");
        if (shortcutFile.exists()) {
            if (shortcutFile.delete()) {
                logger.info("Auto-start disabled successfully");
            } else {
                throw new IOException("Failed to delete startup shortcut");
            }
        } else {
            logger.info("Startup shortcut not found");
        }
    }
    
    /**
     * Checks if auto-start is enabled.
     */
    public static boolean isAutoStartEnabled() {
        File shortcutFile = new File(STARTUP_FOLDER, APP_NAME + ".lnk");
        return shortcutFile.exists();
    }
    
    /**
     * Creates VBScript to create a Windows shortcut.
     */
    private static String createVBScript(String jarPath) {
        return String.format(
            "Set oWS = WScript.CreateObject(\"WScript.Shell\")\n" +
            "sLinkFile = \"%s\\%s.lnk\"\n" +
            "Set oLink = oWS.CreateShortcut(sLinkFile)\n" +
            "oLink.TargetPath = \"java\"\n" +
            "oLink.Arguments = \"-jar \"\"%s\"\"\"\n" +
            "oLink.WorkingDirectory = \"%s\"\n" +
            "oLink.Description = \"SecureView Authentication System\"\n" +
            "oLink.Save",
            STARTUP_FOLDER.replace("\\", "\\\\"),
            APP_NAME,
            jarPath.replace("\\", "\\\\"),
            new File(jarPath).getParent().replace("\\", "\\\\")
        );
    }
    
    /**
     * Alternative method using registry (requires admin privileges).
     */
    public static void enableAutoStartRegistry(String jarPath) throws IOException {
        logger.info("Enabling auto-start via registry...");
        
        String keyPath = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
        String valueName = APP_NAME;
        String value = "java -jar \"" + jarPath + "\"";
        
        try {
            Process process = Runtime.getRuntime().exec(
                "reg add \"" + keyPath + "\" /v \"" + valueName + "\" /t REG_SZ /d \"" + value + "\" /f");
            process.waitFor();
            
            if (process.exitValue() == 0) {
                logger.info("Auto-start enabled via registry");
            } else {
                throw new IOException("Failed to add registry entry");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while enabling auto-start", e);
        }
    }
}

