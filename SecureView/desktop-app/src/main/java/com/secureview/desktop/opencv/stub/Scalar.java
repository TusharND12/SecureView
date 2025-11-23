package com.secureview.desktop.opencv.stub;

/**
 * Stub class for OpenCV Scalar.
 */
public class Scalar {
    public double[] val = new double[4];
    public Scalar() {}
    public Scalar(double v0) { val[0] = v0; }
    public Scalar(double v0, double v1, double v2) {
        val[0] = v0; val[1] = v1; val[2] = v2;
    }
    public Scalar(double v0, double v1, double v2, double v3) {
        val[0] = v0; val[1] = v1; val[2] = v2; val[3] = v3;
    }
}

