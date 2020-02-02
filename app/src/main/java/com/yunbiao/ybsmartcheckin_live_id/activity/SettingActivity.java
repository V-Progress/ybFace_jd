package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.yunbiao.faceview.CameraHelper;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.Config;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.DisplayOrientationEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.common.UpdateVersionControl;
import com.yunbiao.ybsmartcheckin_live_id.faceview.camera.CameraSettings;
import com.yunbiao.ybsmartcheckin_live_id.faceview.camera.ExtCameraManager;
import com.yunbiao.ybsmartcheckin_live_id.faceview.rect.FaceBoxUtil;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;

public class SettingActivity extends BaseActivity {
    private static final String TAG = "SettingActivity";
    private TextView tvNetState;
    private TextView tvCpuTemper;
    private TextView tvCamera;
    private CheckBox cbMirror;
    private Button btnAngle;
    private Spinner spnCameraSize;
    private Switch swAlready;
    private EditText edtDelay;
    private Switch switchFaceDialog;
    private EditText edtSimilar;

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
        tvNetState = findViewById(R.id.tv_wifi_state);
        tvCpuTemper = findViewById(R.id.tv_cpu_temper);
        tvCamera = findViewById(R.id.tv_camera);
        cbMirror = findViewById(R.id.cb_mirror);
        btnAngle = findViewById(R.id.btn_setAngle);
        spnCameraSize = findViewById(R.id.spn_camera_size);
        swAlready = findViewById(R.id.sw_setting_already);
        edtDelay = findViewById(R.id.edt_delay);
        switchFaceDialog = findViewById(R.id.sw_face_dialog);
        edtSimilar = findViewById(R.id.edt_similar_threshold);

