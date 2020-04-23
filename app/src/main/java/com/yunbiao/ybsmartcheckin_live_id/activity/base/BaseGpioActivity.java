package com.yunbiao.ybsmartcheckin_live_id.activity.base;

import android.os.Bundle;
import android.os.Handler;

import com.android.xhapimanager.XHApiManager;
import com.intelligence.hardware.temperature.TemperatureModule;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.WelComeActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.utils.L;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

public abstract class BaseGpioActivity extends LedControlActivity {
    private boolean isOpened = false;//是否已开锁
    private Handler timeHanlder = new Handler();
    private int CLOSE_DELAY = 5;
    private XHApiManager xhApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!(this instanceof WelComeActivity)) {
            if (mSmdtManager != null) {
                try {
                    //设置继电器为非自动模式
                    mSmdtManager.setRelayIoMode(0, 0);
                } catch (Exception e) {
                    L.e("BaseGpioActivity", "onCreate:" + (e == null ? "NULL" : e.getMessage()));
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CLOSE_DELAY = SpUtils.getIntOrDef(SpUtils.GPIO_DELAY, 5);
        close();
        offLight();
    }

    public void openDoor() {
        open();
        startOpenDelay();
    }

    private void open() {
        TemperatureModule.getIns().controlExpandRelay(true);

        if (!(this instanceof WelComeActivity)) {
            if (mSmdtManager != null) {
                mSmdtManager.setRelayIoValue(1);
            }
        }

        if (mSmdtManager != null) {
            for (int i = 0; i < 3; i++) {
                int result = mSmdtManager.smdtSetExtrnalGpioValue(1, false);
            }
        }
        if (xhApi != null) {
            xhApi.XHSetGpioValue(5, 0);
        }
        if(gpioManager!= null){
            gpioManager.pullUpRelay();
        }
    }

    private void close() {
        TemperatureModule.getIns().controlExpandRelay(false);

        if (!(this instanceof WelComeActivity)) {
            if (mSmdtManager != null) {
                mSmdtManager.setRelayIoValue(0);
            }
        }
        if (mSmdtManager != null) {
            for (int i = 0; i < 3; i++) {
                int result = mSmdtManager.smdtSetExtrnalGpioValue(1, true);
                isOpened = result == 0;
            }
        }
        if (xhApi != null) {
            xhApi.XHSetGpioValue(5, 1);
        }
        if(gpioManager!= null){
            gpioManager.pullDownRelay();
        }
    }

    private void xhapiLedOpen() {
        if (xhApi != null) {
            xhApi.XHSetGpioValue(4, 1);
        }
    }

    private void xhapiLedClose() {
        if (xhApi != null) {
            xhApi.XHSetGpioValue(4, 0);
        }
    }

    //开灯
    protected void onLight() {
        xhapiLedOpen();
        smdtLedWhite();
        ysLedWhite();
        startAutoLight();
    }

    //关灯
    private void offLight() {
        xhapiLedClose();
        closeSmdtLedWhite();
        ysLedOff();
    }

    /***
     * 开始执行自动灯光
     */
    protected void startAutoLight() {
        timeHanlder.removeCallbacks(lightControlRunnable);
        timeHanlder.postDelayed(lightControlRunnable, CLOSE_DELAY * 1000);
    }

    //灯光控制
    private Runnable lightControlRunnable = new Runnable() {
        @Override
        public void run() {
            offLight();
        }
    };

    protected void startOpenDelay() {
        timeHanlder.removeCallbacks(runnable);
        timeHanlder.postDelayed(runnable, CLOSE_DELAY * 1000);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            close();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
