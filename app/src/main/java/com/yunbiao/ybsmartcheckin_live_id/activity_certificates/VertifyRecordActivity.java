package com.yunbiao.ybsmartcheckin_live_id.activity_certificates;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.FileSelectActivity;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.VertifyRecord;
import com.yunbiao.ybsmartcheckin_live_id.utils.excel.ExcelUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.IDCardReader;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.excel.Export;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;

public class VertifyRecordActivity extends BaseActivity {
    @BindView(R.id.rlv_vertify_record)
    RecyclerView rlvRecord;
    @BindView(R.id.btn_select_date)
    Button btnSelectDate;
    @BindView(R.id.tv_certificates_native_place)
    TextView tvNative;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private DateFormat format = new SimpleDateFormat("yyyMMddHHmmss");
    private List<VertifyRecord> vertifyRecordList = new ArrayList<>();
    private VertifyRecordAdapter vertifyRecordAdapter;
    private boolean isICCARDMode;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_vertify_record;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_vertify_record;
    }

    @Override
    protected void initView() {
        isICCARDMode = SpUtils.getBoolean(CertificatesConst.Key.IC_CARD_MODE, CertificatesConst.Default.IC_CARD_MODE);
        Float warningThreshold = SpUtils.getFloat(CertificatesConst.Key.WARNING_THRESHOLD, CertificatesConst.Default.WARNING_THRESHOLD);
        int similar = SpUtils.getIntOrDef(CertificatesConst.Key.SIMILAR, CertificatesConst.Default.SIMILAR);
        vertifyRecordAdapter = new VertifyRecordAdapter(vertifyRecordList, this, isICCARDMode);
        vertifyRecordAdapter.setTemperThreshold(warningThreshold);
        vertifyRecordAdapter.setSimilarThreshold(similar);
        rlvRecord.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rlvRecord.setAdapter(vertifyRecordAdapter);

        if (isICCARDMode) {
            tvNative.setText(getResString(R.string.certificates_record_native));
        } else {
            tvNative.setText(getResString(R.string.certificates_record_depart));
        }
    }

    @Override
    protected void initData() {
        Date currDate = new Date();
        String format = dateFormat.format(currDate);
        btnSelectDate.setText(format);
        loadData(format);
    }

    private void loadData(String date) {
        if (TextUtils.isEmpty(date)) {
            return;
        }
        if (vertifyRecordList.size() > 0) {
            vertifyRecordList.clear();
        }
        List<VertifyRecord> vertifyRecords = DaoManager.get().queryVertifyRecordByDate(date);
        if (vertifyRecords != null) {
            vertifyRecordList.addAll(vertifyRecords);
        }
        if (vertifyRecordAdapter != null) {
            vertifyRecordAdapter.notifyDataSetChanged();
        }
    }

    public void selectDate(View thisView) {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(
                VertifyRecordActivity.this,
                (view, year, month, dayOfMonth) -> {
                    String yearStr = String.valueOf(year);

                    int realMonth = month + 1;
                    String monthStr = String.valueOf(realMonth);
                    if (realMonth < 10) {
                        monthStr = "0" + realMonth;
                    }

                    String dayStr = String.valueOf(dayOfMonth);
                    if (dayOfMonth < 10) {
                        dayStr = "0" + dayOfMonth;
                    }
                    String date = yearStr + "-" + monthStr + "-" + dayStr;
                    btnSelectDate.setText(date);
                    loadData(date);
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private static String[] title = {"日期", "时间", "体温", "相似度", "姓名", "性别", "民族", "籍贯", "身份证号码", "家庭地址", "证件有效期", "证件照", "头像", "热成像图"};
    private static final String TAG = "VertifyRecordActivity";
    private boolean isExporting = false;

    public void exportData(View view) {
        FileSelectActivity.selectFile(this,FileSelectActivity.FILE_TYPE_DIR,true,FileSelectActivity.SELECT_REQUEST_CODE);
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

    private void export(File file){
        final File excelFile = new File(file, dateFormat.format(new Date()) + "_" + getResources().getString(R.string.sign_export_record) + ".xls");
        ExcelUtils.initExcelForPoi(excelFile.getPath(), getResString(R.string.sign_list_table_name), title, new Export.ExportCallback() {
            @Override
            public void onProgress(int progress, int max) {

            }

            @Override
            public void onStart() {
                UIUtils.showNetLoading(VertifyRecordActivity.this);
            }

            @Override
            public List<List<String>> getDataList() {
                //获取源数据
                List<VertifyRecord> datas = DaoManager.get().queryAll(VertifyRecord.class);
                Log.e(TAG, "export: 源数据：" + (datas == null ? 0 : datas.size()));
                return createTableData(datas);
            }

            @Override
            public void onFinish(int result, String filePath) {
                UIUtils.dismissNetLoading();
                UIUtils.showShort(VertifyRecordActivity.this, result == 1
                        ? (getResources().getString(R.string.sign_export_record_success) +
                        "\n" +
                        getResources().getString(R.string.sign_export_record_path) + filePath)
                        : getResources().getString(R.string.sign_export_record_failed));
            }
        });
    }

    public List<List<String>> createTableData(List<VertifyRecord> datas) {
        List<List<String>> tableDatas = new ArrayList<>();
        if (datas != null) {
            for (int i = 0; i < datas.size(); i++) {
                VertifyRecord vertifyRecord = datas.get(i);
                List<String> beanList = new ArrayList<>();
                beanList.add(vertifyRecord.getDate());
                beanList.add(timeFormat.format(new Date(vertifyRecord.getTime())));
                beanList.add(vertifyRecord.getTemper());
                beanList.add(vertifyRecord.getSimilar() + "％");
                beanList.add(vertifyRecord.getName());
                beanList.add(TextUtils.equals("1", vertifyRecord.getSex()) ? "男" : "女");
                beanList.add(vertifyRecord.getNation());
                beanList.add(IDCardReader.getNativeplace(vertifyRecord.getIdNum()));
                beanList.add(vertifyRecord.getIdNum());
                beanList.add(vertifyRecord.getAddress());
                beanList.add(vertifyRecord.getTermDate());
                beanList.add(vertifyRecord.getIdCardHeadPath());
                beanList.add(vertifyRecord.getPersonHeadPath());
                beanList.add(vertifyRecord.getHotImagePath());
                tableDatas.add(beanList);
            }
        }
        return tableDatas;
    }
}
