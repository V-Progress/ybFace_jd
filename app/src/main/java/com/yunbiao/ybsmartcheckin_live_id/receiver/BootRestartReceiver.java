package com.yunbiao.ybsmartcheckin_live_id.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.SplashActivity;
import com.yunbiao.ybsmartcheckin_live_id.common.power.PowerOffTool;
import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.ThreadUitls;
import com.yunbiao.ybsmartcheckin_live_id.utils.logutils.LogUtils;


public class BootRestartReceiver extends BroadcastReceiver {
    private static final String TAG = "BootRestartReceiver";
    private String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive: " + action);
        if (action.equals(ACTION)) {
            //自动开关机
            ThreadUitls.runInThread(machineRestartRun);
            //开机重置开关机设置标志，A20定时关机会重走程序，定时开关机失效，然后加上这个标志
            LogUtils.i(TAG, "重启当前时间：" + CommonUtils.getStringDate());
            try {
                //开机恢复之前保存的声音的大小，中恒板子关机实际上是屏幕休眠，但是开机是休眠时间到先关机后开机，rom是这样的，
                int sound = SpUtils.getInt(SpUtils.CURR_VOLUME);
                if (sound > 0) {
                    AudioManager audioManager = (AudioManager) APP.getContext().getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, sound, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Intent i = new Intent(context, SplashActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    public Runnable machineRestartRun = new Runnable() {
        public void run() {
            PowerOffTool.getPowerOffTool().machineStart();
        }
    };
}
