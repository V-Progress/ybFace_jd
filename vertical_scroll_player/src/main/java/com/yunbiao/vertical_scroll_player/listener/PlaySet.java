package com.yunbiao.vertical_scroll_player.listener;

import android.view.View;
import android.widget.TextView;

import com.yunbiao.vertical_scroll_player.model.PlayElement;

public interface PlaySet {
    void set(PlayElement element, View playView, TextView tvWatch);
}