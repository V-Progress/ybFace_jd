package com.yunbiao.ybsmartcheckin_live_id.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.db.SignBean;


import java.text.SimpleDateFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2018/8/6.
 */

public class VisitorAdapter extends BaseAdapter {

    private Context context;
    private List<SignBean> mVisitors;
    private int layoutId;

    public VisitorAdapter(Context context, List<SignBean> mVisitors,int screenOri) {
        this.context = context;
        this.mVisitors = mVisitors;
        if (screenOri == Configuration.ORIENTATION_PORTRAIT) {
            layoutId = R.layout.item_visitor;
        } else {
            layoutId = R.layout.item_visitor_h;
        }
    }

    @Override
    public int getCount() {
        return mVisitors.size();
    }

    @Override
    public SignBean getItem(int position) {
        return mVisitors.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(context, layoutId, null);
            viewHolder = new ViewHolder(convertView);
            viewHolder.iv_userPhoto = (CircleImageView) convertView.findViewById(R.id.iv_userPhoto);
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.tv_job = (TextView) convertView.findViewById(R.id.tv_job);
            viewHolder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        SignBean bean = mVisitors.get(position);
        viewHolder.tv_name.setText(bean.getName());
        if (bean.getName().contains("游客")) {
            viewHolder.tv_name.setText("游客");
        }

        if (bean.getName().contains("游客")) {
            viewHolder.tv_job.setText(bean.getSex() + "," + bean.getAge());
        } else {
            viewHolder.tv_job.setText(bean.getJob());
        }
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        viewHolder.tv_time.setText(df.format(bean.getTime()));
        if (!TextUtils.isEmpty(bean.getImgUrl())) {
            Glide.with(context).load(bean.getImgUrl()).asBitmap().override(100,100).into(viewHolder.iv_userPhoto);
        }

        return convertView;
    }


    class ViewHolder {
        protected CircleImageView iv_userPhoto;
        protected TextView tv_name;
        protected TextView tv_job;
        protected TextView tv_time;

        public ViewHolder(View convertView) {
            iv_userPhoto = (CircleImageView) convertView.findViewById(R.id.iv_userPhoto);
            tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            tv_job = (TextView) convertView.findViewById(R.id.tv_job);
            tv_time = (TextView) convertView.findViewById(R.id.tv_time);
        }
    }
}
