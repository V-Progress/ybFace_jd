package com.yunbiao.ybsmartcheckin_live_id.activity_certificates;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSONObject;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.DisplayOrientationEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.PowerOnOffActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.common.UpdateVersionControl;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Request;
import timber.log.Timber;

public class CertificatesSettingActivity extends BaseActivity {
    private static final String TAG = "SettingActivity";


    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_certificates_setting_h;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_certificates_setting_h;
    }

    @Override
    protected void initView() {
        findViewById(R.id.iv_back).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                finish();
            }
            return false;
        });
    }

    public void jumpTag(View view) {
        final boolean jumpTag = SpUtils.getBoolean(Constants.Key.JUMP_TAG, Constants.Default.JUMP_TAG);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(APP.getContext().getResources().getString(R.string.setting_switch_function));
        builder.setMessage(APP.getContext().getResources().getString(R.string.setting_switch_tip2));
        builder.setNegativeButton(APP.getContext().getResources().getString(R.string.setting_switch_cancel), (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(APP.getContext().getResources().getString(R.string.setting_switch_confirm), (dialog, which) -> {
            dialog.dismiss();
            SpUtils.saveBoolean(Constants.Key.JUMP_TAG, !jumpTag);

            APP.exit2();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void initData() {
        //选择读卡器
        initReaderSetting();
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
        //摄像头设置
        initCameraSetting();
        //打印机配置
        initUsbPrinter();
        //初始化读卡模式
        initICCardModel();
        //收集手机号码
        initCollectPhone();
    }

    private void initCollectPhone(){
        boolean collectPhone = SpUtils.getBoolean(CertificatesConst.Key.COLLECT_PHONE_ENABLED, CertificatesConst.Default.COLLECT_PHONE_ENABLED);
        Switch swCollectPhone = findViewById(R.id.sw_collect_phone);
        swCollectPhone.setChecked(collectPhone);
        swCollectPhone.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(CertificatesConst.Key.COLLECT_PHONE_ENABLED, isChecked));
    }

    private void initICCardModel(){
        boolean icCardMode = SpUtils.getBoolean(CertificatesConst.Key.IC_CARD_MODE,CertificatesConst.Default.IC_CARD_MODE);
        Switch swICCard = findViewById(R.id.sw_ic_card_enabled);
        swICCard.setChecked(icCardMode);
        swICCard.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(CertificatesConst.Key.IC_CARD_MODE,isChecked));
    }

    private void initUsbPrinter(){
        Switch swPrinter = findViewById(R.id.sw_usb_printer_setting);
        boolean enabled = SpUtils.getBoolean(CertificatesConst.Key.USB_PRINTER_ENABLED, CertificatesConst.Default.USB_PRINTER_ENABLED);
        swPrinter.setChecked(enabled);
        swPrinter.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(CertificatesConst.Key.USB_PRINTER_ENABLED, isChecked));
    }

    @Override
    protected void onResume() {
        super.onResume();
        setCorrectUI();
    }

    public void powerOnOff(View view) {
        startActivity(new Intent(this, PowerOnOffActivity.class));
    }

    private void setCorrectUI() {
        //进入矫正
        float thermalCorrect = SpUtils.getFloat(CertificatesConst.Key.CORRECT_VALUE, CertificatesConst.Default.CORRECT_VALUE);
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
                SpUtils.saveFloat(CertificatesConst.Key.CORRECT_VALUE, corrValue);
                edtCorrect.setText(corrValue + "");
            }
        };
        btnCorrectSub.setOnClickListener(correctOnclickListener);
        btnCorrectAdd.setOnClickListener(correctOnclickListener);
        findViewById(R.id.btn_thermal_corr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CertificatesSettingActivity.this, CertificatesCorrectActivity.class));
            }
        });
