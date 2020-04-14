package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;

public class ThermalConst {


    public static final int FACE_ONLY = 0;//仅人脸模式
    public static final int FACE_THERMAL = 1;//人脸+热成像
    public static final int THERMAL_ONLY = 2;//仅热成像
    public static final int INFRARED_ONLY = 3;//仅红外
    public static final int FACE_INFRARED = 4;//人脸+红外
    public static final int THERMAL_16_4_ONLY = 5;//仅热成像16*4
    public static final int FACE_THERMAL_16_4 = 6;//人脸+热成像16*4

    public static final int DEFAULT_THERMAL_MODEL = THERMAL_ONLY;

    public static String[] models;

    static {
        models = APP.getContext().getResources().getStringArray(R.array.models);
    }

    interface Key {
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
    }

    interface Default {
        boolean THERMAL_MIRROR = true;
        boolean LOW_TEMP = false;
        float AMBIENT_CORRECT = 25.0f;
        float THERMAL_CORRECT = 0.0f;
        String NORMAL_BROADCAST = "";
        String WARNING_BROADCAST = "";
        boolean PERSON_FRAME = true;
        boolean DISTANCE_TIP = true;
        long SPEECH_DELAY = 5000;
        float TEMP_MIN_THRESHOLD = 35.5f;
        float TEMP_WARNING_THRESHOLD = 37.3f;
        boolean THERMAL_IMAGE_MIRROR = true;
        boolean THERMAL_F_ENABLED = false;
        boolean SHOW_DIALOG = false;
    }

}