        findViewById(R.id.iv_back).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    finish();
                }
                return false;
            }
        });

        initSetIp();
    }


    @Override
    protected void initData() {
        int cacheDelay = SpUtils.getIntOrDef(SpUtils.GPIO_DELAY, 5);
        edtDelay.setText(cacheDelay + "");
        edtDelay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String s1 = edtDelay.getText().toString();
                if (TextUtils.isEmpty(s1)) {
                    return;
                }
                int delay = Integer.parseInt(s1);
                SpUtils.saveInt(SpUtils.GPIO_DELAY, delay);
                UIUtils.showShort(SettingActivity.this, getString(R.string.act_set_error_modify_success));
            }
        });

        checkNet();

        //获取CPU温度
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
        }, 0, 3, TimeUnit.SECONDS);

        //摄像头模式
        tvCamera.setText("【" + (Config.getCameraType() == Config.CAMERA_AUTO ? getString(R.string.act_set_tip_auto) : Config.getCameraType() == Config.CAMERA_BACK ? getString(R.string.act_set_tip_back) : getString(R.string.act_set_tip_front)) + getString(R.string.act_set_tip_fbl) + CameraSettings.getCameraPreviewWidth() + "*" + CameraSettings.getCameraPreviewHeight() + "】");

        //人脸框镜像
        final boolean mirror = SpUtils.isMirror();
        cbMirror.setChecked(!mirror);
        cbMirror.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.setMirror(!isChecked);
            }
        });

        //摄像头角度
        int angle = SpUtils.getInt(SpUtils.CAMERA_ANGLE);
        btnAngle.setText(getString(R.string.act_set_tip_angle) + ":" + angle);

        setListSize();

        boolean faceDialog = SpUtils.getBoolean(SpUtils.FACE_DIALOG, false);
        switchFaceDialog.setChecked(faceDialog);
        switchFaceDialog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(SpUtils.FACE_DIALOG, isChecked);
            }
        });

        setSimilarInfo();
    }

    private void setSimilarInfo() {
        int similar = SpUtils.getIntOrDef(SpUtils.SIMILAR_THRESHOLD, 80);
        edtSimilar.setText(similar + "");

        findViewById(R.id.btn_set_similar_threshold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String similar = edtSimilar.getText().toString();
                int sml = Integer.parseInt(similar);
                if (sml >= 100) {//太大
                    UIUtils.showTitleTip(SettingActivity.this, "阈值过大将导致无法识别\n请重新设置");
                    return;
                } else if (sml <= 65) {//太小
                    UIUtils.showTitleTip(SettingActivity.this, "阈值过小会导致误识率过高\n请重新设置");
                    return;
                } else if (sml > 90) {//比较大
                    UIUtils.showTitleTip(SettingActivity.this, "阈值设置较大，识别速度将有所变慢\n设置成功");
                } else if (sml < 75) {//比较小
                    UIUtils.showTitleTip(SettingActivity.this, "阈值设置较小，误识率将增高\n设置成功");
                } else {
                    UIUtils.showTitleTip(SettingActivity.this, "设置成功");
                }

                SpUtils.saveInt(SpUtils.SIMILAR_THRESHOLD, sml);
                WelComeActivity activity = APP.getActivity();
                if (activity != null) {
                    activity.setFaceViewSimilar();
                }
            }
        });
    }

    private void checkNet() {
        String net = "";
        boolean intenetConnected = isIntenetConnected(this);
        if (intenetConnected) {
            net = getString(R.string.act_set_tip_ytwlipdz) + getHostIp() + "】";
        } else {
            net = "【WIFI，" + getWifiInfo(0) + getString(R.string.act_set_tip_IPAddress) + getWifiInfo(1) + "】";
        }
        tvNetState.setText(net);
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
        if (type == 0) {

            return getActivity().getResources().getString(R.string.act_set_tip_mc) + wi.getSSID() + getActivity().getResources().getString(R.string.act_set_tip_xhqd) + wi.getRssi();
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

                if (!TextUtils.isEmpty(validPath)
                        && (currentTemp != 0)) {
                    currTemp = currentTemp + "";
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

    private void setListSize() {
        List<Camera.Size> supportSizeList = ExtCameraManager.instance().getSupportSizeList();
        if (supportSizeList == null) {
            return;
        }
        Collections.sort(supportSizeList, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size o1, Camera.Size o2) {
                if (o1.width > o2.width) {
                    return -1;
                }

                if (o1.width == o2.width) {
                    if (o1.height > o2.height) {
                        return -1;
                    }
                    return 0;
                }
                return 1;
            }
        });


        final List<SizeBean> sizeBeanList = new ArrayList<>();
        for (Camera.Size size : supportSizeList) {
            SizeBean sizeBean = new SizeBean();
            sizeBean.width = size.width;
            sizeBean.height = size.height;
            float i = (float) size.width / (float) size.height;

            if (i < 1.6) {
                if (i > 1.3) {
                    sizeBean.desc = "（4 : 3）";
                } else {
                    sizeBean.desc = "（5 : 4）";
                }
            } else {
                sizeBean.desc = "（16 : 9）";
            }

            if (sizeBean.width == 1280 && sizeBean.height == 720) {
                sizeBean.desc += getString(R.string.act_set_tip_zj);
            } else if (sizeBean.width == 1920 && sizeBean.height == 1080) {
                sizeBean.desc += getString(R.string.act_set_tip_zd);
            }
            sizeBeanList.add(sizeBean);
        }

        int cameraWidth = CameraSettings.getCameraWidth();
        int cameraHeight = CameraSettings.getCameraHeight();

        int index = 0;
        for (int i = 0; i < sizeBeanList.size(); i++) {
            SizeBean sizeBean = sizeBeanList.get(i);
            if (cameraWidth == sizeBean.width && cameraHeight == sizeBean.height) {
                index = i;
            }
        }

        spnCameraSize.setAdapter(new SizeAdapter(sizeBeanList));
        Drawable drawable = getResources().getDrawable(R.drawable.shape_spinner_drop);
        spnCameraSize.setPopupBackgroundDrawable(drawable);
        spnCameraSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                SizeBean sizeBean = sizeBeanList.get(position);
                CameraSettings.setCameraPreviewWidth(sizeBean.width);
                CameraSettings.setCameraPreviewHeight(sizeBean.height);
                SpUtils.saveInt(SpUtils.CAMERA_WIDTH, sizeBean.width);
                SpUtils.saveInt(SpUtils.CAMERA_HEIGHT, sizeBean.height);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spnCameraSize.setSelection(index);
    }

    class SizeAdapter extends BaseAdapter {
        List<SizeBean> sizeBeanList;

        public SizeAdapter(List<SizeBean> sizeBeanList) {
            this.sizeBeanList = sizeBeanList;
        }

        @Override
        public int getCount() {
            return sizeBeanList.size();
        }

        @Override
        public Object getItem(int position) {

            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView inflate = (TextView) View.inflate(SettingActivity.this, android.R.layout.simple_dropdown_item_1line, null);
            inflate.setTextColor(Color.parseColor("#59B4CA"));
            inflate.setTextSize(16);
            inflate.setPadding(15, 15, 15, 15);
            SizeBean sizeBean = sizeBeanList.get(position);
            inflate.setText(sizeBean.width + " * " + sizeBean.height + " " + sizeBean.desc);
            return inflate;
        }
    }

    class SizeBean {
        int width;
        int height;
        String desc;
    }

    /**
     * ====功能区==================================================================================================
     */

    public void modifyPWD(View view) {
        setPwd();
    }

    public void checkUpgrade(View view) {
        UpdateVersionControl.getInstance().checkUpdate(this);
    }

    public void setAngle(final View view) {
        int anInt = SpUtils.getInt(SpUtils.CAMERA_ANGLE);
        if (anInt == CameraSettings.ROTATION_0) {
            anInt = CameraSettings.ROTATION_90;
        } else if (anInt == CameraSettings.ROTATION_90) {
            anInt = CameraSettings.ROTATION_180;
        } else if (anInt == CameraSettings.ROTATION_180) {
            anInt = CameraSettings.ROTATION_270;
        } else {
            anInt = CameraSettings.ROTATION_0;
        }
        ((Button) view).setText(getString(R.string.act_set_tip_angle) + ":" + anInt);
        SpUtils.saveInt(SpUtils.CAMERA_ANGLE, anInt);
        EventBus.getDefault().post(new DisplayOrientationEvent());
    }

    public void rebootDevice(View view) {
        showAlert(getString(R.string.act_set_tip_sbjcqsfjx), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ProgressDialog progressDialog = UIUtils.coreInfoShow3sDialog(SettingActivity.this);
                progressDialog.setTitle(getString(R.string.act_set_tip_reStart));
                progressDialog.setMessage(getString(R.string.act_set_tip_3shjcqsb));
                progressDialog.setCancelable(false);
                progressDialog.show();
                UIUtils.restart.start();
            }
        }, null, null);
    }

    private void showAlert(String msg, Dialog.OnClickListener onClickListener, Dialog.OnClickListener onCancel, DialogInterface.OnDismissListener onDissmissListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.base_tip));
        builder.setMessage(msg);
        builder.setPositiveButton(getString(R.string.base_ensure), onClickListener);
        builder.setNegativeButton(getString(R.string.base_cancel), onCancel);
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
                    edtPwd.setError(getString(R.string.act_set_error_mmbkwk));
                    return;
                }
                if (edtPwd.getText().length() < 6) {
                    edtPwd.setError(getString(R.string.act_set_error_mmzssr6w));
                    return;
                }
                if (TextUtils.isEmpty(edtPwd2.getText())) {
                    edtPwd2.setError(getString(R.string.act_set_error_qzcsrmm));
                    return;
                }
                String pwd = edtPwd.getText().toString();
                final String pwd2 = edtPwd2.getText().toString();
                if (!TextUtils.equals(pwd, pwd2)) {
                    edtPwd2.setError(getString(R.string.act_set_error_lcsrdmmbyz));
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
                                UIUtils.showTitleTip(SettingActivity.this, getString(R.string.act_set_error_modify_fail) + ":" + e != null ? e.getMessage() : "NULL");
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
                                    UIUtils.showTitleTip(SettingActivity.this, getString(R.string.act_set_error_modify_success));
                                    SpUtils.saveStr(SpUtils.MENU_PWD, pwd2);
                                    dialog.dismiss();
                                } else {
                                    UIUtils.showTitleTip(SettingActivity.this, getString(R.string.act_set_error_modify_fail));
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
        window.setBackgroundDrawableResource(android.R.color.transparent);
    }


    private void initSetIp() {
        final EditText edtIp = findViewById(R.id.edt_ip);
        final EditText edtResPort = findViewById(R.id.edt_res_port);
        final EditText edtXmppPort = findViewById(R.id.edt_xmpp_port);
        final EditText edtProName = findViewById(R.id.edt_pro_name);
        Button btnSave = findViewById(R.id.btn_save_address);

        final String ip = SpUtils.getStr(SpUtils.IP_CACHE);
        final String resPort = SpUtils.getStr(SpUtils.RESOURCE_PORT_CACHE);
        final String xmppPort = SpUtils.getStr(SpUtils.XMPP_PORT_CACHE);
        final String proName = SpUtils.getStr(SpUtils.PROJECT_NAME_SUFFIX);
        if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(resPort) || TextUtils.isEmpty(xmppPort) || TextUtils.isEmpty(proName)) {
            edtIp.setHint(Constants.NetConfig.PRO_URL);
            edtResPort.setHint(Constants.NetConfig.PRO_RES_PORT);
            edtXmppPort.setHint(Constants.NetConfig.PRO_XMPP_PORT);
            edtProName.setHint(Constants.NetConfig.PRO_SUFFIX);
        } else {
            edtIp.setText(ip);
            edtResPort.setText(resPort);
            edtXmppPort.setText(xmppPort);
            edtProName.setText(proName);
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mIp = edtIp.getText().toString();
                String mResPort = edtResPort.getText().toString();
                String mXmppPort = edtXmppPort.getText().toString();
                String mProName = edtProName.getText().toString();
                if (TextUtils.isEmpty(mIp)) {
                    UIUtils.showTitleTip(SettingActivity.this, "请设置IP地址");
                    return;
                }
                if (TextUtils.isEmpty(mResPort)) {
                    UIUtils.showTitleTip(SettingActivity.this, "请设置接口端口");
                    return;
                }
                if (TextUtils.isEmpty(mXmppPort)) {
                    UIUtils.showTitleTip(SettingActivity.this, "请设置XMPP端口");
                    return;
                }
                if (TextUtils.isEmpty(mProName)) {
                    UIUtils.showTitleTip(SettingActivity.this, "请设置项目名");
                    return;
                }

                if (TextUtils.equals(ip, mIp) && TextUtils.equals(resPort, mResPort) && TextUtils.equals(xmppPort, mXmppPort) && TextUtils.equals(proName, mProName)) {
                    UIUtils.showTitleTip(SettingActivity.this, "未修改");
                    return;
                }

                SpUtils.saveStr(SpUtils.IP_CACHE, mIp);
                SpUtils.saveStr(SpUtils.RESOURCE_PORT_CACHE, mResPort);
                SpUtils.saveStr(SpUtils.XMPP_PORT_CACHE, mXmppPort);
                SpUtils.saveStr(SpUtils.PROJECT_NAME_SUFFIX, mProName);
                UIUtils.showTitleTip(SettingActivity.this, "保存成功,重启APP后生效");
            }
        });

    }
}
