package com.yunbiao.ybsmartcheckin_live_id.activity.base;

import android.app.smdt.SmdtManager;
import android.os.Bundle;
import android.os.Handler;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

public abstract class BaseGpioActivity extends BaseActivity {
    private boolean isOpened = false;//是否已开锁
    private boolean isAlwayOpen = false;//常开
    private Handler timeHanlder = new Handler();
    private SmdtManager smdt;
    private int CLOSE_DELAY = 5;
    private Handler checkHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        smdt = APP.getSmdt();
//        EventBus.getDefault().register(this);
    }

    protected boolean isAlwayOpen() {
        return isAlwayOpen;
    }

    @Override
    protected void onResume() {
        super.onResume();
        CLOSE_DELAY = SpUtils.getIntOrDef(SpUtils.GPIO_DELAY, 5);
//        isAlwayOpen = SpUtils.getBoolean(SpUtils.DOOR_STATE,false);
        d("update: ----- " + isAlwayOpen);
        if(isAlwayOpen){
            startCheckHandler(0);
        } else {
            close();
        }
    }

    private void startCheckHandler(long time){
        checkHandler.removeCallbacks(checkRunnable);
        checkHandler.postDelayed(checkRunnable,time);
    }

    private Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            if(isAlwayOpen){
                openDoor();
            }
            startCheckHandler(1000);
        }
    };


//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void update(GpioEvent gpioEvent) {
//        d("update: ----- 接收到GPIO事件");
//        if(gpioEvent.getState() == GpioEvent.OPEN){
//            UIUtils.showShort(this,"已解锁");
//            openDoor();
//            return;
//        }
//
//        isAlwayOpen = gpioEvent.isIs();
//        d("update: ----- " + isAlwayOpen);
//        if(isAlwayOpen){
//            UIUtils.showShort(this,"已设置常开");
//            openDoor();
//        } else {
//            UIUtils.showShort(this,"已关闭常开");
//            close();
//        }
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void update(OpenDoorTimeEvent gpioEvent) {
//        d("update: ----- 接收到设置门禁时间的事件 " + gpioEvent.getTime());
//        if(gpioEvent != null && gpioEvent.getTime() > 0){
//            CLOSE_DELAY = gpioEvent.getTime();
//        }
//    }

    public void openDoor(){
        open();
        if(isAlwayOpen){
            return;
        }
        startOpenDelay();
    }

    private void open(){
        if(smdt == null){
            UIUtils.showTitleTip(this,"无法控制门禁，请检查板卡型号");
            return;
        }
        for (int i = 0; i < 3; i++) {
            int result = smdt.smdtSetExtrnalGpioValue(1, false);
            isOpened = result == 0;
        }
    }

    private void close(){
        if(smdt == null){
            UIUtils.showTitleTip(this,"无法控制门禁，请检查板卡型号");
            return;
        }
        for (int i = 0; i < 3; i++) {
            int result = smdt.smdtSetExtrnalGpioValue(1, true);
            isOpened = result == 0;
        }
    }

    protected void startOpenDelay(){
        timeHanlder.removeCallbacks(runnable);
        timeHanlder.postDelayed(runnable,CLOSE_DELAY * 1000);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(isAlwayOpen){
                return;
            }
            close();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        EventBus.getDefault().unregister(this);
    }
}
