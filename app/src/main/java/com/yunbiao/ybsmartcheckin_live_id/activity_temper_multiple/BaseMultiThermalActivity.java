package com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.yunbiao.ybsmartcheckin_live_id.ButtonClickListener;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseGpioActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.utils.ExcelUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.NetWorkChangReceiver;
import com.yunbiao.ybsmartcheckin_live_id.utils.SdCardUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.ThreadUitls;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import org.xutils.common.util.FileUtil;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import io.reactivex.functions.Consumer;

public abstract class BaseMultiThermalActivity extends BaseGpioActivity implements NetWorkChangReceiver.NetWorkChangeListener {

    private TextView tvNetState;

    @Override
    protected int getPortraitLayout() {
        return getLayout();
    }

    @Override
    protected int getLandscapeLayout() {
        return getLayout();
    }

    protected abstract int getLayout();

    @Override
    protected void initView() {
        super.initView();
        NetWorkChangReceiver netWorkChangReceiver = new NetWorkChangReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkChangReceiver, filter);
    }

    /**
     * 获取对应的人脸框
     * @param ftRect
     * @return
     */
    protected Rect adjustRect(int previewWidth, int previewHeight, Rect ftRect) {
        if (ftRect == null) {
            return null;
        }

        int canvasWidth = 80;
        int canvasHeight = 60;
        float horizontalRatio = (float) canvasWidth / (float) previewWidth;
        float verticalRatio = (float) canvasHeight / (float) previewHeight;

        Rect rect = new Rect(ftRect);
        rect.left *= horizontalRatio;
        rect.right *= horizontalRatio;
        rect.top *= verticalRatio;
        rect.bottom *= verticalRatio;

        if(rect.left <= 5){
            rect.left += 4;
            rect.right += 6;
        } else if(rect.left < 13){
            rect.left += 1;
            rect.right += 3;
        } else if(rect.right >= 70 && rect.right < 78){
            rect.left -= 1;
        } if(rect.right >= 78){
            rect.right -= 1;
            rect.left -= 3;
        } else {
            rect.left += 1;
            rect.right += 3;
        }

        rect.top -= 7;
        return rect;
    }

    //显示系统信息
    private PopupWindow systemInfoPopup;
    private CountDownTimer countDownTimer;
    protected void showSystemInfoPopup() {
        if (systemInfoPopup != null && systemInfoPopup.isShowing()) {
            dissmissSystemInfo();
            return;
        }
        if (systemInfoPopup == null) {
            systemInfoPopup = new PopupWindow(this);
            systemInfoPopup.setContentView(View.inflate(this, R.layout.popup_layout_multi_thermal, null));
            systemInfoPopup.setWidth(400);
            systemInfoPopup.setHeight(500);
            systemInfoPopup.setAnimationStyle(R.style.multi_thermal_system_info_anim_style);
        }

        //设备编号
        final View rootView = systemInfoPopup.getContentView();
        final String deviceNo = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        final TextView tvDeviceNo = rootView.findViewById(R.id.tv_device_no_info_multi_thermal);
        tvDeviceNo.setText(deviceNo);
        //绑定码
        final String bindCode = SpUtils.getStr(SpUtils.BIND_CODE);
        final TextView tvBindCode = rootView.findViewById(R.id.tv_bindcode_info_multi_thermal);
        tvBindCode.setText(bindCode);
        //公司
        TextView tvCompany = rootView.findViewById(R.id.tv_company_info_multi_thermal);
        int comid = SpUtils.getCompany().getComid();
        String compName = SpUtils.getCompany().getComname();
        if (comid == Constants.NOT_BIND_COMPANY_ID) {
            tvCompany.setText(getResources().getString(R.string.smt_main_not_bind));
        } else {
            tvCompany.setText(compName);
        }
        //网络状态
        tvNetState = rootView.findViewById(R.id.tv_net_state_info_multi_thermal);
        if (isNetConnected) {
            tvNetState.setText(getResources().getString(R.string.smt_main_net_normal));
            tvNetState.setTextColor(Color.GREEN);
        } else {
            tvNetState.setText(getResources().getString(R.string.smt_main_net_no));
            tvNetState.setTextColor(Color.RED);
        }

        //倒计时
        final TextView tvTimeInfo = rootView.findViewById(R.id.tv_time_info_multi_thermal);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimeInfo.setText((millisUntilFinished / 1000) + "");
                String deviceNo2 = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
                if (TextUtils.isEmpty(deviceNo) && !TextUtils.isEmpty(deviceNo2)) {
                    tvDeviceNo.setText(deviceNo2);
                }
                String bindCode2 = SpUtils.getStr(SpUtils.BIND_CODE);
                if (TextUtils.isEmpty(bindCode) && !TextUtils.isEmpty(bindCode2)) {
                    tvBindCode.setText(bindCode2);
                }
            }

            @Override
            public void onFinish() {
                dissmissSystemInfo();
            }
        };
        countDownTimer.start();
        //隐藏
        View ivHidden = rootView.findViewById(R.id.iv_hidden_info_multi_thermal);
        ivHidden.setOnClickListener(v -> dissmissSystemInfo());
        //跳转
        View btnSignList = rootView.findViewById(R.id.btn_sign_list_multi_thermal);
        btnSignList.setOnClickListener(v -> {
            dissmissSystemInfo();
            showSignListPopup();
        });

        rootView.findViewById(R.id.iv_setting_info_multi_thermal).setOnClickListener(v -> {
            dissmissSystemInfo();
            goSetting();
        });

        View decorView = getWindow().getDecorView();
        systemInfoPopup.showAtLocation(decorView, Gravity.LEFT | Gravity.CENTER_VERTICAL, 0, 0);
    }

    private void dissmissSystemInfo() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        if (systemInfoPopup != null && systemInfoPopup.isShowing()) {
            systemInfoPopup.dismiss();
        }
    }

    private boolean isNetConnected = false;

    @Override
    public void connect() {
        isNetConnected = true;
        if (tvNetState != null) {
            tvNetState.setText(getResources().getString(R.string.smt_main_net_normal));
            tvNetState.setTextColor(Color.GREEN);
        }
    }

    @Override
    public void disConnect() {
        isNetConnected = false;
        if (tvNetState != null) {
            tvNetState.setText(getResources().getString(R.string.smt_main_net_no));
            tvNetState.setTextColor(Color.RED);
        }
    }

    private static final String TAG = "BaseMultiThermalActivit";
    //显示记录列表
    private PopupWindow signListPopupWindow;
    private CountDownTimer signListCountDownTimer;

    private void showSignListPopup() {
        if (signListPopupWindow != null && signListPopupWindow.isShowing()) {
            return;
        }

        if (signListPopupWindow == null) {
            signListPopupWindow = new PopupWindow(this);
            signListPopupWindow.setContentView(View.inflate(this, R.layout.popup_sign_multi_thermal, null));
            signListPopupWindow.setWidth(1200);
            signListPopupWindow.setAnimationStyle(R.style.multi_thermal_system_info_anim_style);
        }

        final View rootView = signListPopupWindow.getContentView();
        final TextView tvNoData = rootView.findViewById(R.id.tv_no_date_record);
        final Spinner spinner = rootView.findViewById(R.id.spn_date);
        final ProgressBar progressBar = rootView.findViewById(R.id.pb_load_data_record);
        final ListView lvRecord = rootView.findViewById(R.id.lv_record);
        final TextView tvCountTime = rootView.findViewById(R.id.tv_counttime_record_multi_thermal);
        rootView.findViewById(R.id.iv_export).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportToUD(v);
            }
        });
        rootView.findViewById(R.id.iv_hidden_record_multi_thermal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dissmissSignList();
            }
        });

        loadRecord(signs -> {
            progressBar.setVisibility(View.GONE);
            if (signs == null || signs.size() <= 0) {
                tvNoData.setVisibility(View.VISIBLE);
                return;
            }
            tvNoData.setVisibility(View.GONE);
            boolean aBoolean = SpUtils.getBoolean(Constants.Key.PRIVACY_MODE, Constants.Default.PRIVACY_MODE);
            lvRecord.setAdapter(new MultiRecordAdapter(BaseMultiThermalActivity.this, signs,aBoolean));
        });

        lvRecord.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    if (signListCountDownTimer != null) {
                        signListCountDownTimer.cancel();
                        signListCountDownTimer = null;
                        signListCountDownTimer = new CountDownTimer(120000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                tvCountTime.setText((millisUntilFinished / 1000) + "");
                            }

                            @Override
                            public void onFinish() {
                                dissmissSignList();
                            }
                        };
                        signListCountDownTimer.start();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        signListCountDownTimer = new CountDownTimer(120000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvCountTime.setText((millisUntilFinished / 1000) + "");
            }

            @Override
            public void onFinish() {
                dissmissSignList();
            }
        };
        signListCountDownTimer.start();

        dissmissSystemInfo();

        View decorView = getWindow().getDecorView();
        signListPopupWindow.showAtLocation(decorView, Gravity.LEFT | Gravity.CENTER_VERTICAL, 0, 0);
    }

    private void dissmissSignList() {
        if (signListCountDownTimer != null) {
            signListCountDownTimer.cancel();
            signListCountDownTimer = null;
        }
        if (signListPopupWindow != null && signListPopupWindow.isShowing()) {
            signListPopupWindow.dismiss();
        }
    }

    private void loadRecord(final Consumer<List<Sign>> resultConsumer) {
        if (resultConsumer == null) {
            return;
        }
        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                int comid = SpUtils.getCompany().getComid();
                final List<Sign> signs = DaoManager.get().querySignByComId(comid);
                runOnUiThread(() -> {
                    try {
                        resultConsumer.accept(signs);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }


    private void goSetting() {
        String pwd = SpUtils.getStr(SpUtils.MENU_PWD);
        if (!TextUtils.isEmpty(pwd)) {
            inputPwd(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(BaseMultiThermalActivity.this, MultiThermalSettingActivity.class));
                }
            });
            return;
        }
        startActivity(new Intent(this, MultiThermalSettingActivity.class));
        overridePendingTransition(R.anim.left_in, R.anim.left_out);
    }

    //密码弹窗
    private void inputPwd(final Runnable runnable) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_input_pwd);

        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_edt_shake);
        final View rootView = dialog.findViewById(R.id.ll_input_pwd);
        Button btnConfirm = (Button) dialog.findViewById(R.id.btn_input_confirm);
        final EditText edtPwd = (EditText) dialog.findViewById(R.id.edt_input_pwd);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = edtPwd.getText().toString();
                if (TextUtils.isEmpty(pwd)) {
                    edtPwd.setError(getString(R.string.act_wel_error_bywjsrmmo));
                    rootView.startAnimation(animation);
                    return;
                }
                String spPwd = SpUtils.getStr(SpUtils.MENU_PWD);
                if (!TextUtils.equals(pwd, spPwd)) {
                    edtPwd.setError(getString(R.string.act_wel_error_mmclqcxsrb));
                    rootView.startAnimation(animation);
                    return;
                }
                if (runnable != null) {
                    runnable.run();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int width = dm.widthPixels;
        final Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.mystyle);  //添加动画
        window.setLayout(width / 2, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);
    }


    private boolean isExporting = false;
    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private String[] title;

    private void exportToUD(final View view) {
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

        //获取U盘地址
        String usbDiskPath = SdCardUtils.getUsbDiskPath(this);
        File file = new File(usbDiskPath);
        if (!file.exists()) {
            isExporting = false;
            UIUtils.showTitleTip(BaseMultiThermalActivity.this, getString(R.string.sign_list_tip_usb_disk));
            usbDiskPath = Environment.getExternalStorageDirectory().getPath();
            file = new File(usbDiskPath);
        }
        //创建记录最外层目录
        final File dirFile = new File(file, dateFormat.format(new Date()) + "_" + getResources().getString(R.string.sign_export_record));
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

                ExcelUtils.initExcel(excelFile.getPath(), title);
                final boolean result = ExcelUtils.writeObjListToExcel(tableData, excelFile.getPath());
                Log.e(TAG, "run: excel，导出结果：" + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.setEnabled(true);
                        UIUtils.dismissNetLoading();
                        UIUtils.showShort(BaseMultiThermalActivity.this, result
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

                beanList.add(TextUtils.isEmpty(sign.getName()) ? "" : sign.getName());
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

    private DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
    private DateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ss");
}
