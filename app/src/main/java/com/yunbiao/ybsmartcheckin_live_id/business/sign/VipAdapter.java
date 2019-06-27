package com.yunbiao.ybsmartcheckin_live_id.business.sign;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.db.VIPDetail;

import java.util.LinkedList;
import java.util.List;

public class VipAdapter extends RecyclerView.Adapter<VipAdapter.VH>{
    private List<VIPDetail> mList ;
    private Context mContext;
    public VipAdapter(Context context, LinkedList<VIPDetail> list) {
        mContext = context;
        mList = list;
    }

    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.dialog_vip_item_test, viewGroup, false);
        return new VH(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull VH vh, int i) {
        VIPDetail item = mList.get(i);
        String imgUrl = item.getImgUrl();
        Glide.with(mContext).load(imgUrl).asBitmap().into(vh.ivHead);
        vh.tvName.setText(item.getName());
        vh.tvSign.setText(item.getSignature());
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public class VH extends RecyclerView.ViewHolder{
        public ImageView ivHead;
        public TextView tvName;
        public TextView tvSign;
        public VH(@NonNull View itemView) {
            super(itemView);
            ivHead = itemView.findViewById(R.id.civ_userPhoto);
            tvName = itemView.findViewById(R.id.tv_nameAndJob);
            tvSign = itemView.findViewById(R.id.tv_sign);
        }
    }
}