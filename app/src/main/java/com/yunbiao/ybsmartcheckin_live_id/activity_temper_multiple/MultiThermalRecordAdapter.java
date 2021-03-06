package com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.utils.BlurTransformation;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class MultiThermalRecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<MultiTemperBean> dataList;
    private float warningThreshold = 37.3f;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private int itemId = -1;
    private Activity mAct;

    public void setWarningThreshold(float warningThreshold) {
        this.warningThreshold = warningThreshold;
    }

    public MultiThermalRecordAdapter(Activity activity, List<MultiTemperBean> list, int horizontal) {
        mAct = activity;
        dataList = list;
        if (horizontal == RecyclerView.HORIZONTAL) {
            itemId = R.layout.item_multi_thermal_normal;
        } else if (horizontal == RecyclerView.VERTICAL) {
            itemId = R.layout.item_multi_thermal_warning;
        } else if (horizontal == 3) {
            itemId = R.layout.item_multi_thermal_warning_portrait;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WarningViewHolder(View.inflate(parent.getContext(), itemId, null));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        WarningViewHolder warningViewHolder = (WarningViewHolder) holder;
        warningViewHolder.bindData(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class WarningViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHead;
        ImageView ivHot;
        TextView tvName;
        TextView tvTemper;
        TextView tvTime;

        public WarningViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHead = itemView.findViewById(R.id.iv_head_image_multi_thermal);
            ivHot = itemView.findViewById(R.id.iv_hot_image_multi_thermal);
            tvName = itemView.findViewById(R.id.tv_name_multi_thermal);
            tvTemper = itemView.findViewById(R.id.tv_temper_multi_thermal);
            tvTime = itemView.findViewById(R.id.tv_time_multi_thermal);
        }

        public void bindData(MultiTemperBean multiTemperBean) {
            if (multiTemperBean.getHeadImage() != null) {
                ivHead.setImageBitmap(multiTemperBean.getHeadImage());
            } else {
                Glide.with(mAct).load(multiTemperBean.getHeadPath()).asBitmap().into(ivHead);
            }

            Bitmap hotImage = multiTemperBean.getHotImage();
            if (hotImage != null) {
                ivHot.setImageBitmap(hotImage);
            } else {
                Glide.with(mAct).load(multiTemperBean.getHotPath()).asBitmap().into(ivHot);
            }

            String name = multiTemperBean.getName();
            tvName.setText(name);

            String temperStr = "";
            float temper = multiTemperBean.getTemper();
            float afterTreatmentTemp = temper;
            if (MultiThermalActivity.fEnabled) {
                afterTreatmentTemp = (float) (Math.round((temper * 1.8f + 32) * 10)) / 10;
            }
            if (temper < MultiThermalActivity.normalRangeStart || temper > MultiThermalActivity.normalRangeEnd) {
                temperStr = mAct.getResources().getString(R.string.main_error_multi_thermal) + "（" + afterTreatmentTemp + "）";
            } else {
                temperStr = mAct.getResources().getString(R.string.main_normal_multi_thermal) + "（" + afterTreatmentTemp + "）";
            }
            tvTemper.setText(temperStr);

            tvTime.setText(dateFormat.format(multiTemperBean.getTime()));
        }
    }

}