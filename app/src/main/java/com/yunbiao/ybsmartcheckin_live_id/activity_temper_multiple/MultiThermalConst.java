package com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple;

public class MultiThermalConst {

    public interface Key{
        String THERMAL_MIRROR = "multiThermalMirror";
        String LOW_TEMP = "multiThermalLowTemp";
        String WARNING_TEMP = "multiThermalWarningTemp";
        String CORRECT_VALUE = "multiThermalCorrectValue";

        String CORRECT_AREA_LEFT = "multiThermalCorrectAreaLeft";
        String CORRECT_AREA_TOP = "multiThermalCorrectAreaTop";
        String CORRECT_AREA_RIGHT = "multiThermalCorrectAreaRight";
        String CORRECT_AREA_BOTTOM = "multiThermalCorrectAreaBottom";

        String MI320_CORRECT_AREA_LEFT = "mi320MultiThermalCorrectAreaLeft";
        String MI320_CORRECT_AREA_TOP = "mi320MultiThermalCorrectAreaTop";
        String MI320_CORRECT_AREA_RIGHT = "mi320MultiThermalCorrectAreaRight";
        String MI320_CORRECT_AREA_BOTTOM = "mi320MultiThermalCorrectAreaBottom";

        String THERMAL_FACE_FRAME = "multiThermalFaceFrame";

        String BLACK_BODY_FRAME = "multiThermalBlackBodyFrame";

        String MULTI_TRACK = "multiThermalMultiTrack";

        String BLACK_BODY_PRE_VALUE = "multiThermalBlackBodyPreValue";

        String BLACK_BODY_ENABLE = "multiThermalBlackBodyEnable";

        String BODY_TEMPER = "multiThermalBodyTemper";
        String LAST_MIN_T = "multiThermalLastMinT";

        String THERMAL_F_ENABLED = "fEnabled";

        String NORMAL_RANGE_START = "normalRangeStart";
        String NORMAL_RANGE_END = "normalRangeEnd";

        String MULTI_TEMPER_MODULE = "multiTemperModule";
    }

    public interface Default{
        boolean THERMAL_MIRROR = false;//热成像镜像
        boolean LOW_TEMP = false;//低温模式
        float WARNING_TEMP = 37.3f;
        float BODY_CORRECT_TEMPER = 0.0f;

        int CORRECT_AREA_LEFT = 0;
        int CORRECT_AREA_TOP = 0;
        int CORRECT_AREA_RIGHT = 5;
        int CORRECT_AREA_BOTTOM = 5;

        int MI320_CORRECT_AREA_LEFT = 0;
        int MI320_CORRECT_AREA_TOP = 0;
        int MI320_CORRECT_AREA_RIGHT = 20;
        int MI320_CORRECT_AREA_BOTTOM = 20;

        boolean THERMAL_FACE_FRAME = false;//热成像人脸框
        boolean BLACK_BODY_FRAME = false;//黑体框

        boolean MULTI_TRACK = false;//多次抓取

        int BLACK_BODY_PRE_VALUE = 365;//黑体预设值

        boolean BLACK_BODY_ENABLE = false;//黑体开关

        float BODY_TEMPER = 36.5f;//预设体温
        float LAST_MIN_T = 0.0f;//最近一次的最低温

        boolean THERMAL_F_ENABLED = false;

        boolean LIVENESS_ENABLED = false;

        float NORMAL_RANGE_START = 35.0f;
        float NORMAL_RANGE_END = 37.2f;
    }

    public interface TemperModuleType {
        int K6080 = 101;
        int MI320 = 102;
    }
}
