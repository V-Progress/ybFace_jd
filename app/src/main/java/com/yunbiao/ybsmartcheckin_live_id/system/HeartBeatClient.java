package com.yunbiao.ybsmartcheckin_live_id.system;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;


import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

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
}
