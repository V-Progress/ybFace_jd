package com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.bean.BlackBody;
import com.intelligence.hardware.temperature.bean.FaceIndexInfo;
import com.intelligence.hardware.temperature.callback.HotImageK6080CallBack;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.PowerOnOffActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.common.UpdateVersionControl;
import com.yunbiao.ybsmartcheckin_live_id.utils.L;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.logutils.Utils;

import java.util.ArrayList;

import butterknife.BindView;

public class MultiThermalSettingActivity extends BaseActivity {
    @BindView(R.id.tv_version_info_multi_thermal_system)
    TextView tvVersionInfo;
    @BindView(R.id.tv_version_name_multi_thermal_system)
    TextView tvVersionName;
    @BindView(R.id.fl_version_multi_thermal_system)
    View flVersionLoading;
    @BindView(R.id.sw_privacy_mode)
    Switch swPrivacyMode;

    @BindView(R.id.sv_multi_setting)
    ScrollView svMultiSetting;

    private ImageView ivHotImage;

    private boolean is1280800 = false;
    private View rlCorrectArea;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_multi_thermal_setting;
    }

    @Override
    protected int getLandscapeLayout() {
        if (Utils.getWinWidth(this) == 1280 && Utils.getWinHight(this) == 800) {
            is1280800 = true;
            return R.layout.activity_multi_thermal_setting_1280800;
        }
        return R.layout.activity_multi_thermal_setting;
    }

    @Override
    protected void initView() {
        super.initView();

        initFaceRectMirrorSetting();

        setLowTemp();

        setWarningTemper();

        setHotImageMirror();

        setThermalFaceFrame();

        setBlackBodyFrame();

        initMultiTrack();

        initBlackBodyPreValue();

        initBlackEnable();

        initSetIp();

        initFEnable();
    }

    private void initPrivacy(){
        boolean aBoolean = SpUtils.getBoolean(Constants.Key.PRIVACY_MODE, Constants.Default.PRIVACY_MODE);
        swPrivacyMode.setChecked(aBoolean);
        swPrivacyMode.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(Constants.Key.PRIVACY_MODE,isChecked));
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

    public void autoCorrect(View view){
        boolean lowTemp = SpUtils.getBoolean(MultiThermalConst.Key.LOW_TEMP,MultiThermalConst.Default.LOW_TEMP);
        boolean blackBodyEnable = SpUtils.getBoolean(MultiThermalConst.Key.BLACK_BODY_ENABLE, MultiThermalConst.Default.BLACK_BODY_ENABLE);
        if(blackBodyEnable || lowTemp){
            UIUtils.showShort(this,"为了确保数据准确\n请关闭[黑体矫正]和[低温模式]后\n再进行校准");
            return;
        }
        startActivity(new Intent(this,AutoCorrectionActivity.class));
    }

    public void upgrade(View view){
        UpdateVersionControl.getInstance().checkUpdate(this);
    }

    private void initBlackEnable(){
        final View viewBlackBodyCorrection = findViewById(R.id.tv_black_body_correction_area);
        final boolean blackBodyEnable = SpUtils.getBoolean(MultiThermalConst.Key.BLACK_BODY_ENABLE,MultiThermalConst.Default.BLACK_BODY_ENABLE);
        if(blackBodyEnable){
            viewBlackBodyCorrection.setVisibility(View.VISIBLE);
        } else {
            viewBlackBodyCorrection.setVisibility(View.GONE);
        }
        Switch swBlackBodyEnable = findViewById(R.id.sw_black_body_enable_setting);
        swBlackBodyEnable.setChecked(blackBodyEnable);
        swBlackBodyEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(MultiThermalConst.Key.BLACK_BODY_ENABLE,isChecked);
                if(isChecked){
                    viewBlackBodyCorrection.setVisibility(View.VISIBLE);
                } else {
                    viewBlackBodyCorrection.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initFEnable() {
        boolean fEnabled = SpUtils.getBoolean(MultiThermalConst.Key.THERMAL_F_ENABLED, MultiThermalConst.Default.THERMAL_F_ENABLED);
        Switch swFEnabled = findViewById(R.id.sw_f_enabled_setting);
        swFEnabled.setChecked(fEnabled);
        swFEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(MultiThermalConst.Key.THERMAL_F_ENABLED, isChecked);
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
                UIUtils.showShort(MultiThermalSettingActivity.this, APP.getContext().getResources().getString(R.string.setting_save_success));
            }
        });
    }

    private void initMultiTrack() {
        boolean multiTrack = SpUtils.getBoolean(MultiThermalConst.Key.MULTI_TRACK, MultiThermalConst.Default.MULTI_TRACK);
        Switch swMultiTrack = findViewById(R.id.sw_multi_track_setting);
        swMultiTrack.setChecked(multiTrack);
        swMultiTrack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(MultiThermalConst.Key.MULTI_TRACK, isChecked);
            }
        });
    }

    private void setBlackBodyFrame() {
        Switch swBlackBody = findViewById(R.id.sw_black_body_frame_setting);
        boolean blackBodyFrame = SpUtils.getBoolean(MultiThermalConst.Key.BLACK_BODY_FRAME, MultiThermalConst.Default.BLACK_BODY_FRAME);
        swBlackBody.setChecked(blackBodyFrame);
        swBlackBody.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(MultiThermalConst.Key.BLACK_BODY_FRAME, isChecked);
            }
        });
    }

    private void setThermalFaceFrame() {
        Switch swFaceFrame = findViewById(R.id.sw_thermal_face_frame_setting);
        boolean isThermalFaceFrame = SpUtils.getBoolean(MultiThermalConst.Key.THERMAL_FACE_FRAME, MultiThermalConst.Default.THERMAL_FACE_FRAME);
        swFaceFrame.setChecked(isThermalFaceFrame);
        swFaceFrame.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(MultiThermalConst.Key.THERMAL_FACE_FRAME, isChecked);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        setBodyCorrect();
    }

    private void setBodyCorrect() {
        Button btnSub = findViewById(R.id.btn_body_correct_sub_setting);
        Button btnAdd = findViewById(R.id.btn_body_correct_add_setting);
        final EditText edtCorrect = findViewById(R.id.edt_body_correct__setting);
        float mBodyCorrect = SpUtils.getFloat(MultiThermalConst.Key.CORRECT_VALUE, MultiThermalConst.Default.BODY_CORRECT_TEMPER);
        edtCorrect.setText(mBodyCorrect + "");
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = edtCorrect.getText().toString();
                float v1 = formatF(Float.parseFloat(value));
                if (v.getId() == R.id.btn_body_correct_sub_setting) {
                    v1 -= 0.1f;
                } else {
                    v1 += 0.1f;
                }
                v1 = formatF(v1);
                edtCorrect.setText(v1 + "");
                SpUtils.saveFloat(MultiThermalConst.Key.CORRECT_VALUE, v1);
            }
        };
        btnSub.setOnClickListener(onClickListener);
        btnAdd.setOnClickListener(onClickListener);
    }

    private void setLowTemp() {
        Switch swLowTemp = findViewById(R.id.sw_low_temp_model_setting);
        boolean lowTemp = SpUtils.getBoolean(MultiThermalConst.Key.LOW_TEMP, MultiThermalConst.Default.LOW_TEMP);
        swLowTemp.setChecked(lowTemp);
        swLowTemp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(MultiThermalConst.Key.LOW_TEMP, isChecked);
            }
        });
    }

    private void setWarningTemper() {
        Button btnSub = findViewById(R.id.btn_temp_warning_threshold_sub_setting);
        final EditText edtTemper = findViewById(R.id.edt_temp_warning_threshold_setting);
        Button btnAdd = findViewById(R.id.btn_temp_warning_threshold_add_setting);

        float warningValue = SpUtils.getFloat(MultiThermalConst.Key.WARNING_TEMP, MultiThermalConst.Default.WARNING_TEMP);
        edtTemper.setText(warningValue + "");
        View.OnClickListener warnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = edtTemper.getText().toString();
                float v1 = formatF(Float.parseFloat(value));
                if (v.getId() == R.id.btn_temp_warning_threshold_sub_setting) {
                    v1 -= 0.1;
                } else {
                    v1 += 0.1;
                }
                v1 = formatF(v1);
                edtTemper.setText(v1 + "");
                SpUtils.saveFloat(MultiThermalConst.Key.WARNING_TEMP, v1);
            }
        };
        btnSub.setOnClickListener(warnClickListener);
        btnAdd.setOnClickListener(warnClickListener);
    }


    //初始化人脸框镜像设置
    private void initFaceRectMirrorSetting() {
        CheckBox cbMirror = findViewById(R.id.cb_mirror);
        //人脸框镜像
        final boolean mirror = SpUtils.getBoolean(Constants.Key.IS_H_MIRROR,Constants.Default.IS_H_MIRROR);
        cbMirror.setChecked(mirror);
        cbMirror.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(Constants.Key.IS_H_MIRROR, isChecked);
            }
        });
    }

    private void setHotImageMirror() {
        Switch swThermalImageMirror = findViewById(R.id.sw_thermal_imag_mirror_setting);
        boolean mirror = SpUtils.getBoolean(MultiThermalConst.Key.THERMAL_MIRROR, MultiThermalConst.Default.THERMAL_MIRROR);
        swThermalImageMirror.setChecked(mirror);
        swThermalImageMirror.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(MultiThermalConst.Key.THERMAL_MIRROR, isChecked);
            }
        });
    }

    public void powerOnOff(View view) {
        startActivity(new Intent(this, PowerOnOffActivity.class));
    }

    public void setBlackBody(View view) {
        ivHotImage = findViewById(R.id.iv_hot_image);
        rlCorrectArea = findViewById(R.id.rl_correct_area);
        if (rlCorrectArea.isShown()) {
            if (is1280800) {
                svMultiSetting.setVisibility(View.VISIBLE);
            }
            rlCorrectArea.setVisibility(View.GONE);
            handler.removeCallbacks(runnable);
            TemperatureModule.getIns().closeHotImageK6080();
        } else {
            if (is1280800) {
                svMultiSetting.setVisibility(View.GONE);
            }
            rlCorrectArea.setVisibility(View.VISIBLE);
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, 1000);

            initCorrectArea();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }

    public void saveRect(View view) {
        if (mSaveRect.width() == 0 || mSaveRect.height() == 0) {
            UIUtils.showShort(this, getResources().getString(R.string.setting_save_failed_multi_thermal));
            return;
        }

        int left = mSaveRect.left;
        int top = mSaveRect.top;
        int right = mSaveRect.right;
        int bottom = mSaveRect.bottom;
        SpUtils.saveInt(MultiThermalConst.Key.CORRECT_AREA_LEFT, left);
        SpUtils.saveInt(MultiThermalConst.Key.CORRECT_AREA_TOP, top);
        SpUtils.saveInt(MultiThermalConst.Key.CORRECT_AREA_RIGHT, right);
        SpUtils.saveInt(MultiThermalConst.Key.CORRECT_AREA_BOTTOM, bottom);

        L.e("MultiThermalSettingActivity", "saveRect:保存的数值：" + left + " --- " + top + " --- " + right + " --- " + bottom);

        UIUtils.showShort(this, getResources().getString(R.string.setting_save_success_multi_thermal));

        if (is1280800 && rlCorrectArea != null) {
            svMultiSetting.setVisibility(View.VISIBLE);
            rlCorrectArea.setVisibility(View.GONE);
        }
    }

    private Rect getCacheRect() {
        int left = SpUtils.getIntOrDef(MultiThermalConst.Key.CORRECT_AREA_LEFT, MultiThermalConst.Default.CORRECT_AREA_LEFT);
        int top = SpUtils.getIntOrDef(MultiThermalConst.Key.CORRECT_AREA_TOP, MultiThermalConst.Default.CORRECT_AREA_TOP);
        int right = SpUtils.getIntOrDef(MultiThermalConst.Key.CORRECT_AREA_RIGHT, MultiThermalConst.Default.CORRECT_AREA_RIGHT);
        int bottom = SpUtils.getIntOrDef(MultiThermalConst.Key.CORRECT_AREA_BOTTOM, MultiThermalConst.Default.CORRECT_AREA_BOTTOM);
        return new Rect(left, top, right, bottom);
    }

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            boolean mThermalMirror = SpUtils.getBoolean(MultiThermalConst.Key.THERMAL_MIRROR, MultiThermalConst.Default.THERMAL_MIRROR);

            Rect cacheRect = getCacheRect();

            //开启热成像6080模块
            //isMirror:热成像画面是否左右镜像, isCold:是否为低温补偿模式, hotImageK6080CallBack:数据回调
            TemperatureModule.getIns().startHotImageK6080(mThermalMirror, false, new HotImageK6080CallBack() {
                @Override
                public void newestHotImageData(final Bitmap bitmap, float v, float v1, float v2) {
                    if (ivHotImage != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ivHotImage.setImageBitmap(bitmap);
                            }
                        });
                    }
                }

                @Override
                public void newestHotImageData(Bitmap bitmap, ArrayList<FaceIndexInfo> arrayList) {

                }
            });
            TemperatureModule.getIns().setmCorrectionValue(0.0f);
            BlackBody blackBody = new BlackBody(cacheRect.left, cacheRect.right, cacheRect.top, cacheRect.bottom);
            blackBody.setFrameColor(Color.WHITE);
            blackBody.setTempPreValue(345);
            TemperatureModule.getIns().startK6080BlackBodyMode(blackBody);
        }
    };

    private void initCorrectArea() {
        final SeekBar xSeekBar = findViewById(R.id.sb_x_multi_setting);
        final SeekBar ySeekBar = findViewById(R.id.sb_y_multi_setting);
        final ImageView ivExample = findViewById(R.id.iv_example_multi_setting);
        ivExample.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                final Rect rect = getCacheRect();

                ivExample.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                final Bitmap bitmap = getBitmap(ivExample.getMeasuredWidth(), ivExample.getMeasuredHeight());

                ivExample.setImageBitmap(drawRectangles(bitmap, rect));

                xSeekBar.setProgress(rect.left);
                xSeekBar.setMax(80 - rect.width());

                ySeekBar.setProgress(rect.top);
                ySeekBar.setMax(60 - rect.height());

                final int widthHieght = 5;
                SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (seekBar.getId() == R.id.sb_x_multi_setting) {
                            rect.left = 0 + progress;
                            rect.right = widthHieght + progress;
                            if (rect.right >= 80) {
                                seekBar.setProgress(progress);
                                return;
                            }
                        } else {
                            rect.top = 0 + progress;
                            rect.bottom = widthHieght + progress;
                            if (rect.bottom >= 60) {
                                seekBar.setProgress(progress);
                                return;
                            }
                        }
                        mSaveRect.set(rect);
                        ivExample.setImageBitmap(drawRectangles(bitmap, rect));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                };

                xSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
                ySeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
            }
        });
    }

    private Rect mSaveRect = new Rect();
    private static final String TAG = "MultiThermalSettingActi";

    //生成纯色bitmap
    private Bitmap getBitmap(int measuredWidth, int measuredHeight) {
        Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_4444);
        bitmap.eraseColor(Color.parseColor("#2D000000")); // 填充颜色
        return bitmap;
    }

    //生成带框的bitmap
    private Bitmap drawRectangles(Bitmap imageBitmap, Rect rect) {
        Bitmap mutableBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);//不填充

        int widthOffset = imageBitmap.getWidth() / 80;
        int heightOffset = imageBitmap.getHeight() / 60;
        paint.setStrokeWidth(1 * widthOffset); //线的宽度

        RectF exmpRect = new RectF(rect);
        exmpRect.left *= widthOffset;
        exmpRect.right *= widthOffset;
        exmpRect.top *= heightOffset;
        exmpRect.bottom *= heightOffset;
        canvas.drawRect(exmpRect, paint);
        return mutableBitmap;
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
                    UIUtils.showTitleTip(MultiThermalSettingActivity.this, getResString(R.string.setting_please_set_ip));
                    return;
                }


                if (TextUtils.isEmpty(mResPort)) {
                    UIUtils.showTitleTip(MultiThermalSettingActivity.this, getResString(R.string.setting_please_set_res));
                    return;
                }
                int intResPort = Integer.parseInt(mResPort);
                if(intResPort > 65535){
                    UIUtils.showTitleTip(MultiThermalSettingActivity.this, getResString(R.string.setting_res_port_error));
                    return;
                }

                if (TextUtils.isEmpty(mXmppPort)) {
                    UIUtils.showTitleTip(MultiThermalSettingActivity.this, getResString(R.string.setting_please_set_xmpp));
                    return;
                }
                int intXmppPort = Integer.parseInt(mXmppPort);
                if(intXmppPort > 65535){
                    UIUtils.showTitleTip(MultiThermalSettingActivity.this, getResString(R.string.setting_xmpp_port_error));
                    return;
                }

                if (TextUtils.isEmpty(mProName)) {
                }

                if (rbYun.isChecked()) {
                    SpUtils.saveInt(Constants.Key.SERVER_MODEL, Constants.serverModel.YUN);
                } else if (rbJu.isChecked()) {
                    SpUtils.saveInt(Constants.Key.SERVER_MODEL, Constants.serverModel.JU);
                    SpUtils.saveStr(Constants.Key.JU_IP_CACHE, mIp);
                    SpUtils.saveStr(Constants.Key.JU_RESOURCE_PORT_CACHE, mResPort);
                    SpUtils.saveStr(Constants.Key.JU_XMPP_PORT_CACHE, mXmppPort);
                    SpUtils.saveStr(Constants.Key.JU_PROJECT_NAME_SUFFIX, mProName);
                }
                UIUtils.showTitleTip(MultiThermalSettingActivity.this, getResString(R.string.setting_save_succ_please_restart));
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
            ip = SpUtils.getStr(Constants.Key.JU_IP_CACHE);
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.left_in, R.anim.left_out);
    }
}
