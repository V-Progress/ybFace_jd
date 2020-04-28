package com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple;

import android.app.Activity;
import android.graphics.Bitmap;
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
    private boolean isPrivacy;

    public void setWarningThreshold(float warningThreshold) {
        this.warningThreshold = warningThreshold;
    }

    public void setPrivacy(boolean privacy) {
        isPrivacy = privacy;
    }

    public MultiThermalRecordAdapter(Activity activity, List<MultiTemperBean> list, int horizontal) {
        mAct = activity;
        dataList = list;
        if (horizontal == RecyclerView.HORIZONTAL) {
            itemId = R.layout.item_multi_thermal_normal;
        } else {
            itemId = R.layout.item_multi_thermal_warning;
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
            DrawableTypeRequest request;
            if (multiTemperBean.getHeadImage() != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                multiTemperBean.getHeadImage().compress(Bitmap.CompressFormat.PNG, 100, baos);
                request = Glide.with(mAct).load(baos.toByteArray());
            } else {
                request = Glide.with(mAct).load(multiTemperBean.getHeadPath());
            }
            if(isPrivacy){
                request.transform(new BlurTransformation(mAct, 20f));
            }
            request.into(ivHead);

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
            if (temper <= 0f || temper >= warningThreshold) {
                temperStr = mAct.getResources().getString(R.string.main_error_multi_thermal) + "（" + temper + "）";
            } else {
                temperStr = mAct.getResources().getString(R.string.main_normal_multi_thermal) + "（" + temper + "）";
            }
            tvTemper.setText(temperStr);

            tvTime.setText(dateFormat.format(multiTemperBean.getTime()));
        }
    }

}