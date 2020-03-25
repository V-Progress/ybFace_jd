package com.yunbiao.ybsmartcheckin_live_id.temp_check_in;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbiao.faceview.CompareResult;
import com.yunbiao.faceview.FaceManager;
import com.yunbiao.faceview.FacePreviewInfo;
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

import java.util.List;

/**
 * Created by Administrator on 2018/11/26.
 */

public class ThermalImageActivity extends BaseThermalActivity {
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
    private TextView tvThermalPercent;
    private View llThermalArea;
    private View faceDistanceView;
    private TextView tvMaxT;
    private TextView tvCacheT;
    private View flDotFrame;
    private ImageView ivBigHead;
    private boolean personFrameEnable;
    private ImageView ivInfaredImaging;

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
        faceView.setCallback(faceCallback);
        ivMainLogo = findViewById(R.id.iv_main_logo);//LOGO
        tvMainAbbName = findViewById(R.id.tv_main_abbname);//公司名
        faceDistanceView = findViewById(R.id.view_face_distance);//人脸距离框
        flDotFrame = findViewById(R.id.fl_dot_frame);//红点测温框
        ivBigHead = findViewById(R.id.iv_big_head);//人像测温框
        tvTempTips = findViewById(R.id.tv_temp_tips_main);//温度提示
        ivInfaredImaging = findViewById(R.id.iv_infared_imaging_main);
        ivThermalImaging = findViewById(R.id.iv_thermal_imaging_main);//热成像图像显示
        tvThermalPercent = findViewById(R.id.tv_thermal_percent_main);//热成像温度显示
        llThermalArea = findViewById(R.id.ll_thermal_area_main);//热成像数据区域

        /*测试数据*/
        tvCacheT = findViewById(R.id.tv_thermal_cacheT_main);
        tvMaxT = findViewById(R.id.tv_thermal_maxT_main);

        //加载签到列表Fragment
        signListFragment = new ThermalSignFragment();
        replaceFragment(R.id.ll_list_container, signListFragment);

        if(Constants.isHT){
            ImageFileLoader.setDefaultLogoId(R.mipmap.logo_icon_horizontal);
            ivMainLogo.setImageResource(R.mipmap.logo_icon_horizontal);
        } else {
            ivMainLogo.setImageResource(R.mipmap.logo);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        faceView.resume();

        personFrameEnable = SpUtils.getBoolean(ThermalConst.Key.PERSON_FRAME, ThermalConst.Default.PERSON_FRAME);
        //设置活体开关
        boolean livenessEnabled = SpUtils.getBoolean(SpUtils.LIVENESS_ENABLED, false);
        faceView.setLiveness(livenessEnabled);

        //切换头像框
        if (mCurrMode != ThermalConst.THERMAL_FACE_ONLY) {
            if (personFrameEnable) {
                ivBigHead.setVisibility(View.VISIBLE);
                flDotFrame.setVisibility(View.GONE);
            } else {
                ivBigHead.setVisibility(View.GONE);
                flDotFrame.setVisibility(View.VISIBLE);
            }
        } else {
            ivBigHead.setVisibility(View.GONE);
            flDotFrame.setVisibility(View.GONE);
        }

        initAds();
    }

