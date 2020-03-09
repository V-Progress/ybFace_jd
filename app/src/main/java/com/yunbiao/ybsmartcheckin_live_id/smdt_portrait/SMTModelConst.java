package com.yunbiao.ybsmartcheckin_live_id.smdt_portrait;

public class SMTModelConst {

    public static final int SMT_FACE_ONLY = 0;
    public static final int SMT_FACE_TEMP = 1;
    public static final int SMT_TEMP_ONLY = 2;

    public static final int DEFAULT_SMT_MODEL = SMT_TEMP_ONLY;

    public static final String[] models = {"人脸识别模式", "人脸识别+测温模式", "测温模式"};
}
