package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.tencent.bugly.beta.Beta;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.Config;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.faceview.CameraManager;
import com.yunbiao.ybsmartcheckin_live_id.heartbeat.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;

public class SettingActivity extends BaseActivity {
    private static final String TAG = "SettingActivity";
    private TextView tvAdsType;
    private TextView tvDeviceNo;
    private TextView tvCompName;
    private TextView tvNetState;
    private RadioGroup radioGroup;
    private TextView tvCpuTemper;
    private TextView tvCamera;
    private TextView tvLivenessState;
    private TextView tvMultipleState;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_setting;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initView() {
        tvAdsType = findViewById(R.id.tv_ads_type);
        tvDeviceNo = findViewById(R.id.tv_device_no);
        tvCompName = findViewById(R.id.tv_company_name);
        tvNetState = findViewById(R.id.tv_wifi_state);
        radioGroup = findViewById(R.id.radio_group);
        tvCpuTemper = findViewById(R.id.tv_cpu_temper);
        tvCamera = findViewById(R.id.tv_camera);
        tvLivenessState = findViewById(R.id.tv_liveness_state);
        tvMultipleState = findViewById(R.id.tv_multiple_state);
    }

    @Override
    protected void initData() {
        String compName = SpUtils.getStr(SpUtils.COMPANY_NAME);
        tvCompName.setText(compName);

        String deviceNo = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        tvDeviceNo.setText(deviceNo);

        int adsResource = Config.getAdsResource();
        tvAdsType.setText(adsResource == Config.LOCAL_ADS ? "本地" : "云");

        checkNet();

        radioGroup.check(Config.getLightStrate()+1);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int realId = checkedId - 1;
                Config.setLightStrate(realId);
                Log.e(TAG, "onCheckedChanged: " + realId);
            }
        });

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                final String s = CpuUtils.getCpuTemperatureFinder() + "℃";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvCpuTemper.setText(s);
                    }
                });

            }
        },0, 3 , TimeUnit.SECONDS);

        tvCamera.setText("【" + (Config.getCameraType() == Config.CAMERA_AUTO ? "自动" : Config.getCameraType() == Config.CAMERA_BACK? "后置" : "前置") + "，分辨率：" + CameraManager.getWidth()+"*" + CameraManager.getHeight() + "】" );

        tvLivenessState.setText(Config.isLiveness()? "开" :"关");
        tvMultipleState.setText(Config.isMultiple()? "开" :"关");
    }

    public void selectImage(View view) {
        UIUtils.showTitleTip("正在开发中！");
    }

    public void addPerson(View view) {
        startActivity(new Intent(this, AddEmployActivity.class));
    }

    public void goRecord(View view) {
        startActivity(new Intent(this, SignActivity.class));
    }

    public void checkAdsResource(View view) {
        final int adsResource = Config.getAdsResource();
        String msg = adsResource == Config.LOCAL_ADS ? "本地" : "云";
        String next = adsResource == Config.LOCAL_ADS ? "云" : "本地";
        String tip = "当前广告为：" + msg + "，是否切换为：" + next;
        showAlert(tip, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Config.setAdsResource(adsResource == Config.LOCAL_ADS ? Config.CLOUD_ADS : Config.LOCAL_ADS);
            }
        }, new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                final int adsResource = Config.getAdsResource();
                tvAdsType.setText(adsResource == Config.LOCAL_ADS ? "本地" : "云");
            }
        });
    }

    public void modifyPWD(View view) {
        setPwd();
    }

    public void powerControlStrategy(View view) {
        UIUtils.showTitleTip("正在开发中！");
    }

    public void checkUpgrade(View view) {
        Beta.checkUpgrade();
    }

    public void activeSDK(View view) {
        UIUtils.showTitleTip("正在开发中！");
    }

    public void livenessSwitch(View view) {
        UIUtils.showTitleTip("正在开发中！");
    }

    public void multipleSwitch(View view) {
        UIUtils.showTitleTip("正在开发中！");
    }

    public void turnLight(View view) {
        UIUtils.showTitleTip("正在开发中！");
    }

    public void setRelay(View view) {
        UIUtils.showTitleTip("正在开发中！");
    }

    public void hardwareTest(View view) {
        UIUtils.showTitleTip("正在开发中！");
    }


    /**
     * ====功能区==================================================================================================
     */
    public void rebootDevice(View view) {
        showAlert("设备将重启，是否继续？", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ProgressDialog progressDialog = UIUtils.coreInfoShow3sDialog();
                progressDialog.setTitle("重启");
                progressDialog.setMessage("3秒后将重启设备");
                progressDialog.setCancelable(false);
                progressDialog.show();
                UIUtils.restart.start();
            }
        }, null);
    }

    private void checkNet() {
        String net = "";
        boolean intenetConnected = isIntenetConnected(this);
        if (intenetConnected) {
            net = "【以太网络，IP地址：" + getHostIp() + "】";
        } else {
            net = "【WIFI，" + getWifiInfo(0) + "，IP地址：" + getWifiInfo(1) + "】";
        }
        tvNetState.setText(net);
    }

    private String getHostIp() {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("yao", "SocketException");
            e.printStackTrace();
        }
        return hostIp;
    }

    public static String getWifiInfo(int type) {
        WifiManager wifiManager = (WifiManager) APP.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            return null;
        }

        WifiInfo wi = wifiManager.getConnectionInfo();
        Log.e(TAG, "getWifiInfo() wi=" + wi);
        if (wi == null) {
            return null;
        }
        if(type == 0){
            return "名称：" +wi.getSSID() + "，信号强度："+wi.getRssi();
        }

        //获取32位整型IP地址
        int ipAdd = wi.getIpAddress();
        Log.e(TAG, "getWifiInfo() ipAdd=" + ipAdd);
        if (ipAdd == 0) {
            return null;
        }
        //把整型地址转换成“*.*.*.*”地址
        String ip = intToIp(ipAdd);
        Log.e(TAG, "getWifiInfo() ip=" + ip);

        if (ip == null || ip.startsWith("0")) {
            return null;
        }
        return ip;
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    private static boolean isIntenetConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mInternetNetWorkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
            boolean hasInternet = !isNullObject(mInternetNetWorkInfo) && mInternetNetWorkInfo.isConnected() && mInternetNetWorkInfo.isAvailable();
            return hasInternet;
        }
        return false;
    }

    /**
     * 判断对象是否为空
     *
     * @param object
     * @return
     */
    public static boolean isNullObject(Object object) {

        if (object == null) {
            return true;
        }

        return false;
    }

    private void showAlert(String msg, Dialog.OnClickListener onClickListener, DialogInterface.OnDismissListener onDissmissListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage(msg);
        builder.setPositiveButton("确定", onClickListener);
        if (onDissmissListener != null) {
            builder.setOnDismissListener(onDissmissListener);
        }

        AlertDialog alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        alertDialog.show();
        window.setWindowAnimations(R.style.mystyle);  //添加动画
    }

    public void setPwd() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_set_pwd);

        final EditText edtPwd = (EditText) dialog.findViewById(R.id.edt_set_pwd);
        final EditText edtPwd2 = (EditText) dialog.findViewById(R.id.edt_set_pwd_again);
        final Button btnCancel = (Button) dialog.findViewById(R.id.btn_pwd_cancel);
        final Button btnConfirm = (Button) dialog.findViewById(R.id.btn_pwd_confirm);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(edtPwd.getText())) {
                    edtPwd.setError("密码不可为空");
                    return;
                }
                if (edtPwd.getText().length() < 6) {
                    edtPwd.setError("密码最少输入6位");
                    return;
                }
                if (TextUtils.isEmpty(edtPwd2.getText())) {
                    edtPwd2.setError("请再次输入密码");
                    return;
                }
                String pwd = edtPwd.getText().toString();
                final String pwd2 = edtPwd2.getText().toString();
                if (!TextUtils.equals(pwd, pwd2)) {
                    edtPwd2.setError("两次输入的密码不一致");
                    return;
                }

                btnCancel.setEnabled(false);
                btnConfirm.setEnabled(false);
                Map<String, String> params = new HashMap<>();
                params.put("deviceNo", HeartBeatClient.getDeviceNo());
                params.put("password", pwd2);
                OkHttpUtils.post().url(ResourceUpdate.UPDATE_PWD).params(params).build().execute(new StringCallback() {
                    @Override
                    public void onError(Call call, final Exception e, int id) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UIUtils.showTitleTip("修改失败：" + e != null ? e.getMessage() : "NULL");
                            }
                        });
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        JSONObject jsonObject = JSONObject.parseObject(response);
                        final Integer status = jsonObject.getInteger("status");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (status == 1) {
                                    UIUtils.showTitleTip("修改成功");
                                    SpUtils.saveStr(SpUtils.MENU_PWD, pwd2);
                                    dialog.dismiss();
                                } else {
                                    UIUtils.showTitleTip("修改失败");
                                }
                            }
                        });
                    }

                    @Override
                    public void onAfter(int id) {
                        btnConfirm.setEnabled(true);
                        btnCancel.setEnabled(true);
                    }
                });
            }
        });

        dialog.show();
        Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.mystyle);  //添加动画
    }

    public static class CpuUtils {
        private CpuUtils() {
            //no instance
        }
        private static final List<String> CPU_TEMP_FILE_PATHS = Arrays.asList(
                "/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp",
                "/sys/devices/system/cpu/cpu0/cpufreq/FakeShmoo_cpu_temp",
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/class/i2c-adapter/i2c-4/4-004c/temperature",
                "/sys/devices/platform/tegra-i2c.3/i2c-4/4-004c/temperature",
                "/sys/devices/platform/omap/omap_temp_sensor.0/temperature",
                "/sys/devices/platform/tegra_tmon/temp1_input",
                "/sys/kernel/debug/tegra_thermal/temp_tj",
                "/sys/devices/platform/s5p-tmu/temperature",
                "/sys/class/thermal/thermal_zone1/temp",
                "/sys/class/hwmon/hwmon0/device/temp1_input",
                "/sys/devices/virtual/thermal/thermal_zone1/temp",
                "/sys/devices/virtual/thermal/thermal_zone0/temp",
                "/sys/class/thermal/thermal_zone3/temp",
                "/sys/class/thermal/thermal_zone4/temp",
                "/sys/class/hwmon/hwmonX/temp1_input",
                "/sys/devices/platform/s5p-tmu/curr_temp");

        public static final String getCpuTemperatureFinder() {
            String currTemp = "-1";
            for (String cpuTempFilePath : CPU_TEMP_FILE_PATHS) {
                Double temp = readOneLine(new File(cpuTempFilePath));
                String validPath = "";
                double currentTemp = 0.0D;
                if (isTemperatureValid(temp)) {
                    validPath = cpuTempFilePath;
                    currentTemp = temp;
                } else if (isTemperatureValid(temp / (double) 1000)) {
                    validPath = cpuTempFilePath;
                    currentTemp = temp / (double) 1000;
                }

                if(!TextUtils.isEmpty(validPath)
                        && (currentTemp != 0)){
                    currTemp = currentTemp+"";
                }
            }
            return currTemp;
        }

        private static double readOneLine(File file) {
            FileInputStream fileInputStream = null;
            String s = "";
            try {
                fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                s = bufferedReader.readLine();
                fileInputStream.close();
                inputStreamReader.close();
                bufferedReader.close();
            } catch (IOException e) {
            }

            double result = 0;
            try {
                result = Double.parseDouble(s);
            } catch (NumberFormatException ignored) {
            }
            return result;
        }

        private static boolean isTemperatureValid(double temp) {
            return temp >= -30.0D && temp <= 250.0D;
        }
    }
}
