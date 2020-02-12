package com.yunbiao.vertical_scroll_player.holder;


import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yunbiao.vertical_scroll_player.R;
import com.yunbiao.vertical_scroll_player.listener.InteractionUpdateListener;
import com.yunbiao.vertical_scroll_player.listener.PlaySet;
import com.yunbiao.vertical_scroll_player.model.PlayElement;

public abstract class MyViewHolder extends RecyclerView.ViewHolder {
    protected PlayElement element;

    protected View playView;

    protected View ivGood;
    protected View ivWatch;
    protected View ivShare;
    protected TextView tvGoodNum;
    protected TextView tvWatchNum;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    protected void findMainView(View itemView) {
        playView = itemView.findViewById(R.id.play_view);
        ivGood = itemView.findViewById(R.id.iv_good);
        tvGoodNum = itemView.findViewById(R.id.tv_good_num);
        ivWatch = itemView.findViewById(R.id.iv_watch);
        tvWatchNum = itemView.findViewById(R.id.tv_watch_num);
        ivShare = itemView.findViewById(R.id.iv_share);
    }

    public void bindData(PlayElement element) {
        this.element = element;

        tvGoodNum.setText("" + element.getGoodNum());
        tvWatchNum.setText("" + element.getWatchNum());
    }

    public void bindInteractionListener(InteractionUpdateListener listener) {
        listener.updateView(element, playView, ivGood, tvGoodNum, ivWatch, ivShare);
    }

    public void play(PlaySet set) {
        set.set(element, playView, tvWatchNum);
    }

    public void stop() {

    }

    public PlayElement getElement() {
        return element;
    }

}