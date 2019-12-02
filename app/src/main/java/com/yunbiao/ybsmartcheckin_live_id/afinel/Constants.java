package com.yunbiao.ybsmartcheckin_live_id.afinel;

import android.os.Environment;

import com.yunbiao.ybsmartcheckin_live_id.BuildConfig;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

public class Constants {
    public static final String API_KEY = "1234567890";
    //局域网（军区）// TODO: 2019/6/27 ComById
//    public static final String XMPP_HOST = "192.168.1.1";
//    public static final String XMPP_PORT = "5222";
//    public static final String RESOURCE_HOST = "http://192.168.1.1";
//    public static final String RESOURCE_PORT = "80";
//    public static String RESOURCE_URL = RESOURCE_HOST + ":" + RESOURCE_PORT + "/";

    //天津港
    public static final String XMPP_HOST = BuildConfig.HOST;
    public static final String XMPP_PORT = BuildConfig.XMPP_PORT;
    public static final String RESOURCE_HOST = NetConfig.PRE + BuildConfig.HOST;
    public static final String RESOURCE_PORT = BuildConfig.RESOURCE_PORT;
    //生成主地址
    public static String RESOURCE_URL = RESOURCE_HOST + NetConfig.COLON + RESOURCE_PORT + BuildConfig.SUFFIX;


    public static String LOCAL_ROOT_PATH = Environment.getExternalStorageDirectory().getPath() + "/yb_face_db/";
    public static String DATABASE_PATH = LOCAL_ROOT_PATH + "database/";//数据库路径
    public static String CACHE_PATH = LOCAL_ROOT_PATH + "cache/";//缓存路径

    public static String TEMP_PATH = CACHE_PATH + "temp/";//临时路径（未初始化公司时创建）
    public static String DATA_PATH = TEMP_PATH + "data/";//公司数据
    public static String INFO_PATH = TEMP_PATH + "info/";//公司简介
    public static String ADS_PATH = TEMP_PATH + "ads/";//广告路径
    public static String HEAD_PATH = TEMP_PATH + "photo/";//照片路径
    public static String RECORD_PATH = TEMP_PATH + "record/";//实时人脸记录缓存
    public static String MEETING_PATH = TEMP_PATH + "meeting/";
    public static String SKIN_PATH = LOCAL_ROOT_PATH + "skins/";//皮肤路径

    public static void initStorage() {
        int comid = SpUtils.getInt(SpUtils.COMPANYID);
        Constants.DATA_PATH = Constants.CACHE_PATH + comid + "/data/";
        Constants.ADS_PATH = Constants.CACHE_PATH + comid + "/ads/";
        Constants.HEAD_PATH = Constants.CACHE_PATH + comid + "/img/";
        Constants.RECORD_PATH = Constants.CACHE_PATH + comid + "/rcd/";
        Constants.MEETING_PATH = Constants.CACHE_PATH + comid + "/meet/";
        Constants.INFO_PATH = Constants.CACHE_PATH + comid + "/info/";
    }

    public interface NetConfig {
        String PRE = "http://";
        String COLON = ":";

        /***
         * 正式环境
         */
        String PRO_URL = "47.105.80.245";//主机地址
        String PRO_XMPP_PORT = "5222";//XMPP端口
        String PRO_RES_PORT = "8080";//数据端口
        String PRO_SUFFIX = "/";//项目名（端口为80，项目名不用写）

        /***
         * 曾辉环境
         */
//        String DEV_URL = "192.168.1.45";
//        String DEV_RES_PORT = "8082";
//        String DEV_XMPP_PORT = "5222";
//        String DEV_SUFFIX = "/";

        /***
         * 正式环境
         */
        String DEV_URL = "47.105.80.245";
        String DEV_RES_PORT = "8080";
        String DEV_XMPP_PORT = "5222";
        String DEV_SUFFIX = "/";

        /***
         * 张继桃环境
          */
//        String DEV_URL = "192.168.1.54";
//        String DEV_RES_PORT = "8088";
//        String DEV_XMPP_PORT = "5222";
//        String DEV_SUFFIX = "/ybface/";

        /***
         * 测试环境
         */
//        String DEV_URL = "121.69.8.238";
//        String DEV_XMPP_PORT = "5222";
//        String DEV_RES_PORT = "8088";
//        String DEV_SUFFIX = "/ybface/";
    }
}


