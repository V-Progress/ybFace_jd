package com.yunbiao.ybsmartcheckin_live_id.activity.fragment.child;

import android.util.Log;

import androidx.fragment.app.Fragment;

public abstract class BaseF extends Fragment {
    private static final String TAG = "BaseF";
    private int mType = 0;
    private int time = 10;
    private Switcher switcher;
    protected Runnable switchRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "run:  ----- 切换页面");
            if(switcher != null){
                switcher.onSwitch();
            }
        }
    };

    public void setSwitcher(Switcher switcher) {
        this.switcher = switcher;
    }

    public int getType(){
        return mType;
    }

    public void setType(int type){
        mType = type;
    }

    public void setTime(int time) {
        Log.e(TAG, "setTime: 切换时间 ----- " + time);
        this.time = time;
    }

    public int getTime() {
        return time;
    }

    public interface Switcher{
        void onSwitch();
    }

    public abstract void start();
    public abstract void stop();
}
