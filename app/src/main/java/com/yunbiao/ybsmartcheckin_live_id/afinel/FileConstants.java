package com.yunbiao.ybsmartcheckin_live_id.afinel;

import android.os.Environment;
import android.text.TextUtils;

import com.yunbiao.ybsmartcheckin_live_id.utils.SdCardUtils;


public class FileConstants {

    private static String RESOURSE_MENU = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String CACHE_BASE_PATH = RESOURSE_MENU+ "/facecheckin_online/";
    public static String IMAGE_CACHE_PATH = CACHE_BASE_PATH + "resource/";// 资源存储目录
    public static String PROPERTY_CACHE_PATH = CACHE_BASE_PATH + "property/";// 参数缓存存储目录
    public static String VIDEO_CACHE_PATH = CACHE_BASE_PATH + "video/";// 参数缓存存储目录
    public static String SHOPIMG_CACHE_PATH = CACHE_BASE_PATH + "shop/";// 参数缓存存储目录
    public static String CAllNumQR_CACHE_PATH = CACHE_BASE_PATH + "callCash/";// 参数缓存存储目录

    /**
     * menu 弹出框选择内置卡或者外置卡
     * @param innerSd
     */
    public static void setNewResourcePath(boolean innerSd) {
        if (innerSd) {
            RESOURSE_MENU = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            String path = SdCardUtils.getNoUsbSdcardPath();
            if (!TextUtils.isEmpty(path)) {
                RESOURSE_MENU = path;
            }
        }
    }
}
