package com.yunbiao.ybsmartcheckin_live_id.faceview.camera;

/**
 * Created by michael on 19-5-7.
 */

public class CameraSettings {


    public static final int TYPE_JDY_K2002A_A4 = 2;

    private static int cameraType = TYPE_JDY_K2002A_A4;


    public static int getCameraType() {
        return cameraType;
    }

    public static void setCameraType(int cameraType) {
        CameraSettings.cameraType = cameraType;
    }

    public static final int SIZE_320_240 = 0;
    public static final int SIZE_640_480 = 1;
    public static final int SIZE_1280_720 = 2;
    public static final int SIZE_1280_960 = 3;
    public static final int SIZE_1920_1080 = 4;
    public static final int SIZE_1280_1024 = 5;
    public static final int SIZE_800_600 = 6;

    public static void setCameraPreviewSize(int size) {
        if (size == SIZE_320_240) {
            setCameraPreviewWidth(320);
            setCameraPreviewHeight(240);
            return;
        } else
        if (size == SIZE_640_480) {
            setCameraPreviewWidth(640);
            setCameraPreviewHeight(480);
            return;
        } else
        if (size == SIZE_1280_720) {
            setCameraPreviewWidth(1280);
            setCameraPreviewHeight(720);
            return;
        } else
        if (size == SIZE_1920_1080) {
            setCameraPreviewWidth(1920);
            setCameraPreviewHeight(1080);
            return;
        } else
        if(size == SIZE_1280_1024){
            setCameraPreviewWidth(1280);
            setCameraPreviewHeight(1024);
        } else
        if(size == SIZE_800_600){
            setCameraPreviewWidth(800);
            setCameraPreviewHeight(600);
        }
    }

    private static int CAMERA_PREVIEW_WIDTH = 640;
    private static int CAMERA_PREVIEW_HEIGHT = 480;

    public static void setCameraPreviewWidth(int cameraPreviewWidth) {
        cameraWidth = cameraPreviewWidth;
        CAMERA_PREVIEW_WIDTH = cameraPreviewWidth;
    }

    public static void setCameraPreviewHeight(int cameraPreviewHeight) {
        cameraHeight = cameraPreviewHeight;
        CAMERA_PREVIEW_HEIGHT = cameraPreviewHeight;
    }

    public static int getCameraPreviewWidth() {
        return CAMERA_PREVIEW_WIDTH;
    }

    public static int getCameraPreviewHeight() {
        return CAMERA_PREVIEW_HEIGHT;
    }


    private static int cameraWidth = CAMERA_PREVIEW_WIDTH;
    private static int cameraHeight = CAMERA_PREVIEW_HEIGHT;

    public static int getCameraWidth() {
        return cameraWidth;
    }

    public static int getCameraHeight() {
        return cameraHeight;
    }

    public static void setCameraWidth(int cameraWidth) {
        CameraSettings.cameraWidth = cameraWidth;
    }

    public static void setCameraHeight(int cameraHeight) {
        CameraSettings.cameraHeight = cameraHeight;
    }


    /**
     * 逆时针0度
     */
    public static final int ROTATION_0 = 0;
    /**
     * 逆时针90度
     */
    public static final int ROTATION_90 = 90;
    /**
     * 逆时针180度
     */
    public static final int ROTATION_180 = 180;
    /**
     * 逆时针270度
     */
    public static final int ROTATION_270 = 270;


    private static int cameraImageRotation = ROTATION_0;

    public static int getCameraImageRotation() {
        return cameraImageRotation;
    }

    /**
     * 输入相机输出图像预览角度
     *
     * @param rotation
     */
    public static void setCameraImageRotation(int rotation) {
        cameraImageRotation = rotation;
    }


    private static int cameraDisplayRotation = ROTATION_0;

    public static int getCameraDisplayRotation() {
        return cameraDisplayRotation;
    }

    /**
     * 输入相机预览图像旋转角度
     *
     * @param rotation
     */
    public static void setCameraDisplayRotation(int rotation) {
        cameraDisplayRotation = rotation;
    }


    public static boolean DISPLAY_MIRROR_RGB = true;
    public static boolean DISPLAY_MIRROR_NIR = true;


    public static boolean IMAGE_MIRROR_RGB = true;


}
