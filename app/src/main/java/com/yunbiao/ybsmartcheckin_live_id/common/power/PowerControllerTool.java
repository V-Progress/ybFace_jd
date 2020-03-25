package com.yunbiao.ybsmartcheckin_live_id.common.power;

import android.content.Intent;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Administrator on 2016/8/10 0010.
 */
public class PowerControllerTool {
    private static final String TAG = "PowerControllerTool";

    public static void getPowerContrArray() {
        Long[] powerOn = PowerOffTool.getPowerOffTool().getPowerTime(PowerOffTool.POWER_ON);
        Long[] powerOff = PowerOffTool.getPowerOffTool().getPowerTime(PowerOffTool.POWER_OFF);

        // 如果开关机时间没有设置，就进行网络获取
        if (powerOn != null && powerOff != null) {
            Long offh = powerOff[0] * 24 + powerOff[1];
            Long offm = powerOff[2];
            // 0 23 26
            // onh:0 onm:1
            // 0 23 25
            // offh:23 offm:25
            Long onh = powerOn[0] * 24 + powerOn[1];
            Long onm = powerOn[2];

            Calendar powerOffDate = Calendar.getInstance();
            powerOffDate.add(Calendar.MINUTE, (int) (offh * 60 + offm));
            Calendar powerOnDate = Calendar.getInstance();
            if ((onh * 60 + onm) > (offh * 60 + offm)) {
                long offset = (onh * 60 + onm) - (offh * 60 + offm);
                onm = offset % 60;
                onh = offset / 60;
                powerOnDate.add(Calendar.MINUTE, (int) (offh * 60 + offm) + (int) (onh * 60 + onm));
            } else {
                powerOnDate.add(Calendar.MINUTE, (24 * 60) + (int) (onh * 60 + onm));
            }
            Log.e(TAG, "getPowerContrArray: " + " offh: " + offh + " offm: " + offm + "   onh: " + onh + " onm: " + onm);
            Log.e(TAG, "getPowerContrArray: " + " setPowerOffDate:   " + powerOffDate.getTime().toLocaleString() + "   setPowerOnDate:   " + powerOnDate.getTime().toLocaleString());
            if (powerOn != null && powerOff != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmSS");
                String  onTime= dateFormat.format( powerOnDate.getTime());
                String  offTime= dateFormat.format( powerOffDate.getTime());
                String cmd="system/xbin/test "+offTime+"  "+onTime+" enable";
                Log.e(TAG, "cmd------------>"+cmd );
                execSuCmd(cmd);

            } else {
//                execSuCmd("system/xbin/test 201301181659 201301181700 disable");
            }
        } else {
//            execSuCmd("system/xbin/test 201301181659 201301181700 disable");
        }

    }

    public static void execSuCmd(String cmd) {
        Process process = null;
        DataOutputStream os = null;
        DataInputStream is = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            int aa = process.waitFor();
            is = new DataInputStream(process.getInputStream());
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            String out = new String(buffer);
            Log.i("tag", out + aa);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
