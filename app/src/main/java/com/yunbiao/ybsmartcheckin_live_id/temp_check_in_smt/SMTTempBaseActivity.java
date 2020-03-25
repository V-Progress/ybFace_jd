package com.yunbiao.ybsmartcheckin_live_id.temp_check_in_smt;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.callback.Smt3232TempCallBack;
import com.yunbiao.faceview.CompareResult;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import org.apache.harmony.javax.security.auth.login.LoginException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SMTTempBaseActivity extends SMTBaseActivity {
    private int smtModel;
    private long mSpeechDelay;
    private Float mTempMinThreshold;
    private Float mTempWarningThreshold;
    private float mCurrentTemp = 0.0f;
    private boolean isRunning = false;
    private long mCacheTime = 0;
    private boolean mLowTemp;
    private Float mAmbCorrValue;
    private Float mTempCorrValue;
    private boolean mDistanceTipEnable;
    private boolean mFEnabled;

    @Override
    protected void initData() {
        /*红外模块是9600，热成像模块是115200*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDistanceTipEnable = SpUtils.getBoolean(SMTModelConst.key.DISTANCE_TIP_ENABLE, SMTModelConst.Default.DISTANCE_TIP_ENABLE);

        mSpeechDelay = SpUtils.getLong(SpUtils.SPEECH_DELAY, Constants.DEFAULT_SPEECH_DELAY);
        mTempMinThreshold = SpUtils.getFloat(SpUtils.TEMP_MIN_THRESHOLD, Constants.DEFAULT_TEMP_MIN_THRESHOLD_VALUE); //测温最小阈值
        mTempWarningThreshold = SpUtils.getFloat(SpUtils.TEMP_WARNING_THRESHOLD, Constants.DEFAULT_TEMP_WARNING_THRESHOLD_VALUE); //测温报警阈值
        mLowTemp = SpUtils.getBoolean(SMTModelConst.key.LOW_TEMP, SMTModelConst.Default.LOW_TEMP);
        float tempCorr = SpUtils.getFloat(SpUtils.TEMP_CORRECT_VALUE, Constants.DEFAULT_TEMP_CORRECT_VALUE);
        mFEnabled = SpUtils.getBoolean(SMTModelConst.key.F_ENABLED,SMTModelConst.Default.F_ENABLED);
        TemperatureModule.getIns().setmCorrectionValue(tempCorr);

        int currMode = SpUtils.getIntOrDef(SpUtils.SMT_MODEL_SETTING, SMTModelConst.DEFAULT_SMT_MODEL);
        if (smtModel != currMode) {
            smtModel = currMode;
            onModeChanged(smtModel);
        }

        if (smtModel == SMTModelConst.SMT_TEMP_ONLY || smtModel == SMTModelConst.SMT_FACE_TEMP) {
            TemperatureModule.getIns().initSerialPort(this, "/dev/ttyS3", 115200);
            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    TemperatureModule.getIns().startSmt3232Temp(mLowTemp, smt3232TempCallBack);
                }
            }, 2000);
        }

        mAmbCorrValue = SpUtils.getFloat(SpUtils.AMB_CORRECT_VALUE, Constants.DEFAULT_AMB_CORRECT_VALUE);//环境温度补正
        mTempCorrValue = SpUtils.getFloat(SpUtils.TEMP_CORRECT_VALUE, Constants.DEFAULT_TEMP_CORRECT_VALUE);//体温检测补正
        TemperatureModule.getIns().setmCorrectionValue(mTempCorrValue);
        TemperatureModule.getIns().setaCorrectionValue(mAmbCorrValue);
    }

    @Override
    protected void onStop() {
        super.onStop();
        TemperatureModule.getIns().closeSmt3232Temp();
    }

    protected boolean updateFaceState(boolean isFar) {
        isFaceToFar = isFar;
        return isFaceToFar;
    }

    private float mCacheBeforeTemper = 0.0f;
    private float mFinalTemp = 0.0f;
    //    private long mCacheTime = 0;
    private boolean isHasFace = false;
    //    private boolean isSoFar = false;
    private boolean isFaceToFar = false;
    private boolean isResultShown = false;
    private Bitmap mLastHotBitmap = null;
    private List<Float> tempCacheList = new ArrayList<>();
    private static final String TAG = "SMTTempBaseActivity";
    private Smt3232TempCallBack smt3232TempCallBack = new Smt3232TempCallBack() {
        @Override
        public void newestSmt3232Temp(float measureF, float afterF) {
            if (isOnlyFace()) {
                return;
            }
            //如果没有人脸，清除时间，清除提示，清除list，清除最终值
            if (!isHasFace) {
                mCacheTime = 0;
                sendClearTempTipsMessage();
                tempCacheList.clear();
                mFinalTemp = 0.0f;
                return;
            }
            //如果人脸太远，提示靠近点，清除最终值
            if (isFaceToFar) {
                sendTempLowMessage(getResources().getString(R.string.main_tips_please_close));
                mFinalTemp = 0.0f;
                return;
            }
            //如果处理后的数值小于播报阈值，清除最终值
            if (afterF < mTempMinThreshold) {
                mFinalTemp = 0.0f;
                return;
            }
            //判断时间并缓存
            if (mCacheTime == 0 || System.currentTimeMillis() - mCacheTime > mSpeechDelay) {
                tempCacheList.add(afterF);
                if (tempCacheList.size() < 3) {
                    return;
                }

                mCacheTime = System.currentTimeMillis();

                Float max = Collections.max(tempCacheList);
                tempCacheList.clear();

                mFinalTemp = max;
//                mFinalTemp += mThermalCorrect;
                mFinalTemp = formatF(mFinalTemp);
                sendTempTipsMessage(mFinalTemp);
                if (mFinalTemp < mHighestTemp) {
                    if (smtModel == SMTModelConst.SMT_TEMP_ONLY) {
                        Sign temperatureSign = SignManager.instance().getTemperatureSign(mFinalTemp);
                        temperatureSign.setImgBitmap(getCurrCameraFrame());
                        temperatureSign.setHotImageBitmap(mLastHotBitmap);
                        sendUploadMessage(temperatureSign);
                    } else if (smtModel == SMTModelConst.SMT_FACE_TEMP) {
                        //如果是考勤测温且缓存Sign不为null则继续上传该人的信息
                        if (mCacheSign != null) {
                            mCacheSign.setTemperature(mFinalTemp);
                            mCacheSign.setHotImageBitmap(mLastHotBitmap);
                            mCacheSign.setImgBitmap(getCurrCameraFrame());
                            mCacheSign.setTime(System.currentTimeMillis());
                            sendUploadMessage(mCacheSign);
                        }
                    }
                }
            }
        }
    };

    protected void updateHasFace(boolean has) {
        isHasFace = has;
        if (!has) {
            mCacheSign = null;
            isFaceToFar = false;
        }
    }

    protected boolean isTempTipsShown() {
        return false;
    }

    private void sendClearTempTipsMessage() {
        if (isTempTipsShown()) {
            uiHandler.sendEmptyMessage(-3);
        }
    }

    protected void clearTempTips() {

    }

    protected boolean hasFinalTemp() {
        return mFinalTemp != 0.0f;
    }

    protected void sendTempLowMessage(String tips) {
        if (!mDistanceTipEnable || isResultShown) {
            return;
        }
        Message message = Message.obtain();
        message.what = -2;
        message.obj = tips;
        uiHandler.sendMessageDelayed(message, 100);
    }

    private void sendUpdateMessage(Bitmap bitmap, float temper, float maxT, float cacheTemper) {
        Message message = Message.obtain();
        message.what = 0;
        message.obj = bitmap;
        Bundle bundle = new Bundle();
        bundle.putFloat("temper", temper);
        bundle.putFloat("maxTemper", maxT);
        bundle.putFloat("cacheTemper", cacheTemper);
        message.setData(bundle);
        uiHandler.sendMessage(message);
    }

    private void sendTempTipsMessage(float temper) {
        Message message = Message.obtain();
        message.what = 1;
        message.obj = temper;
        uiHandler.sendMessage(message);
    }

    private void sendUploadMessage(Sign sign) {
        Message message = Message.obtain();
        message.what = 2;
        message.obj = sign;
        uiHandler.sendMessage(message);
    }

    private Sign mCacheSign;

    protected void sendFaceTempMessage(Bitmap facePicture, CompareResult compareResult) {
        Log.e(TAG, "sendFaceTempMessage: 发送人脸识别数据");
        Sign sign = null;
        if (TextUtils.equals(compareResult.getUserName(),"-1")) {
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
        uiHandler.removeMessages(-1);
        uiHandler.sendEmptyMessageDelayed(-1, 3000);
    }

    protected void clearStableTips() {
    }

    private Runnable resultRunnable;
    protected Runnable getResultRunnable(){
        return resultRunnable;
    }
    private float mHighestTemp = 45.0f;
    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case -4://清除距离提示
                    clearStableTips();
                    break;
                case -3://人脸离开后立即清除温度提示
                    isResultShown = false;
                    clearTempTips();
                    break;
                case -2://显示距离提示
                    removeMessages(-4);
                    showStableTips(getResources().getString(R.string.main_temp_tips_please_close), R.mipmap.bg_verify_nopass);
                    KDXFSpeechManager.instance().playNormal(getResources().getString(R.string.main_temp_tips_please_close));
                    sendEmptyMessageDelayed(-4, 2000);
                    break;
                case 0://更新热量图
                    Bitmap bitmap = (Bitmap) msg.obj;
                    Bundle data = msg.getData();
                    float temper = data.getFloat("temper", 0.0f);
                    float maxTemper = data.getFloat("maxTemper");
                    float cacheTemper = data.getFloat("cacheTemper");
                    updateHotImageAndTemper(bitmap, temper, maxTemper, cacheTemper);
                    break;
                case 1://显示温度提示
                    Log.e(TAG, "handleMessage: 提示温度");
                    isResultShown = true;
                    removeMessages(-2);//清除距离提示
                    removeMessages(-1);//清除温度提示
                    KDXFSpeechManager.instance().stopNormal();
                    KDXFSpeechManager.instance().stopWarningRing();

                    int bgId;
                    String resultTip;
                    float resultTemper = (float) msg.obj;
                    if (resultTemper < mTempWarningThreshold) {
                        ledGreen();
                        bgId = R.mipmap.bg_verify_pass;
                        //检查正常播报内容
                        resultTip = getResources().getString(R.string.main_temp_normal_tips);
                        resultRunnable = new Runnable() {
                            @Override
                            public void run() {
                                sendClearMessage();
                            }
                        };
                    } else {
                        ledRed();
                        bgId = R.mipmap.bg_verify_nopass;
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
                        if(mFEnabled){
                            resultTemper = (float) (resultTemper * 1.8 + 32);
                            resultTemper = formatF(resultTemper);
                            resultTip += resultTemper + "℃";
                        } else {
                            resultTip += resultTemper + "℃";
                        }
                    }
                    showUIResult(resultTip, bgId);

                    KDXFSpeechManager.instance().playNormal(resultTip, resultRunnable);
                    break;
                case 2://上传测温数据
                    Log.e(TAG, "handleMessage: 上传数据" );
                    Sign sign = (Sign) msg.obj;
                    if (smtModel == SMTModelConst.SMT_TEMP_ONLY) {
                        if (sign.getTemperature() < mTempWarningThreshold) {
                            openDoor();
                        }
                    } else if (smtModel == SMTModelConst.SMT_FACE_TEMP) {
                        if (sign.getType() != -2 && sign.getType() != -9 && sign.getTemperature() < mTempWarningThreshold) {
                            openDoor();
                        }
                    }
                    updateSignList(sign);
                    SignManager.instance().uploadTemperatureSign(sign);
                    break;
                case -1://清除温度提示
                    isResultShown = false;
                    clearUI();
                    KDXFSpeechManager.instance().stopWarningRing();
                    resetLedDelay(0);
                    break;
            }
        }
    };

    protected void updateSignList(Sign sign) {

    }

    protected abstract boolean showStableTips(String tip, int stableTipsId);

    protected boolean isOnlyTemp() {
        return smtModel == SMTModelConst.SMT_TEMP_ONLY;
    }

    protected boolean isOnlyFace() {
        return smtModel == SMTModelConst.SMT_FACE_ONLY;
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

    /*List<Float> cacheLits = new ArrayList<>();
    private static final String TAG = "SMTTempBaseActivity";
    //开始红外测温逻辑
    private void startTemperatureThread() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    float measuringTemperatureF = TemperatureModule.getIns().getMeasuringTemperatureF();
                    Log.e(TAG, "run: " + measuringTemperatureF);

                    //获取检测温度
                    measuringTemperatureF = formatF(measuringTemperatureF);

                    sendTempUpdateMessage(measuringTemperatureF);
                    mCurrentTemp = measuringTemperatureF;

                    if (smtModel == SMTModelConst.SMT_TEMP_ONLY) {

                        if (measuringTemperatureF < mTempMinThreshold) {
                            sendTempLowMessage();
                            mCacheTime = 0;
                            cacheLits.clear();
                        } else {
                            cacheLits.add(measuringTemperatureF);
                            Log.e("778899", "run: " + cacheLits.size());

                            //检测一个人是否刚进去
                            if (mCacheTime == 0) {
                                mCacheTime = 1;
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                continue;
                            }

                            if (mCacheTime == 1) {
                                mCacheTime = 2;
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                continue;
                            }

                            long timeMillis = System.currentTimeMillis();
                            if (timeMillis - mCacheTime < mSpeechDelay) {
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                continue;
                            }
                            mCacheTime = timeMillis;

                            float finalTemp = Collections.max(cacheLits);
                            cacheLits.clear();

                            *//*if(measuringTemperatureF < 36.0f){
                                measuringTemperatureF += 0.3f;
                            }
                            float finalTemp = measuringTemperatureF;*//*
                            boolean isWarning = finalTemp >= mTempWarningThreshold;

                            //截取摄像头画面并提示
                            Bitmap currCameraFrame = getFaceViewBitmap();
                            final Sign temperatureSign = SignManager.instance().getTemperatureSign(finalTemp);
                            temperatureSign.setImgBitmap(currCameraFrame);
                            temperatureSign.setHotImageBitmap(null);

                            //发送结果
                            sendTempResultMessage(isWarning, temperatureSign);

                            //上传记录
                            SignManager.instance().uploadTemperatureSign(temperatureSign);
                        }

                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void sendTempLowMessage() {
        Message message = Message.obtain();
        message.what = -1;
        tipsHandler.sendMessage(message);
    }

    private void sendTempUpdateMessage(float f) {
        Message message = Message.obtain();
        message.what = 0;
        message.obj = f;
        tipsHandler.sendMessage(message);
    }

    protected void sendTempResultMessage(boolean isWarning, Sign sign) {
        Message message = Message.obtain();
        message.what = 2;
        message.arg1 = isWarning ? 1 : 0;
        message.obj = sign;
        tipsHandler.sendMessage(message);
    }

    private void sendClearMessage() {
        tipsHandler.removeMessages(-1);
        tipsHandler.sendEmptyMessageDelayed(-1, 2000);
    }

    //计时器
    private Handler tipsHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    float mTemp = (float) msg.obj;
                    updateTemp(mTemp);
                    break;
                case 1:
                    sendClearMessage();
                    break;
                case 2:
                    removeMessages(-1);
                    KDXFSpeechManager.instance().stopNormal();
                    KDXFSpeechManager.instance().stopWarningRing();

                    Sign sign = (Sign) msg.obj;
                    boolean isWarning = msg.arg1 == 1;
                    float tempr = sign.getTemperature();
                    String name = sign.getName();

                    Runnable runnable;
                    String tip;
                    int id;
                    if (isWarning) {
                        ledRed();
                        id = R.mipmap.bg_verify_nopass;
                        tip = getResources().getString(R.string.main_temp_warning_tips);
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                sendClearMessage();
                                KDXFSpeechManager.instance().playWaningRing();
                            }
                        };
                    } else {
                        ledGreen();
                        id = R.mipmap.bg_verify_pass;
                        tip = getResources().getString(R.string.main_temp_normal_tips);
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                sendClearMessage();
                            }
                        };
                    }
                    tip += tempr + "℃";

                    setResult(tip, id, sign);

                    tip = TextUtils.isEmpty(name) ? tip : (name + tip);
                    KDXFSpeechManager.instance().playNormal(tip, runnable);
                    break;
                case 3:

                    break;
                case -1:
                    clearUI();
                    KDXFSpeechManager.instance().stopWarningRing();
                    resetLedDelay(0);//5秒后重置灯光为蓝色
                    break;
            }
        }
    };

    private float formatF(float fValue) {
        return (float) (Math.round(fValue * 10)) / 10;
    }


    protected abstract void onModeChanged(int mode);

    protected abstract void updateTemp(float f);

    protected abstract Bitmap getFaceViewBitmap();

    protected abstract void clearUI();

    protected abstract void setResult(String tip, int id, Sign sign);

    protected float getCurrTemp() {
        return mCurrentTemp;
    }

    protected boolean isOnlyTemp() {
        return smtModel == SMTModelConst.SMT_TEMP_ONLY;
    }

    protected boolean isFaceAndTemp() {
        return smtModel == SMTModelConst.SMT_FACE_TEMP;
    }

    protected boolean isOnlyFace() {
        return smtModel == SMTModelConst.SMT_FACE_ONLY;
    }

    protected float getMinThread() {
        return mTempMinThreshold;
    }

    protected float getWarningThread() {
        return mTempWarningThreshold;
    }*/
}
