package com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple;

public class MultiThermalConst {

    public interface Key{
        String THERMAL_MIRROR = "multiThermalMirror";
        String LOW_TEMP = "multiThermalLowTemp";
        String WARNING_TEMP = "multiThermalWarningTemp";
        String BODY_CORRECT_TEMPER = "multiThermalBodyTemper";

        String CORRECT_AREA_LEFT = "multiThermalCorrectAreaLeft";
        String CORRECT_AREA_TOP = "multiThermalCorrectAreaTop";
        String CORRECT_AREA_RIGHT = "multiThermalCorrectAreaRight";
        String CORRECT_AREA_BOTTOM = "multiThermalCorrectAreaBottom";

        String THERMAL_FACE_FRAME = "multiThermalFaceFrame";

        String BLACK_BODY_FRAME = "multiThermalBlackBodyFrame";
    }

    public interface Default{
        boolean THERMAL_MIRROR = false;
        boolean LOW_TEMP = true;
        float WARNING_TEMP = 37.3f;
        float BODY_CORRECT_TEMPER = 0.0f;

        int CORRECT_AREA_LEFT = 0;
        int CORRECT_AREA_TOP = 0;
        int CORRECT_AREA_RIGHT = 5;
        int CORRECT_AREA_BOTTOM = 5;

        boolean THERMAL_FACE_FRAME = false;
        boolean BLACK_BODY_FRAME = false;
    }
}