    // TODO: 2020/3/13 10.1下面增加编号公司名称版本网络
    @Override
    protected void onModeChanged(int mode) {
        if (signListFragment != null) {
            signListFragment.setModelText(ThermalConst.models[mode]);
        }
        //设置人脸间隔
        SignManager.instance().setVerifyDelay(mode == Constants.Model.MODEL_FACE_ONLY ? 10000 : 0);

        //根据模式选择启动逻辑
        if (mode == Constants.Model.MODEL_FACE_ONLY) {
            Log.e(TAG, "onResume: 仅人脸模式");
            flDotFrame.setVisibility(View.GONE);
            tvTempTips.setVisibility(View.GONE);
            llThermalArea.setVisibility(View.GONE);
        } else if (mode == ThermalConst.THERMAL_TEMP_ONLY || mode == ThermalConst.THERMAL_FACE_TEMP) {//热成像模式
            llThermalArea.setVisibility(View.VISIBLE);
            ivInfaredImaging.setVisibility(View.GONE);
            ivThermalImaging.setVisibility(View.VISIBLE);
        } else {
            llThermalArea.setVisibility(View.VISIBLE);
            ivThermalImaging.setVisibility(View.GONE);
            ivInfaredImaging.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void updateHotImageAndTemper(Bitmap bitmap, float temper, float v, float v1, boolean hasPerson) {

        if (mCurrMode == ThermalConst.THERMAL_TEMP_ONLY || mCurrMode == ThermalConst.THERMAL_FACE_TEMP) {
            ivThermalImaging.setImageBitmap(bitmap);
            tvThermalPercent.setText(getResources().getString(R.string.main_thermal_temp) + temper + "℃");
        } else if (mCurrMode == ThermalConst.INFARED_ONLY || mCurrMode == ThermalConst.INFARED_FACE) {
            ivInfaredImaging.setImageBitmap(bitmap);
            tvThermalPercent.setText(hasPerson ? getResources().getString(R.string.main_thermal_has_person) : getResources().getString(R.string.main_thermal_no_person));
        }
        tvMaxT.setText("maxT：" + v);
        tvCacheT.setText("cacheT：" + v1);
    }

    @Override
    protected boolean showStableTips(String tip, int stableTipsId) {
        if (!tvTempTips.isShown()) {
            tvTempTips.setVisibility(View.VISIBLE);
        }
        tvTempTips.setText(tip);
        tvTempTips.setBackgroundResource(stableTipsId);
        return true;
    }

    @Override
    protected Bitmap getCurrCameraFrame() {
        Bitmap bitmap = faceView.takePicture();
        if (bitmap == null) {
            bitmap = faceView.getCurrCameraFrame();
        }
        return bitmap;
    }

    @Override
    protected void showUIResult(String tip, int id) {
        if (!tvTempTips.isShown()) {
            tvTempTips.setVisibility(View.VISIBLE);
        }
        setBigHeadRealLine();
        tvTempTips.setBackgroundResource(id);
        tvTempTips.setText(tip);
    }

    @Override
    protected void clearUI() {
        if (tvTempTips.isShown()) {
            tvTempTips.setVisibility(View.GONE);
        }
        setBigHead();
    }

    @Override
    protected boolean isTempTipsShown() {
        return tvTempTips.isShown();
    }

    @Override
    protected void clearTempTips() {
        tvTempTips.setVisibility(View.GONE);
        setBigHead();
    }

    @Override
    protected void updateSignList(Sign sign) {
        if (signListFragment != null) {
            signListFragment.addSignData(sign);
        }
        if (sign.getType() != -9) {
            KDXFSpeechManager.instance().playNormal(sign.getName(), getResultRunnable());
        }
    }

    private boolean isRealLine = false;

    private void setBigHead() {
        if (!personFrameEnable) {
            return;
        }
        if (isRealLine) {
            isRealLine = false;
            ivBigHead.setImageResource(R.mipmap.big_head);
        }
    }

    private void setBigHeadRealLine() {
        if (!personFrameEnable) {
            return;
        }
        isRealLine = true;
        ivBigHead.setImageResource(R.mipmap.big_head_real_line);
    }

    /*****识别相关回调******************************************************************************************/
    private FaceView.FaceCallback faceCallback = new FaceView.FaceCallback() {
        @Override
        public void onReady() {
            // TODO: 2020/3/18 离线功能
            //初始化人脸库
            FaceManager.getInstance().init(APP.getContext());

            SyncManager.instance().requestCompany();
        }

        @Override
        public void onFaceDetection(Boolean hasFace, List<FacePreviewInfo> facePreviewInfoList) {

        }

        @Override
        public boolean onFaceDetection(boolean hasFace, FacePreviewInfo facePreviewInfo) {
            updateHasFace(hasFace);

            if (!hasFace) {//如果没有人脸
//                setBigHead();
                return false;
            }

            updateCacheSign(facePreviewInfo.getTrackId());

            //检测到人后开灯
            onLight();
            //收起海报界面
            if (adsFragment != null) {
                adsFragment.detectFace();
            }

            //仅人脸
            if (isOnlyFace()) {
                return true;
            }

            Rect rect = facePreviewInfo.getFaceInfo().getRect();
            int distance = faceDistanceView.getMeasuredWidth();

            //人脸较远
            boolean isFar = faceView.checkFaceToFar(rect, distance);
            updateFaceState(isFar);
            if (isFar) {
                return false;
            }

            //检测人脸是否符合范围
            if (isInfared()) {
                boolean b = faceView.checkFaceInFrame(rect, faceDistanceView);
                checkFaceRange(b);
                if (!b) {
                    return false;
                }
            }

            //仅测温
            if (isOnlyTemp()) {
                return false;
            }

            //没有最终温度
            if (!hasFinalTemp()) {
                return false;
            }
            return true;
        }

        @Override
        public void onFaceVerify(CompareResult compareResult) {
            if (isOnlyFace()) {
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
                    VipDialogManager.showVipDialog(ThermalImageActivity.this, sign);
                }

                KDXFSpeechManager.instance().playText(sign.getName());

                if (sign.getType() == -2) {
                    return;
                }

                openDoor();
            } else {
                Bitmap facePicture = faceView.takePicture();
                if (facePicture == null) {
                    facePicture = faceView.getCurrCameraFrame();
                }
                sendFaceTempMessage(facePicture, compareResult);
            }
        }
    };

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
        KDXFSpeechManager.instance().init(this).welcome();

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

        final Animation animation = AnimationUtils.loadAnimation(ThermalImageActivity.this, R.anim.anim_edt_shake);
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
                    startActivity(new Intent(ThermalImageActivity.this, ThermalSystemActivity.class));
                }
            });
            return;
        }
        startActivity(new Intent(ThermalImageActivity.this, ThermalSystemActivity.class));
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