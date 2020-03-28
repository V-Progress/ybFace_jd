package com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MultiRecordAdapter extends BaseAdapter {
    private List<Sign> datas;
    private Activity mAct;

    public MultiRecordAdapter(Activity activity, List<Sign> datas) {
        this.datas = datas;
        mAct = activity;
    }

    private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Override
    public int getCount() {
        return datas == null ? 0 : datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RecordViewHolder recordViewHolder = null;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.item_record_multi_thermal, null);
            recordViewHolder = new RecordViewHolder();
            recordViewHolder.tvIndex = convertView.findViewById(R.id.tv_index_record_multi_thermal);
            recordViewHolder.tvDate = convertView.findViewById(R.id.tv_date_record_multi_thermal);
            recordViewHolder.tvTime = convertView.findViewById(R.id.tv_time_record_multi_thermal);
            recordViewHolder.tvTemper = convertView.findViewById(R.id.tv_temper_record_multi_thermal);
            recordViewHolder.ivHead = convertView.findViewById(R.id.iv_head_record_multi_thermal);
            recordViewHolder.ivHot = convertView.findViewById(R.id.iv_hot_record_multi_thermal);
            convertView.setTag(recordViewHolder);
        } else {
            recordViewHolder = (RecordViewHolder) convertView.getTag();
        }

        recordViewHolder.bindData(datas.get(position), position);

        return convertView;
    }

    private static final String TAG = "MultiRecordAdapter";
    class RecordViewHolder {

        public TextView tvIndex;
        public TextView tvDate;
        public TextView tvTime;
        public TextView tvTemper;
        public ImageView ivHead;
        public ImageView ivHot;

        public void bindData(Sign sign, int position) {
            tvIndex.setText(position + "");

            String date = sign.getDate();

            Locale locale = mAct.getResources().getConfiguration().locale;
            if(!TextUtils.equals(locale.getCountry(),Locale.CHINA.getCountry())){
                if(date.contains("年")){
                    date = date.replaceAll("年","-");
                }
                if(date.contains("月")){
                    date = date.replaceAll("月","-");
                }
                if(date.contains("日")){
                    date = date.replaceAll("日","");
                }
            }

            tvDate.setText(date);
            tvTime.setText(dateFormat.format(sign.getTime()));
            tvTemper.setText(sign.getTemperature() + "℃");
            Glide.with(mAct).load(sign.getHeadPath()).asBitmap().into(ivHead);
            Glide.with(mAct).load(sign.getHotImgPath()).asBitmap().into(ivHot);
        }
    }
}
