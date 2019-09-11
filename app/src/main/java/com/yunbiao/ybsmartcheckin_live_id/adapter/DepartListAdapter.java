package com.yunbiao.ybsmartcheckin_live_id.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yunbiao.ybsmartcheckin_live_id.R;

import java.util.List;


/**
 * Created by Administrator on 2018/9/17.
 */

public class DepartListAdapter extends BaseAdapter {

    private Context context;
    private List<String> mlist;
    public InnerItemOnclickListener mListener;

    public DepartListAdapter(Context context, List<String> mlist) {
        this.context = context;
        this.mlist = mlist;
    }

    @Override
    public int getCount() {
        return mlist.size();
    }

    @Override
    public String getItem(int position) {
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
            convertView = View.inflate(context, R.layout.item_depart_list,null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder= (ViewHolder) convertView.getTag();

        }
        String vip=mlist.get(position);
        viewHolder.tv_No.setText(position+1+"");
        viewHolder.tv_name.setText(vip);
        if (position%2==1){
            convertView.setBackgroundColor(Color.parseColor("#07216d"));
        }

        viewHolder.tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener!=null){
                    mListener.itemClick(v,  position);
                }
            }
        });

        return convertView;
    }

    class ViewHolder{
        TextView tv_No;
        TextView tv_name;
        TextView tv_delete;
        LinearLayout layout_title;

        public ViewHolder(View convertView) {
            tv_No= (TextView) convertView.findViewById(R.id.tv_No);
            tv_name= (TextView) convertView.findViewById(R.id.tv_name);
            tv_delete= (TextView) convertView.findViewById(R.id.tv_delete);
            layout_title= (LinearLayout) convertView.findViewById(R.id.layout_title);


        }
    }

    public interface InnerItemOnclickListener {
        void itemClick(View v, int postion);	}
    public void setOnInnerItemOnClickListener(InnerItemOnclickListener listener){
        this.mListener=listener;	}


}
