package com.yunbiao.ybsmartcheckin_live_id;


import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public class YBPermission {
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