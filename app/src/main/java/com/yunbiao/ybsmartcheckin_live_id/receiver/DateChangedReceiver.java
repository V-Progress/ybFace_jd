package com.yunbiao.ybsmartcheckin_live_id.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.yunbiao.ybsmartcheckin_live_id.APP;

public class DateChangedReceiver extends BroadcastReceiver {
    private static final String TAG = "DateChangedReceiver";
    public final static String ACTION = "android.intent.action.DATE_CHANGED";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive: 日期发生改变");
        if (TextUtils.equals(ACTION,intent.getAction())) {
            APP.restart();
        }
    }
}
