package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;
import com.tencent.bugly.beta.upgrade.UpgradeListener;
import com.tencent.bugly.beta.upgrade.UpgradeStateListener;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.XmppConnectEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.common.UpdateVersionControl;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.faceview.camera.CameraSettings;
import com.yunbiao.ybsmartcheckin_live_id.system.CoreInfoHandler;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.utils.FileUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.RestartAPPTool;
import com.yunbiao.ybsmartcheckin_live_id.utils.SkinLoader;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.yunbiao.ybsmartcheckin_live_id.views.ImageFileLoader;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import skin.support.SkinCompatManager;

public class SystemActivity extends BaseActivity implements View.OnClickListener {

    private Button btn_depart_system;
    private Button btn_add_system;
    private Button btn_data_system;
    private Button btn_setting_system;
    private TextView btn_update_system;
    private TextView tv_company_system;
    private TextView tv_deviceno_system;
    private TextView tv_exp_system;
    private TextView tv_server_system;
    private TextView tv_version_system;
    private TextView tv_online_system;
    private TextView tvDataSize;
    private TextView tvNetState;
    private TextView tvCameraInfo;
    private ImageView ivQrCode;
    private ImageView ivLogo;
    private TextView tvCompName;
    private View ivBack;
    private TextView tv_bindcode_syetem;
    private CheckBox cbMirror;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    protected int getPortraitLayout() {
        if (Constants.SCREEN_TYPE == Constants.ScreenType.TYPE_PORTRAIT_8_800_1280) {
            return R.layout.activity_system_fake_landscape;
        }
        return R.layout.activity_system;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_system_h;
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        ivLogo = (ImageView) findViewById(R.id.iv_system_logo);
        tvCompName = (TextView) findViewById(R.id.tv_system_compName);

        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_depart_system = (Button) findViewById(R.id.btn_depart_system);
        btn_add_system = (Button) findViewById(R.id.btn_add_system);
        btn_data_system = (Button) findViewById(R.id.btn_data_system);
        btn_setting_system = (Button) findViewById(R.id.btn_setting_system);
        btn_update_system = (TextView) findViewById(R.id.btn_update_system);

        tv_bindcode_syetem = (TextView) findViewById(R.id.tv_bindcode_syetem);
        tv_company_system = (TextView) findViewById(R.id.tv_company_system);
        tv_deviceno_system = (TextView) findViewById(R.id.tv_deviceno_system);
        tv_exp_system = (TextView) findViewById(R.id.tv_exp_system);
        tv_server_system = (TextView) findViewById(R.id.tv_server_system);
        tv_version_system = (TextView) findViewById(R.id.tv_version_system);
        tv_online_system = (TextView) findViewById(R.id.tv_online_system);
        ivQrCode = (ImageView) findViewById(R.id.iv_qrcode_system);

        btn_depart_system.setOnClickListener(this);
        btn_add_system.setOnClickListener(this);
        btn_data_system.setOnClickListener(this);
        btn_setting_system.setOnClickListener(this);
        btn_update_system.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        String appName = getResources().getString(R.string.app_name);
        try {
            PackageInfo packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            appName += " V" + packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        tv_version_system.setText(appName);

        updateServerState();

        setInfo();

        com.yunbiao.ybsmartcheckin_live_id.utils.FileUtils.getDataSize(new com.yunbiao.ybsmartcheckin_live_id.utils.FileUtils.OnSizeCallback() {
            @Override
            public void getSize(long size) {
                Log.e("123", "getSize: -----------" + size);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent updateEvent) {
        setInfo();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(XmppConnectEvent connectEvent) {
        tv_online_system.setText(connectEvent.isConnected() ? getString(R.string.act_sys_tip_online) : getString(R.string.act_sys_tip_outline));
    }

    public void setInfo() {
        Company company = SpUtils.getCompany();
        ImageFileLoader.i().loadAndSave(this, company.getComlogo(), Constants.DATA_PATH, ivLogo);

        tv_company_system.setText(company.getComname());

        String serNum = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        tv_deviceno_system.setText(serNum);

        String bindCode = SpUtils.getStr(SpUtils.BIND_CODE);
        tv_bindcode_syetem.setText(bindCode);

        String expDate = SpUtils.getStr(SpUtils.EXP_DATE);
        if (TextUtils.isEmpty(expDate)) {
            expDate = getString(R.string.act_sys_tip_validityPeriod);
        } else {
            expDate = dateFormat.format(new Date(Long.parseLong(expDate)));
        }
        tv_exp_system.setText(expDate);

        tv_online_system.setText(CoreInfoHandler.isOnline ? getString(R.string.act_sys_tip_online) : getString(R.string.act_sys_tip_outline));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_depart_system:
                startActivity(new Intent(this, EmployListActivity.class));
                break;
            case R.id.btn_add_system:
                Intent intent = new Intent(this, EditEmployActivity.class);
                intent.putExtra(EditEmployActivity.KEY_TYPE, EditEmployActivity.TYPE_ADD);
                startActivity(intent);
                break;
            case R.id.btn_data_system:
                startActivity(new Intent(this, SignActivity.class));
                break;
            case R.id.btn_setting_system:
                startActivity(new Intent(this, SettingActivity.class));
                break;
            case R.id.btn_update_system:
                UpdateVersionControl.getInstance().checkUpdate(this);

                Beta.upgradeListener = new UpgradeListener() {
                    @Override
                    public void onUpgrade(int i, UpgradeInfo upgradeInfo, boolean b, boolean b1) {

                    }
                };

                Beta.upgradeStateListener = new UpgradeStateListener() {
                    @Override
                    public void onUpgradeFailed(boolean b) {
                        Log.e("123", "onUpgradeNoVersion: 3333333333333333");
                    }

                    @Override
                    public void onUpgradeSuccess(boolean b) {
                        Log.e("123", "onUpgradeNoVersion: 2222222222222222");
                    }

                    @Override
                    public void onUpgradeNoVersion(boolean b) {
                        Log.e("123", "onUpgradeNoVersion: 11111111111111111");
                    }

                    @Override
                    public void onUpgrading(boolean b) {
                        Log.e("123", "onUpgradeNoVersion: 4444444444444444444");
                    }

                    @Override
                    public void onDownloadCompleted(boolean b) {
                        Log.e("123", "onUpgradeNoVersion: 55555555555555555555");
                    }
                };
                Beta.checkUpgrade(true, false);
                break;
            case R.id.btn_setnet_system:
                setNetServer();
                break;
            default:
                break;
        }
    }

    private void updateServerState() {
        String host = Constants.RESOURCE_URL;
        tv_server_system.setText(getString(R.string.act_sys_tip_cloundService));
        if (host.contains("192.168.")) {
            tv_server_system.setText(getString(R.string.act_sys_tip_localService));
            ivQrCode.setVisibility(View.GONE);
        } else {
//            ivQrCode.setVisibility(View.VISIBLE);
//            tvQrLable.setVisibility(View.VISIBLE);
        }
    }

    // TODO: 2019/6/27 ComById
//    public void setComId(View view) {
//        final String text = ((EditText) findViewById(R.id.edt_comid)).getText().toString();
//        if(TextUtils.isEmpty(text)){
//            UIUtils.showTitleTip("公司ID不可为空");
//            return;
//        }
//        showAlert("公司ID修改为:" + text + "\n重启应用后生效，是否重启？", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                SpUtils.saveCompanyId(Integer.valueOf(text));
//                finishAll();
//                RestartAPPTool.restartAPP(SystemActivity.this);
//            }
//        }, new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                ((EditText)findViewById(R.id.edt_comid)).setText("");
//                ((EditText)findViewById(R.id.edt_comid)).setHint("" +SpUtils.getCompanyId());
//            }
//        });
//    }

    private void setTextWatchers(final EditText[] editTexts) {
        for (int i = 0; i < editTexts.length; i++) {
            EditText editText = editTexts[i];
            final int finalI = i;
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() >= 3 && (finalI < editTexts.length - 1)) {
                        editTexts[finalI + 1].requestFocus();
                    } else if ((s.length() <= 0) && (finalI != 0)) {
                        editTexts[finalI - 1].requestFocus();
                    }
                }
            });
        }
    }

