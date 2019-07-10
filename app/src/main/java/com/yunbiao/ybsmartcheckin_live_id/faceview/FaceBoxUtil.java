package com.yunbiao.ybsmartcheckin_live_id.faceview;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

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
    private static int mOrientation = 1;
    static {
        mOrientation = APP.getContext().getResources().getConfiguration().orientation;
    }
    public static void setIsMirror(){
        IS_MIRROR = SpUtils.isMirror();
    }

    public static void setPreviewWidth(int l,int r,int t,int b,float previewWidth,float previewHeight) {
        IS_MIRROR = SpUtils.isMirror();

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

    public static RectF getRect(Rect faceRect){
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            return getPortraitRect(faceRect);
        } else {
            return getLandRect(faceRect);
        }
    }

    private static RectF getLandRect(Rect faceRect) {
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

    private static RectF getPortraitRect(Rect cameraBox) {
        final int cameraImageWidth = CameraManager.getWidth();
        final int cameraImageHeight = CameraManager.getHeight();

        final float scaleX = previewWidth / cameraImageWidth;
        final float scaleY = previewHeight / cameraImageHeight;
        
        int scaleLeft = (int) (cameraBox.left * scaleX);
        int scaleTop = (int) (cameraBox.top * scaleY);
        int scaleRight = (int) (cameraBox.right * scaleX);
        int scaleBottom = (int) (cameraBox.bottom * scaleY);

        int finalDisplayLeft = scaleLeft;
        int finalDisplayRight = scaleRight;
        int finalDisplayTop = scaleTop;
        int finalDisplayBottom = scaleBottom;

        if (!IS_MIRROR) {
            finalDisplayLeft = (int) (previewWidth - scaleRight);
            finalDisplayRight = (int) (previewWidth - scaleLeft);
        }

        mDrawFaceRect.left = finalDisplayLeft;
        mDrawFaceRect.right = finalDisplayRight;
        mDrawFaceRect.top = finalDisplayTop;
        mDrawFaceRect.bottom = finalDisplayBottom;
        return mDrawFaceRect;
    }

}
