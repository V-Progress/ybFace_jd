package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.BatchImportActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.ThermalDepartListActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.ThermalEditEmployActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.ThermalEmployListActivity;
import com.yunbiao.ybsmartcheckin_live_id.adapter.DepartAdapter;
import com.yunbiao.ybsmartcheckin_live_id.adapter.EmployAdapter;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Depart;
import com.yunbiao.ybsmartcheckin_live_id.db2.User;
import com.yunbiao.ybsmartcheckin_live_id.utils.NetworkUtils;
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
import okhttp3.Request;
import timber.log.Timber;

/**
 * Created by Administrator on 2018/8/7.
 */

public class EmployListActivity extends BaseActivity implements EmployAdapter.EmpOnDeleteListener, EmployAdapter.EmpOnEditListener, View.OnClickListener {

    private static final String TAG = "EmployListActivity";

    private ListView lv_employ_List;
    private List<User> employList;
    private Spinner sp_depart;
    private Button btn_addEmploy;
    private Button btn_sync;
    private ImageView iv_back;
    private TextView tv_deviceNo;
    private View rootView;
    private View avlLoading;
    private EditText edtQuery;

    private long mCurrDepId = 0;
    private int comId = Constants.NOT_BIND_COMPANY_ID;
    private String key = "";

    private List<User> userList;
    private List<User> allUserList;
    private List<Depart> departList;
    private DepartAdapter departAdapter;
    private EmployAdapter userAdapter;

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
        btn_sync = findViewById(R.id.btn_sync);
        iv_back = findViewById(R.id.iv_back);
        tv_deviceNo = findViewById(R.id.tv_deviceNo);
        avlLoading = findViewById(R.id.avl_loading);
        edtQuery = findViewById(R.id.edt_query);

        btn_addEmploy.setOnClickListener(this);
        btn_sync.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        edtQuery.addTextChangedListener(textWatcher);

        departList = new ArrayList<>();
        departAdapter = new DepartAdapter(this,departList);
        sp_depart.setPopupBackgroundDrawable(getResources().getDrawable(R.drawable.shape_employ_button));
        sp_depart.setAdapter(departAdapter);
        sp_depart.setOnItemSelectedListener(onItemSelectedListener);

