package com.secureview.desktop.opencv.stub;

import com.secureview.desktop.opencv.stub.Mat;

/**
 * Stub class for OpenCV Core.
 */
public class Core {
    public static final String NATIVE_LIBRARY_NAME = "opencv_java480";
    
    public static void absdiff(Mat src1, Mat src2, Mat dst) {}
    public static Scalar mean(Mat src) { return new Scalar(); }
    public static void meanStdDev(Mat src, Object mean, Object stddev) {}
    public static void magnitude(Mat x, Mat y, Mat magnitude) {}
}

