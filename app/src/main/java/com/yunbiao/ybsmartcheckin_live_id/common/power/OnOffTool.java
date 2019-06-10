package com.yunbiao.ybsmartcheckin_live_id.common.power;



import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.logutils.LogUtils;

import java.util.Calendar;

class OnOffTool {
    private static final String TAG = "OnOffTool";

    public static void setEnabled(byte on_h, byte on_m, byte off_h, byte off_m) {
        LogUtils.i(TAG, "当前设置定时时间" + CommonUtils.getStringDate());
        LogUtils.i(TAG, on_h + " 小时 " + on_m + " 分钟开机 " + off_h + " 小时 " + off_m + " 分钟后关机");
        setPowerOnOff(on_h, on_m, off_h, off_m, (byte) 3);
    }

    static void setDisabled() {
        setPowerOnOff((byte) 0, (byte) 3, (byte) 0, (byte) 3, (byte) 0);
    }

    /**
     * 所有时间都是相对时间，（关机时间=实际关机时间-当前时间）
     * （开机时间=实际开机时间-当前时间-关机时间）
     *
     * @param off_h  关机小时
     * @param off_m  关机分钟
     * @param on_h   开机小时
     * @param on_m   开机分钟
     * @param enable 自动开关机状态  0：关     3：开
     * @return
     */
    private static int setPowerOnOff(byte off_h, byte off_m, byte on_h, byte on_m, byte enable) {
        try {
            int fd = com.xboot.stdcall.posix.open("/dev/McuCom", com.xboot.stdcall.posix.O_RDWR, 0666);
            int ret = com.xboot.stdcall.posix.poweronoff(off_h, off_m, on_h, on_m, enable, fd);
            com.xboot.stdcall.posix.close(fd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static String getCurTime() {
        String time = "";
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        time = String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(day) + " " + String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(second);
        return time;
    }
}
