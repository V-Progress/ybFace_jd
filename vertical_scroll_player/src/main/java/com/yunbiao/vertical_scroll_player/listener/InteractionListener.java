package com.yunbiao.vertical_scroll_player.listener;

import android.view.View;
import android.widget.TextView;

import com.yunbiao.vertical_scroll_player.model.PlayElement;

/***
 * 互动监听
 */
public interface InteractionListener {
    void clickView(PlayElement element, View playView);

    void clickThumbsUp(PlayElement element, View thumbsUpView, TextView thumbsUpNumber);

    void clickWatch(PlayElement element, View ivWatch);

    void clickShare(PlayElement element, View thumbsUpView);
}
