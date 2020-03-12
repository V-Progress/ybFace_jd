package com.yunbiao.ybsmartcheckin_live_id.thermal_imaging;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.callback.HotImageK3232CallBack;
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
    private float mHighestTemp = 40.0f;
    private boolean lowTempModel;
    private float ambient;
    private float mThermalCorrect;

    @Override
    protected void initData() {
        super.initData();

        TemperatureModule.getIns().initSerialPort(this, "/dev/ttyS4", 115200);
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
        mLowestTemp = ((float) mCurrBodyMinT / 10);
        mHighestTemp = ((float) mCurrBodyMaxT / 10);
        ambient = SpUtils.getFloat(SpUtils.AMBIENT, Constants.DEFAULT_AMBIENT);

        mThermalCorrect = SpUtils.getFloat(ThermalConst.Key.THERMAL_CORRECT, ThermalConst.Default.THERMAL_CORRECT);

        if (mCurrMode != currMode) {
            mCurrMode = currMode;
            onModeChanged(currMode);
        }

        if (mCurrMode != ThermalConst.THERMAL_FACE_ONLY) {
            updateUIHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    TemperatureModule.getIns().startHotImageK3232(mThermalImgMirror, lowTempModel, ambient, imageK3232CallBack);
                }
            }, 1000);
        }

        mAmbCorrValue = SpUtils.getFloat(SpUtils.AMB_CORRECT_VALUE, Constants.DEFAULT_AMB_CORRECT_VALUE);//环境温度补正
        mTempCorrValue = SpUtils.getFloat(SpUtils.TEMP_CORRECT_VALUE, Constants.DEFAULT_TEMP_CORRECT_VALUE);//体温检测补正
        TemperatureModule.getIns().setmCorrectionValue(mTempCorrValue);
        TemperatureModule.getIns().setaCorrectionValue(mAmbCorrValue);
    }

    @Override
    protected void onStop() {
        super.onStop();
        TemperatureModule.getIns().closeHotImageK3232();
    }

    private Bitmap mCurrHotImageBitmap;
    private float mCurrBodyTemper;
    private long mCacheTime = 0;
    private List<Float> mTemperatureCacheList = new ArrayList<>();
    private HotImageK3232CallBack imageK3232CallBack = new HotImageK3232CallBack() {
        @Override
        public void newestHotImageData(final Bitmap imageBmp, final float sensorT, final float maxT, final float minT, final float bodyMaxT, final boolean isBody, final int bodyPercentage) {
            mCurrHotImageBitmap = imageBmp;
            mCurrBodyTemper = maxT;
            sendUpdateMessage(mCurrHotImageBitmap, mCurrBodyTemper);

            if (maxT < mTempMinThreshold) {
                mCacheTime = 0;
                mTemperatureCacheList.clear();
            } else {
                mTemperatureCacheList.add(maxT);
                if (mCacheTime == 0) {
                    mCacheTime = 1;
                    return;
                }
                if (mCacheTime == 1) {
                    mCacheTime = 2;
                    return;
                }

                long timeMillis = System.currentTimeMillis();
                if (timeMillis - mCacheTime < mSpeechDelay) {
                    return;
                }
                mCacheTime = timeMillis;

                float finalTemp = 0.0F;
                Float max = Collections.max(mTemperatureCacheList);
                mTemperatureCacheList.remove(max);
                Float min = Collections.min(mTemperatureCacheList);
                mTemperatureCacheList.remove(min);

                finalTemp = getMean(mTemperatureCacheList);

                mTemperatureCacheList.clear();
//                boolean isWarning = finalTemp >= mTempWarningThreshold;
//                final Sign temperatureSign = SignManager.instance().getTemperatureSign(finalTemp);
//                Bitmap currCameraFrame = getCurrCameraFrame();
//                temperatureSign.setImgBitmap(currCameraFrame);
//                temperatureSign.setHotImageBitmap(imageBmp);

//                sendResultMessage(isWarning, temperatureSign);

                sendResultMessage2(finalTemp);

                //上传记录
//                SignManager.instance().uploadTemperatureSign(temperatureSign);
            }

            /*if (isOnlyTemp()) {
                if (maxT < mTempMinThreshold) {
                    if (maxT > mLowestTemp) {
//                        sendTempLowMessage(STABLE_TIP);
                    }
                    mCacheTime = 0;
                    mTemperatureCacheList.clear();
                } else {
                    mTemperatureCacheList.add(maxT);
                    if (mCacheTime == 0) {
                        mCacheTime = 1;
                        return;
                    }
                    if (mCacheTime == 1) {
                        mCacheTime = 2;
                        return;
                    }

                    long timeMillis = System.currentTimeMillis();
                    if (timeMillis - mCacheTime < mSpeechDelay) {
                        return;
                    }
                    mCacheTime = timeMillis;

                    float finalTemp = 0.0F;
                    Float max = Collections.max(mTemperatureCacheList);
                    mTemperatureCacheList.remove(max);
                    Float min = Collections.min(mTemperatureCacheList);
                    mTemperatureCacheList.remove(min);

                    finalTemp = getMean(mTemperatureCacheList);

                    mTemperatureCacheList.clear();
                    boolean isWarning = finalTemp >= mTempWarningThreshold;
                    final Sign temperatureSign = SignManager.instance().getTemperatureSign(finalTemp);
                    Bitmap currCameraFrame = getCurrCameraFrame();
                    temperatureSign.setImgBitmap(currCameraFrame);
                    temperatureSign.setHotImageBitmap(imageBmp);

                    sendResultMessage(isWarning, temperatureSign);

                    //上传记录
                    SignManager.instance().uploadTemperatureSign(temperatureSign);
                }
            }*/
        }
    };

    private void sendUpdateMessage(Bitmap bitmap, float temper) {
        Message message = Message.obtain();
        message.what = 0;
        message.obj = bitmap;
        Bundle bundle = new Bundle();
        bundle.putFloat("temper", temper);
        message.setData(bundle);
        updateUIHandler.sendMessage(message);
    }

    private void sendResultMessage(boolean isWarning, Sign sign) {
        Message message = Message.obtain();
        message.what = 1;
        message.obj = sign;
        message.arg1 = isWarning ? 0 : 1;
        updateUIHandler.sendMessageDelayed(message, 100);
    }
    private void sendResultMessage2(float temp) {
        Message message = Message.obtain();
        message.what = 2;
        message.obj = temp;
        updateUIHandler.sendMessageDelayed(message, 100);
    }

    private void sendClearMessage() {
        updateUIHandler.removeMessages(-1);
        updateUIHandler.sendEmptyMessageDelayed(-1, 3000);
    }

    protected void sendTempLowMessage(String tips) {
        if (!isStableTipsShown() && !isResultShown) {
            Message message = Message.obtain();
            message.what = -2;
            message.obj = tips;
            updateUIHandler.sendMessageDelayed(message, 100);
        }
    }

    private boolean isResultShown = false;
    public static String STABLE_TIP = "请稳定体温后重新测试";
    private int stableTipsId = R.drawable.shape_main_frame_temperature_warning;
    private Handler updateUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case -2:
                    String tipStr = (String) msg.obj;
                    boolean b = showStableTips(tipStr, stableTipsId);
                    if (b) {
                        KDXFSpeechManager.instance().playNormal(tipStr);
                    }
                    sendClearMessage();
                    break;
                case 0:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    float temper = msg.getData().getFloat("temper", 0.0f);
                    updateHotImageAndTemper(bitmap, temper);
                    break;
                case 2:
                    isResultShown = true;
                    removeMessages(-1);
                    KDXFSpeechManager.instance().stopNormal();
                    KDXFSpeechManager.instance().stopWarningRing();

                    int bgId;
                    String resultTip;
                    Runnable resultRunnable;
                    float resultTemper = (float) msg.obj;
                    if (resultTemper < mTempWarningThreshold) {
                        bgId = R.drawable.shape_main_frame_temperature_normal;
                        resultTip = getResources().getString(R.string.main_temp_normal_tips);
                        resultRunnable = new Runnable() {
                            @Override
                            public void run() {
                                sendClearMessage();
                            }
                        };
                    } else {
                        bgId = R.drawable.shape_main_frame_temperature_warning;
                        if (resultTemper > mHighestTemp) {
                            resultTip = getResources().getString(R.string.main_temp_error_tips);
                        } else {
                            resultTip = getResources().getString(R.string.main_temp_warning_tips);
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
                case 1:
                    isResultShown = true;
                    removeMessages(-1);
                    KDXFSpeechManager.instance().stopNormal();
                    KDXFSpeechManager.instance().stopWarningRing();

                    Sign sign = (Sign) msg.obj;
                    int isPass = msg.arg1;
                    float temp = sign.getTemperature();
                    String name = sign.getName();
                    Runnable runnable;
                    String tip;
                    int id;

                    if (isPass == 0) {
                        ledRed();
                        id = R.drawable.shape_main_frame_temperature_warning;
                        if (temp > mHighestTemp) {
                            tip = getResources().getString(R.string.main_temp_error_tips);
                        } else {
                            tip = getResources().getString(R.string.main_temp_warning_tips);
                        }
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                sendClearMessage();
                                KDXFSpeechManager.instance().playWaningRing();
                            }
                        };
                    } else {
                        ledGreen();
                        id = R.drawable.shape_main_frame_temperature_normal;
                        tip = getResources().getString(R.string.main_temp_normal_tips);
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                sendClearMessage();
                            }
                        };
                    }

                    if (temp <= mHighestTemp) {
                        tip += temp + "℃";
                    }

                    setUIResult(tip, id, sign);

                    tip = TextUtils.isEmpty(name) ? tip : (name + tip);
                    KDXFSpeechManager.instance().playNormal(tip, runnable);
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
    private static final String TAG = "BaseThermalActivity";
    private boolean isBroaded = false;
    private float mLastTemp = 0.0f;

    protected void onNoFace() {
        isBroaded = false;
    }

    protected boolean onFaceDetect() {
        if (mCurrBodyTemper < mTempMinThreshold) {
            if (!isResultShown) {
//                sendTempLowMessage(STABLE_TIP);
            }
            return false;
        }
        if (isBroaded) {
            return false;
        }
        isBroaded = true;
        mLastTemp = mCurrBodyTemper;
        return true;
    }

    protected void onFaceResult(Bitmap facePicture, CompareResult compareResult) {
        mLastTemp += mThermalCorrect;
        boolean isWarning = mLastTemp >= mTempWarningThreshold;
        Sign sign = null;
        if (compareResult.getSimilar() == -1) {
            //直接上报温度
            sign = SignManager.instance().getTemperatureSign(mLastTemp);
        } else {
            sign = SignManager.instance().checkSignData(compareResult, mLastTemp);
            if (sign == null) {
                isBroaded = false;
                return;
            }
        }
        sign.setImgBitmap(facePicture);
        sign.setHotImageBitmap(mCurrHotImageBitmap);

        sendResultMessage(isWarning, sign);

        //上传记录
        SignManager.instance().uploadTemperatureSign(sign);
        //如果是过期或陌生人则结束
        if (sign.getType() == -2 || sign.getType() == -9 || isWarning) {
            return;
        }
        openDoor();
    }

    protected abstract boolean isStableTipsShown();

    protected abstract boolean showStableTips(String tip, int stableTipsId);

    protected boolean isOnlyTemp() {
        return mCurrMode == ThermalConst.THERMAL_TEMP_ONLY;
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

    protected abstract void updateHotImageAndTemper(Bitmap bitmap, float temper);

    protected abstract Bitmap getCurrCameraFrame();

    protected abstract void setUIResult(String tip, int id, Sign sign);

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
