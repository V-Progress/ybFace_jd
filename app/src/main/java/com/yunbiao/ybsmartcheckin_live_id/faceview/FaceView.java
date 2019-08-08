package com.yunbiao.ybsmartcheckin_live_id.faceview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jdjr.risk.face.local.detect.BaseProperty;
import com.jdjr.risk.face.local.extract.FaceProperty;
import com.jdjr.risk.face.local.frame.FaceFrameManager;
import com.jdjr.risk.face.local.verify.VerifyResult;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class FaceView extends FrameLayout implements SurfaceHolder.Callback {
    private static final String TAG = "FaceView";
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private FaceCanvasView mFaceCanvasView;
    private boolean isPaused = false;
    private ProgressBar mCameraProgressBar;
    private Handler mainHandler = new Handler();
    private byte[] mFaceImage;
    private LinearLayout alertView;
    private CacheMap faceCacheMap = new CacheMap();

    public FaceView(Context context) {
        super(context);
        init(context);
    }

    public FaceView(Context context,AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void runOnUiThread(Runnable runnable){
        mainHandler.post(runnable);
    }

    //初始化
    private void init(Context context){
        mSurfaceView = new SurfaceView(context);
        addView(mSurfaceView,LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);

        mFaceCanvasView = new FaceCanvasView(context);
        addView(mFaceCanvasView,LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);//translucent半透明 transparent透明
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);

        mCameraProgressBar = new ProgressBar(context);
        LayoutParams layoutParams = new LayoutParams(100,100);
        layoutParams.gravity = Gravity.CENTER;
        addView(mCameraProgressBar,layoutParams);

        //相机状态
        CameraManager.instance().setStateListener(new CameraManager.CameraStateListener() {
            @Override
            public void onBeforeCamera() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCameraProgressBar.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onPreviewReady() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCameraProgressBar.setVisibility(View.GONE);
                    }
                });

                hideAlertView();
            }

            @Override
            public void onCameraError(int errCode) {
                e("onCameraError: " );
            }

            @Override
            public void onNoneCamera() {
                e("onNoneCamera: " );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAlertView("未检测到摄像头！",false,null);
                    }
                });
            }
        });

        //SDK状态
        if (FaceSDK.getState() == FaceSDK.STATE_NOT_INIT) {
//            FaceSDK.instance().init();
            FaceSDK.instance().configSDK();
            FaceSDK.instance().setActiveListener(new FaceSDK.SDKStateListener(){
                @Override
                public void onStateChanged(int state) {
                    if(state == FaceSDK.STATE_COMPLETE){
                        if(callback != null){
                            callback.onReady();
                        }
                    }
                    d("onStateChanged: -----" + state);
                }
            });
        }

    }

    public byte[] getFaceImage(){
        return mFaceImage;
    }

    public void takePhoto(CameraManager.ShotCallBack shotCallBack){
        CameraManager.instance().shot(shotCallBack);
    }

    private FaceCallback callback;
    public void setCallback(FaceCallback callback){
        this.callback = callback;
    }
    public interface FaceCallback{
        void onReady();
        void onFaceDetection();
        void onFaceVerify(VerifyResult verifyResult, FaceProperty faceProperty);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        CameraManager.instance().openCamera(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        FaceBoxUtil.setPreviewWidth(getLeft(),getRight(),getTop(),getBottom(),getWidth(),getHeight());//布局完成的时候修改预览宽高
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mFaceCanvasView.clearFaceFrame();
    }

    static class CacheMap extends LinkedHashMap<Long,FaceResult>{
        private int MAX_CACHE_NUM = 9;
        @Override
        protected boolean removeEldestEntry(Entry eldest) {
            return size() > MAX_CACHE_NUM;
        }
    }

    private FaceFrameManager.BasePropertyCallback basePropertyCallback = new FaceFrameManager.BasePropertyCallback() {
        @Override
        public void onBasePropertyResult(Map<Long, BaseProperty> basePropertyMap) {
            if(isPaused){
                mFaceCanvasView.clearFaceFrame();
                return;
            }
            if (basePropertyMap != null && basePropertyMap.values() != null && basePropertyMap.values().size() > 0) {
                if(callback != null){
                    callback.onFaceDetection();
                }
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
            if(isPaused){
                return;
            }
            FaceFrameManager.resumeDetect();
        }

        @Override
        public void onVerifyResult(VerifyResult verifyResult) {
            if(isPaused){
                return;
            }
            updateVerifyResult(verifyResult);
            mFaceImage = verifyResult.getFaceImageBytes();

            e("检测耗时----------> " + verifyResult.getCheckConsumeTime() +" 毫秒");
            e("认证耗时----------> " + verifyResult.getVerifyConsumeTime() +" 毫秒");
            e("提取耗时----------> " + verifyResult.getExtractConsumeTime() +" 毫秒");
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
        if(faceCacheMap.size() > 0){
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
        int resultCode = verifyResult.getResult();
        if(resultCode == VerifyResult.UNKNOWN_FACE){
            mFaceCanvasView.showProperty(false);
            e("handleSearchResult: 识别成功");

        } else {
            mFaceCanvasView.showProperty(true);
            if (resultCode == VerifyResult.DEFAULT_FACE) {
                e("handleSearchResult: DEFAULT_FACE");
            } else if(resultCode == VerifyResult.NOT_HUMAN_FACE){
                e("handleSearchResult: 不是真实人脸");
            } else if(resultCode == VerifyResult.REGISTER_FACE){
                e("handleSearchResult: 无法识别此人");
            } else  {
                e("handleSearchResult: 未知错误");
            }
        }

        if(callback != null){
            FaceResult faceResult = faceCacheMap.get(verifyResult.getFaceId());
            if(verifyResult != null && faceResult != null && faceResult.getFaceProperty() != null){
                callback.onFaceVerify(verifyResult,faceResult.getFaceProperty());
            }
        }
    }

    //隐藏错误提示
    private void hideAlertView(){
        if(alertView != null && alertView.isShown()){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    removeView(alertView);
                }
            });
        }
    }

    //显示错误提示
    private void showAlertView(final String alertMsg, final boolean showRetry, final OnClickListener onClickListener){
        if(alertView != null && alertView.isShown()){
            removeView(alertView);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int padding = 20;
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.bottomMargin = padding;

                alertView = new LinearLayout(getContext());
                alertView.setOrientation(LinearLayout.VERTICAL);
                alertView.setGravity(Gravity.LEFT);
                alertView.setBackgroundColor(Color.parseColor("#83272626"));
                alertView.setPadding(padding,padding,padding,padding);
                TextView tvTitle = new TextView(getContext());
                tvTitle.setTextSize(22);
                tvTitle.setTextColor(Color.parseColor("#ffffff"));
                tvTitle.setText("错误");
                alertView.addView(tvTitle,layoutParams);

                TextView alertTv = new TextView(getContext());
                alertTv.setText(alertMsg);
                alertTv.setTextSize(22);
                alertTv.setTextColor(Color.parseColor("#ffffff"));
                alertView.addView(alertTv,layoutParams);

                if(showRetry){
                    Button btn = new Button(getContext());
                    btn.setText("重试");
                    btn.setOnClickListener(onClickListener);
                    alertView.addView(btn);
                }

                LayoutParams layoutParams1 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams1.gravity = Gravity.CENTER;
                FaceView.this.addView(alertView,layoutParams1);
            }
        });

    }

    public void resume(){
        isPaused = false;
        FaceSDK.instance().setCallback(basePropertyCallback,facePropertyCallback,verifyResultCallback);
    }

    public void pause(){
        isPaused = true;
    }

    public void destory(){
        isPaused = true;
        mFaceCanvasView.clearFaceFrame();
        CameraManager.instance().onDestroy();
    }

    private boolean isLog = true;
    private void d(String log){
        if(isLog){
            Log.d(TAG,log);
        }
    }
    private void e(String log){
        if(isLog){
            Log.e(TAG,log);
        }
    }
    public void debug(boolean is){
        isLog = is;
    }
}
