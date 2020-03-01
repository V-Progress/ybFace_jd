package com.yunbiao.ybsmartcheckin_live_id;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.yunbiao.ybsmartcheckin_live_id.activity.PassageDeviceActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.WelComeActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends BaseActivity {
    private static final String TAG = "SplashActivity";

    public static String[] PERMISSONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE
            ,android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ,android.Manifest.permission.ACCESS_FINE_LOCATION
            ,android.Manifest.permission.ACCESS_COARSE_LOCATION
            ,android.Manifest.permission.READ_PHONE_STATE
            ,android.Manifest.permission.CAMERA
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
//        APP.bindProtectService();

        Config.deviceType = CommonUtils.getBroadType();
        ybPermission = new YBPermission(new YBPermission.PermissionListener(){
            @Override
            public void onPermissionFailed(String[] objects) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(objects, YBPermission.PERMISSION_REQUEST_CODE);
                }
            }

            @Override
            public void onFinish(boolean isComplete) {
                if(isComplete){
                    Constants.initStorage();
                    SpUtils.init();

                    int code = FaceEngine.active(APP.getContext(), com.yunbiao.faceview.Constants.APP_ID, com.yunbiao.faceview.Constants.SDK_KEY);
                    if (code == ErrorInfo.MOK || code == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                        Log.e(TAG, "激活成功或已激活");
                        jump();
                    } else {
                        Toast.makeText(SplashActivity.this, "激活失败", Toast.LENGTH_SHORT).show();
                    }

                    overridePendingTransition(0,0);
                    finish();
                    return;
                } else {
                    UIUtils.showTitleTip(SplashActivity.this,getString(R.string.act_spl_tip_qxsqsb));
                }
            }
        });
        ybPermission.checkPermission(this,PERMISSONS);
    }

    private void jump(){
        // TODO: 2019/12/21 设置IP地址
        Constants.checkSetIp();

        if (Constants.SCREEN_TYPE == Constants.ScreenType.TYPE_PORTRAIT_8_800_1280) {
            startActivity(new Intent(SplashActivity.this, PassageDeviceActivity.class));
        } else {
            startActivity(new Intent(SplashActivity.this, WelComeActivity.class));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ybPermission.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    public static class YBPermission{
        public static final int PERMISSION_REQUEST_CODE = 101;
        private PermissionListener permissionListener;
        public YBPermission(PermissionListener permissionListener) {
            this.permissionListener = permissionListener;
        }

        interface PermissionListener{
            void onPermissionFailed(String[] objects);
            void onFinish(boolean isComplete);
        }

        public void checkPermission(Activity activity,String[] permissions){
            if(permissions == null || permissions.length <= 0){
                if(permissionListener != null){
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

            if(deniedPermissionList .size() > 0){
                String[] strings = deniedPermissionList.toArray(new String[deniedPermissionList.size()]);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity.requestPermissions(strings,PERMISSION_REQUEST_CODE);
                }
            } else {
                if(permissionListener != null){
                    permissionListener.onFinish(true);
                }
            }
        }

        public void onRequestPermissionsResult(int requestCode, String[] permissions,  int[] grantResults){
            if(requestCode != PERMISSION_REQUEST_CODE){
                return;
            }

            if (permissions == null || permissions.length<=0) {
                if(permissionListener != null){
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
            if(permiList.size() <= 0){
                if(permissionListener != null){
                    permissionListener.onFinish(true);
                }
                return;
            }

            if(permissionListener != null){
                permissionListener.onPermissionFailed(permiList.toArray(new String[permiList.size()]));
            }
        }
    }
}
