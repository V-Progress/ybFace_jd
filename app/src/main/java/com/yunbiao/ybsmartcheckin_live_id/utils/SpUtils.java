package com.yunbiao.ybsmartcheckin_live_id.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by LiuShao on 2016/2/21.
 */
public class SpUtils {

    public static final String DEVICE_UNIQUE_NO = "deviceNo";//设备唯一号
    public static final String DEVICE_NUMBER = "devicesernum";//设备编号
    public static final String BIND_CODE = "bindCode";//绑定码

    public static final String DOOR_STATE = "doorState";//门禁常开模式
    public static final String GPIO_DELAY = "doorDelay";
    private static SharedPreferences sp;
    private static final String SP_NAME = "YB_FACE";

    public static final String CAMERA_WIDTH = "cameraWidth";//摄像头宽
    public static final String CAMERA_HEIGHT = "cameraHeight";//摄像头高

    public static final String CITYNAME = "city";//城市
    public static final String MENU_PWD = "menu_pwd";//用户访问密码
    public static final String EXP_DATE = "expDate";//过期时间
    public static final String SKIN_ID = "skinId";
    public static final String SKIN_URL = "skinUrl";

    public static final String DISPLAYPOSITION = "displayPosition";//过期时间

    public static Company mCacheCompany;//全局缓存
    public static final String COMPANYID = "companyid";//公司ID
    public static final String COMPANY_INFO = "companyInfo";//公司视频宣传
    public static final String COMPANY_AD_HENG = "ad_heng";//横屏广告
    public static final String COMPANY_AD_SHU = "ad_shu";//竖屏广告

    public static final String IS_MIRROR = "isMirror";//是否镜像
    public static final String BOARD_INFO = "boardInfo";
    public static final String RUN_KEY = "runKey";
    public static final String DEVICE_TYPE = "deviceType";
    public static final String CURR_VOLUME = "currentVolume";

    public static final String CAMERA_ANGLE = "cameraAngle";//摄像头角度
    public static final String CAMERA_SIZE = "cameraSize";

    public static final String LAST_INIT_TIME = "lastInitTime";//上次更新时间

    public static final String IP_CACHE = "ipCache";
    public static final String RESOURCE_PORT_CACHE = "resourcePortCache";
    public static final String XMPP_PORT_CACHE = "xmppPortCache";
    public static final String PROJECT_NAME_SUFFIX = "projectNameSuffix";

    public static final String FACE_DIALOG = "faceDialog";
    public static final String SIMILAR_THRESHOLD = "similarThreshold";
    public static final String LIVENESS_ENABLED = "livenessEnabled";//活体开关

    //测温相关配置
    public static final String TEMP_CORRECT_VALUE = "tempCorrectValue";//测温矫正值
    public static final String AMB_CORRECT_VALUE = "ambCorrectValue";//环境温度校正值
    public static final String MODEL_SETTING = "modelSetting";//模式设置
    public static final String TEMP_TIPS_TIME = "tempTipsTime";//提示时间
    public static final String GET_TEMP_DELAY_TIME = "getTempDelayTime";//取温延迟
    public static final String TEMP_WARNING_THRESHOLD = "tempWarningThreshold";//温度报警阈值
    public static final String TEMP_MIN_THRESHOLD = "tempMinThreshold";//温度最低阈值：默认值35.5f----------
    public static final String POSTER_ENABLED = "posterEnabled";//大屏海报是否可用
    public static final String DISTANCE_TIPS_ENABLED = "distanceTipsEnabled";//距离提示开关
    public static final String TEMP_D_VALUE = "tempDValue";//高低温差值
    public static final String PORT_PATH = "portPath";//端口号缓存
    public static final String BAUD_RATE = "baudRate";//波特率
    public static final String THERMAL_IMAGE_MIRROR = "thermalImageMirror";//热成像画面镜像
    public static final String BODY_PERCENT = "bodyPercent";//身体占比：默认值1----------
    public static final String BODY_MIN_T = "bodyMinT";//身体占比：默认值340------------
    public static final String BODY_MAX_T = "bodyMaxT";//身体占比
    public static final String GET_TEMP_DELAY_ENABLED = "getTempDelayEnabled";
    public static final String SERVER_MODEL = "serverModel";//服务模式
    public static final String JU_IP_CACHE = "juIpCache";//IP
    public static final String JU_RESOURCE_PORT_CACHE = "juResourcePortCache";//端口
    public static final String JU_XMPP_PORT_CACHE = "juXmppPortCache";//XMPP端口
    public static final String JU_PROJECT_NAME_SUFFIX = "juRrojectNameSuffix";//后缀
    public static final String READ_CARD_ENABLED = "readCardEnabled";//读卡模块
    public static final String WELCOM_TIPS = "welcomeTips";//欢迎语
    public static final String QRCODE_ENABLED = "qrCodeEnabled";//二维码开关

