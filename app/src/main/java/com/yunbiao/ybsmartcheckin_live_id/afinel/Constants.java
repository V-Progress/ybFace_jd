package com.yunbiao.ybsmartcheckin_live_id.afinel;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

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
    public static String XMPP_HOST = BuildConfig.HOST;
    public static String XMPP_PORT = BuildConfig.XMPP_PORT;
    public static String RESOURCE_HOST = BuildConfig.HOST;
    public static String RESOURCE_PORT = BuildConfig.RESOURCE_PORT;
    public static String RESOURCE_SUFFIX = BuildConfig.SUFFIX;
    //生成主地址
    public static String RESOURCE_URL = NetConfig.PRE + RESOURCE_HOST + NetConfig.COLON + RESOURCE_PORT + RESOURCE_SUFFIX;


    public static String LOCAL_ROOT_PATH = Environment.getExternalStorageDirectory().getPath() + "/yb_face_db/";
    public static String DATABASE_PATH = LOCAL_ROOT_PATH + "database/";//数据库路径
    public static String CACHE_PATH = LOCAL_ROOT_PATH + "cache/";//缓存路径

    public static String TEMP_PATH = CACHE_PATH + "temp/";//临时路径（未初始化公司时创建）
    public static String DATA_PATH = TEMP_PATH + "data/";//公司数据
    public static String INFO_PATH = TEMP_PATH + "info/";//公司简介
    public static String ADS_PATH = TEMP_PATH + "ads/";//广告路径
    public static String HEAD_PATH = TEMP_PATH + "photo/";//照片路径
    public static String FEATURE_PATH = TEMP_PATH + "features/";//特征库地址
    public static String RECORD_PATH = TEMP_PATH + "record/";//实时人脸记录缓存
    public static String MEETING_PATH = TEMP_PATH + "meeting/";
    public static String SKIN_PATH = LOCAL_ROOT_PATH + "skins/";//皮肤路径

    public static void initStorage() {
        int comid = SpUtils.getInt(SpUtils.COMPANYID);
        Constants.DATA_PATH = Constants.CACHE_PATH + comid + "/data/";
        Constants.ADS_PATH = Constants.CACHE_PATH + comid + "/ads/";
        Constants.HEAD_PATH = Constants.CACHE_PATH + comid + "/img/";
        Constants.FEATURE_PATH = Constants.CACHE_PATH + comid + "/features/";
        Constants.RECORD_PATH = Constants.CACHE_PATH + comid + "/rcd/";
        Constants.MEETING_PATH = Constants.CACHE_PATH + comid + "/meet/";
        Constants.INFO_PATH = Constants.CACHE_PATH + comid + "/info/";
    }

    private static final String TAG = "Constants";

    public static void checkSetIp() {
        String ip = SpUtils.getStr(SpUtils.IP_CACHE);
        String resourcePort = SpUtils.getStr(SpUtils.RESOURCE_PORT_CACHE);
        String xmppPort = SpUtils.getStr(SpUtils.XMPP_PORT_CACHE);
        String suffix = SpUtils.getStr(SpUtils.PROJECT_NAME_SUFFIX);
        if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(resourcePort) || TextUtils.isEmpty(xmppPort) || TextUtils.isEmpty(suffix)) {
            Log.e(TAG, "IP地址：" + Constants.XMPP_HOST);
            Log.e(TAG, "资源端口：" + Constants.RESOURCE_PORT);
            Log.e(TAG, "xmpp端口：" + Constants.XMPP_PORT);
            Log.e(TAG, "项目名：" + Constants.RESOURCE_SUFFIX);
            Log.e(TAG, "资源地址：" + Constants.RESOURCE_URL);
        } else {
            Constants.XMPP_HOST = ip;
            Constants.XMPP_PORT = xmppPort;
            Constants.RESOURCE_HOST = ip;
            Constants.RESOURCE_PORT = resourcePort;
            Constants.RESOURCE_URL = Constants.NetConfig.PRE + Constants.RESOURCE_HOST + Constants.NetConfig.COLON + Constants.RESOURCE_PORT + suffix;

            Log.e(TAG, "资源IP：" + Constants.RESOURCE_HOST);
            Log.e(TAG, "资源端口：" + Constants.RESOURCE_PORT);
            Log.e(TAG, "xmppIP：" + Constants.XMPP_HOST);
            Log.e(TAG, "xmpp端口：" + Constants.XMPP_PORT);
            Log.e(TAG, "资源地址：" + Constants.RESOURCE_URL);
        }
    }

    public interface NetConfig {
        String PRE = "http://";
        String COLON = ":";

        /***
         * 正式环境
         */
        String PRO_URL = "47.105.80.245";//主机地址
        String PRO_RES_PORT = "8080";//数据端口
        String PRO_XMPP_PORT = "5222";//XMPP端口
        String PRO_SUFFIX = "/";//项目名（端口为80，项目名不用写）

        /***
         * 正式环境
         */
        String DEV_URL = "47.105.80.245";
        String DEV_RES_PORT = "8080";
        String DEV_XMPP_PORT = "5222";
        String DEV_SUFFIX = "/";
    }

    public static final int DEVICE_TYPE = DeviceType.TEMPERATURE_CHECK_IN;

    public interface DeviceType {
        int CHECK_IN = 1;//考勤机
        int TEMPERATURE_CHECK_IN = 7;//测温考勤机
    }

    public interface Model {
        int MODEL_FACE_ONLY = 0;
        int MODEL_FACE_TEMPERATURE = 1;
        int MODEL_TEMPERATURE_ONLY = 2;
        int MODEL_THERMAL_IMAGING_ONLY = 3;
        int MODEL_FACE_THERMAL_IMAGING = 4;
        String[] models = {"人脸识别", "人脸 + 红外", "红外测温", "热成像测温", "人脸 + 热成像"};
    }

    public interface serverModel {
        int YUN = 0;//云端
        int JU = 1;//局域网
    }

    //屏幕类型
    public final static int SCREEN_TYPE = ScreenType.TYPE_PORTRAIT_8_800_1280;

    //设置默认模式（如果是8寸机则返回热成像模式，如果是其他则红外模式）
    public static final int DEFAULT_TEMP_MODEL = SCREEN_TYPE == ScreenType.TYPE_PORTRAIT_8_800_1280 ? Model.MODEL_THERMAL_IMAGING_ONLY : Model.MODEL_TEMPERATURE_ONLY;

    public static final int DEFAULT_CAMERA_ANGLE = SCREEN_TYPE == ScreenType.TYPE_PORTRAIT_8_800_1280 ? 270 : 0;

    public interface ScreenType {
        int TYPE_PORTRAIT_42_1920_1080 = 0;//竖版42寸
        int TYPE_LANDSCAPE_21_10_1280_800_or_1920_1080 = 1;//横板21寸和10寸
        int TYPE_PORTRAIT_8_800_1280 = 2;//竖屏闸机
    }
}


