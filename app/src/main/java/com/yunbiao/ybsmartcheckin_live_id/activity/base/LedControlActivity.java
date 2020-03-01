package com.yunbiao.ybsmartcheckin_live_id.activity.base;

import android.app.smdt.SmdtManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.elcapi.jnielc;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mSmdtManager = SmdtManager.create(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        resetLedDelay(1000);
    }

    protected void resetLedDelay(int delay) {
        handler.removeMessages(0);
        handler.sendEmptyMessageDelayed(0, delay);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ledInit();
        }
    };

    protected void ledGreen() {
        if (Constants.SCREEN_TYPE == Constants.ScreenType.TYPE_PORTRAIT_8_800_1280) {
            smdtLedGreen();
        } else {
            jniLedGreen();
        }
    }

    protected void ledRed() {
        if (Constants.SCREEN_TYPE == Constants.ScreenType.TYPE_PORTRAIT_8_800_1280) {
            smdtLedRed();
        } else {
            jniLedRed();
        }
    }

    protected void ledInit() {
        if (Constants.SCREEN_TYPE == Constants.ScreenType.TYPE_PORTRAIT_8_800_1280) {
            smdtCloseAll();
        } else {
            jniLedBlue();
        }
    }

    protected void ledOff() {
        if (Constants.SCREEN_TYPE == Constants.ScreenType.TYPE_PORTRAIT_8_800_1280) {
            smdtCloseAll();
        } else {
            jniLedOff();
        }
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
}
