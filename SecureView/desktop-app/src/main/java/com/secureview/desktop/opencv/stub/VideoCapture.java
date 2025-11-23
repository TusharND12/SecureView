package com.secureview.desktop.opencv.stub;

import com.secureview.desktop.opencv.stub.Mat;

/**
 * Stub class for OpenCV VideoCapture.
 */
public class VideoCapture {
    public VideoCapture(int index) {}
    public boolean isOpened() { return false; }
    public boolean read(Mat frame) { return false; }
    public void set(int prop, double value) {}
    public void release() {}
}

