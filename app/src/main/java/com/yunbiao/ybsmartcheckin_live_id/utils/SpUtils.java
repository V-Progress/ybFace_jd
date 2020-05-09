package com.yunbiao.ybsmartcheckin_live_id.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
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

    public static final String SERVER_MODEL = "serverModel";//服务模式
    public static final String JU_IP_CACHE = "juIpCache";//IP
    public static final String JU_RESOURCE_PORT_CACHE = "juResourcePortCache";//端口
    public static final String JU_XMPP_PORT_CACHE = "juXmppPortCache";//XMPP端口
    public static final String JU_PROJECT_NAME_SUFFIX = "juRrojectNameSuffix";//后缀

    public static final String FACE_DIALOG = "faceDialog";//人脸弹窗
    public static final String SIMILAR_THRESHOLD = "similarThreshold";//相似度阈值
    public static final String LIVENESS_ENABLED = "livenessEnabled";//活体开关
    public static final String POSTER_ENABLED = "posterEnabled";//大屏海报是否可用
    public static final String NORMAL_TIPS = "normalTips";//体温正常播报
    public static final String WARNING_TIPS = "warningTips";//体温异常播报
    public static final String READ_CARD_ENABLED = "readCardEnabled";//读卡模块
    public static final String WELCOM_TIPS = "welcomeTips";//欢迎语
    public static final String QRCODE_ENABLED = "qrCodeEnabled";//二维码开关

    static {
        sp = APP.getContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public static void init() {
        getCompany();
    }

    public static void setCompany(final Company company) {
        mCacheCompany = company;
        if(company != null){
            Observable.create(e -> {
                String json = new Gson().toJson(company);
                saveStr(COMPANY_INFO, json);
            }).subscribeOn(Schedulers.io()).subscribe();
        } else {
            saveStr(COMPANY_INFO, "");
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
                mCacheCompany.setComid(Constants.NOT_BIND_COMPANY_ID);
            }
        }
        return mCacheCompany;
    }

    public static boolean isMirror() {
        return getBoolean(IS_MIRROR, Constants.DEFAULT_FACE_MIRROR);
    }

    public static void setMirror(boolean b) {
        saveBoolean(IS_MIRROR, b);
    }

    public static boolean saveStr(String key, String value) {
        if (sp != null) {
            return sp.edit().putString(key, value).commit();
        }
        return false;
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
}