        userList = new ArrayList<>();
        userAdapter = new EmployAdapter(this,userList);
        userAdapter.setOnEmpDeleteListener(this);
        userAdapter.setOnEmpEditListener(this);
        lv_employ_List.setAdapter(userAdapter);
    }

    private void loadData(){
        if(departList.size() > 0){
            departList.clear();
        }
        comId = SpUtils.getCompany().getComid();
        List<Depart> departs = DaoManager.get().queryDepartByCompId(comId);
        departList.add(new Depart(-999,0,getString(R.string.employ_list_all_depart),comId));
        if(departs != null){
            departList.addAll(departs);
        }
        departAdapter.notifyDataSetChanged();
        allUserList = DaoManager.get().queryUserByCompId(comId);

        if(allUserList == null || allUserList.size() <= 0){
            UIUtils.showShort(this,getResString(R.string.please_add_a_user));
        }
        loadEmployData(mCurrDepId,key);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            key = s.toString();
            loadEmployData(mCurrDepId,key);
        }
    };

    private AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Depart depart = departList.get(position);
            mCurrDepId = depart.getDepId();
            loadEmployData(mCurrDepId,key);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private boolean isContainsKey(User user,String key){
        key = key.toLowerCase();
        String number = user.getNumber().toLowerCase();
        String name = user.getName().toLowerCase();
        String departName = user.getDepartName().toLowerCase();
        String position = user.getPosition().toLowerCase();
        return number.contains(key) || name.contains(key) || departName.contains(key) || position.contains(key);
    }

    private void loadEmployData(long departId,String key) {
        Log.e(TAG, "loadEmployData: " + key);
        showLoading(true);
        userList.clear();
        if(allUserList == null){
            return;
        }
        if(departId == 0 && TextUtils.isEmpty(key)){//两者都为空则全部
            userList.addAll(allUserList);
        } else if(departId != 0 && TextUtils.isEmpty(key)){//部门不空key空则判断部门
            for (User user : allUserList) {
                if(user.getDepartId() == departId){
                    userList.add(user);
                }
            }
        } else if(departId == 0 && !TextUtils.isEmpty(key)){//部门为空key不为空筛选key
            for (User user : allUserList) {
                if(isContainsKey(user,key)){
                    userList.add(user);
                }
            }
        } else {//两者都不为空则双向判断
            for (User user : allUserList) {
                if(user.getDepartId() == departId && isContainsKey(user,key)){
                    userList.add(user);
                }
            }
        }
        userAdapter.notifyDataSetChanged();
        showLoading(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initDevice();
        loadData();
        Company company = SpUtils.getCompany();
//        if(company.getComid() != Constants.NOT_BIND_COMPANY_ID){
//            btn_addDepart.setVisibility(View.GONE);
//        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateUserDBEvent event) {
        loadData();
    }

    private void initDevice() {
        String deviceSernum = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        if (tv_deviceNo != null && !TextUtils.isEmpty(deviceSernum)) {
            tv_deviceNo.setText(deviceSernum);
        }
    }

    private void showLoading(final boolean isShow) {
        avlLoading.post(() -> avlLoading.setVisibility(isShow ? View.VISIBLE : View.GONE));
    }

    @Override
    public void itemDeleteClick(View v, final int postion) {
        final User user = userList.get(postion);
        Log.e(TAG, "itemDeleteClick: " + user.getName());

        showDialog(getString(R.string.delete_user_dialog_title), getString(R.string.employ_list_confirm_delete), (dialog, which) -> {
            // TODO: 2020/3/18 离线功能
            if(user.getCompanyId() == Constants.NOT_BIND_COMPANY_ID){
                DaoManager.get().delete(user);
                userList.remove(postion);
                userAdapter.notifyDataSetChanged();
                boolean b = FaceManager.getInstance().removeUser(user.getFaceId());
                FaceManager.getInstance().reloadRegisterList();
                if(b){
                    UIUtils.showTitleTip(EmployListActivity.this, getString(R.string.employ_list_delete_success));
                } else {
                    UIUtils.showTitleTip(EmployListActivity.this, getString(R.string.employ_list_delete_failed));
                }
                return;
            }

            final Map<String, String> map = new HashMap<>();
            map.put("entryId", user.getId() + "");
            Timber.d("Delete User");
            Timber.d("Address:%s", ResourceUpdate.DELETESTAFF);
            Timber.d("Params:%s", map.toString());
            OkHttpUtils.post().url(ResourceUpdate.DELETESTAFF).params(map).build().execute(new StringCallback() {
                @Override
                public void onBefore(Request request, int id) {
                    super.onBefore(request, id);
                    UIUtils.showNetLoading(EmployListActivity.this);
                }

                @Override
                public void onAfter(int id) {
                    super.onAfter(id);
                    UIUtils.dismissNetLoading();
                }

                @Override
                public void onError(Call call, Exception e, int id) {
                    UIUtils.showTitleTip(EmployListActivity.this, getString(R.string.employ_list_delete_failed) + e != null ? e.getMessage() : "NULL");
                }

                @Override
                public void onResponse(String response, int id) {
                    Timber.d("Response：%s", response);
                    if(!TextUtils.isEmpty(response) && response.contains("1")){
                        boolean delete = false;
                        Map<String, File> allFaceMap = FaceManager.getInstance().getAllFaceMap();
                        if (allFaceMap.containsKey(user.getFaceId())) {
                            File file = allFaceMap.get(user.getFaceId());
                            Timber.d("删除特征文件：" + (file != null ? file.getName() : "NULL"));
                            if (file != null) {
                                delete = !file.exists() || (file.exists() && file.delete());
                            } else {
                                delete = true;
                            }
                        } else {
                            delete = true;
                        }
                        Timber.d("删除特征文件结果：" + delete);
                        if (delete) {
                            DaoManager.get().delete(user);
                            userList.remove(postion);
                            userAdapter.notifyDataSetChanged();
                            UIUtils.showTitleTip(EmployListActivity.this, getString(R.string.employ_list_delete_success));
                            FaceManager.getInstance().reloadRegisterList();
                        }
                    } else {
                        UIUtils.showShort(EmployListActivity.this,getResString(R.string.employ_list_delete_failed));
                    }
                }
            });
        });
    }

    @Override
    public void itemEditClick(View v, final int postion) {
        User user = userList.get(postion);
        Log.e(TAG, "itemEditClick: " + user.getName());
        showDialog(getString(R.string.edit_user_dialog_title),getString(R.string.employ_list_confirm_edit), (dialog, which) -> {
            Intent intent = new Intent(EmployListActivity.this, ThermalEditEmployActivity.class);
            intent.putExtra(ThermalEditEmployActivity.KEY_ID, user.getId());
            intent.putExtra(ThermalEditEmployActivity.KEY_TYPE, ThermalEditEmployActivity.TYPE_EDIT);
            startActivity(intent);
        });
    }

    private void showDialog(String title,String msg, DialogInterface.OnClickListener confirm) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(getString(R.string.base_ensure), confirm);
        builder.setNegativeButton(getString(R.string.base_cancel), (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_addEmploy:
                Intent intent = new Intent(this, EditEmployActivity.class);
                intent.putExtra(ThermalEditEmployActivity.KEY_TYPE, EditEmployActivity.TYPE_ADD);
                startActivity(intent);
                break;
            case R.id.btn_addDepart:
                startActivity(new Intent(this, DepartListActivity.class));
                break;
            case R.id.btn_sync:
                if (NetworkUtils.getNetType() < 1) {
                    UIUtils.showShort(this, getResString(R.string.there_is_no_net));
                    return;
                }
                SyncManager.instance().requestUser();
                break;
            case R.id.iv_back:
                finish();
                break;
            case R.id.btn_import:
                startActivity(new Intent(this, BatchImportActivity.class));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
