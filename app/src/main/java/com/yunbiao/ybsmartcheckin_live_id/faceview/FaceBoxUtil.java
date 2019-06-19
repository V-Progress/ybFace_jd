package com.yunbiao.ybsmartcheckin_live_id.faceview;

import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

/**
 * Created by michael on 19-3-15.
 */

public class FaceBoxUtil {
    private static final String TAG = "FaceBoxUtil";
    private static float previewWidth = 960;
    private static float previewHeight = 540;

    private static int mCameraWidth = 0;
    private static int mCameraHeight = 0;

    private static boolean IS_MIRROR = true;
    private static float mXRatio;
    private static float mYRatio;

    public static void setPreviewWidth(int l,int r,int t,int b,float previewWidth,float previewHeight) {
        mOverRect = new Rect(l,t,r,b);
        FaceBoxUtil.previewWidth = previewWidth;
        FaceBoxUtil.previewHeight = previewHeight;
        Log.e(TAG, "setPreviewWidth: " + previewWidth + " ----- " + previewHeight);

        mCameraWidth = CameraManager.getWidth();
        mCameraHeight = CameraManager.getHeight();

        mXRatio = (float) mOverRect.width() / (float) mCameraWidth;
        mYRatio = (float) mOverRect.height() / (float) mCameraHeight;
        Log.e(TAG, "计算缩放比例：XRatio：" + mXRatio + "---- YRatio：" + mYRatio);
        Log.e(TAG, "计算缩放比例：mOverRect.width()：" + mOverRect.width() + "---- mOverRect.height()：" + mOverRect.height());
        Log.e(TAG, "计算缩放比例：mCameraWidth：" + mCameraWidth + "---- mCameraHeight：" + mCameraHeight);
    }


    private static RectF mDrawFaceRect = new RectF();
    private static Rect mOverRect = new Rect();

    public static RectF setFaceFacingBack(Rect faceRect) {
        if(IS_MIRROR){
            mDrawFaceRect.left = mOverRect.left + (float) faceRect.left * mXRatio;
            mDrawFaceRect.right = mOverRect.left + (float) faceRect.right * mXRatio;
        } else {
            mDrawFaceRect.left = mOverRect.left + (float) (mCameraWidth - faceRect.right) * mXRatio;
            mDrawFaceRect.right = mOverRect.left + (float) (mCameraWidth - faceRect.left) * mXRatio;
        }

        mDrawFaceRect.top = mOverRect.top + (float) faceRect.top * mYRatio;
        mDrawFaceRect.bottom = mOverRect.top + (float) faceRect.bottom * mYRatio;

        return mDrawFaceRect;
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

        if (!IS_MIRROR) {
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
