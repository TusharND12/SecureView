package com.secureview.desktop.opencv.stub;

import java.lang.reflect.Method;

/**
 * Stub class for OpenCV MatOfRect.
 * Uses reflection to work with real OpenCV if available.
 */
public class MatOfRect {
    private Object realMatOfRect;
    private Class<?> realClass;
    private Method toArrayMethod;
    
    public MatOfRect() {
        // Try to load real OpenCV MatOfRect
        try {
            realClass = Class.forName("org.opencv.core.MatOfRect");
            realMatOfRect = realClass.getDeclaredConstructor().newInstance();
            toArrayMethod = realClass.getMethod("toArray");
        } catch (Exception e) {
            // Real OpenCV not available
            realMatOfRect = null;
        }
    }
    
    public MatOfRect(Object realInstance) {
        // Constructor to wrap real OpenCV MatOfRect
        if (realInstance != null && realInstance.getClass().getName().equals("org.opencv.core.MatOfRect")) {
            realMatOfRect = realInstance;
            try {
                realClass = realInstance.getClass();
                toArrayMethod = realClass.getMethod("toArray");
            } catch (Exception e) {
                realMatOfRect = null;
            }
        }
    }
    
    public Rect[] toArray() {
        if (realMatOfRect != null && toArrayMethod != null) {
            try {
                Object[] rectArray = (Object[]) toArrayMethod.invoke(realMatOfRect);
                if (rectArray == null) {
                    return new Rect[0];
                }
                Rect[] result = new Rect[rectArray.length];
                for (int i = 0; i < rectArray.length; i++) {
                    Object rect = rectArray[i];
                    Class<?> rectClass = rect.getClass();
                    int x = rectClass.getField("x").getInt(rect);
                    int y = rectClass.getField("y").getInt(rect);
                    int width = rectClass.getField("width").getInt(rect);
                    int height = rectClass.getField("height").getInt(rect);
                    result[i] = new Rect(x, y, width, height);
                }
                return result;
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(MatOfRect.class)
                    .error("Error converting MatOfRect to array", e);
                return new Rect[0];
            }
        }
        return new Rect[0];
    }
    
    public void release() {
        if (realMatOfRect != null) {
            try {
                Method releaseMethod = realMatOfRect.getClass().getMethod("release");
                releaseMethod.invoke(realMatOfRect);
            } catch (Exception e) {
                // Ignore
            }
        }
    }
    
    public Object getRealInstance() {
        return realMatOfRect;
    }
}

