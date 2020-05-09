package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in_smt;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.callback.Smt3232TempCallBack;
import com.yunbiao.faceview.CompareResult;
import com.yunbiao.faceview.FacePreviewInfo;
import com.yunbiao.faceview.FaceView;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.ThermalConst;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.reactivex.functions.Consumer;

public abstract class SMTTempBase2Activity extends SMTBaseActivity {
    private static final String TAG = "SMTTempBase2Activity";
    private int mCurrMode = -999;
    private long mSpeechDelay;
    private Float mTempMinThreshold;
    private Float mTempWarningThreshold;
    private long mCacheTime = 0;
    private boolean mLowTemp;
    private Float mAmbCorrValue;
    private Float mTempCorrValue;
    private boolean mDistanceTipEnable;
    private boolean mFEnabled;
    private boolean isResultShown = false;
    private SMTViewInterface viewInterface;
    private boolean mHasFace = false;

    @Override
    protected void initData() {
        super.initData();
        KDXFSpeechManager.instance().init(this);
        viewInterface = setViewInterface();
        if (viewInterface == null) {
            viewInterface = new SMTViewInterAbsImp();
        }
    }

    protected abstract SMTViewInterface setViewInterface();

    protected abstract void setMaskDetectEnabled(boolean isMaskDetectEnabled);

    @Override
    protected void onResume() {
        super.onResume();
        mSpeechDelay = SpUtils.getLong(SMTModelConst.key.SPEECH_DELAY, SMTModelConst.Default.SPEECH_DELAY);
        mTempMinThreshold = SpUtils.getFloat(SMTModelConst.key.TEMP_MIN_THRESHOLD, SMTModelConst.Default.TEMP_MIN_THRESHOLD);
        mTempWarningThreshold = SpUtils.getFloat(SMTModelConst.key.TEMP_WARNING_THRESHOLD, SMTModelConst.Default.TEMP_WARNING_THRESHOLD);
        mLowTemp = SpUtils.getBoolean(SMTModelConst.key.LOW_TEMP, SMTModelConst.Default.LOW_TEMP);
        mAmbCorrValue = SpUtils.getFloat(SMTModelConst.key.AMB_CORRECT_VALUE, SMTModelConst.Default.AMB_CORRECT_VALUE);
        mTempCorrValue = SpUtils.getFloat(SMTModelConst.key.TEMP_CORRECT_VALUE, SMTModelConst.Default.TEMP_CORRECT_VALUE);
        mDistanceTipEnable = SpUtils.getBoolean(SMTModelConst.key.DISTANCE_TIP_ENABLE, SMTModelConst.Default.DISTANCE_TIP_ENABLE);
        mFEnabled = SpUtils.getBoolean(SMTModelConst.key.F_ENABLED, SMTModelConst.Default.F_ENABLED);

        int currMode = SpUtils.getIntOrDef(SMTModelConst.key.MODE, SMTModelConst.Default.MODE);
        if (mCurrMode != currMode) {
            mCurrMode = currMode;
            viewInterface.onModeChanged(currMode);
        }

        if (mCurrMode == SMTModelConst.SMT_TEMP_ONLY || mCurrMode == SMTModelConst.SMT_FACE_TEMP) {
            Log.e(TAG, "onResume: 开启测温模块");
            TemperatureModule.getIns().initSerialPort(this, "/dev/ttyS3", 115200);
            updateUIHandler.postDelayed(() -> TemperatureModule.getIns().startSmt3232Temp(mLowTemp, smt3232TempCallBack), 2000);
        }
        TemperatureModule.getIns().setaCorrectionValue(mAmbCorrValue);
        TemperatureModule.getIns().setmCorrectionValue(mTempCorrValue);
    }

    @Override
    protected void onStop() {
        super.onStop();
        TemperatureModule.getIns().closeSmt3232Temp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TemperatureModule.getIns().closeSmt3232Temp();
        TemperatureModule.getIns().closeSerialPort();
    }

