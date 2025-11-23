package com.secureview.desktop.opencv.stub;

import com.secureview.desktop.opencv.stub.Mat;

/**
 * Stub class for OpenCV Dnn.
 */
public class Dnn {
    public static Mat blobFromImage(Mat image, double scalefactor, Object size, Object mean, 
                                   boolean swapRB, boolean crop) {
        return new Mat();
    }
    
    public static Net readNetFromONNX(String modelPath) { return new Net(); }
    public static Net readNetFromTensorflow(String modelPath) { return new Net(); }
    public static Net readNetFromTorch(String modelPath) { return new Net(); }
}

