package com.secureview.desktop.opencv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class to load OpenCV native library dynamically.
 * Supports OpenCV 4.12.0 installation.
 */
public class OpenCVLoader {
    private static final Logger logger = LoggerFactory.getLogger(OpenCVLoader.class);
    private static boolean loaded = false;
    
    /**
     * Attempts to load OpenCV native library.
     * @return true if loaded successfully, false otherwise
     */
    public static boolean loadLibrary() {
        if (loaded) {
            return true;
        }
        
        try {
            // Try to load OpenCV Core class
            Class<?> coreClass = null;
            String nativeLibraryName = null;
            
            try {
                coreClass = Class.forName("org.opencv.core.Core");
                nativeLibraryName = (String) coreClass.getField("NATIVE_LIBRARY_NAME").get(null);
            } catch (ClassNotFoundException e) {
                // If OpenCV classes not in classpath, try common library names
                String[] possibleNames = {
                    "opencv_java4120",  // OpenCV 4.12.0
                    "opencv_java480",   // OpenCV 4.8.0
                    "opencv_java460",   // OpenCV 4.6.0
                    "opencv_java"       // Generic
                };
                
                // Try to find which one exists
                String opencvDir = System.getenv("OPENCV_DIR");
                if (opencvDir == null || opencvDir.isEmpty()) {
                    opencvDir = "C:\\Users\\TUSHAR\\Downloads\\opencv";
                }
                
                String arch = System.getProperty("os.arch").contains("64") ? "x64" : "x86";
                String dllDir = opencvDir + File.separator + "build" + File.separator + 
                               "java" + File.separator + arch;
                
                for (String name : possibleNames) {
                    Path dllPath = Paths.get(dllDir, name + ".dll");
                    if (Files.exists(dllPath)) {
                        nativeLibraryName = name;
                        logger.info("Found OpenCV DLL: {}", dllPath);
                        break;
                    }
                }
                
                if (nativeLibraryName == null) {
                    throw new ClassNotFoundException("OpenCV classes not found and no DLL detected");
                }
            }
            
            // Try multiple methods to load the native library
            boolean loaded = false;
            
            // Method 1: Try loading from system PATH (if OpenCV is in PATH)
            try {
                System.loadLibrary(nativeLibraryName);
                loaded = true;
                logger.info("OpenCV native library loaded from system PATH");
            } catch (UnsatisfiedLinkError e1) {
                // Method 2: Try loading from OPENCV_DIR environment variable
                String opencvDir = System.getenv("OPENCV_DIR");
                if (opencvDir != null && !opencvDir.isEmpty()) {
                    try {
                        // For Windows: x64 or x86
                        String arch = System.getProperty("os.arch").contains("64") ? "x64" : "x86";
                        String libPath = opencvDir + File.separator + "build" + File.separator + 
                                       "java" + File.separator + arch + File.separator + 
                                       nativeLibraryName + ".dll";
                        
                        Path libFile = Paths.get(libPath);
                        if (Files.exists(libFile)) {
                            System.load(libPath);
                            loaded = true;
                            logger.info("OpenCV native library loaded from: {}", libPath);
                        } else {
                            // Try alternative path (without arch subdirectory)
                            libPath = opencvDir + File.separator + "build" + File.separator + 
                                     "java" + File.separator + nativeLibraryName + ".dll";
                            libFile = Paths.get(libPath);
                            if (Files.exists(libFile)) {
                                System.load(libPath);
                                loaded = true;
                                logger.info("OpenCV native library loaded from: {}", libPath);
                            }
                        }
                    } catch (Exception e2) {
                        logger.debug("Failed to load from OPENCV_DIR: {}", e2.getMessage());
                    }
                }
                
                // Method 3: Try common installation paths
                if (!loaded) {
                    String[] commonBasePaths = {
                        "C:\\Users\\TUSHAR\\Downloads\\opencv",  // User's installation
                        "C:\\opencv",
                        "C:\\opencv4120",
                        "C:\\opencv-4.12.0",
                        "C:\\Program Files\\opencv",
                        "C:\\Program Files (x86)\\opencv"
                    };
                    
                    String arch = System.getProperty("os.arch").contains("64") ? "x64" : "x86";
                    
                    for (String basePath : commonBasePaths) {
                        // Try x64/x86 subdirectory first
                        String[] tryPaths = {
                            basePath + "\\build\\java\\" + arch + "\\" + nativeLibraryName + ".dll",
                            basePath + "\\build\\java\\" + nativeLibraryName + ".dll"
                        };
                        
                        for (String path : tryPaths) {
                            Path libFile = Paths.get(path);
                            if (Files.exists(libFile)) {
                                try {
                                    System.load(path);
                                    loaded = true;
                                    logger.info("OpenCV native library loaded from: {}", path);
                                    // Also set OPENCV_DIR for future use
                                    System.setProperty("OPENCV_DIR", basePath);
                                    break;
                                } catch (Exception e3) {
                                    logger.debug("Failed to load from {}: {}", path, e3.getMessage());
                                }
                            }
                        }
                        
                        if (loaded) break;
                    }
                }
                
                if (!loaded) {
                    throw new UnsatisfiedLinkError("Could not load OpenCV native library. " +
                        "Please ensure OpenCV 4.12.0 is installed and OPENCV_DIR is set correctly.");
                }
            }
            
            OpenCVLoader.loaded = true;
            logger.info("OpenCV {} loaded successfully", nativeLibraryName);
            return true;
            
        } catch (ClassNotFoundException e) {
            logger.error("OpenCV classes not found. Please ensure OpenCV JAR is in the classpath.");
            logger.error("Install OpenCV 4.12.0 from https://opencv.org/releases/");
            return false;
        } catch (UnsatisfiedLinkError e) {
            logger.error("Failed to load OpenCV native library: {}", e.getMessage());
            logger.error("Please ensure:");
            logger.error("1. OpenCV 4.12.0 is installed");
            logger.error("2. OPENCV_DIR environment variable is set to your OpenCV installation directory");
            logger.error("3. The native library (opencv_java4120.dll) is accessible");
            return false;
        } catch (Exception e) {
            logger.error("Failed to load OpenCV", e);
            return false;
        }
    }
    
    public static boolean isLoaded() {
        return loaded;
    }
}

