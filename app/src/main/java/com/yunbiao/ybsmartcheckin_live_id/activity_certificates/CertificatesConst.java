package com.yunbiao.ybsmartcheckin_live_id.activity_certificates;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;

public class CertificatesConst {


    public static final int CERTIFICATES_DEFAULT_MODE = Mode.CERTIFICATES_THERMAL;

    public static String[] models;
    static {
        models = APP.getContext().getResources().getStringArray(R.array.certificates_mode);
    }

    interface Mode {
        int CERTIFICATES_THERMAL = 0;//人证+热成像（32*32）
        int CERTIFICATES_THERMAL_16_4 = 1;//人证+热成像（16*4）
    }

    public interface Key{
        String MODE = "certificates_mode";//模式
        String LOW_TEMP = "certificatesLowTemp";//低温模式
        String MIN_THRESHOLD = "certificatesMinThreshold";//最低播报
        String WARNING_THRESHOLD = "certificatesWarningThreshold";//体温报警值
        String CORRECT_VALUE = "certificatesCorrectValue";//矫正
        String THERMAL_MIRROR = "certificatesThermalMirror";//热成像镜像
        String SIMILAR = "certificatesSimilar";
    }

    public interface Default{
        int MODE = Mode.CERTIFICATES_THERMAL;
        boolean LOW_TEMP = true;
        float MIN_THRESHOLD = 35.5f;
        float WARNING_THRESHOLD = 37.3f;
        float CORRECT_VALUE = 0.0f;
        boolean THERMAL_MIRROR = true;
        int SIMILAR = 50;
    }
}
