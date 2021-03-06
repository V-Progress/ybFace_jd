package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateSignDataEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.FileSelectActivity;
import com.yunbiao.ybsmartcheckin_live_id.adapter.SignAdapter;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.utils.excel.ExcelUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.NetworkUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.ThreadUitls;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
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

/**
 * Created by Administrator on 2018/10/10.
 */


public class SignActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "SignActivity";

    private ListView lv_sign_List;
    private TextView tv_date;
    private ImageView iv_back;
    //    private SignDao signDao;
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
    private DateFormat dateFormat1 = new SimpleDateFormat("yyyy年MM月dd日");
    private DateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ss");
    private Button btnUpload;
    private SignAdapter adapter;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_table;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_table;
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        btnUpload = findViewById(R.id.btn_upload);
        lv_sign_List = (ListView) findViewById(R.id.lv_sign_List);
        tv_date = (TextView) findViewById(R.id.tv_date);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        pb_load_list = findViewById(R.id.pb_load_list);
        tv_load_tips = (TextView) findViewById(R.id.tv_load_tips);
        spnDataMode = (Spinner) findViewById(R.id.spn_data_mode);
        tv_date.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
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

        adapter = new SignAdapter(SignActivity.this, mShowList);
        lv_sign_List.setAdapter(adapter);
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
        lv_sign_List.post(() -> {
            lv_sign_List.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        });
    }

    private void loadSignList() {
        loading();
        ThreadUitls.runInThread(() -> {
            mShowList.clear();
            mSignList = DaoManager.get().querySignByComIdAndDate(SpUtils.getInt(SpUtils.COMPANYID), queryDate);
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
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_upload:
                if(NetworkUtils.getNetType() < 1){
                    UIUtils.showShort(this, getResString(R.string.upload_there_is_no_server_connect));
                    return;
                }

                if(SpUtils.getCompany().getComid() == Constants.NOT_BIND_COMPANY_ID){
                    UIUtils.showShort(SignActivity.this, "The device is not tied to the company and the data will be saved locally only");
                    return;
                }

                UIUtils.showNetLoading(this);
                SignManager.instance().uploadSignRecord(aBoolean -> {
                    if (aBoolean) {
                        EventBus.getDefault().post(new UpdateSignDataEvent());
                    }
                    UIUtils.dismissNetLoading();
                    UIUtils.showShort(SignActivity.this, (aBoolean ? getString(R.string.sign_list_upload_success) : getString(R.string.sign_list_upload_failed)));
                });
                break;
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_date:
                Calendar now = Calendar.getInstance();
                new DatePickerDialog(
                        SignActivity.this,
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

    public void exportToUD(final View view) {
        FileSelectActivity.selectFile(this,FileSelectActivity.FILE_TYPE_DIR, true,FileSelectActivity.SELECT_REQUEST_CODE);
    }

    private void export(File file){
        final File excelFile = new File(file, dateFormat.format(new Date()) + "_" + getResources().getString(R.string.sign_export_record) + ".xlsx");
        ExcelUtils.initExcelForPoi(excelFile.getPath(), getResString(R.string.sign_list_table_name), title, new Export.ExportCallback() {
            @Override
            public void onProgress(int progress, int max) {
                UIUtils.setProgress(progress,max);
            }

            @Override
            public void onStart() {
                UIUtils.showNetLoading(SignActivity.this,true);
            }

            @Override
            public List<List<String>> getDataList() {
                return createObjTableData();
            }

            @Override
            public void onFinish(int result, String filePath) {
                UIUtils.dismissNetLoading();
                UIUtils.showShort(SignActivity.this, result == 1
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
                stringList.add(TextUtils.isEmpty(sign.getDepart()) ? sign.getDepart() : "");
                stringList.add(TextUtils.isEmpty(sign.getPosition()) ? "" : "");
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
}