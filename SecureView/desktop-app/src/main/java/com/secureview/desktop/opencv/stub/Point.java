package com.secureview.desktop.opencv.stub;

import java.lang.reflect.Constructor;

/**
 * Stub class for OpenCV Point.
 */
public class Point {
    public double x;
    public double y;
    private Object realPoint;
    
    public Point() {
        this(0, 0);
    }
    
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
        try {
            Class<?> pointClass = Class.forName("org.opencv.core.Point");
            Constructor<?> constructor = pointClass.getDeclaredConstructor(double.class, double.class);
            this.realPoint = constructor.newInstance(x, y);
        } catch (Exception e) {
            // Real OpenCV not available
            this.realPoint = null;
        }
    }
    
    public Point(Object realPointInstance) {
        if (realPointInstance != null) {
            try {
                String className = realPointInstance.getClass().getName();
                if (className.equals("org.opencv.core.Point")) {
                    this.realPoint = realPointInstance;
                    // Extract x, y from real point
                    Class<?> pointClass = realPointInstance.getClass();
                    this.x = ((Number) pointClass.getField("x").get(realPointInstance)).doubleValue();
                    this.y = ((Number) pointClass.getField("y").get(realPointInstance)).doubleValue();
                }
            } catch (Exception e) {
                this.realPoint = null;
            }
        }
    }
    
    public Object getRealInstance() {
        return realPoint;
    }
    
    @Override
    public String toString() {
        return "Point(" + x + ", " + y + ")";
    }
}

