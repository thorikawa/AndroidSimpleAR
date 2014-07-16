package com.polysfactory.simplear.jni;

import java.util.List;

import org.opencv.core.Mat;

public class NativeMarkerDetector {

    private long mNativeObj;

    public NativeMarkerDetector(float fx, float fy, float cx, float cy) {
        mNativeObj = nativeCreateObject(fx, fy, cx, cy);
    }

    public void findMarkers(Mat imageBgra, List<Mat> transformations, float scale) {
        Mat transformationsMat = new Mat();
        nativeFindMarkers(mNativeObj, imageBgra.nativeObj, transformationsMat.nativeObj, scale);
        Converter.transformationsMatToList(transformationsMat, transformations);
    }

    public void release() {
        nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
    }

    private native long nativeCreateObject(float fx, float fy, float cx, float cy);

    private native void nativeFindMarkers(long thiz, long imageBgra, long transformations, float scale);

    private native void nativeDestroyObject(long thiz);
}
