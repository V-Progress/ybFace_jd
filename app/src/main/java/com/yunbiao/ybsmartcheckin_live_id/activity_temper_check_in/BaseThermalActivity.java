package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.callback.HotImageK1604CallBack;
import com.intelligence.hardware.temperature.callback.HotImageK3232CallBack;
import com.intelligence.hardware.temperature.callback.InfraredTempCallBack;
import com.yunbiao.faceview.CompareResult;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseGpioActivity;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.common.power.PowerOffTool;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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

    private long mSpeechDelay;//播报延迟
    protected int mCurrMode = -99;//当前模式
    private Float mTempMinThreshold;//温度播报阈值
    private Float mTempWarningThreshold;//温度报警阈值
    private float mTempCorrect;//温度补正
    private float mHighestTemp = 45.0f;//最高提示温度值
    private boolean mThermalImgMirror;//热成像镜像
    private boolean lowTempModel;//低温模式
    private boolean distanceTipEnable;//距离提示
    private boolean mFEnabled;//华氏度开关

    //最终温度结果集
    private List<Float> tempCacheList = new ArrayList<>();
    //缓存之前的温度
    private float mCacheBeforTemper = 0.0f;
    //缓存时间
    private long mCacheTime = 0;
    //缓存温差值
    private float mCacheDiffValue = 2.0f;
    //热成像画面
    private Bitmap mLastHotBitmap = null;
    private TypedArray noFaceArray;
    private TypedArray hasFaceArray;

    @Override
    protected void initData() {
        super.initData();
        noFaceArray = getResources().obtainTypedArray(R.array.noFaceArray);
        hasFaceArray = getResources().obtainTypedArray(R.array.hasFaceArray);
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
        mTempCorrect = SpUtils.getFloat(ThermalConst.Key.THERMAL_CORRECT, ThermalConst.Default.THERMAL_CORRECT);
        TemperatureModule.getIns().setmCorrectionValue(mTempCorrect);

        mFEnabled = SpUtils.getBoolean(ThermalConst.Key.THERMAL_F_ENABLED, ThermalConst.Default.THERMAL_F_ENABLED);

        //模式切换
        int currMode = SpUtils.getIntOrDef(ThermalConst.Key.MODE, ThermalConst.Default.MODE);//当前模式
        if (mCurrMode != currMode) {
            mCurrMode = currMode;
            onModeChanged(currMode);
        }

        //初始化串口号
        if (mCurrMode == ThermalConst.THERMAL_ONLY || mCurrMode == ThermalConst.FACE_THERMAL) {
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
        } else if (mCurrMode == ThermalConst.INFRARED_ONLY || mCurrMode == ThermalConst.FACE_INFRARED) {
            String portPath;
            if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                portPath = "/dev/ttyS1";
            } else {
                portPath = "/dev/ttyS4";
            }
            TemperatureModule.getIns().initSerialPort(this, portPath, 9600);
            TemperatureModule.getIns().setInfraredTempCallBack(infraredTempCallBack);
        } else if (mCurrMode == ThermalConst.THERMAL_16_4_ONLY || mCurrMode == ThermalConst.FACE_THERMAL_16_4) {
            String portPath;
            if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                Log.e(TAG, "onResume: 竖屏");
                portPath = "/dev/ttyS3";
            } else {
                Log.e(TAG, "onResume: 横屏");
                portPath = "/dev/ttyS4";
            }
            TemperatureModule.getIns().initSerialPort(this, portPath, 19200);
            updateUIHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    TemperatureModule.getIns().startHotImageK1604(mThermalImgMirror, lowTempModel, hotImageK1604CallBack);
                }
            },2000);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        TemperatureModule.getIns().closeHotImageK3232();
        TemperatureModule.getIns().setInfraredTempCallBack(null);
    }

    private Random random = new Random();

    private static final String TAG = "BaseThermalActivity";

    private Bitmap selectInfaredImage(boolean hasFace) {
        int i = random.nextInt(20);
        int id;
        if (hasFace) {
            id = hasFaceArray.getResourceId(i, R.mipmap.h_1);
        } else {
            id = noFaceArray.getResourceId(i, R.mipmap.n_1);
        }
        Log.e(TAG, "selectInfaredImage: 随机数：" + i + "-----资源id：" + id);

        return BitmapFactory.decodeStream(getResources().openRawResource(id));
    }

    private HotImageK1604CallBack hotImageK1604CallBack = new HotImageK1604CallBack() {
        @Override
        public void newestHotImageData(final Bitmap imageBmp, final float originalMaxT, final float maxT, final float minT) {
            if (isOnlyFace()) {
                return;
            }

            sendUpdateMessage(imageBmp, originalMaxT, maxT, mCacheBeforTemper, isHasFace);
            mLastHotBitmap = imageBmp;

            if (!isHasFace) {
                if (mCacheBeforTemper == 0.0f || originalMaxT < mCacheBeforTemper) {
                    mCacheBeforTemper = originalMaxT;
                }
                mCacheTime = 0;
                sendClearTempTipsMessage();
                tempCacheList.clear();
                mFinalTemp = 0.0f;
                return;
            }

            if (isFaceToFar) {
                mCacheTime = 0;
                sendTempLowMessage(getResources().getString(R.string.main_tips_please_close));
                tempCacheList.clear();
                mFinalTemp = 0.0f;
                return;
            }


            if (originalMaxT - mCacheBeforTemper < mCacheDiffValue) {
                return;
            }

            if (maxT < mTempMinThreshold) {
                mCacheTime = 0;
                tempCacheList.clear();
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
                    mFinalTemp += mTempCorrect;

                    float currDiffValue = originalMaxT - mCacheBeforTemper - 3.0f;
                    mCacheDiffValue = mCacheDiffValue == 2.0f
                            //判断当前差值是否大于2.0f，如果是则存值
                            ? Math.max(currDiffValue, mCacheDiffValue)
                            //判断当前差值是否大于2并且小于缓存差值，如果是则存值
                            : (currDiffValue > 2.0f && currDiffValue < mCacheDiffValue ? currDiffValue : mCacheDiffValue);
                }

                mFinalTemp = formatF(mFinalTemp);
                sendTempTipsMessage(mFinalTemp, imageBmp);
                if (mFinalTemp < mHighestTemp) {
                    if (mCurrMode == ThermalConst.THERMAL_16_4_ONLY) {
                        Sign temperatureSign = SignManager.instance().getTemperatureSign(mFinalTemp);
                        temperatureSign.setImgBitmap(getCurrCameraFrame());
                        temperatureSign.setHotImageBitmap(mLastHotBitmap);
                        sendUploadMessage(temperatureSign);
                    }
                }
            }
        }

        @Override
        public void dataRecoveryFailed() {
            showRestartAlert(getResString(R.string.temper_error_tips),getResString(R.string.temper_error_btn_restart), () -> PowerOffTool.getPowerOffTool().restart());
        }
    };

    private InfraredTempCallBack infraredTempCallBack = new InfraredTempCallBack() {
        @Override
        public void newestInfraredTemp(float measureF, float afterF, float ambientF) {
            if (isOnlyFace()) {
                return;
            }

            Bitmap bitmap = selectInfaredImage(isHasFace);
            sendUpdateMessage(bitmap, measureF, afterF, mCacheBeforTemper, isHasFace);

            if (!isHasFace) {
                if (mCacheBeforTemper == 0.0f || measureF < mCacheBeforTemper) {
                    mCacheBeforTemper = measureF;
                }
                sendClearTempTipsMessage();
                mCacheTime = 0;
                tempCacheList.clear();
                mFinalTemp = 0.0f;
                return;
            }

            if (isFaceToFar) {
                sendTempLowMessage(getResources().getString(R.string.main_tips_please_close));
                mCacheTime = 0;
                tempCacheList.clear();
                mFinalTemp = 0.0f;
                return;
            }

            if (!isInsideRange) {
                sendTempLowMessage("请对准人脸框");
                mCacheTime = 0;
                tempCacheList.clear();
                mFinalTemp = 0.0f;
                return;
            }

            if (measureF - mCacheBeforTemper < 2.0f) {
                mCacheTime = 0;
                tempCacheList.clear();
                mFinalTemp = 0.0f;
                return;
            }

            if (afterF < mTempMinThreshold) {
                mCacheTime = 0;
                tempCacheList.clear();
                mFinalTemp = 0.0f;
                return;
            }

            if (mCacheTime == 0 || System.currentTimeMillis() - mCacheTime > mSpeechDelay) {
                tempCacheList.add(afterF);
                if (tempCacheList.size() < 2) {
                    return;
                }
                mCacheTime = System.currentTimeMillis();

                Float max = Collections.max(tempCacheList);
                tempCacheList.clear();
                mFinalTemp = max;

                if (mFinalTemp < mTempWarningThreshold) {
                    mFinalTemp += mTempCorrect;
                }

                mFinalTemp = formatF(mFinalTemp);
                sendTempTipsMessage(mFinalTemp, null);

                if (mFinalTemp < mHighestTemp) {
                    //仅测温模式下发送陌生人记录
                    if (mCurrMode == ThermalConst.INFRARED_ONLY) {
                        Sign temperatureSign = SignManager.instance().getTemperatureSign(mFinalTemp);
                        temperatureSign.setImgBitmap(getCurrCameraFrame());
                        sendUploadMessage(temperatureSign);
                    }
                }
            }
        }
    };

    private HotImageK3232CallBack imageK3232CallBack = new HotImageK3232CallBack() {
        @Override
        public void newestHotImageData(final Bitmap imageBmp, final float sensorT, final float maxT, final float minT, final float bodyMaxT, final boolean isBody, final int bodyPercentage) {
            if (isOnlyFace()) {
                return;
            }

            sendUpdateMessage(imageBmp, sensorT, maxT, mCacheBeforTemper, isHasFace);
            mLastHotBitmap = imageBmp;

            if (!isHasFace) {
                if (mCacheBeforTemper == 0.0f || sensorT < mCacheBeforTemper) {
                    mCacheBeforTemper = sensorT;
                }
                mCacheTime = 0;
                sendClearTempTipsMessage();
                tempCacheList.clear();
                mFinalTemp = 0.0f;
                return;
            }

            if (isFaceToFar) {
                mCacheTime = 0;
                sendTempLowMessage(getResources().getString(R.string.main_tips_please_close));
                tempCacheList.clear();
                mFinalTemp = 0.0f;
                return;
            }

            if (sensorT - mCacheBeforTemper < mCacheDiffValue) {
                return;
            }

            if (maxT < mTempMinThreshold) {
                mCacheTime = 0;
                tempCacheList.clear();
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
                    mFinalTemp += mTempCorrect;


                    float currDiffValue = sensorT - mCacheBeforTemper - 3.0f;
                    mCacheDiffValue = mCacheDiffValue == 2.0f
                            //判断当前差值是否大于2.0f，如果是则存值
                            ? Math.max(currDiffValue, mCacheDiffValue)
                            //判断当前差值是否大于2并且小于缓存差值，如果是则存值
                            : (currDiffValue > 2.0f && currDiffValue < mCacheDiffValue ? currDiffValue : mCacheDiffValue);
                }

                mFinalTemp = formatF(mFinalTemp);
                sendTempTipsMessage(mFinalTemp, imageBmp);
                if (mFinalTemp < mHighestTemp) {
                    if (mCurrMode == ThermalConst.THERMAL_ONLY) {
                        Sign temperatureSign = SignManager.instance().getTemperatureSign(mFinalTemp);
                        temperatureSign.setImgBitmap(getCurrCameraFrame());
                        temperatureSign.setHotImageBitmap(mLastHotBitmap);
                        sendUploadMessage(temperatureSign);
                    }
                }
            }
        }

        @Override
        public void dataRecoveryFailed() {
            showRestartAlert(getResString(R.string.temper_error_tips),getResString(R.string.temper_error_btn_restart), () -> PowerOffTool.getPowerOffTool().restart());
        }
    };

    //人脸逻辑判断======================================================
    private boolean isHasFace = false;
    private Sign mCacheSign;

    //更新是否有人脸的标签
    protected void updateHasFace(boolean hasFace) {
        isHasFace = hasFace;
        if (!hasFace) {
            mCacheSign = null;
            isFaceToFar = false;
        }
    }

    //更新trackId
    private int mCacheTrackId = -1;

    protected void updateCacheSign(int trackId) {
        if (mCacheTrackId != trackId) {
            mCacheTrackId = trackId;
            mCacheSign = null;
        }
    }

    //更新人脸距离
    private boolean isFaceToFar = true;//人脸是否太远

    protected boolean updateFaceState(boolean isFar) {
        isFaceToFar = isFar;
        return isFaceToFar;
    }

    //更新人脸范围
    private boolean isInsideRange = false;

    protected void checkFaceRange(boolean isInside) {
        isInsideRange = isInside;
    }

    //是否有最终值
    private float mFinalTemp = 0.0f;

    protected boolean hasFinalTemp() {
        return mFinalTemp != 0.0f;
    }

    //模式判断=================================================
    protected boolean isOnlyFace() {
        return mCurrMode == ThermalConst.FACE_ONLY;
    }

    protected boolean isOnlyTemp() {
        return mCurrMode == ThermalConst.THERMAL_ONLY || mCurrMode == ThermalConst.INFRARED_ONLY || mCurrMode == ThermalConst.THERMAL_16_4_ONLY;
    }

    protected boolean isInfared() {
        return mCurrMode == ThermalConst.FACE_INFRARED || mCurrMode == ThermalConst.INFRARED_ONLY;
    }

    //UI显示============================================================================
    //模式发生改变
    protected abstract void onModeChanged(int mode);

    //更新热成像图像
    protected abstract void updateHotImageAndTemper(Bitmap bitmap, float temper, float maxT, float cacheT, boolean hasPerson);

    //显示距离提示
    protected abstract boolean showStableTips(String tip, int stableTipsId);

    //如果提示框正在显示
    protected boolean isTempTipsShown() {
        return false;
    }

    //仅清除提示框
    protected void clearTempTips() {
    }

    //显示测温结果
    protected abstract void showUIResult(String tip, int id);

    //获取当前截图
    protected abstract Bitmap getCurrCameraFrame();

    //更新列表
    protected void updateSignList(Sign sign) {
    }

    //清除所有提示UI
    protected abstract void clearUI();

    //发送更新UI的消息===================================================================
    //发送清除提示框的消息
    private void sendClearTempTipsMessage() {
        if (isTempTipsShown()) {
            updateUIHandler.sendEmptyMessage(-3);
        }
    }

    //距离提示
    private boolean isResultShown = false;

    private void sendTempLowMessage(String tips) {
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
    private void sendUpdateMessage(Bitmap bitmap, float temper, float maxT, float cacheTemper, boolean hasPerson) {
        Message message = Message.obtain();
        message.what = 0;
        message.obj = bitmap;
        Bundle bundle = new Bundle();
        bundle.putFloat("temper", temper);
        bundle.putFloat("maxTemper", maxT);
        bundle.putFloat("cacheTemper", cacheTemper);
        bundle.putBoolean("hasPerson", hasPerson);
        message.setData(bundle);
        updateUIHandler.sendMessage(message);
    }

    //发送温度提示
    private void sendTempTipsMessage(float temper, Bitmap hotImage) {
        Message message = Message.obtain();
        message.what = 1;
        message.obj = temper;
        /*message.obj = hotImage;
        Bundle bundle = new Bundle();
        bundle.putFloat("temper", temper);
        message.setData(bundle);*/
        updateUIHandler.sendMessage(message);
    }

    //发送上传消息
    private void sendUploadMessage(Sign sign) {
        Message message = Message.obtain();
        message.what = 2;
        message.obj = sign;
        updateUIHandler.sendMessage(message);
    }

    //发送人脸识别结果
    protected void sendFaceTempMessage(Bitmap facePicture, CompareResult compareResult) {
        if (mFinalTemp > mHighestTemp) {
            return;
        }
        Sign sign = null;
        if (TextUtils.equals(compareResult.getUserName(), "-1")) {
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

    //获取结束时的执行事件
    private Runnable resultRunnable;

    protected Runnable getResultRunnable() {
        return resultRunnable;
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
                    boolean hasPerson = data.getBoolean("hasPerson");
                    updateHotImageAndTemper(bitmap, temper, maxTemper, cacheTemper, hasPerson);
                    break;
                case 1://仅提示温度
                    isResultShown = true;
                    removeMessages(-1);
                    removeMessages(-3);
                    KDXFSpeechManager.instance().stopNormal();
                    KDXFSpeechManager.instance().stopWarningRing();

                    int bgId;
                    String resultTip;
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
                                Log.e(TAG, "run: 语音播报完毕");
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
                                Log.e(TAG, "run: 语音播报完毕");
                                KDXFSpeechManager.instance().playWaningRing();
                                sendClearMessage();
                            }
                        };
                    }

                    //换算华氏度
                    if (resultTemper <= mHighestTemp) {
                        //华氏度
                        if (mFEnabled) {
                            resultTemper = (float) (resultTemper * 1.8 + 32);
                            resultTemper = formatF(resultTemper);
                            resultTip += " " + resultTemper;
                        } else {
                            resultTip += " " + resultTemper;
                        }
                    }

                    //设置文字提示
                    String textTip;
                    if (resultTemper < mHighestTemp) {
                        if (mFEnabled) {
                            textTip = resultTip + " ℉";
                        } else {
                            textTip = resultTip + " ℃";
                        }
                    } else {
                        textTip = resultTip;
                    }

                    showUIResult(textTip, bgId);

                    //设置语音播报
                    Locale locale = APP.getContext().getResources().getConfiguration().locale;
                    boolean isChina = TextUtils.equals(locale.getCountry(), Locale.CHINA.getCountry());
                    String speechTip;
                    if (resultTemper < mHighestTemp) {
                        if (mFEnabled) {
                            speechTip = resultTip + (isChina ? "华氏度" : "Fahrenheit degree");
                        } else {
                            speechTip = resultTip + (isChina ? "摄氏度" : "Centigrade");
                        }
                    } else {
                        speechTip = resultTip;
                    }

                    Log.e(TAG, "handleMessage: 执行语音播报");
                    KDXFSpeechManager.instance().playNormal(speechTip, resultRunnable);

                    //如果是考勤测温且缓存Sign不为null则继续上传该人的信息
                    if (mCurrMode == ThermalConst.FACE_THERMAL || mCurrMode == ThermalConst.FACE_INFRARED || mCurrMode == ThermalConst.FACE_THERMAL_16_4) {
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
                    if (mCurrMode == ThermalConst.THERMAL_ONLY || mCurrMode == ThermalConst.INFRARED_ONLY || mCurrMode == ThermalConst.THERMAL_16_4_ONLY) {
                        if (sign.getTemperature() < mTempWarningThreshold) {
                            openDoor();
                        }
                    } else if (mCurrMode == ThermalConst.FACE_THERMAL || mCurrMode == ThermalConst.FACE_INFRARED || mCurrMode == ThermalConst.FACE_THERMAL_16_4) {
                        if (sign.getType() != -2 && sign.getType() != -9 && sign.getTemperature() < mTempWarningThreshold) {
                            openDoor();
                        }
                    }
                    updateSignList(sign);
                    SignManager.instance().uploadTemperatureSign(sign,true);
                    break;
                case 3:

                    break;
                case -1:
                    Log.e(TAG, "handleMessage: 重置灯光");
                    isResultShown = false;
                    clearUI();
                    KDXFSpeechManager.instance().stopWarningRing();
                    resetLedDelay(0);//5秒后重置灯光为蓝色
                    break;
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        TemperatureModule.getIns().closeSerialPort();
        TemperatureModule.getIns().closeHotImageK3232();
    }
}
