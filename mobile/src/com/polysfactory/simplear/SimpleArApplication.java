package com.polysfactory.simplear;

import org.opencv.android.OpenCVLoader;

import android.app.Application;

public class SimpleArApplication extends Application {
    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        } else {
            System.loadLibrary("simplear");
        }
    }

    public void onCreate() {
    };
}
