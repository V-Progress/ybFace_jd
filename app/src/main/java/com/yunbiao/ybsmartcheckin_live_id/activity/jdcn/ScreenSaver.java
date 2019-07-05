package com.yunbiao.ybsmartcheckin_live_id.activity.jdcn;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;

import com.yunbiao.ybsmartcheckin_live_id.R;

public class ScreenSaver {

    private static ScreenSaver instance = new ScreenSaver();
    private final int MAX_TIME = 20;
    private int mTime = MAX_TIME;
    private static WelComeSmallActivity mAct;
    private View screenSaveLayout;

    public static ScreenSaver get(){
        return instance;
    }

    public ScreenSaver init(WelComeSmallActivity activity){
        mAct = activity;
        screenSaveLayout = mAct.findViewById(R.id.screen_saver);
        return instance;
    }

    private ScreenSaver(){

    }

    public void start(){
        handler.sendEmptyMessage(0);
    }

    public void restart(){
        close();
        mTime = MAX_TIME;
    }

    public boolean isShown(){
        return screenSaveLayout.isShown();
    }

    public void close(){
//        int screenBrightness = getScreenBrightness(mAct);

        setLight(mAct,200);
        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                screenSaveLayout.setVisibility(View.GONE);
            }
        });
    }

    public void open(){
        setLight(mAct,20);
        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                screenSaveLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            mTime--;
            if(mTime <= 0){
                if(!screenSaveLayout.isShown()){
                    open();
                }
            }
            handler.sendEmptyMessageDelayed(0,1000);
        }
    };

    private void setLight(Activity context, int brightness) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
        context.getWindow().setAttributes(lp);
    }

}
