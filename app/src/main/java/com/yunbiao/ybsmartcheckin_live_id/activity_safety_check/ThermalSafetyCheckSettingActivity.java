package com.yunbiao.ybsmartcheckin_live_id.activity_safety_check;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.PowerOnOffActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple.MultiThermalConst;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

public class ThermalSafetyCheckSettingActivity extends BaseActivity {

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_thermal_safety_check_setting;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_thermal_safety_check_setting;
    }

    @Override
    protected void initView() {
        initLowTemp();

        initThermalMirror();

        initTemperThreshold();

        initCorrectValue();

        initBlackBodyPreValue();

        initBlackBodyEnabled();

        initTemperFrame();

        initAutoCalibration();
    }

    private void initAutoCalibration(){
        boolean aBoolean = SpUtils.getBoolean(ThermalSafetyCheckConst.Key.AUTO_CALIBRATION, ThermalSafetyCheckConst.Default.AUTO_CALIBRATION);
        Switch swAutoCalibration = findViewById(R.id.sw_auto_calibration_setting);
        swAutoCalibration.setChecked(aBoolean);
        swAutoCalibration.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(ThermalSafetyCheckConst.Key.AUTO_CALIBRATION, isChecked));
    }

    public void resetWarningNumber(View view){
        SpUtils.saveLong(ThermalSafetyCheckConst.Key.WARNING_NUMBER, 0l);
        UIUtils.showShort(this,APP.getContext().getResources().getString(R.string.setting_tip_warning_number_reseted));
    }

    private void initTemperFrame(){
        boolean isTemperEnable = SpUtils.getBoolean(ThermalSafetyCheckConst.Key.TEMPER_FRAME,ThermalSafetyCheckConst.Default.TEMPER_FRAME);
        Switch swTemperFrame = findViewById(R.id.sw_temper_frame_enable_setting);
        swTemperFrame.setChecked(isTemperEnable);
        swTemperFrame.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(ThermalSafetyCheckConst.Key.TEMPER_FRAME,isChecked);
            }
        });
    }

    private void initBlackBodyEnabled(){
        boolean blackBodyEnabled = SpUtils.getBoolean(ThermalSafetyCheckConst.Key.BLACK_BODY_ENABLED,ThermalSafetyCheckConst.Default.BLACK_BODY_ENABLED);
        findViewById(R.id.tv_black_body_correction_area).setVisibility(blackBodyEnabled ? View.VISIBLE : View.GONE);

        Switch swBlackBodyEnabled = findViewById(R.id.sw_black_body_enable_setting);
        swBlackBodyEnabled.setChecked(blackBodyEnabled);
        swBlackBodyEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(ThermalSafetyCheckConst.Key.BLACK_BODY_ENABLED,isChecked);
                findViewById(R.id.tv_black_body_correction_area).setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void initBlackBodyPreValue(){
        final View llPreValue = findViewById(R.id.ll_black_body_pre_value);
        View viewById = findViewById(R.id.setting_title);
        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llPreValue.setVisibility(llPreValue.isShown() ? View.GONE : View.VISIBLE);
            }
        });
        int preValue = SpUtils.getIntOrDef(MultiThermalConst.Key.BLACK_BODY_PRE_VALUE,MultiThermalConst.Default.BLACK_BODY_PRE_VALUE);
        final EditText edtPreValue = findViewById(R.id.edt_black_body_pre_value);
        edtPreValue.setText(preValue + "");

        Button btnSave = findViewById(R.id.btn_black_body_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = edtPreValue.getText().toString();
                int finalValue = MultiThermalConst.Default.BLACK_BODY_PRE_VALUE;
                if(!TextUtils.isEmpty(value)){
                    finalValue = Integer.parseInt(value);
                }
                SpUtils.saveInt(MultiThermalConst.Key.BLACK_BODY_PRE_VALUE,finalValue);
                UIUtils.showShort(ThermalSafetyCheckSettingActivity.this, APP.getContext().getResources().getString(R.string.setting_save_success));
            }
        });
    }

    private void initCorrectValue(){
        Button btnSub = findViewById(R.id.btn_body_correct_sub_setting);
        Button btnAdd = findViewById(R.id.btn_body_correct_add_setting);
        float correctValue = SpUtils.getFloat(ThermalSafetyCheckConst.Key.CORRECT_VALUE,ThermalSafetyCheckConst.Default.CORRECT_VALUE);
        final EditText edtCorrect = findViewById(R.id.edt_body_correct__setting);
        edtCorrect.setText(correctValue+"");
        View.OnClickListener onClickListener = v -> {
            float value = Float.parseFloat(edtCorrect.getText().toString());
            if(v.getId() == R.id.btn_body_correct_sub_setting){
                value -= 0.1f;
            } else {
                value += 0.1f;
            }
            value = formatF(value);
            edtCorrect.setText(value + "");
            SpUtils.saveFloat(ThermalSafetyCheckConst.Key.CORRECT_VALUE,value);
        };
        btnSub.setOnClickListener(onClickListener);
        btnAdd.setOnClickListener(onClickListener);
    }

    private void initLowTemp(){
        Switch swLowTemp = findViewById(R.id.sw_low_temp_model_setting);
        boolean isLowTemp = SpUtils.getBoolean(ThermalSafetyCheckConst.Key.LOW_TEMP,ThermalSafetyCheckConst.Default.LOW_TEMP);
        swLowTemp.setChecked(isLowTemp);
        swLowTemp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(ThermalSafetyCheckConst.Key.LOW_TEMP,isChecked);
            }
        });
    }

    private void initThermalMirror(){
        Switch swThermalMirror = findViewById(R.id.sw_thermal_imag_mirror_setting);
        boolean isThermalMirror = SpUtils.getBoolean(ThermalSafetyCheckConst.Key.THERMAL_MIRROR,ThermalSafetyCheckConst.Default.THERMAL_MIRROR);
        swThermalMirror.setChecked(isThermalMirror);
        swThermalMirror.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(ThermalSafetyCheckConst.Key.THERMAL_MIRROR,isChecked);
            }
        });
    }

    private void initTemperThreshold(){
        float normalTemper = SpUtils.getFloat(ThermalSafetyCheckConst.Key.NORMAL_TEMPER, ThermalSafetyCheckConst.Default.NORMAL_TEMPER);
        final EditText edtNormalTemp = findViewById(R.id.edt_temp_min_threshold_setting);
        edtNormalTemp.setText(normalTemper+"");
        Button btnNormalSub = findViewById(R.id.btn_temp_min_threshold_sub_setting);
        Button btnNormalAdd = findViewById(R.id.btn_temp_min_threshold_add_setting);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String normalTemp = edtNormalTemp.getText().toString();
                float tempF = Float.parseFloat(normalTemp);
                if (v.getId() == R.id.btn_temp_min_threshold_sub_setting) {
                    tempF -= 0.1f;
                } else {
                    tempF += 0.1f;
                }
                tempF = formatF(tempF);
                edtNormalTemp.setText(tempF + "");

                SpUtils.saveFloat(ThermalSafetyCheckConst.Key.NORMAL_TEMPER, tempF);
            }
        };
        btnNormalSub.setOnClickListener(onClickListener);
        btnNormalAdd.setOnClickListener(onClickListener);

        float warningTemper = SpUtils.getFloat(ThermalSafetyCheckConst.Key.WARNING_TEMPER,ThermalSafetyCheckConst.Default.WARNING_TEMPER);
        final EditText edtWarningTemp = findViewById(R.id.edt_temp_warning_threshold_setting);
        edtWarningTemp.setText(warningTemper + "");
        Button btnWarningAdd = findViewById(R.id.btn_temp_warning_threshold_add_setting);
        Button btnWarningSub = findViewById(R.id.btn_temp_warning_threshold_sub_setting);
        View.OnClickListener onClickListener1 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String warningTemp = edtWarningTemp.getText().toString();
                float tempF = Float.parseFloat(warningTemp);
                if (v.getId() == R.id.btn_temp_warning_threshold_sub_setting) {
                    tempF -= 0.1f;
                } else {
                    tempF += 0.1f;
                }
                tempF = formatF(tempF);
                edtWarningTemp.setText(tempF + "");

                SpUtils.saveFloat(ThermalSafetyCheckConst.Key.WARNING_TEMPER,tempF);
            }
        };
        btnWarningAdd.setOnClickListener(onClickListener1);
        btnWarningSub.setOnClickListener(onClickListener1);
    }

    public void setBlackBody(View view){
        startActivity(new Intent(this,BlackBodyAreaActivity.class));
    }

    public void powerOnOff(View view){
        startActivity(new Intent(this, PowerOnOffActivity.class));
    }
}
