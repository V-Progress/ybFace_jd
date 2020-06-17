package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.DepartListActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Depart;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyXutils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Call;

import static com.yunbiao.ybsmartcheckin_live_id.R2.id.et_departName;

public class ThermalDepartListActivity extends BaseActivity {
    private static final String TAG = "ThermalDepartListActivi";
    @BindView(R.id.btn_addDepart)
    Button btnAddDepart;
    @BindView(R.id.rlv_depart_List)
    RecyclerView rlvDepart;

    private List<Depart> departList = new ArrayList<>();
    private DepartAdapter departAdapter;
//    private Company company;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_thermal_depart_list;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_thermal_depart_list;
    }

    @Override
    protected void initView() {
        rlvDepart.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rlvDepart.addItemDecoration(new SimpleItemDecoration(10));
        departAdapter = new DepartAdapter(departList, onItemOperationClickListener);
        rlvDepart.setAdapter(departAdapter);

        btnAddDepart.setOnClickListener(view -> addDepartDialog());
    }

    @Override
    protected void initData() {
        if (departList.size() > 0) {
            departList.clear();
        }
        List<Depart> departs = DaoManager.get().queryDepartByCompId(SpUtils.getCompany().getComid());
        if (departs != null) {
            departList.addAll(departs);
        }
        departAdapter.notifyDataSetChanged();
    }

    private DepartAdapter.OnItemOperationClickListener onItemOperationClickListener = new DepartAdapter.OnItemOperationClickListener() {
        @Override
        public void onItemEditClick(int position) {
            Depart depart = departList.get(position);
            Log.e(TAG, "onItemEditClick: 修改：" + depart.getDepName());
            editDepartDialog(depart);
        }

        @Override
        public void onItemDeleteClick(int position) {
            Depart depart = departList.get(position);
            Log.e(TAG, "onItemDeleteClick: 删除：" + depart.getDepName());
            deleteDepartDialog(depart);
        }
    };

    /**
     * 删除部门
     *
     * @param depart
     */
    private void deleteDepartDialog(Depart depart) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除");
        builder.setMessage("确定要删除【" + depart.getDepName() + "】吗？");
        builder.setPositiveButton(getString(R.string.base_ensure), (dialog, which) -> {
            if (SpUtils.getCompany().getComid() == Constants.NOT_BIND_COMPANY_ID) {
                DaoManager.get().delete(depart);
                initData();
            } else {

            }
        });
        builder.setNegativeButton(getString(R.string.base_cancel), (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * 新增部门
     */
    private void addDepartDialog() {
        editDepartDialog(0, null);
    }

    /**
     * 修改部门
     *
     * @param depart
     */
    private void editDepartDialog(Depart depart) {
        editDepartDialog(1, depart);
    }

    private void editDepartDialog(int operation, Depart depart) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.act_departList_tip_qsrbmmc));
        View inflate = LayoutInflater.from(this).inflate(R.layout.dialog_depart, null);
        builder.setView(inflate);
        EditText edtDepartName = inflate.findViewById(R.id.et_departName);

        //如果操作符是0则创建对象表示新增
        if (operation == 0) {
            depart = new Depart();
        } else {//如果操作符是1则设置名称表示修改
            edtDepartName.setText(depart.getDepName());
        }

        Depart finalDepart = depart;
        builder.setPositiveButton(getString(R.string.base_ensure), (dialog, which) -> {
            String departName = edtDepartName.getText().toString();
            if (TextUtils.isEmpty(departName)) {
                edtDepartName.setError("请输入部门名称");
                return;
            }
            int comid = SpUtils.getCompany().getComid();
            if (comid == Constants.NOT_BIND_COMPANY_ID) {
                if (operation == 0) {
                    List<Depart> departs = DaoManager.get().queryDepartByCompId(comid);
                    long departId = 0l;
                    if (departs != null) {
                        for (Depart depart1 : departs) {
                            long depId = depart1.getDepId();
                            if (departId <= depId) {
                                departId = depId;
                            }
                        }
                    }
                    departId += 1l;
                    finalDepart.setId(departId);
                    finalDepart.setDepId(departId);
                    finalDepart.setDepName(departName);
                    DaoManager.get().add(finalDepart);
                } else {
                    finalDepart.setDepName(departName);
                    DaoManager.get().update(finalDepart);
                }
                initData();
            } else {
                OkHttpUtils.post()
                        .addParams("name", departName)
                        .addParams("comId", String.valueOf(comid))
                        .build().execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        UIUtils.showShort(ThermalDepartListActivity.this,getResString(R.string.act_editEmploy_tip_tjsb) + "(" + e == null ?"NULL" : e.getMessage() +")");
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        if(TextUtils.isEmpty(response)){
                            UIUtils.showShort(ThermalDepartListActivity.this,getResString(R.string.act_editEmploy_tip_tjsb));
                            return;
                        }

                        com.alibaba.fastjson.JSONObject jsonObject = JSONObject.parseObject(response);
                        Integer status = jsonObject.getInteger("status");
                        if(status == 1){
                            UIUtils.showShort(ThermalDepartListActivity.this,getResString(R.string.act_editEmploy_tip_tjsb));
                            return;
                        }

                        Integer depId = jsonObject.getInteger("depId");
                        Depart de = new Depart();
                        de.setId(depId);
                        de.setDepId(depId);
                        de.setDepName(departName);
                        DaoManager.get().addOrUpdate(de);
                    }
                });
            }
        });
        builder.setNegativeButton(getString(R.string.base_cancel), (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    static class DepartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<Depart> departList;
        private OnItemOperationClickListener onItemOperationClickListener;

        public DepartAdapter(List<Depart> departList, OnItemOperationClickListener onItemOperationClickListener) {
            this.departList = departList;
            this.onItemOperationClickListener = onItemOperationClickListener;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_thermal_depart_list, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Holder h = (Holder) holder;
            h.bindData(departList.get(position), position);
        }

        @Override
        public int getItemCount() {
            return departList == null ? 0 : departList.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            TextView tvNo;
            TextView tvDepartName;
            TextView tv_edit;
            TextView tv_delete;
            View ll_root;

            public Holder(@NonNull View itemView) {
                super(itemView);
                ll_root = itemView.findViewById(R.id.ll_root);
                tvNo = itemView.findViewById(R.id.tv_No);
                tvDepartName = itemView.findViewById(R.id.tv_depart_name);
                tv_edit = itemView.findViewById(R.id.tv_edit);
                tv_delete = itemView.findViewById(R.id.tv_delete);
            }

            public void bindData(Depart depart, int position) {
                if (position % 2 == 0) {
                    ll_root.setBackgroundColor(Color.parseColor("#00ff216d"));
                } else {
                    ll_root.setBackgroundColor(Color.parseColor("#07216d"));
                }
                tvNo.setText(String.valueOf(position));
                tvDepartName.setText(depart.getDepName());
                tv_edit.setOnClickListener(view -> onItemOperationClickListener.onItemEditClick(position));
                tv_delete.setOnClickListener(view -> onItemOperationClickListener.onItemDeleteClick(position));
            }
        }

        public interface OnItemOperationClickListener {
            void onItemEditClick(int position);

            void onItemDeleteClick(int position);
        }
    }

}
