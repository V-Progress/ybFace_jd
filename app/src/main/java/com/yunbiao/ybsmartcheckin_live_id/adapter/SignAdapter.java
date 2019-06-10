package com.yunbiao.ybsmartcheckin_live_id.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.db.SignBean;

import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.List;


/**
 * Created by Administrator on 2018/9/17.
 */

public class SignAdapter extends BaseAdapter {


    private static final String TAG = "SignAdapter";
    private Context context;
    private List<SignBean> mlist;

    public SignAdapter(Context context, List<SignBean> mlist) {
        this.context = context;
        this.mlist = mlist;
    }

    @Override
    public int getCount() {
        return mlist.size();
    }

    @Override
    public SignBean getItem(int position) {
        return mlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
       ViewHolder viewHolder=null;
        if (convertView == null){
            convertView = View.inflate(context, R.layout.item_sign,null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder= (ViewHolder) convertView.getTag();
        }
        SignBean vip=mlist.get(position);
        viewHolder.tv_No.setText(position+1+"");
        if (!TextUtils.isEmpty(vip.getEmployNum())){
            viewHolder.tv_date.setText(vip.getTime()+"");
        }
        if (!TextUtils.isEmpty(vip.getName())){
            viewHolder.tv_employName.setText(vip.getName());
        }
        if (!TextUtils.isEmpty(vip.getDepart())){
            viewHolder.tv_employDepart.setText(vip.getDepart());
        }

        if (!TextUtils.isEmpty(vip.getJob())){
            viewHolder.tv_employJob.setText(vip.getJob());
        }
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        viewHolder.tv_date.setText(df.format(vip.getTime()));
        if (position%2==1){
            convertView.setBackgroundColor(Color.parseColor("#07216d"));
        }
        if (!TextUtils.isEmpty(vip.getImgUrl())){
            x.image().bind(viewHolder.iv_photo,vip.getImgUrl());
        }

        return convertView;
    }

    class ViewHolder{
        TextView tv_No;
        TextView tv_date;
        TextView tv_employName;
        TextView tv_employDepart;
        TextView tv_employJob;
        TextView tv_edit;
        ImageView iv_photo;


        public ViewHolder(View convertView) {
            tv_No= (TextView) convertView.findViewById(R.id.tv_No);
            tv_date= (TextView) convertView.findViewById(R.id.tv_date);
            tv_employName= (TextView) convertView.findViewById(R.id.tv_employName);
            tv_employDepart= (TextView) convertView.findViewById(R.id.tv_employDepart);
            tv_employJob= (TextView) convertView.findViewById(R.id.tv_employJob);
            tv_edit= (TextView) convertView.findViewById(R.id.tv_edit);
            iv_photo= (ImageView) convertView.findViewById(R.id.iv_photo);

        }
    }
}
