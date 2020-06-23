package com.yunbiao.ybsmartcheckin_live_id.temper_5inch.databean;

import androidx.databinding.ObservableField;

public class SettingDataBean {

    public final ObservableField<String> calibrationContent;
    public final ObservableField<String> intervalContent;
    public final ObservableField<String> warningContent;

    public SettingDataBean(ObservableField<String> calibrationContent, ObservableField<String> intervalContent, ObservableField<String> warningContent) {
        this.calibrationContent = calibrationContent;
        this.intervalContent = intervalContent;
        this.warningContent = warningContent;
    }

    public ObservableField<String> getCalibrationContent() {
        return calibrationContent;
    }

    public ObservableField<String> getIntervalContent() {
        return intervalContent;
    }

    public ObservableField<String> getWarningContent() {
        return warningContent;
    }

}
