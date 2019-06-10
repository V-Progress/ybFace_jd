package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.app.DatePickerDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.adapter.SignAdapter;
import com.yunbiao.ybsmartcheckin_live_id.db.SignBean;
import com.yunbiao.ybsmartcheckin_live_id.db.SignDao;
import com.yunbiao.ybsmartcheckin_live_id.utils.ThreadUitls;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Administrator on 2018/10/10.
 */

public class SignActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "SignActivity";

    private ListView lv_sign_List;
    private TextView tv_date;
    private ImageView iv_back;
    private List<SignBean> mSignList;
    private SignAdapter mSignAdapter;
    private SignDao signDao;
    private String today="";
    private View pb_load_list;
    private TextView tv_load_tips;
    private TextView tv_export_sign_data;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_table);
        } else {
            setContentView(R.layout.activity_table_h);
        }

        signDao=new SignDao(this);
        initViews();
        initData();
    }

    private void initViews() {
        lv_sign_List= (ListView) findViewById(R.id.lv_sign_List);
        tv_date= (TextView) findViewById(R.id.tv_date);
        iv_back= (ImageView) findViewById(R.id.iv_back);
        pb_load_list = findViewById(R.id.pb_load_list);
        tv_load_tips = (TextView)findViewById(R.id.tv_load_tips);
        tv_export_sign_data = (TextView)findViewById(R.id.tv_export_sign_data);
        tv_export_sign_data.setOnClickListener(this);
        tv_date.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }

    private void initData() {
        Calendar calendar=Calendar.getInstance();
        String yearStr = calendar.get(Calendar.YEAR)+"";//获取年份
        String monthStr = calendar.get(Calendar.MONTH)+1+"";//获取月份
        String dayStr = calendar.get(Calendar.DAY_OF_MONTH)+"";//获取天
        today=yearStr+"年"+monthStr+"月"+dayStr+"日";
        Log.e(TAG, "today--------->"+today );
        tv_date.setText(today);
        loadSignList(today);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_date:
                Calendar now = Calendar.getInstance();
                new DatePickerDialog(
                        SignActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                Log.d("Orignal", "Got clicked");
                                String  queryDay=year+"年"+(month+1)+"月"+dayOfMonth+"日";
                                tv_date.setText(queryDay);
                                loadSignList(queryDay);
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                ).show();
                break;
            case R.id.tv_export_sign_data:
                if(mSignList.size()<=0){
                    Toast.makeText(this, "暂无数据", Toast.LENGTH_SHORT).show();
                    return;
                }

                String exportListJson = new Gson().toJson(mSignList);
                Log.e(TAG, "当前可导出："+exportListJson);

                break;
        }
    }

    private void loadSignList(final String date){
        mSignList=new ArrayList();

        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                List<SignBean> signBeen = signDao.queryByDate(date);
                for (SignBean signBean : signBeen) {
                    if (signBean.isUpload()) {
                        continue;
                    }
                    mSignList.add(signBean);
                }

                if(mSignList.size()<=0){
                    tv_load_tips.setVisibility(View.VISIBLE);
                    pb_load_list.setVisibility(View.GONE);
                    return;
                }

                mSignAdapter=new SignAdapter(SignActivity.this,mSignList);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lv_sign_List.setAdapter(mSignAdapter);
                        lv_sign_List.setVisibility(View.VISIBLE);
                        pb_load_list.setVisibility(View.GONE);
                    }
                });
            }
        });
    }
}
