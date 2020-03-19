package com.yunbiao.ybsmartcheckin_live_id.faceview.face_new;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.jdjr.risk.face.local.detect.BaseProperty;
import com.jdjr.risk.face.local.extract.FaceProperty;
import com.jdjr.risk.face.local.frame.FaceFrameManager;
import com.jdjr.risk.face.local.verify.VerifyResult;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.faceview.camera.CameraSettings;
import com.yunbiao.ybsmartcheckin_live_id.faceview.camera.ExtCameraManager;
import com.yunbiao.ybsmartcheckin_live_id.faceview.rect.FaceBoxUtil;
import com.yunbiao.ybsmartcheckin_live_id.faceview.rect.FaceFrameView;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class FaceView extends FrameLayout {
    private static final String TAG = "FaceView";
    private byte[] mFaceImage;
    private SurfaceView rgbView;
    private SurfaceView nirView;
    private boolean isInit = true;
    private FaceFrameView faceFrameView;

    public FaceView(Context context) {
        super(context);
        init(context);
    }

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    //初始化
    private void init(Context context) {
        int width = SpUtils.getInt(SpUtils.CAMERA_WIDTH);
        int height = SpUtils.getInt(SpUtils.CAMERA_HEIGHT);
        if(width == 0 || height == 0){
            CameraSettings.setCameraPreviewSize(CameraSettings.SIZE_1280_720);
        } else {
            CameraSettings.setCameraPreviewWidth(width);
            CameraSettings.setCameraPreviewHeight(height);
        }

        rgbView = new SurfaceView(getContext());
        rgbView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        addView(rgbView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,Gravity.CENTER));

        nirView = new SurfaceView(getContext());
        addView(nirView, new LayoutParams(1,1,Gravity.CENTER));

        faceFrameView = new FaceFrameView(getContext());
        addView(faceFrameView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        faceFrameView.setZOrderMediaOverlay(true);

        int angle = SpUtils.getIntOrDef(SpUtils.CAMERA_ANGLE, Constants.DEFAULT_CAMERA_ANGLE);
        CameraSettings.setCameraDisplayRotation(angle);

        ExtCameraManager instance = ExtCameraManager.instance();
        instance.setViewReadyListener(new ExtCameraManager.ViewReadyListener() {
            @Override
            public void onSurfaceReady() {
                //SDK状态
                FaceSDK.instance().configSDK();
                FaceSDK.instance().setActiveListener(new FaceSDK.SDKStateListener() {
                    @Override
                    public void onStateChanged(int state) {
                        if (state == FaceSDK.STATE_COMPLETE) {
                            if(isInit){
                                if (callback != null) {
                                    callback.onReady();
                                }
                                isInit = false;
                            }
                        }
                    }
                });
            }
        });
        instance.init(rgbView,nirView);
    }

    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener(){
        @Override
        public void onGlobalLayout() {
            //在布局完成后移除该监听（SurfaceView会不停的调用）
            rgbView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
            //初始化人脸框位置
            FaceBoxUtil.setPreviewWidth(rgbView.getLeft(),rgbView.getRight(),rgbView.getTop(),rgbView.getBottom());
        }
    };

    public byte[] getFaceImage() {
        return mFaceImage;
    }

    private FaceCallback callback;

    public void setCallback(FaceCallback callback) {
        this.callback = callback;
    }

    public interface FaceCallback {
        void onReady();

        void onFaceDetection(Boolean hasFace);

        void onFaceVerify(VerifyResult verifyResult);
    }

    private FaceFrameManager.BasePropertyCallback basePropertyCallback = new FaceFrameManager.BasePropertyCallback() {
        @Override
        public void onBasePropertyResult(Map<Long, BaseProperty> basePropertyMap) {
            boolean hasFace = basePropertyMap != null && basePropertyMap.size() > 0;
            if(!hasFace){
                mFaceImage = null;
            }

            if(faceFrameView != null){
                faceFrameView.addFace(basePropertyMap);
            }

            if (callback != null) {
                callback.onFaceDetection(hasFace);
            }
        }
    };
    /*
     * 人脸认证回调
     * */
    private FaceFrameManager.VerifyResultCallback verifyResultCallback = new FaceFrameManager.VerifyResultCallback() {
        @Override
        public void onDetectPause() {
            FaceFrameManager.resumeDetect();
        }

        @Override
        public void onVerifyResult(VerifyResult verifyResult) {
            if(verifyResult != null){
                mFaceImage = verifyResult.getFaceImageBytes();
            }
            if (callback != null) {
                callback.onFaceVerify(verifyResult);
            }

            if(faceFrameView != null){
                faceFrameView.updateResult(verifyResult.getFaceId(),verifyResult.getResult());
            }

            e(verifyResult.getResult() == VerifyResult.DEFAULT_FACE ? "未知"
                    : verifyResult.getResult() == VerifyResult.REGISTER_FACE ? "不认识"
                    : verifyResult.getResult() == VerifyResult.NOT_HUMAN_FACE ? "不是真实人脸"
                    : "认识");
            e("检测耗时----------> " + verifyResult.getCheckConsumeTime() + " 毫秒");
            e("认证耗时----------> " + verifyResult.getVerifyConsumeTime() + " 毫秒");
            e("提取耗时----------> " + verifyResult.getExtractConsumeTime() + " 毫秒");
            e("*******************************************************");
        }
    };

    public void resume() {
        FaceBoxUtil.setPreviewWidth(rgbView.getLeft(),rgbView.getRight(),rgbView.getTop(),rgbView.getBottom());
        FaceSDK.instance().setCallback(basePropertyCallback, null, verifyResultCallback);
    }

    public void pause() {
    }

    public void destory() {
        ExtCameraManager.instance().releaseAllCamera();
    }

    private boolean isLog = true;

    private void d(String log) {
        if (isLog) {
            Log.d(TAG, log);
        }
    }

    private void e(String log) {
        if (isLog) {
            Log.e(TAG, log);
        }
    }

    public void debug(boolean is) {
        isLog = is;
    }
}
