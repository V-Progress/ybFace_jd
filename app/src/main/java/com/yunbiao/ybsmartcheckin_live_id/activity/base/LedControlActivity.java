package com.yunbiao.ybsmartcheckin_live_id.activity.base;

import android.app.smdt.SmdtManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.example.elcapi.jnielc;
import com.example.yfaceapi.GPIOManager;
import com.example.yfaceapi.GpioUtils;
import com.intelligence.hardware.temperature.TemperatureModule;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;

public abstract class LedControlActivity extends BaseActivity {

    //右边灯
    private static final int seek_red_r = 0xa1;//红
    private static final int seek_green_r = 0xa2;//绿
    private static final int seek_blue_r = 0xa3;//蓝
    private static final int seek_green_blue_r = 0xa4;//青
    private static final int seek_red_blue_r = 0xa5;//紫
    private static final int seek_red_green_r = 0xa6;//黄
    private static final int seek_all_r = 0xa7;//白

    //左边灯
    private static final int seek_red_l = 0xb1;//红
    private static final int seek_green_l = 0xb2;//绿
    private static final int seek_blue_l = 0xb3;//蓝
    private static final int seek_green_blue_l = 0xb4;//青
    private static final int seek_red_blue_l = 0xb5;//紫
    private static final int seek_red_green_l = 0xb6;//黄
    private static final int seek_all_l = 0xb7;//白

    private int brightness = 15;//亮度：范围0-15
    protected SmdtManager mSmdtManager;
    protected GPIOManager gpioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String broadType = CommonUtils.getBroadType2();
        Log.e(TAG, "onCreate: 当前类型：" + broadType);

