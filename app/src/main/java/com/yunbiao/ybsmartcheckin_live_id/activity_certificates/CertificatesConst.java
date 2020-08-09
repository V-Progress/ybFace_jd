package com.yunbiao.ybsmartcheckin_live_id.activity_certificates;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;

public class CertificatesConst {

    public static String[] readers;
    public static String[] models;
    static {
        readers = APP.getContext().getResources().getStringArray(R.array.certificates_reader);
        models = APP.getContext().getResources().getStringArray(R.array.certificates_mode);
    }

    public static final String NET_READER_IP = "yjm.zrgk.com.cn";
    public static final int NET_READER_PORT = 8222;
    public static final String NET_READER_APP_ID = "IFUHAIU6XXJW";


    public interface Mode {
        int CERTIFICATES_THERMAL = 0;//人证+热成像（32*32）
        int CERTIFICATES_THERMAL_16_4 = 1;//人证+热成像（16*4）
    }

    public interface Reader{
        int LOCAL_READER = 0;
        int NET_READER = 1;
    }

    public interface Key{
        String READER = "certificates_reader";//读卡模块
        String MODE = "certificates_mode";//模式
        String LOW_TEMP = "certificatesLowTemp";//低温模式
        String MIN_THRESHOLD = "certificatesMinThreshold";//最低播报
        String WARNING_THRESHOLD = "certificatesWarningThreshold";//体温报警值
        String CORRECT_VALUE = "certificatesCorrectValue";//矫正
        String THERMAL_MIRROR = "certificatesThermalMirror";//热成像镜像
        String SIMILAR = "certificatesSimilar";
        String WHITE_LIST = "certificatesWhiteList";
        String USB_PRINTER_ENABLED = "certificatesPrinterEnabled";
        String IC_CARD_MODE = "certificatesICCardMode";
        String COLLECT_PHONE_ENABLED = "certificatesCollectPhoneEnabled";
        String TEMPERATURE_ENABLED = "certificatesTemperatureEnabled";
    }

    public static class Default{
        public static final boolean TEMPERATURE_ENABLED = true;
        public static int READER = Reader.LOCAL_READER;
        public static final boolean USB_PRINTER_ENABLED = false;//打印机
        public static final boolean COLLECT_PHONE_ENABLED = false;//收集手机号
        public static int MODE = Mode.CERTIFICATES_THERMAL;
        public static boolean LOW_TEMP = true;
        public static float MIN_THRESHOLD = 35.5f;
        public static float WARNING_THRESHOLD = 37.3f;
        public static float CORRECT_VALUE = 0.0f;
        public static boolean THERMAL_MIRROR = true;
        public static int SIMILAR = 50;
        public static boolean WHITE_LIST = false;
        public static boolean IC_CARD_MODE = false;
    }
}