    //设置网络服务
    private void setNetServer() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_set_server);

        final TextView tvTips = (TextView) dialog.findViewById(R.id.tv_net_tips);
        final EditText tvIp0 = (EditText) dialog.findViewById(R.id.edt_server_ip_0);
        final EditText tvIp1 = (EditText) dialog.findViewById(R.id.edt_server_ip_1);
        final EditText tvIp2 = (EditText) dialog.findViewById(R.id.edt_server_ip_2);
        final EditText tvIp3 = (EditText) dialog.findViewById(R.id.edt_server_ip_3);
        final EditText tvSPort = (EditText) dialog.findViewById(R.id.edt_server_port);
        final EditText[] ipEdts = {tvIp0, tvIp1, tvIp2, tvIp3, tvSPort};
        setTextWatchers(ipEdts);

        final EditText tvRIp0 = (EditText) dialog.findViewById(R.id.edt_res_ip_0);
        final EditText tvRIp1 = (EditText) dialog.findViewById(R.id.edt_res_ip_1);
        final EditText tvRIp2 = (EditText) dialog.findViewById(R.id.edt_res_ip_2);
        final EditText tvRIp3 = (EditText) dialog.findViewById(R.id.edt_res_ip_3);
        final EditText tvRPort = (EditText) dialog.findViewById(R.id.edt_res_port);
        final EditText[] resEdits = {tvRIp0, tvRIp1, tvRIp2, tvRIp3, tvRPort};
        setTextWatchers(resEdits);

        Button btnCancel = (Button) dialog.findViewById(R.id.btn_net_cancel);
        Button btnConfirm = (Button) dialog.findViewById(R.id.btn_net_confirm);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_net_cancel:
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        break;
                    case R.id.btn_net_confirm:
                        String ip0 = tvIp0.getText().toString();
                        String ip1 = tvIp1.getText().toString();
                        String ip2 = tvIp2.getText().toString();
                        String ip3 = tvIp3.getText().toString();
                        String sPort = tvSPort.getText().toString();
                        if (TextUtils.isEmpty(ip0)
                                || TextUtils.isEmpty(ip1)
                                || TextUtils.isEmpty(ip2)
                                || TextUtils.isEmpty(ip3)
                                || TextUtils.isEmpty(sPort)) {
                            tvTips.setText(getString(R.string.act_sys_tip_yfwipdzhdkhbnwk));
                            return;
                        }

                        String rip0 = tvRIp0.getText().toString();
                        String rip1 = tvRIp1.getText().toString();
                        String rip2 = tvRIp2.getText().toString();
                        String rip3 = tvRIp3.getText().toString();
                        String rport = tvRPort.getText().toString();
                        if (TextUtils.isEmpty(ip0)
                                || TextUtils.isEmpty(ip1)
                                || TextUtils.isEmpty(ip2)
                                || TextUtils.isEmpty(ip3)
                                || TextUtils.isEmpty(rport)) {
                            tvTips.setText(getString(R.string.act_sys_tip_zyipdzhdkbnwk));
                            return;
                        }


                        RestartAPPTool.restartAPP(SystemActivity.this);
                        break;
                }
            }
        };
        btnCancel.setOnClickListener(onClickListener);
        btnConfirm.setOnClickListener(onClickListener);

        dialog.show();
        Window window = dialog.getWindow();
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            Display defaultDisplay = getWindowManager().getDefaultDisplay();
            int width = defaultDisplay.getWidth();
            int height = defaultDisplay.getHeight();
            window.setLayout(width / 2, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
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
                    edtPwd.setError(getString(R.string.act_sys_error_mmbkwk));
                    return;
                }
                if (edtPwd.getText().length() < 6) {
                    edtPwd.setError(getString(R.string.act_sys_error_mmzssr6w));
                    return;
                }
                if (TextUtils.isEmpty(edtPwd2.getText())) {
                    edtPwd2.setError(getString(R.string.act_sys_error_qzcsrmm));
                    return;
                }
                String pwd = edtPwd.getText().toString();
                final String pwd2 = edtPwd2.getText().toString();
                if (!TextUtils.equals(pwd, pwd2)) {
                    edtPwd2.setError(getString(R.string.act_sys_error_lcsrdmmbyz));
                    return;
                }

                btnCancel.setEnabled(false);
                btnConfirm.setEnabled(false);
                Map<String, String> params = new HashMap<>();
                params.put("deviceNo", HeartBeatClient.getDeviceNo());
                params.put("password", pwd2);
                OkHttpUtils.post().url(ResourceUpdate.UPDATE_PWD).params(params).build().execute(new StringCallback() {
                    @Override
                    public void onError(Call call, final Exception e, int id) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UIUtils.showTitleTip(SystemActivity.this, getString(R.string.act_sys_error_modify_fail) + "：" + e != null ? e.getMessage() : "NULL");
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
                                    UIUtils.showTitleTip(SystemActivity.this, getString(R.string.act_sys_error_modify_success));
                                    SpUtils.saveStr(SpUtils.MENU_PWD, pwd2);
                                    dialog.dismiss();
                                } else {
                                    UIUtils.showTitleTip(SystemActivity.this, getString(R.string.act_sys_error_modify_fail));
                                }
                            }
                        });
                    }

                    @Override
                    public void onAfter(int id) {
                        btnConfirm.setEnabled(true);
                        btnCancel.setEnabled(true);
                    }
                });
            }
        });

        dialog.show();
        Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.mystyle);  //添加动画
    }

    public void setAngle(View view) {
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
        CameraSettings.setCameraDisplayRotation(anInt);
        ((Button) view).setText(getString(R.string.act_sys_tip_angle) + "：" + anInt);
        SpUtils.saveInt(SpUtils.CAMERA_ANGLE, anInt);
    }

    public void showSetting() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(View.inflate(this, R.layout.layout_setting, null));

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.tv_setting_clear_cache:
                        showAlert(getString(R.string.act_sys_tip_cczjqcyyhcfsfjx), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO: 2019/4/1 清除缓存
                                SpUtils.clear(SystemActivity.this);
                                RestartAPPTool.restartAPP(SystemActivity.this);
                            }
                        });
                        break;
                    case R.id.tv_setting_clear_data:
