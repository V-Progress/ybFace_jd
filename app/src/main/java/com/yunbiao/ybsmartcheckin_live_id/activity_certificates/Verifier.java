package com.yunbiao.ybsmartcheckin_live_id.activity_certificates;


import android.graphics.Bitmap;
import android.util.Log;

import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceSimilar;
import com.yunbiao.faceview.CertificatesView;
import com.yunbiao.ybsmartcheckin_live_id.utils.IdCardMsg;

import java.util.ArrayList;
import java.util.List;

class Verifier extends Thread {
    private static final String TAG = "Verifier";
    private IdCardMsg idCardMsg;
    private Bitmap idCardBitmap;
    private static final int MAX_FACE_FEATURE_NUM = 5;
    private List<FaceFeature> faceFeatureList = new ArrayList<>();
    private Thread compareThread = null;
    private boolean isComparing = true;
    private Thread collectThread = null;
    private boolean isCollecting = false;
    private long NO_FACE_CLEAR_DELAY = 5000;
    private OnCompareCallback compareCallback;
    private long mCacheTime = 0;
    private int mFaceId = -1;
    private CertificatesView certificatesView;


    public void initFaceView(CertificatesView certificatesView){
        this.certificatesView = certificatesView;
    }

    /***
     * 输入身份证的时候开启对比线程，并将时间重置为0
     * @param msg
     * @param bitmap
     * @param compareCallback
     */
    public void inputIDcard(IdCardMsg msg, Bitmap bitmap, OnCompareCallback compareCallback) {
        idCardMsg = msg;
        idCardBitmap = bitmap;
        mCacheTime = 0;
        if (compareCallback != null) {
            compareCallback.getIdCardMsg(idCardMsg, idCardBitmap);
        }
        startCompareThread();
    }

    public void startCollectFaceFeature(int faceId) {
        if (mFaceId != faceId) {
            mFaceId = faceId;
            faceFeatureList.clear();
            closeCollectThread();
        }
        if (faceFeatureList.size() > MAX_FACE_FEATURE_NUM) {
            return;
        }
        if (collectThread != null && collectThread.isAlive()) {
            return;
        }
        Log.e(TAG, "startCollectFaceFeature: 开启收集线程");
        isCollecting = true;
        collectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isCollecting) {
                    if (faceFeatureList.size() <= MAX_FACE_FEATURE_NUM) {
                        if (!certificatesView.hasFace()) {
                            closeCollectThread();
                            break;
                        }
                        Log.e(TAG, "run:当前特征数：" + faceFeatureList.size());
                        FaceFeature faceFeature = certificatesView.getFaceFeature();
                        if (faceFeature != null) {
                            faceFeatureList.add(faceFeature);
                        }
                    } else {
                        closeCollectThread();
                        break;
                    }
                }
            }
        });
        collectThread.start();
    }

    public void closeCollectThread() {
        if (isCollecting) {
            Log.e(TAG, "closeCollectThread: 结束收集线程");
            isCollecting = false;
            collectThread = null;
        }
    }

    private void reset() {
        Log.e(TAG, "reset: 重置状态");
        closeCollectThread();
        closeCompareThread();
        mCacheTime = 0;
        idCardMsg = null;
        idCardBitmap = null;
        compareCallback = null;
        faceFeatureList.clear();
        if (compareCallback != null) {
            compareCallback.onShutdown();
        }
    }

    public void startCompareThread() {
        Log.e(TAG, "startCompareThread: 欲开启对比线程");
        if (compareThread != null && compareThread.isAlive()) {
            Log.e(TAG, "startCompareThread: 对比线程正在运行");
            return;
        }
        Log.e(TAG, "startCompareThread: 开启对比线程");
        isComparing = true;
        compareThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isComparing) {
                    if (!certificatesView.hasFace()) {
                        if (mCacheTime == 0) {
                            mCacheTime = System.currentTimeMillis();
                        } else if (System.currentTimeMillis() - mCacheTime >= NO_FACE_CLEAR_DELAY) {
                            Log.e(TAG, "run: " + NO_FACE_CLEAR_DELAY + "毫秒内没有人脸进入，清除身份信息");
                            reset();
                            closeCompareThread();
                            break;
                        }
                        continue;
                    }

                    if (faceFeatureList.size() <= MAX_FACE_FEATURE_NUM) {
                        continue;
                    }

                    Log.e(TAG, "run: 开始对比");
                    FaceFeature idCardFeature = certificatesView.inputIdCard(idCardBitmap);

                    float finalSimilar = 0;
                    for (FaceFeature faceFeature : faceFeatureList) {
                        FaceSimilar compare = certificatesView.compare(faceFeature, idCardFeature);
                        float comapareSimilar = compare.getScore();
                        Log.e(TAG, "run: 对比:" + comapareSimilar);
                        if (comapareSimilar > finalSimilar) {
                            finalSimilar = comapareSimilar;
                        }
                    }

                    Log.e(TAG, "run: 结果回调：" + finalSimilar);

                    if (compareCallback != null) {
                        compareCallback.compareFinish(finalSimilar);
                    }

                    reset();
                    break;
                }
            }
        });
        compareThread.start();
    }

    public void closeCompareThread() {
        if (isComparing) {
            Log.e(TAG, "startCompareThread: 结束对比线程");
            isComparing = false;
            compareThread = null;
        }
    }

    public interface OnCompareCallback {
        void getIdCardMsg(IdCardMsg msg, Bitmap bitmap);

        void onShutdown();

        void compareFinish(float similar);
    }
    /*
     * 由身份证开启验证线程
     * 验证线程中循环检测人脸，如果没有人脸，则几秒后清除身份证缓存和信息
     * 有人脸以后跳过清除判断，并且开启另一个线程判断人脸特征采集情况
     * 人脸特征采集完毕，开始对比，结果返回
     *
     *
     * */

    private OnCompareCallback onCompareCallback = new OnCompareCallback() {
        @Override
        public void getIdCardMsg(IdCardMsg msg, Bitmap bitmap) {
            Log.e(TAG, "getIdCardMsg: 收到卡号：" + msg.name);
        }

        @Override
        public void onShutdown() {
            Log.e(TAG, "onShutdown: 线程被中断，清除身份证信息");
        }

        @Override
        public void compareFinish(float similar) {
            Log.e(TAG, "compareFinish: 对比结果：" + similar);
        }
    };

}
