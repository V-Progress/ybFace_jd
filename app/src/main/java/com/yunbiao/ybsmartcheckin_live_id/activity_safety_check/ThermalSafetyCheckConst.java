package com.yunbiao.ybsmartcheckin_live_id.activity_safety_check;

public class ThermalSafetyCheckConst {


    public interface Key{
        String WARNING_TEMPER = "thermalSafetyCheckWarningTemper";
        String THERMAL_MIRROR = "thermalSafetyCheckThermalMirror";
        String LOW_TEMP = "thermalSafetyCheckLowTemp";


        String BLACK_BODY_LEFT = "thermalSafetyCheckBlackBodyLeft";
        String BLACK_BODY_TOP = "thermalSafetyCheckBlackBodyTop";
        String BLACK_BODY_RIGHT = "thermalSafetyCheckBlackBodyRight";
        String BLACK_BODY_BOTTOM = "thermalSafetyCheckBlackBodyBottom";

        String TEMPER_AREA_SIZE = "thermalSafetyTemperAreaSize";

        String WARNING_NUMBER = "thermalSafetyCheckWarningNumber";
        String NORMAL_TEMPER = "thermalSafetyCheckNormalTemper";

        String DATE_FOR_WARNGNING_NUMBER = "thermalSafetyCheckDateForWarningNumber";
        String CORRECT_VALUE = "thermalSafetyCheckCorrectionValue";
        String BLACK_BODY_ENABLED = "thermalSafetyCheckBlackBodyEnabled";

        String TEMPER_FRAME = "thermalSafetyCheckTemperFrame";

        String BODY_TEMPER = "thermalSafetyCheckBodyTemper";

        String AUTO_CALIBRATION = "thermalSafetyCheckAutoCalibration";

        String LAST_MINT = "thermalSafetyCheckDateAndMinT";

        String IMMEDIATE_REPORT_MODE = "thermalImmediateReportMode";

        String THERMAL_F_ENABLED = "thermalSafetyCheckFEnabled";
    }

    public interface Default{
        float WARNING_TEMPER = 37.3f;//
        boolean THERMAL_MIRROR = false;//
        boolean LOW_TEMP = true;//

        int BLACK_BODY_LEFT = 11;
        int BLACK_BODY_TOP = 11;
        int BLACK_BODY_RIGHT = 16;
        int BLACK_BODY_BOTTOM = 16;

        int TEMPER_AREA_SIZE = Size.SMALL;

        long WARNING_NUMBER = 0;
        float NORMAL_TEMPER = 35.0f;//
        float CORRECT_VALUE = 0.0f;
        boolean BLACK_BODY_ENABLED = false;

        boolean TEMPER_FRAME = false;

        float BODY_TEMPER = 36.5f;

        boolean AUTO_CALIBRATION = true;

        float LAST_MINT = 0.0f;

        boolean IMMEDIATE_REPORT_MODE = false;

        boolean THERMAL_F_ENABLED = false;
    }

    public interface Size{
        int SMALL = 0;
        int MIDDLE = 1;
        int LARGE = 2;
        int TOO_SMALL = -1;
    }
}
