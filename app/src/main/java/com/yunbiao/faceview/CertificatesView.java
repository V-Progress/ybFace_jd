package com.yunbiao.faceview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.util.ImageUtils;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CertificatesView extends FrameLayout {
    private static final String TAG = "FaceView";

    private CameraHelper cameraHelper;
    private DrawHelper drawHelper;
    private Camera.Size previewSize;
    private Integer rgbCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
    private boolean livenessDetect = false;
    public static float SIMILAR_THRESHOLD = 0.75F;

    private View previewView;
    private FaceRectView faceRectView;

    private FaceEngine imgDetectEngin;
    private FaceEngine videoDetectEngin;
    private FaceEngine frEngine;
    private FaceEngine compareEngin;
    private FaceEngine flEngine;

    public CertificatesView(Context context) {
        super(context);
        initView();
    }

    public CertificatesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CertificatesView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        int angle = SpUtils.getIntOrDef(SpUtils.CAMERA_ANGLE, Constants.DEFAULT_CAMERA_ANGLE);
        cameraHelper = new CameraHelper.Builder()
                .previewViewSize(new Point(previewView.getMeasuredWidth(), previewView.getMeasuredHeight()))
                .rotation(angle)
                .specificCameraId(rgbCameraID != null ? rgbCameraID : Camera.CameraInfo.CAMERA_FACING_FRONT)
                .isMirror(true)
                .previewOn(previewView)
                .cameraListener(cameraListener)
                .build();
        cameraHelper.init();
        cameraHelper.start();
    }

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
        if (drawHelper == null) {
            return null;
        }
        return drawHelper.adjustRect(faceRect);
    }

    public boolean checkFaceInFrame(Rect faceRect, View areaView, RectCallback rectCallback) {
        areaView.getGlobalVisibleRect(mAreaRect);
        mAreaRect.left -= 50;
        mAreaRect.right += 50;
        mAreaRect.top -= 50;
        mAreaRect.bottom += 50;
        Rect rect = drawHelper.adjustRect(faceRect);
        rectCallback.onAreaRect(mAreaRect, rect);
        return mAreaRect.contains(rect);
    }

    public boolean checkFaceInFrame(Rect faceRect, View areaView) {
        areaView.getGlobalVisibleRect(mAreaRect);
        mAreaRect.left -= 20;
        mAreaRect.right += 20;
        mAreaRect.top -= 20;
        mAreaRect.bottom += 20;
        Rect rect = drawHelper.adjustRect(faceRect);
        return mAreaRect.contains(rect);
    }

    public boolean checkFaceInFrame2(Rect faceRect, View areaView) {
        areaView.getGlobalVisibleRect(mAreaRect);
        mAreaRect.left += 20;
        mAreaRect.right -= 20;
        mAreaRect.top += 20;
        mAreaRect.bottom -= 20;
        return faceRect.contains(mAreaRect);
    }

    public interface RectCallback {
        void onAreaRect(Rect mAreaRect, Rect mFaceRect);
    }

    public interface FaceCallback {
        void onReady();

        void onFaceDetection(boolean hasFace, FaceInfo FaceInfo);
    }

    public Bitmap getFaceBitmap(FaceInfo faceInfo) {
        if (faceInfo != null && mCurrBytes != null) {
            try {
                Bitmap bitmap = null;
                YuvImage image = new YuvImage(mCurrBytes, ImageFormat.NV21, cameraHelper.getWidth(), cameraHelper.getHeight(), null);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0, 0, cameraHelper.getWidth(), cameraHelper.getHeight()), 80, stream);
                Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                Rect bestRect = FaceManager.getBestRect(cameraHelper.getWidth(), cameraHelper.getHeight(), faceInfo.getRect());
                int width = bestRect.right - bestRect.left;
                int height = bestRect.bottom - bestRect.top;
                if (width <= 0 || height <= 0) {
                    return null;
                }
                bitmap = Bitmap.createBitmap(bmp, bestRect.left, bestRect.top, width, height);
                int angle = SpUtils.getIntOrDef(SpUtils.CAMERA_ANGLE, Constants.DEFAULT_CAMERA_ANGLE);
                if (bmp != null && angle != 0) {
                    bitmap = ImageUtils.rotateBitmap(bmp, angle);
                }
                stream.close();
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //无人时清除证件照和数据的延时
    //对比完成后清除数据的延时

    private List<FaceInfo> faceInfoList;
    private long mCacheGetFaceBitmapTime = 0;
    private long mCacheCompareTime = 0;
    private final long GET_FACE_BITMAP_DELAY_TIME = 1000;
    private final long COMPARE_DELAY_TIME = 10000;

    public synchronized FaceFeature inputIdCard(Bitmap bmp) {
        if (bmp == null) {
            return null;
        }
        //裁剪图片为合适的尺寸
        Bitmap bitmap = ArcSoftImageUtil.getAlignedBitmap(bmp, true);
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Log.e(TAG, "inputIdCard: 裁剪后：" + width + "---" + height);

        //创建等同于bitmap大小的byte[]
        byte[] bgr24 = ArcSoftImageUtil.createImageData(width, height, ArcSoftImageFormat.BGR24);
        int translateResult = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
        if (translateResult != ArcSoftImageUtilError.CODE_SUCCESS) {
            return null;
        }
        List<FaceInfo> faceInfoList = new ArrayList<>();
        int detectResult = imgDetectEngin.detectFaces(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList);
        if (detectResult != ErrorInfo.MOK && faceInfoList.size() > 0) {
            return null;
        }

        FaceFeature faceFeature = new FaceFeature();
        if(faceInfoList.size() <= 0){
            return null;
        }
        int i = frEngine.extractFaceFeature(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList.get(0), faceFeature);
        if (i != ErrorInfo.MOK) {
            return null;
        }

        return faceFeature;
    }

    public synchronized FaceFeature getFaceFeature() {
        if(faceInfoList != null && faceInfoList.size() > 0){
            synchronized (this){
                if(faceInfoList != null && faceInfoList.size() > 0){
                    try{
                        FaceInfo faceInfo = faceInfoList.get(0);
                        //提取特征
                        FaceFeature faceFeature = new FaceFeature();
                        frEngine.extractFaceFeature(mCurrBytes, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfo, faceFeature);
                        return faceFeature;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    private CameraListener cameraListener = new CameraListener() {

        @Override
        public void onPreview(final byte[] nv21, Camera camera) {
            mCurrBytes = nv21;
            if (faceRectView != null) {
                faceRectView.clearFaceInfo();
            }

            faceInfoList = new ArrayList<>();
            videoDetectEngin.detectFaces(nv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList);

            FaceInfo faceInfo = null;
            if (faceInfoList.size() > 0) {
                faceInfo = faceInfoList.get(0);
            }

            if (callback != null) {
                callback.onFaceDetection(faceInfo != null, faceInfo);
            }

            if (faceInfo != null) {
                Rect rect = faceInfo.getRect();
                drawHelper.draw(faceRectView, new DrawInfo(drawHelper.adjustRect(rect), 0, 0, 0, Color.YELLOW, ""));
            }
        }

        @Override
        public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
            boolean isHMirror = SpUtils.isMirror();
            Log.e(TAG, "onCameraOpened: ---------- " + isHMirror);

            Camera.Size lastPreviewSize = previewSize;
            previewSize = camera.getParameters().getPreviewSize();
            drawHelper = new DrawHelper(previewSize.width, previewSize.height, previewView.getWidth(), previewView.getHeight(), displayOrientation
                    , cameraId, isMirror, isHMirror, false);
            Log.i(TAG, "onCameraOpened: " + drawHelper.toString());
            Log.i(TAG, "CameraDisplayOrientation: " + drawHelper.getCameraDisplayOrientation());
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

    public boolean hasFace() {
        return faceInfoList != null && faceInfoList.size() > 0;
    }

    public int getLivenessInfo() {
        if (faceInfoList != null && faceInfoList.size() > 0) {
            FaceInfo faceInfo = faceInfoList.get(0);
            //提取特征
            FaceFeature faceFeature = new FaceFeature();
            int process = flEngine.process(mCurrBytes, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, Arrays.asList(faceInfo), FaceEngine.ASF_LIVENESS);
            if (process == ErrorInfo.MOK) {
                List<LivenessInfo> livenessInfos = new ArrayList<>();
                int liveness = flEngine.getLiveness(livenessInfos);
                if (liveness == ErrorInfo.MOK && livenessInfos.size() > 0) {
                    LivenessInfo livenessInfo = livenessInfos.get(0);
                    return livenessInfo.getLiveness();
                }
            }
        }
        return -99;
    }

    public FaceSimilar compare(FaceFeature faceFeature, FaceFeature faceFeature1) {
        FaceSimilar faceSimilar = new FaceSimilar();
        compareEngin.compareFaceFeature(faceFeature, faceFeature1, faceSimilar);
        return faceSimilar;
    }

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

    public Bitmap getCurrCameraFrame() {
        if (mCurrBytes != null) {
            try {
                byte[] clone = mCurrBytes.clone();
                YuvImage image = new YuvImage(clone, ImageFormat.NV21, cameraHelper.getWidth(), cameraHelper.getHeight(), null);
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
        return null;
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

    public Bitmap takePicture() {
        if (mCurrBytes != null) {
            try {
                YuvImage image = new YuvImage(mCurrBytes, ImageFormat.NV21, cameraHelper.getWidth(), cameraHelper.getHeight(), null);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0, 0, cameraHelper.getWidth(), cameraHelper.getHeight()), 80, stream);
                Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                if (faceInfoList != null && faceInfoList.size() > 0) {
                    FaceInfo faceInfo = faceInfoList.get(0);
                    if (faceInfo != null) {
                        Rect bestRect = FaceManager.getBestRect(cameraHelper.getWidth(), cameraHelper.getHeight(), faceInfo.getRect());
                        Bitmap bitmap = Bitmap.createBitmap(bmp, bestRect.left, bestRect.top, bestRect.right - bestRect.left, bestRect.bottom - bestRect.top);
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
            }
        }
        return null;
    }

    public void setLiveness(boolean isChecked) {
        livenessDetect = isChecked;
    }

    public boolean getLiveness() {
        return livenessDetect;
    }

    public interface Face_IdCard_CompareListener {
        void onFaceDetection(boolean hasFace, FaceInfo faceInfo);

        void onDetectionFace();

    }

    /**
     * 初始化引擎
     */
    private void initEngine() {
        videoDetectEngin = new FaceEngine();
        videoDetectEngin.init(getContext(), DetectMode.ASF_DETECT_MODE_VIDEO, DetectFaceOrientPriority.ASF_OP_ALL_OUT,
                16, 1, FaceEngine.ASF_FACE_DETECT);

        imgDetectEngin = new FaceEngine();
        imgDetectEngin.init(getContext(), DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_ALL_OUT,
                16, 1, FaceEngine.ASF_FACE_DETECT);

        frEngine = new FaceEngine();
        frEngine.init(getContext(), DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_ALL_OUT,
                16, 1, FaceEngine.ASF_FACE_RECOGNITION);

        compareEngin = new FaceEngine();
        compareEngin.init(getContext(), DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY, 16, 1, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT);

        flEngine = new FaceEngine();
        flEngine.init(getContext(), DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_ALL_OUT,
                16, 1, FaceEngine.ASF_LIVENESS);

    }

    /**
     * 销毁引擎，faceHelper中可能会有特征提取耗时操作仍在执行，加锁防止crash
     */
    private void unInitEngine() {

    }
}
