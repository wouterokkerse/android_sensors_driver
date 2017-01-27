package org.ros.android.android_sensors_driver.publishers.images;

/**
 * Created by main on 26.01.17.
 */


import java.io.FileOutputStream;
import java.util.List;

import org.opencv.android.JavaCameraView;
import org.ros.android.android_sensors_driver.publishers.images3.CameraPublisher;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;

public class SensorCameraView extends JavaCameraView implements PictureCallback {

    private static final String TAG = "Android_Sensors_Driver::SensorCameraView";
    private String mPictureFileName;
    private CameraPublisher mPictureListener;

    public SensorCameraView(Context context, int cameraId) {
        super(context, cameraId);
    }

    public List<String> getEffectList() {
        return mCamera.getParameters().getSupportedColorEffects();
    }

    public boolean isEffectSupported() {
        return (mCamera.getParameters().getColorEffect() != null);
    }

    public String getEffect() {
        return mCamera.getParameters().getColorEffect();
    }

    public void setEffect(String effect) {
        Camera.Parameters params = mCamera.getParameters();
        params.setColorEffect(effect);
        mCamera.setParameters(params);
    }

    public List<Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    public void setResolution(Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }

    public Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }

    public void cameraPictureResolutions() {
        Log.i(TAG, "camera params:" + mCamera.getParameters().flatten());
    }

    public void setupParameters() {
        Camera.Parameters params = mCamera.getParameters();
        params.setPictureSize(3264, 1836);
        params.setJpegQuality(85);
        mCamera.setParameters(params);
    }

    public void takePicture(final String fileName) {
        Log.i(TAG, "Taking picture");
        this.mPictureFileName = fileName;
        // Postview and jpeg are sent in the same buffers if the queue is not empty when performing a capture.
        // Clear up buffers to avoid mCamera.takePicture to be stuck because of a memory issue
        mCamera.setPreviewCallback(null);

        // PictureCallback is implemented by the current class
        mCamera.takePicture(null, null, this);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.i(TAG, "Saving a bitmap to file");
        // The camera preview was automatically stopped. Start it again.
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);

        // Write the image in a file (in jpeg format)
        try {
            FileOutputStream fos = new FileOutputStream(mPictureFileName);

            fos.write(data);
            fos.close();

            if (mPictureListener != null) {
                mPictureListener.onPictureTaken(data);
            }

        } catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }

    }

    public void setCameraPictureListener(CameraPublisher listener) {
        mPictureListener = listener;
    }
}