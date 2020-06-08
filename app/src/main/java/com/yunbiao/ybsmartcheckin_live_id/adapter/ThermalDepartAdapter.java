package com.yunbiao.ybsmartcheckin_live_id.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.db2.Depart;

import java.util.List;

/**
 * Created by Administrator on 2018/10/8.
 */

public class ThermalDepartAdapter extends BaseAdapter {

    private List<Depart> mList;
    private Context mContext;

    public ThermalDepartAdapter(Context pContext, List<Depart> pList) {
        this.mContext = pContext;
        this.mList = pList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //最主要代码
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.item_depart_spinner, parent, false);

        if (convertView != null) {
            TextView _TextView1 = (TextView) convertView.findViewById(R.id.tv_depart);
            Depart depart = mList.get(position);
            _TextView1.setText(depart.getDepName());
        }
        return convertView;
    }
}
