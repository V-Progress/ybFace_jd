package com.yunbiao.ybsmartcheckin_live_id.activity.Event;

public class AdsStateEvent {
    public static final int STATE_OPENED = 1;
    public static final int STATE_CLOSED = 0;
    public int state;

    public AdsStateEvent(int state) {
        this.state = state;
    }
}
