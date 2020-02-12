package com.yunbiao.vertical_scroll_player.listener;

import android.view.View;
import android.widget.TextView;

import com.yunbiao.vertical_scroll_player.model.PlayElement;

/***
 * 互动信息更新监听
 */
public interface InteractionUpdateListener {
    void updateView(PlayElement element, View playView, View good, TextView gooNum,View ivWatch, View share);
}
