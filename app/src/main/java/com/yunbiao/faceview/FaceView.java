package com.yunbiao.faceview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.VersionInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.util.ImageUtils;
import com.intelligence.hardware.temperature.bean.FaceIndexInfo;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.utils.NV21ToBitmap;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import org.apache.harmony.javax.security.auth.login.LoginException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FaceView extends FrameLayout {
    private static final String TAG = "FaceView";

    private static boolean isSignleDetect = true;

    private static int MAX_DETECT_NUM = 10;
    private static int DETECT_FACE_SCALE_VAL = 16;
    /**
     * 当FR成功，活体未成功时，FR等待活体的时间
     */
    private static final int WAIT_LIVENESS_INTERVAL = 100;
    /**
     * 失败重试间隔时间（ms）
     */
    private static final long FAIL_RETRY_INTERVAL = 100;
    /**
     * 出错重试最大次数
     */
    private static final int MAX_RETRY_TIME = 5;

    private CameraHelper cameraHelper;
    private DrawHelper drawHelper;
    private Camera.Size previewSize;

    /**
     * VIDEO模式人脸检测引擎，用于预览帧人脸追踪
     */
    private FaceEngine ftEngine;
    /**
     * 用于特征提取的引擎
     */
    private FaceEngine frEngine;
    /**
     * IMAGE模式活体检测引擎，用于预览帧人脸活体检测
     */
    private FaceEngine flEngine;

    private int ftInitCode = -1;
    private int frInitCode = -1;
    private int flInitCode = -1;
    private FaceHelper faceHelper;
    private List<CompareResult> compareResultList;
    /**
     * 活体检测的开关
     */
    private boolean livenessDetect = false;

    /**
     * 用于记录人脸识别相关状态
     */
    private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();
    /**
     * 用于记录人脸特征提取出错重试次数
     */
    private ConcurrentHashMap<Integer, Integer> extractErrorRetryMap = new ConcurrentHashMap<>();
    /**
     * 用于存储活体值
     */
    private ConcurrentHashMap<Integer, Integer> livenessMap = new ConcurrentHashMap<>();

    /**
     * 用于存储活体检测出错重试次数
     */
    private ConcurrentHashMap<Integer, Integer> livenessErrorRetryMap = new ConcurrentHashMap<>();

    private CompositeDisposable getFeatureDelayedDisposables = new CompositeDisposable();
    private CompositeDisposable delayFaceTaskCompositeDisposable = new CompositeDisposable();

    /**
     * 识别阈值
     */
    private static float SIMILAR_THRESHOLD = 0.75F;

    private View previewView;

    /**
     * 绘制人脸框的控件
     */
    private FaceRectView faceRectView;
    private NV21ToBitmap nv21ToBitmap;

    public FaceView(Context context) {
        super(context);
        initView();
    }

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public FaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void setSimilarThreshold() {
        int intOrDef = SpUtils.getIntOrDef(SpUtils.SIMILAR_THRESHOLD, 80);
        Log.e(TAG, "initView: 即将设置阈值为：" + ((float) intOrDef / 100));
        SIMILAR_THRESHOLD = ((float) intOrDef / 100);
    }

    private void initView() {
        setSimilarThreshold();
        nv21ToBitmap = new NV21ToBitmap(getContext());
        compareResultList = new ArrayList<>();

        previewView = new SurfaceView(getContext());
        //在布局结束后才做初始化操作
        previewView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        addView(previewView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));

        faceRectView = new FaceRectView(getContext());
        addView(faceRectView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    private boolean isInited = false;

    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            previewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            initEngine();
            initCamera();

            isInited = true;

            if (callback != null) {
                callback.onReady();
            }
        }
    };

    private void initCamera() {
        Log.e(TAG, "initCamera: 打开摄像头：" + Constants.CAMERA_ID);
        int angle = SpUtils.getIntOrDef(SpUtils.CAMERA_ANGLE, Constants.DEFAULT_CAMERA_ANGLE);
        cameraHelper = new CameraHelper.Builder()
                .previewViewSize(new Point(previewView.getMeasuredWidth(), previewView.getMeasuredHeight()))
                .rotation(angle)
                .specificCameraId(Constants.CAMERA_ID)
                .isMirror(true)
                .previewOn(previewView)
                .cameraListener(cameraListener)
                .build();
        cameraHelper.init();
        cameraHelper.start();
    }

    private List<FacePreviewInfo> infoList;
    private byte[] mCurrBytes;

    public boolean checkFaceToFar(Rect faceRect, int distance) {
        int faceWidth = faceRect.right - faceRect.left;
        return faceWidth < distance;
    }

    public boolean checkFaceTooClose(Rect faceRect, int distance) {
        int faceHeight = faceRect.bottom - faceRect.top;
        return faceHeight >= distance;
    }

    private Rect mAreaRect = new Rect();

    public Rect getRealRect(Rect faceRect) {
        if (faceRect == null) {
            return null;
        }
        if (drawHelper == null) {
            return faceRect;
        }
        return drawHelper.adjustRect(faceRect);
    }

    private final int faceRangeCorrectValue = 50;

    public boolean checkFaceInFrame(Rect faceRect, View areaView) {
        areaView.getGlobalVisibleRect(mAreaRect);
        mAreaRect.left += faceRangeCorrectValue;
        mAreaRect.right -= faceRangeCorrectValue;
        mAreaRect.top += faceRangeCorrectValue;
        mAreaRect.bottom -= faceRangeCorrectValue;
        Rect realRect = getRealRect(faceRect);

        double widthP = ((double) (realRect.right - realRect.left) / 1280);
        double heightP = ((double) (realRect.bottom - realRect.top) / 800);

        Log.e(TAG, "checkFaceInFrame: 人脸宽比例：" + widthP + "-----人脸高比例：" + heightP);

        return realRect.contains(mAreaRect);
    }

    private boolean thermalFaceFrame = false;

    /***
     * 是否显示热成像人脸框
     * @param isThermalFaceFrame
     */
    public void enableThermalFaceFrame(boolean isThermalFaceFrame) {
        thermalFaceFrame = isThermalFaceFrame;
    }

    private Map<Integer, Float> temperHashMap = new HashMap<>();
    private List<Integer> idList = new ArrayList<>();

    public void setTemperList(ArrayList<FaceIndexInfo> arrayList) {
        if (arrayList == null) {
            Log.e(TAG, "setTemperList: list为空，清除Map");
            if (temperHashMap.isEmpty()) {
                return;
            }
            temperHashMap.clear();
        } else {
            if (!isMultiCallback) {
                idList.clear();
                for (FaceIndexInfo faceIndexInfo : arrayList) {
                    int faceId = faceIndexInfo.getFaceId();
                    float afterTreatmentF = faceIndexInfo.getAfterTreatmentF();
                    idList.add(faceId);
                    if (!temperHashMap.containsKey(faceId)) {
                        temperHashMap.put(faceId, afterTreatmentF);
                    } else {
                        Float aFloat = temperHashMap.get(faceId);
                        if (aFloat < 35.5f && afterTreatmentF >= 35.5f) {
                            temperHashMap.put(faceId, afterTreatmentF);
                        }
                    }
                }

                Iterator<Map.Entry<Integer, Float>> iterator = temperHashMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Integer, Float> next = iterator.next();
                    Integer key = next.getKey();
                    if (!idList.contains(key)) {
                        iterator.remove();
                    }
                }
            } else {
                temperHashMap.clear();
                for (FaceIndexInfo faceIndexInfo : arrayList) {
                    temperHashMap.put(faceIndexInfo.getFaceId(), faceIndexInfo.getAfterTreatmentF());
                }
            }
        }
    }

    public float getTemperByTrackId(int trackId) {
        if (!temperHashMap.containsKey(trackId)) {
            return 0.0f;
        }
        Float temper = temperHashMap.get(trackId);
        return temper;
    }

    private CameraListener cameraListener = new CameraListener() {
        @Override
        public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
            boolean isHMirror = SpUtils.isMirror();
            Log.e(TAG, "onCameraOpened: ---------- " + isHMirror);

            Camera.Size lastPreviewSize = previewSize;
            previewSize = camera.getParameters().getPreviewSize();
            drawHelper = new DrawHelper(previewSize.width, previewSize.height, previewView.getWidth(), previewView.getHeight(), displayOrientation
                    , cameraId, isMirror, isHMirror, Constants.isVerticalMirror);
            Log.i(TAG, "onCameraOpened: " + drawHelper.toString());
            Log.i(TAG, "CameraDisplayOrientation: " + drawHelper.getCameraDisplayOrientation());
            // 切换相机的时候可能会导致预览尺寸发生变化
            if (faceHelper == null ||
                    lastPreviewSize == null ||
                    lastPreviewSize.width != previewSize.width || lastPreviewSize.height != previewSize.height) {
                Integer trackedFaceCount = null;
                // 记录切换时的人脸序号
                if (faceHelper != null) {
                    trackedFaceCount = faceHelper.getTrackedFaceCount();
                    faceHelper.release();
                }
                faceHelper = new FaceHelper.Builder()
                        .ftEngine(ftEngine)
                        .frEngine(frEngine)
                        .flEngine(flEngine)
                        .frQueueSize(MAX_DETECT_NUM)
                        .flQueueSize(MAX_DETECT_NUM)
                        .imgQueueSize(MAX_DETECT_NUM)
                        .previewSize(previewSize)
                        .faceListener(faceListener)
                        .trackedFaceCount(trackedFaceCount == null ? ConfigUtil.getTrackedFaceCount(getContext().getApplicationContext()) : trackedFaceCount)
                        .build();
            }
        }

        @Override
        public void onPreview(final byte[] nv21, Camera camera) {
            mCurrBytes = nv21;
            if (faceRectView != null) {
                faceRectView.clearFaceInfo();
            }
            if (sencondFaceRectView != null) {
                sencondFaceRectView.clearFaceInfo();
            }
            List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(nv21, isSignleDetect);
            infoList = facePreviewInfoList;
            boolean hasFace = facePreviewInfoList != null && facePreviewInfoList.size() > 0;

            // TODO: 2020/3/17 这里最好还是先回调检测人脸再画框
            if (!temperHashMap.isEmpty()) {
                for (int i = 0; i < facePreviewInfoList.size(); i++) {
                    FacePreviewInfo facePreviewInfo = facePreviewInfoList.get(i);
                    int trackId = facePreviewInfo.getTrackId();
                    if (temperHashMap.containsKey(trackId)) {
                        Float temper = temperHashMap.get(trackId);
                        if (temper != null) {
                            facePreviewInfo.setTemper(temper);
                        }
                    }
                    /*if (faceIndexInfo != null) {
                        float originalTempF = faceIndexInfo.getOriginalTempF();
                        float afterTreatmentF = faceIndexInfo.getAfterTreatmentF();
                        facePreviewInfo.setTemper(aFloat);
                        facePreviewInfo.setOringinTemper(originalTempF);
                    }*/
                }
            }
            //绘制人脸框
            if (facePreviewInfoList != null && faceRectView != null && drawHelper != null) {
                drawPreviewInfo(facePreviewInfoList);
            }
            clearLeftFace(facePreviewInfoList);

            //判断单人回调和多人回调
            if (isSignleDetect) {
                if (callback != null) {
                    boolean canNext = callback.onFaceDetection(hasFace, hasFace ? facePreviewInfoList.get(0) : null);
                    if (!canNext) {
                        return;
                    }
                }
            } else {
                if (callback != null) {
                    callback.onFaceDetection(hasFace, facePreviewInfoList);
                }
            }

            if (facePreviewInfoList != null && facePreviewInfoList.size() > 0 && previewSize != null) {
                for (int i = 0; i < facePreviewInfoList.size(); i++) {
                    Integer status = requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId());
                    /**
                     * 在活体检测开启，在人脸识别状态不为成功或人脸活体状态不为处理中（ANALYZING）且不为处理完成（ALIVE、NOT_ALIVE）时重新进行活体检测
                     */

                    // TODO: 2020/3/19 陌生人
                    if (livenessDetect && (status == null || status != RequestFeatureStatus.SUCCEED || status != RequestFeatureStatus.SUCCEED_STRANGER)) {
                        Integer liveness = livenessMap.get(facePreviewInfoList.get(i).getTrackId());
                        if (liveness == null
                                || (liveness != LivenessInfo.ALIVE && liveness != LivenessInfo.NOT_ALIVE && liveness != RequestLivenessStatus.ANALYZING)) {
                            livenessMap.put(facePreviewInfoList.get(i).getTrackId(), RequestLivenessStatus.ANALYZING);
                            faceHelper.requestFaceLiveness(nv21, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId(), LivenessType.RGB);
                        }
                    }

                    /**
                     * 对于每个人脸，若状态为空或者为失败，则请求特征提取（可根据需要添加其他判断以限制特征提取次数），
                     * 特征提取回传的人脸特征结果在{@link FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer, Integer)}中回传
                     */
                    if (status == null
                            || status == RequestFeatureStatus.TO_RETRY) {
                        requestFeatureStatusMap.put(facePreviewInfoList.get(i).getTrackId(), RequestFeatureStatus.SEARCHING);
                        faceHelper.requestFaceFeature(nv21, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());
                    }
                }
            }
        }

        @Override
        public void onCameraClosed() {
            Log.i(TAG, "onCameraClosed: ");
        }

        @Override
        public void onCameraError(Exception e) {
            Log.i(TAG, "onCameraError: " + e.getMessage());
        }

        @Override
        public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
            if (drawHelper != null) {
                drawHelper.setCameraDisplayOrientation(displayOrientation);
            }
            Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
        }
    };

    final FaceListener faceListener = new FaceListener() {
        @Override
        public void onFail(Exception e) {
            Log.e(TAG, "onFail: " + e.getMessage());
        }

        //请求FR的回调
        @Override
        public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId, final Integer errorCode) {
            //FR成功
            if (faceFeature != null) {
//                    Log.i(TAG, "onPreview: fr end = " + System.currentTimeMillis() + " trackId = " + requestId);
                Integer liveness = livenessMap.get(requestId);
                //不做活体检测的情况，直接搜索
                if (!livenessDetect) {
                    searchFace(faceFeature, requestId);
                } else if (liveness != null && liveness == LivenessInfo.ALIVE) {//活体检测通过，搜索特征
                    searchFace(faceFeature, requestId);
                } else {//活体检测未出结果，或者非活体，延迟执行该函数
                    if (requestFeatureStatusMap.containsKey(requestId)) {
                        Observable.timer(WAIT_LIVENESS_INTERVAL, TimeUnit.MILLISECONDS)
                                .subscribe(new Observer<Long>() {
                                    Disposable disposable;

                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        disposable = d;
                                        getFeatureDelayedDisposables.add(disposable);
                                    }

                                    @Override
                                    public void onNext(Long aLong) {
                                        onFaceFeatureInfoGet(faceFeature, requestId, errorCode);
                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onComplete() {
                                        getFeatureDelayedDisposables.remove(disposable);
                                    }
                                });
                    }
                }

            }
            //特征提取失败
            else {
                if (increaseAndGetValue(extractErrorRetryMap, requestId) > MAX_RETRY_TIME) {
                    extractErrorRetryMap.put(requestId, 0);

                    String msg;
                    // 传入的FaceInfo在指定的图像上无法解析人脸，此处使用的是RGB人脸数据，一般是人脸模糊
                    if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
                        msg = getContext().getString(R.string.low_confidence_level);
                    } else {
                        msg = "ExtractCode:" + errorCode;
                    }
                    faceHelper.setName(requestId, getContext().getString(R.string.recognize_failed_notice, msg));
                    // 在尝试最大次数后，特征提取仍然失败，则认为识别未通过
                    requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                    retryRecognizeDelayed(requestId);
                } else {
                    requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                }
            }
        }

        @Override
        public void onFaceLivenessInfoGet(@Nullable LivenessInfo livenessInfo, final Integer requestId, Integer errorCode) {
            if (livenessInfo != null) {
                int liveness = livenessInfo.getLiveness();
                livenessMap.put(requestId, liveness);
                // 非活体，重试
                if (liveness == LivenessInfo.NOT_ALIVE) {
                    faceHelper.setName(requestId, "非活体");
                    // 延迟 FAIL_RETRY_INTERVAL 后，将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
                    retryLivenessDetectDelayed(requestId);
                }
            } else {
                if (increaseAndGetValue(livenessErrorRetryMap, requestId) > MAX_RETRY_TIME) {
                    livenessErrorRetryMap.put(requestId, 0);
                    String msg;
                    // 传入的FaceInfo在指定的图像上无法解析人脸，此处使用的是RGB人脸数据，一般是人脸模糊
                    if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
                        msg = getContext().getString(R.string.low_confidence_level);
                    } else {
                        msg = "ProcessCode:" + errorCode;
                    }
                    faceHelper.setName(requestId, getContext().getString(R.string.recognize_failed_notice, msg));
                    retryLivenessDetectDelayed(requestId);
                } else {
                    livenessMap.put(requestId, LivenessInfo.UNKNOWN);
                }
            }
        }
    };

    public void changeAngle() {
        int angle = SpUtils.getIntOrDef(SpUtils.CAMERA_ANGLE, Constants.DEFAULT_CAMERA_ANGLE);
        Log.e(TAG, "changeAngle111: " + angle);
        if (cameraHelper != null) {
            Log.e(TAG, "changeAngle222: " + angle);
            cameraHelper.changeDisplayOrientation(angle);
        }
    }

    public void resume() {
        boolean mirror = SpUtils.isMirror();
        if (drawHelper != null) {
            Log.e(TAG, "resume: ---------- " + mirror);
            drawHelper.setMirrorHorizontal(mirror);
        }

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

    public void destory() {
        unInitEngine();
        if (cameraHelper != null) {
            cameraHelper.release();
        }
    }

    private FaceCallback callback;

    public void setCallback(FaceCallback callback) {
        this.callback = callback;
    }

    private Bitmap adjustPhotoRotation1(Bitmap bm, final int orientationDegree) {
        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        try {
            Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
            return bm1;
        } catch (OutOfMemoryError ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public FaceInfo getFaceInfo(int trackId) {
        FaceInfo faceInfo = null;
        if (infoList != null && infoList.size() > 0) {
            for (int i = 0; i < infoList.size(); i++) {
                FacePreviewInfo facePreviewInfo = infoList.get(i);
                if (facePreviewInfo.getTrackId() == trackId) {
                    faceInfo = facePreviewInfo.getFaceInfo();
                }
            }
        }
        return faceInfo;
    }

    public Bitmap getPicture(int trackId) {
        FacePreviewInfo facePreviewInfo = null;
        if (infoList != null && infoList.size() > 0) {
            for (int i = 0; i < infoList.size(); i++) {
                FacePreviewInfo info = infoList.get(i);
                if (trackId == info.getTrackId()) {
                    facePreviewInfo = info;
                    break;
                }
            }
        }

        if (facePreviewInfo != null) {
            //如果info不为null，则取出头像
            FaceInfo faceInfo = facePreviewInfo.getFaceInfo();
            Rect bestRect = FaceManager.getBestRect(cameraHelper.getWidth(), cameraHelper.getHeight(), faceInfo.getRect());
            int width = bestRect.right - bestRect.left;
            int height = bestRect.bottom - bestRect.top;
            if (width <= 0 || height <= 0) {
                return getCurrCameraFrame();
            }

            byte[] clone = mCurrBytes.clone();
//            Bitmap bmp = NV21ToBitmap.nv21ToBitmap2(clone, cameraHelper.getWidth(), cameraHelper.getHeight());
            Bitmap bmp = nv21ToBitmap.nv21ToBitmap(clone, cameraHelper.getWidth(), cameraHelper.getHeight());
            Bitmap bitmap = Bitmap.createBitmap(bmp, bestRect.left, bestRect.top, width, height);
            int angle = SpUtils.getIntOrDef(SpUtils.CAMERA_ANGLE, Constants.DEFAULT_CAMERA_ANGLE);
            if (bmp != null && angle != 0) {
                Bitmap bitmap1 = ImageUtils.rotateBitmap(bmp, angle);
                return bitmap1;
            }
            return bitmap;
        }
        return null;
    }

    public Bitmap takePicture() {
        long start = System.currentTimeMillis();
        if (mCurrBytes != null) {
            Bitmap originBitmap = NV21ToBitmap.nv21ToBitmap2(mCurrBytes.clone(), cameraHelper.getWidth(), cameraHelper.getHeight());
            if (infoList != null && infoList.size() > 0) {
                FacePreviewInfo facePreviewInfo = infoList.get(0);
                FaceInfo faceInfo = facePreviewInfo.getFaceInfo();
                if (faceInfo != null) {
                    Rect bestRect = FaceManager.getBestRect(cameraHelper.getWidth(), cameraHelper.getHeight(), faceInfo.getRect());
                    int width = bestRect.right - bestRect.left;
                    int height = bestRect.bottom - bestRect.top;
                    if (width > 0 && height > 0) {
                        Bitmap bitmap = Bitmap.createBitmap(originBitmap, bestRect.left, bestRect.top, width, height);
                        int angle = SpUtils.getIntOrDef(SpUtils.CAMERA_ANGLE, Constants.DEFAULT_CAMERA_ANGLE);
                        if(originBitmap != null && !originBitmap.equals(bitmap) && !originBitmap.isRecycled()){
                            originBitmap.recycle();
                        }

                        Log.e(TAG, "getCurrCameraFrame: 截人像耗时：" + "(" + (System.currentTimeMillis() - start) + ") 毫秒");
                        if (bitmap != null && angle != 0) {
                            return ImageUtils.rotateBitmap(bitmap, angle);
                        } else {
                            return bitmap;
                        }
                    }
                }
            }
        }
        return getCurrCameraFrame();

            /*try {
                YuvImage image = new YuvImage(mCurrBytes, ImageFormat.NV21, cameraHelper.getWidth(), cameraHelper.getHeight(), null);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0, 0, cameraHelper.getWidth(), cameraHelper.getHeight()), 80, stream);
                Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                if (infoList != null && infoList.size() > 0) {
                    FacePreviewInfo facePreviewInfo = infoList.get(0);
                    FaceInfo faceInfo = facePreviewInfo.getFaceInfo();

                    if (faceInfo != null) {
                        Rect bestRect = FaceManager.getBestRect(cameraHelper.getWidth(), cameraHelper.getHeight(), faceInfo.getRect());
                        int width = bestRect.right - bestRect.left;
                        int height = bestRect.bottom - bestRect.top;
                        if (width <= 0 || height <= 0) {
                            return null;
                        }

                        Bitmap bitmap = Bitmap.createBitmap(bmp, bestRect.left, bestRect.top, width, height);
                        int angle = SpUtils.getIntOrDef(SpUtils.CAMERA_ANGLE, Constants.DEFAULT_CAMERA_ANGLE);
                        if (bmp != null && angle != 0) {
                            Bitmap bitmap1 = ImageUtils.rotateBitmap(bmp, angle);
                            return bitmap1;
                        }
                        return bitmap;
                    }
                } else {
                    return null;
                }
                stream.close();
                return bmp;
            } catch (IOException e) {
                e.printStackTrace();
            }*/
    }

    public Bitmap getCurrCameraFrame() {
        long start = System.currentTimeMillis();
        Bitmap bitmap = NV21ToBitmap.nv21ToBitmap(mCurrBytes, cameraHelper.getWidth(), cameraHelper.getHeight());
        int angle = SpUtils.getIntOrDef(SpUtils.CAMERA_ANGLE, Constants.DEFAULT_CAMERA_ANGLE);
        if (bitmap != null && angle != 0) {
            Bitmap bitmap1 = ImageUtils.rotateBitmap(bitmap, angle);
            if(bitmap != null && !bitmap.equals(bitmap1) && !bitmap.isRecycled()){
                bitmap.recycle();
            }
            Log.e(TAG, "getCurrCameraFrame: 截全屏耗时：" + "(" + (System.currentTimeMillis() - start) + ") 毫秒");
            return bitmap1;
        } else {
            return bitmap;
        }
        /*if (mCurrBytes != null) {
            try {
                YuvImage image = new YuvImage(mCurrBytes, ImageFormat.NV21, cameraHelper.getWidth(), cameraHelper.getHeight(), null);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0, 0, cameraHelper.getWidth(), cameraHelper.getHeight()), 80, stream);
                Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());

                int angle = SpUtils.getIntOrDef(SpUtils.CAMERA_ANGLE, Constants.DEFAULT_CAMERA_ANGLE);
                if (bmp != null && angle != 0) {
                    Bitmap bitmap1 = ImageUtils.rotateBitmap(bmp, angle);
                    return bitmap1;
                }
                stream.close();
                return bmp;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;*/
    }

    public interface FaceCallback {
        void onReady();

        void onFaceDetection(Boolean hasFace, List<FacePreviewInfo> facePreviewInfoList);

        boolean onFaceDetection(boolean hasFace, FacePreviewInfo facePreviewInfo);

        void onFaceVerify(CompareResult faceAuth);
    }

    /**
     * 初始化引擎
     */
    private void initEngine() {
        int angle = SpUtils.getIntOrDef(SpUtils.CAMERA_ANGLE, Constants.DEFAULT_CAMERA_ANGLE);
        DetectFaceOrientPriority orientPriority;
        if (angle == 0) {
            orientPriority = DetectFaceOrientPriority.ASF_OP_0_ONLY;
        } else {
            orientPriority = DetectFaceOrientPriority.ASF_OP_ALL_OUT;
        }

        MAX_DETECT_NUM = Constants.MAX_DETECT_NUM;
        DETECT_FACE_SCALE_VAL = Constants.DETECT_FACE_SCALE_VAL;
        Log.e(TAG, "initView: 抓取距离：" + DETECT_FACE_SCALE_VAL);

        ftEngine = new FaceEngine();
        ftInitCode = ftEngine.init(getContext(), DetectMode.ASF_DETECT_MODE_VIDEO, orientPriority,
                DETECT_FACE_SCALE_VAL, MAX_DETECT_NUM, FaceEngine.ASF_FACE_DETECT);

        frEngine = new FaceEngine();
        frInitCode = frEngine.init(getContext(), DetectMode.ASF_DETECT_MODE_IMAGE, orientPriority,
                DETECT_FACE_SCALE_VAL, MAX_DETECT_NUM, FaceEngine.ASF_FACE_RECOGNITION);

        flEngine = new FaceEngine();
        flInitCode = flEngine.init(getContext(), DetectMode.ASF_DETECT_MODE_IMAGE, orientPriority,/*ASF_OP_ALL_OUT*/
                DETECT_FACE_SCALE_VAL, MAX_DETECT_NUM, FaceEngine.ASF_LIVENESS);

        VersionInfo versionInfo = new VersionInfo();
        ftEngine.getVersion(versionInfo);
        Log.i(TAG, "initEngine:  init: " + ftInitCode + "  version:" + versionInfo);

        if (ftInitCode != ErrorInfo.MOK) {
        }
        if (frInitCode != ErrorInfo.MOK) {
        }
        if (flInitCode != ErrorInfo.MOK) {
        }
    }

    /**
     * 销毁引擎，faceHelper中可能会有特征提取耗时操作仍在执行，加锁防止crash
     */
    private void unInitEngine() {
        if (ftInitCode == ErrorInfo.MOK && ftEngine != null) {
            synchronized (ftEngine) {
                int ftUnInitCode = ftEngine.unInit();
                Log.i(TAG, "unInitEngine: " + ftUnInitCode);
            }
        }
        if (frInitCode == ErrorInfo.MOK && frEngine != null) {
            synchronized (frEngine) {
                int frUnInitCode = frEngine.unInit();
                Log.i(TAG, "unInitEngine: " + frUnInitCode);
            }
        }
        if (flInitCode == ErrorInfo.MOK && flEngine != null) {
            synchronized (flEngine) {
                int flUnInitCode = flEngine.unInit();
                Log.i(TAG, "unInitEngine: " + flUnInitCode);
            }
        }
    }

    private void drawPreviewInfo(List<FacePreviewInfo> facePreviewInfoList) {
        List<DrawInfo> drawInfoList = new ArrayList<>();
        for (int i = 0; i < facePreviewInfoList.size(); i++) {

            FacePreviewInfo facePreviewInfo = facePreviewInfoList.get(i);
            String name = faceHelper.getName(facePreviewInfo.getTrackId());
            Integer liveness = livenessMap.get(facePreviewInfo.getTrackId());
            Integer recognizeStatus = requestFeatureStatusMap.get(facePreviewInfo.getTrackId());

            // 根据识别结果和活体结果设置颜色
            int color = RecognizeColor.COLOR_UNKNOWN;
            if (recognizeStatus != null) {
                if (recognizeStatus == RequestFeatureStatus.FAILED) {
                    color = RecognizeColor.COLOR_FAILED;
                }
                if (recognizeStatus == RequestFeatureStatus.SUCCEED) {
                    color = RecognizeColor.COLOR_SUCCESS;
                }
                // TODO: 2020/3/19 增加陌生人识别
                if (recognizeStatus == RequestFeatureStatus.SUCCEED_STRANGER) {
                    color = RecognizeColor.COLOR_STRANGER;
                }
            }
            if (liveness != null && liveness == LivenessInfo.NOT_ALIVE) {
                color = RecognizeColor.COLOR_FAILED;
            }

            DrawInfo drawInfo = new DrawInfo(drawHelper.adjustRect(facePreviewInfo.getFaceInfo().getRect()),
                    GenderInfo.UNKNOWN, AgeInfo.UNKNOWN_AGE, liveness == null ? LivenessInfo.UNKNOWN : liveness, color,
                    String.valueOf(facePreviewInfoList.get(i).getTrackId())
                    , facePreviewInfo.getTemper(), facePreviewInfo.getOringinTemper());
            drawInfo.setThermalFaceFrame(thermalFaceFrame);//设置是否显示人脸框（仅第二人脸框有效）
            drawInfoList.add(drawInfo);
        }
        drawHelper.draw(faceRectView, drawInfoList);

        if (sencondFaceRectView != null && isDrawSecondFaceRect) {
            drawHelper.draw2(sencondFaceRectView, drawInfoList);
        }
    }

    public void enableMutiple(boolean isEnable) {
        isSignleDetect = !isEnable;
    }

    public void setLiveness(boolean isChecked) {
        livenessDetect = isChecked;
        if (drawHelper != null) {
            drawHelper.setLivnessEnable(isChecked);
        }
    }

    public boolean getLiveness() {
        return livenessDetect;
    }

    private SecondFaceRectView sencondFaceRectView;

    private boolean isDrawSecondFaceRect = true;

    public void setDrawSecondFaceRect(boolean isDraw) {
        isDrawSecondFaceRect = isDraw;
    }

    public void setSecondFaceRectView(SecondFaceRectView view) {
        Log.e(TAG, "setSecondFaceRectView: 设置第二个人脸框");
        sencondFaceRectView = view;
    }

    //是否执行多次重试，多次重试时不执行延时，默认100毫秒
    private boolean isMultiRetry = true;
    //是否多次回调
    private boolean isMultiCallback = false;
    //重试次数
    private int retryTimes = 2;
    //重试延迟，只在识别成功或不进行重试次数的情况下使用
    private long retryDelayTime = 1000;

    public void enableMultiRetry(boolean isRetry) {
        isMultiRetry = isRetry;
    }

    public void enableMultiCallback(boolean multiCallback) {
        isMultiCallback = multiCallback;
    }

    public void setRetryTime(int time) {
        retryTimes = time;
    }

    public void setRetryDelayTime(long delay) {
        retryDelayTime = delay;
    }

    /**
     * 删除已经离开的人脸
     *
     * @param facePreviewInfoList 人脸和trackId列表
     */
    private void clearLeftFace(List<FacePreviewInfo> facePreviewInfoList) {
        if (compareResultList != null) {
            for (int i = compareResultList.size() - 1; i >= 0; i--) {
                if (!requestFeatureStatusMap.containsKey(compareResultList.get(i).getTrackId())) {
                    compareResultList.remove(i);
                }
            }
        }

        if (facePreviewInfoList == null || facePreviewInfoList.size() == 0) {
            requestFeatureStatusMap.clear();
            livenessMap.clear();
            livenessErrorRetryMap.clear();
            extractErrorRetryMap.clear();
            if (getFeatureDelayedDisposables != null) {
                getFeatureDelayedDisposables.clear();
            }
            return;
        }
        Enumeration<Integer> keys = requestFeatureStatusMap.keys();
        while (keys.hasMoreElements()) {
            int key = keys.nextElement();
            boolean contained = false;
            for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
                if (facePreviewInfo.getTrackId() == key) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                requestFeatureStatusMap.remove(key);
                livenessMap.remove(key);
                livenessErrorRetryMap.remove(key);
                extractErrorRetryMap.remove(key);
            }
        }
    }

    private void searchFace(final FaceFeature frFace, final Integer requestId) {
        Observable.create(new ObservableOnSubscribe<CompareResult>() {
            @Override
            public void subscribe(final ObservableEmitter<CompareResult> emitter) {
                requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SEARCHING);
                CompareResult compareResult = FaceManager.getInstance().compare(frFace);
                emitter.onNext(compareResult);
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CompareResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CompareResult compareResult) {
                        if (compareResult == null || compareResult.getUserName() == null) {
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                            faceHelper.setName(requestId, "VISITOR " + requestId);
                            return;
                        }

                        if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
                            boolean isAdded = false;
                            if (compareResultList == null) {
                                requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                                faceHelper.setName(requestId, "VISITOR " + requestId);
                                return;
                            }
                            for (CompareResult compareResult1 : compareResultList) {
                                if (compareResult1.getTrackId() == requestId) {
                                    isAdded = true;
                                    break;
                                }
                            }
                            if (!isAdded) {
                                //对于多人脸搜索，假如最大显示数量为 MAX_DETECT_NUM 且有新的人脸进入，则以队列的形式移除
                                if (compareResultList.size() >= MAX_DETECT_NUM) {
                                    compareResultList.remove(0);
                                }
                                //添加显示人员时，保存其trackId
                                compareResult.setTrackId(requestId);
                                compareResultList.add(compareResult);
                            }
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED);

                            String userName = compareResult.getUserName();
                            if (callback != null) {
                                callback.onFaceVerify(compareResult);
                            }
                            faceHelper.setName(requestId, "ID: " + userName);

                            //是否多次回调
                            if (isMultiCallback) {
                                retryRecognizeDelayed(requestId, retryDelayTime);
                            }
                        } else {
                            faceHelper.setName(requestId, "未注册");
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED_STRANGER);

                            //不进行多次重试则直接回调结果
                            if (!isMultiRetry) {
                                //回调识别失败的结果，说明是陌生人
                                if (callback != null) {
                                    CompareResult cr = new CompareResult("-1", 0.0f);
                                    cr.setTrackId(requestId);
                                    callback.onFaceVerify(cr);
                                }
                                //是否多次回调
                                if (isMultiCallback) {
                                    retryRecognizeDelayed(requestId, retryDelayTime);
                                }
                                return;
                            }

                            //进行多次重试,判断重试map里是否包含这个人，如果不包含则添加，然后重试
                            if (!strangerRetryMap.containsKey(requestId)) {
                                strangerRetryMap.put(requestId, 0);
                            }

                            //进行多次重试，取出map中的数进行判断，如果小于次数则继续重试
                            int retryNum = strangerRetryMap.get(requestId);
                            if (retryNum < retryTimes) {
                                retryNum += 1;
                                strangerRetryMap.put(requestId, retryNum);
                                retryRecognizeDelayed(requestId);
                                return;
                            }

                            //重试次数足够，进行回调
                            if (callback != null) {
                                CompareResult cr = new CompareResult("-1", 0.0f);
                                cr.setTrackId(requestId);
                                callback.onFaceVerify(cr);
                            }

                            //回调结束后删除这个人
                            strangerRetryMap.remove(requestId);

                            //如果多次回调则删除map中的数并开启重试
                            if (isMultiCallback) {
                                retryRecognizeDelayed(requestId);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        faceHelper.setName(requestId, "检测失败：" + (e == null ? "NULL" : e.getMessage()));
                        retryRecognizeDelayed(requestId);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    Map<Integer, Integer> strangerRetryMap = new HashMap<>();

    /**
     * 将map中key对应的value增1回传
     *
     * @param countMap map
     * @param key      key
     * @return 增1后的value
     */
    public int increaseAndGetValue(Map<Integer, Integer> countMap, int key) {
        if (countMap == null) {
            return 0;
        }
        Integer value = countMap.get(key);
        if (value == null) {
            value = 0;
        }
        countMap.put(key, ++value);
        return value;
    }

    /**
     * 延迟 FAIL_RETRY_INTERVAL 重新进行活体检测
     *
     * @param requestId 人脸ID
     */
    private void retryLivenessDetectDelayed(final Integer requestId) {
        Observable.timer(FAIL_RETRY_INTERVAL, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Long>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        delayFaceTaskCompositeDisposable.add(disposable);
                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        // 将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
                        if (livenessDetect) {
                            faceHelper.setName(requestId, Integer.toString(requestId));
                        }
                        livenessMap.put(requestId, LivenessInfo.UNKNOWN);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }

    private void retryRecognizeDelayed(final Integer requestId, long delay) {
        Observable.timer(delay, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Long>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        delayFaceTaskCompositeDisposable.add(disposable);
                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        // 将该人脸特征提取状态置为FAILED，帧回调处理时会重新进行活体检测
                        faceHelper.setName(requestId, Integer.toString(requestId));
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }

    /**
     * 延迟 FAIL_RETRY_INTERVAL 重新进行人脸识别
     *
     * @param requestId 人脸ID
     */
    private void retryRecognizeDelayed(final Integer requestId) {
        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
        Observable.timer(FAIL_RETRY_INTERVAL, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Long>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        delayFaceTaskCompositeDisposable.add(disposable);
                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        // 将该人脸特征提取状态置为FAILED，帧回调处理时会重新进行活体检测
                        faceHelper.setName(requestId, Integer.toString(requestId));
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }

}
