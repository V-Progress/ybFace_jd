package com.yunbiao.ybsmartcheckin_live_id.activity_safety_check_double_light;

import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.yunbiao.ybsmartcheckin_live_id.ButtonClickListener;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.PowerOnOffActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.common.UpdateVersionControl;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import butterknife.BindView;

public class SafetyDoubleLightSettingActivity extends BaseActivity {

    @BindView(R.id.tv_version_name_scdl_system)
    TextView tvVersionName;
    @BindView(R.id.tv_version_info_scdl_system)
    TextView tvVersionInfo;
    @BindView(R.id.fl_version_scdl_system)
    View flVersionLoading;

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

        initSetIp();
    }

    @Override
    protected void initData() {
        super.initData();

        checkUpgrade(new CheckUpgradeCallback() {
            @Override
            public void onStart() {
                flVersionLoading.setVisibility(View.VISIBLE);
            }

            @Override
            public void noUpgrade(String currVersionName) {
                tvVersionName.setText(getResString(R.string.update_lable_current) + currVersionName);
                tvVersionInfo.setGravity(Gravity.CENTER);
                tvVersionInfo.setText(getResString(R.string.updateManager_dqbbwzxbb));
                tvVersionInfo.setTextColor(Color.GREEN);
            }

            @Override
            public void haveNewVersion(String versionName, String versionInfo) {
                tvVersionName.setText(getResString(R.string.update_lable_new) + versionName);
                tvVersionInfo.setGravity(Gravity.LEFT);
                tvVersionInfo.setText(TextUtils.isEmpty(versionInfo) ? getResString(R.string.update_no_description) : versionInfo);
                tvVersionInfo.setTextColor(Color.WHITE);
            }

            @Override
            public void onError(String currVersionName, String s) {
                tvVersionName.setText(getResString(R.string.update_lable_current) + currVersionName);
                tvVersionInfo.setGravity(Gravity.CENTER);
                tvVersionInfo.setText(getResString(R.string.update_check_failed));
                tvVersionInfo.setTextColor(Color.GRAY);
            }

            @Override
            public void onFinish() {
                flVersionLoading.setVisibility(View.GONE);
            }
        });
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


    private EditText edtIp;
    private EditText edtResPort;
    private EditText edtXmppPort;
    private EditText edtProName;

    //初始化IP设置
    private void initSetIp() {
        edtIp = findViewById(R.id.edt_ip);
        edtResPort = findViewById(R.id.edt_res_port);
        edtXmppPort = findViewById(R.id.edt_xmpp_port);
        edtProName = findViewById(R.id.edt_pro_name);

        RadioGroup rgServerModel = findViewById(R.id.rg_server_model);
        final RadioButton rbYun = findViewById(R.id.rb_yun);
        final RadioButton rbJu = findViewById(R.id.rb_ju);

        Button btnSave = findViewById(R.id.btn_save_address);

        if (SpUtils.getIntOrDef(SpUtils.SERVER_MODEL, Constants.serverModel.YUN) == Constants.serverModel.YUN) {
            rbYun.setChecked(true);
            setServerInfo(Constants.serverModel.YUN);
        } else {
            rbJu.setChecked(true);
            setServerInfo(Constants.serverModel.JU);
        }

        rgServerModel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (rbYun.isChecked()) {
                    setServerInfo(Constants.serverModel.YUN);
                }
                if (rbJu.isChecked()) {
                    setServerInfo(Constants.serverModel.JU);
                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mIp = edtIp.getText().toString();
                String mResPort = edtResPort.getText().toString();
                String mXmppPort = edtXmppPort.getText().toString();
                String mProName = edtProName.getText().toString();
                if (TextUtils.isEmpty(mIp)) {
                    UIUtils.showTitleTip(SafetyDoubleLightSettingActivity.this, "请设置IP地址");
                    return;
                }


                if (TextUtils.isEmpty(mResPort)) {
                    UIUtils.showTitleTip(SafetyDoubleLightSettingActivity.this, "请设置接口端口");
                    return;
                }
                int intResPort = Integer.parseInt(mResPort);
                if(intResPort > 65535){
                    UIUtils.showTitleTip(SafetyDoubleLightSettingActivity.this, "服务端口格式不正确，请检查");
                    return;
                }

                if (TextUtils.isEmpty(mXmppPort)) {
                    UIUtils.showTitleTip(SafetyDoubleLightSettingActivity.this, "请设置XMPP端口");
                    return;
                }
                int intXmppPort = Integer.parseInt(mXmppPort);
                if(intXmppPort > 65535){
                    UIUtils.showTitleTip(SafetyDoubleLightSettingActivity.this, "通信端口格式不正确，请检查");
                    return;
                }

                if (TextUtils.isEmpty(mProName)) {
                }

                if (rbYun.isChecked()) {
                    SpUtils.saveInt(SpUtils.SERVER_MODEL, Constants.serverModel.YUN);
                } else if (rbJu.isChecked()) {
                    SpUtils.saveInt(SpUtils.SERVER_MODEL, Constants.serverModel.JU);
                    SpUtils.saveStr(SpUtils.JU_IP_CACHE, mIp);
                    SpUtils.saveStr(SpUtils.JU_RESOURCE_PORT_CACHE, mResPort);
                    SpUtils.saveStr(SpUtils.JU_XMPP_PORT_CACHE, mXmppPort);
                    SpUtils.saveStr(SpUtils.JU_PROJECT_NAME_SUFFIX, mProName);
                }
                UIUtils.showTitleTip(SafetyDoubleLightSettingActivity.this, "保存成功,重启APP后生效");
            }
        });
    }

    private void setServerInfo(int model) {
        String ip = Constants.NetConfig.PRO_URL;
        String resPort = Constants.NetConfig.PRO_RES_PORT;
        String xmppPort = Constants.NetConfig.PRO_XMPP_PORT;
        String proName = Constants.NetConfig.PRO_SUFFIX;
        if (model == Constants.serverModel.YUN) {
            edtIp.setText(ip);
            edtResPort.setText(resPort);
            edtXmppPort.setText(xmppPort);
            edtProName.setText(proName);
            edtIp.setEnabled(false);
            edtResPort.setEnabled(false);
            edtXmppPort.setEnabled(false);
            edtProName.setEnabled(false);
        } else {
            ip = SpUtils.getStr(SpUtils.JU_IP_CACHE);
            resPort = SpUtils.getStr(SpUtils.JU_RESOURCE_PORT_CACHE);
            xmppPort = SpUtils.getStr(SpUtils.JU_XMPP_PORT_CACHE);
            proName = SpUtils.getStr(SpUtils.JU_PROJECT_NAME_SUFFIX);
            edtIp.setEnabled(true);
            edtResPort.setEnabled(true);
            edtXmppPort.setEnabled(true);
            edtProName.setEnabled(true);

            edtIp.setText(ip);
            edtResPort.setText(resPort);
            edtXmppPort.setText(xmppPort);
            edtProName.setText(proName);
        }
    }

}
