package com.yunbiao.ybsmartcheckin_live_id.faceview;

import android.graphics.Rect;

/**
 * Created by michael on 19-3-15.
 */

public class FaceBoxUtil {
    private static float previewWidth = 960;
    private static float previewHeight = 540;

    public static void setPreviewWidth(float previewWidth,float previewHeight) {
        FaceBoxUtil.previewWidth = previewWidth;
        FaceBoxUtil.previewHeight = previewHeight;
    }

    public static Rect getPreviewBox(Rect cameraBox) {
        
        final int cameraImageWidth = CameraManager.getWidth();
        final int cameraImageHeight = CameraManager.getHeight();
        
        final float scaleX = previewWidth / cameraImageWidth;
        final float scaleY = previewHeight / cameraImageHeight;
        
        int scaleLeft = (int) (cameraBox.left * scaleX);
        int scaleTop = (int) (cameraBox.top * scaleY);
        int scaleRight = (int) (cameraBox.right * scaleX);
        int scaleBottom = (int) (cameraBox.bottom * scaleY);
        
        int finalLeft = scaleLeft;
        int finalRight = scaleRight;
        
        if (!true) {
            finalLeft = cameraImageWidth - scaleRight;
            finalRight = cameraImageWidth - scaleLeft;
        }
        
        if (!true) {
            finalLeft = cameraImageWidth - scaleRight;
            finalRight = cameraImageWidth - scaleLeft;
        }
        
        int finalTop = scaleTop;
        int finalBottom = scaleBottom;
//        if (!CameraDisplayMirror.MIRROR_PORTRAIT) {
//            finalTop = cameraImageHeight - scaleBottom;
//            finalBottom = cameraImageHeight - scaleTop;
//        }



//        Log.d("FaceLocalScale", "@@@@@@@@@@@@@@@@@@@@@@@@ scaleLeft = " + scaleLeft);
//        Log.d("FaceLocalScale", "@@@@@@@@@@@@@@@@@@@@@@@@ scaleTop = " + scaleTop);
//        Log.d("FaceLocalScale", "@@@@@@@@@@@@@@@@@@@@@@@@ scaleRight = " + scaleRight);
//        Log.d("FaceLocalScale", "@@@@@@@@@@@@@@@@@@@@@@@@ scaleBottom = " + scaleBottom);


//        if (mRGBScaleY == -1) {
//            scaleTop = DisplaySize.HEIGHT - scaleTop;
//            scaleBottom = DisplaySize.HEIGHT - scaleBottom;
//        }
        
        
        
        final Rect previewBox = new Rect(finalLeft, finalTop, finalRight, finalBottom);
        
        return previewBox;
    }




}
