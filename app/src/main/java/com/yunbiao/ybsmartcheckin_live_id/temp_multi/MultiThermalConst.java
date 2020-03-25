package com.yunbiao.ybsmartcheckin_live_id.temp_multi;

public class MultiThermalConst {

    public interface Key{
        String THERMAL_MIRROR = "multiThermalMirror";
        String LOW_TEMP = "multiThermalLowTemp";
        String WARNING_TEMP = "multiThermalWarningTemp";
    }

    public interface Default{
        boolean THERMAL_MIRROR = false;
        boolean LOW_TEMP = true;
        float WARNING_TEMP = 37.3f;
    }
}
