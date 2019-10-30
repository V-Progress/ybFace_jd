package com.yunbiao.ybsmartcheckin_live_id.activity.base;

import android.app.smdt.SmdtManager;
import android.os.Bundle;
import android.os.Handler;

import com.android.xhapimanager.XHApiManager;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public abstract class BaseGpioActivity extends BaseActivity {
    private boolean isOpened = false;//是否已开锁
//    private boolean isAlwayOpen = false;//常开
    private Handler timeHanlder = new Handler();
    private SmdtManager smdt;
    private int CLOSE_DELAY = 5;
    private Handler checkHandler = new Handler();
    private XHApiManager xhApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        smdt = APP.getSmdt();
        xhApi = APP.getXHApi();
//        EventBus.getDefault().register(this);
        if(smdt == null && xhApi == null){
            UIUtils.showShort(this,getString(R.string.act_baseGpio_wfkzmjqjcbkxh));
        }
    }

//    protected boolean isAlwayOpen() {
//        return isAlwayOpen;
//    }

    @Override
    protected void onResume() {
        super.onResume();
        CLOSE_DELAY = SpUtils.getIntOrDef(SpUtils.GPIO_DELAY, 5);
//        isAlwayOpen = SpUtils.getBoolean(SpUtils.DOOR_STATE,false);
//        d("update: ----- " + isAlwayOpen);
//        if(isAlwayOpen){
//            startCheckHandler(0);
//        } else {
//            close();
//        }
        close();
        offLight();
    }

//    private void startCheckHandler(long time){
//        checkHandler.removeCallbacks(checkRunnable);
//        checkHandler.postDelayed(checkRunnable,time);
//    }

//    private Runnable checkRunnable = new Runnable() {
//        @Override
//        public void run() {
////            if(isAlwayOpen){
////                openDoor();
////            }
//            startCheckHandler(1000);
//        }
//    };


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
//        if(isAlwayOpen){
//            return;
//        }
        startOpenDelay();
    }

    private void open(){
        if(smdt != null){
            for (int i = 0; i < 3; i++) {
                int result = smdt.smdtSetExtrnalGpioValue(1, false);
            }
        }
        if(xhApi != null){
            xhApi.XHSetGpioValue(5,0);
        }
    }

    private void close(){
        if(smdt != null){
            for (int i = 0; i < 3; i++) {
                int result = smdt.smdtSetExtrnalGpioValue(1, true);
                isOpened = result == 0;
            }
        }
        if(xhApi != null){
            xhApi.XHSetGpioValue(5,1);
        }
    }

    //开灯
    protected void onLight(){
        if(xhApi != null){
            xhApi.XHSetGpioValue(4,1);
        }
        startAutoLight();
    }

    //关灯
    private void offLight(){
        if(xhApi != null){
            xhApi.XHSetGpioValue(4,0);
        }
    }

    /***
     * 开始执行自动灯光
     */
    protected void startAutoLight(){
        timeHanlder.removeCallbacks(lightControlRunnable);
        timeHanlder.postDelayed(lightControlRunnable,CLOSE_DELAY * 1000);
    }

    //灯光控制
    private Runnable lightControlRunnable = new Runnable() {
        @Override
        public void run() {
            offLight();
        }
    };

    protected void startOpenDelay(){
        timeHanlder.removeCallbacks(runnable);
        timeHanlder.postDelayed(runnable,CLOSE_DELAY * 1000);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
//            if(isAlwayOpen){
//                return;
//            }
            close();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        EventBus.getDefault().unregister(this);
    }
}
