package com.yunbiao.ybsmartcheckin_live_id.xmpp;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import timber.log.Timber;

public class XmppLauncher {

    public static void start(Context context){
        if(context == null){
            return;
        }
        boolean b = context.bindService(new Intent(context, NotificationService.class), serviceConnection, Service.BIND_AUTO_CREATE);
        Timber.d("绑定服务：" + b);
    }

    public static void stop(Context context){
        if(context == null){
            return;
        }
        context.unbindService(serviceConnection);
    }

    private static ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}
