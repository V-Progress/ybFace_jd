package com.yunbiao.ybsmartcheckin_live_id.temp_multi;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class MultiRecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Sign> datas;
    private Activity mAct;

    public MultiRecordAdapter(Activity activity, List<Sign> datas) {
        this.datas = datas;
        mAct = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecordViewHolder(View.inflate(parent.getContext(), R.layout.item_record_multi_thermal, null));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RecordViewHolder recordViewHolder = (RecordViewHolder) holder;
        recordViewHolder.bindData(datas.get(position),position);
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    class RecordViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvIndex;
        private final TextView tvDate;
        private final TextView tvTime;
        private final TextView tvTemper;
        private final ImageView ivHead;
        private final ImageView ivHot;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tv_index_record_multi_thermal);
            tvDate = itemView.findViewById(R.id.tv_date_record_multi_thermal);
            tvTime = itemView.findViewById(R.id.tv_time_record_multi_thermal);
            tvTemper = itemView.findViewById(R.id.tv_temper_record_multi_thermal);
            ivHead = itemView.findViewById(R.id.iv_head_record_multi_thermal);
            ivHot = itemView.findViewById(R.id.iv_hot_record_multi_thermal);
        }

        public void bindData(Sign sign, int position) {
            tvIndex.setText(position + "");
            tvDate.setText(sign.getDate());
            tvTime.setText(dateFormat.format(sign.getTime()));
            tvTemper.setText(sign.getTemperature() + "℃");
            Glide.with(mAct).load(sign.getHeadPath()).asBitmap().into(ivHead);
            Glide.with(mAct).load(sign.getHotImgPath()).asBitmap().into(ivHot);
        }
    }
}
