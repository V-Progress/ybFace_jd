package com.yunbiao.ybsmartcheckin_live_id.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.utils.BlurTransformation;

import java.text.SimpleDateFormat;
import java.util.List;


/**
 * Created by Administrator on 2018/9/17.
 */

public class ThermalSignAdapter extends BaseAdapter {


    private static final String TAG = "SignAdapter";
    private Context context;
    private List<Sign> mlist;
    private boolean isPrivacy;
    private boolean isFahrenheit = false;
    private float mTemperMin = 35.5f;
    private float mWarningMin = 37.2f;
    private OnItemDeleteListener onItemDeleteListener;

    public ThermalSignAdapter(Context context, List<Sign> mlist) {
        this.context = context;
        this.mlist = mlist;
        isPrivacy = false;
    }

    public void setmTemperMin(float mTemperMin) {
        this.mTemperMin = mTemperMin;
    }

    public void setmWarningMin(float mWarningMin) {
        this.mWarningMin = mWarningMin;
    }

    public void setFahrenheit(boolean fahrenheit) {
        isFahrenheit = fahrenheit;
    }

    public ThermalSignAdapter(Context context, List<Sign> mlist, boolean isPrivacy, OnItemDeleteListener deleteListener) {
        this.context = context;
        this.mlist = mlist;
        this.isPrivacy = isPrivacy;
        onItemDeleteListener = deleteListener;
    }

    @Override
    public int getCount() {
        return mlist.size();
    }

    @Override
    public Sign getItem(int position) {
        return mlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_thermal_sign, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.bindData(position, mlist.get(position), convertView);
        return convertView;
    }

    protected float formatF(float fValue) {
        return (float) (Math.round(fValue * 10)) / 10;
    }

    class ViewHolder {
        TextView tv_No;
        TextView tv_date;
        TextView tv_employName;
        TextView tv_employDepart;
        TextView tv_employJob;
        TextView tv_edit;
        ImageView iv_photo;
        TextView tvDelete;

        public ViewHolder(View convertView) {
            tv_No = (TextView) convertView.findViewById(R.id.tv_No);
            tv_date = (TextView) convertView.findViewById(R.id.tv_date);
            tv_employName = (TextView) convertView.findViewById(R.id.tv_employName);
            tv_employDepart = (TextView) convertView.findViewById(R.id.tv_employDepart);
            tv_employJob = (TextView) convertView.findViewById(R.id.tv_employJob);
            tv_edit = (TextView) convertView.findViewById(R.id.tv_edit);
            iv_photo = (ImageView) convertView.findViewById(R.id.iv_photo);
            tvDelete = convertView.findViewById(R.id.tv_delete);
        }

        public void bindData(int position, Sign vip, View convertView) {
            tv_No.setText(position + 1 + "");
            if (!TextUtils.isEmpty(vip.getEmployNum())) {
                tv_date.setText(vip.getTime() + "");
            } else {
                tv_date.setText("");
            }
            if (vip.getType() == -9) {
                tv_employName.setText(APP.getContext().getResources().getString(R.string.fment_sign_visitor_name));
            } else if (!TextUtils.isEmpty(vip.getName())) {
                tv_employName.setText(vip.getName());
            } else {
                tv_employName.setText("");
            }
            if (!TextUtils.isEmpty(vip.getDepart())) {
                tv_employDepart.setText(vip.getDepart());
            } else {
                tv_employDepart.setText("");
            }

            float temperature = vip.getTemperature();
            String temper;
            if (isFahrenheit) {
                temper = formatF((float) (temperature * 1.8 + 32)) + "℉";
            } else {
                temper = vip.getTemperature() + "℃";
            }
            tv_employJob.setText(temper);

            if (temperature < mTemperMin) {
                tv_employJob.setTextColor(Color.WHITE);
            } else if (temperature < mWarningMin) {
                tv_employJob.setTextColor(Color.GREEN);
            } else {
                tv_employJob.setTextColor(Color.RED);
            }

            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            tv_date.setText(df.format(vip.getTime()));
            if (position % 2 == 1) {
                convertView.setBackgroundColor(Color.parseColor("#07216d"));
            } else {
                convertView.setBackgroundColor(Color.parseColor("#0007216d"));
            }
            if (!TextUtils.isEmpty(vip.getHeadPath())) {
                if (isPrivacy) {
                    Glide.with(context).load(vip.getHeadPath()).transform(new BlurTransformation(context, 20)).into(iv_photo);
                } else {
                    Glide.with(context).load(vip.getHeadPath()).asBitmap().into(iv_photo);
                }
            } else {
                iv_photo.setImageBitmap(null);
            }

            tvDelete.setOnClickListener(view -> {
                if (onItemDeleteListener != null) onItemDeleteListener.onItemDelete(position,vip);
            });
        }
    }

    public interface OnItemDeleteListener {
        void onItemDelete(int position,Sign sign);
    }
}
