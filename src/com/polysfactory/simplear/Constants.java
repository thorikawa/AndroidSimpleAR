package com.polysfactory.simplear;

import org.opencv.android.CameraBridgeViewBase;

public class Constants {
    public static final String MARKER_FILE_DIR = "image";
    public static final String MARKER_FILE_NAME = "marker.jpg";
    public static final int CAMERA_INDEX;
    public static final boolean FLIP;
    static {
        // if (Camera.getNumberOfCameras() > 1) {
        // Log.d(L.TAG, "Use front camera");
        // CAMERA_INDEX = CameraBridgeViewBase.CAMERA_ID_FRONT;
        // FLIP = true;
        // } else {
        // Log.d(L.TAG, "Use back camera");
        // CAMERA_INDEX = CameraBridgeViewBase.CAMERA_ID_ANY;
        // FLIP = false;
        // }
        CAMERA_INDEX = CameraBridgeViewBase.CAMERA_ID_BACK;
        FLIP = false;
    }
    // glass 640*360
    // public static final int MAX_FRAME_SIZE_WIDTH = 640;
    // public static final int MAX_FRAME_SIZE_HEIGHT = 480;
    public static final int MAX_FRAME_SIZE_WIDTH = 640;
    public static final int MAX_FRAME_SIZE_HEIGHT = 360;
}
