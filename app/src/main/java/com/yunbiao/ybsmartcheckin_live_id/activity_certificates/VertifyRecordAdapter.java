package com.yunbiao.ybsmartcheckin_live_id.activity_certificates;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.db2.VertifyRecord;
import com.yunbiao.ybsmartcheckin_live_id.utils.IDCardReader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VertifyRecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private List<VertifyRecord> vertifyRecordList;
    private Context mContext;

    public VertifyRecordAdapter(List<VertifyRecord> vertifyRecordList, Context mContext) {
        this.vertifyRecordList = vertifyRecordList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vertify_record,parent,false);
        return new VertifyViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        VertifyViewHolder vvh = (VertifyViewHolder) holder;
        vvh.bindData(position,vertifyRecordList.get(position));
    }

    @Override
    public int getItemCount() {
        return vertifyRecordList != null ? vertifyRecordList.size() : 0;
    }

    private float warningThreshold;
    private int similarThreshold;
    public void setTemperThreshold(Float warningThreshold) {
        this.warningThreshold = warningThreshold;
    }
    public void setSimilarThreshold(int similar) {
        similarThreshold = similar;
    }

    class VertifyViewHolder extends RecyclerView.ViewHolder {
        TextView tvIndex;
        TextView tvTime;
        TextView tvName;
        TextView tvNation;
        TextView tvTemper;
        TextView tvSimilar;
        ImageView ivIdCardHead;
        ImageView ivPersonHead;
        public VertifyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tv_index);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvName = itemView.findViewById(R.id.tv_name);
            tvNation = itemView.findViewById(R.id.tv_nation);
            tvTemper = itemView.findViewById(R.id.tv_temper);
            tvSimilar = itemView.findViewById(R.id.tv_similar);
            ivIdCardHead = itemView.findViewById(R.id.iv_idcard_head);
            ivPersonHead = itemView.findViewById(R.id.iv_person_head);
        }
        public void bindData(int position,VertifyRecord vertifyRecord){
            tvIndex.setText((position + 1) + "");
            tvTime.setText(dateFormat.format(new Date(vertifyRecord.getTime())));
            tvName.setText(vertifyRecord.getName());
            tvNation.setText(IDCardReader.getNativeplace(vertifyRecord.getIdNum()));

            tvTemper.setText(vertifyRecord.getTemper() + "℃");
            if (Float.parseFloat(vertifyRecord.getTemper()) >= warningThreshold) {
                tvTemper.setTextColor(Color.RED);
            } else {
                tvTemper.setTextColor(Color.GREEN);
            }

            tvSimilar.setText(vertifyRecord.getSimilar() + "％");
            if (Integer.parseInt(vertifyRecord.getSimilar()) >= similarThreshold) {
                tvSimilar.setTextColor(Color.GREEN);
            } else {
                tvSimilar.setTextColor(Color.RED);
            }

            Glide.with(mContext).load(vertifyRecord.getIdCardHeadPath()).override(50,50).into(ivIdCardHead);
            Glide.with(mContext).load(vertifyRecord.getPersonHeadPath()).override(50,50).into(ivPersonHead);
        }
    }
}
