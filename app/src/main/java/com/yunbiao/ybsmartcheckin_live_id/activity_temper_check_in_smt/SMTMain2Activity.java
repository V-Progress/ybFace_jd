package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in_smt;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yunbiao.faceview.FaceManager;
import com.yunbiao.faceview.FaceView;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.FlavorType;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.DisplayOrientationEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.ResetLogoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateMediaEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.AdsFragment;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in.ThermalConst;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.LocateManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.utils.RestartAPPTool;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.views.ImageFileLoader;
import com.yunbiao.ybsmartcheckin_live_id.xmpp.ServiceManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;

public class SMTMain2Activity extends SMTTempBase2Activity implements SMTViewInterface {
    private static final String TAG = "SMTMain2Activity";
    @BindView(R.id.face_view)
    FaceView faceView;
    @BindView(R.id.view_face_distance)
    View viewFaceDistance;
    @BindView(R.id.iv_main_logo)
    ImageView ivMainLogo;
    @BindView(R.id.tv_main_abbname)
    TextView tvMainAbbname;
    @BindView(R.id.ll_clock_area)
    LinearLayout llClockArea;
    @BindView(R.id.rl_top_area)
    RelativeLayout rlTopArea;
    @BindView(R.id.iv_big_head)
    ImageView ivBigHead;
    @BindView(R.id.fl_dot_frame)
    RelativeLayout flDotFrame;
    @BindView(R.id.tv_tips_main)
    TextView tvTipsMain;
    @BindView(R.id.tv_temp_tips_main)
    TextView tvTempTipsMain;
    @BindView(R.id.rl_tip1)
    RelativeLayout rlTip1;
    @BindView(R.id.ll_list_container)
    FrameLayout llListContainer;
    @BindView(R.id.ll_face_main)
    FrameLayout llFaceMain;
    private AdsFragment adsFragment;
    private SMTSignFragment signListFragment;
    private boolean personFrameEnable = true;
    private ServiceManager serviceManager;

    @Override
    protected int getLayout() {
        return R.layout.activity_smt_device2;
    }

    @Override
    protected void initView() {
        APP.setMainActivity(this);
        EventBus.getDefault().register(this);

        faceView.setCallback(faceCallback);
        faceView.enableMultiRetry(true);
        faceView.enableMultiCallback(true);
        faceView.setRetryTime(1);
        faceView.setRetryDelayTime(4000);

        //加载签到列表Fragment
        signListFragment = new SMTSignFragment();
        replaceFragment(R.id.ll_list_container, signListFragment);

        if (Constants.FLAVOR_TYPE == FlavorType.HT) {
            ImageFileLoader.setDefaultLogoId(R.mipmap.logo_icon_horizontal);
            setImageByResId(ivMainLogo, R.mipmap.logo_icon_horizontal);
        } else if (Constants.FLAVOR_TYPE == FlavorType.SK) {
            ImageFileLoader.setDefaultLogoId(R.mipmap.icon_logo3);
            setImageByResId(ivMainLogo, R.mipmap.icon_logo3);
        } else if (Constants.FLAVOR_TYPE == FlavorType.OSIMLE) {
            ImageFileLoader.setDefaultLogoId(R.mipmap.osimle_logo);
            setImageByResId(ivMainLogo, R.mipmap.osimle_logo);
        } else if (Constants.FLAVOR_TYPE == FlavorType.SOFT_WORK_Z) {
            ImageFileLoader.setDefaultLogoId(R.mipmap.softworkz_logo);
            setImageByResId(ivMainLogo, R.mipmap.softworkz_logo);
        } else if (Constants.FLAVOR_TYPE == FlavorType.BIO) {
//            ImageFileLoader.setDefaultLogoId(0);
//            ivMainLogo.setImageBitmap(null);
        } else {
            ImageFileLoader.setDefaultLogoId(R.mipmap.yb_logo);
            setImageByResId(ivMainLogo, R.mipmap.yb_logo);
        }
    }

