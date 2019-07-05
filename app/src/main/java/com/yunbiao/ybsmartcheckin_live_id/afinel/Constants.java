package com.yunbiao.ybsmartcheckin_live_id.afinel;

import android.os.Environment;

public class Constants {
    public static final String API_KEY = "1234567890";
    //本地
//    public static final String XMPP_HOST = "192.168.1.54";
//    public static final String XMPP_PORT = "5222";
//    public static final String RESOURCE_HOST = "http://192.168.1.54";
//    public static final String RESOURCE_PORT = "8088";
//    public static String RESOURCE_URL = RESOURCE_HOST + ":" + RESOURCE_PORT + "/ybface/";

    //局域网（军区）// TODO: 2019/6/27 ComById
//    public static final String XMPP_HOST = "192.168.1.1";
//    public static final String XMPP_PORT = "5222";
//    public static final String RESOURCE_HOST = "http://192.168.1.1";
//    public static final String RESOURCE_PORT = "80";
//    public static String RESOURCE_URL = RESOURCE_HOST + ":" + RESOURCE_PORT + "/";

    //云
    public static final String XMPP_HOST = "47.105.80.245";
    public static final String XMPP_PORT = "5222";
    public static final String RESOURCE_HOST = "http://zz.yunbiaowulian.com";
    public static final String RESOURCE_PORT = "80";
    public static String RESOURCE_URL = RESOURCE_HOST + ":" + RESOURCE_PORT + "/";

    public static String LOCAL_ROOT_PATH = Environment.getExternalStorageDirectory().getPath() + "/yb_face_db/";
    public static String ADS_PATH = LOCAL_ROOT_PATH + "ads/";//广告路径
    public static String DATA_PATH = LOCAL_ROOT_PATH + "data/";//数据库路径
    public static String HEAD_PATH = LOCAL_ROOT_PATH + "photo/";//照片路径
    public static String CACHE_PATH = LOCAL_ROOT_PATH + "cache/";//缓存路径
    public static final String CURRENT_FACE_CACHE_PATH = CACHE_PATH + "face/";//实时人脸记录缓存
    public static final String MEETING_PATH = LOCAL_ROOT_PATH + "meeting/";
}


