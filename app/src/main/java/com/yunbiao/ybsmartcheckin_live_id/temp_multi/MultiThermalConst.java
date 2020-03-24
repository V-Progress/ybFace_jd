package com.yunbiao.ybsmartcheckin_live_id.temp_multi;

public class MultiThermalConst {

    public interface Key{
        String THERMAL_MIRROR = "multiThermalMirror";
        String LOW_TEMP = "multiThermalLowTemp";
    }

    public interface Default{
        boolean THERMAL_MIRROR = false;
        boolean LOW_TEMP = true;
    }
}
