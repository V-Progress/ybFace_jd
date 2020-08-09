package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSONObject;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.FlavorType;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.DisplayOrientationEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.activity.PowerOnOffActivity;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.logutils.Utils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.apache.commons.io.FileUtils;
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

public class ThermalSettingActivity extends BaseActivity {
    private static final String TAG = "SettingActivity";
    public static boolean isTurkey = false;

    @Override
    protected int getPortraitLayout() {
        if (Utils.getWinWidth(this) == 600 && Utils.getWinHight(this)  == 976) {
            //适配土耳其客户机器
            isTurkey = true;
            return R.layout.activity_thermal_setting_976600;
        }
        return R.layout.activity_thermal_setting;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_thermal_setting;
    }

    @Override
    protected void initView() {
        RadioGroup rgTab = findViewById(R.id.rg_tab_setting);
        rgTab.check(R.id.rb_face_setting);
        replaceFragment(R.id.fl_setting_content, new FaceSettingFragment());
        rgTab.setOnCheckedChangeListener((group, checkedId) -> {
            Fragment fragment = null;
            switch (checkedId) {
                case R.id.rb_face_setting:
                    fragment = new FaceSettingFragment();
                    break;
                case R.id.rb_page_setting:
                    fragment = new PageSettingFragment();
                    break;
                case R.id.rb_temper_setting:
                    fragment = new TemperSettingFragment();
                    break;
                case R.id.rb_device_setting:
                    fragment = new DeviceSettingFragment();
                    break;
            }
            replaceFragment(R.id.fl_setting_content, fragment);
        });
    }

    private static final int REQEST_SELECT_IMAGES_CODE = 12345;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == FileSelectActivity.SELECT_REQUEST_CODE && resultCode == RESULT_OK){
            ImageView ivMainLogo = findViewById(R.id.iv_main_logo);
            String imgPath = data.getStringExtra(FileSelectActivity.RESULT_PATH_KEY);
            if (TextUtils.isEmpty(imgPath)) {
                UIUtils.showShort(this, getResString(R.string.select_img_failed));
                ivMainLogo.setImageResource(ThermalConst.Default.DEFAULT_LOGO_ID);
                return;
            }

            File oringinFile = new File(imgPath);
            File newFile = new File(Constants.LOGO_DIR_PATH,oringinFile.getName());
            try {
                Timber.d("拷贝文件，源文件" + oringinFile.getPath());
                FileUtils.copyFile(oringinFile,newFile);
                Timber.d("拷贝文件成功，新文件：" + newFile.getPath());
                if (!newFile.exists()) {
                    UIUtils.showShort(this, getResString(R.string.select_img_failed_not_exists));
                    ivMainLogo.setImageResource(ThermalConst.Default.DEFAULT_LOGO_ID);
                } else {
                    SpUtils.saveStr(ThermalConst.Key.MAIN_LOGO_IMG, imgPath);
                    ivMainLogo.setImageBitmap(BitmapFactory.decodeFile(newFile.getPath()));
                }
            } catch (IOException e) {
                e.printStackTrace();
                UIUtils.showShort(this, getResString(R.string.select_img_failed));
                ivMainLogo.setImageResource(ThermalConst.Default.DEFAULT_LOGO_ID);
            }
        }
    }

    public static class FaceSettingFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_face_setting, container, false);
            if (ThermalSettingActivity.isTurkey) {
                rootView.setPadding(20, 10, 0 , 10);
            }
            return rootView;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            initView(getView());
        }

        private void initView(View view){
            //设置对比度======================================================================
            final EditText edtSimilar = view.findViewById(R.id.edt_similar_threshold);
            int similar = SpUtils.getIntOrDef(Constants.Key.SIMILAR_THRESHOLD, Constants.Default.SIMILAR_THRESHOLD);
            edtSimilar.setText(similar + "");
            view.findViewById(R.id.btn_set_similar_threshold).setOnClickListener(v -> {
                String similar1 = edtSimilar.getText().toString();
                if (TextUtils.isEmpty(similar1)) {
                    edtSimilar.setText(similar1 + "");
                    return;
                }
                int sml = Integer.parseInt(similar1);
                SpUtils.saveInt(Constants.Key.SIMILAR_THRESHOLD, sml);
                Activity activity = APP.getMainActivity();
                if (activity != null) {
                    if (activity instanceof ThermalImage2Activity) {
                        ((ThermalImage2Activity) activity).setFaceViewSimilar();
                    }
                }
            });

            //设置活体=========================================================================
            View llLivenessArea = view.findViewById(R.id.ll_liveness_area);
            llLivenessArea.setVisibility(Constants.Default.SHOW_LIVENESS_SETTING ? View.VISIBLE :View.GONE);
            Switch swLiveness = view.findViewById(R.id.sw_liveness_setting);
            boolean liveness = SpUtils.getBoolean(Constants.Key.LIVENESS_ENABLED, Constants.Default.LIVENESS_ENABLED);
            swLiveness.setChecked(liveness);
            swLiveness.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(Constants.Key.LIVENESS_ENABLED, isChecked));

