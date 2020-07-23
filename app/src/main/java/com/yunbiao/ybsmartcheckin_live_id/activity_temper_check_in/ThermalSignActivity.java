package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.yunbiao.ybsmartcheckin_live_id.FlavorType;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateSignDataEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.adapter.ThermalSignAdapter;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.utils.NetworkUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.ThreadUitls;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.excel.ExcelUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.logutils.Utils;
import com.yunbiao.ybsmartcheckin_live_id.utils.excel.Export;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Administrator on 2018/10/10.
 */


public class ThermalSignActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "SignActivity";
    @BindView(R.id.tv_export_sign_data)
    TextView tvExport;
    private ListView lv_sign_List;
    private TextView tv_date;
    private View pb_load_list;
    private TextView tv_load_tips;

    private final int MODE_ALL = 0;
    private final int MODE_SENDED = 1;
    private final int MODE_UNSENDED = 2;
    private int DATA_MODE = MODE_UNSENDED;

    private String queryDate = "";

    private List<Sign> mShowList = new ArrayList<>();
    private List<Sign> mSignList;
    private Spinner spnDataMode;
    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private DateFormat dateFormatter = new SimpleDateFormat("yyyy年MM月dd日");
    private DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
    private DateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ss");
    private Button btnUpload;
    private ThermalSignAdapter adapter;

    @Override
    protected int getPortraitLayout() {
        if (Utils.getWinWidth(this) == 600 && Utils.getWinHight(this)  == 976) {
            //适配土耳其客户机器
            return R.layout.activity_thermal_sign_976600;
        }
        return R.layout.activity_thermal_sign;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_thermal_sign;
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        btnUpload = findViewById(R.id.btn_upload);
        lv_sign_List = (ListView) findViewById(R.id.lv_sign_List);
        tv_date = (TextView) findViewById(R.id.tv_date);
        pb_load_list = findViewById(R.id.pb_load_list);
        tv_load_tips = (TextView) findViewById(R.id.tv_load_tips);
        spnDataMode = (Spinner) findViewById(R.id.spn_data_mode);
        tv_date.setOnClickListener(this);
        btnUpload.setOnClickListener(this);

        if(Constants.FLAVOR_TYPE == FlavorType.SOFT_WORK_Z){
            tvExport.setVisibility(View.INVISIBLE);
            tvExport.setEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    private static String[] title = {"姓名", "员工编号", "部门", "职位", "日期", "时间", "体温", "头像", "热像"};

    @Override
    protected void initData() {
        title = new String[]{
                getResources().getString(R.string.sign_list_name),
                getResources().getString(R.string.sign_list_number),
                getResources().getString(R.string.sign_list_depart),
                getResources().getString(R.string.sign_list_position),
                getResources().getString(R.string.sign_list_date),
                getResources().getString(R.string.sign_list_time),
                getResources().getString(R.string.sign_list_temper),
                getResources().getString(R.string.sign_list_head),
                getResources().getString(R.string.sign_list_hot)};

        String today = dateFormatter.format(new Date());
        tv_date.setText(formatDate(today));
        queryDate = today;
        initSpinner();

        boolean isFEnabled = SpUtils.getBoolean(ThermalConst.Key.THERMAL_F_ENABLED, ThermalConst.Default.THERMAL_F_ENABLED);
        boolean isPrivacy = SpUtils.getBoolean(Constants.Key.PRIVACY_MODE, Constants.Default.PRIVACY_MODE);
        Float minThreshold = SpUtils.getFloat(ThermalConst.Key.TEMP_MIN_THRESHOLD, ThermalConst.Default.TEMP_MIN_THRESHOLD);
        Float warningThreshold = SpUtils.getFloat(ThermalConst.Key.TEMP_WARNING_THRESHOLD, ThermalConst.Default.TEMP_WARNING_THRESHOLD);

        adapter = new ThermalSignAdapter(ThermalSignActivity.this, mShowList, isPrivacy,onItemDeleteListener);
        adapter.setFahrenheit(isFEnabled);
        adapter.setmTemperMin(minThreshold);
        adapter.setmWarningMin(warningThreshold);

        lv_sign_List.setAdapter(adapter);
    }

    private ThermalSignAdapter.OnItemDeleteListener onItemDeleteListener = (position,sign) -> {
        if(mShowList != null && mShowList.size() >= position){
            showDeleteDialog(position,sign);
        }
    };

    private void showDeleteDialog(int position,Sign sign){
        new AlertDialog.Builder(this)
                .setTitle(getResString(R.string.alert_title_warning))
                .setMessage(getResString(R.string.delete_record_msg))
                .setNegativeButton(getResString(R.string.setting_switch_cancel),(dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(getResString(R.string.setting_switch_confirm),(dialog, which) -> {
                    DaoManager.get().deleteSign(sign);
                    mShowList.remove(position);
                    adapter.notifyDataSetChanged();

                    //如果是签到类型，则不删除头像。
                    if(sign.getType() != 0){
                        String headPath = sign.getHeadPath();
                        File file = new File(headPath);
                        if(file.exists()){
                            file.delete();
                        }
                    }

                    String hotImgPath = sign.getHotImgPath();
                    File hotFile = new File(hotImgPath);
                    if (hotFile.exists()) {
                        hotFile.delete();
                    }
                })
                .create()
                .show();
    }

    private String formatDate(String date) {
        return date.replace("年", "-").replace("月", "-").replace("日", "");
    }

    private void initSpinner() {
        final String[] modeArray = {getString(R.string.sign_list_all_type), getString(R.string.sign_list_sent), getString(R.string.sign_list_unsent)};
        ArrayAdapter<String> spnAdapter = new ArrayAdapter<>(this, R.layout.item_spinner_simple_text, modeArray);

        spnDataMode.setAdapter(spnAdapter);
        Drawable drawable = getResources().getDrawable(R.drawable.shape_employ_button);
        spnDataMode.setPopupBackgroundDrawable(drawable);
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
        spnDataMode.setSelection(0);
    }

    private void loading() {
        pb_load_list.post(() -> {
            pb_load_list.setVisibility(View.VISIBLE);
            tv_load_tips.setVisibility(View.GONE);
        });
    }

    private void tips(final String tips) {
        tv_load_tips.post(() -> {
            lv_sign_List.setVisibility(View.GONE);
            pb_load_list.setVisibility(View.GONE);
            tv_load_tips.setVisibility(View.VISIBLE);
            tv_load_tips.setText(tips);
        });
    }

    private void hide() {
        tv_load_tips.post(() -> {
            pb_load_list.setVisibility(View.GONE);
            tv_load_tips.setVisibility(View.GONE);
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateSignDataEvent event) {
        loadSignList();
    }

    private void showData() {
        lv_sign_List.post(new Runnable() {
            @Override
            public void run() {
                lv_sign_List.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void loadSignList() {
        loading();
        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                mShowList.clear();
                mSignList = DaoManager.get().querySignByComIdAndDate(SpUtils.getCompany().getComid(), queryDate);
                if (mSignList == null || mSignList.size() <= 0) {
                    tips(getString(R.string.sign_list_no_data));
                    return;
                }

                for (Sign signBean : mSignList) {
                    if (DATA_MODE == MODE_UNSENDED && !signBean.isUpload()) {
                        mShowList.add(signBean);
                    } else if (DATA_MODE == MODE_SENDED && signBean.isUpload()) {
                        mShowList.add(signBean);
                    } else if (DATA_MODE == MODE_ALL) {
                        mShowList.add(signBean);
                    }
                }

                if (mShowList.size() > 0) {
                    showData();
                    hide();
                } else {
                    tips(getString(R.string.sign_list_no_data));
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_upload:
                if (NetworkUtils.getNetType() < 1) {
                    UIUtils.showShort(this, getResString(R.string.upload_there_is_no_server_connect));
                    return;
                }

                if(SpUtils.getCompany().getComid() == Constants.NOT_BIND_COMPANY_ID){
                    UIUtils.showShort(ThermalSignActivity.this, "The device is not tied to the company and the data will be saved locally only");
                    return;
                }

                UIUtils.showNetLoading(this);
                SignManager.instance().uploadSignRecord(aBoolean -> {
                    if (aBoolean) {
                        EventBus.getDefault().post(new UpdateSignDataEvent());
                    }
                    UIUtils.dismissNetLoading();
                    UIUtils.showShort(ThermalSignActivity.this, (aBoolean ? getString(R.string.sign_list_upload_success) : getString(R.string.sign_list_upload_failed)));
                });
                break;
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_date:
                Calendar now = Calendar.getInstance();
                new DatePickerDialog(
                        ThermalSignActivity.this,
                        (view, year, month, dayOfMonth) -> {
                            String yearStr = year + "年";

                            int realMonth = month + 1;
                            String monthStr = realMonth + "月";
                            if (realMonth < 10) {
                                monthStr = "0" + realMonth + "月";
                            }

                            String dayStr = dayOfMonth + "日";
                            if (dayOfMonth < 10) {
                                dayStr = "0" + dayOfMonth + "日";
                            }
                            String date = yearStr + monthStr + dayStr;
                            tv_date.setText(formatDate(date));

                            queryDate = date;
                            loadSignList();
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                ).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == FileSelectActivity.SELECT_REQUEST_CODE && resultCode == RESULT_OK){
            String stringExtra = data.getStringExtra(FileSelectActivity.RESULT_PATH_KEY);
            Log.e(TAG, "onActivityResult: 选中的目录：" + stringExtra);
            export(new File(stringExtra));
        }
    }

    /**
     * 导出
     * @param view
     */
    public void exportToUD(final View view) {
        FileSelectActivity.selectFile(this,FileSelectActivity.FILE_TYPE_DIR, true,FileSelectActivity.SELECT_REQUEST_CODE);
    }

    private void export(File file){
        final File excelFile = new File(file, dateFormat.format(new Date()) + "_" + getResources().getString(R.string.sign_export_record) + ".xls");
        ExcelUtils.initExcelForPoi(excelFile.getPath(), getResString(R.string.sign_list_table_name), title, new Export.ExportCallback() {
            @Override
            public void onProgress(int progress, int max) {
                UIUtils.setProgress(progress,max);
            }

            @Override
            public void onStart() {
                UIUtils.showNetLoading(ThermalSignActivity.this,true);
            }

            @Override
            public List<List<String>> getDataList() {
                return createObjTableData();
            }

            @Override
            public void onFinish(int result, String filePath) {
                UIUtils.dismissNetLoading();
                UIUtils.showShort(ThermalSignActivity.this, result == 1
                        ? (getResources().getString(R.string.sign_export_record_success) +
                        "\n" +
                        getResources().getString(R.string.sign_export_record_path) + filePath)
                        : getResources().getString(R.string.sign_export_record_failed));
            }
        });
    }

    //创建表格数据
    public List<List<String>> createObjTableData(){
        List<List<String>> stringListList = new ArrayList<>();
        final List<Sign> signs = DaoManager.get().querySignByComId(SpUtils.getCompany().getComid());
        if(signs != null && signs.size() > 0){
            for (int i = 0; i < signs.size(); i++) {
                Sign sign = signs.get(i);
                List<String> stringList = new ArrayList<>();
                stringList.add(TextUtils.isEmpty(sign.getName()) ? getResString(R.string.fment_sign_visitor_name) : sign.getName());
                stringList.add(TextUtils.isEmpty(sign.getEmployNum()) ? "" : sign.getEmployNum());
                stringList.add(TextUtils.isEmpty(sign.getDepart()) ? "" : sign.getDepart());
                stringList.add(TextUtils.isEmpty(sign.getPosition()) ? "" : sign.getPosition());
                stringList.add(dateFormat1.format(sign.getTime()) + "");
                stringList.add(dateFormat2.format(sign.getTime()) + "");
                stringList.add(sign.getTemperature() + "");
                stringList.add(sign.getHeadPath());
                stringList.add(sign.getHotImgPath());
                stringListList.add(stringList);
            }
        }
        return stringListList;
    }

    public void clearAllData(View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.delete_user_dialog_title))
                .setMessage(getResources().getString(R.string.clear_all_data_dialog_message))
                .setPositiveButton(getResources().getString(R.string.setting_switch_confirm), (dialog, which) -> {
                    SignManager.instance().clearAllData(this);
                    loadSignList();
                }).setNegativeButton(getResources().getString(R.string.setting_switch_cancel), (dialog, which) -> {
                    dialog.dismiss();
                }).create();
        alertDialog.show();
    }
}