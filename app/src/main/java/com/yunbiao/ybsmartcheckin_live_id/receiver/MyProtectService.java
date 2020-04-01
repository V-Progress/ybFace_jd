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

import com.yunbiao.ybsmartcheckin_live_id.SplashActivity;

import java.util.ArrayList;
import java.util.List;

public class MyProtectService extends Service {
    private static final String TAG = "MyProtectService";

    //看门狗service
    private String packageName = "com.yunbiao.ybsmartcheckin_live_id";

    private final static int DELAY_TIME = 120 * 1000;//15s轮询一次
    private final static int CHECK_APP = 0x3211;
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
            if(runningService.get(i).service.getClassName().contains(clazz.getSimpleName())){
                return true;
            }
        }
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: 开启守护进程");
        mHandler.sendEmptyMessage(CHECK_APP);
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
                Log.e(TAG, "handleMessage: 轮询");
                if (!isMyAppRunning(MyProtectService.this, packageName)) {
                    startTargetActivity(MyProtectService.this, SplashActivity.class);
                    Log.e(TAG, "进程被结束，准备拉起");
                }
                mHandler.sendEmptyMessageDelayed(CHECK_APP, DELAY_TIME);
            }
            return false;
        }
    });

    /**
     * 根据报名判断app是否运行
     */
    private boolean isMyAppRunning(Context context, String packageName) {
        boolean result = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = am.getRunningAppProcesses();
        if (appProcesses != null) {
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : appProcesses) {
                if(runningAppProcessInfo.processName.contains(packageName)){
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
