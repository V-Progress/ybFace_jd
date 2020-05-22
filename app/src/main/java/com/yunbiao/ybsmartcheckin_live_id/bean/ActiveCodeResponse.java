package com.yunbiao.ybsmartcheckin_live_id.bean;

public class ActiveCodeResponse {

    String activateCode;
    String appid;
    String sdk_key;
    String message;
    int status;

    @Override
    public String toString() {
        return "ActiveResponse{" +
                "activateCode='" + activateCode + '\'' +
                ", appid='" + appid + '\'' +
                ", sdk_key='" + sdk_key + '\'' +
                ", message='" + message + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public String getActivateCode() {
        return activateCode;
    }

    public void setActivateCode(String activateCode) {
        this.activateCode = activateCode;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getSdk_key() {
        return sdk_key;
    }

    public void setSdk_key(String sdk_key) {
        this.sdk_key = sdk_key;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
