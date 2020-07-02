package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yunbiao.faceview.FaceManager;
import com.yunbiao.faceview.FaceView;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.ReadCardUtils;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.DisplayOrientationEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.ResetLogoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateMediaEvent;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.business.LocateManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.business.VipDialogManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.utils.RestartAPPTool;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.xmpp.ServiceManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 此处只进行UI界面的更新以及人脸部分的初始化工作
 */

public class ThermalImage2Activity extends BaseThermal2Activity implements ThermalViewInterface {
    private static final String TAG = "ThermalImage2Activity";
    private ImageView ivMainLogo;//公司logo
    private TextView tvMainAbbName;//公司名

    // xmpp推送服务
    private ServiceManager serviceManager;

    //U口读卡器,类似于外接键盘
    private ReadCardUtils readCardUtils;

    //摄像头分辨率
    public static FaceView faceView;
    private ThermalAdsFragment adsFragment;
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
    private boolean showMainLogo;
    private boolean showMainThermal;
    private View slTestLayout;
    private TextView tvTemperTest;
    private TextView tvModelTest;
    private boolean titleEnabled;
    private View llMainLogoParent;

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
        if (Constants.DEVICE_TYPE == Constants.DeviceType.TEMPERATURE_CHECK_IN_215_INCH) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) faceView.getLayoutParams();
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        faceView.setCallback(this);
        faceView.enableMultiRetry(true);
        faceView.enableMultiCallback(true);
        faceView.setRetryTime(1);
        faceView.setRetryDelayTime(4000);

        llMainLogoParent = findViewById(R.id.ll_main_logo_parent);
        tvTemperTest = findViewById(R.id.tv_temper_test);
        tvModelTest = findViewById(R.id.tv_model_test);
        slTestLayout = findViewById(R.id.sl_test_layout);
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

        //加载签到列表Fragment
        signListFragment = new ThermalSignFragment();
        replaceFragment(R.id.ll_list_container, signListFragment);
    }

    @Override
    protected ThermalViewInterface setViewInterface() {
        return this;
    }

    @Override
    protected void onResume() {
        if (faceView != null) {
            faceView.resume();
        }
        titleEnabled = SpUtils.getBoolean(ThermalConst.Key.TITLE_ENABLED,ThermalConst.Default.TITLE_ENABLED);
        showMainThermal = SpUtils.getBoolean(ThermalConst.Key.SHOW_MAIN_THERMAL, ThermalConst.Default.SHOW_MAIN_THERMAL);
        showMainLogo = SpUtils.getBoolean(ThermalConst.Key.SHOW_MAIN_LOGO, ThermalConst.Default.SHOW_MAIN_LOGO);
        ivMainLogo.setVisibility(showMainLogo ? View.VISIBLE : View.GONE);
        tvMainAbbName.setVisibility(titleEnabled ? View.VISIBLE : View.GONE);
        llMainLogoParent.setVisibility(!showMainLogo && !titleEnabled ? View.GONE : View.VISIBLE);
        mShowDialog = SpUtils.getBoolean(ThermalConst.Key.SHOW_DIALOG, ThermalConst.Default.SHOW_DIALOG);
        personFrameEnable = SpUtils.getBoolean(ThermalConst.Key.PERSON_FRAME, ThermalConst.Default.PERSON_FRAME);
        //设置活体开关
        boolean livenessEnabled = SpUtils.getBoolean(Constants.Key.LIVENESS_ENABLED, Constants.Default.LIVENESS_ENABLED);
        if (faceView != null) {
            faceView.setLiveness(livenessEnabled);
        }
        llThermalArea.setVisibility(showMainThermal ? View.VISIBLE :View.GONE);

        initAds();
        super.onResume();

        setLogo(ivMainLogo,tvMainAbbName);
    }

    private void setLogo(ImageView logoView,TextView tvName) {
        boolean localPriority = SpUtils.getBoolean(ThermalConst.Key.LOCAL_PRIORITY, ThermalConst.Default.LOCAL_PRIORITY);
        if (localPriority) {
            String logoPath = SpUtils.getStr(ThermalConst.Key.MAIN_LOGO_IMG, ThermalConst.Default.MAIN_LOGO_IMG);
            if (TextUtils.isEmpty(logoPath)) {
                logoView.setImageResource(ThermalConst.Default.DEFAULT_LOGO_ID);
            } else {
                logoView.setImageBitmap(BitmapFactory.decodeFile(logoPath));
            }
            tvName.setText(SpUtils.getStr(ThermalConst.Key.MAIN_LOGO_TEXT, ThermalConst.Default.MAIN_LOGO_TEXT));
        } else {
            Company company = SpUtils.getCompany();
            String comlogo = company.getComlogo();
            String abbname = company.getAbbname();
            if(company.getComid() == Constants.NOT_BIND_COMPANY_ID || TextUtils.isEmpty(comlogo)){
                logoView.setVisibility(View.GONE);
                logoView.setImageBitmap(null);
            } else {
                Glide.with(this).load(comlogo).asBitmap().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        logoView.setImageBitmap(resource);
                    }
                });
            }
            tvName.setText(TextUtils.isEmpty(abbname) ? "" : abbname);
        }
    }

    @Override
    public void onModeChanged(boolean temperEnabled,boolean faceEnabled ,int temperModule) {
        //设置人脸间隔
        SignManager.instance().setVerifyDelay(faceEnabled && !temperEnabled ? 10000 : 0);

        //显示模式
        if (signListFragment != null) {
            String[] modules = getResources().getStringArray(R.array.temper_module);
            StringBuffer stringBuffer = new StringBuffer();
            if(faceEnabled && !temperEnabled){
                stringBuffer.append(getResString(R.string.only_label)).append("  ").append(getResString(R.string.face_label));
            } else if(temperEnabled && !faceEnabled){
                stringBuffer.append(getResString(R.string.only_label)).append("  ").append(getResString(R.string.temper_label));
            } else {
                stringBuffer.append(getResString(R.string.face_label)).append(" + ").append(getResString(R.string.temper_label));
            }
            stringBuffer.append("  ").append("(").append(modules[temperModule]).append(")");
            signListFragment.setModelText(stringBuffer.toString());
        }

        //仅人脸或不显示的时候隐藏全部
        if(!showMainThermal || (faceEnabled && !temperEnabled)){
            flDotFrame.setVisibility(View.GONE);
            tvTempTips.setVisibility(View.GONE);
            llThermalArea.setVisibility(View.GONE);
            ivBigHead.setVisibility(View.GONE);
        } else {
            switch (temperModule) {
                case TemperModuleType.HM_32_32:
                    ivInfaredImaging.setVisibility(View.GONE);
                    llThermalArea.setVisibility(View.VISIBLE);
                    ivThermalImaging.setVisibility(View.VISIBLE);
                    break;
                case TemperModuleType.HM_16_4:
                case TemperModuleType.MLX_16_4:
                case TemperModuleType.INFRARED:
                    ivThermalImaging.setVisibility(View.GONE);
                    llThermalArea.setVisibility(View.VISIBLE);
                    ivInfaredImaging.setVisibility(View.VISIBLE);
                    break;
                case TemperModuleType.SMT_32_32:
                    llThermalArea.setVisibility(View.GONE);
                    break;
            }
        }

        if (!(faceEnabled && !temperEnabled)) {
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
        if (!showMainThermal) return;

        if (ivInfaredImaging.isShown()) {
            ivInfaredImaging.setImageBitmap(bitmap);
            tvThermalTemper.setText(hasFace ? getResources().getString(R.string.main_thermal_has_person) : getResources().getString(R.string.main_thermal_no_person));
//            tvThermalTemper.setText(getResources().getString(R.string.main_thermal_temp) + temper + "℃");
        } else if (ivThermalImaging.isShown()) {
            ivThermalImaging.setImageBitmap(bitmap);
            tvThermalTemper.setText(getResources().getString(R.string.main_thermal_temp) + temper + "℃");
        }
    }

    @Override
    protected boolean isTesting() {
        boolean b = slTestLayout != null && slTestLayout.isShown();
        if(b){
            if (stringBuffer == null) {
                stringBuffer = new StringBuffer();
            }
        } else {
            if(stringBuffer != null){
                stringBuffer.setLength(0);
                stringBuffer = null;
            }
        }
        return b;
    }

    StringBuffer stringBuffer = new StringBuffer();
    @Override
    protected void testTemper(float originT, float afterT, float ambientF) {
        String s = "原始温度：" + originT + "，处理温度：" + afterT + "，环境温度：" + ambientF + "\n";
        stringBuffer.insert(0,s);
        if(stringBuffer.length() > 10000){
            stringBuffer.replace(0,5000,"");
        }
        tvTemperTest.post(() -> tvTemperTest.setText(stringBuffer.toString()));
    }

    @Override
    protected void testModel(int currTemperMode) {
        tvModelTest.post(() -> {
            switch (currTemperMode) {
                case 0:
                    tvModelTest.setText("低温模式");
                    break;
                case 1:
                    tvModelTest.setText("常温模式");
                    break;
                case 2:
                    tvModelTest.setText("高温模式");
                    break;
            }
        });
    }

    @Override
    public void hasFace(boolean hasFace) {
        if (hasFace) {
            //检测到人后开灯
            onLight();
            //收起海报界面
            if (adsFragment != null) {
                adsFragment.detectFace();
            }
        } else {
            //没人时第一次进入只重试两次
            if (faceView != null) {
                faceView.setRetryTime(1);
            }
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
        if (faceView != null) {
            faceView.setRetryTime(4);
        }
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
        if (tvTips.isShown()) {
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
        if (tvTips.isShown()) {
            tvTips.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateSignList(Sign sign) {
        if (signListFragment != null) {
            signListFragment.addSignData(sign);
        }
        if (mShowDialog) {
            VipDialogManager.showVipDialog(this, sign);
        }
    }

    @Override
    public View getDistanceView() {
        return faceDistanceView;
    }

    @Override
    public Bitmap getFacePicture() {
        return faceView != null ? faceView.takePicture() : null;
    }

    @Override
    public Rect getRealRect(Rect faceRect) {
        return faceView != null ? faceView.getRealRect(faceRect) : new Rect();
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
            setImageByResId(ivBigHead, R.mipmap.big_head);
        }
    }

    private void setBigHeadRealLine() {
        if (!personFrameEnable) {
            return;
        }
        isRealLine = true;
        setImageByResId(ivBigHead, R.mipmap.big_head_real_line);
    }

    /**
     *
     **************************************************************************************************/
    private void initAds() {
        boolean isPosterEnabled = SpUtils.getBoolean(Constants.Key.POSTER_ENABLED, Constants.Default.POSTER_ENABLED);//大屏海报开关
        if (isPosterEnabled) {
            if (adsFragment != null && adsFragment.isAdded()) {
                return;
            }
            //加载广告Fragment
            adsFragment = new ThermalAdsFragment();
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
        setLogo(ivMainLogo,tvMainAbbName);
        EventBus.getDefault().post(new UpdateMediaEvent());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(ResetLogoEvent event) {
        setLogo(ivMainLogo,tvMainAbbName);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(DisplayOrientationEvent event) {
        Log.e(TAG, "update: 收到摄像头更新事件");
        if (faceView != null) {
            faceView.changeAngle();
        }
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
        if (faceView != null) {
            faceView.setSimilarThreshold();
        }
    }

    public void goSetting() {
        boolean passwordEnabled = SpUtils.getBoolean(Constants.Key.PASSWORD_ENABLED,Constants.Default.PASSWORD_ENABLED);
        String pwd = SpUtils.getStr(SpUtils.MENU_PWD);
        if(passwordEnabled && !TextUtils.isEmpty(pwd)){
            inputPwd(() -> startActivity(new Intent(ThermalImage2Activity.this, ThermalSystemActivity.class)));
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
        RestartAPPTool.showExitDialog(this,
                (dialog, which) -> onBackKeyPressed(() -> moveTaskToBack(true)),
                (dialog, which) -> onBackKeyPressed(() -> APP.exit()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (faceView != null) {
            faceView.pause();
        }
        Log.e(TAG, "onPause: 执行了onPause");
        if (isFinishing()) {
            Log.e(TAG, "onPause: 该Activity正在结束");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop: 执行了onStop");
        if (isFinishing()) {
            Log.e(TAG, "onStop: 该Activity正在结束");
        }
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy: 执行了销毁");
        Log.e(TAG, "onDestroy: 执行了销毁");
        if (readCardUtils != null) {
            readCardUtils.removeScanSuccessListener();
            readCardUtils = null;
        }

        if (faceView != null) {
            faceView.destory();
        }
        destoryXmpp();

        EventBus.getDefault().unregister(this);
        SyncManager.instance().destory();
        KDXFSpeechManager.instance().destroy();
        LocateManager.instance().destory();
        super.onDestroy();
    }
}