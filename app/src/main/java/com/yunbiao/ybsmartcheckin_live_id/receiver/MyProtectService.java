package com.yunbiao.ybsmartcheckin_live_id.receiver;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.Nullable;

import android.util.Log;
import android.widget.Toast;

import com.yunbiao.ybsmartcheckin_live_id.SplashActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.WelComeActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_certificates.CertificatesActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_safety_check.ThermalSafetyCheckActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_safety_check_double_light.SafetyCheckDoubleLightActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.ThermalImage2Activity;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple.MultiThermalActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class MyProtectService extends Service {
    private static final String TAG = "MyProtectService";

    //看门狗service
    private String packageName = "com.yunbiao.ybsmartcheckin_live_id";
    private final static int DELAY_TIME = 90 * 1000;//90s轮询一次
    private final static int CHECK_APP = 0x3211;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: 开启守护进程");
        mHandler.sendEmptyMessageDelayed(CHECK_APP, DELAY_TIME);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == CHECK_APP) {
                Timber.e("守护进程：轮询");
                if (isRunBackground(getApplicationContext())) {
                    Class currMainActivity = getCurrMainActivity();
                    Timber.e("守护进程：检测到程序在后台运行，启用到前台：%s", currMainActivity.getName());
                    backgroundToForeground(currMainActivity);
                } else if (!isMyAppRunning(MyProtectService.this, packageName)) {
                    Timber.e("守护进程：检测到程序未运行，启用APP");
                    startTargetActivity(MyProtectService.this, SplashActivity.class);
                }
                mHandler.sendEmptyMessageDelayed(CHECK_APP, DELAY_TIME);
            }
            return false;
        }
    });

    private Class getCurrMainActivity(){
        Class clazz;
        switch (Constants.DEVICE_TYPE) {
            case Constants.DeviceType.CHECK_IN:
                clazz = WelComeActivity.class;
                break;
            case Constants.DeviceType.TEMPERATURE_CHECK_IN:
            case Constants.DeviceType.HT_TEMPERATURE_CHECK_IN:
                clazz = ThermalImage2Activity.class;
                break;
            case Constants.DeviceType.HT_MULTIPLE_THERMAL:
            case Constants.DeviceType.MULTIPLE_THERMAL:
                clazz = MultiThermalActivity.class;
                break;
            case Constants.DeviceType.HT_SAFETY_CHECK_DOUBLE_LIGHT:
            case Constants.DeviceType.SAFETY_CHECK_DOUBLE_LIGHT:
                clazz = SafetyCheckDoubleLightActivity.class;
                break;
            case Constants.DeviceType.HT_TEMPER_SAFETY_CHECK:
            case Constants.DeviceType.TEMPER_SAFETY_CHECK:
                clazz = ThermalSafetyCheckActivity.class;
                break;
            case Constants.DeviceType.TEMPERATURE_CERTIFICATES:
            case Constants.DeviceType.HT_TEMPERATURE_CERTIFICATES:
                clazz = CertificatesActivity.class;
                break;
            default:
                clazz = ThermalImage2Activity.class;
                break;
        }
        return clazz;
    }

    /***
     * 后台——>前台
     * @param clazz
     */
    private void backgroundToForeground(Class clazz){
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(getApplicationContext().getPackageName(), clazz.getName()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
    }

    private void openAPP(String appPackageName){
        try{
            //如果有，直接打开
            Intent intent = this.getPackageManager().getLaunchIntentForPackage(appPackageName);
            startActivity(intent);
        }catch(Exception e){
            Toast.makeText(this, "没有安装", Toast.LENGTH_LONG).show();
        }
    }
    /**
     * 根据报名判断app是否运行
     */
    private boolean isMyAppRunning(Context context, String packageName) {
        boolean result = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = am.getRunningAppProcesses();
        if (appProcesses != null) {
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : appProcesses) {
                if (runningAppProcessInfo.processName.contains(packageName)) {
                    int status = runningAppProcessInfo.importance;
                    if (status == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE || status == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    public static boolean isActivityRunning(Context mContext, String activityClassName) {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> info = activityManager.getRunningTasks(1);
        if (info != null && info.size() > 0) {
            ComponentName component = info.get(0).topActivity;
            if (activityClassName.equals(component.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断服务是否开启
     *
     * @return
     */
    public static boolean isServiceRunning(Context context, Class clazz) {
        if (clazz == null) {
            return false;
        }
        ActivityManager myManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().contains(clazz.getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断程序是否在后台运行
     *
     * @return true 表示在后台运行
     */
    public static boolean isRunBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = context.getApplicationContext().getPackageName();
        //获取Android设备中所有正在运行的App
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null)
            return true;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance ==
                    ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return false;
            }
        }
        return true;
    }

    /**
     * 通过包名和类名来开启活动
     */
    private void startTargetActivity(Context context, Class clazz) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(context, clazz));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
    }
}
