package com.yunbiao.ybsmartcheckin_live_id.activity.Event;

public class UpdateQRCodeEvent {
    private String localPath;

    public UpdateQRCodeEvent(String localPath) {
        this.localPath = localPath;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
}
