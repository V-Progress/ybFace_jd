package com.yunbiao.ybsmartcheckin_live_id.thermal_imaging;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.callback.HotImageK3232CallBack;
import com.intelligence.hardware.temperature.callback.InfraredTempCallBack;
import com.yunbiao.faceview.CompareResult;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseGpioActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseThermalActivity extends BaseGpioActivity {

    private long mSpeechDelay;
    private int mCurrMode = -99;
    private Float mTempMinThreshold;
    private Float mTempWarningThreshold;
    private Float mAmbCorrValue;
    private Float mTempCorrValue;
    private int mCurrBodyMinT;
    private int mCurrBodyMaxT;
    private int mCurrBodyPercent;
    private boolean mThermalImgMirror;

    private float mLowestTemp = 0.0f;
    private float mHighestTemp = 45.0f;
    private boolean lowTempModel;
    private float ambient;
    private float mThermalCorrect;

    @Override
    protected void initData() {
        super.initData();

        /*if (mCurrMode == ThermalConst.THERMAL_TEMP_ONLY || mCurrMode == ThermalConst.THERMAL_FACE_TEMP) {
            TemperatureModule.getIns().initSerialPort(this, "/dev/ttyS4", 115200);
        } else {
            TemperatureModule.getIns().initSerialPort(this, "/dev/ttyS3", 9600);
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSpeechDelay = SpUtils.getLong(SpUtils.SPEECH_DELAY, Constants.DEFAULT_SPEECH_DELAY);//播报延时
        mTempMinThreshold = SpUtils.getFloat(SpUtils.TEMP_MIN_THRESHOLD, Constants.DEFAULT_TEMP_MIN_THRESHOLD_VALUE); //测温最小阈值
        mTempWarningThreshold = SpUtils.getFloat(SpUtils.TEMP_WARNING_THRESHOLD, Constants.DEFAULT_TEMP_WARNING_THRESHOLD_VALUE); //测温报警阈值
        mThermalImgMirror = SpUtils.getBoolean(SpUtils.THERMAL_IMAGE_MIRROR, Constants.DEFAULT_THERMAL_IMAGE_MIRROR);//热成像图像镜像
        mCurrBodyMinT = SpUtils.getIntOrDef(SpUtils.BODY_MIN_T, Constants.DEFAULT_BODY_MIN_T_VALUE);//最低体温值
        mCurrBodyMaxT = SpUtils.getIntOrDef(SpUtils.BODY_MAX_T, Constants.DEFAULT_BODY_MAX_T_VALUE);//最高体温值
        mCurrBodyPercent = SpUtils.getIntOrDef(SpUtils.BODY_PERCENT, Constants.DEFAULT_BODY_PERCENT_VALUE);//人体百分比
        int currMode = SpUtils.getIntOrDef(SpUtils.THERMAL_MODEL_SETTING, ThermalConst.DEFAULT_THERMAL_MODEL);//当前模式
        lowTempModel = SpUtils.getBoolean(SpUtils.LOW_TEMP_MODEL, Constants.DEFAULT_LOW_TEMP);
//        mLowestTemp = ((float) mCurrBodyMinT / 10);
//        mHighestTemp = ((float) mCurrBodyMaxT / 10);
        ambient = SpUtils.getFloat(SpUtils.AMBIENT, Constants.DEFAULT_AMBIENT);

        mThermalCorrect = SpUtils.getFloat(ThermalConst.Key.THERMAL_CORRECT, ThermalConst.Default.THERMAL_CORRECT);

        if (mCurrMode != currMode) {
            mCurrMode = currMode;
            onModeChanged(currMode);
        }

        if (mCurrMode == ThermalConst.THERMAL_TEMP_ONLY || mCurrMode == ThermalConst.THERMAL_FACE_TEMP) {
            TemperatureModule.getIns().initSerialPort(this, "/dev/ttyS4", 115200);
            updateUIHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    TemperatureModule.getIns().startHotImageK3232(mThermalImgMirror, lowTempModel, imageK3232CallBack);
                }
            }, 2000);
        } else if (mCurrMode == ThermalConst.INFARED_ONLY || mCurrMode == ThermalConst.INFARED_FACE) {
            TemperatureModule.getIns().initSerialPort(this, "/dev/ttyS4", 9600);
            TemperatureModule.getIns().setInfraredTempCallBack(infraredTempCallBack);
        }

        mAmbCorrValue = SpUtils.getFloat(SpUtils.AMB_CORRECT_VALUE, Constants.DEFAULT_AMB_CORRECT_VALUE);//环境温度补正
        mTempCorrValue = SpUtils.getFloat(SpUtils.TEMP_CORRECT_VALUE, Constants.DEFAULT_TEMP_CORRECT_VALUE);//体温检测补正
        TemperatureModule.getIns().setmCorrectionValue(mTempCorrValue);
        TemperatureModule.getIns().setaCorrectionValue(mAmbCorrValue);
    }

    @Override
    protected void onStop() {
        super.onStop();
        TemperatureModule.getIns().setInfraredTempCallBack(null);
        TemperatureModule.getIns().closeHotImageK3232();
    }

    protected boolean updateFaceState(boolean isFar) {
        isFaceToFar = isFar;
        return isFaceToFar;
    }

    private List<Float> tempCacheList = new ArrayList<>();
    private float mCacheMeasureF = 0.0f;
    private static final String TAG = "BaseThermalActivity";
    private float mFinalTemp_F = 0.0f;
    private InfraredTempCallBack infraredTempCallBack = new InfraredTempCallBack() {
        @Override
        public void newestInfraredTemp(float measureF, float afterF, float ambientF) {
            Log.e(TAG, "newestInfraredTemp: " + measureF + " --- " + afterF + " --- " + ambientF + " ---缓存值： " + mCacheMeasureF);

            if (isOnlyFace()) {
                return;
            }

            if (!isHasFace) {
                sendClearTempTipsMessage();
                if (mCacheMeasureF == 0.0f || measureF < mCacheMeasureF) {
                    mCacheMeasureF = measureF;
                }
                mFinalTemp = 0.0f;
                return;
            }

            if (isSoFar) {
                mFinalTemp = 0.0f;
                return;
            }

            if (isFaceToFar) {
                sendTempLowMessage("请靠近点");
                mFinalTemp = 0.0f;
                return;
            }

            if (measureF - mCacheMeasureF < 2.0f) {
                return;
            }

            if (afterF < mTempMinThreshold) {
                mFinalTemp = 0.0f;
                return;
            }

            if (mCacheTime == 0 || System.currentTimeMillis() - mCacheTime > mSpeechDelay) {
                mCacheTime = System.currentTimeMillis();

                mFinalTemp_F = afterF;
                mFinalTemp_F += mThermalCorrect;
                mFinalTemp = formatF(mFinalTemp_F);
                sendTempTipsMessage(mFinalTemp_F);

                if (mFinalTemp_F < mHighestTemp) {
                    if (mCurrMode == ThermalConst.INFARED_ONLY) {
                        Sign temperatureSign = SignManager.instance().getTemperatureSign(mFinalTemp_F);
                        temperatureSign.setImgBitmap(getCurrCameraFrame());
                        sendUploadMessage(temperatureSign);
                    }
                }
            }
        }
    };

    protected void updateSensorTemper(float sensorTemper, float cacheTemper) {
    }


    private float mCacheBeforTemper = 0.0f;
    private float mFinalTemp = 0.0f;
    private long mCacheTime = 0;
    private Bitmap mLastHotBitmap = null;
    private HotImageK3232CallBack imageK3232CallBack = new HotImageK3232CallBack() {
        @Override
        public void newestHotImageData(final Bitmap imageBmp, final float sensorT, final float maxT, final float minT, final float bodyMaxT, final boolean isBody, final int bodyPercentage) {
            sendUpdateMessage(imageBmp, sensorT, maxT, mCacheBeforTemper);
            mLastHotBitmap = imageBmp;

            if (isOnlyFace()) {
                return;
            }

            if (!isHasFace) {
                mCacheTime = 0;
                sendClearTempTipsMessage();
                if (mCacheBeforTemper == 0.0f || sensorT < mCacheBeforTemper) {
                    mCacheBeforTemper = sensorT;
                }

                tempCacheList.clear();
                mFinalTemp = 0.0f;
                return;
            }

            if (isSoFar) {
                mFinalTemp = 0.0f;
                return;
            }

            if (isFaceToFar) {
                sendTempLowMessage("请靠近点");
                mFinalTemp = 0.0f;
                return;
            }

            if (sensorT - mCacheBeforTemper < 2.0f) {
                return;
            }

            if (maxT < mTempMinThreshold) {
                mFinalTemp = 0.0f;
                return;
            }

            if (mCacheTime == 0 || System.currentTimeMillis() - mCacheTime > mSpeechDelay) {
                tempCacheList.add(maxT);
                if (tempCacheList.size() < 5) {
                    return;
                }

                mCacheTime = System.currentTimeMillis();

                Float max = Collections.max(tempCacheList);
                tempCacheList.clear();

                mFinalTemp = max;
                mFinalTemp += mThermalCorrect;
                mFinalTemp = formatF(mFinalTemp);
                sendTempTipsMessage(mFinalTemp);
                if (mFinalTemp_F < mHighestTemp) {
                    if (mCurrMode == ThermalConst.THERMAL_TEMP_ONLY) {
                        Sign temperatureSign = SignManager.instance().getTemperatureSign(mFinalTemp);
                        temperatureSign.setImgBitmap(getCurrCameraFrame());
                        temperatureSign.setHotImageBitmap(mLastHotBitmap);
                        sendUploadMessage(temperatureSign);
                    }
                }
            }
        }
    };
    private boolean isHasFace = false;

    //更新是否有人脸的标签
    protected void updateHasFace(boolean hasFace) {
        isHasFace = hasFace;
        if (!hasFace) {
            isFaceToFar = false;
        }
    }

    private boolean isSoFar = false;

    protected void updateSoFar(boolean soFar) {
        isSoFar = soFar;
    }

    //如果提示框正在显示
    protected boolean isTempTipsShown() {
        return false;
    }

    //发送清除提示框的消息
    private void sendClearTempTipsMessage() {
        if (isTempTipsShown()) {
            updateUIHandler.sendEmptyMessage(-3);
        }
    }

    //清除提示框
    protected void clearTempTips() {

    }

    //是否有最终值
    protected boolean hasFinalTemp() {
        return mFinalTemp != 0.0f;
    }

    //距离提示
    protected void sendTempLowMessage(String tips) {
        Message message = Message.obtain();
        message.what = -2;
        message.obj = tips;
        updateUIHandler.sendMessageDelayed(message, 100);
    }

    //发送界面更新消息
    private void sendUpdateMessage(Bitmap bitmap, float temper, float maxT, float cacheTemper) {
        Message message = Message.obtain();
        message.what = 0;
        message.obj = bitmap;
        Bundle bundle = new Bundle();
        bundle.putFloat("temper", temper);
        bundle.putFloat("maxTemper", maxT);
        bundle.putFloat("cacheTemper", cacheTemper);
        message.setData(bundle);
        updateUIHandler.sendMessage(message);
    }

    //发送温度提示
    private void sendTempTipsMessage(float temper) {
        Message message = Message.obtain();
        message.what = 1;
        message.obj = temper;
        updateUIHandler.sendMessage(message);
    }

    //发送上传消息
    private void sendUploadMessage(Sign sign) {
        Message message = Message.obtain();
        message.what = 2;
        message.obj = sign;
        updateUIHandler.sendMessage(message);
    }

    //发送人脸测温消息
    protected void sendFaceTempMessage(Bitmap facePicture, CompareResult compareResult) {
        if (mFinalTemp > mHighestTemp) {
            return;
        }
        Sign sign = null;
        if (compareResult.getSimilar() == -1) {
            //直接上报温度
            sign = SignManager.instance().getTemperatureSign(mFinalTemp);
        } else {
            sign = SignManager.instance().checkSignData(compareResult, mFinalTemp);
            if (sign == null) {
                return;
            }
        }
        mFinalTemp = 0.0f;
        sign.setImgBitmap(facePicture);
        sign.setHotImageBitmap(mLastHotBitmap);
        sendUploadMessage(sign);
    }

    //发送清除界面的消息
    private void sendClearMessage() {
        updateUIHandler.removeMessages(-1);
        updateUIHandler.sendEmptyMessageDelayed(-1, 3000);
    }

    private Handler updateUIHandler = new Handler() {
        private static final String TAG = "BaseThermalActivity";

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case -3:
                    clearTempTips();
                    break;
                case -2:
                    showStableTips("请靠近点", R.drawable.shape_main_frame_temperature_warning);
                    KDXFSpeechManager.instance().playNormal("请靠近点");
                    break;
                case 0://更新图像和温度
                    Bitmap bitmap = (Bitmap) msg.obj;
                    Bundle data = msg.getData();
                    float temper = data.getFloat("temper", 0.0f);
                    float maxTemper = data.getFloat("maxTemper");
                    float cacheTemper = data.getFloat("cacheTemper");
                    updateHotImageAndTemper(bitmap, temper, maxTemper, cacheTemper);
                    break;
                case 1://仅提示温度
                    isResultShown = true;
                    removeMessages(-1);
                    KDXFSpeechManager.instance().stopNormal();
                    KDXFSpeechManager.instance().stopWarningRing();

                    int bgId;
                    String resultTip;
                    Runnable resultRunnable;
                    float resultTemper = (float) msg.obj;
                    if (resultTemper < mTempWarningThreshold) {
                        ledGreen();
                        bgId = R.drawable.shape_main_frame_temperature_normal;
                        //检查正常播报内容
                        String normalBroad = SpUtils.getStr(ThermalConst.Key.NORMAL_BROADCAST, ThermalConst.Default.NORMAL_BROADCAST);
                        if (TextUtils.isEmpty(normalBroad)) {
                            resultTip = getResources().getString(R.string.main_temp_normal_tips);
                        } else {
                            resultTip = normalBroad;
                        }
                        resultRunnable = new Runnable() {
                            @Override
                            public void run() {
                                sendClearMessage();
                            }
                        };
                    } else {
                        ledRed();
                        bgId = R.drawable.shape_main_frame_temperature_warning;
                        if (resultTemper > mHighestTemp) {
                            resultTip = getResources().getString(R.string.main_temp_error_tips);
                        } else {
                            //检查异常播报内容
                            String warningBroad = SpUtils.getStr(ThermalConst.Key.WARNING_BROADCAST, ThermalConst.Default.WARNING_BROADCAST);
                            if (TextUtils.isEmpty(warningBroad)) {
                                resultTip = getResources().getString(R.string.main_temp_warning_tips);
                            } else {
                                resultTip = warningBroad;
                            }
                        }
                        resultRunnable = new Runnable() {
                            @Override
                            public void run() {
                                KDXFSpeechManager.instance().playWaningRing();
                                sendClearMessage();
                            }
                        };
                    }
                    if (resultTemper <= mHighestTemp) {
                        resultTip += resultTemper + "℃";
                    }
                    showUIResult(resultTip, bgId);
                    KDXFSpeechManager.instance().playNormal(resultTip, resultRunnable);
                    break;
                case 2://上传数据更新记录
                    Sign sign = (Sign) msg.obj;
                    if (mCurrMode == ThermalConst.THERMAL_TEMP_ONLY) {
                        if (sign.getTemperature() < mTempWarningThreshold) {
                            openDoor();
                        }
                    } else if (mCurrMode == ThermalConst.THERMAL_FACE_TEMP) {
                        if (sign.getType() != -2 && sign.getType() != -9 && sign.getTemperature() < mTempWarningThreshold) {
                            openDoor();
                        }
                    }
                    updateSignList(sign);
                    SignManager.instance().uploadTemperatureSign(sign);
                    break;
                case -1:
                    isResultShown = false;
                    clearUI();
                    KDXFSpeechManager.instance().stopWarningRing();
                    resetLedDelay(0);//5秒后重置灯光为蓝色
                    break;
            }
        }
    };

    private boolean isFaceToFar = true;//人脸是否太远

    protected void updateSignList(Sign sign) {

    }

    private boolean isResultShown = false;
    public static String STABLE_TIP = "请稳定体温后重新测试";
    private int stableTipsId = R.drawable.shape_main_frame_temperature_warning;


    protected abstract boolean isStableTipsShown();

    protected abstract boolean showStableTips(String tip, int stableTipsId);

    protected boolean isOnlyTemp() {
        return mCurrMode == ThermalConst.THERMAL_TEMP_ONLY || mCurrMode == ThermalConst.INFARED_ONLY;
    }

    protected boolean isFaceAndThermal() {
        return mCurrMode == ThermalConst.THERMAL_FACE_TEMP;
    }

    protected boolean isOnlyFace() {
        return mCurrMode == ThermalConst.THERMAL_FACE_ONLY;
    }

    protected int getCurrMode() {
        return mCurrMode;
    }

    protected abstract void onModeChanged(int mode);

    protected abstract void updateHotImageAndTemper(Bitmap bitmap, float temper, float maxT, float cacheT);

    protected abstract Bitmap getCurrCameraFrame();

    protected abstract void showUIResult(String tip, int id);

    protected abstract void clearUI();

    private float getMean(List<Float> array) {
        float result = 0.0f;
        if (array.size() == 0) {
            return result;
        }
        for (float anArray : array) {
            result += anArray;
        }
        result = result / array.size();
        result = formatF(result);
        return result;
    }

    private float formatF(float fValue) {
        return (float) (Math.round(fValue * 10)) / 10;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TemperatureModule.getIns().closeSerialPort();
        TemperatureModule.getIns().closeHotImageK3232();
    }
}
