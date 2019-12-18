package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
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
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.ReadCardUtils;
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
import com.yunbiao.ybsmartcheckin_live_id.common.UpdateVersionControl;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.User;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.utils.RestartAPPTool;
import com.yunbiao.ybsmartcheckin_live_id.utils.ScreenShotUtil;
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

    //摄像头分辨率
    public static com.yunbiao.faceview.FaceView faceView;
    private AdsFragment adsFragment;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_welcome;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_welcome_h;
    }

    public void testScreen(View view){
        final ScreenShotUtil instance = ScreenShotUtil.getInstance();
        instance.takeScreenshot(APP.getContext(), new ScreenShotUtil.ScreenShotCallback() {
            @Override
            public void onShotted(boolean isSucc, String filePath) {
                String sid = HeartBeatClient.getDeviceNo();
                instance.sendCutFinish(sid,filePath);
            }
        });
    }

    @Override
    protected void initView() {
        APP.setActivity(this);
        EventBus.getDefault().register(this);
        faceView = findViewById(R.id.face_view);
        faceView.setCallback(faceCallback);
        ivMainLogo = findViewById(R.id.iv_main_logo);
        tvMainAbbName = findViewById(R.id.tv_main_abbname);
        tvMainTopTitle = findViewById(R.id.tv_main_topTitle);
        tvMainBottomTitle = findViewById(R.id.tv_main_bottomTitle);

        //加载签到列表Fragment
        SignFragment signListFragment = new SignFragment();
        replaceFragment(R.id.ll_list_container, signListFragment);

//        只有竖屏情况下加载信息展示Fragment
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            InformationFragment informationFragment = new InformationFragment();
            replaceFragment(R.id.layout_h, informationFragment);
        }

        //加载广告Fragment
        adsFragment = new AdsFragment();
        addFragment(R.id.ll_face_main, adsFragment);
    }

    //U口读卡器,类似于外接键盘
    private ReadCardUtils readCardUtils;

    /**
     * 读卡器初始化
     */
    private void initCardReader() {
        //读卡器声明
        readCardUtils = new ReadCardUtils();
        readCardUtils.setReadSuccessListener(new ReadCardUtils.OnReadSuccessListener() {
            @Override
            public void onScanSuccess(String barcode) {
                Log.e(TAG, "barcode: " + barcode);
                SignManager.instance().checkSign(barcode);
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (ReadCardUtils.isInputFromReader(this, event)) {
            if (readCardUtils != null){
                readCardUtils.resolveKeyEvent(event);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void initData() {
        initCardReader();

        //开启Xmpp
        startXmpp();
        //初始化定位工具
        LocateManager.instance().init(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "run: ------- ");
                UpdateVersionControl.getInstance().checkUpdate(WelComeActivity.this);
            }
        }, 5 * 1000);
    }

    private com.yunbiao.faceview.FaceView.FaceCallback faceCallback = new com.yunbiao.faceview.FaceView.FaceCallback() {
        @Override
        public void onReady() {
            SyncManager.instance().requestCompany();
        }

        @Override
        public void onFaceDetection(Boolean hasFace, List<FacePreviewInfo> facePreviewInfoList) {
            if(hasFace){
                onLight();
                if(adsFragment != null){
                    adsFragment.detectFace();
                }
            }
        }

        @Override
        public void onFaceVerify(CompareResult faceAuth) {
            if (faceAuth != null){
                SignManager.instance().checkSign(faceAuth);
            }
        }
    };

    /*人脸识别回调，由上到下执行*//*
    private FaceView.FaceCallback faceCallback = new FaceView.FaceCallback() {
        @Override
        public void onReady() {
            SyncManager.instance().requestCompany();
        }

        @Override
        public void onFaceDetection(Boolean hasFace) {
            if(hasFace){
                onLight();
                if(adsFragment != null){
                    adsFragment.detectFace();
                }
            }
        }

        @Override
        public void onFaceVerify(VerifyResult verifyResult) {
            if (verifyResult != null)
                SignManager.instance().checkSign(verifyResult);
        }
    };*/

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent event){
        Company company = SpUtils.getCompany();
        if (tvMainAbbName != null) tvMainAbbName.setText(company.getAbbname());
        if (tvMainTopTitle != null) tvMainTopTitle.setText(company.getToptitle());
        if (tvMainBottomTitle != null) tvMainBottomTitle.setText(company.getBottomtitle());

        EventBus.getDefault().post(new UpdateMediaEvent());

        ImageFileLoader.i().loadAndSave(this,company.getComlogo(), Constants.DATA_PATH,ivMainLogo);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(ResetLogoEvent event){
        Company company = SpUtils.getCompany();
        ImageFileLoader.i().loadAndSave(this,company.getComlogo(), Constants.DATA_PATH,ivMainLogo);
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void update(UpdateLogoEvent event){
//        Log.e("112233", "11111: 收到更新图标的事件" );
//        String logoPath = event.getLogoPath();
//        if(!TextUtils.isEmpty(logoPath)){
//            Glide.with(getActivity()).load(logoPath).asBitmap().into(ivMainLogo);
//        }
//    }

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

    private void goSetting() {
//        String pwd = SpUtils.getStr(SpUtils.MENU_PWD);
//        if (!TextUtils.isEmpty(pwd)) {
//            inputPwd(new Runnable() {
//                @Override
//                public void run() {
//                    startActivity(new Intent(WelComeActivity.this, SystemActivity.class));
//                }
//            });
//            return;
//        }
        startActivity(new Intent(WelComeActivity.this, SystemActivity.class));
    }

    private void onBackKeyPressed(Runnable runnable){
        String pwd = SpUtils.getStr(SpUtils.MENU_PWD);
        if(!TextUtils.isEmpty(pwd)){
            inputPwd(runnable);
            return;
        }
        if(runnable != null){
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
    protected void onResume() {
        super.onResume();
        faceView.resume();
//        faceView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        faceView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(readCardUtils != null){
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