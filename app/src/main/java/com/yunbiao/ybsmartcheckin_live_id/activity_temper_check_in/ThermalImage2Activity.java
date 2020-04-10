package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbiao.faceview.FaceManager;
import com.yunbiao.faceview.FaceView;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.ReadCardUtils;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.DisplayOrientationEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.ResetLogoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateMediaEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.AdsFragment;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.business.LocateManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.business.VipDialogManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.serialport.InfraredTemperatureUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.RestartAPPTool;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.views.ImageFileLoader;
import com.yunbiao.ybsmartcheckin_live_id.xmpp.ServiceManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

/**
 * 此处只进行UI界面的更新以及人脸部分的初始化工作
 */

public class ThermalImage2Activity extends BaseThermal2Activity implements ThermalViewInterface {
    private static final String TAG = "WelComeActivity";
    private ImageView ivMainLogo;//公司logo
    private TextView tvMainAbbName;//公司名

    // xmpp推送服务
    private ServiceManager serviceManager;

    //U口读卡器,类似于外接键盘
    private ReadCardUtils readCardUtils;

    //摄像头分辨率
    public static FaceView faceView;
    private AdsFragment adsFragment;
    private ThermalSignFragment signListFragment;
    private TextView tvTempTips;

    //判断是否开启测温
    private ImageView ivThermalImaging;
    private TextView tvThermalTemper;
    private View llThermalArea;
    private View faceDistanceView;
    private View flDotFrame;
    private ImageView ivBigHead;
    private boolean personFrameEnable;
    private ImageView ivInfaredImaging;
    private boolean mShowDialog;
    private TextView tvTips;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_thermal_image;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_thermal_image_h;
    }

    @Override
    protected void initView() {
        APP.setMainActivity(this);
        EventBus.getDefault().register(this);
        faceView = findViewById(R.id.face_view);//人脸识别
        faceView.setCallback(this);
        ivMainLogo = findViewById(R.id.iv_main_logo);//LOGO
        tvMainAbbName = findViewById(R.id.tv_main_abbname);//公司名
        faceDistanceView = findViewById(R.id.view_face_distance);//人脸距离框
        flDotFrame = findViewById(R.id.fl_dot_frame);//红点测温框
        ivBigHead = findViewById(R.id.iv_big_head);//人像测温框
        tvTempTips = findViewById(R.id.tv_temp_tips_main);//温度提示
        tvTips = findViewById(R.id.tv_tips_main);
        ivInfaredImaging = findViewById(R.id.iv_infared_imaging_main);
        ivThermalImaging = findViewById(R.id.iv_thermal_imaging_main);//热成像图像显示
        tvThermalTemper = findViewById(R.id.tv_thermal_temper_main);//热成像温度显示
        llThermalArea = findViewById(R.id.ll_thermal_area_main);//热成像数据区域

        faceView.enableMultiRetry(true);
        faceView.enableMultiCallback(true);
        faceView.setRetryTime(4);
        faceView.setRetryDelayTime(4000);

        //加载签到列表Fragment
        signListFragment = new ThermalSignFragment();
        replaceFragment(R.id.ll_list_container, signListFragment);

        if (Constants.isHT) {
            ImageFileLoader.setDefaultLogoId(R.mipmap.logo_icon_horizontal);
            ivMainLogo.setImageResource(R.mipmap.logo_icon_horizontal);
            setImageByResId(ivMainLogo, R.mipmap.logo_icon_horizontal);
        } else if(Constants.isSK){
            ImageFileLoader.setDefaultLogoId(R.mipmap.icon_logo3);
            ivMainLogo.setImageResource(R.mipmap.icon_logo3);
            setImageByResId(ivMainLogo, R.mipmap.icon_logo3);
        } else {
            ImageFileLoader.setDefaultLogoId(R.mipmap.logo);
            ivMainLogo.setImageResource(R.mipmap.logo);
            setImageByResId(ivMainLogo, R.mipmap.logo);
        }
    }

    @Override
    protected ThermalViewInterface setViewInterface() {
        return this;
    }

    @Override
    protected void onResume() {
        faceView.resume();

        mShowDialog = SpUtils.getBoolean(ThermalConst.Key.SHOW_DIALOG,ThermalConst.Default.SHOW_DIALOG);
        personFrameEnable = SpUtils.getBoolean(ThermalConst.Key.PERSON_FRAME, ThermalConst.Default.PERSON_FRAME);
        //设置活体开关
        boolean livenessEnabled = SpUtils.getBoolean(SpUtils.LIVENESS_ENABLED, false);
        faceView.setLiveness(livenessEnabled);

        initAds();
        super.onResume();
    }


    @Override
    public void onModeChanged(int mode) {
        if (signListFragment != null) {
            signListFragment.setModelText(ThermalConst.models[mode]);
        }

        //设置人脸间隔
        SignManager.instance().setVerifyDelay(mode == Constants.Model.MODEL_FACE_ONLY ? 10000 : 0);

        switch (mode) {
            case ThermalConst.FACE_ONLY:
                flDotFrame.setVisibility(View.GONE);
                tvTempTips.setVisibility(View.GONE);
                llThermalArea.setVisibility(View.GONE);
                ivBigHead.setVisibility(View.GONE);
                break;
            case ThermalConst.THERMAL_ONLY:
            case ThermalConst.FACE_THERMAL:
                ivInfaredImaging.setVisibility(View.GONE);
                llThermalArea.setVisibility(View.VISIBLE);
                ivThermalImaging.setVisibility(View.VISIBLE);
                break;
            case ThermalConst.INFRARED_ONLY:
            case ThermalConst.FACE_INFRARED:
            case ThermalConst.THERMAL_16_4_ONLY:
            case ThermalConst.FACE_THERMAL_16_4:
                ivThermalImaging.setVisibility(View.GONE);
                llThermalArea.setVisibility(View.VISIBLE);
                ivInfaredImaging.setVisibility(View.VISIBLE);
                break;
        }

        //切换头像框
        if (mCurrMode != ThermalConst.FACE_ONLY) {
            if (personFrameEnable) {
                ivBigHead.setVisibility(View.VISIBLE);
                flDotFrame.setVisibility(View.GONE);
            } else {
                ivBigHead.setVisibility(View.GONE);
                flDotFrame.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onFaceViewReady() {
        //初始化人脸库
        FaceManager.getInstance().init(APP.getContext());

        SyncManager.instance().requestCompany();
    }

    @Override
    public void updateHotImage(Bitmap bitmap, float temper, boolean hasFace) {
        if (ivInfaredImaging.isShown()) {
            ivInfaredImaging.setImageBitmap(bitmap);
            tvThermalTemper.setText(hasFace ? getResources().getString(R.string.main_thermal_has_person) : getResources().getString(R.string.main_thermal_no_person));
        } else if (ivThermalImaging.isShown()) {
            ivThermalImaging.setImageBitmap(bitmap);
            tvThermalTemper.setText(getResources().getString(R.string.main_thermal_temp) + temper + "℃");
        }
    }

    @Override
    public void hasFace(boolean hasFace) {
        if(hasFace){
            //检测到人后开灯
            onLight();
            //收起海报界面
            if (adsFragment != null) {
                adsFragment.detectFace();
            }
        } else {
            //没人时第一次进入只重试两次
            faceView.setRetryTime(2);
        }
    }

    @Override
    public void showResult(String tip, int id) {
        if (!tvTempTips.isShown()) {
            tvTempTips.setVisibility(View.VISIBLE);
        }
        setBigHeadRealLine();
        tvTempTips.setBackgroundResource(id);
        tvTempTips.setText(tip);
        //在弹出结果后将重试次数置为5
        faceView.setRetryTime(5);
    }

    @Override
    public void dismissResult() {
        if (tvTempTips.isShown()) {
            tvTempTips.setVisibility(View.GONE);
        }
        setBigHeadDottedLine();
    }

    @Override
    public void clearAllUI() {
        if (tvTempTips.isShown()) {
            tvTempTips.setVisibility(View.GONE);
        }
        if(tvTips.isShown()){
            tvTips.setVisibility(View.GONE);
        }
        setBigHeadDottedLine();
    }

    @Override
    public void showTips(String tip, int stableTipsId) {
        if (!tvTips.isShown()) {
            tvTips.setVisibility(View.VISIBLE);
        }
        tvTips.setText(tip);
        tvTips.setBackgroundResource(stableTipsId);
    }

    @Override
    public void dismissTips() {
        if(tvTips.isShown()){
            tvTips.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateSignList(Sign sign) {
        if(signListFragment != null){
            signListFragment.addSignData(sign);
        }
        if(mShowDialog){
            VipDialogManager.showVipDialog(this,sign);
        }
    }

    @Override
    public View getDistanceView() {
        return faceDistanceView;
    }

    @Override
    public Bitmap getFacePicture() {
        Bitmap bitmap = faceView.takePicture();
        if(bitmap == null){
            bitmap = faceView.getCurrCameraFrame();
        }
        return bitmap;
    }

    @Override
    public Rect getRealRect(Rect faceRect) {
        return faceView.getRealRect(faceRect);
    }

    @Override
    public boolean isTipsShown() {
        return tvTips.isShown();
    }

    @Override
    public boolean isResultShown() {
        return tvTempTips.isShown();
    }

    private boolean isRealLine = false;

    private void setBigHeadDottedLine() {
        if (!personFrameEnable) {
            return;
        }
        if (isRealLine) {
            isRealLine = false;
            setImageByResId(ivBigHead,R.mipmap.big_head);
        }
    }

    private void setBigHeadRealLine() {
        if (!personFrameEnable) {
            return;
        }
        isRealLine = true;
        setImageByResId(ivBigHead,R.mipmap.big_head_real_line);
    }

    /**
     *
     **************************************************************************************************/
    private void initAds() {
        boolean isPosterEnabled = SpUtils.getBoolean(SpUtils.POSTER_ENABLED, Constants.DEFAULT_POSTER_ENABLED);//大屏海报开关
        if (isPosterEnabled) {
            if (adsFragment != null && adsFragment.isAdded()) {
                return;
            }
            //加载广告Fragment
            adsFragment = new AdsFragment();
            addFragment(R.id.ll_face_main, adsFragment);
        } else {
            removeFragment(adsFragment);
            adsFragment = null;
        }
    }

    @Override
    protected void initData() {
        super.initData();
        KDXFSpeechManager.instance().init(this);

        //开启Xmpp
        startXmpp();
        //初始化定位工具
        LocateManager.instance().init(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent event) {
        Company company = SpUtils.getCompany();
        if (tvMainAbbName != null) tvMainAbbName.setText(company.getAbbname());

        EventBus.getDefault().post(new UpdateMediaEvent());

        ImageFileLoader.i().loadAndSave(this, company.getComlogo(), Constants.DATA_PATH, ivMainLogo);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(ResetLogoEvent event) {
        Company company = SpUtils.getCompany();
        ImageFileLoader.i().loadAndSave(this, company.getComlogo(), Constants.DATA_PATH, ivMainLogo);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(DisplayOrientationEvent event) {
        Log.e(TAG, "update: 收到摄像头更新事件");
        faceView.changeAngle();
    }

    private void startXmpp() {//开启xmpp
        serviceManager = new ServiceManager(this);
        serviceManager.startService();
    }

    private void destoryXmpp() {
        if (serviceManager != null) {
            serviceManager.stopService();
            serviceManager = null;
        }
    }

    //密码弹窗
    private void inputPwd(final Runnable runnable) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_input_pwd);

        final Animation animation = AnimationUtils.loadAnimation(ThermalImage2Activity.this, R.anim.anim_edt_shake);
        final View rootView = dialog.findViewById(R.id.ll_input_pwd);
        Button btnConfirm = (Button) dialog.findViewById(R.id.btn_input_confirm);
        final EditText edtPwd = (EditText) dialog.findViewById(R.id.edt_input_pwd);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = edtPwd.getText().toString();
                if (TextUtils.isEmpty(pwd)) {
                    edtPwd.setError(getString(R.string.act_wel_error_bywjsrmmo));
                    rootView.startAnimation(animation);
                    return;
                }
                String spPwd = SpUtils.getStr(SpUtils.MENU_PWD);
                if (!TextUtils.equals(pwd, spPwd)) {
                    edtPwd.setError(getString(R.string.act_wel_error_mmclqcxsrb));
                    rootView.startAnimation(animation);
                    return;
                }
                if (runnable != null) {
                    runnable.run();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int width = dm.widthPixels;
        final Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.mystyle);  //添加动画
        window.setLayout(width / 2, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);
    }

    public void setFaceViewSimilar() {
        Log.e(TAG, "setFaceViewSimilar: 设置人脸识别阈值");
        faceView.setSimilarThreshold();
    }

    private void goSetting() {
        String pwd = SpUtils.getStr(SpUtils.MENU_PWD);
        if (!TextUtils.isEmpty(pwd)) {
            inputPwd(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(ThermalImage2Activity.this, ThermalSystemActivity.class));
                }
            });
            return;
        }
        startActivity(new Intent(ThermalImage2Activity.this, ThermalSystemActivity.class));
    }

    private void onBackKeyPressed(Runnable runnable) {
        String pwd = SpUtils.getStr(SpUtils.MENU_PWD);
        if (!TextUtils.isEmpty(pwd)) {
            inputPwd(runnable);
            return;
        }
        if (runnable != null) {
            runnable.run();
        }
    }

    //跳转设置界面
    public void goSetting(View view) {
        goSetting();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            goSetting();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        RestartAPPTool.showExitDialog(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onBackKeyPressed(new Runnable() {
                    @Override
                    public void run() {
                        moveTaskToBack(true);
                    }
                });
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onBackKeyPressed(new Runnable() {
                    @Override
                    public void run() {
                        APP.exit();
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        faceView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (readCardUtils != null) {
            readCardUtils.removeScanSuccessListener();
            readCardUtils = null;
        }

        InfraredTemperatureUtils.getIns().closeSerialPort();
        faceView.destory();
        destoryXmpp();

        EventBus.getDefault().unregister(this);
        SyncManager.instance().destory();
        KDXFSpeechManager.instance().destroy();
        LocateManager.instance().destory();
    }
}