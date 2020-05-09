package com.yunbiao.ybsmartcheckin_live_id.common.power;

import android.text.TextUtils;
import android.util.Log;


import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.common.cache.ACache;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyXutils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class PowerOffTool {

    private static final String TAG = "PowerOffTool";
    private static PowerOffTool powerOffTool = null;

    private PowerOffTool() {
    }

    public static PowerOffTool getPowerOffTool() {
        if (powerOffTool == null) {
            synchronized (PowerOffTool.class) {
                if (powerOffTool == null) {
                    powerOffTool = new PowerOffTool();
                }
            }
        }
        return powerOffTool;
    }

    public final static String POWER_ON = "poerOn";
    public final static String POWER_OFF = "poerOff";

    private void putPowerParam(String key, String value) {
        ACache acache = ACache.get(new File(ResourceUpdate.PROPERTY_CACHE_PATH));
        acache.put(key, value);
    }

    /**
     * @return 1, 2, 3, 4, 5, 6, 7;08:00
     */
    public String getPowerParam(String key) {
        ACache acache = ACache.get(new File(ResourceUpdate.PROPERTY_CACHE_PATH));
        return acache.getAsString(key);
    }

    private void setPowerRestartTime() {
        Integer type = CommonUtils.getBroadType();
        Log.d("poweroff--板子", "setPowerRestartTime: " + type);
        switch (type.intValue()) {
            case 0:
                setPowerRunTime();
                break;
            case 5:
                PowerControllerTool.getPowerContrArray();
                break;
        }
    }


    /**
     * 获取开关机时间
     *
     * @param uid 设备id
     */
    public void getPowerOffTime(String uid) {
        HashMap<String, String> paramMap = new HashMap();
        paramMap.put("uid", uid);
        MyXutils.getInstance().post(ResourceUpdate.POWER_OFF_URL, paramMap, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {
                Log.e(TAG, "onSuccess: ---------------->"+result);
                if (result.startsWith("\"")) {
                    result = result.substring(1, result.length() - 1);
                }
                putParam(result);
            }

            @Override
            public void onError(Throwable ex) {
                Log.e(TAG, "onError: "+ex.getMessage());
                ex.printStackTrace();
            }

            @Override
            public void onFinish() {

            }
        });
    }

    public void putParam(String powerOffJson) {
        powerOffJson = powerOffJson.replaceAll("\\\\", "");
        if (powerOffJson.startsWith("\"")) {
            powerOffJson = powerOffJson.substring(1, powerOffJson.length() - 1);
        }
        JSONTokener jsonParser = new JSONTokener(powerOffJson);
        try {
            // 开机字符串
            String powerOn = "";
            // 关机字符串
            String powerOff = "";

            JSONArray person = (JSONArray) jsonParser.nextValue();
            for (int i = 0; i < person.length(); i++) {
                JSONObject jsonObject = (JSONObject) person.get(i);
                Integer status = jsonObject.getInt("status");
                Integer runType = jsonObject.getInt("runType");
                String runDate = jsonObject.getString("runDate");
                if (runDate.indexOf(":") != -1) {
                    runDate = runDate.substring(runDate.indexOf(":") + 1, runDate.length());//1,2,3,4,5,6,7,
                }

                String runTime = jsonObject.getString("runTime");
                if (runType == 0 && status == 0) {
                    powerOn = runDate + ";" + runTime;
                } else if (runType == 1 && status == 0) {
                    powerOff = runDate + ";" + runTime;
                }
            }

            // 1,2,3,4,5,6,7;08:00
            putPowerParam(POWER_ON, powerOn);
            putPowerParam(POWER_OFF, powerOff);

            setPowerRestartTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*如果没有正常关机，中途启动了就再次关机*/
    private void executefailedPowerDown() {
        // 获取当天星期几
        Date currentDate = new Date();
        Integer weekDay = getWeekDay(currentDate);
        // 获取时间
        String poweroff = getPowerParam(POWER_OFF);
        if (!TextUtils.isEmpty(poweroff)) {
            // 判断是否存在运行策略
            String[] powerOnArray = poweroff.split(";");
            String runDate = powerOnArray[0];
            String runTime = powerOnArray[1];
            String[] timeDateArray = runTime.split(":");

            String currentTime = CommonUtils.getStringDate();
            String[] timePice = currentTime.split(":");
            int currentHour = Integer.valueOf(timePice[0]);
            int currentMinute = Integer.valueOf(timePice[1]);
            int closeHour = Integer.valueOf(timeDateArray[0]);
            int closeMinute = Integer.valueOf(timeDateArray[1]);

            if (runDate.indexOf(weekDay.toString()) != -1) {// 如果当天是运行策略中包含的周期
                int intervalTime = 6;
                if (currentHour > closeHour) {
                    intervalTime = 60 - closeMinute + currentMinute;
                } else if (currentHour == closeHour) {
                    intervalTime = Math.abs(currentMinute - closeMinute);
                }
                setPowerFailedDown(intervalTime);
            }
        }
    }

    /*//开关机失败。间隔时间小于5秒，重设开关机时间*/
    private void setPowerFailedDown(int intervalTime) {
        if (intervalTime < 5) {
            Long[] powerOn = getPowerTime(POWER_ON);
            if (powerOn != null) {
                Long onh = powerOn[0] * 24 + powerOn[1];
                Long onm = powerOn[2];
                if ((onh * 60 + onm) > 2) {
                    OnOffTool.setEnabled(onh.byteValue(), onm.byteValue(), (byte) 0, (byte) 2);
                }
            }
        }
    }

    /**
     * 单机版开关机设置
     *
     * @param powerOn
     * @param powerOff
     */
    public void setLocalRuntime(String powerOn, String powerOff) {
        if (powerOn != null && !powerOn.isEmpty() && powerOff != null && !powerOff.isEmpty()) {
            putPowerParam(POWER_ON, "1,2,3,4,5,6,7;" + powerOn);
            putPowerParam(POWER_OFF, "1,2,3,4,5,6,7;" + powerOff);
        } else {
            putPowerParam(POWER_ON, "");
            putPowerParam(POWER_OFF, "");
        }
        // 执行设置运行
        setPowerRestartTime();
    }

    /**
     * 机器重启
     */
    public void restart() {
//        OnOffTool.setEnabled((byte) 0, (byte) 1, (byte) 0, (byte) 1);
        Integer type = CommonUtils.getBroadType();
        switch (type) {
            case 0:
            case 2:
            case 3:
            case 5:
                execSuCmd("reboot");
                break;
        }
    }

    public void execSuCmd(String cmd) {
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

    /**
     * 设置机器的开关机时间
     */
    public void setPowerRunTime() {
        Long[] powerOn = getPowerTime(POWER_ON);
        Long[] powerOff = getPowerTime(POWER_OFF);
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
            if ((onh * 60 + onm) > (offh * 60 + offm)) {
                long offset = (onh * 60 + onm) - (offh * 60 + offm);
                onm = offset % 60;
                onh = offset / 60;
            }
            OnOffTool.setEnabled(onh.byteValue(), onm.byteValue(), offh.byteValue(), offm.byteValue());
            Log.e("time::::", "onh:" + onh + "======onm:" + onm + "========offh:" + offh + "======offm:" + offm);
            executefailedPowerDown();
        } else {
            Log.e("time", "没有找到开关机时间");
            OnOffTool.setDisabled();
        }
    }

    /**
     * 机器开机时候，判断是否需要到网络查询
     */
    public void machineStart() {
        Long[] powerOn = getPowerTime(POWER_ON);
        Long[] powerOff = getPowerTime(POWER_OFF);
        // 如果开关机时间没有设置，就进行网络获取
        if (powerOn != null && powerOff != null) {
            setPowerRestartTime();
        } else {
            //开关机时间为空，则去网络下载
            getPowerOffTime(HeartBeatClient.getDeviceNo());
        }
    }

    /**
     * 获取下一个开关机时间
     *
     * @param powerType
     * @return
     */
    public Long[] getPowerTime(String powerType) {
        Long[] runTimeLong = null;
        // 获取当天星期几
        Date currentDate = new Date();
        Integer weekDay = getWeekDay(currentDate);
        // 获取时间
        String powerOn = getPowerParam(powerType);
        if (!TextUtils.isEmpty(powerOn)) {
            // 判断是否存在运行策略
            String[] powerOnArray = powerOn.split(";");
            String runDate = powerOnArray[0];
            String runTime = powerOnArray[1];
            String[] timeDateArray = runTime.split(":");
            Date onDate = new Date();
            onDate.setHours(Integer.parseInt(timeDateArray[0]));
            onDate.setMinutes(Integer.parseInt(timeDateArray[1]));
            boolean isEq = false;
            if (onDate.getHours() == currentDate.getHours() && onDate.getMinutes() == currentDate.getMinutes()) {
                currentDate.setMinutes(currentDate.getMinutes() + 3);
                isEq = true;
            }
            if (runDate.indexOf(weekDay.toString()) != -1) {// 如果当天是运行策略中包含的周期
                if (onDate.getTime() > currentDate.getTime()) {
                    // 如果开机时间大于当前时间，就需要设置
                    Long betwLong = onDate.getTime() - currentDate.getTime();
                    runTimeLong = formatDuring(betwLong);
                } else {
                    // 推迟到运行周期中的下一天的这个时间
                    Integer betweenDay = getBetweenDay(runDate, weekDay);
                    if (powerType.equals(POWER_ON) && isEq) {
                        onDate.setMinutes(onDate.getMinutes() + 3);
                    }
                    onDate.setDate(onDate.getDate() + betweenDay);
                    runTimeLong = formatDuring(onDate.getTime() - currentDate.getTime());
                }
            } else {
                // 如果运行策略中没有这一天
                Integer nextDay = getNextDay(runDate, weekDay);
                onDate.setDate(onDate.getDate() + nextDay);
                if (powerType.equals(POWER_ON) && isEq) {
                    onDate.setMinutes(onDate.getMinutes() + 3);
                }
                runTimeLong = formatDuring(onDate.getTime() - currentDate.getTime());
            }
        }
        return runTimeLong;
    }

    public Integer getNextDay(String runDate, Integer currentWeekDay) {
        Integer between = 0;
        String[] runDateArray = runDate.split(",");
        for (int i = 0; i < runDateArray.length; i++) {
            Integer runDateA = Integer.parseInt(runDateArray[i]);
            if (runDateA > currentWeekDay) {
                between = runDateA - currentWeekDay;
                break;
            }
        }
        return between;
    }

    public Integer getBetweenDay(String runDate, Integer currentWeekDay) {
        Integer between = 0;
        String[] runDateArray = runDate.split(",");
        for (int i = 0; i < runDateArray.length; i++) {
            Integer runDateA = Integer.parseInt(runDateArray[i]);
            if (runDateA == currentWeekDay) {
                if (i == runDateArray.length - 1) {
                    between = Integer.parseInt(runDateArray[0]) + (7 - currentWeekDay);
                } else {
                    runDateA = Integer.parseInt(runDateArray[i + 1]);
                    between = runDateA - currentWeekDay;
                }
                break;
            }
        }
        return between;
    }

    /**
     * 获取时间间隔
     *
     * @param mss
     * @return
     */
    public Long[] formatDuring(Long mss) {
        long days = mss / (1000 * 60 * 60 * 24);
        long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (mss % (1000 * 60)) / 1000;
        return new Long[]{days, hours, minutes, seconds};
    }

    public Integer getWeekDay(Date date) {
        SimpleDateFormat dateFm = new SimpleDateFormat("EEEE");
        String weekDay = dateFm.format(date);
        Integer week = 0;
        if (weekDay.equals("星期一") || weekDay.equals("Monday")) {//Wednesday
            week = 1;
        } else if (weekDay.equals("星期二") || weekDay.equals("Tuesday")) {
            week = 2;
        } else if (weekDay.equals("星期三") || weekDay.equals("Wednesday")) {
            week = 3;
        } else if (weekDay.equals("星期四") || weekDay.equals("Thursday")) {
            week = 4;
        } else if (weekDay.equals("星期五") || weekDay.equals("Friday")) {
            week = 5;
        } else if (weekDay.equals("星期六") || weekDay.equals("Saturday")) {
            week = 6;
        } else if (weekDay.equals("星期日") || weekDay.equals("Sunday")) {
            week = 7;
        }
        return week;
    }

    /**
     * 机器重启
     */
    public void powerReload() {
        String cmd = "su -c reboot";
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
        }
    }

    /**
     * 机器关机
     */
    public void powerShutdown() {
        if (CommonUtils.boardIsXBH()) {
        } else if (CommonUtils.boardIsJYD()) {
        } else {
            Process process = null;
            try {
                process = Runtime.getRuntime().exec("su");
                DataOutputStream out = new DataOutputStream(process.getOutputStream());
                out.writeBytes("reboot -p\n");
                out.writeBytes("exit\n");
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }





}
