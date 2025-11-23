package com.secureview.desktop.opencv.stub;

/**
 * Stub class for OpenCV CascadeClassifier.
 */
public class CascadeClassifier {
    public CascadeClassifier() {}
    public boolean load(String filename) { return false; }
    public void detectMultiScale(Mat image, Object faces, double scaleFactor, 
                                int minNeighbors, int flags, Object minSize, Object maxSize) {}
}

