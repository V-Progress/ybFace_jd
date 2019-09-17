package com.yunbiao.ybsmartcheckin_live_id.faceview.camera;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.jdjr.risk.face.local.frame.FaceFrameManager;
import com.yunbiao.ybsmartcheckin_live_id.faceview.rect.FrameHelper;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExtCameraManager {
    private static final String TAG = "ExtCameraManager";
    private Camera mRGBCamera;
    private Camera mNIRCamera;

    private static ExtCameraManager surfaceCameraManager = new ExtCameraManager();
    private List<Camera.Size> supportedPreviewSizes;
    private final ScheduledExecutorService scheduledExecutorService;

    public static ExtCameraManager instance(){
        return surfaceCameraManager;
    }

    private ExtCameraManager(){
        scheduledExecutorService = Executors.newScheduledThreadPool(2);
    }

    /***
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     */
    public void init(SurfaceView rgbSurface, SurfaceView nirSurface){
        rgbSurface.getHolder().addCallback(rgbCallback);
//        nirSurface.getHolder().addCallback(nirCallback);
    }

//    public void init(SurfaceView rgbSurface){
//        rgbSurface.getHolder().addCallback(rgbCallback);
//    }

    private void delayRun(Runnable runnable){
        scheduledExecutorService.schedule(runnable,1,TimeUnit.SECONDS);
    }

    private SurfaceHolder.Callback rgbCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(final SurfaceHolder holder) {
            if(mListener != null){
                mListener.onSurfaceReady();
            }
            releaseRGBCamera();
            delayRun(new Runnable() {
                @Override
                public void run() {
                    mRGBCamera = doOpenCamera(CameraType.getRGB(),CameraSettings.getCameraPreviewWidth(),CameraSettings.getCameraPreviewHeight(),holder,mRGBCallback);
                }
            });
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            releaseRGBCamera();
        }
    };

    private SurfaceHolder.Callback nirCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(final SurfaceHolder holder) {
            releaseNIRCamera();
            delayRun(new Runnable() {
                @Override
                public void run() {
                    mNIRCamera = doOpenCamera(CameraType.getNIR(),CameraSettings.getCameraPreviewWidth(),CameraSettings.getCameraPreviewHeight(),holder,mNIRCallback);
                }
            });
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            releaseNIRCamera();
        }
    };

    private synchronized Camera doOpenCamera(int cameraType, int cameraPreviewWidth, int cameraPreviewHeight, SurfaceHolder holder , Camera.PreviewCallback previewCallback){
        try{
            Camera camera = Camera.open(cameraType);
            camera.setDisplayOrientation(CameraSettings.getCameraDisplayRotation());
            Camera.Parameters parameters = camera.getParameters();
            supportedPreviewSizes = parameters.getSupportedPreviewSizes();
            for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
                Log.e(TAG, "doOpenCamera: " + supportedPreviewSize.width + " * " + supportedPreviewSize.height);
            }

            parameters.setPreviewSize(cameraPreviewWidth, cameraPreviewHeight);
            camera.setParameters(parameters);
            for (int i = 0; i < 3; i++) {
                int length = cameraPreviewWidth * cameraPreviewHeight * 3 / 2;
                camera.addCallbackBuffer(new byte[length]);
            }
            camera.setPreviewCallbackWithBuffer(previewCallback);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            return camera;
        }catch (Exception e){
            e.printStackTrace();
            Log.d("FaceLocalSystemRGBNIR", "########## doCameraPreview RGB exception");
        }
        return null;
    }

    /***
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     */
    private Camera.PreviewCallback mRGBCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(final byte[] data, final Camera camera) {
            final byte[] frameCopy = Arrays.copyOf(data, data.length);

            if (camera != null) {
                camera.addCallbackBuffer(data);
            }
            final byte[] frameRotateRGB = FrameHelper.getFrameRotate(frameCopy, CameraSettings.getCameraPreviewWidth(), CameraSettings.getCameraPreviewHeight());
            FaceFrameManager.handleCameraFrame(frameRotateRGB, mLastFrameNIR, CameraSettings.getCameraWidth(), CameraSettings.getCameraHeight());
        }
    };

    private byte[] mLastFrameNIR = null;

    private Camera.PreviewCallback mNIRCallback = new Camera.PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            final byte[] copy = Arrays.copyOf(data, data.length);
            if (camera != null) {
                camera.addCallbackBuffer(data);
            }
            final byte[] frameRotate = FrameHelper.getFrameRotate(copy, CameraSettings.getCameraWidth(), CameraSettings.getCameraHeight());
            mLastFrameNIR = frameRotate;
        }
    };

    public List<Camera.Size> getSupportSizeList(){
        return supportedPreviewSizes;
    }

    public void releaseRGBCamera(){
        try {
            if (mRGBCamera != null) {
                mRGBCamera.setPreviewCallback(null);
                mRGBCamera.stopPreview();
                mRGBCamera.release();
                mRGBCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void releaseNIRCamera(){
        try {
            if (mNIRCamera != null) {
                mNIRCamera.setPreviewCallback(null);
                mNIRCamera.stopPreview();
                mNIRCamera.release();
                mNIRCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void releaseAllCamera(){
        releaseRGBCamera();
        releaseNIRCamera();
    }

    public void setViewReadyListener(ViewReadyListener listener) {

        mListener = listener;
    }

    private ViewReadyListener mListener;

    public interface ViewReadyListener{
        void onSurfaceReady();
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;//相位公差
        double targetRatio = (double) w / h;//目标比例
        if (sizes == null) return null;//如果传的尺寸为空就结束
        Camera.Size optimalSize = null;//最佳分辨率
        double minDiff = Double.MAX_VALUE;//最小差异值
        int targetHeight = h;//目标高度为传入的高度
        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {//循环当前支持的size
            double ratio = (double) size.width / size.height;//得出比例
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;//当前比例减去目标比例，如果大于相位公差则表示差距过大，不可用
            if (Math.abs(size.height - targetHeight) < minDiff) {//如果小于相位公差
                optimalSize = size;//则设置最佳尺寸
                minDiff = Math.abs(size.height - targetHeight); //最小差异值修改
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {//如果最佳尺寸为空
            minDiff = Double.MAX_VALUE;//最小差异改为最大
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

}
