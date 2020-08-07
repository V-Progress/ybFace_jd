package com.yunbiao.ybsmartcheckin_live_id.temper_12inch.databean;

import android.graphics.Bitmap;

import androidx.databinding.ObservableField;

public class Setting12InchDataBean {

    public final ObservableField<String> calibrationContent;
    public final ObservableField<String> warningContent;

    public final ObservableField<Boolean> serverInfoEnabled;
    public final ObservableField<String> serverIp;
    public final ObservableField<String> serverResPort;
    public final ObservableField<String> serverXmppPort;
    public final ObservableField<String> serverProName;

    public final ObservableField<String> versionName;
    public final ObservableField<String> versionInfo;

    public final ObservableField<String> deviceNo;
    public final ObservableField<String> bindCode;
    public final ObservableField<String> companyName;
    public final ObservableField<String> netStateInfo;

    public final ObservableField<Bitmap> hotImageBitmap;

    public final ObservableField<Boolean> isCM;

    public final ObservableField<Float> cmMeasurementValue;
    public final ObservableField<Float> cmCalibrationValue;
    public final ObservableField<Float> cmBodyTemperatureValue;

    public Setting12InchDataBean(ObservableField<String> calibrationContent, ObservableField<String> warningContent) {
        this.calibrationContent = calibrationContent;
        this.warningContent = warningContent;
        serverInfoEnabled = new ObservableField<>(false);
        serverIp = new ObservableField<>("");
        serverResPort = new ObservableField<>("");
        serverXmppPort = new ObservableField<>("");
        serverProName = new ObservableField<>("");
        versionName = new ObservableField<>("");
        versionInfo = new ObservableField<>("");
        deviceNo = new ObservableField<>("");
        bindCode = new ObservableField<>("");
        companyName = new ObservableField<>("");
        netStateInfo = new ObservableField<>("");

        hotImageBitmap = new ObservableField<>();

        isCM = new ObservableField<>(false);

        cmMeasurementValue = new ObservableField<>();
        cmCalibrationValue = new ObservableField<>();
        cmBodyTemperatureValue = new ObservableField<>();
    }

    public ObservableField<String> getCalibrationContent() {
        return calibrationContent;
    }

    public ObservableField<String> getWarningContent() {
        return warningContent;
    }

    public ObservableField<Boolean> getServerInfoEnabled() {
        return serverInfoEnabled;
    }

    public ObservableField<String> getServerIp() {
        return serverIp;
    }

    public ObservableField<String> getServerResPort() {
        return serverResPort;
    }

    public ObservableField<String> getServerXmppPort() {
        return serverXmppPort;
    }

    public ObservableField<String> getServerProName() {
        return serverProName;
    }

    public ObservableField<String> getVersionName() {
        return versionName;
    }

    public ObservableField<String> getVersionInfo() {
        return versionInfo;
    }

    public ObservableField<String> getDeviceNo() {
        return deviceNo;
    }

    public ObservableField<String> getBindCode() {
        return bindCode;
    }

    public ObservableField<String> getCompanyName() {
        return companyName;
    }

    public ObservableField<String> getNetStateInfo() {
        return netStateInfo;
    }

    public ObservableField<Bitmap> getHotImageBitmap() {
        return hotImageBitmap;
    }

    public ObservableField<Boolean> getIsCM() {
        return isCM;
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
