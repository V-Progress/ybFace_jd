package com.yunbiao.vertical_scroll_player.listener;

public interface OnViewPagerListener {
    /**
     * 初始化
     */
    void onInitComplete();

    /**
     * 释放
     */
    void onPageRelease(boolean isNext, int position);

    /**
     * 选中
     */
    void onPageSelected(int position, boolean isBottom);
}