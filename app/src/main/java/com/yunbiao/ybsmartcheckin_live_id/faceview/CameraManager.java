package com.yunbiao.ybsmartcheckin_live_id.faceview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import com.jdjr.risk.face.local.frame.FaceFrameManager;
import com.yunbiao.ybsmartcheckin_live_id.APP;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2019/5/22.
 */

public class CameraManager {

    public static final int P = 0;
    public static final int L = 90;
    public static final int P_R = 180;
    public static final int L_R = 270;
    private SurfaceHolder mHolder;

    private static int CAMERA_ORIENTATION = P;
    private static int CAMERA_WIDTH = 1280;
    private static int CAMERA_HEIGHT = 720;

    private static final String TAG = "CameraManager";
    private static CameraManager instance;
    private final ExecutorService cameraThread;
    private Camera mCamera;
    private CameraStateListener mListener;
    private byte[] mBuffer;

    public static CameraManager instance(){
        if(instance == null){
            synchronized(CameraManager.class){
                if(instance == null){
                    instance = new CameraManager();
                }
            }
        }
        return instance;
    }

    private CameraManager(){
        cameraThread = Executors.newSingleThreadExecutor();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        APP.getContext().registerReceiver(cameraReceiver, intentFilter);
    }

    public void init(CameraStateListener listener){
        mListener = listener;
    }

