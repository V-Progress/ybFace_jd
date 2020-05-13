package com.yunbiao.ybsmartcheckin_live_id.activity_certificates;

import android.app.DatePickerDialog;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.ThermalSignActivity;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.db2.VertifyRecord;
import com.yunbiao.ybsmartcheckin_live_id.utils.ExcelUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.IDCardReader;
import com.yunbiao.ybsmartcheckin_live_id.utils.SdCardUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import org.xutils.common.util.FileUtil;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

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
        //获取U盘地址
        String usbDiskPath = SdCardUtils.getUsbDiskPath(this);
        File file = new File(usbDiskPath);
        if (!file.exists()) {
            isExporting = false;
            UIUtils.showTitleTip(this, "未检测到USB存储设备，将导出到本地");
            usbDiskPath = Environment.getExternalStorageDirectory().getPath();
            file = new File(usbDiskPath);
        }
        //创建记录最外层目录
        final File dirFile = new File(file, format.format(new Date()) + "_导出记录");
        if (!dirFile.exists()) {
            boolean mkdirs = dirFile.mkdirs();
            Log.e(TAG, "exportToUD: 创建外层目录：" + dirFile.getPath() + " --- " + mkdirs);
        }
        //创建图片目录
        final File imgDir = new File(dirFile, "image");
        if (!imgDir.exists()) {
            boolean mkdirs = imgDir.mkdirs();
            Log.e(TAG, "exportToUD: 创建图片目录：" + imgDir.getPath() + " --- " + mkdirs);
        }
        //创建xls文件
        final File excelFile = new File(dirFile, format.format(new Date()) + "_导出记录" + ".xls");
        //获取源数据
        List<VertifyRecord> datas = DaoManager.get().queryAll(VertifyRecord.class);
        Log.e(TAG, "export: 源数据：" + (datas == null ? 0 : datas.size()));
        if (datas == null || datas.size() <= 0) {
            UIUtils.showShort(this, "没有数据");
            return;
        }
        //生成导出数据
        final List<List<String>> tableData = createTableData(datas);
        Log.e(TAG, "export: 导出数据：" + tableData.size());
        if (tableData == null || tableData.size() <= 0) {
            UIUtils.showShort(this, "生成导出数据失败，请重试");
            return;
        }
        UIUtils.showNetLoading(this);
        view.setEnabled(false);
        //开始导出
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "run: 拷贝图片");
                for (VertifyRecord data : datas) {
                    String idCardHeadPath = data.getIdCardHeadPath();
                    String personHeadPath = data.getPersonHeadPath();
                    String hotImagePath = data.getHotImagePath();

                    if (!TextUtils.isEmpty(idCardHeadPath)) {
                        File idCardFile = new File(idCardHeadPath);
                        if (idCardFile.exists()) {
                            boolean copy = FileUtil.copy(idCardFile.getPath(), imgDir.getPath() + File.separator + idCardFile.getName());
                            Log.e(TAG, "run: 证件照，拷贝结果：" + copy);
                        }
                    }

                    if (!TextUtils.isEmpty(personHeadPath)) {
                        File headFile = new File(personHeadPath);
                        if (headFile.exists()) {
                            boolean copy = FileUtil.copy(headFile.getPath(), imgDir.getPath() + File.separator + headFile.getName());
                            Log.e(TAG, "run: 头像，拷贝结果：" + copy);
                        }
                    }

                    if (!TextUtils.isEmpty(hotImagePath)) {
                        File hotImageFile = new File(hotImagePath);
                        if (hotImageFile.exists()) {
                            boolean copy = FileUtil.copy(hotImageFile.getPath(), imgDir.getPath() + File.separator + hotImageFile.getName());
                            Log.e(TAG, "run: 热图，拷贝结果：" + copy);
                        }
                    }
                }

                ExcelUtils.initExcel(excelFile.getPath(), title);
                final boolean result = ExcelUtils.writeObjListToExcel(tableData, excelFile.getPath());
                Log.e(TAG, "run: excel，导出结果：" + result);
                runOnUiThread(() -> {
                    view.setEnabled(true);
                    UIUtils.dismissNetLoading();
                    UIUtils.showShort(VertifyRecordActivity.this, result
                            ? (getResources().getString(R.string.sign_export_record_success) +
                            "\n" +
                            getResources().getString(R.string.sign_export_record_path) + dirFile.getPath())
                            : getResources().getString(R.string.sign_export_record_failed));
                });
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
                beanList.add(new File(vertifyRecord.getIdCardHeadPath()).getName());
                beanList.add(new File(vertifyRecord.getPersonHeadPath()).getName());
                beanList.add(new File(vertifyRecord.getHotImagePath()).getName());
                tableDatas.add(beanList);
            }
        }
        return tableDatas;
    }
}
