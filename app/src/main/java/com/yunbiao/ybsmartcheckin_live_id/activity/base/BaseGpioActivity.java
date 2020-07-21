package com.yunbiao.ybsmartcheckin_live_id.activity.base;

import android.os.Bundle;
import android.os.Handler;

import com.yunbiao.ybsmartcheckin_live_id.activity.WelComeActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.utils.L;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

public abstract class BaseGpioActivity extends LedControlActivity {
    private Handler timeHandler = new Handler();
    private int CLOSE_DELAY = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!(this instanceof WelComeActivity)) {
            if (mSmdtManager != null && Constants.DEVICE_TYPE != Constants.DeviceType.TEMPERATURE_CHECK_IN_215_INCH) {
                try {
                    //设置继电器为非自动模式
                    mSmdtManager.setRelayIoMode(0, 0);
                } catch (Exception e) {
                    L.e("BaseGpioActivity", "onCreate:" + (e == null ? "NULL" : e.getMessage()));
                }
            }
        }
    }

    //onResume的时候把灯和门关掉
    @Override
    protected void onResume() {
        super.onResume();
        CLOSE_DELAY = SpUtils.getIntOrDef(Constants.Key.GPIO_DELAY, Constants.Default.GPIO_DELAY);
        close();
        offLight();
    }

    //开门并且执行自动关门定时任务
    public void openDoor() {
        open();
        startOpenDelay();
    }

    //打开补光灯，并且执行自动关灯任务（检测到人脸时将会一直调用该方法，因此不用担心自动关灯会影响其他灯）
    protected void onLight() {
        if(Constants.DEVICE_TYPE == Constants.DeviceType.CHECK_IN){
            return;
        }
        xhapiLedOpen();
        smdtLedWhite();
        ysLedWhite();
        startAutoLight();
    }

    //关灯并且清除自动关灯任务
    protected void offLight() {
        xhapiLedClose();
        closeSmdtLedWhite();
        ysLedOff();
        timeHandler.removeCallbacks(runnable);
    }

    //自动关灯
    protected void startAutoLight() {
        timeHandler.removeCallbacks(lightControlRunnable);
        timeHandler.postDelayed(lightControlRunnable, CLOSE_DELAY * 1000);
    }
    //灯光控制
    private Runnable lightControlRunnable = () -> offLight();

    //自动关门
    protected void startOpenDelay() {
        timeHandler.removeCallbacks(runnable);
        timeHandler.postDelayed(runnable, CLOSE_DELAY * 1000);
    }
    //关门控制
    private Runnable runnable = () -> close();
}
