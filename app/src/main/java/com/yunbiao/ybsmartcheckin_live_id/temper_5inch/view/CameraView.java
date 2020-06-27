package com.yunbiao.ybsmartcheckin_live_id.temper_5inch.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.arcsoft.face.util.ImageUtils;
import com.yunbiao.faceview.CameraHelper;
import com.yunbiao.faceview.CameraListener;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.utils.NV21ToBitmap;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CameraView extends FrameLayout {

    private static final String TAG = "CameraView";

    private View surfaceView;

    private CameraHelper cameraHelper;

    private byte[] mCurrBytes;

    public CameraView(@NonNull Context context) {
        super(context);
        initView();
    }

    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        surfaceView = new TextureView(getContext());

        surfaceView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        addView(surfaceView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
    }

    private boolean isInited = false;

    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            surfaceView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            initCamera();

            isInited = true;
        }
    };

    private void initCamera() {
        Log.e(TAG, "initCamera: 打开摄像头：" + Constants.CAMERA_ID);
        int angle = SpUtils.getIntOrDef(Constants.Key.CAMERA_ANGLE, Constants.Default.CAMERA_ANGLE);
        boolean isMirror = SpUtils.getBoolean(Constants.Key.IS_H_MIRROR, true);
        cameraHelper = new CameraHelper.Builder()
                .previewViewSize(new Point(surfaceView.getMeasuredWidth(), surfaceView.getMeasuredHeight()))
                .rotation(angle)
                .specificCameraId(Constants.CAMERA_ID)
                .isMirror(isMirror)
                .previewOn(surfaceView)
                .cameraListener(cameraListener)
                .build();
        cameraHelper.init();
        cameraHelper.start();
    }

    public void changeAngle() {
        int angle = SpUtils.getIntOrDef(Constants.Key.CAMERA_ANGLE, Constants.Default.CAMERA_ANGLE);
        Log.e(TAG, "changeAngle111: " + angle);
        if (cameraHelper != null) {
            Log.e(TAG, "changeAngle222: " + angle);
            cameraHelper.changeDisplayOrientation(angle);
        }
    }

    public void resume() {
        if (cameraHelper != null && isInited) {
            Log.e(TAG, "resume: helper不为null，并且已经初始化");
            cameraHelper.start();
        } else {
            Log.e(TAG, "resume: 还未初始化");
        }
    }

    public void pause() {
        if (cameraHelper != null && isInited) {
            cameraHelper.stop();
        }
    }

    public void destroy() {
        if (cameraHelper != null) {
            cameraHelper.release();
        }
    }

    public void refresh() {
        if (isInited) {
            destroy();
            initCamera();
        }
    }

    private CameraListener cameraListener = new CameraListener() {

        @Override
        public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {

        }

        @Override
        public void onPreview(byte[] data, Camera camera) {
            mCurrBytes = data;
        }

        @Override
        public void onCameraClosed() {

        }

        @Override
        public void onCameraError(Exception e) {

        }

        @Override
        public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {

        }
    };

    public Bitmap getCurrCameraFrame() {
        if (mCurrBytes != null) {
            try {
                Bitmap bitmap = NV21ToBitmap.nv21ToBitmap2(mCurrBytes.clone(), cameraHelper.getWidth(), cameraHelper.getHeight());
                int angle = SpUtils.getIntOrDef(Constants.Key.CAMERA_ANGLE, Constants.Default.CAMERA_ANGLE);
                if (bitmap != null) {
                    int pictureRotation = SpUtils.getIntOrDef(Constants.Key.PICTURE_ROTATION, Constants.Default.PICTURE_ROTATION);
                    Log.e(TAG, "getCurrCameraFrame: 照片方向：" + pictureRotation );
                    if (pictureRotation != -1) {
                        return ImageUtils.rotateBitmap(bitmap, pictureRotation);
                    } else if (bitmap != null && angle != 0) {
                        return ImageUtils.rotateBitmap(bitmap, angle);
                    } else {
                        return bitmap;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
