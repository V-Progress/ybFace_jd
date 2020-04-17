package com.yunbiao.ybsmartcheckin_live_id.activity_safety_check_double_light;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.yunbiao.ybsmartcheckin_live_id.ButtonClickListener;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.PowerOnOffActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.common.UpdateVersionControl;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import butterknife.BindView;

public class SafetyDoubleLightSettingActivity extends BaseActivity {

    @BindView(R.id.sw_thermal_imag_mirror_setting)
    Switch swThermalMirror;
    @BindView(R.id.sw_low_temp_model_setting)
    Switch swLowTemp;
    @BindView(R.id.btn_body_correct_sub_setting)
    Button btnCorrSub;
    @BindView(R.id.edt_body_correct__setting)
    EditText edtCorr;
    @BindView(R.id.btn_body_correct_add_setting)
    Button btnCorrPlus;
    @BindView(R.id.btn_temp_min_threshold_sub_setting)
    Button btnMinThresholdSub;
    @BindView(R.id.edt_temp_min_threshold_setting)
    EditText edtMinThreshold;
    @BindView(R.id.btn_temp_min_threshold_add_setting)
    Button btnMinThresholdPlus;
    @BindView(R.id.btn_temp_warning_threshold_sub_setting)
    Button btnWarningThresholdSub;
    @BindView(R.id.edt_temp_warning_threshold_setting)
    EditText edtWarningThreshold;
    @BindView(R.id.btn_temp_warning_threshold_add_setting)
    Button btnWarningThresholdPlus;
    @BindView(R.id.sw_black_body_enable_setting)
    Switch swBlackBody;
    @BindView(R.id.sw_black_body_frame_setting)
    Switch swBlackFrame;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_safety_double_light_setting;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_safety_double_light_setting;
    }

    @Override
    protected void initView() {
        initLowTemp();

        initThermalMirror();

        initTempeCorrect();

        initThreshold();

        initBlackEnable();

        initBlackFrameEnable();
    }

    public void powerOnOff(View view) {
        startActivity(new Intent(this, PowerOnOffActivity.class));
    }

    public void upgrade(View view) {
        UpdateVersionControl.getInstance().checkUpdate(this);
    }

    public void setBlackBody(View view){ startActivity(new Intent(this,SafetyCheckDoubleLightBlackBodyAreaActivity.class));}

    private void initLowTemp() {
        boolean lowTemp = SpUtils.getBoolean(SafetyCheckDoubleLightConst.Key.LOW_TEMP, SafetyCheckDoubleLightConst.Default.LOW_TEMP);
        swLowTemp.setChecked(lowTemp);
        swLowTemp.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SpUtils.saveBoolean(SafetyCheckDoubleLightConst.Key.LOW_TEMP, isChecked);
        });
    }

    private void initThermalMirror() {
        boolean thermalMirror = SpUtils.getBoolean(SafetyCheckDoubleLightConst.Key.THERMAL_MIRROR, SafetyCheckDoubleLightConst.Default.THERMAL_MIRROR);
        swThermalMirror.setChecked(thermalMirror);
        swThermalMirror.setOnCheckedChangeListener((buttonView, isCheck) -> {
            SpUtils.saveBoolean(SafetyCheckDoubleLightConst.Key.THERMAL_MIRROR, isCheck);
        });
    }

    private void initTempeCorrect() {
        Float correctValue = SpUtils.getFloat(SafetyCheckDoubleLightConst.Key.CORRECT_VALUE, SafetyCheckDoubleLightConst.Default.CORRECT_VALUE);
        edtCorr.setText(correctValue + "");
        ButtonClickListener buttonClickListener = new ButtonClickListener() {
            @Override
            public void onShortClick(int viewId) {
                String s = edtCorr.getText().toString();
                float value = Float.parseFloat(s);
                if (viewId == R.id.btn_body_correct_add_setting) {
                    value += 0.1f;
                } else {
                    value -= 0.1f;
                }
                value = formatF(value);
                edtCorr.setText(value + "");
                SpUtils.saveFloat(SafetyCheckDoubleLightConst.Key.CORRECT_VALUE, value);
            }
        };
        btnCorrPlus.setOnTouchListener(buttonClickListener);
        btnCorrSub.setOnTouchListener(buttonClickListener);
    }

    private void initThreshold() {
        Float minThreshold = SpUtils.getFloat(SafetyCheckDoubleLightConst.Key.MIN_THRESHOLD, SafetyCheckDoubleLightConst.Default.MIN_THRESHOLD);
        edtMinThreshold.setText(minThreshold + "");
        ButtonClickListener buttonClickListener = new ButtonClickListener() {
            @Override
            public void onShortClick(int viewId) {
                String s = edtMinThreshold.getText().toString();
                float value = Float.parseFloat(s);
                if (viewId == R.id.btn_temp_min_threshold_sub_setting) {
                    value -= 0.1f;
                } else {
                    value += 0.1f;
                }
                value = formatF(value);
                edtMinThreshold.setText(value + "");
                SpUtils.saveFloat(SafetyCheckDoubleLightConst.Key.MIN_THRESHOLD, value);
            }
        };
        btnMinThresholdSub.setOnTouchListener(buttonClickListener);
        btnMinThresholdPlus.setOnTouchListener(buttonClickListener);

        Float warningThreshold = SpUtils.getFloat(SafetyCheckDoubleLightConst.Key.WARNING_THRESHOLD, SafetyCheckDoubleLightConst.Default.WARNING_THRESHOLD);
        edtWarningThreshold.setText(warningThreshold + "");
        ButtonClickListener buttonClickListener1 = new ButtonClickListener() {
            @Override
            public void onShortClick(int viewId) {
                String s = edtWarningThreshold.getText().toString();
                float value = Float.parseFloat(s);
                if (viewId == R.id.btn_temp_warning_threshold_sub_setting) {
                    value -= 0.1f;
                } else {
                    value += 0.1f;
                }
                value = formatF(value);
                edtWarningThreshold.setText(value + "");
                SpUtils.saveFloat(SafetyCheckDoubleLightConst.Key.WARNING_THRESHOLD, value);
            }
        };
        btnWarningThresholdSub.setOnTouchListener(buttonClickListener1);
        btnWarningThresholdPlus.setOnTouchListener(buttonClickListener1);
    }

    private void initBlackEnable(){
        boolean blackEnable = SpUtils.getBoolean(SafetyCheckDoubleLightConst.Key.BLACK_BODY_ENABLED, SafetyCheckDoubleLightConst.Default.BLACK_BODY_ENABLED);
        swBlackBody.setChecked(blackEnable);
        swBlackBody.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(SafetyCheckDoubleLightConst.Key.BLACK_BODY_ENABLED,isChecked));
    }

    private void initBlackFrameEnable(){
        boolean blackFrame = SpUtils.getBoolean(SafetyCheckDoubleLightConst.Key.BLACK_BODY_FRAME, SafetyCheckDoubleLightConst.Default.BLACK_BODY_ENABLED);
        swBlackFrame.setChecked(blackFrame);
        swBlackFrame.setOnCheckedChangeListener((buttonView,isChecked) -> SpUtils.saveBoolean(SafetyCheckDoubleLightConst.Key.BLACK_BODY_FRAME,isChecked));
    }
}
