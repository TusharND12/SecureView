package com.secureview.desktop.opencv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Class loader to load OpenCV classes from the installation directory.
 */
public class OpenCVClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(OpenCVClassLoader.class);
    private static URLClassLoader opencvClassLoader = null;
    
    /**
     * Creates a class loader for OpenCV JAR.
     * @return URLClassLoader for OpenCV classes, or null if not found
     */
    public static URLClassLoader getOpenCVClassLoader() {
        if (opencvClassLoader != null) {
            return opencvClassLoader;
        }
        
        try {
            String opencvDir = System.getenv("OPENCV_DIR");
            if (opencvDir == null || opencvDir.isEmpty()) {
                // Try common paths
                String[] commonPaths = {
                    "C:\\opencv",
                    "C:\\Program Files\\opencv",
                    "C:\\opencv4120"
                };
                
                for (String path : commonPaths) {
                    File jarFile = new File(path + File.separator + "build" + 
                                          File.separator + "java" + 
                                          File.separator + "opencv-4120.jar");
                    if (jarFile.exists()) {
                        opencvDir = path;
                        break;
                    }
                }
            }
            
            if (opencvDir != null && !opencvDir.isEmpty()) {
                File jarFile = new File(opencvDir + File.separator + "build" + 
                                      File.separator + "java" + 
                                      File.separator + "opencv-4120.jar");
                
                if (jarFile.exists()) {
                    URL[] urls = {jarFile.toURI().toURL()};
                    opencvClassLoader = new URLClassLoader(urls, 
                        OpenCVClassLoader.class.getClassLoader());
                    logger.info("OpenCV JAR loaded from: {}", jarFile.getAbsolutePath());
                    return opencvClassLoader;
                } else {
                    logger.warn("OpenCV JAR not found at: {}", jarFile.getAbsolutePath());
                }
            } else {
                logger.warn("OPENCV_DIR not set and OpenCV not found in common paths");
            }
        } catch (Exception e) {
            logger.error("Failed to create OpenCV class loader", e);
        }
        
        return null;
    }
}

