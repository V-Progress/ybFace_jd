package com.yunbiao.ybsmartcheckin_live_id.temper_5inch.utils;

public class TemperatureUnitUtils {

    public static float c2f(float c) {
        return BigDecimalUtils.add(BigDecimalUtils.mul(c, 1.8f, 1).floatValue(), 32, 1).floatValue();
    }

    public static float f2c(float f) {
        return BigDecimalUtils.div(BigDecimalUtils.sub(f, 32, 1).floatValue(), 1.8f, 1).floatValue();
    }

}