    @Override
    protected SMTViewInterface setViewInterface() {
        return this;
    }

    @Override
    protected void onResume() {
        faceView.resume();

        //设置活体开关
        boolean livenessEnabled = SpUtils.getBoolean(SpUtils.LIVENESS_ENABLED, false);
        faceView.setLiveness(livenessEnabled);

        initAds();

        super.onResume();
    }

    @Override
    protected void setMaskDetectEnabled(boolean isMaskDetectEnabled) {
    }

    @Override
    public void onFaceViewReady() {
        //初始化人脸库
        FaceManager.getInstance().init(APP.getContext());

        SyncManager.instance().requestCompany();
    }

    @Override
    public void onModeChanged(int mode) {
        if (signListFragment != null) {
            signListFragment.setModelText(SMTModelConst.models[mode]);
        }

        //设置人脸间隔
        SignManager.instance().setVerifyDelay(mode == ThermalConst.FACE_ONLY ? 10000 : 0);

        //切换头像框
        if (mode != ThermalConst.FACE_ONLY) {
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
    public void updateHotImage(Bitmap bitmap, float temper, boolean hasFace) {

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
            faceView.setRetryTime(1);
        }
    }

    @Override
    public void showResult(String tip, int id) {
        if (!tvTempTipsMain.isShown()) {
            tvTempTipsMain.setVisibility(View.VISIBLE);
        }
        setBigHeadRealLine();
        tvTempTipsMain.setBackgroundResource(id);
        tvTempTipsMain.setText(tip);
        //在弹出结果后将重试次数置为5
        faceView.setRetryTime(3);
    }

    @Override
    public void dismissResult() {
        if (tvTempTipsMain.isShown()) {
            tvTempTipsMain.setVisibility(View.GONE);
        }
        setBigHeadDottedLine();
    }

    @Override
    public void clearAllUI() {
        if (tvTempTipsMain.isShown()) {
            tvTempTipsMain.setVisibility(View.GONE);
        }
        if(tvTipsMain.isShown()){
            tvTipsMain.setVisibility(View.GONE);
        }
        setBigHeadDottedLine();
    }

    @Override
    public void showTips(String tip, int stableTipsId) {
        if (!tvTipsMain.isShown()) {
            tvTipsMain.setVisibility(View.VISIBLE);
        }
        tvTipsMain.setText(tip);
        tvTipsMain.setBackgroundResource(stableTipsId);
    }

    @Override
    public void dismissTips() {
        if(tvTipsMain.isShown()){
            tvTipsMain.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateSignList(Sign sign) {
//        if(mShowDialog){
//            VipDialogManager.showVipDialog(this,sign);
//        }
    }

    @Override
    public View getDistanceView() {
        return viewFaceDistance;
    }

    @Override
    public Bitmap getFacePicture() {
        return faceView.takePicture();
    }

    @Override
    public Rect getRealRect(Rect faceRect) {
        return faceView.getRealRect(faceRect);
    }

    @Override
    public boolean isTipsShown() {
        return tvTipsMain.isShown();
    }

    @Override
    public boolean isMaskTipsShown() {
        return false;
    }

    @Override
    public boolean isResultShown() {
        return tvTempTipsMain.isShown();
    }

    @Override
    public void showMaskTip(String tip) {
    }

    @Override
    public void clearMaskTip() {
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

        startXmpp();

        LocateManager.instance().init(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent event) {
        Company company = SpUtils.getCompany();
        if (tvMainAbbname != null) tvMainAbbname.setText(company.getAbbname());

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

        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_edt_shake);
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
                    startActivity(new Intent(SMTMain2Activity.this, SMTSystemActivity.class));
                }
            });
            return;
        }
        startActivity(new Intent(SMTMain2Activity.this, SMTSystemActivity.class));
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
        faceView.destory();
        destoryXmpp();

        EventBus.getDefault().unregister(this);
        SyncManager.instance().destory();
        LocateManager.instance().destory();
    }
}
