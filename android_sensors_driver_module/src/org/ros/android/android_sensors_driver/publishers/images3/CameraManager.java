package org.ros.android.android_sensors_driver.publishers.images3;


import android.graphics.Camera;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.StackView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.util.Log;
import android.os.Environment;
import android.os.Handler;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.ros.android.android_sensors_driver.MainActivity;
import org.ros.android.android_sensors_driver.R;
import org.ros.android.android_sensors_driver.publishers.images2.ImageParams;
import org.ros.android.android_sensors_driver.publishers.images.SensorCameraView;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraManager  implements NodeMain {

    private static final String TAG = "Android_Sensors_Driver::CameraManager";
    private ArrayList<SensorCameraView> mViewList;
    private ArrayList<CameraPublisher> mNodes;
    private ArrayList<Integer> camera_ids;
    private ArrayList<ImageParams.ViewMode> cameras_viewmode;
    private ArrayList<ImageParams.CompressionLevel> cameras_compression;

    private int camera_current;
    private String robotName;
    private MainActivity mainActivity;
    private ConnectedNode node = null;

    LinearLayout layout;
    LinearLayout.LayoutParams params;

    public CameraManager(MainActivity mainAct, ArrayList<Integer> camera_ids, String robotName, ArrayList<ImageParams.ViewMode> cameras_viewmode, ArrayList<ImageParams.CompressionLevel> cameras_compression) {
        this.mainActivity = mainAct;
        this.camera_ids = camera_ids;
        this.robotName = robotName;
        this.cameras_viewmode = cameras_viewmode;
        this.cameras_compression = cameras_compression;
        // Layout variables
        layout = (LinearLayout) mainActivity.findViewById(R.id.view_main);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    }


    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("android_sensors_driver/camera_publisher");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        this.node = connectedNode;
        this.mViewList = new ArrayList<SensorCameraView>();
        this.mNodes = new ArrayList<CameraPublisher>();
        // See if we can load opencv
        try {
            if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this.mainActivity, mOpenCVCallBack)) {
                Toast toast = Toast.makeText(mainActivity, "Cannot connect to OpenCV Manager", Toast.LENGTH_SHORT);
                toast.show();
                System.out.println("Cannot connect to OpenCV Manager");
                mainActivity.finish();
            }
        } catch(Exception e) {
            // Debug
            System.out.println("Cannot connect to OpenCV Manager");
            e.printStackTrace();
            // Toast to the user
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(mainActivity, "Cannot connect to OpenCV Manager", Toast.LENGTH_SHORT);
                    toast.show();
                    mainActivity.finish();
                }
            });
        }
    }

    @Override
    public void onShutdown(Node node) {
        for(int i=0; i<mViewList.size(); i++) {
            mViewList.get(i).disableView();
            mViewList.get(i).disableFpsMeter();
            mNodes.get(i).onShutdown(node);
        }
        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                for(int i=0; i<mViewList.size(); i++) {
                    layout.removeView(mViewList.get(i));
                }
            }
        });
    }

    @Override
    public void onShutdownComplete(Node node) {

    }

    @Override
    public void onError(Node node, Throwable throwable) {

    }

    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(mainActivity)
    {
        @Override
        @SuppressWarnings("deprecation")
        public void onManagerConnected(int status) {
            // Check if we have successfully loaded opencv
            if(status != LoaderCallbackInterface.SUCCESS) {
                // Tell the user it failed
                mainActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast toast = Toast.makeText(mainActivity, "Cannot connect to OpenCV Manager", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
                System.out.println("OpenCV loading FAILED!");
                // Send to super
                super.onManagerConnected(status);
                return;
            }
            // Our first camera
            camera_current = 0;
            // Create and set views
            for(int i=0; i<camera_ids.size(); i++) {
                // Create a new camera node
                SensorCameraView mOpenCvCameraView = new SensorCameraView(mainActivity, camera_ids.get(i));
                CameraPublisher pub = new CameraPublisher(camera_ids.get(i), robotName, cameras_viewmode.get(i), cameras_compression.get(i));
                mOpenCvCameraView.enableView();
                mOpenCvCameraView.enableFpsMeter();
                mOpenCvCameraView.setCvCameraViewListener(pub);
                mOpenCvCameraView.setCameraPictureListener(pub);
                mViewList.add(mOpenCvCameraView);
                mNodes.add(pub);
                // Start the node
                pub.onStart(node);

            }
            // Add the camera views
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for(int i=0; i<mViewList.size(); i++) {
                        layout.addView(mViewList.get(i), params);
                    }
                }
            });

            /*
            // timer to take picutres
            Timer timer = new Timer();
            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    mainActivity.runOnUiThread(takePictureRunnable());
                }
            }, 5000, 5000);*/

            /*
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Handler handler = new Handler();
                    handler.postDelayed(takePictureRunnable()
                            , 5000); //time in millis
                }
            });
            */
            //mainActivity.runOnUiThread(takePictureRunnable());

            // then continuously take pictures
            Handler handler2 = new Handler();
            handler2.postDelayed(takePictureRunnable(), 3000);
            /*
            Handler handler = new Handler();
            handler.postDelayed(takePictureRunnable());
            }, 5000); //time in millis

            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(takePictureRunnable(), 5000);
            */

        }
    };

    private Runnable takePictureRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                int wait_next_call = 1500;
                for (int i = 0; i < mViewList.size(); i++) {
                    ImageParams.ViewMode viewMode = cameras_viewmode.get(i);
                    if(viewMode == ImageParams.ViewMode.JPGEG_PICTURES) {
                        SensorCameraView mOpenCvCameraView = mViewList.get(i);
                        Log.i(TAG, "takePicutre timer event event for camera: "+i);
                        if(!mOpenCvCameraView.hasActiveCamera()) {
                            Log.i(TAG, "camera not active, skip");
                        } else {
                            mOpenCvCameraView.setupParameters();
                            mOpenCvCameraView.takePicture("");

                            // assume successfull, dont wait too long
                            wait_next_call = 100;

                            TextView infoLabel = (TextView) mainActivity.findViewById(R.id.textView2);
                            infoLabel.setText(String.format("ImageCount: %1$d", mOpenCvCameraView.getImageCount()));
                        }
                    }
                }
                // take picture again in x ms
                Handler handler = new Handler();
                handler.postDelayed(takePictureRunnable(), wait_next_call);
            }
        };
    };
}
