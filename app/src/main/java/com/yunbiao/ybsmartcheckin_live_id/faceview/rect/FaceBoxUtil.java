package com.yunbiao.ybsmartcheckin_live_id.faceview.rect;

import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.yunbiao.ybsmartcheckin_live_id.faceview.camera.CameraSettings;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

/**
 * Created by michael on 19-3-15.
 */

public class FaceBoxUtil {
    private static final String TAG = "FaceBoxUtil";

    private static int mCameraWidth = 960;
    private static int mCameraHeight = 540;

    private static boolean IS_MIRROR;
    private static float mXRatio;
    private static float mYRatio;
    public static void setIsMirror(){
        IS_MIRROR = SpUtils.isMirror();
    }

    public static void setPreviewWidth(int l,int r,int t,int b) {
        IS_MIRROR = SpUtils.isMirror();

        mOverRect = new Rect(l,t,r,b);

        mCameraWidth = CameraSettings.getCameraWidth();
        mCameraHeight = CameraSettings.getCameraHeight();

        mXRatio = (float) mOverRect.width() / (float) mCameraWidth;
        mYRatio = (float) mOverRect.height() / (float) mCameraHeight;
        Log.e(TAG, "计算缩放比例：XRatio：" + mXRatio + "---- YRatio：" + mYRatio);
        Log.e(TAG, "计算缩放比例：mOverRect.width()：" + mOverRect.width() + "---- mOverRect.height()：" + mOverRect.height());
        Log.e(TAG, "计算缩放比例：mCameraWidth：" + mCameraWidth + "---- mCameraHeight：" + mCameraHeight);
    }

    private static RectF mDrawFaceRect = new RectF();
    private static Rect mOverRect = new Rect();

    public static RectF getRect(Rect faceRect){
        return getLandRect(faceRect);
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
}
