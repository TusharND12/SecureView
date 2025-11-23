package com.secureview.desktop.opencv.stub;

import com.secureview.desktop.opencv.stub.Mat;

/**
 * Stub class for OpenCV Imgproc.
 */
public class Imgproc {
    public static final int COLOR_BGR2GRAY = 6;
    public static final int CV_8UC1 = 0;
    public static final int CV_64F = 6;
    
    public static void cvtColor(Mat src, Mat dst, int code) {}
    public static void equalizeHist(Mat src, Mat dst) {}
    public static void resize(Mat src, Mat dst, Object size) {}
    public static void Sobel(Mat src, Mat dst, int ddepth, int dx, int dy, int ksize) {}
    public static void Sobel(Mat src, Mat dst, int ddepth, int dx, int dy, int ksize, double scale, double delta) {}
    public static void calcHist(java.util.List<Mat> images, Object channels, Mat mask, 
                               Mat hist, Object histSize, Object ranges) {}
}

