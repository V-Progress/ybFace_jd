package com.yunbiao.ybsmartcheckin_live_id.afinel;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.yunbiao.ybsmartcheckin_live_id.BuildConfig;
import com.yunbiao.ybsmartcheckin_live_id.R;
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

    public static void checkSetIp() {
        if (SpUtils.getIntOrDef(SpUtils.SERVER_MODEL, serverModel.YUN) == serverModel.JU) {
            String ip = SpUtils.getStr(SpUtils.JU_IP_CACHE);
            String xmppPort = SpUtils.getStr(SpUtils.JU_XMPP_PORT_CACHE);
            String resPort = SpUtils.getStr(SpUtils.JU_RESOURCE_PORT_CACHE);
            String projectName = SpUtils.getStr(SpUtils.JU_PROJECT_NAME_SUFFIX);

            Constants.XMPP_HOST = ip;
            Constants.XMPP_PORT = xmppPort;
            Constants.RESOURCE_HOST = ip;
            Constants.RESOURCE_PORT = resPort;
            Constants.RESOURCE_URL = PRE + ip + COLON + resPort + "/" + (TextUtils.isEmpty(projectName) ? "" : (projectName  + "/"));

            ResourceUpdate.WEB_BASE_URL = Constants.RESOURCE_URL;
        }
    }

    public interface NetConfig {
        String PRO_URL = "47.105.80.245";//主机地址
        String PRO_RES_PORT = "8080";//数据端口
        String PRO_XMPP_PORT = "5222";//XMPP端口
        String PRO_SUFFIX = "/";//项目名（端口为80，项目名不用写）
    }

    public static class Model {
        public static int MODEL_FACE_ONLY = 0;
        public static int MODEL_FACE_TEMPERATURE = 1;
        public static int MODEL_TEMPERATURE_ONLY = 2;
        public static int MODEL_THERMAL_IMAGING_ONLY = 3;
        public static int MODEL_FACE_THERMAL_IMAGING = 4;
        public static int MODEL_CERTIFICATES_THERMAL = 5;
        public static String[] models;

        public static void initModels(Context context) {
            models = context.getResources().getStringArray(R.array.models);
        }
    }

    public interface serverModel {
        int YUN = 0;//云端
        int JU = 1;//局域网
    }

    //修改设备类型
    public static final int DEVICE_TYPE = BuildConfig.APP_TYPE ;
    //是否亨通的版本（独立版本）
    public static boolean isHT = BuildConfig.IS_HT;
    //是否思库的版本(云标分支版本)
    public static boolean isSK = BuildConfig.IS_SK;

    public interface DeviceType {
        int CHECK_IN = 1;//考勤机
        int TEMPERATURE_CHECK_IN = 7;//测温考勤机
        int TEMPERATURE_CHECK_IN_SMT = 8;//测温通行机（视美泰考勤版）
        int TEMPERATURE_CERTIFICATES = 9;//人证测温机
        int MULTIPLE_THERMAL = 10;//多人测温

        int HT_TEMPERATURE_CHECK_IN = 11;//亨通考勤
        int HT_TEMPERATURE_CHECK_IN_SMT = 12;//亨通通行
        int HT_TEMPERATURE_CERTIFICATES = 13;//亨通人证
        int HT_MULTIPLE_THERMAL = 14;//亨通多人
    }

    //设备未绑定时的公司Id
    public static final int NOT_BIND_COMPANY_ID = 0;
    //最大人脸线程数
    public static final int MAX_DETECT_NUM = DEVICE_TYPE == DeviceType.MULTIPLE_THERMAL || DEVICE_TYPE == DeviceType.HT_MULTIPLE_THERMAL ? 30 : 10 ;
    //最远人脸抓取距离
    public static final int DETECT_FACE_SCALE_VAL = DEVICE_TYPE == DeviceType.MULTIPLE_THERMAL || DEVICE_TYPE == DeviceType.HT_MULTIPLE_THERMAL ? 32 : 16;





    //设置默认模式（如果是8寸机则返回热成像模式，如果是其他则红外模式）
    public static final int DEFAULT_TEMP_MODEL = Model.MODEL_THERMAL_IMAGING_ONLY;
    //默认波特率
    public static final int DEFAULT_BAUD_RATE = BaudRate.THERMAL_IMAGING_BAUD_RATE;
    //屏幕角度默认值
    public static int DEFAULT_CAMERA_ANGLE = 0;
    //端口号
    public static final String DEFAULT_PORT_PATH = "/dev/ttyS4";
    //大屏海报开启状态
    public static final boolean DEFAULT_POSTER_ENABLED = false;
    //默认热成像镜像
    public static final boolean DEFAULT_THERMAL_IMAGE_MIRROR = true;
    //默认最小阈值
    public static final float DEFAULT_TEMP_MIN_THRESHOLD_VALUE = 35.5F;
    //默认身体百分比
    public static final int DEFAULT_BODY_PERCENT_VALUE = 1;
    //默认热成像最小体温
    public static final int DEFAULT_BODY_MIN_T_VALUE = 340;
    //默认热成像最大体温值
    public static final int DEFAULT_BODY_MAX_T_VALUE = 400;
    //测温报警值
    public static final float DEFAULT_TEMP_WARNING_THRESHOLD_VALUE = 37.3f;
    //取温延时默认值
    public static final int DEFAULT_GET_TEMP_DELAY_TIME_VALUE = 600;
    //距离提示开关
    public static final boolean DEFAULT_DISTANCE_TIPS_ENABLED_VALUE = true;
    //检测温差值
    public static final float DEFAULT_TEMP_D_VALUE_VALUE = 3.0f;
    //环境温度补正
    public static final float DEFAULT_AMB_CORRECT_VALUE = 0.0F;
    //检测温度补正
    public static final float DEFAULT_TEMP_CORRECT_VALUE = 0.0f;
    //默认读卡器
    public static final boolean DEFAULT_READ_CARD_ENABLED = false;
    //默认二维码
    public static final boolean DEFAULT_QRCODE_ENABLED = true;
    //播报延时默认值
    public static final long DEFAULT_SPEECH_DELAY = 5000;

    public interface BaudRate {
        int INFARED_TEMP_BAUD_RATE = 9600;
        int THERMAL_IMAGING_BAUD_RATE = 115200;
    }

}


