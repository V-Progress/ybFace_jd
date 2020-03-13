package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.yunbiao.faceview.FaceManager;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateUserDBEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.adapter.DepartAdapter;
import com.yunbiao.ybsmartcheckin_live_id.adapter.EmployAdapter;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Depart;
import com.yunbiao.ybsmartcheckin_live_id.db2.User;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by Administrator on 2018/8/7.
 */

public class EmployListActivity extends BaseActivity implements EmployAdapter.EmpOnDeleteListener, EmployAdapter.EmpOnEditListener, View.OnClickListener {

    private static final String TAG = "EmployListActivity";

    private ListView lv_employ_List;
    private EmployAdapter employAdapter;
    private List<User> employList;
    private Spinner sp_depart;
    private Button btn_addEmploy;
    private Button btn_addDepart;
    private Button btn_sync;
    private ImageView iv_back;

    private TextView tv_deviceNo;
    private View rootView;
    private View avlLoading;
    private List<Depart> departs;

    private List<String> mDepartList = new ArrayList<>();
    private List<Long> mDepartIdList = new ArrayList<>();
    private long mCurrDepId = 0;
    private int comid;
    private EditText edtQuery;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_employlist;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_employlist;
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);

        rootView = findViewById(R.id.rl_root);
        lv_employ_List = findViewById(R.id.lv_employ_List);
        sp_depart = findViewById(R.id.sp_depart);
        btn_addEmploy = findViewById(R.id.btn_addEmploy);
        btn_addDepart = findViewById(R.id.btn_addDepart);
        btn_sync = findViewById(R.id.btn_sync);
        iv_back = findViewById(R.id.iv_back);
        tv_deviceNo = findViewById(R.id.tv_deviceNo);
        avlLoading = findViewById(R.id.avl_loading);
        edtQuery = findViewById(R.id.edt_query);

        btn_addEmploy.setOnClickListener(this);
        btn_addDepart.setOnClickListener(this);
        btn_sync.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        employList = new ArrayList<>();
        employAdapter = new EmployAdapter(this, employList);
        employAdapter.setOnEmpDeleteListener(this);
        employAdapter.setOnEmpEditListener(this);
        lv_employ_List.setAdapter(employAdapter);
        comid = SpUtils.getCompany().getComid();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initDevice();
        ininSpinner();
    }

    public void queryClick(View view){
        String queryStr = edtQuery.getText().toString();
        if(TextUtils.isEmpty(queryStr)){
            UIUtils.showTitleTip(this,"请输入员工名");
            return;
        }
        int index = -1;
        for (User user : employList) {
            if(TextUtils.equals(queryStr,user.getName())){
                index = employList.indexOf(user);
            }
        }
        if(index == -1){
            UIUtils.showTitleTip(this,"未搜索到该员工");
            return;
        }
        lv_employ_List.setSelection(index);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateUserDBEvent event){
        ininSpinner();
    }

    private void initDevice() {
        String deviceSernum = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        if (tv_deviceNo != null && !TextUtils.isEmpty(deviceSernum)) {
            tv_deviceNo.setText(deviceSernum);
        }
    }

    private void ininSpinner() {
        showLoading(true);
        departs = DaoManager.get().queryDepartByCompId(comid);
        mDepartList.clear();
        mDepartIdList.clear();
        mDepartList.add(getString(R.string.employ_list_all_depart));
        mDepartIdList.add(0l);
        if (departs != null) {
            for (int i = 0; i < departs.size(); i++) {
                Depart depart = departs.get(i);
                mDepartList.add(depart.getDepName());
                mDepartIdList.add(depart.getDepId());
            }
        }

        DepartAdapter departAdapter = new DepartAdapter(this, mDepartList);
        Drawable drawable = getResources().getDrawable(R.drawable.shape_employ_button);
        sp_depart.setPopupBackgroundDrawable(drawable);
        sp_depart.setAdapter(departAdapter);
        sp_depart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "onItemSelected: ------------->" + mDepartList.get(position));
                Log.e(TAG, "onItemSelected: ------------->" + mDepartIdList.get(position));
                mCurrDepId = mDepartIdList.get(position);
                loadEmployData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void loadEmployData() {
        lv_employ_List.post(new Runnable() {
            @Override
            public void run() {
                employList.clear();
                employAdapter.notifyDataSetChanged();

                List<User> users = null;
                if (mCurrDepId == 0l) {
                    users = DaoManager.get().queryUserByCompId(comid);
                } else {
                    users = DaoManager.get().queryUserByCompIdAndDepId(comid, mCurrDepId);
                }

                if (users != null) {
                    employList.addAll(users);
                }

                employAdapter.notifyDataSetChanged();
                showLoading(false);
            }
        });
    }

    private void showLoading(final boolean isShow) {
        avlLoading.post(new Runnable() {
            @Override
            public void run() {
                avlLoading.setVisibility(isShow ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void itemDeleteClick(View v, final int postion) {
        final User user = employList.get(postion);

        showDialog(getString(R.string.employ_list_confirm_delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Map<String, String> map = new HashMap<>();
                map.put("entryId", user.getId() + "");
                OkHttpUtils.post().url(ResourceUpdate.DELETESTAFF).params(map).build().execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        UIUtils.showTitleTip(EmployListActivity.this, getString(R.string.employ_list_delete_failed) + e != null ? e.getMessage() : "NULL");
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        boolean delete = false;
                        Map<String, File> allFaceMap = FaceManager.getInstance().getAllFaceMap();
                        if(allFaceMap.containsKey(user.getFaceId())){
                            File file = allFaceMap.get(user.getFaceId());
                            if(file != null){
                                delete = !file.exists() || (file.exists() && file.delete());
                            } else {
                                delete = true;
                            }
                        }
                        if (delete) {
                            DaoManager.get().delete(user);
                            employList.remove(postion);
                            employAdapter.notifyDataSetChanged();
                            UIUtils.showTitleTip(EmployListActivity.this, getString(R.string.employ_list_delete_success));
                            FaceManager.getInstance().reloadRegisterList();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void itemEditClick(View v, final int postion) {
        showDialog(getString(R.string.employ_list_confirm_edit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(EmployListActivity.this, EditEmployActivity.class);
                intent.putExtra(EditEmployActivity.KEY_ID, employList.get(postion).getId());
                intent.putExtra(EditEmployActivity.KEY_TYPE, EditEmployActivity.TYPE_EDIT);
                startActivity(intent);
            }
        });
    }

    private void showDialog(String msg,DialogInterface.OnClickListener confirm){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.base_tip)+"!");
        builder.setMessage(msg);
        builder.setPositiveButton(getString(R.string.base_ensure),confirm);
        builder.setNegativeButton(getString(R.string.base_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_addEmploy:
                Intent intent = new Intent(this, EditEmployActivity.class);
                intent.putExtra(EditEmployActivity.KEY_TYPE,EditEmployActivity.TYPE_ADD);
                startActivity(intent);
                break;
            case R.id.btn_addDepart:
                break;
            case R.id.btn_sync:
                SyncManager.instance().requestUser();
                break;
            case R.id.iv_back:
                finish();
                break;
        }
    }

    public static class EmployUpdate {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
