package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.app.DatePickerDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.APP;
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
    private SignDao signDao;
    private View pb_load_list;
    private TextView tv_load_tips;
    private TextView tv_export_sign_data;

    private final int MODE_ALL = 0;
    private final int MODE_SENDED = 1;
    private final int MODE_UNSENDED = 2;
    private int DATA_MODE = MODE_UNSENDED;

    private String queryDate = "";

    private List<SignBean> mSignList;
    private List<SignBean> mShowList = new ArrayList<>();
    private Spinner spnDataMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_table);
        } else {
            setContentView(R.layout.activity_table_h);
        }

        signDao= APP.getSignDao();
        initViews();
        initData();

        initSpinner();
    }

    private void initViews() {
        lv_sign_List= (ListView) findViewById(R.id.lv_sign_List);
        tv_date= (TextView) findViewById(R.id.tv_date);
        iv_back= (ImageView) findViewById(R.id.iv_back);
        pb_load_list = findViewById(R.id.pb_load_list);
        tv_load_tips = (TextView)findViewById(R.id.tv_load_tips);
        tv_export_sign_data = (TextView)findViewById(R.id.tv_export_sign_data);
        spnDataMode = (Spinner) findViewById(R.id.spn_data_mode);
        tv_export_sign_data.setOnClickListener(this);
        tv_date.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }

    private void initData() {
        Calendar calendar=Calendar.getInstance();
        String yearStr = calendar.get(Calendar.YEAR)+"";//获取年份
        String dayStr = calendar.get(Calendar.DAY_OF_MONTH)+"";//获取天
        int realMonth = calendar.get(Calendar.MONTH)+1;//获取月份
        String monthStr = realMonth + "月";
        if(realMonth < 10){
            monthStr = "0" + realMonth + "月";
        }
        String today =yearStr + "年" + monthStr + dayStr + "日";
        tv_date.setText(today);
        queryDate = today;
    }

    private void initSpinner(){
        final String[] modeArray = {"全部","已发送","未发送"};
        ArrayAdapter<String> spnAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,modeArray);

        spnDataMode.setAdapter(spnAdapter);
        spnDataMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DATA_MODE = position;
                loadSignList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spnDataMode.setSelection(modeArray.length-1);
    }

    private void loadSignList(){
        pb_load_list.setVisibility(View.VISIBLE);
        lv_sign_List.setVisibility(View.GONE);
        tv_load_tips.setVisibility(View.GONE);

        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "loadSignList: " + queryDate + " ----- " + DATA_MODE );

                mShowList.clear();
                mSignList = signDao.queryByDate(queryDate);
                if(mSignList == null || mSignList.size()<=0){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_load_tips.setVisibility(View.VISIBLE);
                            pb_load_list.setVisibility(View.GONE);
                        }
                    });
                    return;
                }

                for (SignBean signBean : mSignList) {
                    if(DATA_MODE == MODE_UNSENDED && !signBean.isUpload()){
                        mShowList.add(signBean);
                    } else if(DATA_MODE == MODE_SENDED && signBean.isUpload()){
                        mShowList.add(signBean);
                    } else if(DATA_MODE == MODE_ALL){
                        mShowList.add(signBean);
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mShowList != null && mShowList.size()>0){
                            SignAdapter adapter=new SignAdapter(SignActivity.this,mShowList);
                            lv_sign_List.setAdapter(adapter);

                            lv_sign_List.setVisibility(View.VISIBLE);
                            pb_load_list.setVisibility(View.GONE);
                            tv_load_tips.setVisibility(View.GONE);
                        } else {
                            if(DATA_MODE == MODE_UNSENDED){
                                tv_load_tips.setText("数据已全部上传");
                            } else {
                                tv_load_tips.setText("暂无数据");
                            }
                            tv_load_tips.setVisibility(View.VISIBLE);
                            lv_sign_List.setVisibility(View.GONE);
                            pb_load_list.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
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
                                int realMonth = month + 1;
                                String monthStr = realMonth + "月";
                                if(realMonth < 10){
                                    monthStr = "0" + realMonth + "月";
                                }

                                String date =year+"年"+ monthStr +dayOfMonth+"日";
                                tv_date.setText(date);

                                queryDate = date;
                                loadSignList();
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

}