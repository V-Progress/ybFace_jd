package com.yunbiao.ybsmartcheckin_live_id.faceview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
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
import com.jdjr.risk.face.local.user.FaceUser;
import com.jdjr.risk.face.local.verify.VerifyResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    public void init(Context context){
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
        void onFaceVerify(FaceVerifyResult verifyResult);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        CameraManager.instance().openCamera(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mFaceCanvasView.setOverlayRect(mSurfaceView.getLeft()
                , mSurfaceView.getRight()
                , mSurfaceView.getTop()
                , mSurfaceView.getBottom()
                , CameraManager.getWidth(), CameraManager.getHeight());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mFaceCanvasView.clearFaceFrame();
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



//    private CacheMap cacheMap = new CacheMap();
//    private FaceBean getCacheBean(long faceId){
//        FaceBean faceBean;
//        if(cacheMap.containsKey(faceId)){//如果包含这个Key，则取值，
//            faceBean = cacheMap.get(faceId);
//            if (faceBean == null) {
//                faceBean = new FaceBean();
//            }
//        } else {
//            faceBean = new FaceBean();
//        }
//        return faceBean;
//    }
//    private void checkFace(List<BaseProperty> list){
//        boolean isHas = false;
//        for (Map.Entry<Long, FaceBean> longFaceBeanEntry : cacheMap.entrySet()) {
//            Long key = longFaceBeanEntry.getKey();
//            for (BaseProperty baseProperty : list) {
//                if (baseProperty.getFaceId() == key) {
//                    isHas = true;
//                }
//            }
//            if(!isHas){
//                cacheMap.remove(key);
//            }
//        }
//    }
//
//    private void cacheRect(List<BaseProperty> list){
//        for (BaseProperty face : list) {
//            long faceId = face.getFaceId();
//            FaceBean cacheBean = getCacheBean(faceId);
//            final Rect cameraRect = face.getFaceRect();
//            cacheBean.faceRect = FaceBoxUtil.getPreviewBox(cameraRect);
//            cacheMap.put(faceId,cacheBean);
//        }
//        mFaceCanvasView.updateFaceBoxes(cacheMap);
//    }
//
//    private void cacheProperty(FaceProperty faceProperty){
//        Long faceId = faceProperty.getFaceId();
//        FaceBean cacheBean = getCacheBean(faceId);
//        cacheBean.age = faceProperty.getAge()+"";
//        cacheBean.sex = faceProperty.getGender()==1?"男":"女";
//        cacheMap.put(faceId,cacheBean);
//    }
//
//    private void cacheResult(VerifyResult verifyResult){
//        long faceId = verifyResult.getFaceId();
//        FaceBean cacheBean = getCacheBean(faceId);
//        cacheBean.resultCode = verifyResult.getResult();
//        FaceUser user = verifyResult.getUser();
//        if(user != null){
//            cacheBean.userId = user.getUserId();
//            cacheBean.groupId = user.getNativeGroupId();
//        }
//        cacheMap.put(faceId,cacheBean);
//    }
//
//    static class CacheMap extends LinkedHashMap<Long,FaceBean>{
//        private static final String TAG = "CacheMap";
//        private int MAX_CACHE_NUM = 3;
//        @Override
//        protected boolean removeEldestEntry(Entry eldest) {
//            return size() > MAX_CACHE_NUM;
//        }
//
//        @Override
//        public FaceBean put(Long key, FaceBean value) {
//            Log.e(TAG, "put: " + toString() );
//            return super.put(key, value);
//        }
//    }
//    class FaceBean{
//        String userId;
//        String age;
//        String sex;
//        int resultCode = -9;
//        Rect faceRect;
//        long groupId;
//
//        @Override
//        public String toString() {
//            return "FaceBean{" +
//                    "faceRect=" + faceRect +
//                    '}';
//        }
//    }
//    /*
//     * 人脸区域回调
//     * */
//    FaceFrameManager.BasePropertyCallback basePropertyCallback = new FaceFrameManager.BasePropertyCallback() {
//        @Override
//        public void onBasePropertyResult(List<BaseProperty> list) {
//            if(isPaused){
//                mFaceCanvasView.clearFaceFrame();
//                return;
//            }
//
//            if(list != null && list.size() >0 ){
//                if(callback != null){
//                    callback.onFaceDetection();
//                }
//                cacheRect(list);
//            } else {
//                onFaceLost();
    //        cacheMap.clear();
//            }
//        }
//    };
//    /*
//     * 人脸属性回调
//     * */
//    private FaceFrameManager.FacePropertyCallback facePropertyCallback = new FaceFrameManager.FacePropertyCallback() {
//        @Override
//        public void onFacePropertyResult(FaceProperty faceProperty) {
//            cacheProperty(faceProperty);
//        }
//    };
//    /*
//     * 人脸认证回调
//     * */
//    private FaceFrameManager.VerifyResultCallback verifyResultCallback = new FaceFrameManager.VerifyResultCallback() {
//        @Override
//        public void onDetectPause() {
//            if(isPaused){
//                return;
//            }
//            FaceFrameManager.resumeDetect();
//        }
//
//        @Override
//        public void onVerifyResult(VerifyResult verifyResult) {
//            if(isPaused){
//                return;
//            }
//            cacheResult(verifyResult);
//        }
//    };


    /*
     * 人脸区域回调
     * */
    FaceFrameManager.BasePropertyCallback basePropertyCallback = new FaceFrameManager.BasePropertyCallback() {
        @Override
        public void onBasePropertyResult(List<BaseProperty> list) {
            if(isPaused){
                mFaceCanvasView.clearFaceFrame();
                return;
            }
            if(list != null && list.size() >0 ){
                if(callback != null){
                    callback.onFaceDetection();
                }
                drawFaceBoxes(list);
            } else {
                onFaceLost();
            }
        }
    };

    /*
    * 人脸属性回调
    * */
    private FaceFrameManager.FacePropertyCallback facePropertyCallback = new FaceFrameManager.FacePropertyCallback() {
        @Override
        public void onFacePropertyResult(FaceProperty faceProperty) {
            cacheFaceProperty(faceProperty);
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
            long faceId = verifyResult.getFaceId();
            int resultCode = verifyResult.getResult();
            FaceUser user = verifyResult.getUser();
            mFaceImage = verifyResult.getFaceImageBytes();
            long checkConsumeTime = verifyResult.getCheckConsumeTime();
            long verifyConsumeTime = verifyResult.getVerifyConsumeTime();
            long extractConsumeTime = verifyResult.getExtractConsumeTime();

            e("检测耗时----------> " + checkConsumeTime +" 毫秒");
            e("认证耗时----------> " + verifyConsumeTime +" 毫秒");
            e("提取耗时----------> " + extractConsumeTime +" 毫秒");
            e("*******************************************************");

            handleSearchResult(faceId,resultCode,user);
        }
    };


    /***
     * ====分析认证结果==============================================================================================
     */
    private LinkedList<FaceVerifyResult> mVerifyResults = new LinkedList<>();
    private static final int TIMES_FAILURE = 3;
    //分析认证结果
    private void handleSearchResult(long faceId, int resultCode, FaceUser user) {
        final FaceVerifyResult verifyResult = new FaceVerifyResult(faceId, resultCode);
        if (user != null) {
            String userId = user.getUserId();
            verifyResult.setUserId(userId);
        }
        cacheVerifyResult(verifyResult);

        if(resultCode == VerifyResult.UNKNOWN_FACE){
            mFaceCanvasView.showProperty(false);
            cacheSuccessVerifyResult(faceId, user.getUserId());
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
            callback.onFaceVerify(verifyResult);
        }
    }


    //缓存认证结果
    private void cacheVerifyResult(FaceVerifyResult result) {
        if (mVerifyResults.size() >= TIMES_FAILURE) {
            mVerifyResults.pollFirst();
        }
        mVerifyResults.add(result);
        checkFailureResult(result);
    }
    //缓存认证失败的结果
    private void checkFailureResult(FaceVerifyResult result) {
        final int resultCode = result.getResultCode();
        if (resultCode != VerifyResult.UNKNOWN_FACE) {
            if (mVerifyResults != null && mVerifyResults.size() >= TIMES_FAILURE) {
                boolean isFailureMax = true;
                for (FaceVerifyResult verifyResult : mVerifyResults) {
                    if (verifyResult.getFaceId() != result.getFaceId() || verifyResult.getResultCode() == VerifyResult.UNKNOWN_FACE) {
                        isFailureMax = false;
                        break;
                    }
                }
                if (isFailureMax) {
                    mVerifyResults.clear();
                }
            } else {
            }
        }
    }





    /***
     * ====绘制人脸框==============================================================================================
     */

    private void drawFaceBoxes(List<BaseProperty> faces) {
        if (faces != null && faces.size() > 0) {
            for (BaseProperty face : faces) {
                transformFaceBox(face);
//                transformLandmarks(face);
                appendVerifyResult(face);
                appendFaceProperty(face);
            }
        }
        mFaceCanvasView.updateFaceBoxes(faces);
    }
    public void onFaceLost() {
        mFaceCanvasView.clearFaceFrame();
    }

    private void transformFaceBox(BaseProperty face) {
        final Rect cameraRect = face.getFaceRect();
        final Rect previewRect = FaceBoxUtil.getPreviewBox(cameraRect);
        face.setFaceRect(previewRect);
    }

    private void transformLandmarks(BaseProperty face) {
        final ArrayList<Point> landmarkList = face.getLandmarks();
        for (Point point : landmarkList) {
            if (!true) {
                point.x = 960 - point.x;
            }
//            if (!mirrorPortrait) {
//                point.y = DisplaySize.HEIGHT - point.y;
//            }

            if (!true) {
                point.x = 960 - point.x;
            }

        }
        face.setLandmarks(landmarkList);
    }
    private void appendVerifyResult(BaseProperty face) {
        final long faceId = face.getFaceId();
        final String userId = getVerifyResult(faceId);
        face.setUserId(userId);
    }
    private void appendFaceProperty(BaseProperty face) {
        if (face == null) {
            return;
        }

        final FaceProperty faceProperty = getFaceProperty(face.getFaceId());

        if (faceProperty != null) {
            final int gender = faceProperty.getGender();
            final int age = faceProperty.getAge();
            final float[] emotionScores = faceProperty.getEmotionScore();

            face.setGender(gender);
            face.setAge(age);
            face.setEmotionScores(emotionScores);
        }

    }


    /***
     * ====获取人脸属性==============================================================================================
     */
    private LinkedList<FaceVerifyResultTemp> mResultCache = new LinkedList<>();
    private static final int CAPACITY_RESULT_MAP = 13;
    //缓存认证成功结果
    private void cacheSuccessVerifyResult(long faceId, String userId) {
        if (mResultCache.size() >= CAPACITY_RESULT_MAP) {
            mResultCache.removeFirst();
        }
        final FaceVerifyResultTemp verifyResult = new FaceVerifyResultTemp(faceId, userId);
        mResultCache.add(verifyResult);
    }
    private String getVerifyResult(long faceId) {
        String userId = null;
        for (int i = mResultCache.size() - 1; i >= 0; i--) {
            final FaceVerifyResultTemp resultTemp = mResultCache.get(i);
            if (resultTemp.faceId == faceId && resultTemp.userId != null && resultTemp.userId.length() > 0) {
                userId = resultTemp.userId;
            }
        }
        return userId;
    }

    private static final int MAX_PROPERTY_SIZE = 3;
    private LinkedList<FaceProperty> mFacePropertyCache = new LinkedList<FaceProperty>();
    private void cacheFaceProperty(FaceProperty faceProperty) {
        if (mFacePropertyCache.size() >= MAX_PROPERTY_SIZE) {
            mFacePropertyCache.removeFirst();
        }
        mFacePropertyCache.add(faceProperty);
    }
    private FaceProperty getFaceProperty(long faceId) {
        for (int i = mFacePropertyCache.size() - 1; i >= 0; i--) {
            final FaceProperty faceProperty = mFacePropertyCache.get(i);
            if (faceProperty.getFaceId() == faceId) {
                return faceProperty;
            }
        }
        return null;
    }
    private class FaceVerifyResultTemp {
        public long faceId;
        public String userId;
        public FaceVerifyResultTemp(long faceId, String userId) {
            this.faceId = faceId;
            this.userId = userId;
        }
    }
    public class FaceVerifyResult {
        private long faceId;
        private int resultCode;
        public FaceVerifyResult(long faceId, int resultCode) {
            this.faceId = faceId;
            this.resultCode = resultCode;
        }
        public long getFaceId() {
            return faceId;
        }
        public void setFaceId(long faceId) {
            this.faceId = faceId;
        }
        public int getResultCode() {
            return resultCode;
        }
        public void setResultCode(int resultCode) {
            this.resultCode = resultCode;
        }
        private String userId;
        public String getUserId() {
            return userId;
        }
        public void setUserId(String userId) {
            this.userId = userId;
        }

        @Override
        public String toString() {
            return "FaceVerifyResult{" +
                    "faceId=" + faceId +
                    ", resultCode=" + resultCode +
                    ", userId='" + userId + '\'' +
                    '}';
        }
    }

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
