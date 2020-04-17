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

import java.util.List;


/**
 * Created by Administrator on 2018/9/17.
 */

public class EmployAdapter extends BaseAdapter {


    private static final String TAG = "EmployAdapter";
    private Context context;
    private List<User> mlist;
    public EmpOnDeleteListener empOnDeleteListener;
    public EmpOnEditListener empOnEditListener;
    public EmployAdapter(Context context, List<User> mlist) {
        this.context = context;
        this.mlist = mlist;
    }

    @Override
    public int getCount() {
        return mlist.size();
    }

    @Override
    public User getItem(int position) {
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
            convertView = View.inflate(context, R.layout.item_employ,null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder= (ViewHolder) convertView.getTag();
        }

        User user=mlist.get(position);
        viewHolder.tv_No.setText(position+1+"");

        if (!TextUtils.isEmpty(user.getNumber())){
            viewHolder.tv_employNo.setText(user.getNumber());
        } else {
            viewHolder.tv_employNo.setText("");
        }
        if (!TextUtils.isEmpty(user.getName())){
            viewHolder.tv_employName.setText(user.getName());
        } else {
            viewHolder.tv_employName.setText("");
        }
        if (!TextUtils.isEmpty(user.getDepartName())){
            viewHolder.tv_employDepart.setText(user.getDepartName());
        } else {
            viewHolder.tv_employDepart.setText("");
        }
        if (!TextUtils.isEmpty(user.getPosition())){
            viewHolder.tv_employJob.setText(user.getPosition());
        } else {
            viewHolder.tv_employJob.setText("");
        }

        viewHolder.tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (empOnDeleteListener!=null){
                    empOnDeleteListener.itemDeleteClick(v,  position);
                }
            }
        });

        viewHolder.tv_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (empOnEditListener!=null){
                    empOnEditListener.itemEditClick(v,  position);
                }
            }
        });

        switch (user.getAddTag()) {
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

    class ViewHolder{
        TextView tv_No;
        TextView tv_employNo;
        TextView tv_employName;
        TextView tv_employDepart;
        TextView tv_employJob;
        TextView tv_edit;
        TextView tv_delete;


        public ViewHolder(View convertView) {
            tv_No= (TextView) convertView.findViewById(R.id.tv_No);
            tv_employNo= (TextView) convertView.findViewById(R.id.tv_employNo);
            tv_employName= (TextView) convertView.findViewById(R.id.tv_employName);
            tv_employDepart= (TextView) convertView.findViewById(R.id.tv_employDepart);
            tv_employJob= (TextView) convertView.findViewById(R.id.tv_employJob);
            tv_edit= (TextView) convertView.findViewById(R.id.tv_edit);
            tv_delete= (TextView) convertView.findViewById(R.id.tv_delete);

        }
    }

    public interface EmpOnDeleteListener {
        void itemDeleteClick(View v, int postion);	}
    public void setOnEmpDeleteListener(EmpOnDeleteListener listener){
        this.empOnDeleteListener=listener;	}

    public interface EmpOnEditListener {
        void itemEditClick(View v, int postion);	}
    public void setOnEmpEditListener(EmpOnEditListener listener){
        this.empOnEditListener=listener;	}
}
