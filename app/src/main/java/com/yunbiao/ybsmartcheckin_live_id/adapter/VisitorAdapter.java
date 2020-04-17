package com.yunbiao.ybsmartcheckin_live_id.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.db2.User;
import com.yunbiao.ybsmartcheckin_live_id.db2.Visitor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;


/**
 * Created by Administrator on 2018/9/17.
 */

public class VisitorAdapter extends BaseAdapter {
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final String TAG = "EmployAdapter";
    private Context context;
    private List<Visitor> mlist;

    public VisitorAdapter(Context context, List<Visitor> mlist) {
        this.context = context;
        this.mlist = mlist;
    }

    @Override
    public int getCount() {
        return mlist.size();
    }

    @Override
    public Visitor getItem(int position) {
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
            convertView = View.inflate(context, R.layout.item_visitor, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Visitor visitor = mlist.get(position);
        if (!TextUtils.isEmpty(visitor.getName())) {
            viewHolder.tvName.setText(visitor.getName());
        } else {
            viewHolder.tvName.setText("");
        }

        if (!TextUtils.isEmpty(visitor.getUnit())) {
            viewHolder.tvComp.setText(visitor.getUnit());
        } else {
            viewHolder.tvComp.setText("");
        }

        viewHolder.tvReason.setText(visitor.getReason());
        viewHolder.tvTime.setText(visitor.getCurrStart() + "\n---\n" + visitor.getCurrEnd());

        switch (visitor.getAddTag()) {
            case -1://下载失败
                convertView.setBackgroundColor(Color.parseColor("#ED8E17"));
                break;
            case -2://添加失败
                convertView.setBackgroundColor(Color.parseColor("#FA0808"));
                break;
            case -3://更新失败
                convertView.setBackgroundColor(Color.parseColor("#A2A2A2"));
                break;
            default:
                if (position % 2 == 1) {
                    convertView.setBackgroundColor(Color.parseColor("#07216d"));
                } else {
                    convertView.setBackgroundColor(Color.parseColor("#00ff216d"));
                }
                break;
        }
        return convertView;
    }

    class ViewHolder {
        TextView tvName;
        TextView tvReason;
        TextView tvComp;
        TextView tvTime;

        public ViewHolder(View convertView) {
            tvName = (TextView) convertView.findViewById(R.id.tv_visitorName);
            tvReason = (TextView) convertView.findViewById(R.id.tv_visitorReason);
            tvComp = (TextView) convertView.findViewById(R.id.tv_visitor_company);
            tvTime = (TextView) convertView.findViewById(R.id.tv_visit_time);
        }
    }
}
