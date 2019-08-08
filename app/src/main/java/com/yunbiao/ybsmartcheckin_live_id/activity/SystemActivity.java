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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;
import com.tencent.bugly.beta.upgrade.UpgradeListener;
import com.tencent.bugly.beta.upgrade.UpgradeStateListener;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.Config;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.LogoUpdateEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.PageUpdateEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.SysInfoUpdateEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.XmppConnectEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.system.CoreInfoHandler;
import com.yunbiao.ybsmartcheckin_live_id.db.CompBean;
import com.yunbiao.ybsmartcheckin_live_id.faceview.CameraManager;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.utils.FileUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.RestartAPPTool;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

public class SystemActivity extends BaseActivity implements View.OnClickListener {

    private Button btn_depart_system;
    private Button btn_add_system;
    private Button btn_data_system;
    private Button btn_setting_system;
    private TextView btn_update_system;
    private Button btn_setnet_system;
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
    private TextView tvQrLable;
    private ImageView ivLogo;
    private TextView tvCompName;
    private View ivBack;
    private TextView tv_bindcode_syetem;
    private CheckBox cbMirror;
    private CompBean compBean;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_system;
    }

    @Override
    protected int getLandscapeLayout() {
        if (Config.deviceType == Config.DEVICE_SMALL_FACE) {
            return R.layout.activity_system_h_small;
        } else {
            return R.layout.activity_system_h;
        }
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
        btn_setnet_system = (Button) findViewById(R.id.btn_setnet_system);

        tv_bindcode_syetem = (TextView) findViewById(R.id.tv_bindcode_syetem);
        tv_company_system = (TextView) findViewById(R.id.tv_company_system);
        tv_deviceno_system = (TextView) findViewById(R.id.tv_deviceno_system);
        tv_exp_system = (TextView) findViewById(R.id.tv_exp_system);
        tv_server_system = (TextView) findViewById(R.id.tv_server_system);
        tv_version_system = (TextView) findViewById(R.id.tv_version_system);
        tv_online_system = (TextView) findViewById(R.id.tv_online_system);
        ivQrCode = (ImageView) findViewById(R.id.iv_qrcode_system);
        tvQrLable = (TextView) findViewById(R.id.tv_qr_lable);

        btn_depart_system.setOnClickListener(this);
        btn_add_system.setOnClickListener(this);
        btn_data_system.setOnClickListener(this);
        btn_setting_system.setOnClickListener(this);
        btn_update_system.setOnClickListener(this);
        btn_setnet_system.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        compBean = APP.getCompBean();

        String appName = getResources().getString(R.string.app_name);
        try {
            PackageInfo packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            appName += " V" + packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        tv_version_system.setText(appName);

        updateServerState();

        setIcon();
        setInfo();

        com.yunbiao.ybsmartcheckin_live_id.utils.FileUtils.getDataSize(new com.yunbiao.ybsmartcheckin_live_id.utils.FileUtils.OnSizeCallback() {
            @Override
            public void getSize(long size) {
                Log.e("123", "getSize: -----------" + size);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(LogoUpdateEvent updateEvent) {
        d("update: ----- 收到LOGO更新事件");
        setIcon();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(SysInfoUpdateEvent updateEvent) {
        setInfo();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(XmppConnectEvent connectEvent) {
        tv_online_system.setText(connectEvent.isConnected() ? "在线" : "离线");
    }

    public void setIcon(){
        if(compBean == null) return;

        if(tv_company_system != null){
            tv_company_system.setText(compBean.getCompName());
        }

        if(ivLogo != null){
            File file = new File(compBean.getIconPath());
            Glide.with(this)
                    .load(file.exists() ? file : compBean.getIconUrl())
                    .skipMemoryCache(true)
                    .crossFade(500)
                    .into(ivLogo);
        }

        if(tvCompName != null){
            tvCompName.setText(compBean.getCompName());
        }
    }

    public void setInfo() {
        String serNum = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        tv_deviceno_system.setText(serNum);

        String bindCode = SpUtils.getStr(SpUtils.BINDCODE);
        tv_bindcode_syetem.setText(bindCode);

        String expDate = SpUtils.getStr(SpUtils.EXP_DATE);
        tv_exp_system.setText(TextUtils.isEmpty(expDate) ? "无限期" : expDate);

        tv_online_system.setText(CoreInfoHandler.isOnline ? "在线" : "离线");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_depart_system:
                startActivity(new Intent(this, EmployListActivity.class));
                break;
            case R.id.btn_add_system:
                startActivity(new Intent(this, AddEmployActivity.class));
                break;
            case R.id.btn_data_system:
                startActivity(new Intent(this, SignActivity.class));
                break;
            case R.id.btn_setting_system:
                startActivity(new Intent(this,SettingActivity.class));
                break;
            case R.id.btn_update_system:

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
                Beta.checkUpgrade(true,false);
                break;
            case R.id.btn_setnet_system:
                setNetServer();
                break;
            default:
                break;
        }
    }

    private void updateServerState(){
        String host = Constants.RESOURCE_URL;
        tv_server_system.setText("云服务");
        if (host.contains("192.168.")) {
            tv_server_system.setText("本地服务");
            ivQrCode.setVisibility(View.GONE);
            tvQrLable.setVisibility(View.GONE);
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

    private void setTextWatchers(final EditText[] editTexts){
        for (int i = 0; i < editTexts.length; i++) {
            EditText editText = editTexts[i];
            final int finalI = i;
            editText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if(s.length()>=3 && (finalI <editTexts.length-1)){
                        editTexts[finalI +1].requestFocus();
                    } else if((s.length()<=0) && (finalI != 0)){
                        editTexts[finalI-1].requestFocus();
                    }
                }
            });
        }
    }

    //设置网络服务
    private void setNetServer(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_set_server);

        final TextView tvTips = (TextView) dialog.findViewById(R.id.tv_net_tips);
        final EditText tvIp0 = (EditText) dialog.findViewById(R.id.edt_server_ip_0);
        final EditText tvIp1 = (EditText) dialog.findViewById(R.id.edt_server_ip_1);
        final EditText tvIp2 = (EditText) dialog.findViewById(R.id.edt_server_ip_2);
        final EditText tvIp3 = (EditText) dialog.findViewById(R.id.edt_server_ip_3);
        final EditText tvSPort = (EditText) dialog.findViewById(R.id.edt_server_port);
        final EditText[] ipEdts = {tvIp0,tvIp1,tvIp2,tvIp3,tvSPort};
        setTextWatchers(ipEdts);

        final EditText tvRIp0 = (EditText) dialog.findViewById(R.id.edt_res_ip_0);
        final EditText tvRIp1 = (EditText) dialog.findViewById(R.id.edt_res_ip_1);
        final EditText tvRIp2 = (EditText) dialog.findViewById(R.id.edt_res_ip_2);
        final EditText tvRIp3 = (EditText) dialog.findViewById(R.id.edt_res_ip_3);
        final EditText tvRPort = (EditText) dialog.findViewById(R.id.edt_res_port);
        final EditText[] resEdits = {tvRIp0,tvRIp1,tvRIp2,tvRIp3,tvRPort};
        setTextWatchers(resEdits);

        Button btnCancel = (Button) dialog.findViewById(R.id.btn_net_cancel);
        Button btnConfirm = (Button) dialog.findViewById(R.id.btn_net_confirm);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_net_cancel:
                        if(dialog.isShowing()){
                            dialog.dismiss();
                        }
                        break;
                    case R.id.btn_net_confirm:
                        String ip0 = tvIp0.getText().toString();
                        String ip1 = tvIp1.getText().toString();
                        String ip2 = tvIp2.getText().toString();
                        String ip3 = tvIp3.getText().toString();
                        String sPort = tvSPort.getText().toString();
                        if(TextUtils.isEmpty(ip0)
                                || TextUtils.isEmpty(ip1)
                                || TextUtils.isEmpty(ip2)
                                || TextUtils.isEmpty(ip3)
                                || TextUtils.isEmpty(sPort)){
                            tvTips.setText("云服务IP地址或端口号不可为空");
                            return;
                        }

                        String rip0 = tvRIp0.getText().toString();
                        String rip1 = tvRIp1.getText().toString();
                        String rip2 = tvRIp2.getText().toString();
                        String rip3 = tvRIp3.getText().toString();
                        String rport = tvRPort.getText().toString();
                        if(TextUtils.isEmpty(ip0)
                                || TextUtils.isEmpty(ip1)
                                || TextUtils.isEmpty(ip2)
                                || TextUtils.isEmpty(ip3)
                                || TextUtils.isEmpty(rport)){
                            tvTips.setText("资源IP地址或端口号不可为空");
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
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT){
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            Display defaultDisplay = getWindowManager().getDefaultDisplay();
            int width = defaultDisplay.getWidth();
            int height = defaultDisplay.getHeight();
            window.setLayout(width/2, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        window.setWindowAnimations(R.style.mystyle);  //添加动画
    }

    public void setPwd(){
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

        btnConfirm .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(edtPwd.getText())){
                    edtPwd.setError("密码不可为空");
                    return;
                }
                if(edtPwd.getText().length()<6){
                    edtPwd.setError("密码最少输入6位");
                    return;
                }
                if(TextUtils.isEmpty(edtPwd2.getText())){
                    edtPwd2.setError("请再次输入密码");
                    return;
                }
                String pwd = edtPwd.getText().toString();
                final String pwd2 = edtPwd2.getText().toString();
                if(!TextUtils.equals(pwd,pwd2)){
                    edtPwd2.setError("两次输入的密码不一致");
                    return;
                }

                btnCancel.setEnabled(false);
                btnConfirm.setEnabled(false);
                Map<String,String> params = new HashMap<>();
                params.put("deviceNo", HeartBeatClient.getDeviceNo());
                params.put("password",pwd2);
                OkHttpUtils.post().url(ResourceUpdate.UPDATE_PWD).params(params).build().execute(new StringCallback() {
                    @Override
                    public void onError(Call call, final Exception e, int id) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UIUtils.showTitleTip(SystemActivity.this,"修改失败：" + e!=null?e.getMessage():"NULL");
                            }
                        });
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        JSONObject jsonObject =JSONObject.parseObject(response);
                        final Integer status = jsonObject.getInteger("status");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(status == 1){
                                    UIUtils.showTitleTip(SystemActivity.this,"修改成功");
                                    SpUtils.saveStr(SpUtils.MENU_PWD,pwd2);
                                    dialog.dismiss();
                                } else {
                                    UIUtils.showTitleTip(SystemActivity.this,"修改失败");
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

    public void setAngle(View view){
        int anInt = SpUtils.getInt(SpUtils.CAMERA_ANGLE);
        if(anInt == CameraManager.L){
            anInt = CameraManager.P;
        } else if(anInt == CameraManager.P) {
            anInt = CameraManager.L_R;
        } else if(anInt == CameraManager.L_R){
            anInt = CameraManager.P_R;
        } else {
            anInt = CameraManager.L;
        }
        CameraManager.instance().setOrientation(anInt);
        ((Button)view).setText("角度：" + anInt);
        SpUtils.saveInt(SpUtils.CAMERA_ANGLE,anInt);
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
                        showAlert("此操作将清除应用缓存，是否继续？", new DialogInterface.OnClickListener() {
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
        int anInt = SpUtils.getInt(SpUtils.CAMERA_ANGLE);
        btn.setText("角度：" + anInt);

//        setCamOri(dialog);
        checkDataSize();

        final boolean mirror = SpUtils.isMirror();
        cbMirror.setChecked(true);
        cbMirror.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlert("更改摄像头配置需要重启应用才能生效，是否继续？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        SpUtils.setMirror(!mirror);
//                        cbMirror.setChecked(!mirror);
//                        FaceBoxUtil.setIsMirror(!mirror);
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
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT){
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            Display defaultDisplay = getWindowManager().getDefaultDisplay();
            int width = defaultDisplay.getWidth();
            window.setLayout((int) (width/1.5), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        window.setWindowAnimations(R.style.mystyle);  //添加动画
    }

    private void checkDataSize(){
        FileUtils.getDataSize(new FileUtils.OnSizeCallback() {
            @Override
            public void getSize(long size) {
                if(size > 0){
                    size = size / 1024 / 1024;
                }
                tvDataSize.setText(size+"mb");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    //设置网络状态
    private void setNetState(){
        CheckNet checkNet = new CheckNet(this);
        boolean intenetConnected = checkNet.isIntenetConnected();
        if(intenetConnected){//网线连接
            if(checkNet.isEtherneteConncted()){//已连接
                tvNetState.setText("网线连接");
            } else {
                tvNetState.setText("网线连接：无网络");
            }
            return;
        }

        boolean wifiEnabled = checkNet.isWifiEnabled();
        if(!wifiEnabled){
            //代表无网络
            tvNetState.setText("无网络连接");
            return;
        }

        boolean wifiConnected = checkNet.isWifiConnected();
        if(!wifiConnected){
            //代表无网络
            tvNetState.setText("无网络连接");
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("网络类型：WIFI");
        stringBuilder.append("，网络名称："+checkNet.getWifiName());
        stringBuilder.append("，信号："+checkNet.getStrength());
        tvNetState.setText(stringBuilder.toString());
    }

    //设置摄像头信息
    private void setCameraInfo(){
        CheckCamera checkCamera = new CheckCamera();
        String cameraInfo = checkCamera.getCameraInfo();
        tvCameraInfo.setText(cameraInfo);
    }

    private void showAlert(String msg, Dialog.OnClickListener onClickListener){
        showAlert(msg,onClickListener,null);
    }

    private void showAlert(String msg, Dialog.OnClickListener onClickListener, DialogInterface.OnDismissListener onDissmissListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage(msg);
        builder.setPositiveButton("确定",onClickListener);
        if(onDissmissListener != null){
            builder.setOnDismissListener(onDissmissListener);
        }

        AlertDialog alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        alertDialog.show();
        window.setWindowAnimations(R.style.mystyle);  //添加动画
    }


    class CheckCamera{
        public String getCameraInfo(){
            StringBuilder cameraInfo = new StringBuilder();
            int numberOfCameras = android.hardware.Camera.getNumberOfCameras();
            if(numberOfCameras <= 0){
                return "无摄像头";
            }
            for (int i = 0; i < numberOfCameras; i++) {
                android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
                android.hardware.Camera.getCameraInfo(i,info);
                boolean isFront = info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;
                int orientation = info.orientation;
                cameraInfo
                        .append("共"+numberOfCameras+"个：")
                        .append("【编号："+i+"，")
                        .append(isFront ? "前置":"后置")
                        .append("，角度："+orientation)
                        .append("】");
            }

            return cameraInfo.toString();
        }
    }

    class CheckNet {
        private  WifiManager wifiManager;
        private  ConnectivityManager connectManager;
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

        public boolean isEtherneteConncted(){
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

        public String getStrength(){
            String strength = "";
            WifiInfo info = wifiManager.getConnectionInfo();
            int rssi = info.getRssi();
            if(rssi<=0 && rssi >= -50){//信号最好
                strength = "强";
            }else if(rssi< -50 && rssi >= -70){//信号一般
                strength = "一般";
            }else if(rssi > -70){
                strength = "差";
            }else if(rssi <= -200){
                strength = "无网络";
            }
            return strength;
        }
    }

}

