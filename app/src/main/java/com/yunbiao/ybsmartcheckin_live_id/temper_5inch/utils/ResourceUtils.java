package com.yunbiao.ybsmartcheckin_live_id.temper_5inch.utils;

import android.os.Environment;

import com.yunbiao.ybsmartcheckin_live_id.APP;

public class ResourceUtils {

    public static String RESOURCE_PATH = Environment.getExternalStorageDirectory() + "/TemperatureMachine";

    public static String DEBUG_LOG_PATH = RESOURCE_PATH + "/log/";
    public static String ERROR_LOG_PATH = RESOURCE_PATH + "/error/";
    public static String AD_PATH = RESOURCE_PATH + "/ad/";
    public static String LOGO_PATH = RESOURCE_PATH + "/logo/main_logo.png";
    public static String LANGUAGE_PATH = RESOURCE_PATH + "/language/";
    public static String SUBTITLE_PATH = RESOURCE_PATH + "/subtitle/content.txt";

    public static String getStringResource(int id) {
        return APP.getContext().getResources().getString(id);
    }

    public static int getColorResource(int id) {
        return APP.getContext().getResources().getColor(id);
    }

}
