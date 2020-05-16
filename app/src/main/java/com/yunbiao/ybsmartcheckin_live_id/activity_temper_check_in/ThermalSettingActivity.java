package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.lcw.library.imagepicker.ImagePicker;
import com.lcw.library.imagepicker.utils.ImageLoader;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.FlavorType;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.DisplayOrientationEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
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
import java.util.Arrays;
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
        //隐私模式
        initPrivacyMode();
        //初始化清除策略
        initClearPolicy();
        //调整语速
        initVoiceSpeed();
        //隐藏首页LOGO
        initMainLogo();
        //隐藏首页信息
        initMainInfo();
        //显示首页热成像
        initMainThermal();
        //靠近提示
        initCloseTips();
        //首页文字
        initLogoText();
        //首页Logo
        initMainLogoImage();
    }

    private void initMainLogoImage() {
        Button btnRestore = findViewById(R.id.btn_restore_main_logo);
        Button btnSaveMainLogo = findViewById(R.id.btn_save_main_logo);
        ImageView ivMainLogo = findViewById(R.id.iv_main_logo);
        String mainLogoImg = SpUtils.getStr(ThermalConst.Key.MAIN_LOGO_IMG, ThermalConst.Default.MAIN_LOGO_IMG);
        if (TextUtils.isEmpty(mainLogoImg)) {
            ivMainLogo.setImageResource(R.mipmap.yb_logo);
        } else {
            File file = new File(mainLogoImg);
            if (!file.exists()) {
                ivMainLogo.setImageResource(R.mipmap.yb_logo);
            } else {
                ivMainLogo.setImageBitmap(BitmapFactory.decodeFile(mainLogoImg));
            }
        }

        btnSaveMainLogo.setOnClickListener(v -> {
            ImagePicker.getInstance()
                    .setTitle(getResString(R.string.select_img_title))//设置标题
                    .showCamera(true)//设置是否显示拍照按钮
                    .showImage(true)//设置是否展示图片
                    .showVideo(false)//设置是否展示视频
//                    .setMaxCount(1)//设置最大选择图片数目(默认为1，单选)
//                    .setImagePaths(mImageList)//保存上一次选择图片的状态，如果不需要可以忽略
                    .setImageLoader(new ImgLoader())//设置自定义图片加载器
                    .start(ThermalSettingActivity.this, REQEST_SELECT_IMAGES_CODE);//REQEST_SELECT_IMAGES_CODE为Intent调用的
        });
        btnRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpUtils.remove(ThermalConst.Key.MAIN_LOGO_IMG);
                ivMainLogo.setImageResource(R.mipmap.yb_logo);
            }
        });
    }

    private final int REQEST_SELECT_IMAGES_CODE = 12345;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQEST_SELECT_IMAGES_CODE && resultCode == RESULT_OK) {
            List<String> imagePaths = data.getStringArrayListExtra(ImagePicker.EXTRA_SELECT_IMAGES);
            if (1 > imagePaths.size()) {
                return;
            }
            String imgPath = imagePaths.get(0);
            ImageView ivMainLogo = findViewById(R.id.iv_main_logo);
            if (TextUtils.isEmpty(imgPath)) {
                UIUtils.showShort(this, getResString(R.string.select_img_failed));
                ivMainLogo.setImageResource(R.mipmap.yb_logo);
            } else {
                File file = new File(imgPath);
                if (!file.exists()) {
                    UIUtils.showShort(this, getResString(R.string.select_img_failed_not_exists));
                    ivMainLogo.setImageResource(R.mipmap.yb_logo);
                } else {
                    SpUtils.saveStr(ThermalConst.Key.MAIN_LOGO_IMG, imgPath);
                    ivMainLogo.setImageBitmap(BitmapFactory.decodeFile(file.getPath()));
                }
            }
        }
    }

    class ImgLoader implements ImageLoader {

        @Override
        public void loadImage(ImageView imageView, String imagePath) {
            Glide.with(APP.getContext()).load(imagePath).asBitmap().override(50, 50).into(imageView);
        }

        @Override
        public void loadPreImage(ImageView imageView, String imagePath) {
            Glide.with(APP.getContext()).load(imagePath).asBitmap().override(50, 50).into(imageView);
        }

        @Override
        public void clearMemoryCache() {
            //清理缓存
            Glide.get(APP.getContext()).clearMemory();
        }
    }

    private void initLogoText() {
        String mainLogoText = SpUtils.getStr(ThermalConst.Key.MAIN_LOGO_TEXT, ThermalConst.Default.MAIN_LOGO_TEXT);
        EditText edtMainLogoText = findViewById(R.id.edt_main_logo_text);
        edtMainLogoText.setText(mainLogoText);
        edtMainLogoText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String s1 = s.toString().trim();
                if(TextUtils.isEmpty(s1)) {
                    SpUtils.remove(ThermalConst.Key.MAIN_LOGO_TEXT);
                } else {
                    SpUtils.saveStr(ThermalConst.Key.MAIN_LOGO_TEXT, s1);
                }
            }
        });
    }

    private void initCloseTips() {
        String closeTips = SpUtils.getStr(ThermalConst.Key.CLOSE_TIPS, ThermalConst.Default.CLOSE_TIPS);
        EditText edtPleaseClose = findViewById(R.id.edt_please_close_tips);
        if (TextUtils.isEmpty(closeTips)) {
            edtPleaseClose.setHint(getResString(R.string.main_tips_please_close));
        } else {
            edtPleaseClose.setText(closeTips);
        }
        edtPleaseClose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String tips = s.toString().trim();
                if(TextUtils.isEmpty(tips)) {
                    SpUtils.remove(ThermalConst.Key.CLOSE_TIPS);
                } else {
                    SpUtils.saveStr(ThermalConst.Key.CLOSE_TIPS,tips);
                }
            }
        });
    }

    private void initUISetting() {
        String welcomeTips = SpUtils.getStr(SpUtils.WELCOM_TIPS, "");
        EditText edtWelComeTips = findViewById(R.id.edt_welcome_tips);
        if (TextUtils.isEmpty(welcomeTips)) {
            String tips = Constants.FLAVOR_TYPE == FlavorType.YB ? getResString(R.string.setting_default_welcome_tip) : Constants.FLAVOR_TYPE == FlavorType.HT ? getResString(R.string.setting_default_welcome_tip2) : "";
            edtWelComeTips.setHint(tips);
        } else {
            edtWelComeTips.setText(welcomeTips);
        }
        edtWelComeTips.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String inputWelcome = s.toString()/*.trim()*/;
                if(TextUtils.isEmpty(inputWelcome)){
                    SpUtils.remove(SpUtils.WELCOM_TIPS);
                } else {
                    SpUtils.saveStr(SpUtils.WELCOM_TIPS, inputWelcome);
                }
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

    public void jumpTag(View view) {
        final boolean jumpTag = SpUtils.getBoolean(Constants.JUMP_TAG, Constants.DEFAULT_JUMP_TAG);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(APP.getContext().getResources().getString(R.string.setting_switch_function));
        builder.setMessage(APP.getContext().getResources().getString(R.string.setting_switch_tip1));
        builder.setNegativeButton(APP.getContext().getResources().getString(R.string.setting_switch_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(APP.getContext().getResources().getString(R.string.setting_switch_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                SpUtils.saveBoolean(Constants.JUMP_TAG, !jumpTag);

                APP.exit2();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void initMainThermal(){
        boolean showMainThermal = SpUtils.getBoolean(ThermalConst.Key.SHOW_MAIN_THERMAL, ThermalConst.Default.SHOW_MAIN_THERMAL);
        Switch swMainThermal = findViewById(R.id.sw_main_thermal_setting);
        swMainThermal.setChecked(showMainThermal);
        swMainThermal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(ThermalConst.Key.SHOW_MAIN_THERMAL, isChecked);
            }
        });
    }

    private void initMainInfo(){
        boolean showMainInfo = SpUtils.getBoolean(ThermalConst.Key.SHOW_MAIN_INFO, ThermalConst.Default.SHOW_MAIN_INFO);
        Switch swMainInfo = findViewById(R.id.sw_main_info_setting);
        swMainInfo.setChecked(showMainInfo);
        swMainInfo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(ThermalConst.Key.SHOW_MAIN_INFO, isChecked);
            }
        });
    }

    private void initMainLogo(){
        boolean showMainLogo = SpUtils.getBoolean(ThermalConst.Key.SHOW_MAIN_LOGO, ThermalConst.Default.SHOW_MAIN_LOGO);
        Switch swMainLogo = findViewById(R.id.sw_main_logo_setting);
        swMainLogo.setChecked(showMainLogo);
        swMainLogo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(ThermalConst.Key.SHOW_MAIN_LOGO, isChecked);
            }
        });
    }

    private void initVoiceSpeed() {
        final Float voidSpeed = SpUtils.getFloat(ThermalConst.Key.VOICE_SPEED, ThermalConst.Default.VOICE_SPEED);
        Button btnSpeedSub = findViewById(R.id.btn_speed_sub_setting);
        Button btnSpeedPlus = findViewById(R.id.btn_speed_plus_setting);
        EditText edtSpeed = findViewById(R.id.edt_speed_setting);
        edtSpeed.setText(String.valueOf(voidSpeed));
        View.OnClickListener onClickListener = v -> {
            String value = edtSpeed.getText().toString();
            float speed = formatF(Float.parseFloat(value));

            if(v.getId() == R.id.btn_speed_sub_setting){
                speed -= 0.1f;
            } else {
                speed += 0.1f;
            }
            speed = formatF(speed);
            edtSpeed.setText(String.valueOf(speed));
            SpUtils.saveFloat(ThermalConst.Key.VOICE_SPEED,speed);
        };
        btnSpeedPlus.setOnClickListener(onClickListener);
        btnSpeedSub.setOnClickListener(onClickListener);
    }

    private void initClearPolicy() {
        RadioGroup rgClear = findViewById(R.id.rg_clear_policy);
        if (Constants.FLAVOR_TYPE != FlavorType.SOFT_WORK_Z) {
            rgClear.setVisibility(View.GONE);
            return;
        }
        TextView tvClearPolicy = findViewById(R.id.tv_clear_policy);
        String str = tvClearPolicy.getText().toString();

        int date = 0;
        int clearPolicy = SpUtils.getIntOrDef(Constants.Key.CLEAR_POLICY, Constants.Default.CLEAR_POLICY);
        switch (clearPolicy) {
            case 0:
                date = 7;
                rgClear.check(R.id.rb_clear_policy_7);
                break;
            case 1:
                date = 15;
                rgClear.check(R.id.rb_clear_policy_15);
                break;
            case 2:
                date = 30;
                rgClear.check(R.id.rb_clear_policy_30);
                break;
        }
        String format = String.format(str, String.valueOf(date));
        tvClearPolicy.setText(format);
        rgClear.setOnCheckedChangeListener((group, checkedId) -> {
            int i = 0;
            switch (checkedId) {
                case R.id.rb_clear_policy_7:
                    i = 7;
                    SpUtils.saveInt(Constants.Key.CLEAR_POLICY, 0);
                    break;
                case R.id.rb_clear_policy_15:
                    i = 15;
                    SpUtils.saveInt(Constants.Key.CLEAR_POLICY, 1);
                    break;
                case R.id.rb_clear_policy_30:
                    i = 30;
                    SpUtils.saveInt(Constants.Key.CLEAR_POLICY, 2);
                    break;
            }
            String s = String.format(str, String.valueOf(i));
            tvClearPolicy.setText(s);
        });
    }

    private void initPrivacyMode() {
        boolean isPrivacyModeEnabled = SpUtils.getBoolean(Constants.Key.PRIVACY_MODE, Constants.Default.PRIVACY_MODE);
        Switch swPrivacy = findViewById(R.id.sw_privacy_mode);
        swPrivacy.setChecked(isPrivacyModeEnabled);
        swPrivacy.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SpUtils.saveBoolean(Constants.Key.PRIVACY_MODE, isChecked);
        });
    }

    public void powerOnOff(View view) {
        startActivity(new Intent(this, PowerOnOffActivity.class));
    }

    private void initReadCardSetting() {
        boolean readCardEnabled = SpUtils.getBoolean(SpUtils.READ_CARD_ENABLED, Constants.DEFAULT_READ_CARD_ENABLED);
        Switch swReadCard = findViewById(R.id.sw_readcard_setting);
        swReadCard.setChecked(readCardEnabled);
        swReadCard.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(SpUtils.READ_CARD_ENABLED, isChecked));
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
        swPersonFrame.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(ThermalConst.Key.PERSON_FRAME, isChecked));

        //模式==================================================================================
        final TextView tvModelSetting = findViewById(R.id.tv_model_setting);
        final String[] items = ThermalConst.models;

        final int model = SpUtils.getIntOrDef(ThermalConst.Key.MODE, ThermalConst.Default.MODE);

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
                final int currModel = SpUtils.getIntOrDef(ThermalConst.Key.MODE, ThermalConst.Default.MODE);
                AlertDialog.Builder builder = new AlertDialog.Builder(ThermalSettingActivity.this);
                builder.setTitle(getResources().getString(R.string.setting_select_model));
                builder.setSingleChoiceItems(items, currModel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int whichModel) {

                        Log.e(TAG, "onClick: 模式选择：" + whichModel);

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

                        SpUtils.saveInt(ThermalConst.Key.MODE, whichModel);
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
        //正常
        String normalTips = SpUtils.getStr(ThermalConst.Key.NORMAL_BROADCAST, ThermalConst.Default.NORMAL_BROADCAST);
        EditText edtNormalTips = findViewById(R.id.edt_normal_tips_tips);
        if (TextUtils.isEmpty(normalTips)) {
            edtNormalTips.setHint(getResources().getString(R.string.main_temp_normal_tips));
        } else {
            edtNormalTips.setText(normalTips);
        }
        edtNormalTips.addTextChangedListener(new TextWatcherImpl() {
            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString()/*.trim()*/;
                if (TextUtils.isEmpty(input)) {
                    SpUtils.remove(ThermalConst.Key.NORMAL_BROADCAST);
                } else {
                    SpUtils.saveStr(ThermalConst.Key.NORMAL_BROADCAST, input);
                }
            }
        });
        //异常
        String warningTips = SpUtils.getStr(ThermalConst.Key.WARNING_BROADCAST, ThermalConst.Default.WARNING_BROADCAST);
        EditText edtWarningTips = findViewById(R.id.edt_warning_tips_tips);
        if (TextUtils.isEmpty(warningTips)) {
            edtWarningTips.setHint(getResources().getString(R.string.main_temp_warning_tips));
        } else {
            edtWarningTips.setText(warningTips);
        }
        edtWarningTips.addTextChangedListener(new TextWatcherImpl() {
            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString()/*.trim()*/;
                if (TextUtils.isEmpty(input)) {
                    SpUtils.remove(ThermalConst.Key.WARNING_BROADCAST);
                } else {
                    SpUtils.saveStr(ThermalConst.Key.WARNING_BROADCAST, input);
                }
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
                    UIUtils.showTitleTip(ThermalSettingActivity.this, APP.getContext().getResources().getString(R.string.setting_please_set_ip));
                    return;
                }


                if (TextUtils.isEmpty(mResPort)) {
                    UIUtils.showTitleTip(ThermalSettingActivity.this, APP.getContext().getResources().getString(R.string.setting_please_set_res));
                    return;
                }
                int intResPort = Integer.parseInt(mResPort);
                if (intResPort > 65535) {
                    UIUtils.showTitleTip(ThermalSettingActivity.this, APP.getContext().getResources().getString(R.string.setting_res_port_error));
                    return;
                }

                if (TextUtils.isEmpty(mXmppPort)) {
                    UIUtils.showTitleTip(ThermalSettingActivity.this, APP.getContext().getResources().getString(R.string.setting_please_set_xmpp));
                    return;
                }
                int intXmppPort = Integer.parseInt(mXmppPort);
                if (intXmppPort > 65535) {
                    UIUtils.showTitleTip(ThermalSettingActivity.this, APP.getContext().getResources().getString(R.string.setting_xmpp_port_error));
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
                UIUtils.showTitleTip(ThermalSettingActivity.this, APP.getContext().getResources().getString(R.string.setting_save_succ_please_restart));
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

    //初始化人脸弹窗开关
    private void initFaceVipDialogSetting() {
        Switch switchFaceDialog = findViewById(R.id.sw_face_dialog);
        boolean faceDialog = SpUtils.getBoolean(ThermalConst.Key.SHOW_DIALOG, ThermalConst.Default.SHOW_DIALOG);
        switchFaceDialog.setChecked(faceDialog);
        switchFaceDialog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(ThermalConst.Key.SHOW_DIALOG, isChecked);
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

    //初始化相似度阈值设置
    private void initSimilarSetting() {
        final EditText edtSimilar = findViewById(R.id.edt_similar_threshold);
        int similar = SpUtils.getIntOrDef(SpUtils.SIMILAR_THRESHOLD, 80);
        edtSimilar.setText(similar + "");

        findViewById(R.id.btn_set_similar_threshold).setOnClickListener(v -> {
            String similar1 = edtSimilar.getText().toString();
            if (TextUtils.isEmpty(similar1)) {
                edtSimilar.setText(similar1 + "");
                return;
            }
            int sml = Integer.parseInt(similar1);
            SpUtils.saveInt(SpUtils.SIMILAR_THRESHOLD, sml);
            Activity activity = APP.getMainActivity();
            if (activity != null) {
                if (activity instanceof ThermalImage2Activity) {
                    ((ThermalImage2Activity) activity).setFaceViewSimilar();
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

    public void setAngle(final View view) {
        int anInt = SpUtils.getIntOrDef(SpUtils.CAMERA_ANGLE, Constants.DEFAULT_CAMERA_ANGLE);
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