package com.yunbiao.ybsmartcheckin_live_id.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


import com.yunbiao.ybsmartcheckin_live_id.AppActivityManager;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.Config;
import com.yunbiao.ybsmartcheckin_live_id.common.CameraTool;
import com.yunbiao.ybsmartcheckin_live_id.utils.logutils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by LiuShao on 2016/4/6.
 */
public class CommonUtils {
    private static final String TAG = "CommonUtils";

    public static String getWifiMac(){
        String macAddress = "";
        WifiManager wifiManager = (WifiManager) APP.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        WifiInfo info = wifiManager.getConnectionInfo();
        if (info != null) {
            macAddress = info.getMacAddress();
        }

        if (!TextUtils.isEmpty(macAddress) && macAddress.equals("02:00:00:00:00:00")) {//6.0及以上系统获取的mac错误
            macAddress = CommonUtils.getMacAddr();
        }

        if (TextUtils.isEmpty(macAddress)) {
            macAddress = CommonUtils.getSixOSMac();
        }

        macAddress = macAddress.toUpperCase();
        return macAddress;
    }
    /**
     * android 7.0及以上 （2）扫描各个网络接口获取mac地址
     *
     */
    /**
     * 获取设备HardwareAddress地址
     *
     * @return
     */
    public static String getLocalMac() {
        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        String hardWareAddress = null;
        NetworkInterface iF = null;
        if (interfaces == null) {
            return null;
        }
        while (interfaces.hasMoreElements()) {
            iF = interfaces.nextElement();
            try {
                hardWareAddress = bytesToString(iF.getHardwareAddress());
                if (hardWareAddress != null)
                    break;
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        if (hardWareAddress != null) {
            String mac = hardWareAddress.toUpperCase();
            return mac;
        } else {
            return "mac";
        }
    }

    /***
     * byte转为String
     *
     * @param bytes
     * @return
     */
    private static String bytesToString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        for (byte b : bytes) {
            buf.append(String.format("%02X:", b));
        }
        if (buf.length() > 0) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }
    @SuppressLint("HardwareIds")
    public static String getMacAddress() {
        String macAddress = "";
        WifiManager wifiManager = (WifiManager) APP.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        WifiInfo info = wifiManager.getConnectionInfo();
        if (info != null) {
            macAddress = info.getMacAddress();
        }

        if (!TextUtils.isEmpty(macAddress) && macAddress.equals("02:00:00:00:00:00")) {//6.0及以上系统获取的mac错误
            macAddress = CommonUtils.getMacAddr();
        }

        if (TextUtils.isEmpty(macAddress)) {
            macAddress = CommonUtils.getSixOSMac();
        }

        macAddress = macAddress.toUpperCase();
        String macS = "";
        for (int i = macAddress.length() - 1; i >= 0; i--) {
            macS += macAddress.charAt(i);
        }
        UUID uuid2 = new UUID(macS.hashCode(), macAddress.hashCode());
        return uuid2.toString();
    }

    /**
     * 获取现在时间
     *
     * @return 返回短时间字符串格式yyyy-MM-dd HH:mm:ss
     */
    public static String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(currentTime);
    }

