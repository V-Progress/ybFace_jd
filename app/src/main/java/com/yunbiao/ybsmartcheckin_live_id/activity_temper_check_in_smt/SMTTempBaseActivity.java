package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in_smt;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.callback.Smt3232TempCallBack;
import com.yunbiao.faceview.CompareResult;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
    private boolean mPrivacyMode;

    @Override
    protected void initData() {
        /*红外模块是9600，热成像模块是115200*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPrivacyMode = SpUtils.getBoolean(Constants.Key.PRIVACY_MODE,Constants.Default.PRIVACY_MODE);
        mDistanceTipEnable = SpUtils.getBoolean(SMTModelConst.key.DISTANCE_TIP_ENABLE, SMTModelConst.Default.DISTANCE_TIP_ENABLE);
        mTempWarningThreshold = SpUtils.getFloat(SMTModelConst.key.TEMP_WARNING_THRESHOLD, SMTModelConst.Default.TEMP_WARNING_THRESHOLD); //测温报警阈值
        mTempMinThreshold = SpUtils.getFloat(SMTModelConst.key.TEMP_MIN_THRESHOLD, SMTModelConst.Default.TEMP_MIN_THRESHOLD); //测温最小阈值
        mSpeechDelay = SpUtils.getLong(SMTModelConst.key.SPEECH_DELAY, SMTModelConst.Default.SPEECH_DELAY);
        mLowTemp = SpUtils.getBoolean(SMTModelConst.key.LOW_TEMP, SMTModelConst.Default.LOW_TEMP);
        mFEnabled = SpUtils.getBoolean(SMTModelConst.key.F_ENABLED,SMTModelConst.Default.F_ENABLED);

        int currMode = SpUtils.getIntOrDef(SMTModelConst.key.MODE, SMTModelConst.Default.MODE);
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

        mAmbCorrValue = SpUtils.getFloat(SMTModelConst.key.AMB_CORRECT_VALUE, SMTModelConst.Default.AMB_CORRECT_VALUE);//环境温度补正
        mTempCorrValue = SpUtils.getFloat(SMTModelConst.key.TEMP_CORRECT_VALUE, SMTModelConst.Default.TEMP_CORRECT_VALUE);//体温检测补正
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
                        }
                        if(mCacheSign != null){
                            mCacheSign.setHotImageBitmap(mLastHotBitmap);
                        }
                        if(mCacheSign != null){
                            mCacheSign.setImgBitmap(getCurrCameraFrame());
                        }
                        if(mCacheSign != null){
                            mCacheSign.setTime(System.currentTimeMillis());
                        }
                        if(mCacheSign != null){
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
                    float resultTemper = (float) msg.obj;
                    if(resultTemper <= 0.0f){
                        break;
                    }

                    isResultShown = true;
                    removeMessages(-2);//清除距离提示
                    removeMessages(-1);//清除温度提示
                    KDXFSpeechManager.instance().stopNormal();
                    KDXFSpeechManager.instance().stopWarningRing();

                    int bgId = getBgId(resultTemper);
                    String resultText = getResultText(mFEnabled,resultTemper);//获取显示文字
                    String speechText = getSpeechText(mFEnabled,resultTemper);//获取阅读文字
                    resultRunnable = speechCallback(resultTemper);//获取后续操作
                    Log.e(TAG, "handleMessage: 显示文字：" + resultText);
                    Log.e(TAG, "handleMessage: 阅读文字：" + speechText);

                    showUIResult(resultText, bgId);
                    KDXFSpeechManager.instance().playNormal(speechText, resultRunnable);
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
                    SignManager.instance().uploadTemperatureSign(sign,mPrivacyMode);
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
    private Runnable speechCallback(float temperature){
        if(temperature >= mTempWarningThreshold){
            ledRed();
            return resultRunnable = () -> {
                KDXFSpeechManager.instance().playWaningRing();
                sendClearMessage();
            };
        } else {
            ledGreen();
            return resultRunnable = () -> sendClearMessage();
        }
    }
    private int getBgId(float temperature){
        if(temperature > 45.0f){
            return R.mipmap.bg_verify_nopass;
        } else if(temperature >= mTempWarningThreshold){
            return R.mipmap.bg_verify_nopass;
        } else {
            return R.mipmap.bg_verify_pass;
        }
    }
    private String getSpeechText(boolean fEnabled, float temperature) {
        StringBuffer stringBuffer = new StringBuffer();
        if(temperature > 45.0f){
            return getResources().getString(R.string.main_temp_error_tips);
        }

        if(temperature >= mTempWarningThreshold){
            String warningText = SpUtils.getStr(SMTModelConst.key.WARNING_BROADCAST,SMTModelConst.Default.WARNING_BROADCAST);
            stringBuffer.append(TextUtils.isEmpty(warningText) ? getResources().getString(R.string.main_temp_warning_tips) : warningText);
        } else {
            String normalText = SpUtils.getStr(SMTModelConst.key.NORMAL_BROADCAST,SMTModelConst.Default.NORMAL_BROADCAST);
            stringBuffer.append(TextUtils.isEmpty(normalText) ? getResources().getString(R.string.main_temp_normal_tips) : normalText);
        }

        //设置语音播报
        Locale locale = APP.getContext().getResources().getConfiguration().locale;
        boolean isChina = TextUtils.equals(locale.getLanguage(), "zh");
        if (fEnabled) {
            stringBuffer.append(" " + formatF((float) (temperature * 1.8 + 32)));
            stringBuffer.append(isChina ? "华氏度" : "Fahrenheit degree");
        } else {
            stringBuffer.append(" " + temperature);
            stringBuffer.append(isChina ? "摄氏度" : "Centigrade");
        }
        return stringBuffer.toString();
    }

    private String getResultText(boolean fEnabled,float temperature){
        StringBuffer stringBuffer = new StringBuffer();
        if(temperature > 45.0f){
            return getResources().getString(R.string.main_temp_error_tips);
        }

        if(temperature >= mTempWarningThreshold){
            String warningText = SpUtils.getStr(SMTModelConst.key.WARNING_BROADCAST,SMTModelConst.Default.WARNING_BROADCAST);
            stringBuffer.append(TextUtils.isEmpty(warningText) ? getResources().getString(R.string.main_temp_warning_tips) : warningText);
        } else {
            String normalText = SpUtils.getStr(SMTModelConst.key.NORMAL_BROADCAST,SMTModelConst.Default.NORMAL_BROADCAST);
            stringBuffer.append(TextUtils.isEmpty(normalText) ? getResources().getString(R.string.main_temp_normal_tips) : normalText);
        }
        stringBuffer.append(" ").append(fEnabled ? formatF((float) (temperature * 1.8 + 32)) : temperature);
        stringBuffer.append(fEnabled ? "℉" :"℃");
        return stringBuffer.toString();
    }

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
}
