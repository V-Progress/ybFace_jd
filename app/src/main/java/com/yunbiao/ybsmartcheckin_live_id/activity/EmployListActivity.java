package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.adapter.DepartAdapter;
import com.yunbiao.ybsmartcheckin_live_id.adapter.EmployAdapter;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.db.DepartBean;
import com.yunbiao.ybsmartcheckin_live_id.db.DepartDao;
import com.yunbiao.ybsmartcheckin_live_id.db.VIPDetail;
import com.yunbiao.ybsmartcheckin_live_id.db.UserDao;
import com.yunbiao.ybsmartcheckin_live_id.faceview.FaceSDK;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by Administrator on 2018/8/7.
 */

public class EmployListActivity extends BaseActivity implements EmployAdapter.EmpOnDeleteListener,EmployAdapter.EmpOnEditListener,View.OnClickListener{

    private static final String TAG = "EmployListActivity";

    private ListView lv_employ_List;
    private EmployAdapter employAdapter;
    private List<VIPDetail> employList;
    private  Spinner sp_depart;
    private Button btn_addEmploy;
    private Button btn_addDepart;
    private Button btn_sync;
    private ImageView iv_back;

    private List<String> mDepartList;
    private DepartDao departDao;
    private List<DepartBean> mDepartlist;
    private UserDao userDao;
    private String depart="全部部门";

    private TextView tv_deviceNo;
    private View rootView;
    private View avlLoading;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_employlist);
        } else {
            setContentView(R.layout.activity_employlist_h);
        }

        EventBus.getDefault().register(this);

        departDao= APP.getDepartDao();
        userDao=APP.getUserDao();

        initViews();

        employList=new ArrayList<>();
        employAdapter=new EmployAdapter(this,employList);
        employAdapter.setOnEmpDeleteListener(this);
        employAdapter.setOnEmpEditListener(this);
        lv_employ_List.setAdapter(employAdapter);

        initData();
        initDevice();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ininSpinner();
    }

    //摄像头错误监听
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(EmployUpdate employUpdate) {
        ininSpinner();
    }

    public static class EmployUpdate{

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void ininSpinner() {
        avlLoading.setVisibility(View.VISIBLE);
        lv_employ_List.setVisibility(View.GONE);

        mDepartList=new ArrayList<>();
        mDepartList.add("全部部门");
        mDepartlist   =	 departDao.selectAll();
        if (mDepartlist!=null){
            for (int i = 0; i <mDepartlist.size() ; i++) {
                mDepartList.add(mDepartlist.get(i).getName());
            }
        }

        DepartAdapter departAdapter=new DepartAdapter(this,mDepartList);
        sp_depart.setAdapter(departAdapter);
        sp_depart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "onItemSelected: ------------->"+mDepartList.get(position) );
                lv_employ_List.setVisibility(View.VISIBLE);
                avlLoading.setVisibility(View.GONE);

                employList.clear();
                String departName=mDepartList.get(position);
                if (departName.equals("全部部门")){
                    if (userDao.selectAll()!=null){
                        employList.addAll( userDao.selectAll());
                        employAdapter.notifyDataSetChanged();
                    }

                }else {
                    if (mDepartList.get(position)!=null){
                        employList.addAll( userDao.queryByDepart(mDepartList.get(position)));
                        employAdapter.notifyDataSetChanged();
                    }

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    private void initData() {
    }

    private void initViews() {
        rootView = findViewById(R.id.rl_root);
        lv_employ_List= (ListView) findViewById(R.id.lv_employ_List);
        sp_depart= (Spinner) findViewById(R.id.sp_depart);
        btn_addEmploy= (Button) findViewById(R.id.btn_addEmploy);
        btn_addDepart= (Button) findViewById(R.id.btn_addDepart);
        btn_sync= (Button) findViewById(R.id.btn_sync);
        iv_back= (ImageView) findViewById(R.id.iv_back);
        tv_deviceNo= (TextView) findViewById(R.id.tv_deviceNo);
        avlLoading = findViewById(R.id.avl_loading);

        btn_addEmploy.setOnClickListener(this);
        btn_addDepart.setOnClickListener(this);
        btn_sync.setOnClickListener(this);
        iv_back.setOnClickListener(this);

    }

    private void initDevice() {
        String  deviceSernum= SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        if (!TextUtils.isEmpty(deviceSernum)){
            tv_deviceNo.setText(deviceSernum);
        }

    }

    @Override
    public void itemDeleteClick(View v,final int postion) {
        final VIPDetail vipDetail = employList.get(postion);
        //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
        AlertDialog.Builder builder = new AlertDialog.Builder(EmployListActivity.this);

        //    设置Title的内容
        builder.setTitle("提示！");
        //    设置Content来显示一个信息
        builder.setMessage("确定删除吗？");
        //    设置一个PositiveButton
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final Map<String, String> map = new HashMap<String, String>();
                map.put("entryId", vipDetail.getEmpId()+"");
                OkHttpUtils.post().url(ResourceUpdate.DELETESTAFF).params(map).build().execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        UIUtils.showTitleTip("删除失败 " + e != null?e.getMessage():"NULL");
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        boolean b = FaceSDK.instance().removeUser(String.valueOf(vipDetail.getFaceId()));
                        if(b){
                            userDao.delete(employList.get(postion));
                            employList.remove(postion);
                            employAdapter.notifyDataSetChanged();
                            UIUtils.showTitleTip("删除成功");
                        }
                    }
                });
            }
        });
        //    设置一个NegativeButton
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        //    显示出该对话框
        builder.show();
    }

    @Override
    public void itemEditClick(View v, final int postion) {
        //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
        AlertDialog.Builder builder = new AlertDialog.Builder(EmployListActivity.this);

        //    设置Title的内容
        builder.setTitle("提示！");
        //    设置Content来显示一个信息
        builder.setMessage("确定去修改吗？");
        //    设置一个PositiveButton
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                Intent intent=new Intent(EmployListActivity.this, EditEmployActivity.class);
                intent.putExtra("name",employList.get(postion).getName());
                intent.putExtra("depart",employList.get(postion).getDepart());
                startActivity(intent);

            }
        });
        //    设置一个NegativeButton
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        //    显示出该对话框
        builder.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_addEmploy:
                startActivity(new Intent(EmployListActivity.this, AddEmployActivity.class));
            break;
            case R.id.btn_addDepart:
                startActivity(new Intent(EmployListActivity.this, DepartListActivity.class));
                break;
            case R.id.btn_sync:
                SyncManager.instance().initInfo();
                break;
            case R.id.iv_back:
                finish();
                break;
        }
    }
}
