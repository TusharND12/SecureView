package com.secureview.desktop.opencv.stub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Stub class for OpenCV Core.
 * Uses reflection to call real OpenCV methods if available.
 */
public class Core {
    private static final Logger logger = LoggerFactory.getLogger(Core.class);
    public static final String NATIVE_LIBRARY_NAME = "opencv_java480";
    public static final int NORM_MINMAX = 32;
    
    private static Class<?> realCoreClass;
    private static Method normalizeMethod;
    private static Method minMaxLocMethod;
    private static Method subtractMethod;
    private static Method meanMethod;
    
    static {
        try {
            realCoreClass = Class.forName("org.opencv.core.Core");
            normalizeMethod = realCoreClass.getMethod("normalize",
                Class.forName("org.opencv.core.Mat"),
                Class.forName("org.opencv.core.Mat"),
                double.class, double.class, int.class);
            // minMaxLoc has multiple overloads, try the one with Point parameters
            try {
                minMaxLocMethod = realCoreClass.getMethod("minMaxLoc",
                    Class.forName("org.opencv.core.Mat"),
                    double[].class, double[].class,
                    Class.forName("org.opencv.core.Point"),
                    Class.forName("org.opencv.core.Point"));
            } catch (NoSuchMethodException e) {
                // Try without Point parameters
                minMaxLocMethod = realCoreClass.getMethod("minMaxLoc",
                    Class.forName("org.opencv.core.Mat"),
                    double[].class, double[].class);
            }
            subtractMethod = realCoreClass.getMethod("subtract",
                Class.forName("org.opencv.core.Mat"),
                Class.forName("org.opencv.core.Scalar"),
                Class.forName("org.opencv.core.Mat"));
            meanMethod = realCoreClass.getMethod("mean",
                Class.forName("org.opencv.core.Mat"));
        } catch (Exception e) {
            logger.debug("OpenCV Core class not found. Some features will be limited.", e);
        }
    }
    
    public static void absdiff(Mat src1, Mat src2, Mat dst) {}
    
    public static Scalar mean(Mat src) {
        if (meanMethod != null && src.getRealInstance() != null) {
            try {
                Object realScalar = meanMethod.invoke(null, src.getRealInstance());
                return new Scalar(realScalar);
            } catch (Exception e) {
                logger.debug("Failed to calculate mean", e);
            }
        }
        return new Scalar();
    }
    
    public static void meanStdDev(Mat src, Object mean, Object stddev) {}
    public static void magnitude(Mat x, Mat y, Mat magnitude) {}
    
    public static void normalize(Mat src, Mat dst, double alpha, double beta, int normType) {
        if (normalizeMethod != null && src.getRealInstance() != null && dst.getRealInstance() != null) {
            try {
                normalizeMethod.invoke(null, src.getRealInstance(), dst.getRealInstance(), alpha, beta, normType);
            } catch (Exception e) {
                logger.debug("Failed to normalize", e);
            }
        }
    }
    
    public static void minMaxLoc(Mat src, double[] minVal, double[] maxVal) {
        if (minMaxLocMethod != null && src.getRealInstance() != null) {
            try {
                // Try with Point parameters first, fall back to without
                try {
                    Class<?> pointClass = Class.forName("org.opencv.core.Point");
                    Object minLoc = pointClass.getDeclaredConstructor().newInstance();
                    Object maxLoc = pointClass.getDeclaredConstructor().newInstance();
                    minMaxLocMethod.invoke(null, src.getRealInstance(), minVal, maxVal, minLoc, maxLoc);
                } catch (Exception e) {
                    // Try without Point parameters
                    minMaxLocMethod.invoke(null, src.getRealInstance(), minVal, maxVal);
                }
            } catch (Exception e) {
                logger.debug("Failed to find min/max", e);
            }
        }
    }
    
    public static void subtract(Mat src1, Scalar src2, Mat dst) {
        if (subtractMethod != null && src1.getRealInstance() != null && dst.getRealInstance() != null) {
            try {
                Class<?> scalarClass = Class.forName("org.opencv.core.Scalar");
                Object realScalar = scalarClass.getDeclaredConstructor(double.class, double.class, double.class, double.class)
                    .newInstance(src2.val[0], src2.val[1], src2.val[2], src2.val[3]);
                subtractMethod.invoke(null, src1.getRealInstance(), realScalar, dst.getRealInstance());
            } catch (Exception e) {
                logger.debug("Failed to subtract", e);
            }
        }
    }
}