        if (TextUtils.equals("SMT", broadType)) {
            mSmdtManager = SmdtManager.create(this);
        } else if(TextUtils.equals("HARRIS",broadType)){
            gpioManager = GPIOManager.getInstance(this);
        }
        resetLedDelay(1000);
    }

    protected void resetLedDelay(int delay) {
        handler.removeMessages(0);
        handler.sendEmptyMessageDelayed(0, delay + 100);
    }

    private static final String TAG = "LedControlActivity";
    private Handler handler = new Handler(msg -> {
        Log.e(TAG, "handleMessage: --------- 已重置灯光");
        ledInit();
        return true;
    });

    protected void ledGreen() {
        smdtLedGreen();
        jniLedGreen();
        ysLedGreen();
    }

    protected void ledRed() {
        smdtLedRed();
        jniLedRed();
        ysLedRed();
    }

    protected void ledInit() {
        smdtCloseAll();
        jniLedBlue();
        ysLedOff();
    }

    protected void ledOff() {
        smdtCloseAll();
        jniLedOff();
        ysLedOff();
    }

    /*================================================================================*/
    private boolean isWhiteLedShow = false;
    private boolean isGreenLedShow = false;
    private boolean isRedLedShow = false;

    protected void smdtLedWhite() {
        if (mSmdtManager == null) {
            return;
        }
        if (isRedLedShow || isGreenLedShow) {
            return;
        }
        isWhiteLedShow = true;
        mSmdtManager.smdtSetControl(3, 1);
    }

    protected void closeSmdtLedWhite() {
        if (mSmdtManager == null) {
            return;
        }
        isWhiteLedShow = false;
        mSmdtManager.smdtSetControl(3, 0);
    }

    private void smdtLedRed() {
        if (mSmdtManager == null) {
            return;
        }
        if (isWhiteLedShow) {
            closeSmdtLedWhite();
        }
        if (isGreenLedShow) {
            closeSmdtLedGreen();
        }
        isRedLedShow = true;
        mSmdtManager.smdtSetUsbPower(1, 3, 1);
    }

    private void closeSmdtLedRed() {
        if (mSmdtManager == null) {
            return;
        }
        isRedLedShow = false;
        mSmdtManager.smdtSetUsbPower(1, 3, 0);
    }

    private void smdtLedGreen() {
        if (mSmdtManager == null) {
            return;
        }
        if (isWhiteLedShow) {
            closeSmdtLedWhite();
        }
        if (isRedLedShow) {
            closeSmdtLedRed();
        }
        isGreenLedShow = true;
        mSmdtManager.smdtSetGpioDirection(4, 1, 1);
    }

    private void closeSmdtLedGreen() {
        if (mSmdtManager == null) {
            return;
        }
        isGreenLedShow = false;
        mSmdtManager.smdtSetGpioDirection(4, 1, 0);
    }

    private void smdtCloseAll() {
        if (mSmdtManager != null) {
            closeSmdtLedWhite();
            closeSmdtLedRed();
            closeSmdtLedGreen();
        }
    }

    /*======================================================================*/
    private void jniLedGreen() {
        jnielc.seekstart();
        jnielc.ledseek(seek_green_r, brightness);
        jnielc.ledseek(seek_green_l, brightness);
        jnielc.seekstop();
    }

    private void jniLedBlue() {
        jnielc.seekstart();
        jnielc.ledseek(seek_blue_r, brightness);
        jnielc.ledseek(seek_blue_l, brightness);
        jnielc.seekstop();
    }

    private void jniLedRed() {
        jnielc.seekstart();
        jnielc.ledseek(seek_red_r, brightness);
        jnielc.ledseek(seek_red_l, brightness);
        jnielc.seekstop();
    }

    private void jniLedOff() {
        //灭灯
        jnielc.seekstart();
        jnielc.ledoff();
        jnielc.seekstop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ledOff();
    }

    //亿舜============================================================
    protected void ysLedWhite(){
        TemperatureModule.getIns().controlExpandLight(1);
        if(gpioManager == null){
            return;
        }
        String greenLightStatus = gpioManager.getGreenLightStatus();
        String redLightStatus = gpioManager.getRedLightStatus();
        String whiteLightStatus = gpioManager.getWhiteLightStatus();
        if(TextUtils.equals("1",greenLightStatus) || TextUtils.equals("1",redLightStatus)){
            return;
        }
        if(!TextUtils.equals("1",whiteLightStatus)){
            gpioManager.pullUpWhiteLight();
        }
    }

    protected void ysLedGreen(){
        TemperatureModule.getIns().controlExpandLight(2);
        if(gpioManager == null){
            return;
        }
        String redLightStatus = gpioManager.getRedLightStatus();
        String whiteLightStatus = gpioManager.getWhiteLightStatus();
        String greenLightStatus = gpioManager.getGreenLightStatus();
        if(TextUtils.equals("1",redLightStatus)){
            gpioManager.pullDownRedLight();
        }
        if(TextUtils.equals("1",whiteLightStatus)){
            gpioManager.pullDownWhiteLight();
        }
        if(!TextUtils.equals("1",greenLightStatus)){
            gpioManager.pullUpGreenLight();
        }
    }

    protected void ysLedRed(){
        TemperatureModule.getIns().controlExpandLight(3);
        if(gpioManager == null){
            return;
        }
        String whiteLightStatus = gpioManager.getWhiteLightStatus();
        String redLightStatus = gpioManager.getRedLightStatus();
        String greenLightStatus = gpioManager.getGreenLightStatus();
        if(TextUtils.equals("1",whiteLightStatus)){
            gpioManager.pullDownWhiteLight();
        }
        if(TextUtils.equals("1",greenLightStatus)){
            gpioManager.pullDownGreenLight();
        }
        if(!TextUtils.equals("1",redLightStatus)){
            gpioManager.pullUpRedLight();
        }
    }

    protected void ysLedOff(){
        TemperatureModule.getIns().controlExpandLight(-1);
        if(gpioManager == null){
            return;
        }
        gpioManager.pullDownRedLight();
        gpioManager.pullDownWhiteLight();
        gpioManager.pullDownGreenLight();
    }
}
