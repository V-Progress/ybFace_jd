package com.yunbiao.ybsmartcheckin_live_id.system;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;


import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.lang.reflect.Method;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class HeartBeatClient {
    /**
     * 获取设备唯一编号
     *
     * @return
     */
    public static String getDeviceNo() {
        String sbDeviceId = SpUtils.getStr(SpUtils.DEVICE_UNIQUE_NO);
        if(TextUtils.isEmpty(sbDeviceId)){
            sbDeviceId = CommonUtils.getMacAddress();
            SpUtils.saveStr(SpUtils.DEVICE_UNIQUE_NO,sbDeviceId);
        }
        return sbDeviceId;
    }

    public static String getDeviceSN(){
        String serialNumber = android.os.Build.SERIAL;
        if(!TextUtils.isEmpty(serialNumber)){
            return serialNumber;
        }

        String serial = null;
        try {
            Class<?> c =Class.forName("android.os.SystemProperties");
            Method get =c.getMethod("get", String.class);
            serial = (String)get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }
}
