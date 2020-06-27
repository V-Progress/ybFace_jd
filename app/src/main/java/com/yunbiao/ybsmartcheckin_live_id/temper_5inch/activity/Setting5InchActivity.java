package com.yunbiao.ybsmartcheckin_live_id.temper_5inch.activity;

import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioGroup;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.common.UpdateVersionControl;
import com.yunbiao.ybsmartcheckin_live_id.databinding.Activity5inchSettingBinding;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.Temper5InchConst;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.databean.SettingDataBean;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.utils.BigDecimalUtils;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.utils.TemperatureUnitUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;

public class Setting5InchActivity extends Base5InchActivity {

    private Activity5inchSettingBinding activity5inchSettingBinding;
    private SettingDataBean settingDataBean;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_5inch_setting;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_5inch_setting;
    }

    @Override
    protected void initView() {
        activity5inchSettingBinding = DataBindingUtil.setContentView(this, R.layout.activity_5inch_setting);

        settingDataBean = new SettingDataBean(
                new ObservableField<>(String.valueOf(SpUtils.getFloat(Temper5InchConst.Key.CALIBRATION_VALUE, Temper5InchConst.Default.calibrationValueDef))),
                new ObservableField<>(String.valueOf(SpUtils.getIntOrDef(Temper5InchConst.Key.CAROUSEL_INTERVAL, Temper5InchConst.Default.carouselIntervalDef))),
                new ObservableField<>(String.valueOf(SpUtils.getFloat(Temper5InchConst.Key.WARNING_VALUE, Temper5InchConst.Default.warningValueDef)))
        );

        activity5inchSettingBinding.setSettingDataBean(settingDataBean);

        if (SpUtils.getIntOrDef(Temper5InchConst.Key.TEMPERATURE_UNIT, Temper5InchConst.Default.temperatureUnitDef) == 1) {
            activity5inchSettingBinding.rbCelsius.setChecked(true);
        } else {
            activity5inchSettingBinding.rbFahrenheit.setChecked(true);
        }

        int subtitleSpeed = SpUtils.getIntOrDef(Temper5InchConst.Key.SUBTITLE_SPEED, Temper5InchConst.Default.subtitleSpeedDef);
        if (subtitleSpeed == 1) {
            activity5inchSettingBinding.rbFast.setChecked(true);
        } else if (subtitleSpeed == 2) {
            activity5inchSettingBinding.rbModerate.setChecked(true);
        } else if (subtitleSpeed == 3) {
            activity5inchSettingBinding.rbSlow.setChecked(true);
        }

        int angle = SpUtils.getIntOrDef(Constants.Key.CAMERA_ANGLE, Constants.Default.CAMERA_ANGLE);
        if (angle == 0) {
            activity5inchSettingBinding.rb0.setChecked(true);
        } else if (angle == 90) {
            activity5inchSettingBinding.rb90.setChecked(true);
        } else if (angle == 180) {
            activity5inchSettingBinding.rb180.setChecked(true);
        } else if (angle == 270) {
            activity5inchSettingBinding.rb270.setChecked(true);
        }

        if (SpUtils.getBoolean(Constants.Key.IS_H_MIRROR, true)) {
            activity5inchSettingBinding.cbMirror.setChecked(true);
        } else {
            activity5inchSettingBinding.cbMirror.setChecked(false);
        }

        if (SpUtils.getIntOrDef(Constants.Key.SERVER_MODEL, Constants.Default.SERVER_MODEL) == Constants.serverModel.YUN) {
            activity5inchSettingBinding.rbYun.setChecked(true);
            setServerInfo(Constants.serverModel.YUN);
        } else {
            activity5inchSettingBinding.rbJu.setChecked(true);
            setServerInfo(Constants.serverModel.JU);
        }

        setEditTextSelection();

        activity5inchSettingBinding.setEventListener(new EventListener());
    }

    @Override
    protected void initData() {
        checkUpgrade(new CheckUpgradeCallback() {
            @Override
            public void onStart() {
                activity5inchSettingBinding.flVersion5inchSystem.setVisibility(View.VISIBLE);
            }

            @Override
            public void noUpgrade(String currVersionName) {
                settingDataBean.versionName.set(getResString(R.string.update_lable_current) + currVersionName);
                settingDataBean.versionInfo.set(getResString(R.string.updateManager_dqbbwzxbb));
                activity5inchSettingBinding.tvVersionInfo5inchSystem.setGravity(Gravity.CENTER);
                activity5inchSettingBinding.tvVersionInfo5inchSystem.setTextColor(Color.GREEN);
            }

            @Override
            public void haveNewVersion(String newVersionName, String versionInfo) {
                settingDataBean.versionName.set(getResString(R.string.update_lable_new) + newVersionName);
                settingDataBean.versionInfo.set(TextUtils.isEmpty(versionInfo) ? getResString(R.string.update_no_description) : versionInfo);
                activity5inchSettingBinding.tvVersionInfo5inchSystem.setGravity(Gravity.LEFT);
                activity5inchSettingBinding.tvVersionInfo5inchSystem.setTextColor(Color.WHITE);
            }

            @Override
            public void onError(String currVersionName, String s) {
                settingDataBean.versionName.set(getResString(R.string.update_lable_current) + currVersionName);
                settingDataBean.versionInfo.set(getResString(R.string.update_check_failed));
                activity5inchSettingBinding.tvVersionInfo5inchSystem.setGravity(Gravity.CENTER);
                activity5inchSettingBinding.tvVersionInfo5inchSystem.setTextColor(Color.GRAY);
            }

            @Override
            public void onFinish() {
                activity5inchSettingBinding.flVersion5inchSystem.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveConfiguration();
    }

    private void saveConfiguration() {
        int temperatureUnit = 1;
        if (activity5inchSettingBinding.rbCelsius.isChecked()) {
            temperatureUnit = 1;
        } else if (activity5inchSettingBinding.rbFahrenheit.isChecked()) {
            temperatureUnit = 2;
        }
        SpUtils.saveInt(Temper5InchConst.Key.TEMPERATURE_UNIT, temperatureUnit);
        if (TextUtils.isEmpty(settingDataBean.calibrationContent.get())) {
            SpUtils.saveFloat(Temper5InchConst.Key.CALIBRATION_VALUE, Temper5InchConst.Default.calibrationValueDef);
        } else {
            SpUtils.saveFloat(Temper5InchConst.Key.CALIBRATION_VALUE, Float.valueOf(settingDataBean.calibrationContent.get()));
        }
        if (TextUtils.isEmpty(settingDataBean.warningContent.get())) {
            if (temperatureUnit == 1) {
                SpUtils.saveFloat(Temper5InchConst.Key.WARNING_VALUE, Temper5InchConst.Default.warningValueDef);
            } else {
                SpUtils.saveFloat(Temper5InchConst.Key.WARNING_VALUE, TemperatureUnitUtils.c2f(Temper5InchConst.Default.warningValueDef));
            }
        } else {
            SpUtils.saveFloat(Temper5InchConst.Key.WARNING_VALUE, Float.valueOf(settingDataBean.warningContent.get()));
        }
        if (TextUtils.isEmpty(settingDataBean.intervalContent.get())) {
            SpUtils.saveInt(Temper5InchConst.Key.CAROUSEL_INTERVAL, Temper5InchConst.Default.carouselIntervalDef);
        } else {
            SpUtils.saveInt(Temper5InchConst.Key.CAROUSEL_INTERVAL, Integer.parseInt(settingDataBean.intervalContent.get()));
        }
        int subtitleSpeed = 2;
        if (activity5inchSettingBinding.rbFast.isChecked()) {
            subtitleSpeed = 1;
        } else if (activity5inchSettingBinding.rbModerate.isChecked()) {
            subtitleSpeed = 2;
        } else if (activity5inchSettingBinding.rbSlow.isChecked()) {
            subtitleSpeed = 3;
        }
        SpUtils.saveInt(Temper5InchConst.Key.SUBTITLE_SPEED, subtitleSpeed);
        if (activity5inchSettingBinding.cbMirror.isChecked()) {
            SpUtils.saveBoolean(Constants.Key.IS_H_MIRROR, true);
        } else {
            SpUtils.saveBoolean(Constants.Key.IS_H_MIRROR, false);
        }
    }

    private void setServerInfo(int model) {
        String ip = Constants.NetConfig.PRO_URL;
        String resPort = Constants.NetConfig.PRO_RES_PORT;
        String xmppPort = Constants.NetConfig.PRO_XMPP_PORT;
        String proName = Constants.NetConfig.PRO_SUFFIX;
        if (model == Constants.serverModel.YUN) {
            settingDataBean.serverInfoEnabled.set(false);
        } else {
            settingDataBean.serverInfoEnabled.set(true);

            ip = SpUtils.getStr(Constants.Key.JU_IP_CACHE);
            resPort = SpUtils.getStr(Constants.Key.JU_RESOURCE_PORT_CACHE);
            xmppPort = SpUtils.getStr(Constants.Key.JU_XMPP_PORT_CACHE);
            proName = SpUtils.getStr(Constants.Key.JU_PROJECT_NAME_SUFFIX);
        }
        settingDataBean.serverIp.set(ip);
        settingDataBean.serverResPort.set(resPort);
        settingDataBean.serverXmppPort.set(xmppPort);
        settingDataBean.serverProName.set(proName);
    }

    public void setEditTextSelection() {
        if (activity5inchSettingBinding != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    activity5inchSettingBinding.etCalibrationContent.setSelection(settingDataBean.getCalibrationContent().get().length());
                    activity5inchSettingBinding.etIntervalContent.setSelection(settingDataBean.getIntervalContent().get().length());
                    activity5inchSettingBinding.etWaringContent.setSelection(settingDataBean.getWarningContent().get().length());
                }
            }, 200);
        }
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
                    String mIp = settingDataBean.serverIp.get();
                    String mResPort = settingDataBean.serverResPort.get();
                    String mXmppPort = settingDataBean.serverXmppPort.get();
                    String mProName = settingDataBean.serverProName.get();
                    if (TextUtils.isEmpty(mIp)) {
                        UIUtils.showTitleTip(Setting5InchActivity.this, getResString(R.string.setting_please_set_ip));
                        return;
                    }
                    if (TextUtils.isEmpty(mResPort)) {
                        UIUtils.showTitleTip(Setting5InchActivity.this, getResString(R.string.setting_please_set_res));
                        return;
                    }
                    int intResPort = Integer.parseInt(mResPort);
                    if(intResPort > 65535){
                        UIUtils.showTitleTip(Setting5InchActivity.this, getResString(R.string.setting_res_port_error));
                        return;
                    }
                    if (TextUtils.isEmpty(mXmppPort)) {
                        UIUtils.showTitleTip(Setting5InchActivity.this, getResString(R.string.setting_please_set_xmpp));
                        return;
                    }
                    int intXmppPort = Integer.parseInt(mXmppPort);
                    if(intXmppPort > 65535){
                        UIUtils.showTitleTip(Setting5InchActivity.this, getResString(R.string.setting_xmpp_port_error));
                        return;
                    }
                    if (TextUtils.isEmpty(mProName)) {
                    }
                    if (activity5inchSettingBinding.rbYun.isChecked()) {
                        SpUtils.saveInt(Constants.Key.SERVER_MODEL, Constants.serverModel.YUN);
                    } else if (activity5inchSettingBinding.rbJu.isChecked()) {
                        SpUtils.saveInt(Constants.Key.SERVER_MODEL, Constants.serverModel.JU);
                        SpUtils.saveStr(Constants.Key.JU_IP_CACHE, mIp);
                        SpUtils.saveStr(Constants.Key.JU_RESOURCE_PORT_CACHE, mResPort);
                        SpUtils.saveStr(Constants.Key.JU_XMPP_PORT_CACHE, mXmppPort);
                        SpUtils.saveStr(Constants.Key.JU_PROJECT_NAME_SUFFIX, mProName);
                    }
                    UIUtils.showTitleTip(Setting5InchActivity.this, getResString(R.string.setting_save_succ_please_restart));
                    break;
                case R.id.btn_update_system:
                    UpdateVersionControl.getInstance().checkUpdate(Setting5InchActivity.this);
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
                            settingDataBean.warningContent.set(String.valueOf(TemperatureUnitUtils.f2c(Float.valueOf(settingDataBean.warningContent.get()))));
                            break;
                        case R.id.rb_fahrenheit:
                            settingDataBean.warningContent.set(String.valueOf(TemperatureUnitUtils.c2f(Float.valueOf(settingDataBean.warningContent.get()))));
                            break;
                    }
                    break;
                case R.id.rg_angle:
                    switch (checkedId) {
                        case R.id.rb_0:
                            SpUtils.saveInt(Constants.Key.CAMERA_ANGLE, 0);
                            break;
                        case R.id.rb_90:
                            SpUtils.saveInt(Constants.Key.CAMERA_ANGLE, 90);
                            break;
                        case R.id.rb_180:
                            SpUtils.saveInt(Constants.Key.CAMERA_ANGLE, 180);
                            break;
                        case R.id.rb_270:
                            SpUtils.saveInt(Constants.Key.CAMERA_ANGLE, 270);
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
                        settingDataBean.calibrationContent.set(BigDecimalUtils.add(settingDataBean.calibrationContent.get(), "0.1", 1).toString());
                    } else {
                        settingDataBean.calibrationContent.set(BigDecimalUtils.sub(settingDataBean.calibrationContent.get(), "0.1", 1).toString());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            super.run();
        }
    }
}