    private final float HIGHEST_TEMPER = 45.0f;//最高提示温度值
    private List<Float> mCacheTemperList = Collections.synchronizedList(new ArrayList<Float>());//最终温度结果集
    private boolean isFaceToFar = false;
    private int distanceTipNumber = 0;
    private int maskTipNumber = 0;
    private int maskTag = 0;
    private Smt3232TempCallBack smt3232TempCallBack = new Smt3232TempCallBack() {
        @Override
        public void newestSmt3232Temp(float measureF, float afterF) {
            if (mCurrMode == SMTModelConst.SMT_FACE_ONLY) {
                return;
            }

            if (!mHasFace) {
                if (distanceTipNumber != 0) {
                    distanceTipNumber = 0;
                }
                if (maskTipNumber != 0) {
                    maskTipNumber = 0;
                }
                mCacheTime = 0;
                if (mCacheTemperList.size() > 0) {
                    mCacheTemperList.clear();
                }
                sendClearAllUIMessage();
                return;
            }

            if (isFaceToFar && !isResultShown) {
                if (mCacheTime != 0) mCacheTime = 0;
                if (mCacheTemperList.size() > 0) mCacheTemperList.clear();
                String resString = getResString(R.string.main_tips_please_close);
                sendTipsMessage(resString);
                if (mDistanceTipEnable && distanceTipNumber < 5) KDXFSpeechManager.instance().playNormalAdd(resString, () -> distanceTipNumber++);
                return;
            }

            //如果处理后的数值小于播报阈值，清除最终值
            if (afterF < mTempMinThreshold) {
                if (mCacheTime != 0) mCacheTime = 0;
                if (mCacheTemperList.size() > 0) mCacheTemperList.clear();
                return;
            }

            if (mCurrMode == SMTModelConst.SMT_TEMP_ONLY) {
                if (mCacheTime == 0 || System.currentTimeMillis() - mCacheTime > mSpeechDelay) {
                    mCacheTemperList.add(afterF);
                    if (mCacheTemperList.size() < 3) {
                        return;
                    }
                    mCacheTime = System.currentTimeMillis();

                    Float max = Collections.max(mCacheTemperList);
                    mCacheTemperList.clear();

                    float resultTemper = formatF(max);
                    sendResultMessage(resultTemper, "");//发送结果
                    if (resultTemper >= mTempMinThreshold && resultTemper < mTempWarningThreshold) {
                        openDoor();
                    }

                    if(resultTemper < HIGHEST_TEMPER){
                        Sign temperatureSign = SignManager.instance().getTemperatureSign(resultTemper);
                        SignManager.instance().uploadTemperatureSign(viewInterface.getFacePicture(), null, temperatureSign, false, new Consumer<Sign>() {
                            @Override
                            public void accept(Sign sign) throws Exception {
                                sendUpdateSignMessage(sign);//发送列表更新事件
                            }
                        });
                    }
                }
            } else {
                mCacheTemperList.add(afterF);
            }
        }
    };

    private float getResultTemperForFandT() {
        if (mCacheTemperList.size() <= 0) {
            return 0.0f;
        }
        Float max = Collections.max(mCacheTemperList);
        mCacheTemperList.clear();
        return max;
    }

    private View mDistanceView;
    //===人脸相关=================================================================================
    //===人脸相关=================================================================================
    //===人脸相关=================================================================================
    //===人脸相关=================================================================================
    //===人脸相关=================================================================================
    //===人脸相关=================================================================================
    //===人脸相关=================================================================================
    //===人脸相关=================================================================================
    //===人脸相关=================================================================================
    //===人脸相关=================================================================================
    FaceView.FaceCallback faceCallback = new FaceView.FaceCallback() {
        @Override
        public void onReady() {
            viewInterface.onFaceViewReady();
            mDistanceView = viewInterface.getDistanceView();
        }

        @Override
        public void onFaceDetection(Boolean hasFace, List<FacePreviewInfo> facePreviewInfoList) {

        }

        @Override
        public boolean onFaceDetection(boolean hasFace, FacePreviewInfo facePreviewInfo) {
            mHasFace = hasFace;
            viewInterface.hasFace(hasFace);
            if (!hasFace) {
                if (mCurrMode == ThermalConst.FACE_ONLY) {
                    maskTipNumber = 0;
                    sendClearMaskTipMessage(0);
                }
                return false;
            }

            //如果是人脸模式
            if (mCurrMode == ThermalConst.FACE_ONLY) {
                return true;
            }

            //如果限制View为null
            if (mDistanceView == null) {
                return false;
            }

            //更新距离标签
            Rect rect = facePreviewInfo.getFaceInfo().getRect();
            int distance = mDistanceView.getMeasuredWidth();
            //如果人脸太远
            isFaceToFar = checkFaceToFar(rect, distance);
            if (isFaceToFar) {
                return false;
            }

            if(mCurrMode == SMTModelConst.SMT_TEMP_ONLY){
                return false;
            }

            if(mCacheTemperList.size() < 3){
                return false;
            }

            return true;
        }

        @Override
        public void onFaceVerify(CompareResult faceAuth) {
            if(mCurrMode == SMTModelConst.SMT_FACE_ONLY){
                if (faceAuth == null || faceAuth.getSimilar() == -1) {
                    return;
                }
                Sign sign = SignManager.instance().checkSignData(faceAuth, 0.0f);
                if (sign == null) {
                    return;
                }
                KDXFSpeechManager.instance().playNormal(sign.getName());
                viewInterface.updateSignList(sign);

                if (sign.getType() == -2) {
                    return;
                }
                openDoor();
            } else {
                float resultTemper = getResultTemperForFandT();
                if (resultTemper <= 0.0f) {
                    return;
                }

                if (resultTemper >= HIGHEST_TEMPER) {
                    sendResultMessage(resultTemper, "");
                    return;
                }

                Log.e(TAG, "onFaceVerify: 识别到的FaceId：" + faceAuth.getUserName());

                Sign sign = null;
                if (!TextUtils.equals("-1", faceAuth.getUserName())) {
                    sign = SignManager.instance().checkSignData(faceAuth, resultTemper);
                }
                if (sign == null) {
                    sign = SignManager.instance().getTemperatureSign(resultTemper);
                }
                Log.e(TAG, "onFaceVerify: 识别的用户名：" + sign.getName());

                sendResultMessage(resultTemper, sign.getName());

                SignManager.instance().uploadTemperatureSign(viewInterface.getFacePicture(), null, sign, false, new Consumer<Sign>() {
                    @Override
                    public void accept(Sign sign) throws Exception {
                        viewInterface.updateSignList(sign);
                    }
                });

                if (sign.getType() == -2 || sign.getType() == -9) {
                    return;
                }
                openDoor();
            }
        }
    };

