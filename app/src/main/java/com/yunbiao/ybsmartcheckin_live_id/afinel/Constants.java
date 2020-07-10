package com.yunbiao.ybsmartcheckin_live_id.afinel;

import android.hardware.Camera;
import android.os.Environment;
import android.text.TextUtils;

import com.yunbiao.ybsmartcheckin_live_id.BuildConfig;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import timber.log.Timber;

public class Constants {
    public static final String API_KEY = "1234567890";

    private static String PRE = "http://";
    private static String COLON = ":";
    //地址
    public static String XMPP_HOST = NetConfig.COMMUNICATION_HOST;
    public static String XMPP_PORT = NetConfig.COMMUNICATION_PORT;
    public static String RESOURCE_HOST = NetConfig.SERVICE_HOST;
    public static String RESOURCE_PORT = NetConfig.SERVICE_PORT;
    public static String RESOURCE_SUFFIX = NetConfig.SERVICE_NAME;
    public static String RESOURCE_URL = PRE + RESOURCE_HOST + COLON + RESOURCE_PORT + RESOURCE_SUFFIX;

    //资源路径
    public static String LOCAL_ROOT_PATH = Environment.getExternalStorageDirectory().getPath() + "/yb_face_db/";
    public static String _T = LOCAL_ROOT_PATH + "/_T/";
    public static String SPLASH_DIR_PATH = LOCAL_ROOT_PATH + "/splash/";
    public static String LOGO_DIR_PATH = LOCAL_ROOT_PATH + "/logo/";
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
        if (SpUtils.getIntOrDef(Constants.Key.SERVER_MODEL, Constants.Default.SERVER_MODEL) == serverModel.JU) {
            String serviceIp = SpUtils.getStr(Key.JU_SERVICE_IP_CACHE);
            String xmppIp = SpUtils.getStr(Key.JU_XMPP_IP_CACHE);

            Constants.RESOURCE_HOST = TextUtils.isEmpty(serviceIp) ? xmppIp : serviceIp;
            Constants.RESOURCE_PORT = SpUtils.getStr(Constants.Key.JU_RESOURCE_PORT_CACHE);
            String projectName = SpUtils.getStr(Constants.Key.JU_PROJECT_NAME_SUFFIX);

            Constants.XMPP_HOST = xmppIp;
            Constants.XMPP_PORT = SpUtils.getStr(Constants.Key.JU_XMPP_PORT_CACHE);

            Constants.RESOURCE_URL = PRE + Constants.RESOURCE_HOST + COLON + Constants.RESOURCE_PORT + "/" + (TextUtils.isEmpty(projectName)||TextUtils.equals("/",projectName) ? "" : (projectName + "/"));
            ResourceUpdate.refreshAddress();
        }
        logNetConfig(TAG);
    }

    public static void logNetConfig(String TAG){
        Timber.e("服务地址：%s", Constants.RESOURCE_HOST);
        Timber.e("服务端口：%s", Constants.RESOURCE_PORT);
        Timber.e("通信地址：%s", Constants.XMPP_HOST);
        Timber.e("通信端口：%s", Constants.XMPP_PORT);
        Timber.e("服务地址：%s", Constants.RESOURCE_URL);
    }

    //修改设备类型
    public static final int DEVICE_TYPE = BuildConfig.APP_TYPE;
    //打包版本
    public static final int FLAVOR_TYPE = BuildConfig.FLAVOR_TYPE;
    //最大人脸抓取数
    public static final int MAX_DETECT_NUM = BuildConfig.MAX_DETECT_FACE_NUM;
    //最远人脸抓取距离
    public static final int DETECT_FACE_SCALE_VAL = BuildConfig.DETECT_FACE_SCALE_VAL;
    //默认屏保
    public static int DEFAULT_SCREE_BG = R.mipmap.splash;
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

        String SERVICE_HOST = "47.105.80.245";
        String SERVICE_PORT = "8080";
        String SERVICE_NAME = "/";
        String COMMUNICATION_HOST = "47.105.80.245";
        String COMMUNICATION_PORT = "5222";
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

