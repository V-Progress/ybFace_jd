package com.yunbiao.ybsmartcheckin_live_id.temp_check_in;

import android.content.res.Configuration;
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
import com.yunbiao.ybsmartcheckin_live_id.temp_check_in_smt.SMTModelConst;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/***
 * 1.数据上传逻辑
 * 如果是仅测温模式，播报后上传
 * 如果是人脸+测温模式，则人脸等待最终播报数值后上传数据，重复播报时检查Sign缓存，如果不为空，更新温度，时间，头像和热图后上传
 * 显示提示均采用handler发送的方式
 * 人脸识别与温度检测，数据拆分上传，
 *
 *
 *
 */

public abstract class BaseThermalActivity extends BaseGpioActivity {

    private long mSpeechDelay;
    protected int mCurrMode = -99;
    private Float mTempMinThreshold;
    private Float mTempWarningThreshold;
    //    private Float mAmbCorrValue;
//    private Float mTempCorrValue;
    private boolean mThermalImgMirror;

    private float mHighestTemp = 45.0f;
    private boolean lowTempModel;
    private float mThermalCorrect;
    private boolean distanceTipEnable;

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //播报延时
        mSpeechDelay = SpUtils.getLong(ThermalConst.Key.SPEECH_DELAY, ThermalConst.Default.SPEECH_DELAY);
        //测温最小阈值
        mTempMinThreshold = SpUtils.getFloat(ThermalConst.Key.TEMP_MIN_THRESHOLD, ThermalConst.Default.TEMP_MIN_THRESHOLD);
        //测温报警阈值
        mTempWarningThreshold = SpUtils.getFloat(ThermalConst.Key.TEMP_WARNING_THRESHOLD, ThermalConst.Default.TEMP_WARNING_THRESHOLD);
        //热成像图像镜像
        mThermalImgMirror = SpUtils.getBoolean(ThermalConst.Key.THERMAL_IMAGE_MIRROR, ThermalConst.Default.THERMAL_IMAGE_MIRROR);
        //低温模式
        lowTempModel = SpUtils.getBoolean(ThermalConst.Key.LOW_TEMP_MODE, ThermalConst.Default.LOW_TEMP);
        //距离提示
        distanceTipEnable = SpUtils.getBoolean(ThermalConst.Key.DISTANCE_TIP, ThermalConst.Default.DISTANCE_TIP);
        //温度补偿
        mThermalCorrect = SpUtils.getFloat(ThermalConst.Key.THERMAL_CORRECT, ThermalConst.Default.THERMAL_CORRECT);
        TemperatureModule.getIns().setmCorrectionValue(mThermalCorrect);

        //模式切换
        int currMode = SpUtils.getIntOrDef(SpUtils.THERMAL_MODEL_SETTING, ThermalConst.DEFAULT_THERMAL_MODEL);//当前模式
        if (mCurrMode != currMode) {
            mCurrMode = currMode;
            onModeChanged(currMode);
        }

        //初始化串口号
        if (mCurrMode == ThermalConst.THERMAL_TEMP_ONLY || mCurrMode == ThermalConst.THERMAL_FACE_TEMP) {
            String portPath;
            if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                portPath = "/dev/ttyS1";
            } else {
                portPath = "/dev/ttyS4";
            }
            TemperatureModule.getIns().initSerialPort(this, portPath, 115200);
            updateUIHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    TemperatureModule.getIns().startHotImageK3232(mThermalImgMirror, lowTempModel, imageK3232CallBack);
                }
            }, 2000);
        } else if (mCurrMode == ThermalConst.INFARED_ONLY || mCurrMode == ThermalConst.INFARED_FACE) {
            String portPath;
            if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                portPath = "/dev/ttyS1";
            } else {
                portPath = "/dev/ttyS4";
            }
            TemperatureModule.getIns().initSerialPort(this, portPath, 9600);
            TemperatureModule.getIns().setInfraredTempCallBack(infraredTempCallBack);
        }

        //温度补正
