package com.yunbiao.ybsmartcheckin_live_id.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.yunbiao.ybsmartcheckin_live_id.APP;

public class NetUtil {
    public static final int NET_ETHERNET = 1;
    public static final int NET_WIFI = 2;
    public static final int NET_MOBILE = 3;
    public static final int NET_NOCONNECT = 0;


    public static int isNetworkAvailable() {
        ConnectivityManager connectMgr = (ConnectivityManager) APP.getContext().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ethNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);


        if (ethNetInfo != null && ethNetInfo.isConnected()) {
            return NET_ETHERNET;
        } else if (wifiNetInfo != null && wifiNetInfo.isConnected()) {
            return NET_WIFI;
        } else if (mobileNetInfo != null && mobileNetInfo.isConnected()) {
            return NET_MOBILE;
        } else {
            return NET_NOCONNECT;
        }
    }


    /**
     * 检测网络是否可用
     *
     * @return
     */
    public static boolean isNetworkConnected( ) {
        return isNetworkAvailable() != NET_NOCONNECT;
    }
}