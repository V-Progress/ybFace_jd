package com.yunbiao.vertical_scroll_player.listener;

import android.widget.TextView;

import com.yunbiao.vertical_scroll_player.model.PlayElement;

public interface UpdateWatchListener {
    void update(PlayElement element, TextView tvWatchNumber);
}
