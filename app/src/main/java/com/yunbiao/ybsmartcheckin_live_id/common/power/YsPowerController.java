package com.yunbiao.ybsmartcheckin_live_id.common.power;

import android.text.TextUtils;
import android.util.Log;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.common.cache.ACache;

import java.io.File;

import startest.ys.com.poweronoff.PowerOnOffManager;

public class YsPowerController {
    private static final String TAG = "YsPowerController";
    public final static String POWER_ON = "poerOn";
    public final static String POWER_OFF = "poerOff";

    public static void setPower() {
        String powerOn = getPowerParam(POWER_ON);
        String powerOff = getPowerParam(POWER_OFF);
        if(TextUtils.isEmpty(powerOn) || TextUtils.isEmpty(powerOff)){
            return;
        }

        String[] onArr = powerOn.split(";");
        String[] offArr = powerOff.split(";");
        if(onArr.length != 2 || offArr.length != 2){
            return;
        }

        String onDays = onArr[0];//日期
        String onTime = onArr[1];//时间
        String offDays = offArr[0];//日期
        String offTime = offArr[1];//时间
        if(TextUtils.isEmpty(onDays) || TextUtils.isEmpty(onTime) || TextUtils.isEmpty(offDays) || TextUtils.isEmpty(offTime)){
            return;
        }

        String[] onDayArray = onDays.split(",");//分割日期
        String[] offDayArray = offDays.split(",");//分割日期
        if (onDayArray.length <= 0 || offDayArray.length <= 0) {
            return;
        }

        int[] dateArray = {0,0,0,0,0,0,0};
        for (String s : onDayArray) {
            switch (s) {
                case "1":
                    dateArray[0] = 1;
                    break;
                case "2":
                    dateArray[1] = 1;
                    break;
                case "3":
                    dateArray[2] = 1;
                    break;
                case "4":
                    dateArray[3] = 1;
                    break;
                case "5":
                    dateArray[4] = 1;
                    break;
                case "6":
                    dateArray[5] = 1;
                    break;
                case "7":
                    dateArray[6] = 1;
                    break;
            }
        }

        String[] onTimeArray = onTime.split(":");
        String[] offTimeArray = offTime.split(":");
        if(onTimeArray.length < 2 || offTimeArray.length < 2){
            return;
        }

        int[] onTimeArr = {Integer.parseInt(onTimeArray[0]),Integer.parseInt(onTimeArray[1])};
        int[] offTimeArr = {Integer.parseInt(offTimeArray[0]),Integer.parseInt(offTimeArray[1])};

        PowerOnOffManager powerOnOffManager = PowerOnOffManager.getInstance(APP.getContext());
        powerOnOffManager.clearPowerOnOffTime();
        powerOnOffManager.setPowerOnOffWithWeekly(onTimeArr,offTimeArr,dateArray);
    }

    /**
     * @return 1, 2, 3, 4, 5, 6, 7;08:00
     */
    public static String getPowerParam(String key) {
        ACache acache = ACache.get(new File(ResourceUpdate.PROPERTY_CACHE_PATH));
        return acache.getAsString(key);
    }
}
