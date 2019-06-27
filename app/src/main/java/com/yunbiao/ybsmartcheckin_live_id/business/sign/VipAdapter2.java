package com.yunbiao.ybsmartcheckin_live_id.business.sign;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.db.SignBean;

import java.util.LinkedList;
import java.util.List;

public class VipAdapter2 extends RecyclerView.Adapter<VipAdapter2.VH>{
    private List<SignBean> mList ;
    private Context mContext;
    public VipAdapter2(Context context, LinkedList<SignBean> list) {
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
        SignBean item = mList.get(i);
        String imgUrl = item.getImgUrl();
        if(!TextUtils.isEmpty(imgUrl)){
            Bitmap bitmap = BitmapFactory.decodeFile(imgUrl);
            vh.ivHead.setImageBitmap(bitmap);
        }
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