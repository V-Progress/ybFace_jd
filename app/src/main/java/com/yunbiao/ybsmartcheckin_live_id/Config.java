package com.yunbiao.ybsmartcheckin_live_id;

import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

public class Config {
    public static final int DEVICE_ONLY_FACE = 0;//人脸签到广告机
    public static final int DEVICE_FACE_CARD = 1;//人卡签到（电子班牌）
    public static final int DEVICE_PASSAGE_ONLY_FACE = 2;//人脸闸机
    public static final int DEVICE_PASSAGE_FACE_CARD = 3;//人卡闸机
    public static final int DEVICE_SMALL_FACE = 4;//京东超脑签到机
    public static int deviceType;

    private static final String TAG = "Config";
    public static final int NONE_SERVICE = -1;//无服务（单机版）
    public static final int LOCAL_SERVICE = 0;//本地服务
    public static final int CLOUD_SERVICE = 1;//云端服务
    public static Integer DEVICE_SMALL_10_INCh = 5;
    private static int service = CLOUD_SERVICE;//服务状态

    private static final boolean isLiveness ;//是否检测活体
    private static final boolean isMultiple;//是否检测多人

    public static final int LIGNT_SMART = 0;//智能点亮
    public final int LIGHT_LONG_BRIGHT = 1;//长亮
    public final int LIGHT_ONLY_NIGHT = 2;//仅夜间
    private static int lightStrate = LIGNT_SMART;//补光灯规则

    public static final int POWER_WORKING_DAY = 0;//工作日
    public static final int POWER_EVERY_DAY = 1;//每天
    private static int powerStrate;//电源规则

    public static final String DEFAULT_TIPS = "您好，";//默认提示语
    private static String tips ;//提示语

    public static final int LOCAL_ADS = 0;//本地广告
    public static final int CLOUD_ADS = 1;//云端广告
    private static int adsResource = CLOUD_ADS;//广告资源

    public static final int CAMERA_BACK = 0;//后置
    public static final int CAMERA_FRONT = 1;//前置
    public static final int CAMERA_AUTO = -1;//自动
    private static int cameraType = CAMERA_AUTO;//摄像头类型

    private static int face_record_compress_ratio = 30;//人脸记录头像的压缩比

    static {
        service = SpUtils.getIntOrDef(KEY.SERVICE_TYPE, CLOUD_SERVICE);
        isLiveness = SpUtils.getBoolean(KEY.LIVENESS,true);
        isMultiple = SpUtils.getBoolean(KEY.MULTIPLE,true);
        lightStrate = SpUtils.getIntOrDef(KEY.LIGHT_STRATE, LIGNT_SMART);
        powerStrate = SpUtils.getIntOrDef(KEY.LIGHT_STRATE, POWER_EVERY_DAY);
        tips = SpUtils.getStr(KEY.TIPS,DEFAULT_TIPS);
        adsResource = SpUtils.getIntOrDef(KEY.ADS_RES,CLOUD_ADS);
        cameraType = SpUtils.getIntOrDef(KEY.CAMERA_TYPE,CAMERA_AUTO);
        face_record_compress_ratio = SpUtils.getIntOrDef(KEY.COMPRESS_RATIO,50);
    }

    interface KEY{
        String LIVENESS = "liveness";
        String SERVICE_TYPE = "serviceType";
        String MULTIPLE = "multiple";
        String LIGHT_STRATE = "lightStrate";
        String TIPS = "tips";
        String ADS_RES = "adsResource";
        String CAMERA_TYPE = "cameraType";
        String COMPRESS_RATIO = "compressRatio";
    }

    public static int getAdsResource(){
        return adsResource;
    }

    public static void setAdsResource(int resource){
        if(adsResource == resource){
            return;
        }
        SpUtils.saveInt(KEY.ADS_RES,resource);
        adsResource = resource;
        // TODO: 2019/7/3 配置更新
    }

    public static int getLightStrate(){
        return lightStrate;
    }

    public static void setLightStrate(int strate){
        if(strate == lightStrate){
            return;
        }
        SpUtils.saveInt(KEY.LIGHT_STRATE,strate);
        lightStrate = strate;
        // TODO: 2019/7/3 通知相关功能块更新
    }

    /***
     * 获取摄像头类型
     * @return
     */
    public static int getCameraType(){
        return cameraType;
    }

    /***
     * 是否开启活体检测
     * @return
     */
    public static boolean isLiveness(){
        return isLiveness;
    }

    /***
     * 是否开启多人识别
     * @return
     */
    public static boolean isMultiple(){
        return isMultiple;
    }

    /***
     * 获取人脸记录的压缩比
     * @return
     */
    public static int getCompressRatio(){
        return face_record_compress_ratio;
    }
}
