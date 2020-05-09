package com.yunbiao.ybsmartcheckin_live_id.activity_safety_check_double_light;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
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

public class DoubleLightRecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Sign> datas;
    private Context context;
    private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public DoubleLightRecordAdapter(List<Sign> datas, Context context) {
        this.datas = datas;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_double_light_record, parent, false);
        return new VH(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        VH vh = (VH) holder;
        vh.bindData(datas.get(position));
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class VH extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView tvTemper;
        private TextView tvTime;

        public VH(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_head_double_light);
            tvTemper = itemView.findViewById(R.id.tv_temper_double_light);
            tvTime = itemView.findViewById(R.id.tv_time_double_light);
        }

        public void bindData(Sign sign) {
            Bitmap imgBitmap = sign.getImgBitmap();
            if(imgBitmap != null){
                imageView.setImageBitmap(imgBitmap);
            } else {
                Glide.with(context).load(sign.getHeadPath()).asBitmap().into(imageView);
            }
            tvTemper.setText(sign.getTemperature() + "℃");
            tvTime.setText(dateFormat.format(sign.getTime()));
        }
    }
}
