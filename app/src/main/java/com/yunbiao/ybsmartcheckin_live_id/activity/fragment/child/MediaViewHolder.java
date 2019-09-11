package com.yunbiao.ybsmartcheckin_live_id.activity.fragment.child;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.InformationFragment;

class MediaViewHolder extends RecyclerView.ViewHolder{
    private final VideoView vvInfo;
    private final ImageView ivInfo;
    private Activity mActivity;

    public MediaViewHolder(Activity act, @NonNull View itemView) {
        super(itemView);
        mActivity = act;
        ivInfo = itemView.findViewById(R.id.iv_info);
        vvInfo = itemView.findViewById(R.id.vv_info);
    }

    public void bindData(InformationFragment.PlayBean.PathBean pathBean){
        String localPath = pathBean.getLocalPath();
        if(TextUtils.isEmpty(localPath)){
            localPath = pathBean.getUrl();
        }
        boolean isImg = pathBean.getType() == InformationFragment.PlayBean.PathBean.TYPE_IMG;
        if (isImg) {
            ivInfo.setVisibility(View.VISIBLE);
            vvInfo.setVisibility(View.GONE);
            vvInfo.setTag(isImg);
            Glide.with(mActivity).load(localPath).asBitmap().into(ivInfo);
        } else {
            ivInfo.setVisibility(View.GONE);
            vvInfo.setVisibility(View.VISIBLE);
            vvInfo.setVideoPath(localPath);
        }
    }

}