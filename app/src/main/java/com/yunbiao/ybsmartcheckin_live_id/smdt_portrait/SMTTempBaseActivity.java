package com.yunbiao.ybsmartcheckin_live_id.smdt_portrait;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.intelligence.hardware.temperature.TemperatureModule;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public abstract class SMTTempBaseActivity extends SMTBaseActivity {
    private int smtModel;
    private long mSpeechDelay;
    private Float mTempMinThreshold;
    private Float mTempWarningThreshold;
    private float mCurrentTemp = 0.0f;
    private boolean isRunning = false;
    private long mCacheTime = 0;

    @Override
    protected void initData() {
        /*红外模块是9600，热成像模块是115200*/
        TemperatureModule.getIns().initSerialPort(this, "/dev/ttyS3", 115200);
        TemperatureModule.getIns().startSmt3232Temp(300);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSpeechDelay = SpUtils.getLong(SpUtils.SPEECH_DELAY, Constants.DEFAULT_SPEECH_DELAY);
        mTempMinThreshold = SpUtils.getFloat(SpUtils.TEMP_MIN_THRESHOLD, Constants.DEFAULT_TEMP_MIN_THRESHOLD_VALUE); //测温最小阈值
        mTempWarningThreshold = SpUtils.getFloat(SpUtils.TEMP_WARNING_THRESHOLD, Constants.DEFAULT_TEMP_WARNING_THRESHOLD_VALUE); //测温报警阈值

        float tempCorr = SpUtils.getFloat(SpUtils.TEMP_CORRECT_VALUE, Constants.DEFAULT_TEMP_CORRECT_VALUE);
        TemperatureModule.getIns().setmCorrectionValue(tempCorr);

        int currMode = SpUtils.getIntOrDef(SpUtils.SMT_MODEL_SETTING, SMTModelConst.DEFAULT_SMT_MODEL);
        if (smtModel != currMode) {
            smtModel = currMode;
            onModeChanged(smtModel);
        }

        if (smtModel == SMTModelConst.SMT_FACE_ONLY) {
            closeTemperatureThread();
        } else {
            startTemperatureThread();
        }
    }

    private void closeTemperatureThread() {
        isRunning = false;
    }

    List<Float> cacheLits = new ArrayList<>();
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

                            /*if(measuringTemperatureF < 36.0f){
                                measuringTemperatureF += 0.3f;
                            }
                            float finalTemp = measuringTemperatureF;*/
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
    }
}
