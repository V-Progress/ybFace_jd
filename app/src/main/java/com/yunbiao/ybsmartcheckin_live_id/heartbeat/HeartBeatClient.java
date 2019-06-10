package com.yunbiao.ybsmartcheckin_live_id.heartbeat;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;


import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class HeartBeatClient {
    private static String sbDeviceId = null;

    static {
        if(TextUtils.isEmpty(sbDeviceId)){
            sbDeviceId = CommonUtils.getDeviceNo();
            SpUtils.saveStr(SpUtils.DEVICE_UNIQUE_NO,sbDeviceId);
        }
    }

    /**
     * 获取设备唯一编号
     *
     * @return
     */
    public static String getDeviceNo() {
        sbDeviceId = CommonUtils.getDeviceNo();
        Log.e("99999", "getDeviceNo: " + sbDeviceId);
        return sbDeviceId;
    }
}
