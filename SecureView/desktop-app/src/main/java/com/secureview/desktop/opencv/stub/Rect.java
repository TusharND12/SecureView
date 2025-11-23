package com.secureview.desktop.opencv.stub;

/**
 * Stub class for OpenCV Rect.
 */
public class Rect {
    public int x, y, width, height;
    public Rect() {}
    public Rect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    public double area() { return width * height; }
}

