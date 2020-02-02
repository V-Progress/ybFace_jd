package com.yunbiao.ybsmartcheckin_live_id.business;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class AlarmManagerUtil {

    private static DateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 获取AlarmManager实例
    public static AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    private static final String TAG = "AlarmManagerUtil";
    public static void setAlarm(Company company) {
        String currDay = dayFormat.format(new Date());

        String gotime = company.getGotime();
        String downtime = company.getDowntime();

        if (gotime.length() <= 5) {
            gotime += ":00";
        }
        gotime = currDay + " " + gotime;

        if (downtime.length() <= 5) {
            downtime += ":00";
        }
        downtime = currDay + " " + downtime;

        Log.e(TAG, "setAlarm: 闹钟开始时间：" + gotime);
        Log.e(TAG, "setAlarm: 闹钟结束时间：" + downtime);

        try {
            Date goDate = timeFormat.parse(gotime);
            Date downDate = timeFormat.parse(downtime);
            Log.e(TAG, "setAlarm: " + goDate.getTime());
            Log.e(TAG, "setAlarm: " + downDate.getTime());


            Date date = new Date();
            if(date.before(goDate)){
                sendAlarmBroadcast(APP.getContext(), 111, AlarmManager.RTC_WAKEUP, goDate.getTime(), AlarmBroadcast.class);
            } else {
                Log.e(TAG, "setAlarm: 已超时：" + gotime );
            }

            if(date.before(downDate)){
                sendAlarmBroadcast(APP.getContext(), 112, AlarmManager.RTC_WAKEUP, downDate.getTime(), AlarmBroadcast.class);
            } else {
                Log.e(TAG, "setAlarm: 已超时：" + downtime);
            }
            Log.e(TAG, "setAlarm: 设置闹钟成功");
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(TAG, "setAlarm: 设置闹钟失败：" + e.getMessage());
        }
    }

    private static String ACTION_SET_ALARM = "alarm_action";
    // 发送定时广播（执行广播中的定时任务）
    // 参数：
    // context:上下文
    // requestCode:请求码，用于区分不同的任务
    // type:alarm启动类型
    // triggerAtTime:定时任务开启的时间，毫秒为单位
    // cls:广播接收器的class
    public static void sendAlarmBroadcast(Context context, int requestCode,
                                          int type, long triggerAtTime, Class cls) {
        AlarmManager mgr = getAlarmManager(context);

        Intent intent = new Intent(context, cls);
        intent.setAction(ACTION_SET_ALARM);
        intent.putExtra("time","" + triggerAtTime);
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode,
                intent, 0);

        mgr.set(type, triggerAtTime, pi);
    }

    // 取消指定requestCode的定时任务
    // 参数：
    // context:上下文
    // requestCode:请求码，用于区分不同的任务
    // cls:广播接收器的class
    public static void cancelAlarmBroadcast(Context context, int requestCode,
                                            Class cls) {
        AlarmManager mgr = getAlarmManager(context);

        Intent intent = new Intent(context, cls);
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode,
                intent, 0);
        mgr.cancel(pi);
        Toast.makeText(context, "取消定时服务成功" + " @requestCode:" + requestCode, Toast.LENGTH_SHORT).show();
        Log.d("取消定时服务成功", "@requestCode:" + requestCode);
    }

    // 周期性执行定时任务
    // 参数：
    // context:上下文
    // requestCode:请求码，用于区分不同的任务
    // type:alarm启动类型
    // startTime:开始的时间，毫秒为单位
    // cycleTime:定时任务的重复周期，毫秒为单位
    // cls:广播接收器的class
    public static void sendRepeatAlarmBroadcast(Context context,
                                                int requestCode, int type, long startTime, long cycleTime, Class cls) {
        AlarmManager mgr = getAlarmManager(context);

        Intent intent = new Intent(context, cls);
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode,
                intent, 0);

        mgr.setRepeating(type, startTime, cycleTime, pi);
    }

    public static class AlarmBroadcast extends BroadcastReceiver {
        public AlarmBroadcast() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(TextUtils.equals(ACTION_SET_ALARM,action)){
                String time = intent.getStringExtra("time");

                Log.e(TAG, "onReceive: 闹钟开始啦：" + time);
                playRingn();
            }

        }
    }

    private static Timer timer = new Timer();
    private static void playRingn(){
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        final Ringtone ringtone = RingtoneManager.getRingtone(APP.getContext(), notification);
        setRingtoneRepeat(ringtone);
        ringtone.play();

        if(timer != null){
            timer.cancel();
            timer = null;
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ringtone.stop();
            }
        },30 * 1000);
    }

    //反射设置闹铃重复播放
    private static void setRingtoneRepeat(Ringtone ringtone) {
        Class<Ringtone> clazz =Ringtone.class;
        try {
            Field field = clazz.getDeclaredField("mLocalPlayer");//返回一个 Field 对象，它反映此 Class 对象所表示的类或接口的指定公共成员字段（※这里要进源码查看属性字段）
            field.setAccessible(true);
            MediaPlayer target = (MediaPlayer) field.get(ringtone);//返回指定对象上此 Field 表示的字段的值
            target.setLooping(true);//设置循环
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}