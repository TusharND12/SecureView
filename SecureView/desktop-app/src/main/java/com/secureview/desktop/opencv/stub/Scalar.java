package com.secureview.desktop.opencv.stub;

/**
 * Stub class for OpenCV Scalar.
 */
public class Scalar {
    public double[] val = new double[4];
    
    public Scalar() {
        val[0] = 0; val[1] = 0; val[2] = 0; val[3] = 0;
    }
    
    public Scalar(double v0) { 
        val[0] = v0; val[1] = 0; val[2] = 0; val[3] = 0;
    }
    
    public Scalar(double v0, double v1, double v2) {
        val[0] = v0; val[1] = v1; val[2] = v2; val[3] = 0;
    }
    
    public Scalar(double v0, double v1, double v2, double v3) {
        val[0] = v0; val[1] = v1; val[2] = v2; val[3] = v3;
    }
    
    /**
     * Constructor to wrap a real OpenCV Scalar instance.
     */
    public Scalar(Object realScalarInstance) {
        if (realScalarInstance != null) {
            try {
                java.lang.reflect.Field valField = realScalarInstance.getClass().getField("val");
                double[] realVal = (double[]) valField.get(realScalarInstance);
                if (realVal != null && realVal.length >= 4) {
                    val = realVal.clone();
                }
            } catch (Exception e) {
                // Fall back to default
                val = new double[4];
            }
        }
    }
}

