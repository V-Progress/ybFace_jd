package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in_smt;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;

public class SMTModelConst {

    public static final int SMT_FACE_ONLY = 0;
    public static final int SMT_FACE_TEMP = 1;
    public static final int SMT_TEMP_ONLY = 2;

    public static String[] models = {"人脸识别模式", "人脸识别+测温模式", "测温模式"};

    static{
        models = APP.getContext().getResources().getStringArray(R.array.smt_models);
    }

    interface key{
        String LOW_TEMP = "lowTemp";
        String DISTANCE_TIP_ENABLE = "distanceTipEnable";
        String F_ENABLED = "smtFEnabled";
        String SPEECH_DELAY = "speechDelay";
        String TEMP_MIN_THRESHOLD = "tempMinThreshold";
        String TEMP_WARNING_THRESHOLD = "tempWarningThreshold";
        String TEMP_CORRECT_VALUE = "tempCorrectValue";
        String AMB_CORRECT_VALUE = "ambCorrectValue";
        String MODE = "smt_model_setting";
        String NORMAL_BROADCAST = "smtNormalBroadcast";
        String WARNING_BROADCAST = "smtWarningBroadcast";
    }

    interface Default{
        boolean LOW_TEMP = true;
        boolean DISTANCE_TIP_ENABLE = true;
        boolean F_ENABLED = false;
        long SPEECH_DELAY = 5000;
        float TEMP_MIN_THRESHOLD = 35.5F;
        float TEMP_WARNING_THRESHOLD = 37.3f;
        float TEMP_CORRECT_VALUE= 0.0f;
        float AMB_CORRECT_VALUE= 0.0f;
        int MODE = SMT_TEMP_ONLY;
        String  NORMAL_BROADCAST = "";
        String WARNING_BROADCAST = "";
    }
}
