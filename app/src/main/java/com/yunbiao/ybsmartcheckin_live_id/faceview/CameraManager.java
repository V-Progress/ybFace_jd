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
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2019/5/22.
 */

public class CameraManager {

    public static final int L = 0;
    public static final int P = 90;
    public static final int L_R = 180;
    public static final int P_R = 270;
    private SurfaceHolder mHolder;

    private static int CAMERA_ORIENTATION = P;
    private static int CAMERA_WIDTH = 640;//720
    private static int CAMERA_HEIGHT = 480;//1280

    private static final String TAG = "CameraManager";
    private static CameraManager instance;
    private Camera mCamera;
    private CameraStateListener mListener;
    private byte[] mBuffer;

    private List<Camera.Size> supportSizeList = new ArrayList<>();

    private final Object mLock = new Object();

//        private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;

    public int getCameraType(){
        return CAMERA_TYPE;
    }

    public static CameraManager instance() {
        if (instance == null) {
            synchronized (CameraManager.class) {
                if (instance == null) {
                    instance = new CameraManager();
                }
            }
        }
        return instance;
    }

    private CameraManager() {
        CAMERA_ORIENTATION = SpUtils.getInt(SpUtils.CAMERA_ANGLE);
        String str = SpUtils.getStr(SpUtils.CAMERA_SIZE, "1280*720");
        String[] split = str.split("\\*");
        CAMERA_WIDTH = Integer.valueOf(split[0]);
        CAMERA_HEIGHT = Integer.valueOf(split[1]);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        APP.getContext().registerReceiver(cameraReceiver, intentFilter);
    }

