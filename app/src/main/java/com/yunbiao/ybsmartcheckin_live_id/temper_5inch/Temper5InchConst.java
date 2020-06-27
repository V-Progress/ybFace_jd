package com.yunbiao.ybsmartcheckin_live_id.temper_5inch;

public class Temper5InchConst {

    public interface Key{
        String SOFTWARE_LANGUAGE = "softwareLanguage";
        String TEMPERATURE_UNIT = "temperatureUnit";
        String CALIBRATION_VALUE = "calibrationValue";
        String CAROUSEL_INTERVAL = "carouselInterval";
        String SUBTITLE_SPEED = "subtitleSpeed";
        String WARNING_VALUE = "warningValue";
    }

    public interface Default{
        String softwareLanguageDef = "zh_CH_Simplified";
        int temperatureUnitDef = 1;
        float calibrationValueDef = 0;
        int carouselIntervalDef = 5;
        int subtitleSpeedDef = 2;
        float warningValueDef = 37.3f;
    }

    public static final float stableLowerLimit = 35.0f;
    public static final float stableUpperLimit = 37.0f;

    public static float[] stableTemp = new float[]{
            36.2f,
            36.3f, 36.3f, 36.3f, 36.3f, 36.3f, 36.3f, 36.3f, 36.3f,
            36.4f, 36.4f, 36.4f, 36.4f, 36.4f, 36.4f, 36.4f, 36.4f,
            36.5f, 36.5f, 36.5f, 36.5f, 36.5f, 36.5f, 36.5f, 36.5f,
            36.6f, 36.6f, 36.6f, 36.6f, 36.6f, 36.6f, 36.6f, 36.6f,
            36.7f,
            36.8f,
    };

    public static float defaultBodyValue = 36.5f;
}