//                        showAlert("此操作将清空应用数据并重启系统，是否继续？\n包括【人脸数据、员工信息、广告资源、签到数据】", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                boolean b = FileUtils.clearData();
//                                if(b){
//                                    finishAll();
//                                    RestartAPPTool.restartAPP(SystemActivity.this);
//                                }
//                            }
//                        });
                        break;
                    case R.id.tv_setting_pwd:
                        setPwd();
                        break;
                    case R.id.tv_setting_check_camera:
                        setCameraInfo();
                        break;
                }
            }
        };
        dialog.findViewById(R.id.tv_setting_clear_cache).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.tv_setting_clear_data).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.tv_setting_pwd).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.tv_setting_check_camera).setOnClickListener(onClickListener);
        tvDataSize = (TextView) dialog.findViewById(R.id.tv_setting_data_size);
        tvNetState = (TextView) dialog.findViewById(R.id.tv_setting_net_state);
        tvCameraInfo = (TextView) dialog.findViewById(R.id.tv_setting_camera_info);
        cbMirror = (CheckBox) dialog.findViewById(R.id.cb_mirror);

        Button btn = (Button) dialog.findViewById(R.id.btn_setAngle);
        int anInt = SpUtils.getIntOrDef(SpUtils.CAMERA_ANGLE, Constants.DEFAULT_CAMERA_ANGLE);
        btn.setText("角度：" + anInt);

