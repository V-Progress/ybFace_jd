package com.yunbiao.ybsmartcheckin_live_id.faceview;

import android.graphics.Rect;

/**
 * Created by michael on 19-3-15.
 */

public class FaceBoxUtil {
    public static Rect getPreviewBox(Rect cameraBox) {
        
        final int cameraImageWidth = 1280;
        final int cameraImageHeight = 720;
        
        final float scaleX = ((float) 960) / cameraImageWidth;
        final float scaleY = ((float) 540) / cameraImageHeight;
        
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
