package com.secureview.desktop.opencv.stub;

/**
 * Stub class for OpenCV Mat.
 * At runtime, if OpenCV is available, the actual org.opencv.core.Mat will be used.
 */
public class Mat {
    // This is a stub - actual implementation will use reflection to load real OpenCV Mat
    public Mat() {} // Default constructor
    public Mat(Mat m, Rect r) {} // Constructor for submatrix
    public Mat(Size size, int type) {} // Constructor with size and type
    
    public boolean empty() { return true; }
    public void release() {}
    public int cols() { return 0; }
    public int rows() { return 0; }
    public int channels() { return 0; }
    public long total() { return 0; }
    public void get(int row, int col, byte[] data) {}
    public void get(int row, int col, double[] data) {}
    public double[] get(int row, int col) { return new double[1]; }
    public void put(int row, int col, byte[] data) {}
    public void put(int row, int col, int data) {}
    public void copyTo(Mat dst) {}
    public void convertTo(Mat dst, int type, double alpha) {}
    public void convertTo(Mat dst, int type, double alpha, double beta) {}
    public Size size() { return new Size(); }
}

