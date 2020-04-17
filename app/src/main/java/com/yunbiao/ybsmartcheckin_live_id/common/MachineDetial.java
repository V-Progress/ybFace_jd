package com.yunbiao.ybsmartcheckin_live_id.common;


import android.os.SystemProperties;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.afinel.VersionUpdateConstants;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.utils.*;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyXutils;
import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LiuShao on 2016/3/4.
 */

public class MachineDetial {
    private static final String TAG = "MachineDetial";

    private static MachineDetial machineDetial;

    public static MachineDetial getInstance() {
        if (machineDetial == null) {
            machineDetial = new MachineDetial();
        }
        return machineDetial;
    }

    private MachineDetial() {

    }
    /**
     * 上传设备信息
     */
    public void upLoadHardWareMessage() {
        HandleMessageUtils.getInstance().runInThread(() -> {
            Map<String, String> map = new HashMap<>();
            map.put("deviceNo", HeartBeatClient.getDeviceNo());
            map.put("screenWidth", String.valueOf(CommonUtils.getScreenWidth(APP.getContext())));
            map.put("screenHeight", String.valueOf(CommonUtils.getScreenHeight(APP.getContext())));
            map.put("diskSpace", SdCardUtils.getMemoryTotalSize());
            map.put("useSpace", SdCardUtils.getMemoryUsedSize());
            map.put("softwareVersion", CommonUtils.getAppVersion(APP.getContext()) + "_" + VersionUpdateConstants
                    .CURRENT_VERSION);
            map.put("screenRotate", String.valueOf(SystemProperties.get("persist.sys.hwrotation")));
            map.put("deviceCpu", CommonUtils.getCpuName() + " " + CommonUtils.getNumCores() + "核" + CommonUtils
                    .getMaxCpuFreq() + "khz");
            map.put("deviceIp", NetworkUtils.getIpAddress());//当前设备IP地址
            map.put("mac", NetworkUtils.getLocalMacAddress());//设备的本机MAC地址
            map.put("camera", CommonUtils.checkCamera());//设备是否有摄像头 1有  0没有

            MyXutils.getInstance().post(ResourceUpdate.UPLOAD_MACHINE_INFO, map, new MyXutils.XCallBack() {
                @Override
                public void onSuccess(String result) {

                }

                @Override
                public void onError(Throwable ex) {

                }

                @Override
                public void onFinish() {

                }
            });
        });
    }
}