    // Android 6.0以上获取WiFi的Mac地址
    //由于android6.0对wifi mac地址获取进行了限制，用原来的方法获取会获取到02:00:00:00:00:00这个固定地址。
    //但是可以通过读取节点进行获取"/sys/class/net/wlan0/address"
    public static String getMacAddr() {
        try {
            return loadFileAsString("/sys/class/net/wlan0/address")
                    .toUpperCase().substring(0, 17);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String loadFileAsString(String filePath)
            throws java.io.IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }


    /**
     * 获得屏幕高度
     *
     * @return
     */
    public static int getScreenWidth() {
        WindowManager wm = (WindowManager) APP.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public static void backgroundAlpha(float bgAlpha) {
        Activity activity = AppActivityManager.getAppActivityManager().currentActivity();
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        activity.getWindow().setAttributes(lp);
    }


    public static void backAlpha(float bgAlpha) {
        Activity activity = AppActivityManager.getAppActivityManager().currentActivity();
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        activity.getWindow().setAttributes(lp);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    /**
     * 获得屏幕宽度
     *
     * @return
     */
    public static int getScreenHeight() {
        WindowManager wm = (WindowManager) APP.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }


    /**
     * 渠道信息
     *
     * @param context
     * @param metaName
     * @return
     */
    public static int getChannel(Context context, String metaName) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return appInfo.metaData.getInt(metaName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }


    /**
     * 隐藏输入键盘
     *
     * @param view
     * @param context
     */
    public static void hideSoftInput(EditText view, Context context) {
        InputMethodManager inputMeMana = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMeMana.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 显示软键盘
     */
    public static void showSoftInput(Context context) {
        InputMethodManager inputMeMana = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMeMana.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static double doubleFormat(double valued) {
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String p = decimalFormat.format(valued);
        valued = Double.valueOf(p);
        return valued;
    }

    // 获取CPU名字
    public static String getCpuName() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            for (int i = 0; i < array.length; i++) {
            }
            return array[1];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    //CPU个数
    public static int getNumCores() {
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                return Pattern.matches("cpu[0-9]", pathname.getName());
            }
        }
        try {
            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(new CpuFilter());
            return files.length;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }


    /**
     * 获取CPU最大频率，单位KHZ
     *
     * @return
     */
    public static String getMaxCpuFreq() {
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = {"/system/bin/cat", "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = "N/A";
        }
        Double cpuGhz = Double.valueOf(result.trim()) / 1000000;
        return String.valueOf(cpuGhz);
    }

    public static boolean boardIsXBH() {//判断板子是不是小百合的，返回true
        Integer broadInfo = CommonUtils.getBroadType();
        return broadInfo == 4;
    }

    public static boolean boardIsJYD() {//判断板子是不是建益达的，返回true
        Integer broadInfo = CommonUtils.getBroadType();
        return broadInfo == 5;
    }

    /**
     * 获取json 值
     *
     * @param jsonObj jsonObejct
     * @param param   需要获取的参数
     * @param defVal  缺省值
     * @return Object
     */
    public static Object getJsonObj(JSONObject jsonObj, String param, Object defVal) {
        Object objValue;
        try {
            objValue = jsonObj.get(param);
            if (objValue == null && defVal != null) {
                objValue = defVal;
            }
            if (objValue instanceof String) {
                if (TextUtils.isEmpty((String) objValue)) {
                    objValue = defVal;
                }
            }
        } catch (JSONException e) {
            objValue = defVal;
        }
        return objValue;
    }

    /**
     * 获取当前版本号
     */
    public static String getAppVersion(Context context) {
        String version = "";
        try {
            // 获取packagemanager的实例
            PackageManager packageManager = context.getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            version = packInfo.versionName;
        } catch (Exception ignored) {
        }
        return version;
    }

    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获得屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    /**
     * 设备是否有摄像头
     */
    public static String checkCamera() {//1有  0没有
        Integer type = CommonUtils.getBroadType();
        String isHas;
        if (type == 0) {//荣朗 判断摄像头是否存在 判断dev/video0是否存在
            File file = new File("dev/video0");
            boolean exists = file.exists();
            if (exists) {
                isHas = "1";
            } else {
                isHas = "0";
            }
            return isHas;
        } else {//其他主板
            int camera = CameraTool.getCamera();
            if (camera != -1) {//有
                isHas = "1";
            } else {
                isHas = "0";
            }
            return isHas;
        }
    }


    /**
     * 获取主板的信息存到sp里 供以后判断主板厂家使用
     */
    public static String saveBroadInfo() {
        Process process = null;
        BufferedReader br = null;
        try {
            process = Runtime.getRuntime().exec("cat /proc/version");
            InputStream outs = process.getInputStream();
            InputStreamReader isrout = new InputStreamReader(outs);
            br = new BufferedReader(isrout, 8 * 1024);
            StringBuffer result = new StringBuffer("");
            String line;
            while ((line = br.readLine()) != null) {
                result.append(line);
            }
            String broadInfo = result.toString();
            LogUtils.e(TAG, "主板信息: " + broadInfo);
            return broadInfo;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 判断是不是板子的来源厂家
     *
     * @return 0其他（视美泰 默认）  5是 10寸人脸平板
     */
    public static Integer getBroadType() {
        String broad_info = SpUtils.getStr(SpUtils.BOARD_INFO);
        if (TextUtils.isEmpty(broad_info)) {
            broad_info = saveBroadInfo();
            SpUtils.saveStr(SpUtils.BOARD_INFO, broad_info);
        }
        if (broad_info.contains("lxr")) {//视美泰
            return 5;
        } else if (broad_info.contains("lxr")) {
            return 5;
        } else if (broad_info.contains("HARRIS") || broad_info.contains("silence")) {//亿晟
            return 4;
        } else {
            return 4;
        }
    }

    public static String getBroadType2() {
        String broad_info = SpUtils.getStr(SpUtils.BOARD_INFO);
        if (TextUtils.isEmpty(broad_info)) {
            broad_info = saveBroadInfo();
            SpUtils.saveStr(SpUtils.BOARD_INFO, broad_info);
        }
        Log.e(TAG, "getBroadType2: " + broad_info);
        if (broad_info.contains("wxl")) {
            return "SMT";//视美泰
        } else if (broad_info.contains("lxr")) {
            return "LXR";//亿莱顿
        } else if (broad_info.contains("harris") || broad_info.contains("silence")) {
            return "HARRIS";//亿晟
        } else {
            return "HARRIS";//亿晟
        }
//        return broad_info;
    }

    /**
     * 6.0及以上的安卓系统获取mac为02:00:00:00:00:00，使用这个方法修改
     */
    public static String getSixOSMac() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }
                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
