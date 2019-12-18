package com.yunbiao.yb_smart_attendance;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtil {
    public static String[] PERMISSONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE
            , android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            , android.Manifest.permission.ACCESS_FINE_LOCATION
            , android.Manifest.permission.ACCESS_COARSE_LOCATION
            , android.Manifest.permission.READ_PHONE_STATE
            , android.Manifest.permission.CAMERA
            /*,Manifest.permission.SYSTEM_ALERT_WINDOW*/};
    private static final int PERMI_REQ_CODE = 123;

    public static void check(Activity activity,PermissionCallback permissionCallback) {
        List<String> notAlloweds = cycleInspection(activity);
        if(notAlloweds.size() <= 0){
            permissionCallback.onAllPermissionAllowed(true);
            return;
        }
        String[] strings = notAlloweds.toArray(new String[notAlloweds.size()]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(strings,PERMI_REQ_CODE);
        }
    }

    private static List<String> cycleInspection(Activity activity){
        List<String> notAlloweds = new ArrayList<>();
        for (String permission : PERMISSONS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    notAlloweds.add(permission);
                }
            }
        }
        return notAlloweds;
    }

    public static void onActivityResult(Activity activity,int requestCode,PermissionCallback callback){
        if(requestCode != PERMI_REQ_CODE){
            return;
        }
        List<String> strings = cycleInspection(activity);
        if(callback != null){
            callback.onAllPermissionAllowed(strings.size() <= 0);
        }
    }

    interface PermissionCallback {
        void onAllPermissionAllowed(boolean isAllAllowed);
    }
}
