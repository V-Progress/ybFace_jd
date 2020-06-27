package com.yunbiao.ybsmartcheckin_live_id.temper_5inch.databean;

import androidx.databinding.ObservableField;

public class MainDataBean {

    public final ObservableField<String> temperature;

    public final ObservableField<String> temperatureUnit;

    public final ObservableField<Integer> normalNum;

    public final ObservableField<Integer> abnormalNum;

    public final ObservableField<String> subtitleContent;

    public final ObservableField<Integer> tempStatus;

    public final ObservableField<Boolean> isTest;

    public final ObservableField<Float> cmMeasurementValue;

    public final ObservableField<Float> cmCalibrationValue;

    public final ObservableField<Float> cmBodyTemperatureValue;

    public MainDataBean(ObservableField<String> temperatureUnit, ObservableField<Integer> normalNum, ObservableField<Integer> abnormalNum) {
        this.temperature = new ObservableField<>("0.0");
        this.temperatureUnit = temperatureUnit;
        this.normalNum = normalNum;
        this.abnormalNum = abnormalNum;
        this.subtitleContent = new ObservableField<>("");
        this.tempStatus = new ObservableField<>(1);
        this.isTest = new ObservableField<>(false);
        cmMeasurementValue = new ObservableField<>();
        cmCalibrationValue = new ObservableField<>();
        cmBodyTemperatureValue = new ObservableField<>();
    }

    public ObservableField<String> getSubtitleContent() {
        return subtitleContent;
    }

    public ObservableField<String> getTemperature() {
        return temperature;
    }

    public ObservableField<String> getTemperatureUnit() {
        return temperatureUnit;
    }

    public ObservableField<Integer> getNormalNum() {
        return normalNum;
    }

    public ObservableField<Integer> getAbnormalNum() {
        return abnormalNum;
    }

    public ObservableField<Integer> getTempStatus() {
        return tempStatus;
    }

    public ObservableField<Boolean> getIsTest() {
        return isTest;
    }

    public ObservableField<Float> getCmMeasurementValue() {
        return cmMeasurementValue;
    }

    public ObservableField<Float> getCmCalibrationValue() {
        return cmCalibrationValue;
    }

    public ObservableField<Float> getCmBodyTemperatureValue() {
        return cmBodyTemperatureValue;
    }
}
