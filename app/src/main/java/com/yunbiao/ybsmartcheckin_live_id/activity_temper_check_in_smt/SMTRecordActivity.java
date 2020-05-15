package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in_smt;

import android.app.DatePickerDialog;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateSignDataEvent;
import com.yunbiao.ybsmartcheckin_live_id.adapter.SignAdapter;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.utils.ExcelUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SdCardUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.ThreadUitls;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.common.util.FileUtil;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import io.reactivex.functions.Consumer;

/**
 * Created by Administrator on 2018/10/10.
 */


public class SMTRecordActivity extends SMTBaseActivity implements View.OnClickListener {

    private static final String TAG = "SignActivity";

    private ListView lv_sign_List;
    private TextView tv_date;
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
    private DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
    private DateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ss");
    private Button btnUpload;
    private SignAdapter adapter;

    @Override
    protected int getLayout() {
        return R.layout.activity_smt_record;
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

        boolean isPrivacy = SpUtils.getBoolean(Constants.Key.PRIVACY_MODE, Constants.Default.PRIVACY_MODE);
        adapter = new SignAdapter(SMTRecordActivity.this, mShowList,isPrivacy);
        lv_sign_List.setAdapter(adapter);
    }

    private String formatDate(String date){
        return date.replace("年","-").replace("月","-").replace("日","");
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
        spnDataMode.setSelection(modeArray.length - 1);
    }

    private void loading() {
        pb_load_list.post(new Runnable() {
            @Override
            public void run() {
                pb_load_list.setVisibility(View.VISIBLE);
                tv_load_tips.setVisibility(View.GONE);
            }
        });
    }

    private void tips(final String tips) {
        tv_load_tips.post(new Runnable() {
            @Override
            public void run() {
                lv_sign_List.setVisibility(View.GONE);
                pb_load_list.setVisibility(View.GONE);
                tv_load_tips.setVisibility(View.VISIBLE);
                tv_load_tips.setText(tips);
            }
        });
    }

