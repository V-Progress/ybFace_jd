package com.yunbiao.ybsmartcheckin_live_id.temper_12inch.activity;

import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioGroup;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.common.UpdateVersionControl;
import com.yunbiao.ybsmartcheckin_live_id.databinding.Activity12inchSettingBinding;
import com.yunbiao.ybsmartcheckin_live_id.temper_12inch.Temper12InchConst;
import com.yunbiao.ybsmartcheckin_live_id.temper_12inch.databean.Setting12InchDataBean;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.Temper5InchConst;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.utils.BigDecimalUtils;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.utils.ResourceUtils;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.utils.TemperatureUnitUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.NetWorkChangReceiver;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;

public class Setting12InchActivity extends Base12InchActivity implements NetWorkChangReceiver.NetWorkChangeListener {

    private Activity12inchSettingBinding activity12inchSettingBinding;
    private Setting12InchDataBean setting12InchDataBean;

    private NetWorkChangReceiver netWorkChangReceiver;
    private boolean isNetConnected = false;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_12inch_setting;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_12inch_setting;
    }

    @Override
    protected void initView() {
        activity12inchSettingBinding = DataBindingUtil.setContentView(this, R.layout.activity_12inch_setting);

        setting12InchDataBean = new Setting12InchDataBean(
                new ObservableField<>(String.valueOf(SpUtils.getFloat(Temper5InchConst.Key.CALIBRATION_VALUE, Temper5InchConst.Default.calibrationValueDef))),
                new ObservableField<>(String.valueOf(SpUtils.getFloat(Temper5InchConst.Key.WARNING_VALUE, Temper5InchConst.Default.warningValueDef)))
        );

        activity12inchSettingBinding.setSetting12InchDataBean(setting12InchDataBean);

        if (SpUtils.getIntOrDef(Temper12InchConst.Key.TEMPERATURE_UNIT, Temper12InchConst.Default.temperatureUnitDef) == 1) {
            activity12inchSettingBinding.rbCelsius.setChecked(true);
        } else {
            activity12inchSettingBinding.rbFahrenheit.setChecked(true);
        }

        if (SpUtils.getIntOrDef(Constants.Key.SERVER_MODEL, Constants.Default.SERVER_MODEL) == Constants.serverModel.YUN) {
            activity12inchSettingBinding.rbYun.setChecked(true);
            setServerInfo(Constants.serverModel.YUN);
        } else {
            activity12inchSettingBinding.rbJu.setChecked(true);
            setServerInfo(Constants.serverModel.JU);
        }

        setting12InchDataBean.deviceNo.set(SpUtils.getStr(SpUtils.DEVICE_NUMBER));
        setting12InchDataBean.bindCode.set(SpUtils.getStr(SpUtils.BIND_CODE));
        int comid = SpUtils.getCompany().getComid();
        String compName = SpUtils.getCompany().getComname();
        if (comid == Constants.NOT_BIND_COMPANY_ID) {
            setting12InchDataBean.companyName.set(ResourceUtils.getStringResource(R.string.smt_main_not_bind));
        } else {
            setting12InchDataBean.companyName.set(compName);
        }
        setNetStateInfo();

        setEditTextSelection();

        activity12inchSettingBinding.setEventListener(new EventListener());
    }

    @Override
    protected void initData() {
        netWorkChangReceiver = new NetWorkChangReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkChangReceiver, filter);

        checkUpgrade(new CheckUpgradeCallback() {
            @Override
            public void onStart() {
                activity12inchSettingBinding.flVersion5inchSystem.setVisibility(View.VISIBLE);
            }

            @Override
            public void noUpgrade(String currVersionName) {
                setting12InchDataBean.versionName.set(getResString(R.string.update_lable_current) + currVersionName);
                setting12InchDataBean.versionInfo.set(getResString(R.string.updateManager_dqbbwzxbb));
                activity12inchSettingBinding.tvVersionInfo5inchSystem.setGravity(Gravity.CENTER);
                activity12inchSettingBinding.tvVersionInfo5inchSystem.setTextColor(Color.GREEN);
            }

            @Override
            public void haveNewVersion(String newVersionName, String versionInfo) {
                setting12InchDataBean.versionName.set(getResString(R.string.update_lable_new) + newVersionName);
                setting12InchDataBean.versionInfo.set(TextUtils.isEmpty(versionInfo) ? getResString(R.string.update_no_description) : versionInfo);
                activity12inchSettingBinding.tvVersionInfo5inchSystem.setGravity(Gravity.LEFT);
                activity12inchSettingBinding.tvVersionInfo5inchSystem.setTextColor(Color.WHITE);
            }

            @Override
            public void onError(String currVersionName, String s) {
                setting12InchDataBean.versionName.set(getResString(R.string.update_lable_current) + currVersionName);
                setting12InchDataBean.versionInfo.set(getResString(R.string.update_check_failed));
                activity12inchSettingBinding.tvVersionInfo5inchSystem.setGravity(Gravity.CENTER);
                activity12inchSettingBinding.tvVersionInfo5inchSystem.setTextColor(Color.GRAY);
            }

            @Override
            public void onFinish() {
                activity12inchSettingBinding.flVersion5inchSystem.setVisibility(View.GONE);
            }
        });
    }

    private void setNetStateInfo() {
        if (isNetConnected) {
            setting12InchDataBean.netStateInfo.set(ResourceUtils.getStringResource(R.string.smt_main_net_normal2));
            activity12inchSettingBinding.tvNetStateInfo5inch.setTextColor(Color.GREEN);
        } else {
            setting12InchDataBean.netStateInfo.set(ResourceUtils.getStringResource(R.string.smt_main_net_no));
            activity12inchSettingBinding.tvNetStateInfo5inch.setTextColor(Color.RED);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveConfiguration();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (netWorkChangReceiver != null) {
            unregisterReceiver(netWorkChangReceiver);
        }
    }

    private void saveConfiguration() {
        int temperatureUnit = 1;
        if (activity12inchSettingBinding.rbCelsius.isChecked()) {
            temperatureUnit = 1;
        } else if (activity12inchSettingBinding.rbFahrenheit.isChecked()) {
            temperatureUnit = 2;
        }
        SpUtils.saveInt(Temper12InchConst.Key.TEMPERATURE_UNIT, temperatureUnit);
        if (TextUtils.isEmpty(setting12InchDataBean.calibrationContent.get())) {
            SpUtils.saveFloat(Temper12InchConst.Key.CALIBRATION_VALUE, Temper12InchConst.Default.calibrationValueDef);
        } else {
            SpUtils.saveFloat(Temper12InchConst.Key.CALIBRATION_VALUE, Float.valueOf(setting12InchDataBean.calibrationContent.get()));
        }
        if (TextUtils.isEmpty(setting12InchDataBean.warningContent.get())) {
            if (temperatureUnit == 1) {
                SpUtils.saveFloat(Temper12InchConst.Key.WARNING_VALUE, Temper12InchConst.Default.warningValueDef);
            } else {
                SpUtils.saveFloat(Temper12InchConst.Key.WARNING_VALUE, TemperatureUnitUtils.c2f(Temper5InchConst.Default.warningValueDef));
            }
        } else {
            SpUtils.saveFloat(Temper12InchConst.Key.WARNING_VALUE, Float.valueOf(setting12InchDataBean.warningContent.get()));
        }
    }

    private void setServerInfo(int model) {
        String ip = Constants.NetConfig.PRO_URL;
        String resPort = Constants.NetConfig.PRO_RES_PORT;
        String xmppPort = Constants.NetConfig.PRO_XMPP_PORT;
        String proName = Constants.NetConfig.PRO_SUFFIX;
        if (model == Constants.serverModel.YUN) {
            setting12InchDataBean.serverInfoEnabled.set(false);
        } else {
            setting12InchDataBean.serverInfoEnabled.set(true);

            ip = SpUtils.getStr(Constants.Key.JU_XMPP_IP_CACHE);
            resPort = SpUtils.getStr(Constants.Key.JU_RESOURCE_PORT_CACHE);
            xmppPort = SpUtils.getStr(Constants.Key.JU_XMPP_PORT_CACHE);
            proName = SpUtils.getStr(Constants.Key.JU_PROJECT_NAME_SUFFIX);
        }
        setting12InchDataBean.serverIp.set(ip);
        setting12InchDataBean.serverResPort.set(resPort);
        setting12InchDataBean.serverXmppPort.set(xmppPort);
        setting12InchDataBean.serverProName.set(proName);
    }

    public void setEditTextSelection() {
        if (activity12inchSettingBinding != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    activity12inchSettingBinding.etCalibrationContent.setSelection(setting12InchDataBean.getCalibrationContent().get().length());
                    activity12inchSettingBinding.etWaringContent.setSelection(setting12InchDataBean.getWarningContent().get().length());
                }
            }, 200);
        }
    }

    @Override
    public void connect() {
        isNetConnected = true;
        setNetStateInfo();
    }

    @Override
    public void disConnect() {
        isNetConnected = false;
        setNetStateInfo();
    }

    private SubOrAddThread subOrAddThread;
    private boolean isOnLongClick = false;

    public class EventListener {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_close_btn:
                    finish();
                    break;
                case R.id.btn_save_address:
                    String mIp = setting12InchDataBean.serverIp.get();
                    String mResPort = setting12InchDataBean.serverResPort.get();
                    String mXmppPort = setting12InchDataBean.serverXmppPort.get();
                    String mProName = setting12InchDataBean.serverProName.get();
                    if (TextUtils.isEmpty(mIp)) {
                        UIUtils.showTitleTip(Setting12InchActivity.this, getResString(R.string.setting_please_set_ip));
                        return;
                    }
                    if (TextUtils.isEmpty(mResPort)) {
                        UIUtils.showTitleTip(Setting12InchActivity.this, getResString(R.string.setting_please_set_res));
                        return;
                    }
                    int intResPort = Integer.parseInt(mResPort);
                    if(intResPort > 65535){
                        UIUtils.showTitleTip(Setting12InchActivity.this, getResString(R.string.setting_res_port_error));
                        return;
                    }
                    if (TextUtils.isEmpty(mXmppPort)) {
                        UIUtils.showTitleTip(Setting12InchActivity.this, getResString(R.string.setting_please_set_xmpp));
                        return;
                    }
                    int intXmppPort = Integer.parseInt(mXmppPort);
                    if(intXmppPort > 65535){
                        UIUtils.showTitleTip(Setting12InchActivity.this, getResString(R.string.setting_xmpp_port_error));
                        return;
                    }
                    if (TextUtils.isEmpty(mProName)) {
                    }
                    if (activity12inchSettingBinding.rbYun.isChecked()) {
                        SpUtils.saveInt(Constants.Key.SERVER_MODEL, Constants.serverModel.YUN);
                    } else if (activity12inchSettingBinding.rbJu.isChecked()) {
                        SpUtils.saveInt(Constants.Key.SERVER_MODEL, Constants.serverModel.JU);
                        SpUtils.saveStr(Constants.Key.JU_XMPP_IP_CACHE, mIp);
                        SpUtils.saveStr(Constants.Key.JU_RESOURCE_PORT_CACHE, mResPort);
                        SpUtils.saveStr(Constants.Key.JU_XMPP_PORT_CACHE, mXmppPort);
                        SpUtils.saveStr(Constants.Key.JU_PROJECT_NAME_SUFFIX, mProName);
                    }
                    UIUtils.showTitleTip(Setting12InchActivity.this, getResString(R.string.setting_save_succ_please_restart));
                    break;
                case R.id.btn_update_system:
                    UpdateVersionControl.getInstance().checkUpdate(Setting12InchActivity.this);
                    break;
            }
        }

        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.fl_sub_btn:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        subOrAddThread = new SubOrAddThread(false);
                        isOnLongClick = true;
                        subOrAddThread.start();
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        isOnLongClick = false;
                        subOrAddThread = null;
                        setEditTextSelection();
                    }
                    break;
                case R.id.fl_add_btn:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        subOrAddThread = new SubOrAddThread(true);
                        isOnLongClick = true;
                        subOrAddThread.start();
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        isOnLongClick = false;
                        subOrAddThread = null;
                        setEditTextSelection();
                    }
                    break;
            }
            return false;
        }

        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (group.getId()) {
                case R.id.rg_display:
                    switch (checkedId) {
                        case R.id.rb_celsius:
                            setting12InchDataBean.warningContent.set(String.valueOf(TemperatureUnitUtils.f2c(Float.valueOf(setting12InchDataBean.warningContent.get()))));
                            break;
                        case R.id.rb_fahrenheit:
                            setting12InchDataBean.warningContent.set(String.valueOf(TemperatureUnitUtils.c2f(Float.valueOf(setting12InchDataBean.warningContent.get()))));
                            break;
                    }
                    break;
                case R.id.rg_server_model:
                    switch (checkedId) {
                        case R.id.rb_yun:
                            setServerInfo(Constants.serverModel.YUN);
                            break;
                        case R.id.rb_ju:
                            setServerInfo(Constants.serverModel.JU);
                            break;
                    }
                    break;
            }
        }
    }

    //加减操作
    private class SubOrAddThread extends Thread {
        private boolean isAdd;
        public SubOrAddThread(boolean isAdd) {
            this.isAdd = isAdd;
        }
        @Override
        public void run() {
            while (isOnLongClick) {
                try {
                    Thread.sleep(150);
                    if (isAdd) {
                        setting12InchDataBean.calibrationContent.set(BigDecimalUtils.add(setting12InchDataBean.calibrationContent.get(), "0.1", 1).toString());
                    } else {
                        setting12InchDataBean.calibrationContent.set(BigDecimalUtils.sub(setting12InchDataBean.calibrationContent.get(), "0.1", 1).toString());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            super.run();
        }
    }
}