//        setCamOri(dialog);
        checkDataSize();

        final boolean mirror = SpUtils.isMirror();
        cbMirror.setChecked(true);
        cbMirror.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlert(getString(R.string.act_sys_tip_ggsxtpzxycqyycnsx), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }, new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        cbMirror.setChecked(mirror);
                    }
                });
            }
        });


        Button btn_close_setting = (Button) dialog.findViewById(R.id.btn_close_setting);
        btn_close_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                setNetState();
                setCameraInfo();
            }
        });

        dialog.show();
        Window window = dialog.getWindow();
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            Display defaultDisplay = getWindowManager().getDefaultDisplay();
            int width = defaultDisplay.getWidth();
            window.setLayout((int) (width / 1.5), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        window.setWindowAnimations(R.style.mystyle);  //添加动画
    }

    private void checkDataSize() {
        FileUtils.getDataSize(new FileUtils.OnSizeCallback() {
            @Override
            public void getSize(long size) {
                if (size > 0) {
                    size = size / 1024 / 1024;
                }
                tvDataSize.setText(size + "mb");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    //设置网络状态
    private void setNetState() {
        CheckNet checkNet = new CheckNet(this);
        boolean intenetConnected = checkNet.isIntenetConnected();
        if (intenetConnected) {//网线连接
            if (checkNet.isEtherneteConncted()) {//已连接
                tvNetState.setText(getString(R.string.act_sys_tip_wxlj));
            } else {
                tvNetState.setText(getString(R.string.act_sys_tip_wxlj_wwl));
            }
            return;
        }

        boolean wifiEnabled = checkNet.isWifiEnabled();
        if (!wifiEnabled) {
            //代表无网络
            tvNetState.setText(getString(R.string.act_sys_tip_wxlj_wwllj));
            return;
        }

        boolean wifiConnected = checkNet.isWifiConnected();
        if (!wifiConnected) {
            //代表无网络
            tvNetState.setText(getString(R.string.act_sys_tip_wxlj_wwllj));
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getString(R.string.act_sys_tip_wxlj_wllx_wifi));
        stringBuilder.append(getString(R.string.act_sys_tip_wxlj_wlmc));
        stringBuilder.append(getString(R.string.act_sys_tip_xh));
        tvNetState.setText(stringBuilder.toString());
    }

    //设置摄像头信息
    private void setCameraInfo() {
        CheckCamera checkCamera = new CheckCamera();
        String cameraInfo = checkCamera.getCameraInfo();
        tvCameraInfo.setText(cameraInfo);
    }

    private void showAlert(String msg, Dialog.OnClickListener onClickListener) {
        showAlert(msg, onClickListener, null);
    }

    private void showAlert(String msg, Dialog.OnClickListener onClickListener, DialogInterface.OnDismissListener onDissmissListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.base_tip));
        builder.setMessage(msg);
        builder.setPositiveButton(getString(R.string.base_ensure), onClickListener);
        if (onDissmissListener != null) {
            builder.setOnDismissListener(onDissmissListener);
        }

        AlertDialog alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        alertDialog.show();
        window.setWindowAnimations(R.style.mystyle);  //添加动画
    }

    class SkinModel {
        String skinName;
        File skinFile;
    }

    public void seeVisitor(View view) {
        startActivity(new Intent(this, VisitorActivity.class));
    }

    public void selectSkin(View view) {
        final File file = new File(Constants.SKIN_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        final File[] files = file.listFiles();

        if (files == null || files.length <= 0) {
            UIUtils.showTitleTip(this, "暂无皮肤");
            return;
        }

        final List<SkinModel> skinList = new ArrayList<>();
        SkinModel skinModel = new SkinModel();
        skinModel.skinName = "默认";
        skinList.add(skinModel);

        for (File file1 : files) {
            if (file1.getName().endsWith(".skin")) {
                SkinModel skin = new SkinModel();
                skin.skinName = file1.getName();
                skin.skinFile = file1;
                skinList.add(skin);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this).setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return skinList.size();
            }

            @Override
            public Object getItem(int position) {
                return skinList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                VH vh;
                if (convertView == null) {
                    vh = new VH();
                    convertView = View.inflate(parent.getContext(), android.R.layout.simple_list_item_1, null);
                    vh.tv = convertView.findViewById(android.R.id.text1);
                    convertView.setTag(vh);
                } else {
                    vh = (VH) convertView.getTag();
                }
                SkinModel skinModel = skinList.get(position);
                vh.tv.setText(skinModel.skinName);
                return convertView;
            }

            class VH {
                TextView tv;
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.e(TAG, "onClick: " + which);
                SkinModel skinModel = skinList.get(which);
                File skinFile = skinModel.skinFile;
                if (which == 0 || skinFile == null) {
                    SkinCompatManager.getInstance().restoreDefaultTheme();
                } else {
                    load(skinFile.getName());
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private static final String TAG = "SystemActivity";

    private void load(final String apkName) {
        Log.e(TAG, "load: 加载：" + " : " + apkName);
        SkinLoader.setSkin(apkName);
    }

    class CheckCamera {
        public String getCameraInfo() {
            StringBuilder cameraInfo = new StringBuilder();
            int numberOfCameras = android.hardware.Camera.getNumberOfCameras();
            if (numberOfCameras <= 0) {
                return getString(R.string.act_sys_tip_noCamera);
            }
            for (int i = 0; i < numberOfCameras; i++) {
                android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
                android.hardware.Camera.getCameraInfo(i, info);
                boolean isFront = info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;
                int orientation = info.orientation;
                cameraInfo
                        .append(getString(R.string.act_sys_tip_total) + numberOfCameras + getString(R.string.act_sys_tip_ge) + ":")
                        .append("【" + getString(R.string.act_sys_tip_bh) + ":" + i + "，")
                        .append(isFront ? getString(R.string.act_sys_tip_front) : getString(R.string.act_sys_tip_back))
                        .append("，" + getString(R.string.act_sys_tip_angle) + ":" + orientation)
                        .append("】");
            }

            return cameraInfo.toString();
        }
    }

    class CheckNet {
        private WifiManager wifiManager;
        private ConnectivityManager connectManager;
        private Context context;

        public CheckNet(Context context) {
            this.context = context;
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            connectManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        /**
         * 判断以太网网络是否可用
         *
         * @return
         */
        public boolean isIntenetConnected() {
            if (context != null) {
                ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mInternetNetWorkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
                boolean hasInternet = !isNullObject(mInternetNetWorkInfo) && mInternetNetWorkInfo.isConnected() && mInternetNetWorkInfo.isAvailable();
                return hasInternet;
            }
            return false;
        }

        /**
         * 判断对象是否为空
         *
         * @param object
         * @return
         */
        private boolean isNullObject(Object object) {
            return object == null;
        }

        //获取wifi状态
        public boolean isWifiEnabled() {
            int wifiState = wifiManager.getWifiState();
            return wifiState == WifiManager.WIFI_STATE_ENABLED;
        }

        public boolean isWifiConnected() {
            //wifi连接
            NetworkInfo info = connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return info.isConnected();
        }

        public boolean isEtherneteConncted() {
            NetworkInfo info = connectManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
            return info.isConnected();
        }

        //获取wifi名称
        public String getWifiName() {
            WifiInfo info = wifiManager.getConnectionInfo();
            if ((info != null) && (!TextUtils.isEmpty(info.getSSID()))) {
                return info.getSSID();
            }
            return "NULL";
        }

        public String getStrength() {
            String strength = "";
            WifiInfo info = wifiManager.getConnectionInfo();
            int rssi = info.getRssi();
            if (rssi <= 0 && rssi >= -50) {//信号最好
                strength = getString(R.string.act_sys_tip_strong);
            } else if (rssi < -50 && rssi >= -70) {//信号一般
                strength = getString(R.string.act_sys_tip_general);
            } else if (rssi > -70) {
                strength = getString(R.string.act_sys_tip_poor);
            } else if (rssi <= -200) {
                strength = getString(R.string.act_sys_tip_wwl);
            }
            return strength;
        }
    }

}