    private void hide() {
        tv_load_tips.post(new Runnable() {
            @Override
            public void run() {
                pb_load_list.setVisibility(View.GONE);
                tv_load_tips.setVisibility(View.GONE);
            }
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
                mSignList = DaoManager.get().querySignByComIdAndDate(SpUtils.getInt(SpUtils.COMPANYID), queryDate);
                if (mSignList == null || mSignList.size() <= 0) {
                    tips(getString(R.string.sign_list_no_data));
                    return;
                }

                //删除类型为-9的记录
                /*Iterator<Sign> iterator = mSignList.iterator();
                while (iterator.hasNext()) {
                    Sign next = iterator.next();
                    if (next.getType() == -9) {
                        iterator.remove();
                    }
                }*/

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
                UIUtils.showNetLoading(this);
                SignManager.instance().uploadSignRecord(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            EventBus.getDefault().post(new UpdateSignDataEvent());
                        }
                        UIUtils.dismissNetLoading();
                        UIUtils.showShort(SMTRecordActivity.this, (aBoolean ? getString(R.string.sign_list_upload_success) : getString(R.string.sign_list_upload_failed)));
                    }
                });
                break;
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_date:
                Calendar now = Calendar.getInstance();
                new DatePickerDialog(
                        SMTRecordActivity.this,
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

    private boolean isExporting = false;

    public void exportToUD(final View view) {
        //获取U盘地址
        String usbDiskPath = SdCardUtils.getUsbDiskPath(this);
        File file = new File(usbDiskPath);
        if (!file.exists()) {
            isExporting = false;
            UIUtils.showTitleTip(SMTRecordActivity.this, getString(R.string.sign_list_tip_usb_disk));
            usbDiskPath = Environment.getExternalStorageDirectory().getPath();
            file = new File(usbDiskPath);
        }
        /*String[] list = file.list();
        for (String s : list) {
            File usbFile = new File(file, s);
            if (usbFile.isDirectory()) {
                file = usbFile;
            }
        }*/
        //创建记录最外层目录
        final File dirFile = new File(file, dateFormat.format(new Date()) + "_" + getResources().getString(R.string.sign_export_record));
        if (!dirFile.exists()) {
            boolean mkdirs = dirFile.mkdirs();
            Log.e(TAG, "exportToUD: 创建外层目录：" + dirFile.getPath() + " --- " + mkdirs);
        }
        Log.e(TAG, "exportToUD: " + dirFile.getPath() );

        //创建图片目录
        final File imgDir = new File(dirFile, "image");
        if (!imgDir.exists()) {
            boolean mkdirs = imgDir.mkdirs();
            Log.e(TAG, "exportToUD: 创建图片目录：" + imgDir.getPath() + " --- " + mkdirs);
        }
        //创建xls文件
        final File excelFile = new File(dirFile, dateFormat.format(new Date()) + "_" + getResources().getString(R.string.sign_export_record) + ".xls");
        //获取源数据
        final List<Sign> signs = DaoManager.get().querySignByComId(SpUtils.getCompany().getComid());
        Log.e(TAG, "export: 源数据：" + (signs == null ? 0 : signs.size()));
        if (signs == null || signs.size() <= 0) {
            UIUtils.showShort(this, getResources().getString(R.string.sign_export_no_record));
            return;
        }
        //生成导出数据
        final List<List<String>> tableData = createTableData(signs);
        Log.e(TAG, "export: 导出数据：" + tableData.size());
        if (tableData == null || tableData.size() <= 0) {
            UIUtils.showShort(this, getResources().getString(R.string.sign_export_create_record_failed));
            return;
        }
        UIUtils.showNetLoading(this);
        view.setEnabled(false);
        //开始导出
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "run: 拷贝图片");
                for (Sign sign : signs) {
                    String headPath = sign.getHeadPath();
                    String hotImgPath = sign.getHotImgPath();

                    if (!TextUtils.isEmpty(headPath)) {
                        File headFile = new File(headPath);
                        if (headFile.exists()) {
                            boolean copy = FileUtil.copy(headFile.getPath(), imgDir.getPath() + File.separator + headFile.getName());
                            Log.e(TAG, "run: 头像，拷贝结果：" + copy);
                        }
                    }

                    if (!TextUtils.isEmpty(hotImgPath)) {
                        File hotImageFile = new File(hotImgPath);
                        if (hotImageFile.exists()) {
                            boolean copy = FileUtil.copy(hotImageFile.getPath(), imgDir.getPath() + File.separator + hotImageFile.getName());
                            Log.e(TAG, "run: 热图，拷贝结果：" + copy);
                        }
                    }
                }

                ExcelUtils.initExcel(excelFile.getPath(), getResString(R.string.sign_list_table_name),title);
                final boolean result = ExcelUtils.writeObjListToExcel(tableData, excelFile.getPath());
                Log.e(TAG, "run: excel，导出结果：" + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.setEnabled(true);
                        UIUtils.dismissNetLoading();
                        UIUtils.showShort(SMTRecordActivity.this, result
                                ? (getResources().getString(R.string.sign_export_record_success) +
                                "\n" +
                                getResources().getString(R.string.sign_export_record_path) + dirFile.getPath())
                                : getResources().getString(R.string.sign_export_record_failed));
                    }
                });
            }
        });
    }

    public List<List<String>> createTableData(List<Sign> signs) {
        List<List<String>> tableDatas = new ArrayList<>();
        if (signs != null) {
            for (int i = 0; i < signs.size(); i++) {
                Sign sign = signs.get(i);
                List<String> beanList = new ArrayList<>();

                beanList.add(TextUtils.isEmpty(sign.getName()) ? getResString(R.string.fment_sign_visitor_name) : sign.getName());
                beanList.add(TextUtils.isEmpty(sign.getEmployNum()) ? "" : sign.getEmployNum());
                beanList.add(TextUtils.isEmpty(sign.getDepart()) ? sign.getDepart() : "");
                beanList.add(TextUtils.isEmpty(sign.getPosition()) ? "" : "");
                beanList.add(dateFormat1.format(sign.getTime()) + "");
                beanList.add(dateFormat2.format(sign.getTime()) + "");
                beanList.add(sign.getTemperature() + "");
                beanList.add(new File(sign.getHeadPath()).getName() + "");
                beanList.add(TextUtils.isEmpty(sign.getHotImgPath()) ? "" : new File(sign.getHotImgPath()).getName());
                tableDatas.add(beanList);
            }
        }
        return tableDatas;
    }

    private String getSDPath() {
        File sdDir = new File(Environment.getExternalStorageDirectory().getPath());
        return sdDir.getPath();
    }

    public void makeDir(File dir) {
        if (!dir.getParentFile().exists()) {
            makeDir(dir.getParentFile());
        }
        dir.mkdir();
    }
}