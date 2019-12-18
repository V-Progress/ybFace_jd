package com.yunbiao.ybsmartcheckin_live_id.business.sign;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.db2.User;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.util.LinkedList;
import java.util.List;

public class VipAdapter2 extends RecyclerView.Adapter<VipAdapter2.VH> {
    private List<Sign> mList;
    private Context mContext;

    public VipAdapter2(Context context, LinkedList<Sign> list) {
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
        Sign item = mList.get(i);
//        byte[] imgBytes = item.getImgBytes();
//        if (imgBytes != null) {
//            Bitmap bitmap = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);
//            vh.ivHead.setImageBitmap(bitmap);
//        }
        Glide.with(mContext).load(item.getHeadPath()).asBitmap().into(vh.ivHead);
        vh.tvName.setText(item.getName());
        int type = item.getType();
        if (type == -2) {
            vh.tvSign.setTextColor(Color.RED);
            vh.tvSign.setText("\n" + item.getAutograph());
        } else if (type == -1) {
            vh.tvSign.setTextColor(Color.GREEN);
            long visEntryId = item.getVisEntryId();
            String signText = "\n" + item.getAutograph();
            User user = DaoManager.get().queryUserById(visEntryId);
            if(user != null){
                signText += "\n访问：" + user.getName();
            }
            vh.tvSign.setText(signText);
        } else {
            vh.tvSign.setTextColor(Color.WHITE);
            int isShowJob = SpUtils.getInt(SpUtils.DISPLAYPOSITION);
            vh.tvSign.setText((isShowJob == 0 ? item.getPosition() : "") + " \n  \n " + item.getAutograph());
        }
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public class VH extends RecyclerView.ViewHolder {
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