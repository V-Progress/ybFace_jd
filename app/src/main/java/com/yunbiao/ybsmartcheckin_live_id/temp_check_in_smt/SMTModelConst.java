package com.yunbiao.ybsmartcheckin_live_id.temp_check_in_smt;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;

public class SMTModelConst {

    public static final int SMT_FACE_ONLY = 0;
    public static final int SMT_FACE_TEMP = 1;
    public static final int SMT_TEMP_ONLY = 2;

    public static final int DEFAULT_SMT_MODEL = SMT_TEMP_ONLY;

    public static String[] models = {"人脸识别模式", "人脸识别+测温模式", "测温模式"};

    static{
        models = APP.getContext().getResources().getStringArray(R.array.smt_models);
    }

    interface key{
        String LOW_TEMP = "lowTemp";
        String DISTANCE_TIP_ENABLE = "distanceTipEnable";
    }

    interface Default{
        boolean LOW_TEMP = true;
        boolean DISTANCE_TIP_ENABLE = true;
    }
}
