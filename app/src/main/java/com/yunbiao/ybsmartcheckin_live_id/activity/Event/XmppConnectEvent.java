package com.yunbiao.ybsmartcheckin_live_id.activity.Event;

public class XmppConnectEvent {
    private boolean isConnected = false;

    public XmppConnectEvent(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }
}