//        int TEMPERATURE_CHECK_IN_SMT = 8;//测温通行机（视美泰考勤版）
//        int HT_TEMPERATURE_CHECK_IN_SMT = 12;//亨通通行

        int TEMPERATURE_CERTIFICATES = 9;//人证测温机
        int HT_TEMPERATURE_CERTIFICATES = 13;//亨通人证

        int MULTIPLE_THERMAL = 10;//多人测温
        int HT_MULTIPLE_THERMAL = 14;//亨通多人

        int TEMPER_SAFETY_CHECK = 15;//安检门
        int HT_TEMPER_SAFETY_CHECK = 16;//亨通安检

        int SAFETY_CHECK_DOUBLE_LIGHT = 17;//双光安检
        int HT_SAFETY_CHECK_DOUBLE_LIGHT = 18;//亨通双光安检

        int TEMPERATURE_MEASUREMENT_5_INCH = 21;//5寸

        int TEMPERATURE_CHECK_IN_215_INCH = 22;//21.5寸
    }

    //默认摄像头ID
    public static int CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_BACK;

    public interface Key{
        String PRIVACY_MODE = "privacyMode";//隐私模式
        String CLEAR_POLICY = "clearPolicy";//清理策略
        String CLEAR_POLICY_CUSTOM = "clearPolicyCustom";//自定义清理策略
        String SIMILAR_THRESHOLD = "similarThreshold";//相似度阈值

        String FACE_DIALOG = "faceDialog";//人脸弹窗
        String LIVENESS_ENABLED = "livenessEnabled";//活体开关
        String POSTER_ENABLED = "posterEnabled";//大屏海报是否可用
        String READ_CARD_ENABLED = "readCardEnabled";//读卡模块
        String QRCODE_ENABLED = "qrCodeEnabled";//二维码开关

        String DOOR_STATE = "doorState";//门禁常开模式
        String GPIO_DELAY = "doorDelay";//继电器延时
        String IS_H_MIRROR = "isMirror";//是否镜像
        String IS_V_MIRROR = "isVerticalMirror";//纵向镜像
        String PICTURE_ROTATION = "pictureRotation";//照片方向
        String CAMERA_ANGLE = "cameraAngle";//摄像头角度
        String CAMERA_SIZE = "cameraSize";

        String SERVER_MODEL = "serverModel";//服务模式
        String JU_XMPP_IP_CACHE = "juIpCache";//通信地址
        String JU_SERVICE_IP_CACHE = "juServiceIpCache";//服务地址
        String JU_RESOURCE_PORT_CACHE = "juResourcePortCache";//端口
        String JU_XMPP_PORT_CACHE = "juXmppPortCache";//XMPP端口
        String JU_PROJECT_NAME_SUFFIX = "juRrojectNameSuffix";//后缀

        String JUMP_TAG = "jumpTag";//跳转标签

        String MAIN_SIGN_LIST = "mainSignList";

        String PASSWORD_ENABLED = "passwordEnabled";
    }

    public static class Default{
        public static final boolean PRIVACY_MODE = false;
        public static final int CLEAR_POLICY = 3;
        public static final int CLEAR_POLICY_CUSTOM = 30;
        public static final int SIMILAR_THRESHOLD = 80;
        public static boolean QRCODE_ENABLED = true;
        public static final boolean READ_CARD_ENABLED = false;
        public static boolean POSTER_ENABLED = false;
        public static boolean LIVENESS_ENABLED = false;
        public static boolean SHOW_LIVENESS_SETTING = true;
        public static final boolean FACE_DIALOG = false;
        public static boolean IS_H_MIRROR = true;
        public static final boolean IS_V_MIRROR = false;
        //屏幕角度默认值
        public static int CAMERA_ANGLE = 0;
        //截图方向
        public static int PICTURE_ROTATION = -1;
        public static int GPIO_DELAY = 5;

        public static int SERVER_MODEL = serverModel.YUN;

        public static final boolean JUMP_TAG = false;

        public static boolean MAIN_SIGN_LIST = true;

        public static boolean PASSWORD_ENABLED = true;
    }
}