    //检查人脸距离
    public boolean checkFaceToFar(Rect faceRect, int distance) {
        int faceWidth = faceRect.right - faceRect.left;
        return faceWidth < distance;
    }

    //==UI相关============================================================================================
    //==UI相关============================================================================================
    //==UI相关============================================================================================
    //==UI相关============================================================================================
    //==UI相关============================================================================================
    //==UI相关============================================================================================
    //==UI相关============================================================================================
    //==UI相关============================================================================================
    //==UI相关============================================================================================
    //==UI相关============================================================================================
    //更新热成像图
    private void sendUpdateHotInfoMessage(Bitmap imageBmp, float maxT) {
        Message message = Message.obtain();
        message.what = 0;
        message.obj = imageBmp;
        Bundle bundle = new Bundle();
        bundle.putFloat("temper", maxT);
        message.setData(bundle);
        updateUIHandler.sendMessage(message);
    }

    //测温结果
    private void sendResultMessage(float temperature, String name) {
        Message message = Message.obtain();
        message.what = 1;
        message.obj = temperature;
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        message.setData(bundle);
        updateUIHandler.sendMessage(message);
    }

    //重置UI
    private void sendResetResultMessage() {
        Log.e(TAG, "sendResetResultMessage: 发送重置测温结果的消息");
        updateUIHandler.removeMessages(3);
        updateUIHandler.sendEmptyMessageDelayed(3, 5000);
    }

    //提示相关
    private void sendTipsMessage(String string) {
        if (viewInterface.isTipsShown()) {
            return;
        }
        updateUIHandler.removeMessages(2);
        Message message = Message.obtain();
        message.what = 2;
        message.obj = string;
        updateUIHandler.sendMessage(message);
    }

    //重置UI
    private void sendResetTipsMessage(int delay) {
        if (viewInterface.isTipsShown()) {
            updateUIHandler.removeMessages(4);
            updateUIHandler.sendEmptyMessageDelayed(4, delay);
        }
    }

    //发送口罩提示
    private void sendMaskTipMessage(String tip) {
        if (viewInterface.isMaskTipsShown()) {
            return;
        }
        Message message = Message.obtain();
        message.what = 7;
        message.obj = tip;
        updateUIHandler.sendMessageDelayed(message, 100);
    }

    //发送清除口罩提示的消息
    private void sendClearMaskTipMessage(long delay) {
        if (viewInterface.isMaskTipsShown()) {
            updateUIHandler.removeMessages(8);
            updateUIHandler.sendEmptyMessageDelayed(8, delay);
        }
    }

    //清除所有UI提示
    private void sendClearAllUIMessage() {
        if (viewInterface.isTipsShown() || viewInterface.isResultShown() || viewInterface.isMaskTipsShown()) {
            updateUIHandler.removeMessages(5);
            updateUIHandler.sendEmptyMessage(5);
        }
    }

    //发送更新签到列表的消息
    private void sendUpdateSignMessage(Sign sign) {
        Message message = Message.obtain();
        message.what = 6;
        message.obj = sign;
        updateUIHandler.sendMessage(message);
    }

    private Handler updateUIHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0://更新热成像图
                    Bitmap bitmap = (Bitmap) msg.obj;
                    float temper = msg.getData().getFloat("temper", 0.0F);
                    viewInterface.updateHotImage(bitmap, temper, mHasFace);
                    break;
                case 1://测温结果
                    float temperature = (float) msg.obj;
                    String name = msg.getData().getString("name");
                    if (temperature <= 0.0f) {
                        break;
                    }

                    updateUIHandler.removeMessages(3);//清除隐藏事件
                    updateUIHandler.sendEmptyMessage(4);
                    KDXFSpeechManager.instance().stopNormal();
                    KDXFSpeechManager.instance().stopWarningRing();
                    isResultShown = true;

