package com.yunbiao.ybsmartcheckin_live_id;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.activity.WelComeActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_certificates.CertificatesConst;
import com.yunbiao.ybsmartcheckin_live_id.activity_safety_check.ThermalSafetyCheckActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_safety_check_double_light.SafetyCheckDoubleLightActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.ConfigLoader;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.TemperModuleType;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.ThermalConst;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.ThermalImage2Activity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.common.power.PowerOffTool;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Exception;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.activity_certificates.CertificatesActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple.MultiThermalActivity;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.activity.Main5InchActivity;
import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.ThreadUitls;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import timber.log.Timber;

public class SplashActivity extends BaseActivity {
    private static final String TAG = "SplashActivity";

    public static String[] PERMISSONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE
            , android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            , android.Manifest.permission.ACCESS_FINE_LOCATION
            , android.Manifest.permission.ACCESS_COARSE_LOCATION
            , android.Manifest.permission.READ_PHONE_STATE
            , android.Manifest.permission.CAMERA
            /*,Manifest.permission.SYSTEM_ALERT_WINDOW*/};
    private YBPermission ybPermission;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_splash;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initView() {
        super.initView();

        GifImageView gifImageView = findViewById(R.id.giv);
        setOpenGif(gifImageView);

        String deviceSN = HeartBeatClient.getDeviceSN();
        Timber.e("initView: 当前设备的DeviceSN是：%s", deviceSN);
        String localMac = CommonUtils.getLocalMac();
        String wifiMac = CommonUtils.getWifiMac();
        Timber.e("initView: 当前wifiMac：%s", wifiMac);
        Timber.e("initView: 当前localMac：%s", localMac);
    }

    private void setOpenGif(GifImageView gifImageView) {
        File splashDir = new File(Constants.SPLASH_DIR_PATH);
        Log.e(TAG, "闪屏动画：" + splashDir.getPath());
        if (!splashDir.exists() || !splashDir.isDirectory()) {
            splashDir.mkdirs();
        }

        GifDrawable gifDrawable;

        File[] files = splashDir.listFiles(pathname -> pathname.isFile());
        if (files != null && files.length > 0) {
            Arrays.sort(files, (o1, o2) -> o1.isDirectory() && o2.isFile() ? -1 : o1.isFile() && o2.isDirectory() ? 1 : o1.getName().compareTo(o2.getName()));
            File file = files[0];
            try {
                gifDrawable = new GifDrawable(file);
                gifImageView.setImageDrawable(gifDrawable);
                gifDrawable.setLoopCount(0);
                gifDrawable.setSpeed(3.0f);
            } catch (IOException e) {
                gifImageView.setImageURI(Uri.fromFile(file));
            }
        } else {
            try {
                gifDrawable = new GifDrawable(getResources(), R.mipmap.splash);
                gifImageView.setImageDrawable(gifDrawable);
                gifDrawable.setLoopCount(0);
                gifDrawable.setSpeed(3.0f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void initData() {
        ybPermission = new YBPermission(permissionListener);
        ybPermission.checkPermission(this, PERMISSONS);
    }

    private YBPermission.PermissionListener permissionListener = new YBPermission.PermissionListener() {
        @Override
        public void onPermissionFailed(String[] objects) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(objects, YBPermission.PERMISSION_REQUEST_CODE);
            }
        }

        @Override
        public void onFinish(boolean isComplete) {
            if (!isComplete) {
                UIUtils.showShort(SplashActivity.this, getString(R.string.splash_request_permission_failed));
                return;
            }

            SpUtils.init();
            DaoManager.get().initDb();

            switch (Constants.FLAVOR_TYPE) {
                case FlavorType.XENON:
                    setIp("api-eu.feverdefence.com", "34.247.168.20", "5222", "8080", "");
                    break;
                /*case FlavorType.YB:
                    setIp("192.168.255.115","192.168.255.115","5222","80","ybface");
                    break;*/
                default:
                    checkServiceIp();
                    break;
            }

            Constants.checkSetIp();
            Constants.initStorage();
            OutputLog.getInstance().initFile(Constants.LOCAL_ROOT_PATH);
            ThreadUitls.runInThread(() -> PowerOffTool.getPowerOffTool().machineStart());

            if (Constants.DEVICE_TYPE == Constants.DeviceType.TEMPERATURE_CHECK_IN
                    || Constants.DEVICE_TYPE == Constants.DeviceType.HT_TEMPERATURE_CHECK_IN) {
                ConfigLoader.load();
            }

            uploadException(nextRunnable);
        }
    };

    private Runnable nextRunnable = () -> {
        UIUtils.dismissNetLoading();
        APP.bindProtectService();
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            boolean canGo = Constants.DEVICE_TYPE != Constants.DeviceType.TEMPER_SAFETY_CHECK
                    && Constants.DEVICE_TYPE != Constants.DeviceType.HT_TEMPER_SAFETY_CHECK
                    && Constants.DEVICE_TYPE != Constants.DeviceType.TEMPERATURE_MEASUREMENT_5_INCH;
            FaceSDKActive.active(FaceSDKActive.TYPE_REMOTE, canGo, (result, message) -> {
                if(!result){
                    runOnUiThread(() -> UIUtils.showLong(SplashActivity.this,getResources().getString(R.string.splash_active_failed) + "(" + message + ")"));
                }
                jump();
                finish();
            });
        }).start();
    };

    private void jump() {
        switch (Constants.FLAVOR_TYPE) {
            case FlavorType.HT:
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.logo_icon_horizontal;
                ThermalConst.Default.MAIN_LOGO_TEXT = "";
                ThermalConst.Default.WELCOME_TIP_CONTENT = getResString(R.string.setting_default_welcome_tip);
                break;
            case FlavorType.SK:
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.icon_logo3;
                ThermalConst.Default.MAIN_LOGO_TEXT = "";
                ThermalConst.Default.WELCOME_TIP_CONTENT = getResString(R.string.setting_default_welcome_tip);
                break;
            case FlavorType.OSIMLE:
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.osimle_logo;
                ThermalConst.Default.MAIN_LOGO_TEXT = "";
                ThermalConst.Default.WELCOME_TIP_CONTENT = getResString(R.string.setting_default_welcome_tip);
                break;
            case FlavorType.SOFT_WORK_Z:
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.softworkz_logo;
                ThermalConst.Default.MAIN_LOGO_TEXT = "";
                ThermalConst.Default.WELCOME_TIP_CONTENT = getResString(R.string.setting_default_welcome_tip);
                break;
            case FlavorType.SCAN_TEMP:
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.scan_temp;
                ThermalConst.Default.MAIN_LOGO_TEXT = "";
                ThermalConst.Default.WELCOME_TIP_CONTENT = getResString(R.string.setting_default_welcome_tip);
                break;
            case FlavorType.PING_TECH:
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.pingtech_logo;
                ThermalConst.Default.MAIN_LOGO_TEXT = "";
                ThermalConst.Default.WELCOME_TIP_CONTENT = getResString(R.string.setting_default_welcome_tip);
                break;
            case FlavorType.XENON:
                Constants.DEFAULT_SCREE_BG = R.mipmap.xenon_screen_saver;
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.xenon_logo;
                ThermalConst.Default.MAIN_LOGO_TEXT = "";
                ThermalConst.Default.WELCOME_TIP_CONTENT = getResString(R.string.xenon_welcome_tip);
                ThermalConst.Default.SHOW_MAIN_LOGO = true;
                Constants.Default.POSTER_ENABLED = true;
                ThermalConst.Default.SHOW_MAIN_INFO = true;
                Constants.Default.QRCODE_ENABLED = false;
                break;
            case FlavorType.TURKEY:
                ThermalConst.Default.LOW_TEMP = false;
                ThermalConst.Default.AUTO_TEMPER = false;
                break;
            default:
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.yb_logo;
                ThermalConst.Default.MAIN_LOGO_TEXT = "YBFACE";
                ThermalConst.Default.WELCOME_TIP_CONTENT = getResString(R.string.setting_default_welcome_tip);
                break;
        }

        String broadTypeStr = CommonUtils.getBroadType2();
        Log.e(TAG, "jump: 板卡信息：" + broadTypeStr);
        switch (broadTypeStr) {
            case "SMT":
                Constants.Default.CAMERA_ANGLE = 270;
                Constants.Default.IS_H_MIRROR = false;
                CertificatesConst.Default.MODE = CertificatesConst.Mode.CERTIFICATES_THERMAL_16_4;
                ThermalConst.Default.TEMPER_MODULE = TemperModuleType.HM_16_4;
                break;
            case "LXR":
                Constants.Default.CAMERA_ANGLE = 0;//横屏
                Constants.Default.IS_H_MIRROR = true;
                CertificatesConst.Default.MODE = CertificatesConst.Mode.CERTIFICATES_THERMAL;
                ThermalConst.Default.TEMPER_MODULE = TemperModuleType.HM_32_32;
                break;
            case "HARRIS":
            default:
                Constants.Default.CAMERA_ANGLE = 90;
                Constants.Default.IS_H_MIRROR = false;
                CertificatesConst.Default.MODE = CertificatesConst.Mode.CERTIFICATES_THERMAL_16_4;
                ThermalConst.Default.TEMPER_MODULE = TemperModuleType.MLX_16_4;
                break;
        }

        int lastModel = SpUtils.getIntOrDef("thermalModelSetting", -1);
        if (lastModel != -1) {
            Log.e(TAG, "jump: 检测到旧模式：" + lastModel);
            NewModuleType newModuleType = oldModelToModuleType(lastModel);
            Log.e(TAG, "jump: 转换为新模式：" + newModuleType.toString());
            SpUtils.saveInt(ThermalConst.Key.TEMPER_MODULE, newModuleType.module);
            SpUtils.saveBoolean(ThermalConst.Key.FACE_ENABLED, newModuleType.faceEnabled);
            SpUtils.saveBoolean(ThermalConst.Key.TEMPER_ENABLED, newModuleType.temperEnabled);
            SpUtils.remove("thermalModelSetting");
        }

        switch (Constants.DEVICE_TYPE) {
            case Constants.DeviceType.CHECK_IN:
                startActivity(new Intent(this, WelComeActivity.class));
                break;
            case Constants.DeviceType.TEMPERATURE_CHECK_IN:
            case Constants.DeviceType.HT_TEMPERATURE_CHECK_IN:
                if (SpUtils.getBoolean(Constants.Key.JUMP_TAG, Constants.Default.JUMP_TAG)) {
                    startActivity(new Intent(SplashActivity.this, CertificatesActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, ThermalImage2Activity.class));
                }
                break;
            case Constants.DeviceType.TEMPERATURE_CERTIFICATES:
            case Constants.DeviceType.HT_TEMPERATURE_CERTIFICATES:
                //判断，如果不跳转则默认进入人证
                if (SpUtils.getBoolean(Constants.Key.JUMP_TAG, Constants.Default.JUMP_TAG)) {
                    startActivity(new Intent(SplashActivity.this, ThermalImage2Activity.class));
                } else {
                    startActivity(new Intent(this, CertificatesActivity.class));
                }
                break;
            case Constants.DeviceType.MULTIPLE_THERMAL:
            case Constants.DeviceType.HT_MULTIPLE_THERMAL:
                Constants.Default.CAMERA_ANGLE = 0;
                startActivity(new Intent(this, MultiThermalActivity.class));
                break;
            case Constants.DeviceType.TEMPER_SAFETY_CHECK:
            case Constants.DeviceType.HT_TEMPER_SAFETY_CHECK:
                startActivity(new Intent(this, ThermalSafetyCheckActivity.class));
                break;
            case Constants.DeviceType.SAFETY_CHECK_DOUBLE_LIGHT:
            case Constants.DeviceType.HT_SAFETY_CHECK_DOUBLE_LIGHT:
                Constants.HORIZONTAL_OFFSET = 4;
                Constants.Default.IS_H_MIRROR = false;
                Constants.CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_FRONT;
                startActivity(new Intent(this, SafetyCheckDoubleLightActivity.class));
                break;
            case Constants.DeviceType.TEMPERATURE_MEASUREMENT_5_INCH:
                Constants.Default.CAMERA_ANGLE = 0;
                startActivity(new Intent(this, Main5InchActivity.class));
                break;
        }
    }

    private void checkServiceIp() {
        int intOrDef = SpUtils.getIntOrDef(Constants.Key.SERVER_MODEL, Constants.Default.SERVER_MODEL);
        if (intOrDef == Constants.serverModel.JU) {
            String serIp = SpUtils.getStr(Constants.Key.JU_SERVICE_IP_CACHE, "");
            if (TextUtils.isEmpty(serIp)) {
                String xmppIp = SpUtils.getStr(Constants.Key.JU_XMPP_IP_CACHE, "");
                SpUtils.saveStr(Constants.Key.JU_SERVICE_IP_CACHE, xmppIp);
            }
        }
    }

    private void setIp(String sIp, String cIp, String xPort, String rPort, String pName) {
        int intOrDef = SpUtils.getIntOrDef(Constants.Key.SERVER_MODEL, -99);
        if (intOrDef == -99)
            SpUtils.saveInt(Constants.Key.SERVER_MODEL, Constants.serverModel.JU);

        String serIp = SpUtils.getStr(Constants.Key.JU_SERVICE_IP_CACHE, "");
        Log.e(TAG, "setIp: 设置的服务地址：" + serIp);
        if (TextUtils.isEmpty(serIp))
            SpUtils.saveStr(Constants.Key.JU_SERVICE_IP_CACHE, sIp);
        String resPort = SpUtils.getStr(Constants.Key.JU_RESOURCE_PORT_CACHE, "");
        if (TextUtils.isEmpty(resPort))
            SpUtils.saveStr(Constants.Key.JU_RESOURCE_PORT_CACHE, rPort);
        String projectName = SpUtils.getStr(Constants.Key.JU_PROJECT_NAME_SUFFIX, "");
        if (TextUtils.isEmpty(projectName))
            SpUtils.saveStr(Constants.Key.JU_PROJECT_NAME_SUFFIX, pName);

        String xmppIp = SpUtils.getStr(Constants.Key.JU_XMPP_IP_CACHE, "");
        if (TextUtils.isEmpty(xmppIp))
            SpUtils.saveStr(Constants.Key.JU_XMPP_IP_CACHE, cIp);
        String xmppPort = SpUtils.getStr(Constants.Key.JU_XMPP_PORT_CACHE, "");
        if (TextUtils.isEmpty(xmppPort))
            SpUtils.saveStr(Constants.Key.JU_XMPP_PORT_CACHE, xPort);

    }

    private NewModuleType oldModelToModuleType(int model) {
        NewModuleType newModuleType = new NewModuleType();
        switch (model) {
            case 0:
                newModuleType.module = -1;
                newModuleType.faceEnabled = true;
                newModuleType.temperEnabled = false;
                break;
            case 1:
                newModuleType.module = -1;
                newModuleType.faceEnabled = false;
                newModuleType.temperEnabled = true;
                break;
            case 2:
                newModuleType.module = -1;
                newModuleType.faceEnabled = true;
                newModuleType.temperEnabled = true;
                break;
            case 3:
                newModuleType.module = TemperModuleType.HM_32_32;
                newModuleType.faceEnabled = false;
                newModuleType.temperEnabled = true;
                break;
            case 4:
                newModuleType.module = TemperModuleType.HM_32_32;
                newModuleType.faceEnabled = true;
                newModuleType.temperEnabled = true;
                break;
            case 5:
                newModuleType.module = TemperModuleType.HM_16_4;
                newModuleType.faceEnabled = false;
                newModuleType.temperEnabled = true;
                break;
            case 6:
                newModuleType.module = TemperModuleType.HM_16_4;
                newModuleType.faceEnabled = true;
                newModuleType.temperEnabled = true;
                break;
            case 7:
                newModuleType.module = TemperModuleType.MLX_16_4;
                newModuleType.faceEnabled = false;
                newModuleType.temperEnabled = true;
                break;
            case 8:
                newModuleType.module = TemperModuleType.MLX_16_4;
                newModuleType.faceEnabled = true;
                newModuleType.temperEnabled = true;
                break;
            case 9:
                newModuleType.module = TemperModuleType.SMT_32_32;
                newModuleType.faceEnabled = false;
                newModuleType.temperEnabled = true;
                break;
            case 10:
                newModuleType.module = TemperModuleType.SMT_32_32;
                newModuleType.faceEnabled = true;
                newModuleType.temperEnabled = true;
                break;
        }

        if (newModuleType.module == -1) {
            String broadTypeStr = CommonUtils.getBroadType2();
            if (TextUtils.equals("SMT", broadTypeStr)) {
                newModuleType.module = TemperModuleType.HM_16_4;
            } else if (TextUtils.equals("LXR", broadTypeStr)) {
                newModuleType.module = TemperModuleType.HM_32_32;
            } else if (TextUtils.equals("HARRIS", broadTypeStr)) {
                newModuleType.module = TemperModuleType.MLX_16_4;
            }
        }
        return newModuleType;
    }

    class NewModuleType {
        int module;
        boolean faceEnabled;
        boolean temperEnabled;

        @Override
        public String toString() {
            return "NewModuleType{" +
                    "module=" + module +
                    ", faceEnabled=" + faceEnabled +
                    ", temperEnabled=" + temperEnabled +
                    '}';
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ybPermission != null) {
            ybPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //上传错误日志
    private void uploadException(final Runnable runnable) {
        UIUtils.showNetLoading(this);
        new Thread(() -> {
            final List<Exception> exceptions = DaoManager.get().queryAll(Exception.class);
            if (exceptions == null || exceptions.size() <= 0) {
                Log.e(TAG, "run: 没有异常");
                if (runnable != null) {
                    runOnUiThread(runnable);
                }
                return;
            }
            Log.e(TAG, "run: 异常条数：" + exceptions.size());
            //地址
            String url = ResourceUpdate.DEVICE_EXCEPTION_UPLOAD;
            //版本号
            String versionName = "x.x.x";
            int versionCode = -1;
            try {
                PackageManager mPackageManager = getPackageManager();
                PackageInfo packageInfo = mPackageManager.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
                versionName = packageInfo.versionName;
                versionCode = packageInfo.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            //设备号
            String str = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
            if (TextUtils.isEmpty(str)) {
                str = "";
            }
            //转jsonString
            String crashArray = new Gson().toJson(exceptions);
            //参数
            Map<String, String> params = new HashMap<>();
            params.put("deviceId", HeartBeatClient.getDeviceNo() + "");
            params.put("deviceType", Constants.DEVICE_TYPE + "");
            params.put("versionName", versionName + "");
            params.put("versionCode", versionCode + "");
            params.put("cpuAbi", "");
            params.put("boardType", CommonUtils.saveBroadInfo() + "");
            params.put("deviceNumber", str + "");
            params.put("crasharray", crashArray + "");

            Log.e(TAG, "异常上传：" + url);
            Log.e(TAG, "参数：" + params.toString());

            OkHttpUtils.post()
                    .url(ResourceUpdate.DEVICE_EXCEPTION_UPLOAD)
                    .params(params)
                    .build()
                    .readTimeOut(5000)
                    .writeTimeOut(5000)
                    .connTimeOut(5000)
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, java.lang.Exception e, int id) {
                            Log.e(TAG, "onError: 上传失败：" + (e == null ? "NULL" : e.getMessage()));
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.e(TAG, "onResponse: 上传结果：" + response);
                            for (Exception exception : exceptions) {
                                DaoManager.get().delete(exception);
                            }
                        }

                        @Override
                        public void onAfter(int id) {
                            if (runnable != null) {
                                runnable.run();
                            }
                        }
                    });
        }).start();
    }

}