    public static final String NORMAL_TIPS = "normalTips";//体温正常播报
    public static final String WARNING_TIPS = "warningTips";//体温异常播报

    public static final String SPEECH_DELAY = "speechDelay";//播报延时

    //视美泰相关
    public static final String SMT_MODEL_SETTING = "smt_model_setting";

    //热成像
    public static final String THERMAL_MODEL_SETTING = "thermalModelSetting";
    public static final String LOW_TEMP_MODEL = "lowTempModel";

    public static final String AMBIENT = "ambient";

    public static void init() {
        getCompany();
    }

    public static void setCompany(final Company company) {
        mCacheCompany = company;
        if(company != null){
            Observable.create(new ObservableOnSubscribe<Object>() {
                @Override
                public void subscribe(ObservableEmitter<Object> e) throws Exception {
                    String json = new Gson().toJson(company);
                    saveStr(COMPANY_INFO, json);
                }
            }).subscribeOn(Schedulers.io()).subscribe();
        }
    }

    public static Company getCompany() {
        if (mCacheCompany == null) {
            String str = SpUtils.getStr(COMPANY_INFO);
            if (!TextUtils.isEmpty(str)) {
                Company company = new Gson().fromJson(str, Company.class);
                mCacheCompany = company;
            }
            if (mCacheCompany == null) {
                mCacheCompany = new Company();
                mCacheCompany.setComid(0);
            }
        }
        return mCacheCompany;
    }

    static {
        sp = APP.getContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isMirror() {
        return getBoolean(IS_MIRROR, true);
    }

    public static void setMirror(boolean b) {
        saveBoolean(IS_MIRROR, b);
    }

    public static void saveStr(String key, String value) {
        if (sp != null) {
            sp.edit().putString(key, value).commit();
        }
    }

    public static void saveInt(String key, int value) {
        if (sp != null) {
            sp.edit().putInt(key, value).commit();
        }
    }

    public static void saveFloat(String key, float value) {
        if (sp != null) {
            sp.edit().putFloat(key, value).commit();
        }
    }

    public static void saveLong(String key, long value) {
        if (sp != null) {
            sp.edit().putLong(key, value).commit();
        }
    }

    public static long getLong(String key) {
        if (sp != null) {
            return sp.getLong(key, 0);
        }
        return 0;
    }

    public static long getLong(String key,long defaultValue){
        if(sp != null){
            return sp.getLong(key,defaultValue);
        }
        return defaultValue;
    }

    public static String getStr(String key) {
        if (sp != null) {
            return sp.getString(key, "");
        }
        return "";
    }

    public static String getStr(String key, String defaultValue) {
        if (sp != null) {
            return sp.getString(key, defaultValue);
        }
        return defaultValue;
    }

    // TODO: 2019/6/27 ComById
//    public static int getCompanyId(){
//        if(sp != null){
//            return sp.getInt(COMPANYID,56);
//        }
//        return -1;
//    }
//
//    public static void saveCompanyId(int comId){
//        if(sp != null){
//            sp.edit().putInt(COMPANYID,comId).commit();
//        }
//    }

    public static int getInt(String key) {
        if (sp != null) {
            return sp.getInt(key, 0);
        }
        return 0;
    }

    public static Float getFloat(String key, float defaultValue) {
        if (sp != null) {
            return sp.getFloat(key, defaultValue);
        }
        return 0f;
    }

    public static int getIntOrDef(String key, int def) {
        if (sp != null) {
            return sp.getInt(key, def);
        }
        return def;
    }

    public static void clear(Context context) {
        if (sp != null) {
            sp.edit().clear().apply();
        }
    }

    public static void saveBoolean(String key, boolean b) {
        if (sp != null) {
            sp.edit().putBoolean(key, b).commit();
        }
    }

    public static boolean getBoolean(String key, boolean defValue) {
        if (sp != null) {
            return sp.getBoolean(key, defValue);
        }
        return defValue;
    }

//    public static void saveString(Context context, String key, String value) {
//        if (sp == null)
//            sp = context.getSharedPreferences(SP_NAME, 0);
//        sp.edit().putString(key, value).apply();
//    }
//
//    public static String getString(Context context, String key, String defValue) {
//        if (sp == null)
//            sp = context.getSharedPreferences(SP_NAME, 0);
//        return sp.getString(key, defValue);
//    }
//
//    public static void saveInt(Context context, String key, int value) {
//        if (sp == null)
//            sp = context.getSharedPreferences(SP_NAME, 0);
//        sp.edit().putInt(key, value).apply();
//    }
//
//    public static int getInt(Context context, String key, int value) {
//        if (sp == null)
//            sp = context.getSharedPreferences(SP_NAME, 0);
//        return sp.getInt(key, value);
//    }
}
