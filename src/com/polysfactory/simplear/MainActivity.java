package com.polysfactory.simplear;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraViewEx;
import org.opencv.core.Mat;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.polysfactory.simplear.jni.NativeMarkerDetector;

public class MainActivity extends Activity implements CvCameraViewListener2 {

    private ViewGroup mContainer;
    private JavaCameraViewEx mCameraView;
    private GLSurfaceView mGLView;
    private NativeMarkerDetector mMarkerDetector;
    private boolean mFindMarker = false;
    private float[] mTransformation = new float[16];
    private int mWidth;
    private int mHeight;
    private float mScale;

    private static final float[] mCameraParameters = { 357.658935546875f, 357.658935546875f, 319.5f, 179.5f };

    // OpenGL constants
    private static final FloatBuffer squareVertices = allocateFloatBufferDirect(new float[] { -0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f, 0.5f, 0.5f });
    private static final ByteBuffer squareColors = allocateByteBufferDirect(new byte[] { (byte) 255, (byte) 255, 0,
            (byte) 255, 0, (byte) 255, (byte) 255, (byte) 255, 0, 0, 0, 0, (byte) 255, 0, (byte) 255, (byte) 255 });
    private static final FloatBuffer lineX = allocateFloatBufferDirect(new float[] { 0f, 0f, 0f, 1f, 0f, 0f });
    private static final FloatBuffer lineY = allocateFloatBufferDirect(new float[] { 0f, 0f, 0f, 0f, 1f, 0f });
    private static final FloatBuffer lineZ = allocateFloatBufferDirect(new float[] { 0f, 0f, 0f, 0f, 0f, 1f });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.marker_tracking);

        mContainer = (ViewGroup) findViewById(R.id.container);

        mWidth = getResources().getDimensionPixelSize(R.dimen.view_width);
        mHeight = getResources().getDimensionPixelSize(R.dimen.view_height);

        mCameraView = (JavaCameraViewEx) findViewById(R.id.fd_activity_surface_view);
        mCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_ANY);
        mCameraView.setCvCameraViewListener(this);
        mCameraView.setMaxFrameSize(mWidth, mHeight);
        mCameraView.disableView();

        mGLView = new GLSurfaceView(getApplication());
        mGLView.setZOrderOnTop(true);
        mGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLView.setRenderer(new GLRenderer());
        mGLView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        LayoutParams lp = new LayoutParams(mWidth, mHeight);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mContainer.addView(mGLView, lp);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCameraView != null) {
            mCameraView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMarkerDetector = new NativeMarkerDetector(mCameraParameters[0], mCameraParameters[1], mCameraParameters[2],
                mCameraParameters[3]);
        mCameraView.enableView();
    }

    public void onDestroy() {
        super.onDestroy();
        mCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(L.TAG, "framesize:" + width + "," + height);
        mScale = mCameraView.getScale();
    }

    @Override
    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        Mat frame = inputFrame.rgba();
        if (mMarkerDetector != null) {
            List<Mat> transformations = new ArrayList<Mat>();
            float scale = mScale == 0.0f ? 1.0f : mScale;
            mMarkerDetector.findMarkers(frame, transformations, scale);
            int count = transformations.size();
            if (count > 0) {
                Mat mat = transformations.get(0);
                synchronized (this) {
                    mat.get(0, 0, mTransformation);
                }
                mFindMarker = true;
            } else {
                mFindMarker = false;
            }
        }

        return frame;
    }

    private static FloatBuffer allocateFloatBufferDirect(float[] data) {
        FloatBuffer result;
        ByteBuffer vbb = ByteBuffer.allocateDirect(data.length * Float.SIZE / 8);
        vbb.order(ByteOrder.nativeOrder());
        result = vbb.asFloatBuffer();
        result.put(data);
        result.flip();
        return result;
    }

    private static ByteBuffer allocateByteBufferDirect(byte[] data) {
        ByteBuffer vbb = ByteBuffer.allocateDirect(data.length * Byte.SIZE / 8);
        vbb.order(ByteOrder.nativeOrder());
        vbb.put(data);
        vbb.flip();
        return vbb;
    }

    private class GLRenderer implements GLSurfaceView.Renderer {

        private float[] projectionMatrix;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            projectionMatrix = buildProjectionMatrix(width, height);
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadMatrixf(allocateFloatBufferDirect(projectionMatrix));
        }

        @Override
        public void onDrawFrame(GL10 gl) {

            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            drawAR(gl);
        }

        private void drawAR(GL10 gl) {

            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();

            gl.glDepthMask(true);
            gl.glEnable(GL10.GL_DEPTH_TEST);

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);

            gl.glPushMatrix();
            gl.glLineWidth(5.0f);

            if (mFindMarker) {
                synchronized (this) {
                    gl.glLoadMatrixf(allocateFloatBufferDirect(mTransformation));
                }

                gl.glVertexPointer(2, GL10.GL_FLOAT, 0, squareVertices);
                gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                gl.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, squareColors);
                gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

                gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
                gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

                float scale = 0.5f;
                gl.glScalef(scale, scale, scale);

                gl.glTranslatef(0f, 0f, 0.1f);

                gl.glColor4f(1f, 0f, 0f, 1f);
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineX);
                gl.glDrawArrays(GL10.GL_LINES, 0, 2);

                gl.glColor4f(0f, 1f, 0f, 1f);
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineY);
                gl.glDrawArrays(GL10.GL_LINES, 0, 2);

                gl.glColor4f(0f, 0f, 1f, 1f);
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineZ);
                gl.glDrawArrays(GL10.GL_LINES, 0, 2);
            }

            gl.glPopMatrix();
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        }
    }

    private static float[] buildProjectionMatrix(int w, int h) {
        float near = 0.01f; // Near clipping distance
        float far = 100f; // Far clipping distance

        // Camera parameters
        float f_x = mCameraParameters[0]; // Focal length in x axis
        float f_y = mCameraParameters[1]; // Focal length in y axis (usually the same?)
        float c_x = mCameraParameters[2]; // Camera primary point x
        float c_y = mCameraParameters[3]; // Camera primary point y

        float projectionMatrix[] = new float[16];
        float size = 2.0f;
        projectionMatrix[0] = -2.0f * f_x / w;
        projectionMatrix[1] = 0.0f;
        projectionMatrix[2] = 0.0f;
        projectionMatrix[3] = 0.0f;

        projectionMatrix[4] = 0.0f;
        projectionMatrix[5] = 2.0f * f_y / h;
        projectionMatrix[6] = 0.0f;
        projectionMatrix[7] = 0.0f;

        projectionMatrix[8] = size * c_x / w - 1.0f;
        projectionMatrix[9] = size * c_y / h - 1.0f;
        projectionMatrix[10] = -(far + near) / (far - near);
        projectionMatrix[11] = -1.0f;

        projectionMatrix[12] = 0.0f;
        projectionMatrix[13] = 0.0f;
        projectionMatrix[14] = -2.0f * far * near / (far - near);
        projectionMatrix[15] = 0.0f;

        return projectionMatrix;
    }

}