                    int bgId = getBgId(temperature);
                    Runnable resultRunnable = speechCallback(temperature);
                    String resultText = getResultText(mFEnabled, temperature);
                    String speechText = getSpeechText(mFEnabled, temperature);
                    if (!TextUtils.isEmpty(name)) {
                        speechText += "，" + name;
                    }

                    viewInterface.showResult(resultText, bgId);
                    KDXFSpeechManager.instance().playNormal(speechText, resultRunnable);

                    sendResetResultMessage();
                    break;
                case 2://提示相关
                    int id;
                    if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                        id = R.mipmap.bg_verify_nopass;
                    } else {
                        id = R.drawable.shape_main_frame_temperature_warning;
                    }
                    String tips = (String) msg.obj;
                    viewInterface.showTips(tips, id);
                    break;
                case 3://重置UI
                    Log.e(TAG, "sendResetResultMessage: 清除UI");
                    isResultShown = false;
                    viewInterface.dismissResult();
                    KDXFSpeechManager.instance().stopWarningRing();
                    resetLedDelay(0);//5秒后重置灯光为蓝色
                    break;
                case 4://重置提示
                    viewInterface.dismissTips();
                    break;
                case 5://清除所有UI（除灯光和语音播报）
                    viewInterface.clearAllUI();
                    break;
                case 6://仅添加
                    Sign sign = (Sign) msg.obj;
                    viewInterface.updateSignList(sign);
                    break;
                case 7:
                    String tip = (String) msg.obj;
                    viewInterface.showMaskTip(tip);
                    break;
                case 8:
                    viewInterface.clearMaskTip();
                    break;
            }
            return true;
        }
    });

    //获取后续操作
    private Runnable speechCallback(float temperature) {
        if (temperature >= mTempWarningThreshold) {
            ledRed();
            return () -> {
                Log.e(TAG, "发送重置结果的UI 11111111111111111");
                KDXFSpeechManager.instance().playWaningRing();
//                sendResetResultMessage();
            };
        } else {
            ledGreen();
            if (mCurrMode == ThermalConst.INFRARED_ONLY || mCurrMode == ThermalConst.THERMAL_16_4_ONLY || mCurrMode == ThermalConst.THERMAL_ONLY) {
                openDoor();
            }
            return () -> {
                Log.e(TAG, "发送重置结果的UI 11111111111111111");
//                sendResetResultMessage();
            };
        }
    }

    //获取温度提示背景
    private int getBgId(float temperature) {
        if (temperature >= mTempWarningThreshold) {
            if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                return R.mipmap.bg_verify_nopass;
            } else {
                return R.drawable.shape_main_frame_temperature_warning;
            }
        } else {
            if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                return R.mipmap.bg_verify_pass;
            } else {
                return R.drawable.shape_main_frame_temperature_normal;
            }
        }
    }

    //获取播报内容
    private String getSpeechText(boolean fEnabled, float temperature) {
        StringBuffer stringBuffer = new StringBuffer();
        if (temperature > 45.0f) {
            return getResources().getString(R.string.main_temp_error_tips);
        }

        if (temperature >= mTempWarningThreshold) {
            String warningText = SpUtils.getStr(SMTModelConst.key.WARNING_BROADCAST, SMTModelConst.Default.WARNING_BROADCAST);
            stringBuffer.append(TextUtils.isEmpty(warningText) ? getResources().getString(R.string.main_temp_warning_tips) : warningText);
        } else {
            String normalText = SpUtils.getStr(SMTModelConst.key.NORMAL_BROADCAST, SMTModelConst.Default.NORMAL_BROADCAST);
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

    //获取播报文字
    private String getResultText(boolean fEnabled, float temperature) {
        StringBuffer stringBuffer = new StringBuffer();
        if (temperature > 45.0f) {
            return getResources().getString(R.string.main_temp_error_tips);
        }

        if (temperature >= mTempWarningThreshold) {
            String warningText = SpUtils.getStr(SMTModelConst.key.WARNING_BROADCAST, SMTModelConst.Default.WARNING_BROADCAST);
            stringBuffer.append(TextUtils.isEmpty(warningText) ? getResources().getString(R.string.main_temp_warning_tips) : warningText);
        } else {
            String normalText = SpUtils.getStr(SMTModelConst.key.NORMAL_BROADCAST, SMTModelConst.Default.NORMAL_BROADCAST);
            stringBuffer.append(TextUtils.isEmpty(normalText) ? getResources().getString(R.string.main_temp_normal_tips) : normalText);
        }
        stringBuffer.append(" ").append(fEnabled ? formatF((float) (temperature * 1.8 + 32)) : temperature);
        stringBuffer.append(fEnabled ? "℉" : "℃");
        return stringBuffer.toString();
    }

}
