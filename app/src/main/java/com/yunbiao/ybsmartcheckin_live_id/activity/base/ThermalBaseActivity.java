package com.yunbiao.ybsmartcheckin_live_id.activity.base;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.callback.HotImageK3232CallBack;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ThermalBaseActivity extends BaseGpioActivity {

    private String mCurrPort = "";
    private int mCurrBaudRate = -1;
    private boolean mThermalImageMirror = false;
    private int mBodyPercent = -1;
    private int mBodyMinT = -1;
    private int mBodyMaxT = -1;

    private int mDelayTime = 0;
    private float mMinThreshold;
    private float mWarningThreshold;
    private float mAmbCorrValue = 0.0f;
    private float mTempCorrValue = 0.0f;

    @Override
    protected void onResume() {
        super.onResume();

        mDelayTime = SpUtils.getIntOrDef(SpUtils.GET_TEMP_DELAY_TIME, Constants.DEFAULT_GET_TEMP_DELAY_TIME_VALUE);//设置测温延时
        mMinThreshold = SpUtils.getFloat(SpUtils.TEMP_MIN_THRESHOLD, Constants.DEFAULT_TEMP_MIN_THRESHOLD_VALUE); //测温最小阈值
        mWarningThreshold = SpUtils.getFloat(SpUtils.TEMP_WARNING_THRESHOLD, Constants.DEFAULT_TEMP_WARNING_THRESHOLD_VALUE); //测温报警阈值
        mAmbCorrValue = SpUtils.getFloat(SpUtils.AMB_CORRECT_VALUE, Constants.DEFAULT_AMB_CORRECT_VALUE);
        mTempCorrValue = SpUtils.getFloat(SpUtils.TEMP_CORRECT_VALUE, Constants.DEFAULT_TEMP_CORRECT_VALUE);

        initSerialPort();
        openHotImage();

        TemperatureModule.getIns().setaCorrectionValue(mAmbCorrValue);
        TemperatureModule.getIns().setmCorrectionValue(mTempCorrValue);
    }

    private void initSerialPort() {
        //初始化端口
        String port = SpUtils.getStr(SpUtils.PORT_PATH, Constants.DEFAULT_PORT_PATH);
        int baudRate = SpUtils.getIntOrDef(SpUtils.BAUD_RATE, Constants.DEFAULT_BAUD_RATE);
        if (!TextUtils.equals(port, mCurrPort) || mCurrBaudRate != baudRate) {
            mCurrPort = port;
            mCurrBaudRate = baudRate;
            TemperatureModule.getIns().initSerialPort(this, mCurrPort, mCurrBaudRate);
        }
    }

    private void openHotImage() {
        boolean thermalImageMirror = SpUtils.getBoolean(SpUtils.THERMAL_IMAGE_MIRROR, Constants.DEFAULT_THERMAL_IMAGE_MIRROR);
        int bodyMinT = SpUtils.getIntOrDef(SpUtils.BODY_MIN_T, Constants.DEFAULT_BODY_MIN_T_VALUE);
        int bodyMaxT = SpUtils.getIntOrDef(SpUtils.BODY_MAX_T, Constants.DEFAULT_BODY_MAX_T_VALUE);
        int bodyPercent = SpUtils.getIntOrDef(SpUtils.BODY_PERCENT, Constants.DEFAULT_BODY_PERCENT_VALUE);

        if (mThermalImageMirror != thermalImageMirror || mBodyMinT != bodyMinT || mBodyMaxT != bodyMaxT || mBodyPercent != bodyPercent) {
            mThermalImageMirror = thermalImageMirror;
            mBodyMinT = bodyMinT;
            mBodyMaxT = bodyMaxT;
            mBodyPercent = bodyPercent;
            TemperatureModule.getIns().closeHotImageK3232();
            updateHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    TemperatureModule.getIns().startHotImageK3232(mThermalImageMirror, mBodyMinT, mBodyMaxT, mBodyPercent, hotImageK3232CallBack);
                }
            }, 1000);
        }
    }

    private static final String TAG = "ThermalBaseActivity";
    private List<Float> mCacheList = new ArrayList<>();
    private long mCacheTime = 0;
    private boolean broadStatus = false;
    private HotImageK3232CallBack hotImageK3232CallBack = new HotImageK3232CallBack() {
        @Override
        public void newestHotImageData(final Bitmap imageBmp, final float sensorT,
                                       final float maxT, final float minT,
                                       final float bodyMaxT, final boolean isBody, final int bodyPercentage) {
            //更新头像
            Message message = Message.obtain();
            message.what = 1;
            message.obj = imageBmp;
            Bundle bundle = new Bundle();
            bundle.putFloat("bodyMaxT", bodyMaxT);
            message.setData(bundle);
            updateHandler.sendMessage(message);
            //如果没人就清调播报状态，缓存时间，缓存温度集合
            if (!isBody) {
                broadStatus = false;
                mCacheTime = 0;
                mCacheList.clear();
                return;
            }
            //是否已播报
            if (broadStatus) {
                return;
            }
            //如果温度小于阈值，则不存
            if (bodyMaxT < mMinThreshold) {
                return;
            }
            //添加温度
            if (mCacheList.size() <= 0 /*|| !mCacheList.contains(bodyMaxT)*/ || mCacheList.size() < 5) {
                mCacheList.add(bodyMaxT);
                return;
            }

            //判断延时
           /* if (mCacheTime == 0) {
                mCacheTime = System.currentTimeMillis();
                updateHandler.sendEmptyMessage(2);
                return;
            } else if (System.currentTimeMillis() - mCacheTime < mDelayTime) {
                return;
            }*/

//            Log.e(TAG, "newestHotImageData: 延迟时间已过");
            for (Float aFloat : mCacheList) {
                Log.e(TAG, "newestHotImageData: " + aFloat);
            }
            //判断温度，如果为0或者低于阈值则重新测试
            Float finalTemp = mCacheList.size() <= 0 ? 0.0f : Collections.max(mCacheList);
            if (finalTemp == 0.0f) {
                broadStatus = false;
                mCacheTime = 0;
                mCacheList.clear();
                updateHandler.sendEmptyMessageDelayed(3, 200);
                return;
            }
            finalTemp -= 0.3f;


            Log.e(TAG, "newestHotImageData: 开始播报");
            //已经播报
            broadStatus = true;
            //发送结果
            Message resultMessage = Message.obtain();
            resultMessage.what = 4;
            resultMessage.obj = imageBmp;
            Bundle resultBundle = new Bundle();
            resultBundle.putFloat("resultTemp", finalTemp);
            resultBundle.putBoolean("tempOk", finalTemp < mWarningThreshold);
            resultMessage.setData(resultBundle);
            updateHandler.sendMessage(resultMessage);
        }
    };

    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1://更新图像
                    Bitmap bitmap = (Bitmap) msg.obj;
                    Bundle data = msg.getData();
                    float bodyMaxT = data.getFloat("bodyMaxT", 0.0f);
                    updateHotImageBitmap(bitmap, bodyMaxT);
                    break;
                case 2://正在测
                    onMeasuring();
                    break;
                case 3://重测
                    onReMeasuring();
                    break;
                case 4://收到结果
                    Bitmap hotImageBitmap = (Bitmap) msg.obj;
                    Bundle result = msg.getData();
                    float resultTemp = result.getFloat("resultTemp");
                    boolean tempOk = result.getBoolean("tempOk");
                    onMeasureResult(hotImageBitmap, resultTemp, tempOk);
                    break;
            }
        }
    };

    protected abstract void updateHotImageBitmap(Bitmap bitmap, float bodyMaxT);

    protected abstract void onMeasuring();

    protected abstract void onReMeasuring();

    protected abstract void onMeasureResult(Bitmap hotImageBitmap, float resultTemp, boolean tempOk);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TemperatureModule.getIns().closeSerialPort();
        TemperatureModule.getIns().closeHotImageK3232();
    }
}
