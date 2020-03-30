package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.DisplayOrientationEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.common.UpdateVersionControl;
import com.yunbiao.ybsmartcheckin_live_id.faceview.camera.CameraSettings;
import com.yunbiao.ybsmartcheckin_live_id.faceview.camera.ExtCameraManager;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.activity.PowerOnOffActivity;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Request;

public class ThermalSettingActivity extends BaseActivity {
    private static final String TAG = "SettingActivity";


    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_thermal_setting;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_thermal_setting;
    }

    @Override
    protected void initView() {
        switch (Constants.DEVICE_TYPE) {
            case Constants.DeviceType.HT_TEMPERATURE_CHECK_IN:
            case Constants.DeviceType.HT_TEMPERATURE_CHECK_IN_SMT:
            case Constants.DeviceType.HT_TEMPERATURE_CERTIFICATES:
            case Constants.DeviceType.HT_MULTIPLE_THERMAL:
                findViewById(R.id.ll_qr_code_enable_area).setVisibility(View.GONE);
                break;
        }
    }

    private void initUISetting() {
        String welcomeTips = SpUtils.getStr(SpUtils.WELCOM_TIPS, Constants.isHT
                ? APP.getContext().getResources().getString(R.string.setting_default_welcome_tip2)
                : APP.getContext().getResources().getString(R.string.setting_default_welcome_tip));
        EditText edtWelComeTips = findViewById(R.id.edt_welcome_tips);
        edtWelComeTips.setText(welcomeTips);
        edtWelComeTips.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String inputWelcome = s.toString();
                SpUtils.saveStr(SpUtils.WELCOM_TIPS, inputWelcome);
            }
        });

        boolean qrCodeEnabled = SpUtils.getBoolean(SpUtils.QRCODE_ENABLED, Constants.DEFAULT_QRCODE_ENABLED);
        Switch swQrCode = findViewById(R.id.sw_qrcode_setting);
        swQrCode.setChecked(qrCodeEnabled);
        swQrCode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(SpUtils.QRCODE_ENABLED, isChecked);
            }
        });
    }

    @Override
    protected void initData() {
        initUISetting();
        //当前模式
        initModelSetting();
        //设置IP
        initSetIp();
        //CPU温度
        startUpdateCpuTemperature();
        //继电器设置
        initRelayDelay();
        //网络信息
        initNetInfo();
        //人脸框镜像
        initFaceRectMirrorSetting();
        //摄像头尺寸
        initCameraSizeSetting();
        //相似度阈值
        initSimilarSetting();
        //测温模块设置
        initTemperatureSetting();
        //人脸弹窗设置
        initFaceVipDialogSetting();
        //摄像头设置
        initCameraSetting();
        //活体开关
        initLivenessSetting();
        //大屏海报开关
        initPosterSetting();
        //读卡器模块
        initReadCardSetting();
    }

    public void powerOnOff(View view) {
        startActivity(new Intent(this, PowerOnOffActivity.class));
    }

    private void initReadCardSetting() {
        boolean readCardEnabled = SpUtils.getBoolean(SpUtils.READ_CARD_ENABLED, Constants.DEFAULT_READ_CARD_ENABLED);
        Switch swReadCard = findViewById(R.id.sw_readcard_setting);
        swReadCard.setChecked(readCardEnabled);
        swReadCard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(SpUtils.READ_CARD_ENABLED, isChecked);
            }
        });
    }

    private void initPosterSetting() {
        boolean isEnabled = SpUtils.getBoolean(SpUtils.POSTER_ENABLED, Constants.DEFAULT_POSTER_ENABLED);
        Switch swPoster = findViewById(R.id.sw_poster_setting);
        swPoster.setChecked(isEnabled);
        swPoster.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(SpUtils.POSTER_ENABLED, isChecked);
            }
        });
    }

    public static String[] models = {};

    @Override
    protected void onResume() {
        super.onResume();
        setCorrectUI();
    }

    private void setCorrectUI() {
        //进入矫正
        float thermalCorrect = SpUtils.getFloat(ThermalConst.Key.THERMAL_CORRECT, ThermalConst.Default.THERMAL_CORRECT);
        Button btnCorrectSub = findViewById(R.id.btn_correct_sub_setting);
        Button btnCorrectAdd = findViewById(R.id.btn_correct_add_setting);
        final EditText edtCorrect = findViewById(R.id.edt_correct_setting);
        edtCorrect.setText(thermalCorrect + "");

        View.OnClickListener correctOnclickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = edtCorrect.getText().toString();
                float corrValue = Float.parseFloat(s);
                if (v.getId() == R.id.btn_correct_sub_setting) {
                    corrValue -= 0.1f;
                } else {
                    corrValue += 0.1f;
                }
                corrValue = formatF(corrValue);
                SpUtils.saveFloat(ThermalConst.Key.THERMAL_CORRECT, corrValue);
                edtCorrect.setText(corrValue + "");
            }
        };
        btnCorrectSub.setOnClickListener(correctOnclickListener);
        btnCorrectAdd.setOnClickListener(correctOnclickListener);
        findViewById(R.id.btn_thermal_corr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ThermalSettingActivity.this, TemperatureCorrectActivity.class));
            }
        });
    }

    private void initModelSetting() {
        setCorrectUI();
        //人相框========================
        boolean aBoolean1 = SpUtils.getBoolean(ThermalConst.Key.PERSON_FRAME, ThermalConst.Default.PERSON_FRAME);
        Switch swPersonFrame = findViewById(R.id.sw_person_frame_setting);
        swPersonFrame.setChecked(aBoolean1);
        swPersonFrame.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(ThermalConst.Key.PERSON_FRAME, isChecked);
            }
        });


        //模式==================================================================================
        final TextView tvModelSetting = findViewById(R.id.tv_model_setting);
        final String[] items = ThermalConst.models;

        final int model = SpUtils.getIntOrDef(SpUtils.THERMAL_MODEL_SETTING, ThermalConst.DEFAULT_THERMAL_MODEL);

        //如果是红外模式或人脸模式则隐藏矫正按钮
        if (model == ThermalConst.FACE_INFRARED || model == ThermalConst.INFRARED_ONLY || model == ThermalConst.FACE_ONLY || model == ThermalConst.THERMAL_16_4_ONLY || model == ThermalConst.FACE_THERMAL_16_4) {
            findViewById(R.id.btn_thermal_corr).setVisibility(View.GONE);
        } else {
            findViewById(R.id.btn_thermal_corr).setVisibility(View.VISIBLE);
        }

        tvModelSetting.setText(items[model]);
        tvModelSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int currModel = SpUtils.getIntOrDef(SpUtils.THERMAL_MODEL_SETTING, ThermalConst.DEFAULT_THERMAL_MODEL);
                AlertDialog.Builder builder = new AlertDialog.Builder(ThermalSettingActivity.this);
                builder.setTitle(getResources().getString(R.string.setting_select_model));
                builder.setSingleChoiceItems(items, currModel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int whichModel) {
                        //如果模式相同则直接隐藏
                        if (whichModel == currModel) {
                            dialog.dismiss();
                            return;
                        }
                        //如果是红外模式则隐藏矫正按钮
                        if (whichModel == ThermalConst.FACE_INFRARED || whichModel == ThermalConst.INFRARED_ONLY || whichModel == ThermalConst.FACE_ONLY) {
                            findViewById(R.id.btn_thermal_corr).setVisibility(View.GONE);
                        } else {
                            findViewById(R.id.btn_thermal_corr).setVisibility(View.VISIBLE);
                        }

                        SpUtils.saveInt(SpUtils.THERMAL_MODEL_SETTING, whichModel);
                        tvModelSetting.setText(items[whichModel]);

                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        //距离提示==========================================================================================
        boolean distanceEnabled = SpUtils.getBoolean(ThermalConst.Key.DISTANCE_TIP, ThermalConst.Default.DISTANCE_TIP);
        Switch swDistance = findViewById(R.id.sw_distance_setting);
        swDistance.setChecked(distanceEnabled);
        swDistance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(ThermalConst.Key.DISTANCE_TIP, isChecked);
            }
        });

        //热成像镜像==========================================================================================
        boolean thermalImgMirror = SpUtils.getBoolean(ThermalConst.Key.THERMAL_IMAGE_MIRROR, ThermalConst.Default.THERMAL_IMAGE_MIRROR);
        Switch swThermalMirror = findViewById(R.id.sw_thermal_imag_mirror_setting);
        swThermalMirror.setChecked(thermalImgMirror);
        swThermalMirror.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(ThermalConst.Key.THERMAL_IMAGE_MIRROR, isChecked);
            }
        });

        //修改测温阈值==========================================================================================
        Button btnMinSub = findViewById(R.id.btn_temp_min_threshold_sub_setting);
        Button btnMinAdd = findViewById(R.id.btn_temp_min_threshold_add_setting);
        final EditText edtMinThreshold = findViewById(R.id.edt_temp_min_threshold_setting);
        //温度最低阈值
        final float minValue = SpUtils.getFloat(ThermalConst.Key.TEMP_MIN_THRESHOLD, ThermalConst.Default.TEMP_MIN_THRESHOLD);
        edtMinThreshold.setText(minValue + "");
        View.OnClickListener minClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = edtMinThreshold.getText().toString();
                float v1 = formatF(Float.parseFloat(value));
                switch (v.getId()) {
                    case R.id.btn_temp_min_threshold_sub_setting:
                        v1 -= 0.1;
                        break;
                    case R.id.btn_temp_min_threshold_add_setting:
                        v1 += 0.1;
                        break;
                }
                v1 = formatF(v1);
                edtMinThreshold.setText(v1 + "");
                SpUtils.saveFloat(ThermalConst.Key.TEMP_MIN_THRESHOLD, v1);
            }
        };
        btnMinSub.setOnClickListener(minClickListener);
        btnMinAdd.setOnClickListener(minClickListener);

        //修改测温报警值==========================================================================================
        Button btnWarnSub = findViewById(R.id.btn_temp_warning_threshold_sub_setting);
        Button btnWarnAdd = findViewById(R.id.btn_temp_warning_threshold_add_setting);
        final EditText edtWarnThreshold = findViewById(R.id.edt_temp_warning_threshold_setting);
        final float warningValue = SpUtils.getFloat(ThermalConst.Key.TEMP_WARNING_THRESHOLD, ThermalConst.Default.TEMP_WARNING_THRESHOLD);
        edtWarnThreshold.setText(warningValue + "");
        View.OnClickListener warnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = edtWarnThreshold.getText().toString();
                float v1 = formatF(Float.parseFloat(value));
                if (v.getId() == R.id.btn_temp_warning_threshold_sub_setting) {
                    v1 -= 0.1;
                } else {
                    v1 += 0.1;
                }
                v1 = formatF(v1);
                edtWarnThreshold.setText(v1 + "");

                SpUtils.saveFloat(ThermalConst.Key.TEMP_WARNING_THRESHOLD, v1);
            }
        };
        btnWarnSub.setOnClickListener(warnClickListener);
        btnWarnAdd.setOnClickListener(warnClickListener);

        //体温播报设置==========================================================================================
        String normalTips = SpUtils.getStr(ThermalConst.Key.NORMAL_BROADCAST, ThermalConst.Default.NORMAL_BROADCAST);
        EditText edtNormalTips = findViewById(R.id.edt_normal_tips_tips);
        edtNormalTips.setText(normalTips);
        edtNormalTips.addTextChangedListener(new TextWatcherImpl() {
            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                SpUtils.saveStr(ThermalConst.Key.NORMAL_BROADCAST, TextUtils.isEmpty(input) ? "" : input);
            }
        });
        String warningTips = SpUtils.getStr(ThermalConst.Key.WARNING_BROADCAST, ThermalConst.Default.WARNING_BROADCAST);
        EditText edtWarningTips = findViewById(R.id.edt_warning_tips_tips);
        edtWarningTips.setText(warningTips);
        edtWarningTips.addTextChangedListener(new TextWatcherImpl() {
            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                SpUtils.saveStr(ThermalConst.Key.WARNING_BROADCAST, TextUtils.isEmpty(input) ? "" : input);
            }
        });

        //体温播报延时==========================================================================================
        Button btnSpeechDelaySub = findViewById(R.id.btn_speech_delay_sub_setting);
        Button btnSpeechDelayAdd = findViewById(R.id.btn_speech_delay_add_setting);
        final EditText edtSpeechDelay = findViewById(R.id.edt_speech_delay_setting);
        long speechDelayTime = SpUtils.getLong(ThermalConst.Key.SPEECH_DELAY, ThermalConst.Default.SPEECH_DELAY);
        edtSpeechDelay.setText(speechDelayTime + "");
        View.OnClickListener speechDelayOnClickLitsener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = edtSpeechDelay.getText().toString();
                long l = Long.parseLong(s);
                if (v.getId() == R.id.btn_speech_delay_add_setting) {
                    l += 100;
                } else {
                    l -= 100;
                    if (l < 1500) {
                        l = 1500;
                    }
                }

                edtSpeechDelay.setText(l + "");
                SpUtils.saveLong(ThermalConst.Key.SPEECH_DELAY, l);
            }
        };
        btnSpeechDelayAdd.setOnClickListener(speechDelayOnClickLitsener);
        btnSpeechDelaySub.setOnClickListener(speechDelayOnClickLitsener);

        //===低温模式=========================================================
        Switch swLowTempModel = findViewById(R.id.sw_low_temp_model_setting);
        boolean aBoolean = SpUtils.getBoolean(ThermalConst.Key.LOW_TEMP_MODE, ThermalConst.Default.LOW_TEMP);
        swLowTempModel.setChecked(aBoolean);
        swLowTempModel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(ThermalConst.Key.LOW_TEMP_MODE, isChecked);
            }
        });

        //====华氏度开关==============================================================
        boolean fEnabled = SpUtils.getBoolean(ThermalConst.Key.THERMAL_F_ENABLED, ThermalConst.Default.THERMAL_F_ENABLED);
        Switch swFEnabled = findViewById(R.id.sw_f_enabled_setting);
        swFEnabled.setChecked(fEnabled);
        swFEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(ThermalConst.Key.THERMAL_F_ENABLED, isChecked);
            }
        });

