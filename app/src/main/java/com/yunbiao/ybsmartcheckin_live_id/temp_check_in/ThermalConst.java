package com.yunbiao.ybsmartcheckin_live_id.temp_check_in;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;

public class ThermalConst {


    public static final int THERMAL_FACE_ONLY = 0;
    public static final int THERMAL_FACE_TEMP = 1;
    public static final int THERMAL_TEMP_ONLY = 2;
    public static final int INFARED_ONLY = 3;
    public static final int INFARED_FACE = 4;
    public static final int CERTIFICATES = 5;

    public static final int DEFAULT_THERMAL_MODEL = THERMAL_TEMP_ONLY;

    public static String[] models = {"人脸识别模式", "人脸识别+热成像测温模式", "热成像测温模式", "红外测温模式", "人脸+红外测温模式", "人证比对模式"};

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
    }

    interface Default {
        boolean THERMAL_MIRROR = true;
        boolean LOW_TEMP = true;
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
    }

}
