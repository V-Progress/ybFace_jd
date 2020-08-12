package com.yunbiao.ybsmartcheckin_live_id.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by LiuShao on 2016/2/21.
 */
public class SpUtils {
    private static SharedPreferences sp;
    private static final String SP_NAME = "YB_FACE";

    public static Company mCacheCompany;//全局缓存
    public static final String DEVICE_UNIQUE_NO = "deviceNo";//设备唯一号
    public static final String DEVICE_NUMBER = "devicesernum";//设备编号
    public static final String BIND_CODE = "bindCode";//绑定码
    public static final String CITYNAME = "city";//城市
    public static final String MENU_PWD = "menu_pwd";//用户访问密码
    public static String DEFAULT_MENU_PWD = "";
    public static final String EXP_DATE = "expDate";//过期时间
    public static final String DISPLAYPOSITION = "displayPosition";//过期时间
    public static final String COMPANYID = "companyid";//公司ID
    public static final String COMPANY_INFO = "companyInfo";//公司视频宣传
    public static final String COMPANY_AD_HENG = "ad_heng";//横屏广告
    public static final String COMPANY_AD_SHU = "ad_shu";//竖屏广告
    public static final String BOARD_INFO = "boardInfo";//主板信息
    public static final String RUN_KEY = "runKey";//key
    public static final String DEVICE_TYPE = "deviceType";//设备类型
    public static final String CURR_VOLUME = "currentVolume";//当前音量
    public static final String LAST_INIT_TIME = "lastInitTime";//上次更新时间

    public static final String MULTI_BOX_PORTRAIT_OFFSET = "multiBoxPortraitOffset";//大通量热成像人脸框纵向偏移值
    public static final String MULTI_BOX_SIZE_OFFSET = "multiBoxSizeOffset";//大通量热成像人脸框大小偏移值

    public static final String POWER_ON_OFF_SWITCH = "powerOnOffSwitch";
    public static boolean powerOnOffDef = false;

    static {
        sp = APP.getContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public static void init() {
        getCompany();
        Timber.d("初始化时公司Id为：" + (mCacheCompany == null ? "NULL" : mCacheCompany.getComid()));
    }

    public static void setCompany(final Company company) {
        mCacheCompany = company;
        if(company != null){
            //存公司ID
            saveInt(COMPANYID, mCacheCompany.getComid());
            saveStr(MENU_PWD, mCacheCompany.getDevicePwd());
            saveInt(DISPLAYPOSITION, mCacheCompany.getDisplayPosition());
            Observable.create(e -> {
                String json = new Gson().toJson(company);
                saveStr(COMPANY_INFO, json);
            }).subscribeOn(Schedulers.io()).subscribe();
        } else {
            saveStr(COMPANY_INFO, "");
            saveInt(COMPANYID, 0);
            saveInt(DISPLAYPOSITION, 0);
        }
        Timber.d("同步后公司Id为：" + (mCacheCompany == null ? "NULL" : mCacheCompany.getComid()));
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
            saveInt(COMPANYID, mCacheCompany.getComid());
        }
        return mCacheCompany;
    }

    public static boolean remove(String key){
        if(sp != null){
            return sp.edit().remove(key).commit();
        }
        return false;
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

    public static boolean clear() {
        if (sp != null) {
            return sp.edit().clear().commit();
        }
        return false;
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

    public static Map<String, ?> getAll() {
        if(sp != null){
            return sp.getAll();
        }
        return null;
    }
}