/*        //====环境温度================================================================
        Button btnAmbientSub = findViewById(R.id.btn_ambient_sub_setting);
        Button btnAmbientAdd = findViewById(R.id.btn_ambient_add_setting);
        final EditText edtAmbient = findViewById(R.id.edt_ambient_setting);
        float ambient = SpUtils.getFloat(SpUtils.AMBIENT, Constants.DEFAULT_AMBIENT);
        edtAmbient.setText(ambient + "");
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = edtAmbient.getText().toString();
                float v1 = Float.parseFloat(s);

                if (v.getId() == R.id.btn_ambient_sub_setting) {
                    v1 -= 0.1f;
                } else if (v.getId() == R.id.btn_ambient_add_setting) {

                    v1 += 0.1f;
                    if (v1 >= 35.8f) {
                        v1 = 35.8f;
                    }
                }
                v1 = formatF(v1);
                edtAmbient.setText(v1 + "");
                SpUtils.saveFloat(SpUtils.AMBIENT, v1);
            }
        };
        btnAmbientSub.setOnClickListener(onClickListener);
        btnAmbientAdd.setOnClickListener(onClickListener);

        View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {


                return false;
            }
        };
        btnAmbientSub.setOnLongClickListener(onLongClickListener);*/
    }

    class TextWatcherImpl implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private void initLivenessSetting() {
        Switch swLiveness = findViewById(R.id.sw_liveness_setting);
        boolean liveness = SpUtils.getBoolean(SpUtils.LIVENESS_ENABLED, false);
        swLiveness.setChecked(liveness);
        swLiveness.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(SpUtils.LIVENESS_ENABLED, isChecked);
            }
        });
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
                    UIUtils.showTitleTip(ThermalSettingActivity.this, "请设置IP地址");
                    return;
                }


                if (TextUtils.isEmpty(mResPort)) {
                    UIUtils.showTitleTip(ThermalSettingActivity.this, "请设置接口端口");
                    return;
                }
                int intResPort = Integer.parseInt(mResPort);
                if(intResPort > 65535){
                    UIUtils.showTitleTip(ThermalSettingActivity.this, "服务端口格式不正确，请检查");
                    return;
                }

                if (TextUtils.isEmpty(mXmppPort)) {
                    UIUtils.showTitleTip(ThermalSettingActivity.this, "请设置XMPP端口");
                    return;
                }
                int intXmppPort = Integer.parseInt(mXmppPort);
                if(intXmppPort > 65535){
                    UIUtils.showTitleTip(ThermalSettingActivity.this, "通信端口格式不正确，请检查");
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
                UIUtils.showTitleTip(ThermalSettingActivity.this, "保存成功,重启APP后生效");
            }
        });
    }

    private void setServerInfo(int model) {
        String ip;
        String resPort;
        String xmppPort;
        String proName;

        if (model == Constants.serverModel.YUN) {
            ip = SpUtils.getStr(SpUtils.IP_CACHE);
            resPort = SpUtils.getStr(SpUtils.RESOURCE_PORT_CACHE);
            xmppPort = SpUtils.getStr(SpUtils.XMPP_PORT_CACHE);
            proName = SpUtils.getStr(SpUtils.PROJECT_NAME_SUFFIX);

            if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(resPort) || TextUtils.isEmpty(xmppPort) || TextUtils.isEmpty(proName)) {
                edtIp.setText(Constants.NetConfig.PRO_URL);
                edtResPort.setText(Constants.NetConfig.PRO_RES_PORT);
                edtXmppPort.setText(Constants.NetConfig.PRO_XMPP_PORT);
                edtProName.setText(Constants.NetConfig.PRO_SUFFIX);
            } else {
                edtIp.setText(ip);
                edtResPort.setText(resPort);
                edtXmppPort.setText(xmppPort);
                edtProName.setText(proName);
            }

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

    //初始化人脸弹窗开关
    private void initFaceVipDialogSetting() {
        Switch switchFaceDialog = findViewById(R.id.sw_face_dialog);
        boolean faceDialog = SpUtils.getBoolean(SpUtils.FACE_DIALOG, false);
        switchFaceDialog.setChecked(faceDialog);
        switchFaceDialog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(SpUtils.FACE_DIALOG, isChecked);
            }
        });

    }

    //初始化摄像头设置
    private void initCameraSetting() {
//        TextView tvCamera = findViewById(R.id.tv_camera);
//        //摄像头模式
//        tvCamera.setText("【" + (Config.getCameraType() == Config.CAMERA_AUTO ? getString(R.string.act_set_tip_auto) : Config.getCameraType() == Config.CAMERA_BACK ? getString(R.string.act_set_tip_back) : getString(R.string.act_set_tip_front)) + getString(R.string.act_set_tip_fbl) + CameraSettings.getCameraPreviewWidth() + "*" + CameraSettings.getCameraPreviewHeight() + "】");
        //摄像头角度
        Button btnAngle = findViewById(R.id.btn_setAngle);
        int angle = SpUtils.getIntOrDef(SpUtils.CAMERA_ANGLE, Constants.DEFAULT_CAMERA_ANGLE);
        btnAngle.setText(getString(R.string.setting_cam_angle) + ":" + angle);
    }

    //初始化人脸框镜像设置
    private void initFaceRectMirrorSetting() {
        CheckBox cbMirror = findViewById(R.id.cb_mirror);
        //人脸框镜像
        final boolean mirror = SpUtils.isMirror();
        cbMirror.setChecked(mirror);
        cbMirror.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.setMirror(isChecked);
            }
        });

    }

    //开始自动更新CPU温度
    private void startUpdateCpuTemperature() {
        final TextView tvCpuTemper = findViewById(R.id.tv_cpu_temper);
        //获取CPU温度
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                final String s = CpuUtils.getCpuTemperatureFinder() + "℃";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvCpuTemper.setText(s);
                    }
                });
            }
        }, 0, 3, TimeUnit.SECONDS);

    }

    //初始化继电器设置
    private void initRelayDelay() {
        final EditText edtDelay = findViewById(R.id.edt_delay);
        int cacheDelay = SpUtils.getIntOrDef(SpUtils.GPIO_DELAY, 5);
        edtDelay.setText(cacheDelay + "");
        edtDelay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String s1 = edtDelay.getText().toString();
                if (TextUtils.isEmpty(s1)) {
                    return;
                }
                int delay = Integer.parseInt(s1);
                SpUtils.saveInt(SpUtils.GPIO_DELAY, delay);
                UIUtils.showShort(ThermalSettingActivity.this, getString(R.string.setting_edit_password_success));
            }
        });
    }

    //初始化温度检测模块的设置
    private void initTemperatureSetting() {
        //提示时间
        final EditText edtTipsTime = findViewById(R.id.edt_temp_tip_time_setting);
        Button btnSaveTime = findViewById(R.id.btn_save_temp_tip_time_setting);
        final int time = SpUtils.getIntOrDef(SpUtils.TEMP_TIPS_TIME, 7000);
        edtTipsTime.setText(time + "");
        btnSaveTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int saveTime = time;
                String s = edtTipsTime.getText().toString();
                if (!TextUtils.isEmpty(s)) {
                    saveTime = Integer.parseInt(s);
                }
                edtTipsTime.setText(saveTime + "");
                SpUtils.saveInt(SpUtils.TEMP_TIPS_TIME, saveTime);
                UIUtils.showTitleTip(ThermalSettingActivity.this, "保存成功");
            }
        });

        Button btnSaveDValue = findViewById(R.id.btn_save_temp_d_value_setting);
        final EditText edtTempDValue = findViewById(R.id.edt_temp_d_value_setting);
        final Float dValue = SpUtils.getFloat(SpUtils.TEMP_D_VALUE, Constants.DEFAULT_TEMP_D_VALUE_VALUE);
        edtTempDValue.setText(dValue + "");
        btnSaveDValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = edtTempDValue.getText().toString();
                if (TextUtils.isEmpty(s)) {
                    edtTempDValue.setText(dValue + "");
                }
                s = edtTempDValue.getText().toString();
                SpUtils.saveFloat(SpUtils.TEMP_D_VALUE, Float.parseFloat(s));
                UIUtils.showTitleTip(ThermalSettingActivity.this, "保存成功");
            }
        });
    }

    //初始化相似度阈值设置
    private void initSimilarSetting() {
        final EditText edtSimilar = findViewById(R.id.edt_similar_threshold);
        int similar = SpUtils.getIntOrDef(SpUtils.SIMILAR_THRESHOLD, 80);
        edtSimilar.setText(similar + "");

        findViewById(R.id.btn_set_similar_threshold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String similar = edtSimilar.getText().toString();
                int sml = Integer.parseInt(similar);
                if (sml >= 100) {//太大
                    UIUtils.showTitleTip(ThermalSettingActivity.this, "阈值过大将导致无法识别\n请重新设置");
                    return;
                } else if (sml <= 65) {//太小
                    UIUtils.showTitleTip(ThermalSettingActivity.this, "阈值过小会导致误识率过高\n请重新设置");
                    return;
                } else if (sml > 90) {//比较大
                    UIUtils.showTitleTip(ThermalSettingActivity.this, "阈值设置较大，识别速度将有所变慢\n设置成功");
                } else if (sml < 75) {//比较小
                    UIUtils.showTitleTip(ThermalSettingActivity.this, "阈值设置较小，误识率将增高\n设置成功");
                } else {
                    UIUtils.showTitleTip(ThermalSettingActivity.this, "设置成功");
                }

                SpUtils.saveInt(SpUtils.SIMILAR_THRESHOLD, sml);
                Activity activity = APP.getMainActivity();
                if (activity != null) {
                    if (activity instanceof ThermalImageActivity) {
                        ((ThermalImageActivity) activity).setFaceViewSimilar();
                    }
                }
            }
        });
    }

    //初始化网络信息
    private void initNetInfo() {
        TextView tvNetState = findViewById(R.id.tv_wifi_state);
        String net = "";
        boolean intenetConnected = isInternetConnected(this);
        if (intenetConnected) {
            net = getString(R.string.setting_net_prefix) + getHostIp() + "】";
        } else {
            net = "【WIFI，" + getWifiInfo(0) + getString(R.string.setting_ip_info) + getWifiInfo(1) + "】";
        }
        tvNetState.setText(net);
    }

    //初始化摄像头尺寸设置
    private void initCameraSizeSetting() {
        List<Camera.Size> supportSizeList = ExtCameraManager.instance().getSupportSizeList();
        if (supportSizeList == null) {
            return;
        }
        Collections.sort(supportSizeList, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size o1, Camera.Size o2) {
                if (o1.width > o2.width) {
                    return -1;
                }

                if (o1.width == o2.width) {
                    if (o1.height > o2.height) {
                        return -1;
                    }
                    return 0;
                }
                return 1;
            }
        });


        final List<SizeBean> sizeBeanList = new ArrayList<>();
        for (Camera.Size size : supportSizeList) {
            SizeBean sizeBean = new SizeBean();
            sizeBean.width = size.width;
            sizeBean.height = size.height;
            float i = (float) size.width / (float) size.height;

            if (i < 1.6) {
                if (i > 1.3) {
                    sizeBean.desc = "（4 : 3）";
                } else {
                    sizeBean.desc = "（5 : 4）";
                }
            } else {
                sizeBean.desc = "（16 : 9）";
            }

            if (sizeBean.width == 1280 && sizeBean.height == 720) {
                sizeBean.desc += getString(R.string.setting_best_camera_size);
            } else if (sizeBean.width == 1920 && sizeBean.height == 1080) {
                sizeBean.desc += getString(R.string.setting_max_camera_size);
            }
            sizeBeanList.add(sizeBean);
        }

        int cameraWidth = CameraSettings.getCameraWidth();
        int cameraHeight = CameraSettings.getCameraHeight();

        int index = 0;
        for (int i = 0; i < sizeBeanList.size(); i++) {
            SizeBean sizeBean = sizeBeanList.get(i);
            if (cameraWidth == sizeBean.width && cameraHeight == sizeBean.height) {
                index = i;
            }
        }

        Spinner spnCameraSize = findViewById(R.id.spn_camera_size);
        spnCameraSize.setAdapter(new SizeAdapter(sizeBeanList));
        Drawable drawable = getResources().getDrawable(R.drawable.shape_spinner_drop);
        spnCameraSize.setPopupBackgroundDrawable(drawable);
        spnCameraSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                SizeBean sizeBean = sizeBeanList.get(position);
                CameraSettings.setCameraPreviewWidth(sizeBean.width);
                CameraSettings.setCameraPreviewHeight(sizeBean.height);
                SpUtils.saveInt(SpUtils.CAMERA_WIDTH, sizeBean.width);
                SpUtils.saveInt(SpUtils.CAMERA_HEIGHT, sizeBean.height);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spnCameraSize.setSelection(index);
    }

    public void modifyPWD(View view) {
        setPwd();
    }

    public void checkUpgrade(View view) {
        UpdateVersionControl.getInstance().checkUpdate(this);
    }

    public void setAngle(final View view) {
        int anInt = SpUtils.getIntOrDef(SpUtils.CAMERA_ANGLE, Constants.DEFAULT_CAMERA_ANGLE);
        if (anInt == CameraSettings.ROTATION_0) {
            anInt = CameraSettings.ROTATION_90;
        } else if (anInt == CameraSettings.ROTATION_90) {
            anInt = CameraSettings.ROTATION_180;
        } else if (anInt == CameraSettings.ROTATION_180) {
            anInt = CameraSettings.ROTATION_270;
        } else {
            anInt = CameraSettings.ROTATION_0;
        }
        ((Button) view).setText(getString(R.string.setting_cam_angle) + ":" + anInt);
        SpUtils.saveInt(SpUtils.CAMERA_ANGLE, anInt);
        EventBus.getDefault().post(new DisplayOrientationEvent());
    }

    public void rebootDevice(View view) {
        showAlert(getString(R.string.setting_device_will_reboot), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ProgressDialog progressDialog = UIUtils.coreInfoShow3sDialog(ThermalSettingActivity.this);
                progressDialog.setTitle(getString(R.string.setting_device_reboot));
                progressDialog.setMessage(getString(R.string.setting_3_scond_reboot));
                progressDialog.setCancelable(false);
                progressDialog.show();
                UIUtils.restart.start();
            }
        }, null, null);
    }

    /**
     * ====功能区==================================================================================================
     */

    private static boolean isInternetConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mInternetNetWorkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
            boolean hasInternet = !isNullObject(mInternetNetWorkInfo) && mInternetNetWorkInfo.isConnected() && mInternetNetWorkInfo.isAvailable();
            return hasInternet;
        }
        return false;
    }

    private String getHostIp() {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("yao", "SocketException");
            e.printStackTrace();
        }
        return hostIp;
    }

    private static String getWifiInfo(int type) {
        WifiManager wifiManager = (WifiManager) APP.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            return null;
        }

        WifiInfo wi = wifiManager.getConnectionInfo();
        Log.e(TAG, "getWifiInfo() wi=" + wi);
        if (wi == null) {
            return null;
        }
        if (type == 0) {
            return APP.getContext().getResources().getString(R.string.setting_wifi_name) + wi.getSSID() + APP.getContext().getResources().getString(R.string.setting_wifi_rssi) + wi.getRssi();
        }

        //获取32位整型IP地址
        int ipAdd = wi.getIpAddress();
        Log.e(TAG, "getWifiInfo() ipAdd=" + ipAdd);
        if (ipAdd == 0) {
            return null;
        }
        //把整型地址转换成“*.*.*.*”地址
        String ip = intToIp(ipAdd);
        Log.e(TAG, "getWifiInfo() ip=" + ip);

        if (ip == null || ip.startsWith("0")) {
            return null;
        }
        return ip;
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    /**
     * 判断对象是否为空
     *
     * @param object
     * @return
     */
    private static boolean isNullObject(Object object) {

        if (object == null) {
            return true;
        }

        return false;
    }

    private static class CpuUtils {
        private CpuUtils() {
            //no instance
        }

        private static final List<String> CPU_TEMP_FILE_PATHS = Arrays.asList(
                "/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp",
                "/sys/devices/system/cpu/cpu0/cpufreq/FakeShmoo_cpu_temp",
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/class/i2c-adapter/i2c-4/4-004c/temperature",
                "/sys/devices/platform/tegra-i2c.3/i2c-4/4-004c/temperature",
                "/sys/devices/platform/omap/omap_temp_sensor.0/temperature",
                "/sys/devices/platform/tegra_tmon/temp1_input",
                "/sys/kernel/debug/tegra_thermal/temp_tj",
                "/sys/devices/platform/s5p-tmu/temperature",
                "/sys/class/thermal/thermal_zone1/temp",
                "/sys/class/hwmon/hwmon0/device/temp1_input",
                "/sys/devices/virtual/thermal/thermal_zone1/temp",
                "/sys/devices/virtual/thermal/thermal_zone0/temp",
                "/sys/class/thermal/thermal_zone3/temp",
                "/sys/class/thermal/thermal_zone4/temp",
                "/sys/class/hwmon/hwmonX/temp1_input",
                "/sys/devices/platform/s5p-tmu/curr_temp");

        public static final String getCpuTemperatureFinder() {
            String currTemp = "-1";
            for (String cpuTempFilePath : CPU_TEMP_FILE_PATHS) {
                Double temp = readOneLine(new File(cpuTempFilePath));
                String validPath = "";
                double currentTemp = 0.0D;
                if (isTemperatureValid(temp)) {
                    validPath = cpuTempFilePath;
                    currentTemp = temp;
                } else if (isTemperatureValid(temp / (double) 1000)) {
                    validPath = cpuTempFilePath;
                    currentTemp = temp / (double) 1000;
                }

                if (!TextUtils.isEmpty(validPath)
                        && (currentTemp != 0)) {
                    currTemp = currentTemp + "";
                }
            }
            return currTemp;
        }

        private static double readOneLine(File file) {
            FileInputStream fileInputStream = null;
            String s = "";
            try {
                fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                s = bufferedReader.readLine();
                fileInputStream.close();
                inputStreamReader.close();
                bufferedReader.close();
            } catch (IOException e) {
            }

            double result = 0;
            try {
                result = Double.parseDouble(s);
            } catch (NumberFormatException ignored) {
            }
            return result;
        }

        private static boolean isTemperatureValid(double temp) {
            return temp >= -30.0D && temp <= 250.0D;
        }
    }

    class SizeAdapter extends BaseAdapter {
        List<SizeBean> sizeBeanList;

        public SizeAdapter(List<SizeBean> sizeBeanList) {
            this.sizeBeanList = sizeBeanList;
        }

        @Override
        public int getCount() {
            return sizeBeanList.size();
        }

        @Override
        public Object getItem(int position) {

            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView inflate = (TextView) View.inflate(ThermalSettingActivity.this, android.R.layout.simple_dropdown_item_1line, null);
            inflate.setTextColor(Color.parseColor("#59B4CA"));
            inflate.setTextSize(16);
            inflate.setPadding(15, 15, 15, 15);
            SizeBean sizeBean = sizeBeanList.get(position);
            inflate.setText(sizeBean.width + " * " + sizeBean.height + " " + sizeBean.desc);
            return inflate;
        }
    }

    class SizeBean {
        int width;
        int height;
        String desc;
    }

    private void showAlert(String msg, Dialog.OnClickListener onClickListener, Dialog.OnClickListener onCancel, DialogInterface.OnDismissListener onDissmissListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.base_tip));
        builder.setMessage(msg);
        builder.setPositiveButton(getString(R.string.base_ensure), onClickListener);
        builder.setNegativeButton(getString(R.string.base_cancel), onCancel);
        if (onDissmissListener != null) {
            builder.setOnDismissListener(onDissmissListener);
        }

        AlertDialog alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        alertDialog.show();
        window.setWindowAnimations(R.style.mystyle);  //添加动画
    }

    public void setPwd() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_set_pwd);

        final EditText edtPwd = (EditText) dialog.findViewById(R.id.edt_set_pwd);
        final EditText edtPwd2 = (EditText) dialog.findViewById(R.id.edt_set_pwd_again);
        final Button btnCancel = (Button) dialog.findViewById(R.id.btn_pwd_cancel);
        final Button btnConfirm = (Button) dialog.findViewById(R.id.btn_pwd_confirm);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(edtPwd.getText())) {
                    edtPwd.setError(getString(R.string.setting_password_not_null));
                    return;
                }
                if (edtPwd.getText().length() < 6) {
                    edtPwd.setError(getString(R.string.setting_password_min_6));
                    return;
                }
                if (TextUtils.isEmpty(edtPwd2.getText())) {
                    edtPwd2.setError(getString(R.string.setting_please_input_password_agian));
                    return;
                }
                String pwd = edtPwd.getText().toString();
                final String pwd2 = edtPwd2.getText().toString();
                if (!TextUtils.equals(pwd, pwd2)) {
                    edtPwd2.setError(getString(R.string.setting_password_disaccord));
                    return;
                }

                btnCancel.setEnabled(false);
                btnConfirm.setEnabled(false);
                Map<String, String> params = new HashMap<>();
                params.put("deviceNo", HeartBeatClient.getDeviceNo());
                params.put("password", pwd2);
                OkHttpUtils.post().url(ResourceUpdate.UPDATE_PWD).params(params).build().execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        UIUtils.showNetLoading(ThermalSettingActivity.this);
                    }

                    @Override
                    public void onError(Call call, final Exception e, int id) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UIUtils.showTitleTip(ThermalSettingActivity.this, getString(R.string.setting_edit_password_failed) + ":" + e != null ? e.getMessage() : "NULL");
                            }
                        });
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        JSONObject jsonObject = JSONObject.parseObject(response);
                        final Integer status = jsonObject.getInteger("status");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (status == 1) {
                                    UIUtils.showTitleTip(ThermalSettingActivity.this, getString(R.string.setting_edit_password_success));
                                    SpUtils.saveStr(SpUtils.MENU_PWD, pwd2);
                                    dialog.dismiss();
                                } else {
                                    UIUtils.showTitleTip(ThermalSettingActivity.this, getString(R.string.setting_edit_password_failed));
                                }
                            }
                        });
                    }

                    @Override
                    public void onAfter(int id) {
                        UIUtils.dismissNetLoading();
                        btnConfirm.setEnabled(true);
                        btnCancel.setEnabled(true);
                    }
                });
            }
        });

        dialog.show();
        Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.mystyle);  //添加动画
        window.setBackgroundDrawableResource(android.R.color.transparent);
    }

}
