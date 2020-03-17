package com.yunbiao.ybsmartcheckin_live_id.temp_cetificates;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;

public class CertificatesConst {


    public static final int CERTIFICATES_DEFAULT_MODE = Mode.CERTIFICATES_THERMAL;

    public static String[] models;
    static {
        models = APP.getContext().getResources().getStringArray(R.array.certificates_mode);
    }

    interface Mode {
        int CERTIFICATES_THERMAL = 0;//人证+热成像
        int CERTIFICATES_INFARED = 1;//人证+红外
        //        int CERTIFICATES_ONLY = 2;//仅人证
    }

    interface Key{
        String MODE = "certificates_mode";
        String LOW_TEMP = "certificatesLowTemp";
        String MIN_THRESHOLD = "certificatesMinThreshold";//最低播报
        String WARNING_THRESHOLD = "certificatesWarningThreshold";//体温报警值
    }

    interface Default{
        boolean LOW_TEMP = true;
    }
}
