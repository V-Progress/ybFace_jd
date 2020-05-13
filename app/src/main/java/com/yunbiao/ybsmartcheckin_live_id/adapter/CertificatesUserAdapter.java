package com.yunbiao.ybsmartcheckin_live_id.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity_certificates.CertificatesUserListActivity;
import com.yunbiao.ybsmartcheckin_live_id.db2.CertificatesUser;
import com.yunbiao.ybsmartcheckin_live_id.db2.User;

import java.util.List;


/**
 * Created by Administrator on 2018/9/17.
 */

public class CertificatesUserAdapter extends BaseAdapter {


    private static final String TAG = "EmployAdapter";
    private Context context;
    private List<CertificatesUser> mlist;
    public EmpOnDeleteListener empOnDeleteListener;
    public EmpOnEditListener empOnEditListener;
    public CertificatesUserAdapter(Context context, List<CertificatesUser> mlist) {
        this.context = context;
        this.mlist = mlist;
    }

    @Override
    public int getCount() {
        return mlist.size();
    }

    @Override
    public CertificatesUser getItem(int position) {
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
            convertView = View.inflate(context, R.layout.item_certificates_user,null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder= (ViewHolder) convertView.getTag();
        }

        CertificatesUser user=mlist.get(position);
        viewHolder.tv_No.setText(position+1+"");

        if (!TextUtils.isEmpty(user.getName())){
            viewHolder.tv_employName.setText(user.getName());
        } else {
            viewHolder.tv_employName.setText("");
        }
        if (!TextUtils.isEmpty(user.getDepart())){
            viewHolder.tv_employDepart.setText(user.getDepart());
        } else {
            viewHolder.tv_employDepart.setText("");
        }

        if(!TextUtils.isEmpty(user.getNum())){
            viewHolder.tv_eployNum.setText(user.getNum());
        } else {
            viewHolder.tv_eployNum.setText("");
        }
        viewHolder.tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (empOnDeleteListener!=null){
                    empOnDeleteListener.itemDeleteClick(v,  position);
                }
            }
        });
        return convertView;
    }

    class ViewHolder{
        TextView tv_No;
        TextView tv_employName;
        TextView tv_employDepart;
        TextView tv_eployNum;
        TextView tv_edit;
        TextView tv_delete;


        public ViewHolder(View convertView) {
            tv_No= (TextView) convertView.findViewById(R.id.tv_No);
            tv_employName= (TextView) convertView.findViewById(R.id.tv_employName);
            tv_employDepart= (TextView) convertView.findViewById(R.id.tv_employDepart);
            tv_eployNum= (TextView) convertView.findViewById(R.id.tv_eployNum);
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
