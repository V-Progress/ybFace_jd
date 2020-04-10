package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
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

import com.yunbiao.faceview.CompareResult;
import com.yunbiao.faceview.FacePreviewInfo;
import com.yunbiao.faceview.FaceView;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.ReadCardUtils;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.DisplayOrientationEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.ResetLogoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateMediaEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseGpioActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.AdsFragment;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.InformationFragment;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.SignFragment;
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
import com.yunbiao.ybsmartcheckin_live_id.views.ImageFileLoader;
import com.yunbiao.ybsmartcheckin_live_id.xmpp.ServiceManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by Administrator on 2018/11/26.
 */

public class WelComeActivity extends BaseGpioActivity {
    private static final String TAG = "WelComeActivity";
    private ImageView ivMainLogo;//公司logo
    private TextView tvMainAbbName;//公司名
    private TextView tvMainTopTitle;//标题
    private TextView tvMainBottomTitle;//底部标题

    // xmpp推送服务
    private ServiceManager serviceManager;

    //U口读卡器,类似于外接键盘
    private ReadCardUtils readCardUtils;

    //摄像头分辨率
    public static FaceView faceView;
    private AdsFragment adsFragment;
    private SignFragment signListFragment;

    //判断是否开启测温
    private boolean isPosterEnabled;//大屏海报是否开启

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_welcome;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_welcome_h;
    }

    @Override
    protected void initView() {
        APP.setMainActivity(this);
        EventBus.getDefault().register(this);
        faceView = findViewById(R.id.face_view);
        faceView.setCallback(faceCallback);
        faceView.enableMutiple(true);

        ivMainLogo = findViewById(R.id.iv_main_logo);
        tvMainAbbName = findViewById(R.id.tv_main_abbname);
        tvMainTopTitle = findViewById(R.id.tv_main_topTitle);
        tvMainBottomTitle = findViewById(R.id.tv_main_bottomTitle);

        //加载签到列表Fragment
        signListFragment = new SignFragment();
        replaceFragment(R.id.ll_list_container, signListFragment);

//        只有竖屏情况下加载信息展示Fragment
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            InformationFragment informationFragment = new InformationFragment();
            replaceFragment(R.id.layout_h, informationFragment);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ivMainLogo.setClickable(true);
        isPosterEnabled = SpUtils.getBoolean(SpUtils.POSTER_ENABLED, Constants.DEFAULT_POSTER_ENABLED);//大屏海报开关

        //设置人脸间隔
        SignManager.instance().setVerifyDelay(10000);

        //设置活体开关
        boolean livenessEnabled = SpUtils.getBoolean(SpUtils.LIVENESS_ENABLED, false);
        faceView.setLiveness(livenessEnabled);

        faceView.resume();

        initAds();
    }

    /*****识别相关回调******************************************************************************************/
    private com.yunbiao.faceview.FaceView.FaceCallback faceCallback = new com.yunbiao.faceview.FaceView.FaceCallback() {
        @Override
        public void onReady() {
            SyncManager.instance().requestCompany();
        }

        @Override
        public void onFaceDetection(Boolean hasFace, List<FacePreviewInfo> facePreviewInfoList) {
            if(!hasFace){
                return;
            }
            //检测到人后开灯
            onLight();
            //收起海报界面
            if (adsFragment != null) {
                adsFragment.detectFace();
            }
        }

        @Override
        public boolean onFaceDetection(boolean hasFace, FacePreviewInfo facePreviewInfo) {
            return true;
        }

        @Override
        public void onFaceVerify(CompareResult compareResult) {
            //======以下是普通识别流程====================================
            if (compareResult == null || compareResult.getSimilar() == -1) {
                return;
            }
            Sign sign = SignManager.instance().checkSignData(compareResult, 0f);
            if (sign == null) {
                return;
            }
            if (signListFragment != null) {
                signListFragment.addSignData(sign);
            }

            if (SpUtils.getBoolean(SpUtils.FACE_DIALOG, false)) {
                VipDialogManager.showVipDialog(WelComeActivity.this, sign);
            }

            KDXFSpeechManager.instance().playNormal(sign.getName());

            if (sign.getType() == -2) {
                return;
            }

            openDoor();
        }
    };

    private ReadCardUtils.OnReadSuccessListener readCardListener = new ReadCardUtils.OnReadSuccessListener() {
        @Override
        public void onScanSuccess(String barcode) {
            Log.e(TAG, "barcode: " + barcode);
            Sign sign = SignManager.instance().checkSignForCard(barcode);

            if (signListFragment != null) {
                signListFragment.addSignData(sign);
            }

            if (SpUtils.getBoolean(SpUtils.FACE_DIALOG, false)) {
                VipDialogManager.showVipDialog(WelComeActivity.this, sign);
            }

            KDXFSpeechManager.instance().playNormal(sign.getName());

            if (sign.getType() == -2) {
                return;
            }

            openDoor();
        }
    };

    /****************************************************************************************************/
    private void initAds() {
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

    /**
     * 读卡器初始化
     */
    private void initCardReader() {
        //读卡器声明
        readCardUtils = new ReadCardUtils();
        readCardUtils.setReadSuccessListener(readCardListener);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        /*if (ReadCardUtils.isInputFromReader(this, event)) {
            if (readCardUtils != null) {
                readCardUtils.resolveKeyEvent(event);
            }
        }*/
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void initData() {
        initCardReader();

        KDXFSpeechManager.instance().init(this);

        //开启Xmpp
        startXmpp();
        //初始化定位工具
        LocateManager.instance().init(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent event) {

        Log.e(TAG, "update: 11111111111111");
        Company company = SpUtils.getCompany();
        if (tvMainAbbName != null) tvMainAbbName.setText(company.getAbbname());
        if (tvMainTopTitle != null) tvMainTopTitle.setText(company.getToptitle());
        if (tvMainBottomTitle != null) tvMainBottomTitle.setText(company.getBottomtitle());

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

        final Animation animation = AnimationUtils.loadAnimation(WelComeActivity.this, R.anim.anim_edt_shake);
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
                    startActivity(new Intent(WelComeActivity.this, SystemActivity.class));
                }
            });
            return;
        }
        startActivity(new Intent(WelComeActivity.this, SystemActivity.class));
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
        view.setClickable(false);
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

        faceView.destory();
        destoryXmpp();

        EventBus.getDefault().unregister(this);
        SyncManager.instance().destory();
        KDXFSpeechManager.instance().destroy();
        LocateManager.instance().destory();
    }
}