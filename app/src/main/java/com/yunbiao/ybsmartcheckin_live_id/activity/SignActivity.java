package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.app.DatePickerDialog;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.Config;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateSignDataEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.adapter.SignAdapter;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.utils.SdCardUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.ThreadUitls;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import io.reactivex.functions.Consumer;

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

    @Override
    protected void initData() {
//        signDao = APP.getSignDao();
        String today = dateFormatter.format(new Date());
        tv_date.setText(today);
        queryDate = today;
        initSpinner();

        adapter = new SignAdapter(SignActivity.this, mShowList);
        lv_sign_List.setAdapter(adapter);
    }

    private void initSpinner() {
        final String[] modeArray = {getString(R.string.act_sign_tip_qb), getString(R.string.act_sign_tip_yfs), getString(R.string.act_sign_tip_wfs)};
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
                    tips(getString(R.string.act_sign_tip_zwsj));
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
                    tips(getString(R.string.act_sign_tip_zwsj));
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
                        UIUtils.showShort(SignActivity.this, (aBoolean ? getString(R.string.act_sign_tip_sccg) : getString(R.string.act_sign_tip_scsbqjcwl)));
                    }
                });
                break;
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_date:
                Calendar now = Calendar.getInstance();
                new DatePickerDialog(
                        SignActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                String yearStr = year + getString(R.string.base_year);

                                int realMonth = month + 1;
                                String monthStr = realMonth + getString(R.string.base_month);
                                if (realMonth < 10) {
                                    monthStr = "0" + realMonth + getString(R.string.base_month);
                                }

                                String dayStr = dayOfMonth + getString(R.string.base_day);
                                if (dayOfMonth < 10) {
                                    dayStr = "0" + dayOfMonth + getString(R.string.base_day);
                                }
                                String date = yearStr + monthStr + dayStr;
                                tv_date.setText(date);

                                queryDate = date;
                                loadSignList();
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                ).show();
                break;
        }
    }

    private boolean isExporting = false;

    public void exportToUD(View view) {
        if (isExporting) {
            UIUtils.showTitleTip(SignActivity.this, getString(R.string.act_sign_tip_zzdcqsd));
            return;
        }
        isExporting = true;
        String usbDiskPath = SdCardUtils.getUsbDiskPath(this);
        File file = new File(usbDiskPath);
        if (!file.exists()) {
            isExporting = false;
            UIUtils.showTitleTip(SignActivity.this, getString(R.string.act_sign_tip_qcrup));
            return;
        }

        String[] list = file.list();
        for (String s : list) {
            File usbFile = new File(file, s);
            if (usbFile.isDirectory()) {
                file = usbFile;
            }
        }

        final String fileName = "faceRecord_" + dateFormat.format(new Date()) + ".txt";
        final File jsonFile = new File(file, fileName);

        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                List<ExportSignBean> exportList = new ArrayList<>();
                List<Sign> signs = DaoManager.get().queryAll(Sign.class);
                for (Sign sign : signs) {
                    if (sign.isUpload()) {
                        continue;
                    }
                    ExportSignBean exportSignBean = new ExportSignBean();
                    exportSignBean.setEntryid(sign.getEmpId());
                    exportSignBean.setSignTime(sign.getTime());
                    exportList.add(exportSignBean);
                }

                if (exportList.size() <= 0) {
                    isExporting = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UIUtils.showTitleTip(SignActivity.this, getString(R.string.act_sign_tip_sjyqbsc) + "\n" + getString(R.string.act_sign_tip_mykdcdsj));
                        }
                    });
                    return;
                }

                String jsonStr = new Gson().toJson(exportList);

                OutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(jsonFile);
                    outputStream.write(jsonStr.getBytes());
                    outputStream.flush();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (jsonFile.exists()) {
                            UIUtils.showTitleTip(SignActivity.this, getString(R.string.act_sign_tip_dccgwjlj) + ":\n" + jsonFile.getPath());
                        } else {
                            UIUtils.showTitleTip(SignActivity.this, getString(R.string.act_sign_tip_dcsb));
                        }
                    }
                });

                isExporting = false;
            }
        });
    }

    class ExportSignBean {
        private long entryid;
        private long signTime;

        public long getSignTime() {
            return signTime;
        }

        public void setSignTime(long signTime) {
            this.signTime = signTime;
        }

        public long getEntryid() {
            return entryid;
        }

        public void setEntryid(long entryid) {
            this.entryid = entryid;
        }
    }
}