    public void init(CameraStateListener listener) {
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
                    if (i > 0) {
                        d("检测到摄像头已连接... ");
                        openCamera(mHolder);
                    } else {
                        d("摄像头已移除... ");
                        if (mListener != null) {
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

        if (mListener != null) {
            mListener.onBeforeCamera();
        }
        //释放
        releaseCamera();

        try{
            //开启
            doOpenCamera();
        }catch (Exception e){
            e.printStackTrace();
            if (mListener != null) {
                mListener.onNoneCamera();
            }
            return;
        }

        //回调
        setCallback();
        //参数
        setParameters();
        //设置预览
        setPreviewBuffer();
        //开启预览
        startPreview();
    }

    private void doOpenCamera(){
        d("准备开启摄像头... ");
        synchronized (mLock) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            d("摄像头数量... " + Camera.getNumberOfCameras());
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, info);
                d("摄像头id... " + i +" 种类：" + (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK ? "后置":"前置"));
            }

            if (Camera.getNumberOfCameras() <= 0) {
                d("无可用摄像头... ");
                if (mListener != null) {
                    mListener.onNoneCamera();
                }
                return;
            }

            try{
                for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                    Camera.getCameraInfo(i, info);
                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        d("准备开启... " + i);
                        mCamera = Camera.open(i); // 打开对应的摄像头，获取到camera实例
                        if(mCamera != null){
                            CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;
                            d("已开启... 后置");
                            if (mListener != null) {
                                mListener.onCameraOpened();
                            }
                        }
                        break;
                    }
                }
            }catch (Exception e){
                throw e;
            }

            if(mCamera != null){
                return;
            }

            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    d("准备开启... " + i);
                    mCamera = Camera.open(i); // 打开对应的摄像头，获取到camera实例
                    if(mCamera != null){
                        CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_FRONT;
                        d("已开启... 前置");
                        if (mListener != null) {
                            mListener.onCameraOpened();
                        }
                    }
                    break;
                }
            }
        }
    }

    public List<Camera.Size> getSupportSizeList(){
        return supportSizeList;
    }

    private void setCallback() {
        if (mCamera != null) {
            d("设置Error回调... ");
            mCamera.setErrorCallback(errorCallback);
        }
    }

    private void setParameters() {
        synchronized (mLock) {
            if (mCamera == null) {
                return;
            }
            d("设置参数... ");
            //设置参数
            try{
                Camera.Parameters params = mCamera.getParameters();
                supportSizeList.clear();
                final List<Camera.Size> sizes = params.getSupportedPreviewSizes();
                supportSizeList.addAll(sizes);
                Log.e(TAG, "setParameters: ----- " + CAMERA_WIDTH + "*" + CAMERA_HEIGHT);
                params.setPreviewSize(CAMERA_WIDTH, CAMERA_HEIGHT);
                mCamera.setParameters(params);
            }catch (Exception e){
                CAMERA_WIDTH = 640;
                CAMERA_HEIGHT = 480;
                e.printStackTrace();
            }
            mCamera.setDisplayOrientation(CAMERA_ORIENTATION);
        }
    }

    private void setPreviewBuffer() {
        synchronized (mLock) {
            if (mCamera == null) {
                return;
            }
            d("设置预览回调... ");
            mBuffer = new byte[CAMERA_WIDTH * CAMERA_HEIGHT * 3 / 2];
            mCamera.addCallbackBuffer(mBuffer);
            mCamera.setPreviewCallbackWithBuffer(previewCallback);
        }
    }

    private void startPreview() {
        if (mHolder == null) {
            d("开启预览失败... surfaceHolder can not be null");
            return;
        }
        synchronized (mLock) {
            d("开启预览... ");

            if (mCamera != null) {
                try {
                    mCamera.setPreviewDisplay(mHolder);
                    mCamera.startPreview();

                    if (mListener != null) {
                        mListener.onPreviewReady();
                    }
                } catch (IOException e) {
                    d("开启预览失败... " + e !=null?e.getMessage():"NULL");
                    e.printStackTrace();
                }
            } else {
                int i = checkCamera();
                if(i <= 0){
                    d("无摄像头，停止... ");
                    return;
                }
                d("Camera为null，重新开启... ");
                openCamera(mHolder);
            }
        }
    }

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
//            final byte[] frameCopy = Arrays.copyOf(data, data.length);
            if (camera != null) {
                camera.addCallbackBuffer(data);
            }
            final byte[] frameRotateRGB = FrameHelper.getFrameRotate(data, CAMERA_WIDTH, CAMERA_HEIGHT);
            FaceFrameManager.handleCameraFrame(frameRotateRGB, null, CAMERA_WIDTH, CAMERA_HEIGHT);
        }
    };

    private Camera.ErrorCallback errorCallback = new Camera.ErrorCallback() {
        @Override
        public void onError(int error, Camera camera) {
            d("摄像头异常... " + error);
            if (mListener != null) {
                mListener.onCameraError(error);
            }

            int i = checkCamera();
            if (i <= 0) {
                d("无可用摄像头... ");
                if (mListener != null) {
                    mListener.onNoneCamera();
                }
                return;
            }

            d("准备重启摄像头服务... ");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    openCamera(mHolder);
                }
            }, 200);
        }
    };

    /**
     * 释放相机资源
     */
    public void releaseCamera() {
        if (null != mCamera) {
            d("释放摄像头... ");
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
            boolean isBack = info.facing == CAMERA_TYPE;
            cameraResult = isBack ? 1 : 0;
        }
        return cameraResult;
    }

    public static int getWidth() {
        return CAMERA_WIDTH;
    }

    public static int getHeight() {
        return CAMERA_HEIGHT;
    }

    public boolean isInit() {
        return mCamera != null;
    }

    public void setOrientation(int tag) {
        if (mCamera != null) {
            CAMERA_ORIENTATION = tag;
            mCamera.setDisplayOrientation(tag);
        }
    }

    public static int getOrientation() {
        return CAMERA_ORIENTATION;
    }

    public void onDestroy() {
        releaseCamera();
        APP.getContext().unregisterReceiver(cameraReceiver);
    }

    public void setStateListener(CameraStateListener listener) {
        mListener = listener;
    }

    public abstract static class CameraStateListener {
        void onBeforeCamera() { }

        void onCameraOpened() { }

        void onPreviewReady() { }

        void onCameraError(int errCode) { }

        void onNoneCamera() { }
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
                    final Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
                    if (shotCallBack != null) {
                        shotCallBack.onShoted(bm);
                    }
                }
            });
        }
    }

    private void d(String msg){
//        XLog.d(TAG,msg);
        Log.d(TAG,msg);
    }
}
