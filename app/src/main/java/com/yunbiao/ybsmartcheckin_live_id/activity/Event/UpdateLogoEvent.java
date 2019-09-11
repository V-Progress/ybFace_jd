package com.yunbiao.ybsmartcheckin_live_id.activity.Event;

/***
 * logo更新事件
 */
public class UpdateLogoEvent {
    String logoPath;

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public UpdateLogoEvent(String logoPath) {
        this.logoPath = logoPath;
    }

    @Override
    public String toString() {
        return "UpdateLogoEvent{" +
                "logoPath='" + logoPath + '\'' +
                '}';
    }
}
