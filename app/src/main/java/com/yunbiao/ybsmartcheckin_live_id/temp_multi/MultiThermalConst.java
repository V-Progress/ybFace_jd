package com.yunbiao.ybsmartcheckin_live_id.temp_multi;

public class MultiThermalConst {

    public interface Key{
        String THERMAL_MIRROR = "multiThermalMirror";
        String LOW_TEMP = "multiThermalLowTemp";
        String WARNING_TEMP = "multiThermalWarningTemp";
        String BODY_CORRECT_TEMPER = "multiThermalBodyTemper";
        String CORRECT_AREA_JSON = "multiThermalCorrectAreaJson";
    }

    public interface Default{
        boolean THERMAL_MIRROR = false;
        boolean LOW_TEMP = true;
        float WARNING_TEMP = 37.3f;
        float BODY_CORRECT_TEMPER = 0.0f;
        String CORRECT_AREA_JSON = "{\"bottom\":5,\"left\":0,\"right\":5,\"top\":0}";
    }
}
