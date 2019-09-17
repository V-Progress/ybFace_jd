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

import java.util.List;

public class MyProtectService extends Service {
    private static final String TAG = "MyProtectService";

    //看门狗service
    private String packageName = "com.yunbiao.ybsmartcheckin_live_id";
    private String packageClassName = "com.yunbiao.ybsmartcheckin_live_id.SplashActivity";

    private final static int DELAY_TIME = 120 * 1000;//15s轮询一次
    private final static int CHECK_APP = 0x3211;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.sendEmptyMessage(CHECK_APP);
        Log.e(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == CHECK_APP) {
                if (!isMyAppRunning(MyProtectService.this, packageName)) {
                    startTargetActivity(packageName, packageClassName);
                    Log.e(TAG, "startTargetActivity");
                }
                mHandler.sendEmptyMessageDelayed(CHECK_APP, DELAY_TIME);
            }
        }
    };

    /**
     * 根据报名判断app是否运行
     */
    private boolean isMyAppRunning(Context context, String packageName) {
        boolean result = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = am.getRunningAppProcesses();
        if (appProcesses != null) {
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : appProcesses) {
                if (runningAppProcessInfo.processName.equals(packageName)) {
                    int status = runningAppProcessInfo.importance;
                    if (status == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE || status == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 通过包名和类名来开启活动
     */
    private void startTargetActivity(String packageName, String className) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, className));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
}
