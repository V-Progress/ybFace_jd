package com.yunbiao.ybsmartcheckin_live_id.activity.base;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.elcapi.jnielc;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resetLedDelay(3000);
    }

    protected void resetLedDelay(int delay) {
        handler.removeMessages(0);
        handler.sendEmptyMessageDelayed(0, delay);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ledBlue();
        }
    };

    protected void ledGreen() {
        jnielc.seekstart();
        jnielc.ledseek(seek_green_r, brightness);
        jnielc.ledseek(seek_green_l, brightness);
        jnielc.seekstop();
    }

    protected void ledBlue() {
        jnielc.seekstart();
        jnielc.ledseek(seek_blue_r, brightness);
        jnielc.ledseek(seek_blue_l, brightness);
        jnielc.seekstop();
    }

    protected void ledRed() {
        jnielc.seekstart();
        jnielc.ledseek(seek_red_r, brightness);
        jnielc.ledseek(seek_red_l, brightness);
        jnielc.seekstop();
    }

    protected void ledOff() {
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