//        findViewById(R.id.btn_thermal_corr).setVisibility(View.GONE);
    }


    private void initReaderSetting(){
        TextView tvReader = findViewById(R.id.tv_reader_setting);
        String[] readers = CertificatesConst.readers;
        final int readerType = SpUtils.getIntOrDef(CertificatesConst.Key.READER, CertificatesConst.Default.READER);
        tvReader.setText(readers[readerType]);
        tvReader.setOnClickListener(v -> {
            int currType = SpUtils.getIntOrDef(CertificatesConst.Key.READER, CertificatesConst.Default.READER);
            AlertDialog.Builder builder = new AlertDialog.Builder(CertificatesSettingActivity.this);
            builder.setTitle("选择读卡器");
            builder.setSingleChoiceItems(readers,currType,(dialog,which) -> {
                Timber.d("选择的模式：" + which + ", " + readers[which]);
                if(which == readerType){
                    dialog.dismiss();
                    return;
                }

                SpUtils.saveInt(CertificatesConst.Key.READER,which);
                tvReader.setText(readers[which]);

                dialog.dismiss();
            });
            builder.create().show();
        });

    }

    private void initModelSetting() {
        setCorrectUI();

        TextView tvMode = findViewById(R.id.tv_model_setting);
        final String[] items = CertificatesConst.models;
        final int model = SpUtils.getIntOrDef(CertificatesConst.Key.MODE, CertificatesConst.Default.MODE);
        tvMode.setText(items[model]);
        tvMode.setOnClickListener(v -> {
            final int currModel = SpUtils.getIntOrDef(CertificatesConst.Key.MODE, CertificatesConst.Default.MODE);
            Log.e(TAG,  "initModelSetting: 当前模式：" + model);
            AlertDialog.Builder builder = new AlertDialog.Builder(CertificatesSettingActivity.this);
            builder.setTitle(getResources().getString(R.string.setting_select_model));
            builder.setSingleChoiceItems(items, currModel, (dialog, whichModel) -> {
                Log.e(TAG, "initModelSetting: 选择后的模式：" + whichModel);
                //如果模式相同则直接隐藏
                if (whichModel == currModel) {
                    dialog.dismiss();
                    return;
                }

                SpUtils.saveInt(CertificatesConst.Key.MODE, whichModel);
                tvMode.setText(items[whichModel]);

                dialog.dismiss();
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
        //测温开关=============================================================================================
        boolean temperEnabled = SpUtils.getBoolean(CertificatesConst.Key.TEMPERATURE_ENABLED, CertificatesConst.Default.TEMPERATURE_ENABLED);
        Switch swTemperEnable = findViewById(R.id.sw_temperature_enable);
        swTemperEnable.setChecked(temperEnabled);
        swTemperEnable.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(CertificatesConst.Key.TEMPERATURE_ENABLED, isChecked));

        //热成像镜像==========================================================================================
        boolean thermalImgMirror = SpUtils.getBoolean(CertificatesConst.Key.THERMAL_MIRROR, CertificatesConst.Default.THERMAL_MIRROR);
        Switch swThermalMirror = findViewById(R.id.sw_thermal_imag_mirror_setting);
        swThermalMirror.setChecked(thermalImgMirror);
        swThermalMirror.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(CertificatesConst.Key.THERMAL_MIRROR, isChecked);
            }
        });

        //修改测温阈值==========================================================================================
        Button btnMinSub = findViewById(R.id.btn_temp_min_threshold_sub_setting);
        Button btnMinAdd = findViewById(R.id.btn_temp_min_threshold_add_setting);
        final EditText edtMinThreshold = findViewById(R.id.edt_temp_min_threshold_setting);
        final float minValue = SpUtils.getFloat(CertificatesConst.Key.MIN_THRESHOLD, CertificatesConst.Default.MIN_THRESHOLD);
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
                SpUtils.saveFloat(CertificatesConst.Key.MIN_THRESHOLD, v1);
            }
        };
        btnMinSub.setOnClickListener(minClickListener);
        btnMinAdd.setOnClickListener(minClickListener);

        //修改测温报警值==========================================================================================
        Button btnWarnSub = findViewById(R.id.btn_temp_warning_threshold_sub_setting);
        Button btnWarnAdd = findViewById(R.id.btn_temp_warning_threshold_add_setting);
        final EditText edtWarnThreshold = findViewById(R.id.edt_temp_warning_threshold_setting);
        final float warningValue = SpUtils.getFloat(CertificatesConst.Key.WARNING_THRESHOLD, CertificatesConst.Default.WARNING_THRESHOLD);
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

                SpUtils.saveFloat(CertificatesConst.Key.WARNING_THRESHOLD, v1);
            }
        };
        btnWarnSub.setOnClickListener(warnClickListener);
        btnWarnAdd.setOnClickListener(warnClickListener);

        //===低温模式=========================================================
        Switch swLowTempModel = findViewById(R.id.sw_low_temp_model_setting);
        boolean aBoolean = SpUtils.getBoolean(CertificatesConst.Key.LOW_TEMP, CertificatesConst.Default.LOW_TEMP);
        swLowTempModel.setChecked(aBoolean);

        //===高温模式=========================================================
        Switch swHighTempModel = findViewById(R.id.sw_high_temp_model_setting);
        boolean highTemp = SpUtils.getBoolean(CertificatesConst.Key.HIGH_TEMP,CertificatesConst.Default.HIGH_TEMP);
        swHighTempModel.setChecked(highTemp);

        swLowTempModel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(isChecked && swHighTempModel.isChecked()){
//                    swHighTempModel.setChecked(false);
//                }
                SpUtils.saveBoolean(CertificatesConst.Key.LOW_TEMP, isChecked);
            }
        });

        swHighTempModel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(isChecked && swLowTempModel.isChecked()){
//                    swLowTempModel.setChecked(false);
//                }
                SpUtils.saveBoolean(CertificatesConst.Key.HIGH_TEMP,isChecked);
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

        if (SpUtils.getIntOrDef(Constants.Key.SERVER_MODEL, Constants.Default.SERVER_MODEL) == Constants.serverModel.YUN) {
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
                    UIUtils.showTitleTip(CertificatesSettingActivity.this, "请设置IP地址");
                    return;
                }
                if (TextUtils.isEmpty(mResPort)) {
                    UIUtils.showTitleTip(CertificatesSettingActivity.this, "请设置接口端口");
                    return;
                }
                if (TextUtils.isEmpty(mXmppPort)) {
                    UIUtils.showTitleTip(CertificatesSettingActivity.this, "请设置XMPP端口");
                    return;
                }
                if (TextUtils.isEmpty(mProName)) {
                    UIUtils.showTitleTip(CertificatesSettingActivity.this, "请设置项目名");
                    return;
                }

                if (rbYun.isChecked()) {
                    SpUtils.saveInt(Constants.Key.SERVER_MODEL, Constants.serverModel.YUN);
                } else if (rbJu.isChecked()) {
                    SpUtils.saveInt(Constants.Key.SERVER_MODEL, Constants.serverModel.JU);
                    SpUtils.saveStr(Constants.Key.JU_XMPP_IP_CACHE, mIp);
                    SpUtils.saveStr(Constants.Key.JU_RESOURCE_PORT_CACHE, mResPort);
                    SpUtils.saveStr(Constants.Key.JU_XMPP_PORT_CACHE, mXmppPort);
                    SpUtils.saveStr(Constants.Key.JU_PROJECT_NAME_SUFFIX, mProName);
                }
                UIUtils.showTitleTip(CertificatesSettingActivity.this, "保存成功,重启APP后生效");
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
            ip = SpUtils.getStr(Constants.Key.JU_XMPP_IP_CACHE);
            resPort = SpUtils.getStr(Constants.Key.JU_RESOURCE_PORT_CACHE);
            xmppPort = SpUtils.getStr(Constants.Key.JU_XMPP_PORT_CACHE);
            proName = SpUtils.getStr(Constants.Key.JU_PROJECT_NAME_SUFFIX);
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

    //初始化摄像头设置
    private void initCameraSetting() {
//        TextView tvCamera = findViewById(R.id.tv_camera);
//        //摄像头模式
//        tvCamera.setText("【" + (Config.getCameraType() == Config.CAMERA_AUTO ? getString(R.string.act_set_tip_auto) : Config.getCameraType() == Config.CAMERA_BACK ? getString(R.string.act_set_tip_back) : getString(R.string.act_set_tip_front)) + getString(R.string.act_set_tip_fbl) + CameraSettings.getCameraPreviewWidth() + "*" + CameraSettings.getCameraPreviewHeight() + "】");
        //摄像头角度
        Button btnAngle = findViewById(R.id.btn_setAngle);
        int angle = SpUtils.getIntOrDef(Constants.Key.CAMERA_ANGLE, Constants.Default.CAMERA_ANGLE);
        btnAngle.setText(getString(R.string.setting_cam_angle) + ":" + angle);

        Button btnPicRotation = findViewById(R.id.btn_picture_rotation);
        int picRotation = SpUtils.getIntOrDef(Constants.Key.PICTURE_ROTATION, Constants.Default.PICTURE_ROTATION);
        btnPicRotation.setText(picRotation == -1 ? (getResources().getString(R.string.setting_picture_rotation)) : (getString(R.string.setting_cam_angle) + ":" + picRotation));
        btnPicRotation.setOnClickListener(v -> {
            int picRotation1 = SpUtils.getIntOrDef(Constants.Key.PICTURE_ROTATION, Constants.Default.PICTURE_ROTATION);
            if (picRotation1 == -1) {
                picRotation1 = 0;
            } else if (picRotation1 == 0) {
                picRotation1 = 90;
            } else if (picRotation1 == 90) {
                picRotation1 = 180;
            } else if (picRotation1 == 180) {
                picRotation1 = 270;
            } else {
                picRotation1 = -1;
            }
            btnPicRotation.setText(picRotation1 == -1 ? (getResources().getString(R.string.setting_picture_rotation)) : (getString(R.string.setting_cam_angle) + ":" + picRotation1));
            SpUtils.saveInt(Constants.Key.PICTURE_ROTATION, picRotation1);
        });
    }

    //初始化人脸框镜像设置
    private void initFaceRectMirrorSetting() {
        //人脸框横向镜像
        CheckBox cbMirror = findViewById(R.id.cb_mirror);
        final boolean mirror = SpUtils.getBoolean(Constants.Key.IS_H_MIRROR,Constants.Default.IS_H_MIRROR);
        cbMirror.setChecked(mirror);
        cbMirror.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(Constants.Key.IS_H_MIRROR, isChecked);
            }
        });

        //人脸框纵向镜像
        CheckBox cbVertical = findViewById(R.id.cb_vertical_mirror);
        boolean isVMirror = SpUtils.getBoolean(Constants.Key.IS_V_MIRROR,Constants.Default.IS_V_MIRROR);
        cbVertical.setChecked(isVMirror);
        cbVertical.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(Constants.Key.IS_V_MIRROR,isChecked);
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
        int cacheDelay = SpUtils.getIntOrDef(Constants.Key.GPIO_DELAY, Constants.Default.GPIO_DELAY);
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
                SpUtils.saveInt(Constants.Key.GPIO_DELAY, delay);
                UIUtils.showShort(CertificatesSettingActivity.this, getString(R.string.setting_edit_password_success));
            }
        });
    }

    //初始化相似度阈值设置
    private void initSimilarSetting() {
        final EditText edtSimilar = findViewById(R.id.edt_similar_threshold);
        int similar = SpUtils.getIntOrDef(CertificatesConst.Key.SIMILAR, CertificatesConst.Default.SIMILAR);
        edtSimilar.setText(similar + "");

        findViewById(R.id.btn_set_similar_threshold).setOnClickListener(v -> {
            String similar1 = edtSimilar.getText().toString();
            int sml = Integer.parseInt(similar1);
            SpUtils.saveInt(CertificatesConst.Key.SIMILAR, sml);
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
        /*List<Camera.Size> supportSizeList = ExtCameraManager.instance().getSupportSizeList();
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
        spnCameraSize.setSelection(index);*/
    }

    public void modifyPWD(View view) {
        setPwd();
    }

    public void checkUpgrade(View view) {
        UpdateVersionControl.getInstance().checkUpdate(this);
    }

    public void setAngle(final View view) {
        int anInt = SpUtils.getIntOrDef(Constants.Key.CAMERA_ANGLE, Constants.Default.CAMERA_ANGLE);
        if (anInt == 0) {
            anInt = 90;
        } else if (anInt == 90) {
            anInt = 180;
        } else if (anInt == 180) {
            anInt = 270;
        } else {
            anInt = 0;
        }
        ((Button) view).setText(getString(R.string.setting_cam_angle) + ":" + anInt);
        SpUtils.saveInt(Constants.Key.CAMERA_ANGLE, anInt);
        EventBus.getDefault().post(new DisplayOrientationEvent());
    }

    public void rebootDevice(View view) {
        showAlert(getString(R.string.setting_device_will_reboot), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ProgressDialog progressDialog = UIUtils.coreInfoShow3sDialog(CertificatesSettingActivity.this);
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
            TextView inflate = (TextView) View.inflate(CertificatesSettingActivity.this, android.R.layout.simple_dropdown_item_1line, null);
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
                        UIUtils.showNetLoading(CertificatesSettingActivity.this);
                    }

                    @Override
                    public void onError(Call call, final Exception e, int id) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UIUtils.showTitleTip(CertificatesSettingActivity.this, getString(R.string.setting_edit_password_failed) + ":" + e != null ? e.getMessage() : "NULL");
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
                                    UIUtils.showTitleTip(CertificatesSettingActivity.this, getString(R.string.setting_edit_password_success));
                                    SpUtils.saveStr(SpUtils.MENU_PWD, pwd2);
                                    dialog.dismiss();
                                } else {
                                    UIUtils.showTitleTip(CertificatesSettingActivity.this, getString(R.string.setting_edit_password_failed));
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
