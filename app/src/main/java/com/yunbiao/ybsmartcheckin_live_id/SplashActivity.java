package com.yunbiao.ybsmartcheckin_live_id;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.activity.WelComeActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_certificates.CertificatesConst;
import com.yunbiao.ybsmartcheckin_live_id.activity_safety_check.ThermalSafetyCheckActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_safety_check_double_light.SafetyCheckDoubleLightActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.ThermalConst;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.ThermalImage2Activity;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in_smt.SMTMain2Activity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.common.power.PowerOffTool;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Exception;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.activity_certificates.CertificatesActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple.MultiThermalActivity;
import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.ThreadUitls;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

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
        try {
            GifDrawable gifDrawable = new GifDrawable(getResources(),R.mipmap.splash);
            gifImageView.setImageDrawable(gifDrawable);
            gifDrawable.setLoopCount(0);
            gifDrawable.setSpeed(3.0f);
        } catch (IOException e) {
            e.printStackTrace();
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
            Constants.checkSetIp();
            Constants.initStorage();
            OutputLog.getInstance().initFile(Constants.LOCAL_ROOT_PATH);

            ThreadUitls.runInThread(() -> PowerOffTool.getPowerOffTool().machineStart());

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
            int code = FaceEngine.active(APP.getContext(), com.yunbiao.faceview.Constants.APP_ID, com.yunbiao.faceview.Constants.SDK_KEY);
            Log.e(TAG, "激活结果: " + code);
            if (code != ErrorInfo.MOK && code != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UIUtils.showShort(SplashActivity.this, getResources().getString(R.string.splash_active_failed));
                    }
                });
            }
            jump();
            finish();
        }).start();
    };

    private void jump() {
        switch (Constants.FLAVOR_TYPE) {
            case FlavorType.HT:
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.logo_icon_horizontal;
                ThermalConst.Default.MAIN_LOGO_TEXT = "HT";
                break;
            case FlavorType.SK:
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.icon_logo3;
                ThermalConst.Default.MAIN_LOGO_TEXT = "";
                break;
            case FlavorType.OSIMLE:
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.osmile_logo;
                ThermalConst.Default.MAIN_LOGO_TEXT = "";
                break;
            case FlavorType.SOFT_WORK_Z:
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.softworkz_logo;
                ThermalConst.Default.MAIN_LOGO_TEXT = "";
                break;
            case FlavorType.SCAN_TEMP:
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.scan_temp;
                ThermalConst.Default.MAIN_LOGO_TEXT = "";
                break;
            default:
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.yb_logo;
                ThermalConst.Default.MAIN_LOGO_TEXT = "YBFACE";
                break;
        }

        String broadTypeStr = CommonUtils.getBroadType2();
        Log.e(TAG, "jump: 板卡信息：" + broadTypeStr);
        switch (broadTypeStr) {
            case "SMT":
                Constants.DEFAULT_CAMERA_ANGLE = 270;
                Constants.DEFAULT_H_MIRROR = false;
                CertificatesConst.Default.MODE = CertificatesConst.Mode.CERTIFICATES_THERMAL_16_4;
                ThermalConst.Default.MODE = ThermalConst.ONLY_THERMAL_HM_16_4;
                break;
            case "LXR":
                Constants.DEFAULT_CAMERA_ANGLE = 0;//横屏
                Constants.DEFAULT_H_MIRROR = true;
                CertificatesConst.Default.MODE = CertificatesConst.Mode.CERTIFICATES_THERMAL;
                ThermalConst.Default.MODE = ThermalConst.ONLY_THERMAL_HM_32_32;
                break;
            case "HARRIS":
            default:
                Constants.DEFAULT_CAMERA_ANGLE = 90;
                Constants.DEFAULT_H_MIRROR = false;
                CertificatesConst.Default.MODE = CertificatesConst.Mode.CERTIFICATES_THERMAL_16_4;
                ThermalConst.Default.MODE = ThermalConst.ONLY_THERMAL_MLX_16_4;
                break;
        }

        switch (Constants.DEVICE_TYPE) {
            case Constants.DeviceType.CHECK_IN:
                startActivity(new Intent(this, WelComeActivity.class));
                break;
            case Constants.DeviceType.TEMPERATURE_CHECK_IN:
            case Constants.DeviceType.HT_TEMPERATURE_CHECK_IN:
                if (SpUtils.getBoolean(Constants.JUMP_TAG, Constants.DEFAULT_JUMP_TAG)) {
                    startActivity(new Intent(SplashActivity.this, CertificatesActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, ThermalImage2Activity.class));
                }
                break;
            case Constants.DeviceType.TEMPERATURE_CHECK_IN_SMT:
            case Constants.DeviceType.HT_TEMPERATURE_CHECK_IN_SMT:
                Constants.DEFAULT_CAMERA_ANGLE = 270;
                Constants.DEFAULT_H_MIRROR = false;
                startActivity(new Intent(SplashActivity.this, SMTMain2Activity.class));
                break;
            case Constants.DeviceType.TEMPERATURE_CERTIFICATES:
            case Constants.DeviceType.HT_TEMPERATURE_CERTIFICATES:
                //判断，如果不跳转则默认进入人证
                if (SpUtils.getBoolean(Constants.JUMP_TAG, Constants.DEFAULT_JUMP_TAG)) {
                    startActivity(new Intent(SplashActivity.this, ThermalImage2Activity.class));
                } else {
                    startActivity(new Intent(this, CertificatesActivity.class));
                }
                break;
            case Constants.DeviceType.MULTIPLE_THERMAL:
            case Constants.DeviceType.HT_MULTIPLE_THERMAL:
                Constants.DEFAULT_CAMERA_ANGLE = 0;
                startActivity(new Intent(this, MultiThermalActivity.class));
                break;
            case Constants.DeviceType.TEMPER_SAFETY_CHECK:
            case Constants.DeviceType.HT_TEMPER_SAFETY_CHECK:
                startActivity(new Intent(this, ThermalSafetyCheckActivity.class));
                break;
            case Constants.DeviceType.SAFETY_CHECK_DOUBLE_LIGHT:
            case Constants.DeviceType.HT_SAFETY_CHECK_DOUBLE_LIGHT:
                Constants.HORIZONTAL_OFFSET = 4;
                Constants.DEFAULT_H_MIRROR = false;
                Constants.CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_FRONT;
                startActivity(new Intent(this, SafetyCheckDoubleLightActivity.class));
                break;
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
                    .connTimeOut(5000)
                    .readTimeOut(5000)
                    .writeTimeOut(5000)
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
