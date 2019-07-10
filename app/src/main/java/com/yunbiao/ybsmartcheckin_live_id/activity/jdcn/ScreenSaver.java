package com.yunbiao.ybsmartcheckin_live_id.activity.jdcn;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbiao.ybsmartcheckin_live_id.R;

public class ScreenSaver {

    private static ScreenSaver instance = new ScreenSaver();
    private final int MAX_TIME = 20;
    private int mTime = MAX_TIME;
    private static WelComeSmallActivity mAct;
    private View screenSaveLayout;
    private ImageView ivWeather;
    private TextView tvWeather;

    public static ScreenSaver get(){
        return instance;
    }

    public ScreenSaver init(WelComeSmallActivity activity){
        mAct = activity;
        screenSaveLayout = mAct.findViewById(R.id.screen_saver);
        ivWeather = mAct.findViewById(R.id.iv_weather);
        tvWeather = mAct.findViewById(R.id.tv_weather);
        return instance;
    }

    public void setWeather(int iconId,String weather){
        if(ivWeather != null && iconId != -1){
            ivWeather.setImageResource(iconId);
        }

        if(tvWeather != null && !TextUtils.isEmpty(weather)){
            tvWeather.setText(weather);
        }
    }

    private ScreenSaver(){
        handler.sendEmptyMessage(0);
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            mTime--;
            if(mTime <= 0){
                mTime = MAX_TIME;
                open();
                return;
            }
            handler.sendEmptyMessageDelayed(0,1000);
        }
    };

    public void restart(){
        if(isShown()){//如果显示就关闭并开启计时
            close();
            handler.sendEmptyMessage(0);
        }
        mTime = MAX_TIME;//如果不显示就重置
    }

    public boolean isShown(){
        return screenSaveLayout.isShown();
    }

    public void close(){
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

    private void setLight(Activity context, int brightness) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
        context.getWindow().setAttributes(lp);
    }

}
