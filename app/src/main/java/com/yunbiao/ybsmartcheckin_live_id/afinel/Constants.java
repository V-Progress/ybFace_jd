package com.yunbiao.ybsmartcheckin_live_id.afinel;

import android.os.Environment;
import android.util.Log;

import com.yunbiao.ybsmartcheckin_live_id.utils.PropsUtil;

public class Constants {
    /**
     * 资源url web
     * http://zz.yunbiaowulian.com
     */
    public static String DOMAIN = "http://zz.yunbiaowulian.com";
//    public static String RESOURCE_URL = DOMAIN + "/ybface/";
    public static String RESOURCE_URL = DOMAIN;
    public static String UP_LOAD_ERR_FILE = RESOURCE_URL + "queue/upLoadText.html";

    public static String LOCAL_ROOT_PATH = Environment.getExternalStorageDirectory().getPath() + "/yb_face_db/";
    public static String ADS_PATH = LOCAL_ROOT_PATH + "ads/";//广告路径
    public static String DATA_PATH = LOCAL_ROOT_PATH + "data/";//数据库路径
    public static String HEAD_PATH = LOCAL_ROOT_PATH + "photo/";//照片路径
    public static String CACHE_PATH = LOCAL_ROOT_PATH + "cache/";//缓存路径

    public static void init(){
        DOMAIN = PropsUtil.instance().getResHost();
        String resPort = PropsUtil.instance().getResPort();
        RESOURCE_URL = DOMAIN+":"+resPort+"/";//"/ybface/"
        UP_LOAD_ERR_FILE = RESOURCE_URL + "queue/upLoadText.html";
        ResourceUpdate.initUrl();
    }
}


