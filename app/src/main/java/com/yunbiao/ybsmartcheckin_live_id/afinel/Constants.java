package com.yunbiao.ybsmartcheckin_live_id.afinel;

import android.hardware.Camera;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.yunbiao.ybsmartcheckin_live_id.BuildConfig;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

public class Constants {
    public static final String API_KEY = "1234567890";

    private static String PRE = "http://";
    private static String COLON = ":";
    //地址
    public static String XMPP_HOST = NetConfig.PRO_URL;
    public static String XMPP_PORT = NetConfig.PRO_XMPP_PORT;
    public static String RESOURCE_HOST = NetConfig.PRO_URL;
    public static String RESOURCE_PORT = NetConfig.PRO_RES_PORT;
    public static String RESOURCE_SUFFIX = NetConfig.PRO_SUFFIX;
    //生成主地址
    public static String RESOURCE_URL = PRE + RESOURCE_HOST + COLON + RESOURCE_PORT + RESOURCE_SUFFIX;

    //资源路径
    public static String LOCAL_ROOT_PATH = Environment.getExternalStorageDirectory().getPath() + "/yb_face_db/";
    public static String APK_PATH = LOCAL_ROOT_PATH + "apk/";
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
        if (SpUtils.getIntOrDef(SpUtils.SERVER_MODEL, serverModel.YUN) == serverModel.JU) {
            Constants.XMPP_HOST = SpUtils.getStr(SpUtils.JU_IP_CACHE);
            Constants.XMPP_PORT = SpUtils.getStr(SpUtils.JU_XMPP_PORT_CACHE);
            Constants.RESOURCE_HOST = SpUtils.getStr(SpUtils.JU_IP_CACHE);
            Constants.RESOURCE_PORT = SpUtils.getStr(SpUtils.JU_RESOURCE_PORT_CACHE);
            String projectName = SpUtils.getStr(SpUtils.JU_PROJECT_NAME_SUFFIX);
            Constants.RESOURCE_URL = PRE + Constants.RESOURCE_HOST + COLON + Constants.RESOURCE_PORT + "/" + (TextUtils.isEmpty(projectName)||TextUtils.equals("/",projectName) ? "" : (projectName + "/"));
            ResourceUpdate.refreshAddress();
        }
        logNetConfig(TAG);
    }

    public static void logNetConfig(String TAG){
        Log.e(TAG, "checkSetIp: IP地址：" + Constants.XMPP_HOST);
        Log.e(TAG, "checkSetIp: 通信端口：" + Constants.XMPP_PORT);
        Log.e(TAG, "checkSetIp: 服务地址：" + Constants.RESOURCE_HOST);
        Log.e(TAG, "checkSetIp: 服务端口：" + Constants.RESOURCE_PORT);
        Log.e(TAG, "checkSetIp: 服务地址：" + Constants.RESOURCE_URL);
    }

    //修改设备类型
    public static final int DEVICE_TYPE = BuildConfig.APP_TYPE;
    //是否亨通的版本（独立版本）
    public static final boolean isHT = BuildConfig.IS_HT;
    //是否思库的版本(云标分支版本)
    public static final boolean isSK = BuildConfig.IS_SK;
    //是否是Osmile的版本
    public static final boolean isOsimle = BuildConfig.IS_OSIMLE;
    //是否SoftWorkz的版本
    public static final boolean isSoftWorkz = BuildConfig.IS_SOFT_WORK_Z;
    //最大人脸抓取数
    public static final int MAX_DETECT_NUM = BuildConfig.MAX_DETECT_FACE_NUM;
    //最远人脸抓取距离
    public static final int DETECT_FACE_SCALE_VAL = BuildConfig.DETECT_FACE_SCALE_VAL;
    //设备未绑定时的公司Id
    public static final int NOT_BIND_COMPANY_ID = 0;
    //双光头横向大小偏移量
    public static int HORIZONTAL_OFFSET = 4;

    /***
     * 网络配置
     */
    public interface NetConfig {
        String PRO_URL = "47.105.80.245";//主机地址
        String PRO_RES_PORT = "8080";//数据端口
        String PRO_XMPP_PORT = "5222";//XMPP端口
        String PRO_SUFFIX = "/";//项目名（端口为80，项目名不用写）
    }

    /***
     * 服务模式
     */
    public interface serverModel {
        int YUN = 0;//云端
        int JU = 1;//局域网
    }

    /***
     * 设备类型
     */
    public interface DeviceType {
        int CHECK_IN = 1;//考勤机
        int TEMPERATURE_CHECK_IN = 7;//测温考勤机
        int HT_TEMPERATURE_CHECK_IN = 11;//亨通考勤

        int TEMPERATURE_CHECK_IN_SMT = 8;//测温通行机（视美泰考勤版）
        int HT_TEMPERATURE_CHECK_IN_SMT = 12;//亨通通行

        int TEMPERATURE_CERTIFICATES = 9;//人证测温机
        int HT_TEMPERATURE_CERTIFICATES = 13;//亨通人证

        int MULTIPLE_THERMAL = 10;//多人测温
        int HT_MULTIPLE_THERMAL = 14;//亨通多人

        int TEMPER_SAFETY_CHECK = 15;//安检门
        int HT_TEMPER_SAFETY_CHECK = 16;//亨通安检

        int SAFETY_CHECK_DOUBLE_LIGHT = 17;//双光安检
        int HT_SAFETY_CHECK_DOUBLE_LIGHT = 18;//亨通双光安检
    }

    //人证、考勤跳转标识
    public static final String JUMP_TAG = "jumpTag";
    //人证、考勤跳转默认值
    public static final boolean DEFAULT_JUMP_TAG = false;
    //默认人脸镜像
    public static boolean DEFAULT_FACE_MIRROR = true;
    //默认人脸竖镜像
    public static boolean isVerticalMirror = false;
    //默认摄像头ID
    public static int CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_BACK;
    //屏幕角度默认值
    public static int DEFAULT_CAMERA_ANGLE = 0;
    //大屏海报开启状态
    public static final boolean DEFAULT_POSTER_ENABLED = false;
    //默认二维码
    public static final boolean DEFAULT_QRCODE_ENABLED = true;
    //默认读卡器
    public static final boolean DEFAULT_READ_CARD_ENABLED = false;

    public interface Key{
        String PRIVACY_MODE = "privacyMode";
        String CLEAR_POLICY = "clearPolicy";
    }

    public static class Default{
        public static final boolean PRIVACY_MODE = false;
        public static final int CLEAR_POLICY = 2;
    }
}


