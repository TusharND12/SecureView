package com.secureview.desktop.opencv.stub;

import com.secureview.desktop.opencv.stub.Mat;
import com.secureview.desktop.opencv.OpenCVClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Stub class for OpenCV Imgcodecs.
 * Uses reflection to call real OpenCV methods if available.
 */
public class Imgcodecs {
    private static final Logger logger = LoggerFactory.getLogger(Imgcodecs.class);
    
    public static boolean imwrite(String filename, Mat img) {
        if (img != null && img.getRealInstance() != null) {
            try {
                Class<?> imgcodecsClass = Class.forName("org.opencv.imgcodecs.Imgcodecs");
                Method imwriteMethod = imgcodecsClass.getMethod("imwrite", String.class, 
                    Class.forName("org.opencv.core.Mat"));
                Object realMat = img.getRealInstance();
                if (realMat != null) {
                    return (Boolean) imwriteMethod.invoke(null, filename, realMat);
                }
            } catch (Exception e) {
                logger.error("Error calling real Imgcodecs.imwrite()", e);
            }
        }
        return false;
    }
    
    public static Mat imread(String filename) {
        try {
            Class<?> imgcodecsClass = Class.forName("org.opencv.imgcodecs.Imgcodecs");
            Method imreadMethod = imgcodecsClass.getMethod("imread", String.class);
            Object realMat = imreadMethod.invoke(null, filename);
            if (realMat != null) {
                // Check if Mat is not empty
                Method emptyMethod = realMat.getClass().getMethod("empty");
                boolean isEmpty = (Boolean) emptyMethod.invoke(realMat);
                if (!isEmpty) {
                    return new Mat(realMat);
                }
            }
        } catch (ClassNotFoundException e) {
            logger.debug("OpenCV not loaded, cannot read image");
        } catch (Exception e) {
            logger.error("Error calling real Imgcodecs.imread()", e);
        }
        return new Mat(); // Return empty Mat if failed
    }
}

