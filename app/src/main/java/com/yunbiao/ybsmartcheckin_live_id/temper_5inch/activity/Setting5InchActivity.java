package com.yunbiao.ybsmartcheckin_live_id.temper_5inch.activity;

import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioGroup;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.databinding.Activity5inchSettingBinding;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.Temper5InchConst;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.databean.SettingDataBean;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.utils.BigDecimalUtils;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.utils.TemperatureUnitUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.LogUtil;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

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

        if (SpUtils.getBoolean(Constants.Key.IS_H_MIRROR, Constants.Default.IS_H_MIRROR)) {
            activity5inchSettingBinding.cbMirror.setChecked(true);
        } else {
            activity5inchSettingBinding.cbMirror.setChecked(false);
        }

        setEditTextSelection();

        activity5inchSettingBinding.setEventListener(new EventListener());
    }

    @Override
    protected void initData() {

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
