package com.yunbiao.ybsmartcheckin_live_id.activity_certificates;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.ThermalEditEmployActivity;
import com.yunbiao.ybsmartcheckin_live_id.adapter.CertificatesUserAdapter;
import com.yunbiao.ybsmartcheckin_live_id.adapter.EmployAdapter;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.CertificatesUser;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/8/7.
 */

public class CertificatesUserListActivity extends BaseActivity implements CertificatesUserAdapter.EmpOnDeleteListener, CertificatesUserAdapter.EmpOnEditListener, View.OnClickListener {

    private static final String TAG = "EmployListActivity";

    private ListView lv_employ_List;
    private CertificatesUserAdapter userAdapter;
    private List<CertificatesUser> userList = new ArrayList<>();
    private Button btn_addEmploy;

    private View rootView;
    private View avlLoading;
    private int comid;
    private EditText edtQuery;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_certificates_userlist;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_certificates_userlist;
    }

    @Override
    protected void initView() {
        rootView = findViewById(R.id.rl_root);
        lv_employ_List = findViewById(R.id.lv_employ_List);
        btn_addEmploy = findViewById(R.id.btn_addEmploy);
        avlLoading = findViewById(R.id.avl_loading);
        edtQuery = findViewById(R.id.edt_query);
        btn_addEmploy.setOnClickListener(v -> startActivity(new Intent(CertificatesUserListActivity.this,CertificatesAddUserActivity.class)));
    }

    @Override
    protected void initData() {
        userAdapter = new CertificatesUserAdapter(this,userList);
        userAdapter.setOnEmpDeleteListener(this);
        userAdapter.setOnEmpEditListener(this);
        lv_employ_List.setAdapter(userAdapter);
        comid = SpUtils.getCompany().getComid();
    }

    @Override
    protected void onResume() {
        super.onResume();
        userList.clear();
        List<CertificatesUser> certificatesUsers = DaoManager.get().queryAll(CertificatesUser.class);
        if(certificatesUsers != null && certificatesUsers.size() > 0){
            userList.addAll(certificatesUsers);
        }
        userAdapter.notifyDataSetChanged();
    }

    @Override
    public void itemDeleteClick(View v, final int postion) {
        final CertificatesUser user = userList.get(postion);
        showDialog(getString(R.string.employ_list_confirm_delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DaoManager.get().delete(user);
                userList.remove(user);
            }
        });
    }

    @Override
    public void itemEditClick(View v, final int postion) {
    }

    private void showDialog(String msg, DialogInterface.OnClickListener confirm) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.base_tip) + "!");
        builder.setMessage(msg);
        builder.setPositiveButton(getString(R.string.base_ensure), confirm);
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
                Intent intent = new Intent(this, ThermalEditEmployActivity.class);
                intent.putExtra(ThermalEditEmployActivity.KEY_TYPE, ThermalEditEmployActivity.TYPE_ADD);
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
