package com.yunbiao.ybsmartcheckin_live_id.activity_safety_check_double_light;

public class SafetyCheckDoubleLightConst {


    public interface Key{
        String THERMAL_MIRROR = "safetyCheckDoubleLightThermalMirror";
        String LOW_TEMP = "safetyCheckDoubleLightLowTemp";
        String CORRECT_VALUE = "safetyCheckDoubleLightCorrectValue";
        String BLACK_BODY_ENABLED = "safetyCheckDoubleLightBlackBodyEnabled";
        String AUTO_CALIBRATION = "safetyCheckDoubleLightAutoCalibration";
        String PRE_VALUE = "safetyCheckDoubleLightPreValue";
        String WARNING_THRESHOLD = "safetyCheckDoubleLightWarningThreshold";
        String BODY_TEMPER = "safetyCheckDoubleLightBodyTemper";
        String MIN_THRESHOLD = "safetyCheckDoubleLightMinThreshold";

        String BLACK_BODY_LEFT = "safetyCheckDoubleLightBlackBodyLeft";
        String BLACK_BODY_TOP = "safetyCheckDoubleLightBlackBodyTop";
        String BLACK_BODY_RIGHT = "safetyCheckDoubleLightBlackBodyRight";
        String BLACK_BODY_BOTTOM = "safetyCheckDoubleLightBlackBodyBottom";

        String BLACK_BODY_FRAME = "safetyCheckDoubleLightBlackBodyFrame";
        String LAST_MIN_T = "safetyCheckDoubleLightLastMinT";
    }

    public abstract static class Default{
        public static boolean THERMAL_MIRROR = false;
        public static boolean LOW_TEMP = false;
        public static float CORRECT_VALUE = 0.0f;
        public static boolean BLACK_BODY_ENABLED = false;
        public static boolean AUTO_CALIBRATION = false;
        public static int PRE_VALUE = 345;
        public static float WARNING_THRESHOLD = 37.3f;
        public static float BODY_TEMPER = 36.7f;
        public static float MIN_THRESHOLD = 35.5f;

        public static int BLACK_BODY_LEFT = 11;
        public static int BLACK_BODY_TOP = 11;
        public static int BLACK_BODY_RIGHT = 16;
        public static int BLACK_BODY_BOTTOM = 16;

        public static boolean BLACK_BODY_FRAME = false;
        public static float LAST_MIN_T = 0.0f;
    }
}
