package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;

public class ThermalConst {

    public interface Key {
        String FACE_ENABLED = "thermalFaceEnabled"; //启用人脸识别
        String TEMPER_ENABLED = "thermalTemperEnabled";//启用测温
        String TEMPER_MODULE = "thermalTemperatureModule";//测温模块
        String LOW_TEMP_MODE = "lowTempMode";//低温模式
        String AMBIENT_CORRECT = "ambientCorrect";//环境温度补偿
        String THERMAL_CORRECT = "thermalCorrect";//测温补偿
        String PERSON_FRAME = "personFrame";//人像
        String SPEECH_DELAY = "speechDelay";//播报延时
        String TEMP_MIN_THRESHOLD = "tempMinThreshold";//低温阈值
        String TEMP_WARNING_THRESHOLD = "tempWarningThreshold";//高温阈值
        String THERMAL_IMAGE_MIRROR = "thermalImageMirror";//热成像镜像
        String THERMAL_F_ENABLED = "fEnabled";//华氏度
        String SHOW_DIALOG = "thermalShowDialog";//识别弹窗
        String VOICE_SPEED = "thermalVoiceSpeed";//语音播报
        String SHOW_MAIN_LOGO = "thermalShowMainLogo";//首页Logo开关
        String SHOW_MAIN_INFO = "thermalShowMainInfo";//首页信息开关
        String SHOW_MAIN_THERMAL = "thermalShowMainThermal";//首页热成像开关
        String MAIN_LOGO_TEXT = "thermalMainLogoText";//首页Logo文字设置
        String MAIN_LOGO_IMG = "thermalMainLogoImg";//首页LOGO图片设置
        String HIGH_TEMPER_MODE = "highTempMode";//高温模式
        String LOCAL_PRIORITY = "thermalLocalPriority";//本地优先级
        String MASK_DETECT_ENABLED = "thermalMaskDetectEnabled";//口罩检测
        String WELCOME_TIP_CONTENT = "welcomeTips";//欢迎语设置
        String WELCOME_TIP_ENABLED = "welcomeTipEnabled";//欢迎语开关
        String DISTANCE_TIP_CONTENT = "thermalCloseTips";//距离提示设置
        String DISTANCE_TIP_ENABLED = "distanceTip";//距离提示开关
        String FRAME_TIP_CONTENT = "frameTipContent";//对框提示设置
        String FRAME_TIP_ENABLED = "frameTipEnabled";//对框提示开关
        String NORMAL_BROADCAST = "normalBroadcast";//正常播报设置
        String NORMAL_TEMPER_SHOW = "normalTemperShow";//正常播报温度开关
        String NORMAL_TEMPER_LOCATION = "normalTemperLocation";//温度位置
        String NORMAL_BROADCAST_ENABLED = "normalBroadcastEnabled";//正常播报开关
        String WARNING_BROADCAST = "warningBroadcast";//异常播报设置
        String WARNING_TEMPER_SHOW = "warningTemperShow";//异常播报温度开关
        String WARNING_TEMPER_LOCATION = "warningTemperLocation";//温度位置
        String WARNING_BROAD_ENABLED = "warningBroadcastEnabled";//异常播报开关
        String CENTIGRADE = "centigradeUnit";//摄氏度单位
        String FAHRENHEIT = "fahrenheitUnit";//华氏度单位
        String MASK_TIP = "thermalMaskTip";//口罩提示开关
        String TITLE_ENABLED = "thermalTitleEnabled";//标题显示
        String NO_FACE_TEMPER = "thermalNoFaceThermal";
        String TIP_DELAY = "thermalTipDelay";
    }

    public static class Default {
        public static final long TIP_DELAY = 500;
        public static final boolean NO_FACE_TEMPER = false;//无人脸测温
        public static boolean FACE_ENABLED = false;//启用人脸识别
        public static boolean TEMPER_ENABLED = true;//启用测温
        public static int TEMPER_MODULE = TemperModuleType.MLX_16_4;
        public static final boolean TITLE_ENABLED = true;
        public static int DEFAULT_LOGO_ID = R.mipmap.yb_logo;
        public static final float VOICE_SPEED = 1.8f;
        public static boolean SHOW_MAIN_LOGO = true;//LOGO
        public static boolean SHOW_MAIN_INFO = false;//主页信息
        public static boolean SHOW_MAIN_THERMAL = true;//热成像
        public static final boolean SHOW_DIALOG = false;
        public static boolean LOW_TEMP = true;
        public static float AMBIENT_CORRECT = 25.0f;
        public static float THERMAL_CORRECT = 0.0f;
        public static boolean PERSON_FRAME = true;
        public static long SPEECH_DELAY = 5000;
        public static float TEMP_MIN_THRESHOLD = 35.5f;
        public static float TEMP_WARNING_THRESHOLD = 37.3f;
        public static boolean THERMAL_IMAGE_MIRROR = true;
        public static boolean THERMAL_F_ENABLED = false;
        public static String MAIN_LOGO_TEXT = "YBFACE";
        public static String MAIN_LOGO_IMG = "";
        public static boolean HIGH_TEMPER_MODE = false;
        public static boolean MASK_DETECT_ENABLED = true;
        public static boolean LOCAL_PRIORITY = true;
        public static final boolean WELCOME_TIP_ENABLED = true;
        public static final boolean DISTANCE_TIP_ENABLED = false;
        public static final boolean FRAME_TIP_ENABLED = true;
        public static final boolean NORMAL_TEMPER_SHOW = true;
        public static final int NORMAL_TEMPER_LOCATION = 2;
        public static boolean NORMAL_BROADCAST_ENABLED = true;
        public static final boolean WARNING_TEMPER_SHOW = true;
        public static final int WARNING_TEMPER_LOCATION = 2;
        public static boolean WARNING_BROAD_ENABLED = true;

        public static String WELCOME_TIP_CONTENT = APP.getContext().getResources().getString(R.string.setting_default_welcome_tip);
    }

}
