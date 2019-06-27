package com.yunbiao.ybsmartcheckin_live_id.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.yunbiao.ybsmartcheckin_live_id.APP;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Created by LiuShao on 2016/2/21.
 */
public class SpUtils {

    private static SharedPreferences sp;
    private static final String SP_NAME = "YB_FACE";

    public static final String DEVICE_UNIQUE_NO = "deviceNo";//设备唯一号

    public static final String COMPANYID = "companyid";//公司ID
    public static final String DEVICE_NUMBER = "devicesernum";//设备编号
    public static final String BINDCODE = "bindCode";//绑定码

    public static final String AD_HENG = "ad_heng";//横屏广告
    public static final String AD_SHU = "ad_shu";//竖屏广告

    public static final String GOTIME = "gotime";//上班时间
    public static final String DOWNTIME = "downtime";//下班时间
    public static final String GOTIPS = "gotips";//打卡提示
    public static final String DOWNTIPS = "downtips";//下班提示
    public  static final String CITYNAME= "city";//城市

    public static final String EXP_DATE = "expDate";//过期时间
    public static final String COMPANY_NAME = "companyName";//公司名称
    public static final String COMPANY_INFO = "companyInfo";//公司信息

    public static final String CITY_NAME = "city_name";//定位城市
    public static final String MENU_PWD = "menu_pwd";//用户访问密码

    public static final String IS_MIRROR = "isMirror";//是否镜像
    public static final String BOARD_INFO = "boardInfo";
    public static final String RUN_KEY = "runKey";
    public static final String DEVICE_TYPE = "deviceType";
    public static final String CURR_VOLUME = "currentVolume";

    public static final String CAMERA_ANGLE = "cameraAngle";//摄像头角度

    static {
        sp = APP.getContext().getSharedPreferences(SP_NAME,Context.MODE_PRIVATE);
    }

    public static boolean isMirror(){
        return getBoolean(IS_MIRROR,true);
    }

    public static void setMirror(boolean b){
        saveBoolean(IS_MIRROR,b);
    }

    public static void saveStr(String key, String value){
        if(sp != null){
            sp.edit().putString(key,value).commit();
        }
    }

    public static void saveInt(String key,int value){
        if(sp != null){
            sp.edit().putInt(key,value).commit();
        }
    }

    public static String getStr(String key){
        if(sp != null){
            return sp.getString(key,"");
        }
        return "";
    }
    public static String getStr(String key,String defaultValue){
        if(sp != null){
            return sp.getString(key,defaultValue);
        }
        return "";
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

    public static int getInt(String key){
        if(sp != null){
            return sp.getInt(key,0);
        }
        return 0;
    }

    public static void clear(Context context){
        if(sp != null){
            sp.edit().clear().apply();
        }
    }

    public static void saveBoolean(String key,boolean b){
        if(sp != null){
            sp.edit().putBoolean(key,b).commit();
        }
    }

    public static boolean getBoolean(String key,boolean defValue){
        if(sp != null){
            return sp.getBoolean(key,defValue);
        }
        return false;
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
