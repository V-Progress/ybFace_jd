package com.yunbiao.vertical_scroll_player.holder;

import android.view.View;

import androidx.annotation.NonNull;

import com.yunbiao.vertical_scroll_player.custom.TextureVideoView;

public class VideoHolder extends MyViewHolder {
    public VideoHolder(@NonNull View itemView) {
        super(itemView);
        findMainView(itemView);
    }

    @Override
    public void stop() {
        ((TextureVideoView) playView).stopPlayback();
    }
}
