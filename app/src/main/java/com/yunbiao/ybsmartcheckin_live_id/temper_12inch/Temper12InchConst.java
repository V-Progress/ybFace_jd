package com.yunbiao.ybsmartcheckin_live_id.temper_12inch;

public class Temper12InchConst {

    public interface Key{
        String TEMPERATURE_UNIT = "temperatureUnit";
        String CALIBRATION_VALUE = "calibrationValue";
        String WARNING_VALUE = "warningValue";
    }

    public interface Default{
        int temperatureUnitDef = 1;
        float calibrationValueDef = 0;
        float warningValueDef = 37.3f;
    }

    public static float defaultBodyValue = 36.5f;

}