    private BroadcastReceiver cameraReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
//                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    int i = checkCamera();
                    if(i > 0){
                        openCamera(mHolder);
                    } else {
                        if(mListener != null){
                            mListener.onNoneCamera();
                        }
                    }
                    break;
            }
        }
    };
    /*------------------------------------------------------------------------------*/

    /***
     * 开启摄像头
     */
    public void openCamera(final SurfaceHolder holder) {
        mHolder = holder;
        if(mListener != null){
            mListener.onBeforeCamera();
        }
        cameraThread.execute(new Runnable() {
            @Override
            public void run() {

                Log.e(TAG, "run: 1");
                releaseCamera();

                Camera.CameraInfo info = new Camera.CameraInfo();
                Log.e(TAG, "共有摄像头：" + Camera.getNumberOfCameras());
                for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                    Camera.getCameraInfo(i, info);
                    Log.e(TAG, "摄像头id：" + i + " 种类：" +info.facing);
                }

                if(Camera.getNumberOfCameras() <= 0){
                    Log.e(TAG, "run: 2");
                    if(mListener != null){
                        mListener.onNoneCamera();
                    }
                    return;
                }

                for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                    Log.e(TAG, "run: 3");
                    Camera.getCameraInfo(i, info);
                    Log.e(TAG, "run: 4");
                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        Log.e(TAG, "run: 5");
                        Log.d(TAG, "run: 即将打开 " + i);
                        mCamera = Camera.open(i); // 打开对应的摄像头，获取到camera实例
                        break;
                    }
                }

                setCallback();

                setParameters();

                setPreviewBuffer();

                if(mListener != null){
                    mListener.onCameraOpened(mCamera);
                }

                if(mHolder != null){
                    Log.e(TAG, "run: 6");
                    startPreview();
                }
            }
        });
    }

    private void setCallback(){
        mCamera.setErrorCallback(errorCallback);
    }

    private void setParameters(){
        if(mCamera == null || checkCamera() <= 0){
            return;
        }
        //设置参数
        Camera.Parameters params = mCamera.getParameters();
        final List<Camera.Size> sizes =  params.getSupportedPreviewSizes();

        for (Camera.Size size : sizes) {
            Log.d("FaceLocalCamera", "............ size width = " + size.width);
            Log.d("FaceLocalCamera", "............ size height = " + size.height);
        }

        params.setPreviewSize(CAMERA_WIDTH, CAMERA_HEIGHT);
        mCamera.setParameters(params);
    }

    private void setPreviewBuffer(){
        if(mCamera == null || checkCamera() <= 0){
            return;
        }
        mCamera.setDisplayOrientation(CAMERA_ORIENTATION);
        mBuffer = new byte[CAMERA_WIDTH * CAMERA_HEIGHT * 3 / 2];
        mCamera.addCallbackBuffer(mBuffer);
        mCamera.setPreviewCallbackWithBuffer(previewCallback);
    }

    private void startPreview() {
        Log.e(TAG, "run: 7");
        if(mHolder == null){
            return;
        }
        cameraThread.execute(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "run: 8");
                if(mCamera != null){
                    Log.e(TAG, "run: 9");
                    try {
                        Log.e(TAG, "run: 10");
                        mCamera.setPreviewDisplay(mHolder);
                        mCamera.startPreview();

                        if(mListener != null){
                            mListener.onPreviewReady();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "run: 11");
                        e.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "run: 12");
                    openCamera(mHolder);
                }
            }
        });
    }

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            final byte[] frameCopy = Arrays.copyOf(data, data.length);
            if (camera != null) {
                camera.addCallbackBuffer(data);
            }
            final byte[] frameRotateRGB = FrameHelper.getFrameRotate(frameCopy, CAMERA_WIDTH, CAMERA_HEIGHT);
            FaceFrameManager.handleCameraFrame(frameRotateRGB, null, CAMERA_WIDTH, CAMERA_HEIGHT);
        }
    };

    private Camera.ErrorCallback errorCallback = new Camera.ErrorCallback() {
        @Override
        public void onError(int error, Camera camera) {
            if(mListener != null){
                mListener.onCameraError(error);
            }
            int i = checkCamera();
            if(i <= 0){
                if(mListener != null){
                    mListener.onNoneCamera();
                }
                return;
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    openCamera(mHolder);
                }
            },200);
        }
    };

    /**
     * 释放相机资源
     */
    public void releaseCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private int checkCamera() {
        int cameraResult = 0;
        int numberOfCameras = Camera.getNumberOfCameras();
        if (numberOfCameras <= 0) {
            cameraResult = 0;
        }
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            boolean isBack = info.facing == Camera.CameraInfo.CAMERA_FACING_BACK;
            cameraResult = isBack ? 1 : 0;
        }
        return cameraResult;
    }

    public static void setCameraSize(int width,int height){
        CAMERA_WIDTH = width;
        CAMERA_HEIGHT = height;
    }

    public static int getWidth(){
        return CAMERA_WIDTH;
    }

    public static int getHeight(){
        return CAMERA_HEIGHT;
    }

    public boolean isInit(){
        return mCamera != null;
    }

    public void setOrientation(int tag){
        if(mCamera != null){
            CAMERA_ORIENTATION = tag;
            mCamera.setDisplayOrientation(tag);
        }
    }

    public static int getOrientation(){
        return CAMERA_ORIENTATION;
    }

    public void onDestroy(){
        releaseCamera();
        APP.getContext().unregisterReceiver(cameraReceiver);
    }

//    public void setPreviewCallback(Camera.PreviewCallback previewCallback){
//        if(mCamera != null){
//            this.previewCallback = previewCallback;
//            mCamera.setPreviewCallback(previewCallback);
//        }
//    }

    public void setStateListener(CameraStateListener listener){
        mListener = listener;
    }

    public abstract static class CameraStateListener{
        public void onBeforeCamera(){}
        public void onCameraOpened(Camera mCamera){}
        public void onPreviewReady(){}
        public void onCameraError(int errCode){}
        public void onNoneCamera(){}
    }


    public interface ShotCallBack {
        void onShoted(Bitmap bitmap);
    }

    public void shot(final ShotCallBack shotCallBack) {
        if (mCamera != null) {
            mCamera.takePicture(new Camera.ShutterCallback() {
                @Override
                public void onShutter() {
                    try {
                        mCamera.cancelAutoFocus();
                        mCamera.reconnect();
                        mCamera.startPreview();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(final byte[] data, Camera camera) {
                    cameraThread.execute(new Runnable() {
                        @Override
                        public void run() {
                            // 根据拍照所得的数据创建位图
                            final Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
                            if (shotCallBack != null) {
                                shotCallBack.onShoted(bm);
                            }
                        }
                    });
                }
            });
        }
    }
}
