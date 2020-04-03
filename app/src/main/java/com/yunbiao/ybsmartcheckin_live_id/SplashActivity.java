package com.yunbiao.ybsmartcheckin_live_id;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.activity.WelComeActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_safety_check.ThermalSafetyCheckActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.common.power.PowerOffTool;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Exception;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.activity_certificates.CertificatesActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.ThermalImageActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in_smt.SMTMainActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple.MultiThermalActivity;
import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.ThreadUitls;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

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
    protected void initData() {
        Constants.checkSetIp();

        ThreadUitls.runInThread(machineRestartRun);
        ybPermission = new YBPermission(new YBPermission.PermissionListener() {
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

                uploadException(nextRunnable);
            }
        });
        ybPermission.checkPermission(this, PERMISSONS);
    }

    public Runnable machineRestartRun = new Runnable() {
        public void run() {
            PowerOffTool.getPowerOffTool().machineStart();
        }
    };

    private void uploadException(final Runnable runnable) {
        UIUtils.showNetLoading(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
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
            }
        }).start();
    }

    private Runnable nextRunnable = new Runnable() {
        @Override
        public void run() {
            UIUtils.dismissNetLoading();

            Constants.initStorage();
            SpUtils.init();

            int code = FaceEngine.active(APP.getContext(), com.yunbiao.faceview.Constants.APP_ID, com.yunbiao.faceview.Constants.SDK_KEY);
            if (code == ErrorInfo.MOK || code == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                jump();
            } else {
                UIUtils.showShort(SplashActivity.this, getResources().getString(R.string.splash_active_failed));
            }

            overridePendingTransition(0, 0);
            finish();
        }
    };

    private void jump() {
        APP.bindProtectService();
        //普通考勤机
        if (Constants.DEVICE_TYPE == Constants.DeviceType.CHECK_IN) {//考勤机
            startActivity(new Intent(this, WelComeActivity.class));
            finish();
            return;
        }

        //测温考勤
        if (Constants.DEVICE_TYPE == Constants.DeviceType.TEMPERATURE_CHECK_IN || Constants.DEVICE_TYPE == Constants.DeviceType.HT_TEMPERATURE_CHECK_IN) {//测温考勤机
            //调整摄像头默认角度
            if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {//竖屏
                Constants.DEFAULT_CAMERA_ANGLE = 270;
            } else {
                Constants.DEFAULT_CAMERA_ANGLE = 0;//横屏
            }
//            startActivity(new Intent(SplashActivity.this, ThermalImageActivity.class));

            // TODO: 2020/4/3功能切换
            boolean jumpTag = SpUtils.getBoolean(Constants.JUMP_TAG, Constants.DEFAULT_JUMP_TAG);
            if(!jumpTag){
                startActivity(new Intent(SplashActivity.this, ThermalImageActivity.class));
            } else {
                if(Constants.DEVICE_TYPE == Constants.DeviceType.HT_TEMPERATURE_CHECK_IN){
                    Constants.DEVICE_TYPE = Constants.DeviceType.HT_TEMPERATURE_CERTIFICATES;
                } else if(Constants.DEVICE_TYPE == Constants.DeviceType.TEMPERATURE_CHECK_IN){
                    Constants.DEVICE_TYPE = Constants.DeviceType.TEMPERATURE_CERTIFICATES;
                }
                startActivity(new Intent(SplashActivity.this, CertificatesActivity.class));
            }
            finish();
            return;
        }

        //视美泰测温考勤
        if (Constants.DEVICE_TYPE == Constants.DeviceType.TEMPERATURE_CHECK_IN_SMT || Constants.DEVICE_TYPE == Constants.DeviceType.HT_TEMPERATURE_CHECK_IN_SMT) {//测温考勤机视美泰版
            Constants.DEFAULT_CAMERA_ANGLE = 270;
            startActivity(new Intent(SplashActivity.this, SMTMainActivity.class));
            finish();
            return;
        }

        //人证测温
        if (Constants.DEVICE_TYPE == Constants.DeviceType.TEMPERATURE_CERTIFICATES || Constants.DEVICE_TYPE == Constants.DeviceType.HT_TEMPERATURE_CERTIFICATES) {//人证机
            Constants.DEFAULT_CAMERA_ANGLE = 0;
//            startActivity(new Intent(this, CertificatesActivity.class));

            // TODO: 2020/4/3功能切换
            boolean jumpTag = SpUtils.getBoolean(Constants.JUMP_TAG, Constants.DEFAULT_JUMP_TAG);
            //判断，如果不跳转则默认进入人证
            if (!jumpTag) {
                startActivity(new Intent(this, CertificatesActivity.class));
            } else {
                //如果跳转则判断当前类型，如果是亨通考勤版本则把相应的类型改成对应的亨通人证版本
                if(Constants.DEVICE_TYPE == Constants.DeviceType.HT_TEMPERATURE_CERTIFICATES){//如果是亨通人证则修改为亨通考勤
                    Constants.DEVICE_TYPE = Constants.DeviceType.HT_TEMPERATURE_CHECK_IN;
                } else if(Constants.DEVICE_TYPE == Constants.DeviceType.TEMPERATURE_CERTIFICATES){//如果是云标人证则修改为云标考勤
                    Constants.DEVICE_TYPE = Constants.DeviceType.TEMPERATURE_CHECK_IN;
                }
                startActivity(new Intent(SplashActivity.this, ThermalImageActivity.class));
            }
            finish();
            return;
        }

        //高通量
        if (Constants.DEVICE_TYPE == Constants.DeviceType.MULTIPLE_THERMAL || Constants.DEVICE_TYPE == Constants.DeviceType.HT_MULTIPLE_THERMAL) {
            startActivity(new Intent(this, MultiThermalActivity.class));
            finish();
            return;
        }

        //安检门
        if (Constants.DEVICE_TYPE == Constants.DeviceType.TEMPER_SAFETY_CHECK || Constants.DEVICE_TYPE == Constants.DeviceType.HT_TEMPER_SAFETY_CHECK) {
            startActivity(new Intent(this, ThermalSafetyCheckActivity.class));
            finish();
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ybPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public static class YBPermission {
        public static final int PERMISSION_REQUEST_CODE = 101;
        private PermissionListener permissionListener;

        public YBPermission(PermissionListener permissionListener) {
            this.permissionListener = permissionListener;
        }

        interface PermissionListener {
            void onPermissionFailed(String[] objects);

            void onFinish(boolean isComplete);
        }

        public void checkPermission(Activity activity, String[] permissions) {
            if (permissions == null || permissions.length <= 0) {
                if (permissionListener != null) {
                    permissionListener.onFinish(false);
                }
                return;
            }

            List<String> deniedPermissionList = new ArrayList<>();
            for (String permission : permissions) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissionList.add(permission);
                    }
                }
            }

            if (deniedPermissionList.size() > 0) {
                String[] strings = deniedPermissionList.toArray(new String[deniedPermissionList.size()]);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity.requestPermissions(strings, PERMISSION_REQUEST_CODE);
                }
            } else {
                if (permissionListener != null) {
                    permissionListener.onFinish(true);
                }
            }
        }

        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            if (requestCode != PERMISSION_REQUEST_CODE) {
                return;
            }

            if (permissions == null || permissions.length <= 0) {
                if (permissionListener != null) {
                    permissionListener.onFinish(false);
                }
                return;
            }

            List<String> permiList = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    permiList.add(permissions[i]);
                }
            }
            if (permiList.size() <= 0) {
                if (permissionListener != null) {
                    permissionListener.onFinish(true);
                }
                return;
            }

            if (permissionListener != null) {
                permissionListener.onPermissionFailed(permiList.toArray(new String[permiList.size()]));
            }
        }
    }
}
