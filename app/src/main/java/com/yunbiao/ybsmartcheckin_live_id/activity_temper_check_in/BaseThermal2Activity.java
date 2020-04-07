package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.View;

import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.callback.HotImageK1604CallBack;
import com.intelligence.hardware.temperature.callback.HotImageK3232CallBack;
import com.intelligence.hardware.temperature.callback.InfraredTempCallBack;
import com.yunbiao.faceview.CompareResult;
import com.yunbiao.faceview.FacePreviewInfo;
import com.yunbiao.faceview.FaceView;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseGpioActivity;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public abstract class BaseThermal2Activity extends BaseGpioActivity implements FaceView.FaceCallback {
    private static final String TAG = "BaseThermalActivity";
    private long mSpeechDelay;//播报延迟
    protected int mCurrMode = -99;//当前模式
    private final float HIGHEST_TEMPER = 45.0f;//最高提示温度值
    private Float mTempMinThreshold;//温度播报阈值
    private Float mTempWarningThreshold;//温度报警阈值
    private float mTempCorrect;//温度补正
    private boolean mThermalImgMirror;//热成像镜像
    private boolean lowTempModel;//低温模式
    private boolean distanceTipEnable;//距离提示
    private boolean mFEnabled;//华氏度开关

    private Random random = new Random();
    private TypedArray noFaceArray;
    private TypedArray hasFaceArray;

    private Bitmap mLastHotImage;
    private List<Float> mCacheTemperList = new ArrayList<>();//最终温度结果集
    private float mCacheBeforeTemper = 0.0f;//缓存之前的温度
    private long mCacheTime = 0;//缓存时间
    private float mCacheDiffValue = 2.0f;//缓存温差值
    private boolean mHasFace = false;//是否有人脸
    private boolean isFaceToFar = true;//人脸距离
    private boolean isFaceInsideRange = false;//人脸是否在范围内（仅红外）
    private View mDistanceView;//范围限制View
    private boolean isResultShown = false;//测温结果是否正在显示
    private Rect mAreaRect = new Rect();//范围限制View的Rect

    private ThermalViewInterface viewInterface;

    @Override
    protected void initData() {
        super.initData();
        noFaceArray = getResources().obtainTypedArray(R.array.noFaceArray);
        hasFaceArray = getResources().obtainTypedArray(R.array.hasFaceArray);
        viewInterface = setViewInterface();
        if (viewInterface == null) {
            viewInterface = new ThermalViewInterAbsImp();
        }
    }

    protected abstract ThermalViewInterface setViewInterface();

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
        int currMode = SpUtils.getIntOrDef(SpUtils.THERMAL_MODEL_SETTING, ThermalConst.DEFAULT_THERMAL_MODEL);//当前模式
        if (mCurrMode != currMode) {
            mCurrMode = currMode;
            viewInterface.onModeChanged(currMode);
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
                portPath = "/dev/ttyS3";
            } else {
                portPath = "/dev/ttyS4";
            }
            TemperatureModule.getIns().initSerialPort(this, portPath, 19200);
            updateUIHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    TemperatureModule.getIns().startHotImageK1604(mThermalImgMirror, lowTempModel, hotImageK1604CallBack);
                }
            }, 2000);
        }
    }

    //==测温相关============================================================================================================
    //==测温相关============================================================================================================
    //==测温相关============================================================================================================
    //==测温相关============================================================================================================
    //==测温相关============================================================================================================
    //==测温相关============================================================================================================
    //==测温相关============================================================================================================
    //==测温相关============================================================================================================
    //==测温相关============================================================================================================
    //==测温相关============================================================================================================
    private Bitmap selectInfaredImage(boolean hasFace) {
        int i = random.nextInt(20);
        int id;
        if (hasFace) {
            id = hasFaceArray.getResourceId(i, R.mipmap.h_1);
        } else {
            id = noFaceArray.getResourceId(i, R.mipmap.n_1);
        }
        return BitmapFactory.decodeStream(getResources().openRawResource(id));
    }

    private HotImageK1604CallBack hotImageK1604CallBack = new HotImageK1604CallBack() {
        @Override
        public void newestHotImageData(final Bitmap imageBmp, final float originalMaxT, final float maxT, final float minT) {
            handleTemperature(imageBmp, originalMaxT, maxT);
        }
    };
    private InfraredTempCallBack infraredTempCallBack = new InfraredTempCallBack() {
        @Override
        public void newestInfraredTemp(float measureF, float afterF, float ambientF) {
            handleTemperature(null, measureF, afterF);
        }
    };
    private HotImageK3232CallBack imageK3232CallBack = new HotImageK3232CallBack() {
        @Override
        public void newestHotImageData(final Bitmap imageBmp, final float sensorT, final float maxT, final float minT, final float bodyMaxT, final boolean isBody, final int bodyPercentage) {
            handleTemperature(imageBmp, sensorT, maxT);
        }
    };

    //温度处理的主要逻辑
    private void handleTemperature(Bitmap imageBmp, float originT, float afterT) {
        if (mCurrMode == ThermalConst.FACE_ONLY) {
            return;
        }

        if (imageBmp == null) {
            imageBmp = selectInfaredImage(mHasFace);
        }

        mLastHotImage = imageBmp;
        sendUpdateHotInfoMessage(imageBmp, originT);

        if (!mHasFace) {
            if (mCacheBeforeTemper == 0.0f || originT < mCacheBeforeTemper) {
                mCacheBeforeTemper = originT;
            }
            mCacheTime = 0;
            if (mCacheTemperList.size() > 0) {
                mCacheTemperList.clear();
            }
            sendClearAllUIMessage();
            return;
        }

        if (isFaceToFar) {
            mCacheTime = 0;
            if (mCacheTemperList.size() > 0) {
                mCacheTemperList.clear();
            }
            sendTipsMessage(getResources().getString(R.string.main_tips_please_close));
            return;
        }

        if (mCurrMode == ThermalConst.INFRARED_ONLY || mCurrMode == ThermalConst.FACE_INFRARED) {
            if (!isFaceInsideRange) {
                mCacheTime = 0;
                if (mCacheTemperList.size() > 0) {
                    mCacheTemperList.clear();
                }
                sendTipsMessage("请对准人脸框");
                return;
            }
        }

        if (originT - mCacheBeforeTemper < mCacheDiffValue) {
            return;
        }

        if (afterT < mTempMinThreshold) {
            mCacheTime = 0;
            if (mCacheTemperList.size() > 0) {
                mCacheTemperList.clear();
            }
            return;
        }

        final int temperListSize = mCurrMode == ThermalConst.INFRARED_ONLY || mCurrMode == ThermalConst.FACE_INFRARED ? 2 : 4;
        if (mCurrMode == ThermalConst.THERMAL_ONLY || mCurrMode == ThermalConst.INFRARED_ONLY || mCurrMode == ThermalConst.THERMAL_16_4_ONLY) {
            long currentTimeMillis = System.currentTimeMillis();
            if (mCacheTime != 0 && currentTimeMillis - mCacheTime < mSpeechDelay) {
                return;
            }
            mCacheTemperList.add(afterT);
            if (mCacheTemperList.size() < temperListSize) {
                return;
            }
            mCacheTime = currentTimeMillis;

            Float max = Collections.max(mCacheTemperList);
            mCacheTemperList.clear();

            float resultTemper = max;
            if (resultTemper < mTempWarningThreshold) {
                resultTemper += mTempCorrect;

                if (mCurrMode != ThermalConst.INFRARED_ONLY && mCurrMode != ThermalConst.FACE_INFRARED) {
                    float currDiffValue = originT - mCacheBeforeTemper - 3.0f;
                    mCacheDiffValue = mCacheDiffValue == 2.0f
                            //判断当前差值是否大于2.0f，如果是则存值
                            ? Math.max(currDiffValue, mCacheDiffValue)
                            //判断当前差值是否大于2并且小于缓存差值，如果是则存值
                            : (currDiffValue > 2.0f && currDiffValue < mCacheDiffValue ? currDiffValue : mCacheDiffValue);
                }
            }
            resultTemper = formatF(resultTemper);
            //发送结果
            sendResultMessage(resultTemper, "");

            if (resultTemper < HIGHEST_TEMPER) {
                //上传数据
                Sign temperatureSign = SignManager.instance().getTemperatureSign(resultTemper);
                Bitmap copyImage = imageBmp.copy(Bitmap.Config.ARGB_4444, false);
                temperatureSign.setHotImageBitmap(copyImage);
                temperatureSign.setImgBitmap(viewInterface.getFacePicture());
                sendUpdateSignMessage(temperatureSign);
                SignManager.instance().uploadTemperatureSign(temperatureSign);
            }
        } else if (mCurrMode == ThermalConst.FACE_THERMAL || mCurrMode == ThermalConst.FACE_INFRARED || mCurrMode == ThermalConst.FACE_THERMAL_16_4) {
            mCacheTemperList.add(afterT);
        }
    }

    private float getResultTemperForFandT() {
        if (mCacheTemperList.size() <= 0) {
            return 0.0f;
        }
        Float max = Collections.max(mCacheTemperList);
        mCacheTemperList.clear();
        return max;
    }

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
        //距离
        Rect rect = facePreviewInfo.getFaceInfo().getRect();
        int distance = mDistanceView.getMeasuredWidth();
        //如果人脸太远
        isFaceToFar = checkFaceToFar(rect, distance);
        if (isFaceToFar) {
            return false;
        }
        //如果是红外模式则判断人脸区域
        if (mCurrMode == ThermalConst.INFRARED_ONLY || mCurrMode == ThermalConst.FACE_INFRARED) {
            Rect realRect = viewInterface.getRealRect(rect);
            isFaceInsideRange = checkFaceInFrame(realRect, mDistanceView);
            if (!isFaceInsideRange) {
                return false;
            }
        }
        //判断是否仅测温模式
        if (mCurrMode == ThermalConst.INFRARED_ONLY || mCurrMode == ThermalConst.THERMAL_ONLY || mCurrMode == ThermalConst.THERMAL_16_4_ONLY) {
            return false;
        }
        //判断集合中的温度数据
        if (mCacheTemperList.size() < (mCurrMode == ThermalConst.FACE_INFRARED ? 2 : 3)) {
            return false;
        }
        return true;
    }

    @Override
    public void onFaceVerify(CompareResult faceAuth) {
        if (mCurrMode == ThermalConst.FACE_ONLY) {
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

            Bitmap facePicture = viewInterface.getFacePicture();
            Sign sign = null;
            if (!TextUtils.equals("-1", faceAuth.getUserName())) {
                sign = SignManager.instance().checkSignData(faceAuth, resultTemper);
            }
            if (sign == null) {
                sign = SignManager.instance().getTemperatureSign(resultTemper);
            }
            Log.e(TAG, "onFaceVerify: 识别的用户名：" + sign.getName());

            sign.setImgBitmap(facePicture);
            Bitmap copyHotImage = mLastHotImage.copy(Bitmap.Config.ARGB_4444, false);
            sign.setHotImageBitmap(copyHotImage);

            sendResultMessage(resultTemper, sign.getName());
            viewInterface.updateSignList(sign);

            SignManager.instance().uploadTemperatureSign(sign);
        }
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
        updateUIHandler.removeMessages(3);
        updateUIHandler.sendEmptyMessageDelayed(3, 3000);
    }

    //提示相关
    private void sendTipsMessage(String string) {
        if (isResultShown) {
            return;
        }
        updateUIHandler.removeMessages(2);
        Message message = Message.obtain();
        message.what = 2;
        message.obj = string;
        updateUIHandler.sendMessage(message);
    }

    //重置UI
    private void sendResetTipsMessage() {
        updateUIHandler.removeMessages(4);
        updateUIHandler.sendEmptyMessageDelayed(4, 3000);
    }

    //清除所有UI提示
    private void sendClearAllUIMessage() {
        if (viewInterface.isTipsShown() || viewInterface.isResultShown()) {
            updateUIHandler.removeMessages(5);
            updateUIHandler.sendEmptyMessage(5);
        }
    }

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
                    updateUIHandler.removeMessages(3);//清除隐藏事件
                    updateUIHandler.sendEmptyMessage(4);
                    KDXFSpeechManager.instance().stopNormal();
                    KDXFSpeechManager.instance().stopWarningRing();

                    float temperature = (float) msg.obj;
                    if (temperature <= 0.0f) {
                        break;
                    }

                    String name = msg.getData().getString("name");

                    isResultShown = true;

                    String resultTip = getTextTip(temperature);
                    int bgId = getTipBackground(temperature);
                    Runnable resultRunnable;
                    if (temperature < mTempWarningThreshold) {
                        ledGreen();
                        resultRunnable = new Runnable() {
                            @Override
                            public void run() {
                                sendResetResultMessage();
                            }
                        };
                    } else {
                        ledRed();
                        resultRunnable = new Runnable() {
                            @Override
                            public void run() {
                                KDXFSpeechManager.instance().playWaningRing();
                                sendResetResultMessage();
                            }
                        };
                    }

                    //设置文字提示
                    String textTip;
                    //换算华氏度
                    if (temperature < HIGHEST_TEMPER) {
                        //华氏度
                        if (mFEnabled) {
                            temperature = (float) (temperature * 1.8 + 32);
                            temperature = formatF(temperature);
                            resultTip += " " + temperature;
                            textTip = resultTip + " ℉";
                        } else {
                            resultTip += " " + temperature;
                            textTip = resultTip + " ℃";
                        }
                    } else {
                        textTip = resultTip;
                    }

                    viewInterface.showResult(textTip, bgId);
                    String speechText = getSpeechText(resultTip, temperature);
                    if (!TextUtils.isEmpty(name)) {
                        speechText += "，" + name;
                    }

                    KDXFSpeechManager.instance().playNormal(speechText, resultRunnable);
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
                    if (distanceTipEnable) {
                        KDXFSpeechManager.instance().playNormal(tips);
                    }
                    sendResetTipsMessage();
                    break;
                case 3://重置UI
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
            }
            return true;
        }
    });

    private int getTipBackground(float temperature) {
        int bgId;
        if (temperature < mTempWarningThreshold) {
            if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                bgId = R.mipmap.bg_verify_pass;
            } else {
                bgId = R.drawable.shape_main_frame_temperature_normal;
            }
        } else {
            if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                bgId = R.mipmap.bg_verify_nopass;
            } else {
                bgId = R.drawable.shape_main_frame_temperature_warning;
            }
        }
        return bgId;
    }

    private String getTextTip(float temperature) {
        String resultTip;
        if (temperature < mTempWarningThreshold) {
            String normalBroad = SpUtils.getStr(ThermalConst.Key.NORMAL_BROADCAST, ThermalConst.Default.NORMAL_BROADCAST);
            if (TextUtils.isEmpty(normalBroad)) {
                resultTip = getResources().getString(R.string.main_temp_normal_tips);
            } else {
                resultTip = normalBroad;
            }
        } else {
            if (temperature > HIGHEST_TEMPER) {
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
        }
        return resultTip;
    }

    private String getSpeechText(String resultTip, float temperature) {
        //设置语音播报
        Locale locale = APP.getContext().getResources().getConfiguration().locale;
        boolean isChina = TextUtils.equals(locale.getLanguage(), "zh");
        String speechTip;
        if (temperature < HIGHEST_TEMPER) {
            if (mFEnabled) {
                speechTip = resultTip + (isChina ? "华氏度" : "Fahrenheit degree");
            } else {
                speechTip = resultTip + (isChina ? "摄氏度" : "Centigrade");
            }
        } else {
            speechTip = resultTip;
        }
        return speechTip;
    }

    public boolean checkFaceToFar(Rect faceRect, int distance) {
        int faceWidth = faceRect.right - faceRect.left;
        return faceWidth < distance;
    }

    public boolean checkFaceInFrame(Rect realRect, View areaView) {
        areaView.getGlobalVisibleRect(mAreaRect);
        //人脸范围补正数值
        int faceRangeCorrectValue = 50;
        mAreaRect.left += faceRangeCorrectValue;
        mAreaRect.right -= faceRangeCorrectValue;
        mAreaRect.top += faceRangeCorrectValue;
        mAreaRect.bottom -= faceRangeCorrectValue;
        return realRect.contains(mAreaRect);
    }

    @Override
    protected void onStop() {
        super.onStop();
        TemperatureModule.getIns().closeHotImageK3232();
        TemperatureModule.getIns().setInfraredTempCallBack(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TemperatureModule.getIns().closeSerialPort();
        TemperatureModule.getIns().closeHotImageK3232();
    }
}
