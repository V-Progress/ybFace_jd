package com.yunbiao.ybsmartcheckin_live_id.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;


import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.xmpp.Constants;

import org.xutils.common.Callback;
import org.xutils.x;

import java.util.Map;

/**
 * Created by LiuShao on 2016/4/6.
 */
public class NetUtils {

    /**
     * 网络状态
     */
    public static boolean hasNetwork() {
        ConnectivityManager cManager = (ConnectivityManager) APP.getContext().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取xmpp连接情况
     *
     * @return
     */
    public static String getXmppConnected() {
        String connectStatus;
        if (Constants.xmppManager != null && Constants.xmppManager.isConnected()) {
            connectStatus = "在线";
        } else {
            connectStatus = "离线";
        }
        return connectStatus;
    }


    /**
     * 获取连接的wifi名称
     *
     * @return
     */
    public static String getConnectWifiSsid() {
        WifiManager wifiManager = (WifiManager) APP.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    }

    /**
     * 获取本机的ip地址
     */
    public static String getIpAdress() {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) APP.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        return ip;
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }




    /**
     * post请求
     * @param url      要访问的url
     * @param map      请求带的参数
     * @param callback 回调
     * @param <T>
     * @return
     */
    public static <T> Callback.Cancelable Post(String url, Map<String, Object> map, Callback.CommonCallback<T> callback) {
        org.xutils.http.RequestParams params = new org.xutils.http.RequestParams(url);
        if (null != map) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                params.addParameter(entry.getKey(), entry.getValue());
            }
        }
        Callback.Cancelable cancelable = x.http().post(params, callback);
        return cancelable;
    }

    /**
     * 下载文件
     *
     * @param <T>
     */
    public static <T> Callback.Cancelable DownLoadFile(String url, String filepath, Callback.CommonCallback<T> callback) {
        org.xutils.http.RequestParams params = new org.xutils.http.RequestParams(url);
        //设置断点续传
        params.setAutoResume(true);
        params.setSaveFilePath(filepath);
        Callback.Cancelable cancelable = x.http().get(params, callback);
        return cancelable;
    }
}
