package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;

public class ThermalConst {

    public static final int FACE_ONLY = 0;//ONLY FACE
    public static final int INFRARED_ONLY = 1;//ONLY SMALL
    public static final int FACE_INFRARED = 2;//FACE + SMALL
    public static final int THERMAL_ONLY = 3;//ONLY HM-32*32
    public static final int FACE_THERMAL = 4;//FACE + HM-32*32
    public static final int THERMAL_16_4_ONLY = 5;//ONLY HM-16*4
    public static final int FACE_THERMAL_16_4 = 6;//FACE HM-16*4
    public static final int ONLY_THERMAL_MLX_16_4 = 7;//ONLY MLX-16*4
    public static final int FACE_THERMAL_MLX_16_4 = 8;//FACE MLX-16*4
    public static final int ONLY_SMT_THERMAL = 9;
    public static final int FACE_SMT_THERMAL = 10;

    public static String[] models;

    static {
        models = APP.getContext().getResources().getStringArray(R.array.models);
    }

    interface Key {
        String MODE = "thermalModelSetting";
        String THERMAL_MIRROR = "thermalMirror";
        String LOW_TEMP_MODE = "lowTempMode";
        String AMBIENT_CORRECT = "ambientCorrect";
        String THERMAL_CORRECT = "thermalCorrect";
        String NORMAL_BROADCAST = "normalBroadcast";
        String WARNING_BROADCAST = "warningBroadcast";
        String PERSON_FRAME = "personFrame";
        String DISTANCE_TIP = "distanceTip";
        String SPEECH_DELAY = "speechDelay";
        String TEMP_MIN_THRESHOLD = "tempMinThreshold";
        String TEMP_WARNING_THRESHOLD = "tempWarningThreshold";
        String THERMAL_IMAGE_MIRROR = "thermalImageMirror";
        String THERMAL_F_ENABLED = "fEnabled";
        String SHOW_DIALOG = "thermalShowDialog";
        String VOICE_SPEED = "thermalVoiceSpeed";
        String SHOW_MAIN_LOGO = "thermalShowMainLogo";
        String SHOW_MAIN_INFO = "thermalShowMainInfo";
        String SHOW_MAIN_THERMAL = "thermalShowMainThermal";
        String CLOSE_TIPS = "thermalCloseTips";
        String MAIN_LOGO_TEXT = "thermalMainLogoText";
        String MAIN_LOGO_IMG = "thermalMainLogoImg";
        String AUTO_TEMPER = "thermalAutoTemper";
    }

    public static class Default {
        public static final float VOICE_SPEED = 1.8f;
        public static final boolean SHOW_MAIN_LOGO = false;//LOGO
        public static final boolean SHOW_MAIN_INFO = false;//主页信息
        public static final boolean SHOW_MAIN_THERMAL = false;//热成像
        public static final boolean SHOW_DIALOG = false;
        public static int MODE = ONLY_THERMAL_MLX_16_4;
        public static boolean THERMAL_MIRROR = true;
        public static boolean LOW_TEMP = true;
        public static float AMBIENT_CORRECT = 25.0f;
        public static float THERMAL_CORRECT = 0.0f;
        public static final String CLOSE_TIPS = "";
        public static String NORMAL_BROADCAST = "";
        public static String WARNING_BROADCAST = "";
        public static boolean PERSON_FRAME = true;
        public static boolean DISTANCE_TIP = true;
        public static long SPEECH_DELAY = 5000;
        public static float TEMP_MIN_THRESHOLD = 35.5f;
        public static float TEMP_WARNING_THRESHOLD = 37.3f;
        public static boolean THERMAL_IMAGE_MIRROR = true;
        public static boolean THERMAL_F_ENABLED = false;
        public static String MAIN_LOGO_TEXT = "YBFACE";
        public static String MAIN_LOGO_IMG = "";
        public static boolean AUTO_TEMPER = false;
    }

}