/*
            Button btnLiveThreSub = view.findViewById(R.id.btn_liveness_threshold_sub_setting);
            Button btnLiveThrePlus = view.findViewById(R.id.btn_liveness_threshold_add_setting);
            Float livenessThreshold = SpUtils.getFloat(Constants.Key.LIVENESS_THRESHOLD, Constants.Default.LIVENESS_THRESHOLD);
            int thresholdInt = (int) (livenessThreshold * 100);
            Timber.d("预设值:" + thresholdInt);
            EditText edtLivenessThreshold = view.findViewById(R.id.edt_liveness_threshold_setting);
            edtLivenessThreshold.setText(String.valueOf(thresholdInt));
            View.OnClickListener onClickListener = v -> {
                float thresholdF = livenessThreshold;

                String string = edtLivenessThreshold.getText().toString();
                int i = Integer.parseInt(string);
                Timber.d("转换值:" + i);
                if(v.getId() == R.id.btn_liveness_threshold_sub_setting){
                    thresholdF -= 0.1f;
                } else {
                    thresholdF += 0.1f;
                }
                float resultThreshold = ((BaseActivity) getActivity()).formatF((float) i / 100);
                Timber.d("转换值：" + resultThreshold);
                SpUtils.saveFloat(Constants.Key.LIVENESS_THRESHOLD,resultThreshold);
            };
            btnLiveThrePlus.setOnClickListener(onClickListener);
            btnLiveThreSub.setOnClickListener(onClickListener);
*/

            //人脸框=========================================================================
            //人脸框横向镜像
            CheckBox cbMirror = view.findViewById(R.id.cb_mirror);
            final boolean mirror = SpUtils.getBoolean(Constants.Key.IS_H_MIRROR,Constants.Default.IS_H_MIRROR);
            cbMirror.setChecked(mirror);
            cbMirror.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(Constants.Key.IS_H_MIRROR,isChecked));
            //人脸框纵向镜像
            CheckBox cbVerticalMirror = view.findViewById(R.id.cb_vertical_mirror);
            boolean isVMirror = SpUtils.getBoolean(Constants.Key.IS_V_MIRROR, Constants.Default.IS_V_MIRROR);
            cbVerticalMirror.setChecked(isVMirror);
            cbVerticalMirror.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(Constants.Key.IS_V_MIRROR, isChecked));

            //人脸弹窗=========================================================================
            Switch switchFaceDialog = view.findViewById(R.id.sw_face_dialog);
            boolean faceDialog = SpUtils.getBoolean(ThermalConst.Key.SHOW_DIALOG, ThermalConst.Default.SHOW_DIALOG);
            switchFaceDialog.setChecked(faceDialog);
            switchFaceDialog.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(ThermalConst.Key.SHOW_DIALOG, isChecked));

            //摄像头设置=========================================================================
            //摄像头角度
            Button btnAngle = view.findViewById(R.id.btn_setAngle);
            int angle = SpUtils.getIntOrDef(Constants.Key.CAMERA_ANGLE, Constants.Default.CAMERA_ANGLE);
            btnAngle.setText(getString(R.string.setting_cam_angle) + ":" + angle);
            btnAngle.setOnClickListener(v -> {
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
                btnAngle.setText(getString(R.string.setting_cam_angle) + ":" + anInt);
                SpUtils.saveInt(Constants.Key.CAMERA_ANGLE, anInt);
                EventBus.getDefault().post(new DisplayOrientationEvent());
            });

            Button btnPicRotation = view.findViewById(R.id.btn_picture_rotation);
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
    }

    public static class PageSettingFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_page_setting, container, false);
            if (ThermalSettingActivity.isTurkey) {
                rootView = inflater.inflate(R.layout.fragment_page_setting_976600, container, false);
                rootView.setPadding(20, 10, 0 , 10);
            }
            return rootView;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            initView(getView());
        }

        private void initView(View view){
            //屏保===========================================================================================
            boolean isEnabled = SpUtils.getBoolean(Constants.Key.POSTER_ENABLED, Constants.Default.POSTER_ENABLED);
            Switch swPoster = view.findViewById(R.id.sw_poster_setting);
            swPoster.setChecked(isEnabled);
            swPoster.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SpUtils.saveBoolean(Constants.Key.POSTER_ENABLED, isChecked);
                }
            });

            //人相框===========================================================================================
            boolean aBoolean1 = SpUtils.getBoolean(ThermalConst.Key.PERSON_FRAME, ThermalConst.Default.PERSON_FRAME);
            Switch swPersonFrame = view.findViewById(R.id.sw_person_frame_setting);
            swPersonFrame.setChecked(aBoolean1);
            swPersonFrame.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(ThermalConst.Key.PERSON_FRAME, isChecked));

            //logo设置===========================================================================================
            boolean showMainLogo = SpUtils.getBoolean(ThermalConst.Key.SHOW_MAIN_LOGO, ThermalConst.Default.SHOW_MAIN_LOGO);
            Switch swMainLogo = view.findViewById(R.id.sw_main_logo_setting);
            swMainLogo.setChecked(showMainLogo);
            swMainLogo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SpUtils.saveBoolean(ThermalConst.Key.SHOW_MAIN_LOGO, isChecked);
                }
            });

            //热成像设置===========================================================================================
            boolean showMainThermal = SpUtils.getBoolean(ThermalConst.Key.SHOW_MAIN_THERMAL, ThermalConst.Default.SHOW_MAIN_THERMAL);
            Switch swMainThermal = view.findViewById(R.id.sw_main_thermal_setting);
            swMainThermal.setChecked(showMainThermal);
            swMainThermal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SpUtils.saveBoolean(ThermalConst.Key.SHOW_MAIN_THERMAL, isChecked);
                }
            });

            //首页信息==============================================================================================
            boolean showMainInfo = SpUtils.getBoolean(ThermalConst.Key.SHOW_MAIN_INFO, ThermalConst.Default.SHOW_MAIN_INFO);
            Switch swMainInfo = view.findViewById(R.id.sw_main_info_setting);
            swMainInfo.setChecked(showMainInfo);
            swMainInfo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SpUtils.saveBoolean(ThermalConst.Key.SHOW_MAIN_INFO, isChecked);
                }
            });

            //二维码设置=============================================================================================
            boolean qrCodeEnabled = SpUtils.getBoolean(Constants.Key.QRCODE_ENABLED, Constants.Default.QRCODE_ENABLED);
            Switch swQrCode = view.findViewById(R.id.sw_qrcode_setting);
            swQrCode.setChecked(qrCodeEnabled);
            swQrCode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SpUtils.saveBoolean(Constants.Key.QRCODE_ENABLED, isChecked);
                }
            });

            //首页列表================================================================================================
            boolean showMainSignList = SpUtils.getBoolean(Constants.Key.MAIN_SIGN_LIST,Constants.Default.MAIN_SIGN_LIST);
            View llMainListParent = view.findViewById(R.id.ll_main_list_parent);
            if(getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE){
                llMainListParent.setVisibility(View.GONE);
            }
            Switch swMainList = view.findViewById(R.id.sw_main_list_setting);
            swMainList.setChecked(showMainSignList);
            swMainList.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SpUtils.saveBoolean(Constants.Key.MAIN_SIGN_LIST,isChecked);
            });

            //首页logo文字============================================================================================
            String mainLogoText = SpUtils.getStr(ThermalConst.Key.MAIN_LOGO_TEXT, ThermalConst.Default.MAIN_LOGO_TEXT);
            EditText edtMainLogoText = view.findViewById(R.id.edt_main_logo_text);
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
                    String s1 = s.toString();
                    if (TextUtils.isEmpty(s1)) {
                        SpUtils.remove(ThermalConst.Key.MAIN_LOGO_TEXT);
                    } else {
                        SpUtils.saveStr(ThermalConst.Key.MAIN_LOGO_TEXT, s1);
                    }
                }
            });
            boolean titleEnabled = SpUtils.getBoolean(ThermalConst.Key.TITLE_ENABLED,ThermalConst.Default.TITLE_ENABLED);
            Switch swTitle = view.findViewById(R.id.sw_title_display);
            swTitle.setChecked(titleEnabled);
            swTitle.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(ThermalConst.Key.TITLE_ENABLED,isChecked));

            //设置首页LOGO================================================================================
            Button btnRestore = view.findViewById(R.id.btn_restore_main_logo);
            Button btnSaveMainLogo = view.findViewById(R.id.btn_save_main_logo);
            ImageView ivMainLogo = view.findViewById(R.id.iv_main_logo);
            String mainLogoImg = SpUtils.getStr(ThermalConst.Key.MAIN_LOGO_IMG, ThermalConst.Default.MAIN_LOGO_IMG);
            if (TextUtils.isEmpty(mainLogoImg)) {
                ivMainLogo.setImageResource(ThermalConst.Default.DEFAULT_LOGO_ID);
            } else {
                File file = new File(mainLogoImg);
                if (!file.exists()) {
                    ivMainLogo.setImageResource(ThermalConst.Default.DEFAULT_LOGO_ID);
                } else {
                    ivMainLogo.setImageBitmap(BitmapFactory.decodeFile(mainLogoImg));
                }
            }
            btnSaveMainLogo.setOnClickListener(v -> FileSelectActivity.selectFile(getActivity(),FileSelectActivity.FILE_TYPE_IMG,true,FileSelectActivity.SELECT_REQUEST_CODE));
            btnRestore.setOnClickListener(v -> {
                SpUtils.remove(ThermalConst.Key.MAIN_LOGO_IMG);
                ivMainLogo.setImageResource(ThermalConst.Default.DEFAULT_LOGO_ID);
            });

            //优先级配置=======================================================================================
            Switch swLocalLogo = view.findViewById(R.id.sw_local_logo_setting);
            boolean localPriority = SpUtils.getBoolean(ThermalConst.Key.LOCAL_PRIORITY, ThermalConst.Default.LOCAL_PRIORITY);
            swLocalLogo.setChecked(localPriority);
            swLocalLogo.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(ThermalConst.Key.LOCAL_PRIORITY, isChecked));

            //跳转语音设置
            view.findViewById(R.id.btn_go_speech).setOnClickListener(v -> startActivity(new Intent(getActivity(), SpeechContentActivity.class)));

        }
    }

    public static class TemperSettingFragment extends Fragment {

        private EditText edtCorrect;
        private TextView tvCorrectionF;
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_temper_setting, container, false);
            if (ThermalSettingActivity.isTurkey) {
                rootView.setPadding(20, 10, 0 , 10);
            }
            return rootView;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            initView(getView());
        }

        @Override
        public void onResume() {
            super.onResume();

            float thermalCorrect = SpUtils.getFloat(ThermalConst.Key.THERMAL_CORRECT, ThermalConst.Default.THERMAL_CORRECT);
            edtCorrect.setText(thermalCorrect + "");
            tvCorrectionF.setText(getCorrectionFahrenheit(thermalCorrect));
        }

        private void initView(View view) {
            Button btn_thermal_corr = view.findViewById(R.id.btn_thermal_corr);
            View llBroadDelay = view.findViewById(R.id.ll_broad_delay);
            final TextView tvModelSetting = view.findViewById(R.id.tv_model_setting);
            CheckBox cbFace = view.findViewById(R.id.cb_face);
            CheckBox cbTemper = view.findViewById(R.id.cb_temper);

            String[] temperModuleArray = getResources().getStringArray(R.array.temper_module);
            int currTemperModule = SpUtils.getIntOrDef(ThermalConst.Key.TEMPER_MODULE,ThermalConst.Default.TEMPER_MODULE);
            boolean faceEnabled = SpUtils.getBoolean(ThermalConst.Key.FACE_ENABLED,ThermalConst.Default.FACE_ENABLED);
            boolean temperEnabled = SpUtils.getBoolean(ThermalConst.Key.TEMPER_ENABLED,ThermalConst.Default.TEMPER_ENABLED);
            llBroadDelay.setVisibility(faceEnabled ? View.GONE : View.VISIBLE);
            tvModelSetting.setText(temperModuleArray[currTemperModule]);
            cbFace.setChecked(faceEnabled);
            cbTemper.setChecked(temperEnabled);
            if (temperEnabled) {
                btn_thermal_corr.setVisibility(View.VISIBLE);
            } else {
                btn_thermal_corr.setVisibility(View.GONE);
            }

            tvModelSetting.setOnClickListener(view1 -> {
                int currModule = SpUtils.getIntOrDef(ThermalConst.Key.TEMPER_MODULE,ThermalConst.Default.TEMPER_MODULE);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(R.string.setting_select_model));
                builder.setSingleChoiceItems(temperModuleArray, currModule, (dialog, whichModel) -> {
                    //如果模式相同则直接隐藏
                    if (whichModel == currModule) {
                        dialog.dismiss();
                        return;
                    }
                    SpUtils.saveInt(ThermalConst.Key.TEMPER_MODULE, whichModel);
                    tvModelSetting.setText(temperModuleArray[whichModel]);
                    dialog.dismiss();
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            });
            CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (buttonView, isChecked) -> {
                if(buttonView.getId() == R.id.cb_face){
                    boolean temperE = SpUtils.getBoolean(ThermalConst.Key.TEMPER_ENABLED, ThermalConst.Default.TEMPER_ENABLED);
                    if(!isChecked && !temperE){
                        cbFace.setChecked(true);
                        UIUtils.showShort(getActivity(),getString(R.string.model_check_tip));
                        return;
                    }
                    llBroadDelay.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                    SpUtils.saveBoolean(ThermalConst.Key.FACE_ENABLED,isChecked);
                } else {
                    boolean faceE = SpUtils.getBoolean(ThermalConst.Key.FACE_ENABLED, ThermalConst.Default.FACE_ENABLED);
                    if(!isChecked && !faceE){
                        cbTemper.setChecked(true);
                        UIUtils.showShort(getActivity(),getString(R.string.model_check_tip));
                        return;
                    }
                    SpUtils.saveBoolean(ThermalConst.Key.TEMPER_ENABLED,isChecked);
                    if (isChecked) {
                        btn_thermal_corr.setVisibility(View.VISIBLE);
                    } else {
                        btn_thermal_corr.setVisibility(View.GONE);
                    }
                }
            };
            cbFace.setOnCheckedChangeListener(onCheckedChangeListener);
            cbTemper.setOnCheckedChangeListener(onCheckedChangeListener);

            //隐私模式=====================================================================================================
            boolean isPrivacyModeEnabled = SpUtils.getBoolean(Constants.Key.PRIVACY_MODE, Constants.Default.PRIVACY_MODE);
            Switch swPrivacy = view.findViewById(R.id.sw_privacy_mode);
            swPrivacy.setChecked(isPrivacyModeEnabled);

            boolean noDataMode = SpUtils.getBoolean(Constants.Key.NODATA_MODE, Constants.Default.NODATA_MODE);
            Switch swNoDataMode = view.findViewById(R.id.sw_no_data_mode);
            swNoDataMode.setChecked(noDataMode);

            swPrivacy.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if(isChecked && swNoDataMode.isChecked()){
                    swNoDataMode.setChecked(false);
                }
                SpUtils.saveBoolean(Constants.Key.PRIVACY_MODE, isChecked);
            });

            //无数据模式===================================================================================================
            swNoDataMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if(isChecked && swPrivacy.isChecked()){
                    swPrivacy.setChecked(false);
                }
                SpUtils.saveBoolean(Constants.Key.NODATA_MODE,isChecked);
            });

            //热成像镜像==========================================================================================
            boolean thermalImgMirror = SpUtils.getBoolean(ThermalConst.Key.THERMAL_IMAGE_MIRROR, ThermalConst.Default.THERMAL_IMAGE_MIRROR);
            Switch swThermalMirror = view.findViewById(R.id.sw_thermal_imag_mirror_setting);
            swThermalMirror.setChecked(thermalImgMirror);
            swThermalMirror.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(ThermalConst.Key.THERMAL_IMAGE_MIRROR, isChecked));

            //====华氏度开关==============================================================
            boolean fEnabled = SpUtils.getBoolean(ThermalConst.Key.THERMAL_F_ENABLED, ThermalConst.Default.THERMAL_F_ENABLED);
            Switch swFEnabled = view.findViewById(R.id.sw_f_enabled_setting);
            swFEnabled.setChecked(fEnabled);
            swFEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(ThermalConst.Key.THERMAL_F_ENABLED, isChecked));

            //===低温模式=========================================================
            Switch swLowTempModel = view.findViewById(R.id.sw_low_temp_model_setting);
            Switch swHighTempModel = view.findViewById(R.id.sw_high_temp_model_setting);

            boolean aBoolean = SpUtils.getBoolean(ThermalConst.Key.LOW_TEMP_MODE, ThermalConst.Default.LOW_TEMP);
            swLowTempModel.setChecked(aBoolean);
            swLowTempModel.setOnCheckedChangeListener((buttonView, isChecked) -> {
                //如果在低温模式开启的时候，高温模式也开启了，那么就把高温模式关闭
                if (isChecked && swHighTempModel.isChecked()) {
                    SpUtils.saveBoolean(ThermalConst.Key.LOW_TEMP_MODE, false);
                    swHighTempModel.setChecked(false);
                }
                SpUtils.saveBoolean(ThermalConst.Key.LOW_TEMP_MODE, isChecked);
            });

            //===自动模式=========================================================
            boolean autoTemper = SpUtils.getBoolean(ThermalConst.Key.HIGH_TEMPER_MODE, ThermalConst.Default.HIGH_TEMPER_MODE);
            swHighTempModel.setChecked(autoTemper);
            swHighTempModel.setOnCheckedChangeListener((buttonView, isChecked) -> {
                //如果在开启高温模式的时候，低温模式也开启了，那么就把低温模式关闭
                if (isChecked && swLowTempModel.isChecked()) {
                    SpUtils.saveBoolean(ThermalConst.Key.LOW_TEMP_MODE, false);
                    swLowTempModel.setChecked(false);
                }
                SpUtils.saveBoolean(ThermalConst.Key.HIGH_TEMPER_MODE, isChecked);
            });

            //===无人脸报温=======================================================
            boolean noFaceTemper = SpUtils.getBoolean(ThermalConst.Key.NO_FACE_TEMPER,ThermalConst.Default.NO_FACE_TEMPER);
            Switch swNoFaceTemper = view.findViewById(R.id.sw_no_face_temper);
            swNoFaceTemper.setChecked(noFaceTemper);
            swNoFaceTemper.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(ThermalConst.Key.NO_FACE_TEMPER,isChecked));


            TextView tvMaxF = view.findViewById(R.id.tv_max_threshold_fahrenheit);
            tvCorrectionF = view.findViewById(R.id.tv_correction_fahrenheit);
            TextView tvMinF = view.findViewById(R.id.tv_min_threshold_fahrenheit);
            //进入矫正=======================================================================================================
            float thermalCorrect = SpUtils.getFloat(ThermalConst.Key.THERMAL_CORRECT, ThermalConst.Default.THERMAL_CORRECT);
            Button btnCorrectSub = view.findViewById(R.id.btn_correct_sub_setting);
            Button btnCorrectAdd = view.findViewById(R.id.btn_correct_add_setting);
            edtCorrect = view.findViewById(R.id.edt_correct_setting);
            edtCorrect.setText(thermalCorrect + "");
            tvCorrectionF.setText(getCorrectionFahrenheit(thermalCorrect));

            View.OnClickListener correctOnclickListener = v -> {
                String s = edtCorrect.getText().toString();
                float corrValue = Float.parseFloat(s);
                if (v.getId() == R.id.btn_correct_sub_setting) {
                    corrValue -= 0.1f;
                } else {
                    corrValue += 0.1f;
                }
                corrValue = ((ThermalSettingActivity)getActivity()).formatF(corrValue);
                SpUtils.saveFloat(ThermalConst.Key.THERMAL_CORRECT, corrValue);
                edtCorrect.setText(corrValue + "");

                tvCorrectionF.setText(getCorrectionFahrenheit(corrValue));
            };
            btnCorrectSub.setOnClickListener(correctOnclickListener);
            btnCorrectAdd.setOnClickListener(correctOnclickListener);

            btn_thermal_corr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), TemperatureCorrectActivity.class));
                }
            });

            //修改测温阈值==========================================================================================
            Button btnMinSub = view.findViewById(R.id.btn_temp_min_threshold_sub_setting);
            Button btnMinAdd = view.findViewById(R.id.btn_temp_min_threshold_add_setting);
            final EditText edtMinThreshold = view.findViewById(R.id.edt_temp_min_threshold_setting);
            final float minValue = SpUtils.getFloat(ThermalConst.Key.TEMP_MIN_THRESHOLD, ThermalConst.Default.TEMP_MIN_THRESHOLD);
            edtMinThreshold.setText(minValue + "");
            tvMinF.setText(getFahrenheit(minValue));
            View.OnClickListener minClickListener = v -> {
                String value = edtMinThreshold.getText().toString();
                float v1 = ((ThermalSettingActivity)getActivity()).formatF(Float.parseFloat(value));
                switch (v.getId()) {
                    case R.id.btn_temp_min_threshold_sub_setting:
                        v1 -= 0.1;
                        break;
                    case R.id.btn_temp_min_threshold_add_setting:
                        v1 += 0.1;
                        break;
                }
                v1 = ((ThermalSettingActivity)getActivity()).formatF(v1);
                edtMinThreshold.setText(v1 + "");
                SpUtils.saveFloat(ThermalConst.Key.TEMP_MIN_THRESHOLD, v1);

                tvMinF.setText(getFahrenheit(v1));
            };
            btnMinSub.setOnClickListener(minClickListener);
            btnMinAdd.setOnClickListener(minClickListener);

            //修改测温报警值==========================================================================================
            Button btnWarnSub = view.findViewById(R.id.btn_temp_warning_threshold_sub_setting);
            Button btnWarnAdd = view.findViewById(R.id.btn_temp_warning_threshold_add_setting);
            final EditText edtWarnThreshold = view.findViewById(R.id.edt_temp_warning_threshold_setting);
            final float warningValue = SpUtils.getFloat(ThermalConst.Key.TEMP_WARNING_THRESHOLD, ThermalConst.Default.TEMP_WARNING_THRESHOLD);
            edtWarnThreshold.setText(warningValue + "");
            tvMaxF.setText(getFahrenheit(warningValue));
            View.OnClickListener warnClickListener = v -> {
                String value = edtWarnThreshold.getText().toString();
                float v1 = ((ThermalSettingActivity)getActivity()).formatF(Float.parseFloat(value));
                if (v.getId() == R.id.btn_temp_warning_threshold_sub_setting) {
                    v1 -= 0.1;
                } else {
                    v1 += 0.1;
                }
                v1 = ((ThermalSettingActivity)getActivity()).formatF(v1);
                edtWarnThreshold.setText(v1 + "");
                SpUtils.saveFloat(ThermalConst.Key.TEMP_WARNING_THRESHOLD, v1);

                tvMaxF.setText(getFahrenheit(v1));
            };
            btnWarnSub.setOnClickListener(warnClickListener);
            btnWarnAdd.setOnClickListener(warnClickListener);

            //体温播报延时==========================================================================================
            Button btnSpeechDelaySub = view.findViewById(R.id.btn_speech_delay_sub_setting);
            Button btnSpeechDelayAdd = view.findViewById(R.id.btn_speech_delay_add_setting);
            final EditText edtSpeechDelay = view.findViewById(R.id.edt_speech_delay_setting);
            long speechDelayTime = SpUtils.getLong(ThermalConst.Key.SPEECH_DELAY, ThermalConst.Default.SPEECH_DELAY);
            edtSpeechDelay.setText(speechDelayTime + "");
            View.OnClickListener speechDelayOnClickLitsener = v -> {
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
            };
            btnSpeechDelayAdd.setOnClickListener(speechDelayOnClickLitsener);
            btnSpeechDelaySub.setOnClickListener(speechDelayOnClickLitsener);
        }

        private String getFahrenheit(float centigrade){
            return "( " + formatF((float) (centigrade * 1.8 + 32)) + "℉ )";
        }

        private String getCorrectionFahrenheit(float centigrade){
            return "( " + formatF((float) ((centigrade * 1.8 + 32) - 32)) + "℉ )";
        }

        protected float formatF(float fValue) {
            return (float) (Math.round(fValue * 10)) / 10;
        }
    }

    public static class DeviceSettingFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_device_setting, container, false);
            if (ThermalSettingActivity.isTurkey) {
                rootView.setPadding(20, 10, 0 , 10);
            }
            return rootView;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            initView(getView());
        }

        public void jumpTag() {
            final boolean jumpTag = SpUtils.getBoolean(Constants.Key.JUMP_TAG, Constants.Default.JUMP_TAG);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(APP.getContext().getResources().getString(R.string.setting_switch_function));
            builder.setMessage(APP.getContext().getResources().getString(R.string.setting_switch_tip1));
            builder.setNegativeButton(APP.getContext().getResources().getString(R.string.setting_switch_cancel), (dialog, which) -> dialog.dismiss());
            builder.setPositiveButton(APP.getContext().getResources().getString(R.string.setting_switch_confirm), (dialog, which) -> {
                dialog.dismiss();
                SpUtils.saveBoolean(Constants.Key.JUMP_TAG, !jumpTag);

                APP.exit2();
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        private void initView(View view) {
            View llSwitchFunction = view.findViewById(R.id.ll_switch_function);
            if(Constants.FLAVOR_TYPE != FlavorType.HT){
                llSwitchFunction.setVisibility(View.GONE);
            } else {
                llSwitchFunction.setVisibility(View.VISIBLE);
                //切换功能
                Button btnSwitchFunction = view.findViewById(R.id.btn_switch_function);
                btnSwitchFunction.setOnClickListener(view1 -> jumpTag());
            }

            //CPU状态==================================================================================
            final TextView tvCpuTemper = view.findViewById(R.id.tv_cpu_temper);
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                final String s = CpuUtils.getCpuTemperatureFinder() + "℃";
                getActivity().runOnUiThread(() -> tvCpuTemper.setText(s));
            }, 0, 3, TimeUnit.SECONDS);

            //网络状态=================================================================================
            TextView tvNetState = view.findViewById(R.id.tv_wifi_state);
            String net = "";
            boolean intenetConnected = isInternetConnected(getActivity());
            if (intenetConnected) {
                net = getString(R.string.setting_net_prefix) + getHostIp() + "】";
            } else {
                net = "【WIFI，" + getWifiInfo(0) + getString(R.string.setting_ip_info) + getWifiInfo(1) + "】";
            }
            tvNetState.setText(net);

            //IC读卡器===============================================================================
            boolean readCardEnabled = SpUtils.getBoolean(Constants.Key.READ_CARD_ENABLED, Constants.Default.READ_CARD_ENABLED);
            Switch swReadCard = view.findViewById(R.id.sw_readcard_setting);
            swReadCard.setChecked(readCardEnabled);
            swReadCard.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(Constants.Key.READ_CARD_ENABLED, isChecked));

            //开门时间===================================================================================
            final EditText edtDelay = view.findViewById(R.id.edt_delay);
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
                    UIUtils.showShort(getActivity(), getString(R.string.open_door_delay_saved_tips));
                }
            });

            //清除策略================================================================================
            RadioGroup rgClear = view.findViewById(R.id.rg_clear_policy);
            EditText edtPolicy = view.findViewById(R.id.edt_policy_custom);
            int customDate = SpUtils.getIntOrDef(Constants.Key.CLEAR_POLICY_CUSTOM, Constants.Default.CLEAR_POLICY_CUSTOM);
            edtPolicy.setText(String.valueOf(customDate));
            edtPolicy.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
                @Override
                public void afterTextChanged(Editable s) {
                    String content = s.toString();
                    if(TextUtils.isEmpty(content)){
                        SpUtils.remove(Constants.Key.CLEAR_POLICY_CUSTOM);
                    } else {
                        SpUtils.saveInt(Constants.Key.CLEAR_POLICY_CUSTOM,Integer.parseInt(content));
                    }
                }
            });

            int clearPolicy = SpUtils.getIntOrDef(Constants.Key.CLEAR_POLICY, Constants.Default.CLEAR_POLICY);
            switch (clearPolicy) {
                case 0:
                    rgClear.check(R.id.rb_clear_policy_7);
                    break;
                case 1:
                    rgClear.check(R.id.rb_clear_policy_15);
                    break;
                case 2:
                    rgClear.check(R.id.rb_clear_policy_30);
                    break;
                case 3:
                    rgClear.check(R.id.rb_clear_policy_custom);
                    break;
            }
            rgClear.setOnCheckedChangeListener((group, checkedId) -> {
                switch (checkedId) {
                    case R.id.rb_clear_policy_7:
                        edtPolicy.setEnabled(false);
                        SpUtils.saveInt(Constants.Key.CLEAR_POLICY,0);
                        break;
                    case R.id.rb_clear_policy_15:
                        edtPolicy.setEnabled(false);
                        SpUtils.saveInt(Constants.Key.CLEAR_POLICY,1);
                        break;
                    case R.id.rb_clear_policy_30:
                        edtPolicy.setEnabled(false);
                        SpUtils.saveInt(Constants.Key.CLEAR_POLICY,2);
                        break;
                    case R.id.rb_clear_policy_custom:
                        edtPolicy.setEnabled(true);
                        SpUtils.saveInt(Constants.Key.CLEAR_POLICY,3);
                        break;
                }
            });

            //重启=====================================================================================
            view.findViewById(R.id.tv_reboot).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAlert(getString(R.string.restart_dialog_title), getString(R.string.setting_device_will_reboot), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ProgressDialog progressDialog = UIUtils.coreInfoShow3sDialog(getActivity());
                            progressDialog.setTitle(getString(R.string.setting_device_reboot));
                            progressDialog.setMessage(getString(R.string.setting_3_scond_reboot));
                            progressDialog.setCancelable(false);
                            progressDialog.show();
                            UIUtils.restart.start();
                        }
                    }, null, null);
                }
            });

            //管理密码================================================================================
            view.findViewById(R.id.btn_pwd).setOnClickListener(v -> setPwd());

            //电源设置================================================================================
            view.findViewById(R.id.tv_power).setOnClickListener(v -> startActivity(new Intent(getActivity(), PowerOnOffActivity.class)));

            //设置IP=================================================================================
            initSetIp(view);

            //清除所有数据============================================================================
            view.findViewById(R.id.tv_clear_all).setOnClickListener(view1 -> showDialog());
        }

        private void showDialog(){
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(getResources().getString(R.string.delete_user_dialog_title))
                    .setMessage(getResources().getString(R.string.clear_all_data_dialog_message))
                    .setPositiveButton(getResources().getString(R.string.setting_switch_confirm), (dialog, which) -> {
                        SignManager.instance().clearAllData(new SignManager.ClearListener() {
                            @Override
                            public void onStart() {
                                UIUtils.showNetLoading(getActivity());
                            }

                            @Override
                            public void onFinish(Integer size) {
                                UIUtils.dismissNetLoading();
                                UIUtils.showShort(getActivity(), getResources().getString(R.string.clear_no_data) + size);
                            }
                        });
                    }).setNegativeButton(getResources().getString(R.string.setting_switch_cancel), (dialog, which) -> {
                        dialog.dismiss();
                    }).create();
            alertDialog.show();
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

        private EditText edtCommunicationIp;
        private EditText edtResPort;
        private EditText edtXmppPort;
        private EditText edtProName;
        private EditText edtServiceIp;

        //初始化IP设置
        private void initSetIp(View view) {
            edtServiceIp = view.findViewById(R.id.edt_service_ip);
            edtCommunicationIp = view.findViewById(R.id.edt_communication_ip);
            edtResPort = view.findViewById(R.id.edt_res_port);
            edtXmppPort = view.findViewById(R.id.edt_xmpp_port);
            edtProName = view.findViewById(R.id.edt_pro_name);
            RadioGroup rgServerModel = view.findViewById(R.id.rg_server_model);
            final RadioButton rbYun = view.findViewById(R.id.rb_yun);
            final RadioButton rbJu = view.findViewById(R.id.rb_ju);
            Button btnSave = view.findViewById(R.id.btn_save_address);
            if (SpUtils.getIntOrDef(Constants.Key.SERVER_MODEL, Constants.Default.SERVER_MODEL) == Constants.serverModel.YUN) {
                rbYun.setChecked(true);
                setServerInfo(Constants.serverModel.YUN);
            } else {
                rbJu.setChecked(true);
                setServerInfo(Constants.serverModel.JU);
            }
            rgServerModel.setOnCheckedChangeListener((group, checkedId) -> {
                if (rbYun.isChecked()) {
                    setServerInfo(Constants.serverModel.YUN);
                }
                if (rbJu.isChecked()) {
                    setServerInfo(Constants.serverModel.JU);
                }
            });

            btnSave.setOnClickListener(v -> {
                String mServiceIp = edtServiceIp.getText().toString();
                String mCommunicationIp = edtCommunicationIp.getText().toString();
                String mResPort = edtResPort.getText().toString();
                String mXmppPort = edtXmppPort.getText().toString();
                String mProName = edtProName.getText().toString();
                if(TextUtils.isEmpty(mServiceIp)){
                    edtServiceIp.setError(APP.getContext().getResources().getString(R.string.setting_please_set_ip));
                    return;
                }
                if (TextUtils.isEmpty(mCommunicationIp)) {
                    edtCommunicationIp.setError(APP.getContext().getResources().getString(R.string.setting_please_set_ip));
                    return;
                }
                if(!TextUtils.isEmpty(mResPort)){
                    int intResPort = Integer.parseInt(mResPort);
                    if (intResPort > 65535) {
                        edtResPort.setError(APP.getContext().getResources().getString(R.string.setting_res_port_error));
                        return;
                    }
                }

                if (TextUtils.isEmpty(mXmppPort)) {
                    edtXmppPort.setError(APP.getContext().getResources().getString(R.string.setting_please_set_xmpp));
                    return;
                }
                int intXmppPort = Integer.parseInt(mXmppPort);
                if (intXmppPort > 65535) {
                    edtXmppPort.setError(APP.getContext().getResources().getString(R.string.setting_xmpp_port_error));
                    return;
                }

                ConfigLoader.save();

                if (rbYun.isChecked()) {
                    SpUtils.saveInt(Constants.Key.SERVER_MODEL, Constants.serverModel.YUN);
                } else if (rbJu.isChecked()) {
                    SpUtils.saveInt(Constants.Key.SERVER_MODEL, Constants.serverModel.JU);
                    SpUtils.saveStr(Constants.Key.JU_SERVICE_IP_CACHE,mServiceIp);
                    SpUtils.saveStr(Constants.Key.JU_RESOURCE_PORT_CACHE, mResPort);
                    SpUtils.saveStr(Constants.Key.JU_XMPP_IP_CACHE, mCommunicationIp);
                    SpUtils.saveStr(Constants.Key.JU_XMPP_PORT_CACHE, mXmppPort);
                    SpUtils.saveStr(Constants.Key.JU_PROJECT_NAME_SUFFIX, mProName);
                }
                UIUtils.showTitleTip(getActivity(), APP.getContext().getResources().getString(R.string.setting_save_succ_please_restart));
            });
        }

        private void setServerInfo(int model) {
            String sIp = Constants.NetConfig.SERVICE_HOST;
            String sPort = Constants.NetConfig.SERVICE_PORT;
            String sPName = Constants.NetConfig.SERVICE_NAME;

            String cIp = Constants.NetConfig.COMMUNICATION_HOST;
            String cPort = Constants.NetConfig.COMMUNICATION_PORT;

            if (model == Constants.serverModel.YUN) {
                edtServiceIp.setText(sIp);
                edtResPort.setText(sPort);
                edtProName.setText(sPName);
                edtCommunicationIp.setText(cIp);
                edtXmppPort.setText(cPort);

                edtCommunicationIp.setEnabled(false);
                edtResPort.setEnabled(false);
                edtXmppPort.setEnabled(false);
                edtProName.setEnabled(false);
            } else {
                sIp = SpUtils.getStr(Constants.Key.JU_SERVICE_IP_CACHE);
                sPort = SpUtils.getStr(Constants.Key.JU_RESOURCE_PORT_CACHE);
                sPName = SpUtils.getStr(Constants.Key.JU_PROJECT_NAME_SUFFIX);
                cIp = SpUtils.getStr(Constants.Key.JU_XMPP_IP_CACHE);
                cPort = SpUtils.getStr(Constants.Key.JU_XMPP_PORT_CACHE);

                edtCommunicationIp.setEnabled(true);
                edtResPort.setEnabled(true);
                edtXmppPort.setEnabled(true);
                edtProName.setEnabled(true);

                edtServiceIp.setText(sIp);
                edtResPort.setText(sPort);
                edtProName.setText(sPName);
                edtCommunicationIp.setText(cIp);
                edtXmppPort.setText(cPort);
            }
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

        private String getWifiInfo(int type) {
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

        private static boolean isNullObject(Object object) {

            if (object == null) {
                return true;
            }

            return false;
        }

        private boolean isInternetConnected(Context context) {
            if (context != null) {
                ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mInternetNetWorkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
                boolean hasInternet = !isNullObject(mInternetNetWorkInfo) && mInternetNetWorkInfo.isConnected() && mInternetNetWorkInfo.isAvailable();
                return hasInternet;
            }
            return false;
        }

        private void showAlert(String title, String msg, Dialog.OnClickListener onClickListener, Dialog.OnClickListener onCancel, DialogInterface.OnDismissListener onDissmissListener) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(title);
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
            final Dialog dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.layout_set_pwd);

            Switch swPasswordEnabled = dialog.findViewById(R.id.sw_enabled_password);
            final EditText edtPwd = (EditText) dialog.findViewById(R.id.edt_set_pwd);
            final EditText edtPwd2 = (EditText) dialog.findViewById(R.id.edt_set_pwd_again);
            final Button btnCancel = (Button) dialog.findViewById(R.id.btn_pwd_cancel);
            final Button btnConfirm = (Button) dialog.findViewById(R.id.btn_pwd_confirm);

            boolean passwordEnabled = SpUtils.getBoolean(Constants.Key.PASSWORD_ENABLED,Constants.Default.PASSWORD_ENABLED);
            swPasswordEnabled.setChecked(passwordEnabled);
            swPasswordEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(Constants.Key.PASSWORD_ENABLED,isChecked));
            btnCancel.setOnClickListener(v -> dialog.dismiss());
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

                    Company company = SpUtils.getCompany();
                    int comid = company.getComid();
                    if(comid == Constants.NOT_BIND_COMPANY_ID){
                        UIUtils.showTitleTip(getActivity(), getString(R.string.setting_edit_password_success));
                        SpUtils.saveStr(SpUtils.MENU_PWD, pwd2);
                        dialog.dismiss();
                    } else {
                        btnCancel.setEnabled(false);
                        btnConfirm.setEnabled(false);
                        Map<String, String> params = new HashMap<>();
                        params.put("deviceNo", HeartBeatClient.getDeviceNo());
                        params.put("password", pwd2);
                        Timber.d("修改密码：" + ResourceUpdate.UPDATE_PWD);
                        Timber.d("参数:" + params.toString());
                        OkHttpUtils.post().url(ResourceUpdate.UPDATE_PWD).params(params).build().execute(new StringCallback() {
                            @Override
                            public void onBefore(Request request, int id) {
                                super.onBefore(request, id);
                                UIUtils.showNetLoading(getActivity());
                            }

                            @Override
                            public void onError(Call call, final Exception e, int id) {
                                Timber.d("错误：" + e);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        UIUtils.showTitleTip(getActivity(), getString(R.string.setting_edit_password_failed) + ":" + (e != null ? e.getMessage() : "NULL"));
                                    }
                                });
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                Timber.d("响应：" + response);
                                JSONObject jsonObject = JSONObject.parseObject(response);
                                final Integer status = jsonObject.getInteger("status");
                                getActivity().runOnUiThread(() -> {
                                    if (status == 1) {
                                        UIUtils.showTitleTip(getActivity(), getString(R.string.setting_edit_password_success));
                                        SpUtils.saveStr(SpUtils.MENU_PWD, pwd2);
                                        dialog.dismiss();
                                    } else {
                                        UIUtils.showTitleTip(getActivity(), getString(R.string.setting_edit_password_failed));
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

                }
            });

            dialog.show();
            Window window = dialog.getWindow();
            window.setWindowAnimations(R.style.mystyle);  //添加动画
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        ConfigLoader.save();
    }
}