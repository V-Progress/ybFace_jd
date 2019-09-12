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
import com.yunbiao.ybsmartcheckin_live_id.faceview.camera.CameraSettings;
import com.yunbiao.ybsmartcheckin_live_id.faceview.camera.ExtCameraManager;
import com.yunbiao.ybsmartcheckin_live_id.faceview.rect.FaceBoxUtil;
import com.yunbiao.ybsmartcheckin_live_id.faceview.rect.FaceCanvasView;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class FaceView extends FrameLayout {
    private static final String TAG = "FaceView";
    private FaceCanvasView mFaceCanvasView;
    private byte[] mFaceImage;
    private CacheMap faceCacheMap = new CacheMap();
    private SurfaceView rgbView;
    private SurfaceView nirView;
    private boolean isInit = true;

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

//        float w = CameraSettings.getCameraPreviewWidth();
//        float h = CameraSettings.getCameraPreviewHeight();
//
//        if(w > h){
//            float ratio = w / h;
//            h = w;
//            w = h * ratio;
//        }

        rgbView = new SurfaceView(getContext());
        rgbView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        addView(rgbView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,Gravity.CENTER));

        nirView = new SurfaceView(getContext());
        addView(nirView, new LayoutParams(1,1,Gravity.CENTER));

        mFaceCanvasView = new FaceCanvasView(context);
        addView(mFaceCanvasView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        int angle = SpUtils.getIntOrDef(SpUtils.CAMERA_ANGLE,CameraSettings.ROTATION_0);
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

        void onFaceDetection(FaceResult basePropertyMap);

        void onFaceVerify(VerifyResult verifyResult);
    }

    static class CacheMap extends LinkedHashMap<Long, FaceResult> {
        private int MAX_CACHE_NUM = 9;

        @Override
        protected boolean removeEldestEntry(Entry eldest) {
            return size() > MAX_CACHE_NUM;
        }
    }

    private FaceFrameManager.BasePropertyCallback basePropertyCallback = new FaceFrameManager.BasePropertyCallback() {
        @Override
        public void onBasePropertyResult(Map<Long, BaseProperty> basePropertyMap) {
            if (basePropertyMap != null && basePropertyMap.values() != null && basePropertyMap.values().size() > 0) {
                drawFaceBoxes(basePropertyMap);
            } else {
                onFaceLost();
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
            updateVerifyResult(verifyResult);
            mFaceImage = verifyResult.getFaceImageBytes();

            if(verifyResult.getResult() == VerifyResult.DEFAULT_FACE){
                e("未知");
            } else if(verifyResult.getResult() == VerifyResult.REGISTER_FACE){
                e("不认识");
            } else if(verifyResult.getResult() == VerifyResult.NOT_HUMAN_FACE){
                e("不是真实人脸");
            } else {
                e("认识");
            }

            e("检测耗时----------> " + verifyResult.getCheckConsumeTime() + " 毫秒");
            e("认证耗时----------> " + verifyResult.getVerifyConsumeTime() + " 毫秒");
            e("提取耗时----------> " + verifyResult.getExtractConsumeTime() + " 毫秒");
            e("*******************************************************");

            handleSearchResult(verifyResult);
        }
    };

    /*
     * 人脸属性回调
     * */
    private FaceFrameManager.FacePropertyCallback facePropertyCallback = new FaceFrameManager.FacePropertyCallback() {
        @Override
        public void onFacePropertyResult(FaceProperty faceProperty) {
            updateFaceProperty(faceProperty);
        }
    };

    //无人脸
    private void onFaceLost() {
        mFaceImage = null;
        mFaceCanvasView.clearFaceFrame();
    }

    //绘制人脸框
    private void drawFaceBoxes(Map<Long, BaseProperty> basePropertyMap) {
        if (basePropertyMap == null || basePropertyMap.values() == null || basePropertyMap.values().size() == 0) {
            return;
        }

        //删除不可见的人脸
        if (faceCacheMap.size() > 0) {
            final Set<Long> oldFaceIds = faceCacheMap.keySet();
            final Set<Long> finalFaceIds = new HashSet<>();
            for (long faceId : oldFaceIds) {
                finalFaceIds.add(faceId);
            }
            for (long finalFaceId : finalFaceIds) {
                if (!basePropertyMap.containsKey(finalFaceId)) {
                    faceCacheMap.remove(finalFaceId);
                }
            }
        }

        // 更新可见的人脸
        for (BaseProperty baseProperty : basePropertyMap.values()) {
            final long faceId = baseProperty.getFaceId();
            if (!faceCacheMap.containsKey(faceId)) {
                faceCacheMap.put(faceId, new FaceResult(baseProperty));
            } else {
                faceCacheMap.get(faceId).setBaseProperty(baseProperty);
            }
        }
        mFaceCanvasView.updateFaceBoxes(faceCacheMap);

        for (Long aLong : faceCacheMap.keySet()) {
            FaceResult faceResult = faceCacheMap.get(aLong);
            if (callback != null) {
                callback.onFaceDetection(faceResult);
            }
        }

    }

    //更新人脸认证信息
    private void updateVerifyResult(VerifyResult verifyResult) {
        if (faceCacheMap == null) {
            return;
        }
        final long faceId = verifyResult.getFaceId();
        if (faceCacheMap.containsKey(faceId)) {
            faceCacheMap.get(faceId).setVerifyResult(verifyResult);
        }
    }

    //更新人脸属性
    private void updateFaceProperty(FaceProperty faceProperty) {
        if (faceCacheMap == null) {
            return;
        }

        final long faceId = faceProperty.getFaceId();
        if (faceCacheMap.containsKey(faceId)) {
            faceCacheMap.get(faceId).setFaceProperty(faceProperty);
        }
    }

    //处理人脸认证
    private void handleSearchResult(VerifyResult verifyResult) {
        if(verifyResult == null) return;

        int resultCode = verifyResult.getResult();
        // TODO: 2019/7/28
        if (resultCode == VerifyResult.UNKNOWN_FACE) {
            mFaceCanvasView.showProperty(false);
        } else {
            mFaceCanvasView.showProperty(true);
        }

        if (callback != null) {
            callback.onFaceVerify(verifyResult);
        }
    }

    public void resume() {
        FaceBoxUtil.setPreviewWidth(rgbView.getLeft(),rgbView.getRight(),rgbView.getTop(),rgbView.getBottom());
        FaceSDK.instance().setCallback(basePropertyCallback, facePropertyCallback, verifyResultCallback);
    }

    public void pause() {
    }

    public void destory() {
        mFaceCanvasView.clearFaceFrame();
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
