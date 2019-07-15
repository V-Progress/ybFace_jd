package com.yunbiao.ybsmartcheckin_live_id.business;

import android.os.Handler;
import android.os.Message;

import com.android.xhapimanager.XHApiManager;

public class ApiManager {

    private XHApiManager xhApiManager;
    int lightTime = 5;
    int gateTime = 5;
    private static ApiManager apiManager = new ApiManager();
    private boolean lignHandlerRunning = false;
    private boolean gateHandlerRunning = false;

    Handler lightHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            lignHandlerRunning = true;
            lightTime--;
            if(lightTime <= 0){
                lightTime = 5;
                offLight();
                lignHandlerRunning = false;
                return;
            }
            sendEmptyMessageDelayed(0,1000);
        }
    };

    Handler gateHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            gateHandlerRunning = true;
            gateTime--;
            if(gateTime <= 0){
                gateTime = 5;
                offGate();
                gateHandlerRunning = false;
                return;
            }
            sendEmptyMessageDelayed(0,1000);
        }
    };

    public static ApiManager instance(){
        return apiManager;
    }
    private ApiManager(){
        try{
            // TODO: 2019/7/11 非90群板子必须关
//            xhApiManager = new XHApiManager();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /***
     * 必须在Application中初始化
     */
    public void init(){
        if(isLighting()){
            offLight();
        }
    }

    public void onLignt(){
        if(xhApiManager == null){return;}
        if(!isLighting()){
            xhApiManager.XHSetGpioValue(4,1);
        } else {
            lightTime = 5;
        }

        if(!lignHandlerRunning){
            lightHandler.sendEmptyMessage(0);
        }
    }

    public void offLight(){
        xhApiManager.XHSetGpioValue(4,0);
    }

    public boolean isLighting(){
        return xhApiManager != null && xhApiManager.XHReadGpioValue(4) == 1;
    }

    public void onGate(){
        if(xhApiManager == null){return;}
        if(!isGateOpened()){
            xhApiManager.XHSetGpioValue(5,0);
        } else {
            gateTime = 5;
        }

        if(!gateHandlerRunning){
            gateHandler.sendEmptyMessage(0);
        }
    }

    public void offGate(){
        if(xhApiManager == null){return;}
        xhApiManager.XHSetGpioValue(5,1);
    }
    public boolean isGateOpened(){
        if(xhApiManager == null){return false;}
        return xhApiManager.XHReadGpioValue(5) == 0;
    }

}
