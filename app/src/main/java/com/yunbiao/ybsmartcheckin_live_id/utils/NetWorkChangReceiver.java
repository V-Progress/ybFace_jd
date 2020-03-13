package com.yunbiao.ybsmartcheckin_live_id.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.yunbiao.ybsmartcheckin_live_id.APP;

public class NetWorkChangReceiver extends BroadcastReceiver {

    private NetWorkChangeListener netWorkChangeListener;

    public NetWorkChangReceiver(NetWorkChangeListener netWorkChangeListener) {
        this.netWorkChangeListener = netWorkChangeListener;
    }

    public static interface NetWorkChangeListener {
        void connect();

        void disConnect();
    }

    /**
     * 获取连接类型
     *
     * @param type
     * @return
     */
    private String getConnectionType(int type) {
        String connType = "";
        if (type == ConnectivityManager.TYPE_MOBILE) {
            connType = "3G网络数据";
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            connType = "WIFI网络";
        }
        return connType;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)
                || "android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {

            switch (NetUtil.isNetworkAvailable()) {
                case NetUtil.NET_ETHERNET:
                    System.out.println("-----------networktest---------有线");
                    netWorkChangeListener.connect();
                case NetUtil.NET_WIFI:
                    System.out.println("-----------networktest---------无线");
                    netWorkChangeListener.connect();
                    break;
                case NetUtil.NET_MOBILE:
                    System.out.println("-----------networktest---------GPRS");
                    netWorkChangeListener.connect();
                    break;
                case NetUtil.NET_NOCONNECT:
                    System.out.println("-----------networktest---------无网络");
                    netWorkChangeListener.disConnect();
                    break;
                default:
                    break;
            }
        }
    }

    public static class NetUtil {
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
        public static boolean isNetworkConnected() {
            return isNetworkAvailable() != NET_NOCONNECT;
        }
    }
}