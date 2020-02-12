package com.yunbiao.vertical_scroll_player.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yunbiao.vertical_scroll_player.R;
import com.yunbiao.vertical_scroll_player.VerticalPlayer;
import com.yunbiao.vertical_scroll_player.holder.ImageHolder;
import com.yunbiao.vertical_scroll_player.holder.MyViewHolder;
import com.yunbiao.vertical_scroll_player.holder.VideoHolder;
import com.yunbiao.vertical_scroll_player.model.PlayElement;

import java.util.List;

public class PlayViewAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private List<PlayElement> playList;
    private boolean isInfiniteScroll = false;

    public PlayViewAdapter(List<PlayElement> playList) {
        this.playList = playList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if (viewType == VerticalPlayer.TYPE_IMG) {
            View view = View.inflate(parent.getContext(), R.layout.item_image, null);
            view.setLayoutParams(layoutParams);
            return new ImageHolder(view);
        } else {
            View view = View.inflate(parent.getContext(), R.layout.item_video, null);
            view.setLayoutParams(layoutParams);
            return new VideoHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        position = getRealPosition(position);
        holder.bindData(playList.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        position = getRealPosition(position);
        return playList.get(position).getResourceType();
    }

    @Override
    public int getItemCount() {
        return isInfiniteScroll ? Integer.MAX_VALUE : playList.size();
    }

    private int getRealPosition(int position){
        return isInfiniteScroll ? position % playList.size() : position;
    }
}
