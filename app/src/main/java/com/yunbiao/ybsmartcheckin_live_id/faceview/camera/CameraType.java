package com.yunbiao.ybsmartcheckin_live_id.faceview.camera;

import android.util.Log;


/**
 * Created by michael on 19-1-29.
 */

public class CameraType {
    private static int RGB = 0;
    private static int NIR = 1;


    public static int getRGB() {
        Log.d("FaceLocalCamera", "................ CameraType RGB = " + RGB);
        return RGB;
    }

    public static void setRGB(int RGB) {
        Log.d("FaceLocalCamera", "............. init camera type RGB = " + RGB);
        CameraType.RGB = RGB;
//        FaceLogHelper.persistentInitRGB(RGB);
    }

    public static int getNIR() {
        Log.d("FaceLocalCamera", "................ CameraType NIR = " + NIR);
        return NIR;
    }

    public static void setNIR(int NIR) {
        Log.d("FaceLocalCamera", "............. init camera type NIR = " + NIR);
        CameraType.NIR = NIR;
//        FaceLogHelper.persistentInitNIR(NIR);


    }

    public static String getCameraLabel(int cameraType) {
        if (cameraType == RGB) {
            return "RGB";
        }

        if (cameraType == NIR) {
            return "NIR";
        }

        return null;

    }


}
