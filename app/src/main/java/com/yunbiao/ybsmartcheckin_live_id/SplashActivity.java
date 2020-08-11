package com.yunbiao.ybsmartcheckin_live_id;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.activity.WelComeActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_certificates.CertificatesConst;
import com.yunbiao.ybsmartcheckin_live_id.activity_safety_check.ThermalSafetyCheckActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_safety_check_double_light.SafetyCheckDoubleLightActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.ConfigLoader;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.TemperModuleType;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.ThermalConst;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.ThermalImage2Activity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.common.power.PowerOffTool;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Exception;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.activity_certificates.CertificatesActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple.MultiThermalActivity;
import com.yunbiao.ybsmartcheckin_live_id.temper_12inch.activity.Main12InchActivity;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.activity.Main5InchActivity;
import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SdCardUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.ThreadUitls;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import timber.log.Timber;

public class SplashActivity extends BaseActivity {
    private static final String TAG = "SplashActivity";

    public static String[] PERMISSONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE
            , android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            , android.Manifest.permission.ACCESS_FINE_LOCATION
            , android.Manifest.permission.ACCESS_COARSE_LOCATION
            , android.Manifest.permission.READ_PHONE_STATE
            , android.Manifest.permission.CAMERA
            /*,Manifest.permission.SYSTEM_ALERT_WINDOW*/};
    private YBPermission ybPermission;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_splash;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initView() {
        super.initView();
    }

    @Override
    protected void initData() {
        long folderSize = SdCardUtils.getFolderSize(new File(Constants.LOCAL_ROOT_PATH));
        Timber.d("当前应用占用空间大小：" + (folderSize / 1024 / 1024) + "mb");

        SdCardUtils.Capacity capacity = SdCardUtils.getUsedCapacity();
        double remainingSpace = capacity.getAll_mb() - capacity.getUsed_mb();
        String availSpace = String.format(getResString(R.string.alvailable_space),capacity.formatD(remainingSpace),"mb");
        if(remainingSpace <= 30){
            Toast.makeText(this, getResString(R.string.clean_storage_must), Toast.LENGTH_SHORT).show();
            finish();
        } else if (remainingSpace <= 160) {
            showAlert(getResString(R.string.clean_storage_must) + "\n"+ availSpace,false,null, APP::exit,0);
        } else if (capacity.getAll_mb() - capacity.getUsed_mb() < (capacity.getAll_mb() / 10)) {
            showAlert(getResString(R.string.clean_storage_please) + "\n"+ availSpace,true, APP::exit, this::checkPermission,45000);
        } else {
            checkPermission();
        }
    }

    private void checkPermission(){
        GifImageView gifImageView = findViewById(R.id.giv);
        setOpenGif(gifImageView);

        ybPermission = new YBPermission(permissionListener);
        ybPermission.checkPermission(this, PERMISSONS);
    }

    private void setOpenGif(GifImageView gifImageView) {
        File splashDir = new File(Constants.SPLASH_DIR_PATH);
        if (!splashDir.exists() || !splashDir.isDirectory()) {
            splashDir.mkdirs();
        }

        GifDrawable gifDrawable;

        File[] files = splashDir.listFiles(pathname -> pathname.isFile());
        if (files != null && files.length > 0) {
            Arrays.sort(files, (o1, o2) -> o1.isDirectory() && o2.isFile() ? -1 : o1.isFile() && o2.isDirectory() ? 1 : o1.getName().compareTo(o2.getName()));
            File file = files[0];
            try {
                gifDrawable = new GifDrawable(file);
                gifImageView.setImageDrawable(gifDrawable);
                gifDrawable.setLoopCount(0);
                gifDrawable.setSpeed(3.0f);
            } catch (IOException e) {
                gifImageView.setImageURI(Uri.fromFile(file));
            }
        } else {
            try {
                gifDrawable = new GifDrawable(getResources(), R.mipmap.splash);
                gifImageView.setImageDrawable(gifDrawable);
                gifDrawable.setLoopCount(0);
                gifDrawable.setSpeed(3.0f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String msg, boolean showPositive, Runnable negativeRunnable, Runnable positiveRunnable,long delayTime) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(getResString(R.string.alert_title_warning))
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(getResString(R.string.base_ensure), (dialog, which) -> {
                    dialog.dismiss();
                    if (positiveRunnable != null) {
                        positiveRunnable.run();
                    }
                });
        if (showPositive) {
            builder.setNegativeButton(getResString(R.string.base_cancel), (dialog, which) -> {
                dialog.dismiss();
                if (negativeRunnable != null) {
                    negativeRunnable.run();
                }
            });
        }
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        if(delayTime > 0){
            Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            CharSequence text = btnPositive.getText();
            CountDownTimer countDownTimer = new CountDownTimer(delayTime,1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    int second = millisUntilFinished < 1000 ? 0 : (int) (millisUntilFinished / 1000);
                    btnPositive.setText(text + "(" + second + ")");
                }

                @Override
                public void onFinish() {
                    btnPositive.performClick();
                }
            };
            countDownTimer.start();
        }
    }

    private YBPermission.PermissionListener permissionListener = new YBPermission.PermissionListener() {
        @Override
        public void onPermissionFailed(String[] objects) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(objects, YBPermission.PERMISSION_REQUEST_CODE);
            }
        }

        @Override
        public void onFinish(boolean isComplete) {
            if (!isComplete) {
                UIUtils.showShort(SplashActivity.this, getString(R.string.splash_request_permission_failed));
                return;
            }

            SpUtils.init();
            DaoManager.get().initDb();

            switch (Constants.FLAVOR_TYPE) {
                case FlavorType.XENON:
                    setIp("api-eu.feverdefence.com", "34.247.168.20", "5222", "", "");
                    break;
                /*case FlavorType.YB:
                    setIp("192.168.255.115","192.168.255.115","5222","80","ybface");
                    break;*/
                default:
                    checkServiceIp();
                    break;
            }

            Constants.checkSetIp();
            Constants.initStorage();
            OutputLog.getInstance().initFile(Constants.LOCAL_ROOT_PATH);
            ThreadUitls.runInThread(() -> PowerOffTool.getPowerOffTool().machineStart());

            if (Constants.DEVICE_TYPE == Constants.DeviceType.TEMPERATURE_CHECK_IN
                    || Constants.DEVICE_TYPE == Constants.DeviceType.HT_TEMPERATURE_CHECK_IN) {
                ConfigLoader.load();
            }

            uploadException(nextRunnable);
        }
    };

    private Runnable nextRunnable = () -> {
        UIUtils.dismissNetLoading();
        APP.bindProtectService();
        new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Timber.d("开始激活");
            FaceSDKActive.ActiveCallback activeCallback = (result, message) -> {
                Timber.d("激活结果：" + result + ", " + message);
                if (!result) {
                    runOnUiThread(() -> UIUtils.showLong(SplashActivity.this, getResources().getString(R.string.splash_active_failed) + "(" + message + ")"));
                }
                jump();
                finish();
            };

            if(Constants.FLAVOR_TYPE == FlavorType.XENON){
                Timber.d("爱尔兰版本，本地激活");
                FaceSDKActive.activeLocal(FaceSDKActive.YB_APPID,FaceSDKActive.YB_SDKKEY,activeCallback);
            } else {
                Timber.d("其他版本，网络激活");
                boolean canGo = Constants.DEVICE_TYPE != Constants.DeviceType.TEMPER_SAFETY_CHECK
                        && Constants.DEVICE_TYPE != Constants.DeviceType.HT_TEMPER_SAFETY_CHECK
                        && Constants.DEVICE_TYPE != Constants.DeviceType.TEMPERATURE_MEASUREMENT_5_INCH
                        && Constants.DEVICE_TYPE != Constants.DeviceType.TEMPERATURE_MEASUREMENT_12_INCH;
                FaceSDKActive.active(canGo,activeCallback);
            }
        }).start();
    };

    private void jump() {
        if (Constants.DEVICE_TYPE == Constants.DeviceType.CHECK_IN) {
            Constants.Default.CAMERA_ANGLE = 0;//横屏
            Constants.Default.IS_H_MIRROR = true;
        } else {
            String broadTypeStr = CommonUtils.getBroadType2();
            switch (broadTypeStr) {
                case "SMT":
                    Constants.Default.CAMERA_ANGLE = 270;
                    Constants.Default.IS_H_MIRROR = false;
                    Constants.Default.IS_V_MIRROR = false;
                    CertificatesConst.Default.MODE = CertificatesConst.Mode.CERTIFICATES_THERMAL_16_4;
                    ThermalConst.Default.TEMPER_MODULE = TemperModuleType.HM_16_4;
                    break;
                case "LXR":
                    Constants.Default.CAMERA_ANGLE = 0;//横屏
                    Constants.Default.IS_H_MIRROR = true;
                    Constants.Default.IS_V_MIRROR = false;
                    CertificatesConst.Default.MODE = CertificatesConst.Mode.CERTIFICATES_THERMAL;
                    ThermalConst.Default.TEMPER_MODULE = TemperModuleType.HM_32_32;
                    break;
                case "HARRIS":
                default:
                    Constants.Default.CAMERA_ANGLE = 90;
                    Constants.Default.IS_H_MIRROR = false;
                    Constants.Default.IS_V_MIRROR = false;
                    if(Constants.DEVICE_TYPE == Constants.DeviceType.HT_TEMPERATURE_CERTIFICATES){
                        Constants.Default.PICTURE_ROTATION = 270;
                        Constants.Default.CAMERA_ANGLE = 90;
                        Constants.Default.IS_H_MIRROR = true;
                        Constants.Default.IS_V_MIRROR = true;
                    }
                    CertificatesConst.Default.MODE = CertificatesConst.Mode.CERTIFICATES_THERMAL_16_4;
                    ThermalConst.Default.TEMPER_MODULE = TemperModuleType.MLX_16_4;
                    break;
            }
        }
        switch (Constants.FLAVOR_TYPE) {
            case FlavorType.HT:
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.logo_icon_horizontal;
                ThermalConst.Default.MAIN_LOGO_TEXT = "";
                break;
            case FlavorType.SK:
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.icon_logo3;
                ThermalConst.Default.MAIN_LOGO_TEXT = "";
                break;
            case FlavorType.OSIMLE:
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.osimle_logo;
                ThermalConst.Default.MAIN_LOGO_TEXT = "";
                break;
            case FlavorType.SOFT_WORK_Z:
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.softworkz_logo;
                ThermalConst.Default.MAIN_LOGO_TEXT = "";
                break;
            case FlavorType.SCAN_TEMP:
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.scan_temp;
                ThermalConst.Default.MAIN_LOGO_TEXT = "";
                break;
            case FlavorType.PING_TECH:
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.pingtech_logo;
                ThermalConst.Default.MAIN_LOGO_TEXT = "";
                break;
            case FlavorType.XENON:
                Constants.DEFAULT_SCREE_BG = R.mipmap.xenon_screen_saver;
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.xenon_logo;
                ThermalConst.Default.MAIN_LOGO_TEXT = "";
                ThermalConst.Default.WELCOME_TIP_CONTENT = "Welcome to Xenon Fever Defence";
                ThermalConst.Default.SHOW_MAIN_LOGO = true;
                Constants.Default.POSTER_ENABLED = true;
                ThermalConst.Default.SHOW_MAIN_INFO = true;
                Constants.Default.QRCODE_ENABLED = false;
                Constants.Default.IS_H_MIRROR = false;
                Constants.Default.IS_V_MIRROR = false;
                break;
            case FlavorType.TURKEY:
                ThermalConst.Default.LOW_TEMP = false;
                ThermalConst.Default.HIGH_TEMPER_MODE = false;
                break;
            case FlavorType.TOUCHLESS_ENTRY:
                ThermalConst.Default.THERMAL_F_ENABLED = true;
                ThermalConst.Default.WELCOME_TIP_CONTENT = "Welcome to Touchless Entry Non-Contact Temperature Scanning Terminal";
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.yb_logo;
                ThermalConst.Default.MAIN_LOGO_TEXT = "Touchless Entry";
                Constants.Default.IS_H_MIRROR = true;
                Constants.Default.IS_V_MIRROR = true;
                Constants.Default.PICTURE_ROTATION = 270;
                break;
            default:
                //朱培元客户
//                Constants.Default.LIVENESS_ENABLED = true;
//                Constants.Default.SHOW_LIVENESS_SETTING = false;
                //Melody客户
//                ThermalConst.Default.NORMAL_BROADCAST_ENABLED = false;
//                ThermalConst.Default.WARNING_BROAD_ENABLED = false;
                Constants.Default.IS_H_MIRROR = true;
                Constants.Default.IS_V_MIRROR = true;
                Constants.Default.PICTURE_ROTATION = 270;
                //普通
                ThermalConst.Default.DEFAULT_LOGO_ID = R.mipmap.yb_logo;
                ThermalConst.Default.MAIN_LOGO_TEXT = "YBFACE";
                break;
        }

        int lastModel = SpUtils.getIntOrDef("thermalModelSetting", -1);
        if (lastModel != -1) {
            Timber.d("jump: 检测到旧模式：" + lastModel);
            NewModuleType newModuleType = oldModelToModuleType(lastModel);
            Timber.d("jump: 转换为新模式：" + newModuleType.toString());
            SpUtils.saveInt(ThermalConst.Key.TEMPER_MODULE, newModuleType.module);
            SpUtils.saveBoolean(ThermalConst.Key.FACE_ENABLED, newModuleType.faceEnabled);
            SpUtils.saveBoolean(ThermalConst.Key.TEMPER_ENABLED, newModuleType.temperEnabled);
            SpUtils.remove("thermalModelSetting");
        }

        switch (Constants.DEVICE_TYPE) {
            case Constants.DeviceType.CHECK_IN:
                startActivity(new Intent(this, WelComeActivity.class));
                break;
            case Constants.DeviceType.TEMPERATURE_CHECK_IN:
            case Constants.DeviceType.HT_TEMPERATURE_CHECK_IN:
            case Constants.DeviceType.TEMPERATURE_CHECK_IN_215_INCH:
                if (SpUtils.getBoolean(Constants.Key.JUMP_TAG, Constants.Default.JUMP_TAG)) {
                    startActivity(new Intent(SplashActivity.this, CertificatesActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, ThermalImage2Activity.class));
                }
                break;
            case Constants.DeviceType.TEMPERATURE_CERTIFICATES:
            case Constants.DeviceType.HT_TEMPERATURE_CERTIFICATES:
                //判断，如果不跳转则默认进入人证
                if (SpUtils.getBoolean(Constants.Key.JUMP_TAG, Constants.Default.JUMP_TAG)) {
                    startActivity(new Intent(SplashActivity.this, ThermalImage2Activity.class));
                } else {
                    startActivity(new Intent(this, CertificatesActivity.class));
                }
                break;
            case Constants.DeviceType.MULTIPLE_THERMAL:
            case Constants.DeviceType.HT_MULTIPLE_THERMAL:
                Constants.Default.CAMERA_ANGLE = 0;
                startActivity(new Intent(this, MultiThermalActivity.class));
                break;
            case Constants.DeviceType.TEMPER_SAFETY_CHECK:
            case Constants.DeviceType.HT_TEMPER_SAFETY_CHECK:
                startActivity(new Intent(this, ThermalSafetyCheckActivity.class));
                break;
            case Constants.DeviceType.SAFETY_CHECK_DOUBLE_LIGHT:
            case Constants.DeviceType.HT_SAFETY_CHECK_DOUBLE_LIGHT:
                Constants.HORIZONTAL_OFFSET = 4;
                Constants.Default.IS_H_MIRROR = false;
                Constants.CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_FRONT;
                startActivity(new Intent(this, SafetyCheckDoubleLightActivity.class));
                break;
            case Constants.DeviceType.TEMPERATURE_MEASUREMENT_5_INCH:
                Constants.Default.CAMERA_ANGLE = 0;
                startActivity(new Intent(this, Main5InchActivity.class));
                break;
            case Constants.DeviceType.TEMPERATURE_MEASUREMENT_12_INCH:
                startActivity(new Intent(this, Main12InchActivity.class));
                break;
        }
    }

    private void checkServiceIp() {
        int intOrDef = SpUtils.getIntOrDef(Constants.Key.SERVER_MODEL, Constants.Default.SERVER_MODEL);
        if (intOrDef == Constants.serverModel.JU) {
            String serIp = SpUtils.getStr(Constants.Key.JU_SERVICE_IP_CACHE, "");
            if (TextUtils.isEmpty(serIp)) {
                String xmppIp = SpUtils.getStr(Constants.Key.JU_XMPP_IP_CACHE, "");
                SpUtils.saveStr(Constants.Key.JU_SERVICE_IP_CACHE, xmppIp);
            }
        }
    }

    private void setIp(String sIp, String cIp, String xPort, String rPort, String pName) {
        int intOrDef = SpUtils.getIntOrDef(Constants.Key.SERVER_MODEL, -99);
        if (intOrDef == -99)
            SpUtils.saveInt(Constants.Key.SERVER_MODEL, Constants.serverModel.JU);

        String serIp = SpUtils.getStr(Constants.Key.JU_SERVICE_IP_CACHE, "");
        Timber.d("setIp: 设置的服务地址：" + serIp);
        if (TextUtils.isEmpty(serIp))
            SpUtils.saveStr(Constants.Key.JU_SERVICE_IP_CACHE, sIp);
        String resPort = SpUtils.getStr(Constants.Key.JU_RESOURCE_PORT_CACHE, "");
        if (TextUtils.isEmpty(resPort))
            SpUtils.saveStr(Constants.Key.JU_RESOURCE_PORT_CACHE, rPort);
        String projectName = SpUtils.getStr(Constants.Key.JU_PROJECT_NAME_SUFFIX, "");
        if (TextUtils.isEmpty(projectName))
            SpUtils.saveStr(Constants.Key.JU_PROJECT_NAME_SUFFIX, pName);

        String xmppIp = SpUtils.getStr(Constants.Key.JU_XMPP_IP_CACHE, "");
        if (TextUtils.isEmpty(xmppIp))
            SpUtils.saveStr(Constants.Key.JU_XMPP_IP_CACHE, cIp);
        String xmppPort = SpUtils.getStr(Constants.Key.JU_XMPP_PORT_CACHE, "");
        if (TextUtils.isEmpty(xmppPort))
            SpUtils.saveStr(Constants.Key.JU_XMPP_PORT_CACHE, xPort);

    }

    private NewModuleType oldModelToModuleType(int model) {
        NewModuleType newModuleType = new NewModuleType();
        switch (model) {
            case 0:
                newModuleType.module = -1;
                newModuleType.faceEnabled = true;
                newModuleType.temperEnabled = false;
                break;
            case 1:
                newModuleType.module = -1;
                newModuleType.faceEnabled = false;
                newModuleType.temperEnabled = true;
                break;
            case 2:
                newModuleType.module = -1;
                newModuleType.faceEnabled = true;
                newModuleType.temperEnabled = true;
                break;
            case 3:
                newModuleType.module = TemperModuleType.HM_32_32;
                newModuleType.faceEnabled = false;
                newModuleType.temperEnabled = true;
                break;
            case 4:
                newModuleType.module = TemperModuleType.HM_32_32;
                newModuleType.faceEnabled = true;
                newModuleType.temperEnabled = true;
                break;
            case 5:
                newModuleType.module = TemperModuleType.HM_16_4;
                newModuleType.faceEnabled = false;
                newModuleType.temperEnabled = true;
                break;
            case 6:
                newModuleType.module = TemperModuleType.HM_16_4;
                newModuleType.faceEnabled = true;
                newModuleType.temperEnabled = true;
                break;
            case 7:
                newModuleType.module = TemperModuleType.MLX_16_4;
                newModuleType.faceEnabled = false;
                newModuleType.temperEnabled = true;
                break;
            case 8:
                newModuleType.module = TemperModuleType.MLX_16_4;
                newModuleType.faceEnabled = true;
                newModuleType.temperEnabled = true;
                break;
            case 9:
                newModuleType.module = TemperModuleType.SMT_32_32;
                newModuleType.faceEnabled = false;
                newModuleType.temperEnabled = true;
                break;
            case 10:
                newModuleType.module = TemperModuleType.SMT_32_32;
                newModuleType.faceEnabled = true;
                newModuleType.temperEnabled = true;
                break;
        }

        if (newModuleType.module == -1) {
            String broadTypeStr = CommonUtils.getBroadType2();
            if (TextUtils.equals("SMT", broadTypeStr)) {
                newModuleType.module = TemperModuleType.HM_16_4;
            } else if (TextUtils.equals("LXR", broadTypeStr)) {
                newModuleType.module = TemperModuleType.HM_32_32;
            } else if (TextUtils.equals("HARRIS", broadTypeStr)) {
                newModuleType.module = TemperModuleType.MLX_16_4;
            }
        }
        return newModuleType;
    }

    class NewModuleType {
        int module;
        boolean faceEnabled;
        boolean temperEnabled;

        @Override
        public String toString() {
            return "NewModuleType{" +
                    "module=" + module +
                    ", faceEnabled=" + faceEnabled +
                    ", temperEnabled=" + temperEnabled +
                    '}';
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ybPermission != null) {
            ybPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //上传错误日志
    private void uploadException(final Runnable runnable) {
        UIUtils.showNetLoading(this);
        new Thread(() -> {
            final List<Exception> exceptions = DaoManager.get().queryAll(Exception.class);
            if (exceptions == null || exceptions.size() <= 0) {
                if (runnable != null) {
                    runOnUiThread(runnable);
                }
                return;
            }
            Timber.d("run: 异常条数：" + exceptions.size());
            //地址
            String url = ResourceUpdate.DEVICE_EXCEPTION_UPLOAD;
            //版本号
            String versionName = "x.x.x";
            int versionCode = -1;
            try {
                PackageManager mPackageManager = getPackageManager();
                PackageInfo packageInfo = mPackageManager.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
                versionName = packageInfo.versionName;
                versionCode = packageInfo.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            //设备号
            String str = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
            if (TextUtils.isEmpty(str)) {
                str = "";
            }
            //转jsonString
            String crashArray = new Gson().toJson(exceptions);
            //参数
            Map<String, String> params = new HashMap<>();
            params.put("deviceId", HeartBeatClient.getDeviceNo() + "");
            params.put("deviceType", Constants.DEVICE_TYPE + "");
            params.put("versionName", versionName + "");
            params.put("versionCode", versionCode + "");
            params.put("cpuAbi", "");
            params.put("boardType", CommonUtils.saveBroadInfo() + "");
            params.put("deviceNumber", str + "");
            params.put("crasharray", crashArray + "");

            Timber.d("异常上传：" + url);
            Timber.d("参数：" + params.toString());

            OkHttpUtils.post()
                    .url(ResourceUpdate.DEVICE_EXCEPTION_UPLOAD)
                    .params(params)
                    .build()
                    .readTimeOut(5000)
                    .writeTimeOut(5000)
                    .connTimeOut(5000)
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, java.lang.Exception e, int id) {
                            Timber.d("onError: 上传失败：" + (e == null ? "NULL" : e.getMessage()));
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Timber.d("onResponse: 上传结果：" + response);
                            for (Exception exception : exceptions) {
                                DaoManager.get().delete(exception);
                            }
                        }

                        @Override
                        public void onAfter(int id) {
                            if (runnable != null) {
                                runnable.run();
                            }
                        }
                    });
        }).start();
    }

}
