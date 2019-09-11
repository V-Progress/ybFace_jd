package com.yunbiao.ybsmartcheckin_live_id.activity.fragment.child;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.InformationFragment;

import java.util.ArrayList;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter {
    private static final String TAG = "MediaAdapter";
    private final Activity activity;
    private List<InformationFragment.PlayBean.PathBean> pathBeans;

    public MediaAdapter(Activity fragmentActivity, ArrayList<InformationFragment.PlayBean.PathBean> datas, RecyclerView rlv, int time) {
        activity = fragmentActivity;
        pathBeans = datas;
    }

    public int getType(int position) {
        return pathBeans.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MediaViewHolder(activity, LayoutInflater.from(activity).inflate(R.layout.item_video, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ((MediaViewHolder) viewHolder).bindData(pathBeans.get(i));
    }

    @Override
    public int getItemCount() {
        return pathBeans.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}