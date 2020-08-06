package com.yunbiao.ybsmartcheckin_live_id.temper_12inch.databean;

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

}