//        mAmbCorrValue = SpUtils.getFloat(SpUtils.AMB_CORRECT_VALUE, Constants.DEFAULT_AMB_CORRECT_VALUE);//环境温度补正
//        mTempCorrValue = SpUtils.getFloat(SpUtils.TEMP_CORRECT_VALUE, Constants.DEFAULT_TEMP_CORRECT_VALUE);//体温检测补正
//        TemperatureModule.getIns().setaCorrectionValue(mAmbCorrValue);
    }

    @Override
    protected void onStop() {
        super.onStop();
        TemperatureModule.getIns().closeHotImageK3232();
        TemperatureModule.getIns().setInfraredTempCallBack(null);
    }

    protected boolean updateFaceState(boolean isFar) {
        isFaceToFar = isFar;
        return isFaceToFar;
    }

    private List<Float> tempCacheList = new ArrayList<>();
    private float mCacheMeasureF = 0.0f;
    private static final String TAG = "BaseThermalActivity";
    private InfraredTempCallBack infraredTempCallBack = new InfraredTempCallBack() {
        @Override
        public void newestInfraredTemp(float measureF, float afterF, float ambientF) {
            Log.e(TAG, "newestInfraredTemp: 原始值：" + measureF + " --- 处理值：" + afterF);

            if (isOnlyFace()) {
                return;
            }

            if (!isHasFace) {
                tempCacheList.clear();
                sendClearTempTipsMessage();
                if (mCacheMeasureF == 0.0f || measureF < mCacheMeasureF) {
                    mCacheMeasureF = measureF;
                }
                mFinalTemp = 0.0f;
                return;
            }

            if (isFaceToFar) {
                tempCacheList.clear();
                sendTempLowMessage(getResources().getString(R.string.main_tips_please_close));
                mFinalTemp = 0.0f;
                return;
            }

            if (!isInsideRange) {
                tempCacheList.clear();
                sendTempLowMessage("请对准人脸框");
                mFinalTemp = 0.0f;
                return;
            }

            if (measureF - mCacheMeasureF < 2.0f) {
                tempCacheList.clear();
                return;
            }

            if (afterF < mTempMinThreshold) {
                mFinalTemp = 0.0f;
                tempCacheList.clear();
                return;
            }

            if (mCacheTime == 0 || System.currentTimeMillis() - mCacheTime > mSpeechDelay) {
                tempCacheList.add(afterF);
                if (tempCacheList.size() < 3) {
                    return;
                }

                for (int i = 0; i < tempCacheList.size(); i++) {
                    Float aFloat = tempCacheList.get(i);
                    Log.e(TAG, "newestInfraredTemp: " + aFloat);
                }

                mCacheTime = System.currentTimeMillis();

                Float max = Collections.max(tempCacheList);
                mFinalTemp = max;

                if (mFinalTemp < mTempWarningThreshold) {
                    mFinalTemp += mThermalCorrect;
                }

                mFinalTemp = formatF(mFinalTemp);
                sendTempTipsMessage(mFinalTemp);

                if (mFinalTemp < mHighestTemp) {
                    //仅测温模式下发送陌生人记录
                    if (mCurrMode == ThermalConst.INFARED_ONLY) {
                        Sign temperatureSign = SignManager.instance().getTemperatureSign(mFinalTemp);
                        temperatureSign.setImgBitmap(getCurrCameraFrame());
                        sendUploadMessage(temperatureSign);
                    }
                }
            }
        }
    };

    private boolean isInsideRange = false;

    protected void checkFaceRange(boolean isInside) {
        isInsideRange = isInside;
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

            if (isFaceToFar) {
                sendTempLowMessage(getResources().getString(R.string.main_tips_please_close));
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
                if (tempCacheList.size() < 4) {
                    return;
                }

                mCacheTime = System.currentTimeMillis();

                Float max = Collections.max(tempCacheList);
                tempCacheList.clear();
                mFinalTemp = max;

                if (mFinalTemp < mTempWarningThreshold) {
                    mFinalTemp += mThermalCorrect;
                }

                mFinalTemp = formatF(mFinalTemp);
                sendTempTipsMessage(mFinalTemp);
                if (mFinalTemp < mHighestTemp) {
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
            mCacheSign = null;
            isFaceToFar = false;
        }
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
        updateUIHandler.removeMessages(-2);
        if (!distanceTipEnable || isResultShown) {
            return;
        }
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
        mCacheSign = sign;
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
                case -3://清除提示
                    isResultShown = false;
                    clearTempTips();
                    break;
                case -2://显示提示
                    removeMessages(-3);
                    int id;
                    if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                        id = R.mipmap.bg_verify_nopass;
                    } else {
                        id = R.drawable.shape_main_frame_temperature_warning;
                    }
                    String tip = (String) msg.obj;
                    showStableTips(tip, id);
                    KDXFSpeechManager.instance().playNormal(tip);
                    sendEmptyMessageDelayed(-3, 2000);
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
                    removeMessages(-3);
                    KDXFSpeechManager.instance().stopNormal();
                    KDXFSpeechManager.instance().stopWarningRing();

                    int bgId;
                    String resultTip;
                    Runnable resultRunnable;
                    float resultTemper = (float) msg.obj;
                    if (resultTemper < mTempWarningThreshold) {
                        ledGreen();
                        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                            bgId = R.mipmap.bg_verify_pass;
                        } else {
                            bgId = R.drawable.shape_main_frame_temperature_normal;
                        }
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
                        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                            bgId = R.mipmap.bg_verify_nopass;
                        } else {
                            bgId = R.drawable.shape_main_frame_temperature_warning;
                        }
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
                        resultTip += " " + resultTemper + " ℃";
                    }
                    showUIResult(resultTip, bgId);
                    KDXFSpeechManager.instance().playNormal(resultTip, resultRunnable);

                    //如果是考勤测温且缓存Sign不为null则继续上传该人的信息
                    if (mCurrMode == ThermalConst.THERMAL_FACE_TEMP || mCurrMode == ThermalConst.INFARED_FACE) {
                        if (mCacheSign != null) {
                            mCacheSign.setTemperature(resultTemper);
                            mCacheSign.setHotImageBitmap(mLastHotBitmap);
                            mCacheSign.setImgBitmap(getCurrCameraFrame());
                            mCacheSign.setTime(System.currentTimeMillis());
                            sendUploadMessage(mCacheSign);
                        }
                    }
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
                case 3:

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

    private Sign mCacheSign;
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
