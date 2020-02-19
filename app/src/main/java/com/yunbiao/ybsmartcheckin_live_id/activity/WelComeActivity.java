package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
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
import com.yunbiao.ybsmartcheckin_live_id.common.UpdateVersionControl;
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

import java.text.DecimalFormat;
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
    public static com.yunbiao.faceview.FaceView faceView;
    private AdsFragment adsFragment;
    private TextView tvTemp;
    private TextView tvRangeTips;
    private SignFragment signListFragment;
    private TextView tvAmbient;
    private TextView tvTempTips;

    //判断是否开启测温
    private boolean isTemperatureEnabled = false;
    private FrameLayout flFaceRange;
    private TextView tvHasTest;
    private TextView tvRangeTest;
    private TextView tvDistanceTest;
    private View viewDistance;
    private TextView tvTempTest;
    private TextView tvAreaTest;
    private TextView tvFaceTest;
    private Switch swTest;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_welcome;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_welcome_h;
    }

    private boolean isFanZhuanTest = false;

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

        flFaceRange = findViewById(R.id.fl_face_range);//额头检测框
        viewDistance = findViewById(R.id.view_face_distance);
        tvAmbient = findViewById(R.id.tv_ambient_temperature_main);//实时环境温度
        tvTemp = findViewById(R.id.tv_temperature_main);//实时检测温度
        tvRangeTips = findViewById(R.id.tv_range_tips_main);//人脸距离提示
        tvTempTips = findViewById(R.id.tv_temp_tips_main);//温度提示

        //测试提示
        tvHasTest = findViewById(R.id.tv_has_test);
        tvDistanceTest = findViewById(R.id.tv_distance_test);
        tvRangeTest = findViewById(R.id.tv_range_test);
        tvTempTest = findViewById(R.id.tv_temp_test);
        tvAreaTest = findViewById(R.id.tv_area_rect_test);
        tvFaceTest = findViewById(R.id.tv_face_rect_test);
        swTest = findViewById(R.id.sw_test);
        swTest.setChecked(isFanZhuanTest);
        swTest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isFanZhuanTest = isChecked;
            }
        });

        //加载签到列表Fragment
        signListFragment = new SignFragment();
        replaceFragment(R.id.ll_list_container, signListFragment);

        //加载广告Fragment
        adsFragment = new AdsFragment();
        addFragment(R.id.ll_face_main, adsFragment);

//        只有竖屏情况下加载信息展示Fragment
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            InformationFragment informationFragment = new InformationFragment();
            replaceFragment(R.id.layout_h, informationFragment);
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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (ReadCardUtils.isInputFromReader(this, event)) {
            if (readCardUtils != null) {
                readCardUtils.resolveKeyEvent(event);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void initData() {
        initCardReader();

        KDXFSpeechManager.instance().init(getActivity()).welcome();

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

        startUpdateTemperatureRunnable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isTemperatureEnabled = SpUtils.getBoolean(SpUtils.TEMPERATURE_ENABLED, true);
        if (!isTemperatureEnabled) {
            closeTemperatureTips();
        }

        InfraredTemperatureUtils.getIns().setaCorrectionValue(SpUtils.getFloat(SpUtils.AMB_CORRECT_VALUE, 0.0f));
        InfraredTemperatureUtils.getIns().setmCorrectionValue(SpUtils.getFloat(SpUtils.TEMP_CORRECT_VALUE, 1.5f));

        faceView.resume();
    }

    /****温控检测区域************************************************************************************************/
    private DecimalFormat decimalFormat = new DecimalFormat("#.#");

    private void closeTemperatureTips() {
        flFaceRange.setVisibility(View.GONE);
        viewDistance.setVisibility(View.GONE);
        tvAmbient.setVisibility(View.GONE);
        tvTemp.setVisibility(View.GONE);
        tvRangeTips.setVisibility(View.GONE);
        tvTempTips.setVisibility(View.GONE);
    }

    private Handler temperatureUpdateHandler = new Handler();

    //开始自动测温
    private void startUpdateTemperatureRunnable() {
        InfraredTemperatureUtils.getIns().initSerialPort();
        temperatureUpdateHandler.removeCallbacks(temperatureUpdateRunnable);
        temperatureUpdateHandler.post(temperatureUpdateRunnable);
    }

    private Runnable temperatureUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            //获取检测温度
            float measuringTemperatureF = InfraredTemperatureUtils.getIns().getMeasuringTemperatureF();
            tvTemp.setText(decimalFormat.format(measuringTemperatureF) + " ℃");

            //获取环境温度
            float ambientTemperatureF = InfraredTemperatureUtils.getIns().getAmbientTemperatureF();
            tvAmbient.setText(decimalFormat.format(ambientTemperatureF) + "℃");

            temperatureUpdateHandler.postDelayed(temperatureUpdateRunnable, 500);
        }
    };

    //关闭测温串口
    private void closeUpdateTemperatureRunnable() {
        temperatureUpdateHandler.removeCallbacks(temperatureUpdateRunnable);
        InfraredTemperatureUtils.getIns().closeSerialPort();
    }

    //设置温度提示框的内容
    private void showTemperatureTips(final String tips, final int backgroundId) {
        //如果未显示，则显示
        if (!tvTempTips.isShown()) {
            tvTempTips.post(new Runnable() {
                @Override
                public void run() {
                    tvTempTips.setVisibility(View.VISIBLE);
                }
            });
        }

        //然后修改显示内容
        tvTempTips.post(new Runnable() {
            @Override
            public void run() {
                tvTempTips.setText(tips);
                tvTempTips.setBackgroundResource(backgroundId);
            }
        });

        //发送计时关闭任务
        tempTipsHandler.removeMessages(0);
        tempTipsHandler.sendEmptyMessageDelayed(0, 2000);
    }

    //计时器
    private Handler tempTipsHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (tvTempTips.isShown()) {
                tvTempTips.setVisibility(View.INVISIBLE);
            }
        }
    };

    /*****识别相关回调******************************************************************************************/
    private com.yunbiao.faceview.FaceView.FaceCallback faceCallback = new com.yunbiao.faceview.FaceView.FaceCallback() {
        @Override
        public void onReady() {
            SyncManager.instance().requestCompany();
        }

        @Override
        public void onFaceDetection(Boolean hasFace, List<FacePreviewInfo> facePreviewInfoList) {

        }

        @Override
        public boolean onFaceDetection(boolean hasFace, FacePreviewInfo facePreviewInfo) {

            if (!hasFace) {//如果没有人脸
                mStrangerTempTrackId = -1;
                tvHasTest.setText("无人");
                tvDistanceTest.setText("");
                tvRangeTest.setText("");
                tvTempTest.setText("");
                return false;
            }
            tvHasTest.setText("有人");

            //检测到人后开灯
            onLight();
            //收起海报界面
            if (adsFragment != null) {
                adsFragment.detectFace();
            }

            //判断测温逻辑是否开启
            if (isTemperatureEnabled) {
                //如果未初始化完成
                if (tvRangeTips == null) {
                    return false;
                }
                //取出人脸的rect
                Rect rect = facePreviewInfo.getFaceInfo().getRect();
                if (rect == null) {//如果巨星为null
                    return false;
                }
                //判断人脸距离是否合适
                if (faceView.checkFaceDistance(rect, viewDistance.getMeasuredWidth())) {
                    tvRangeTips.setText("距离太远，请靠近");
                    tvDistanceTest.setText("距离太远");
                    tvRangeTest.setText("");
                    tvTempTest.setText("");
                    return false;
                }
                tvDistanceTest.setText("距离合适");

                //判断人脸区域是否正确
                boolean faceInframe = faceView.checkFaceInFrame(rect, flFaceRange, new FaceView.RectCallback() {
                    @Override
                    public void onAreaRect(Rect mAreaRect, Rect mFaceRect) {
                        tvAreaTest.setText("区域坐标：左：" + mAreaRect.left + "，右：" + mAreaRect.right + "\n上：" + mAreaRect.top + "，下：" + mAreaRect.bottom);
                        tvFaceTest.setText("人脸坐标：左：" + mFaceRect.left + "，右：" + mFaceRect.right + "\n上：" + mFaceRect.top + "，下：" + mFaceRect.bottom);
                    }
                });

                if (!faceInframe) {
                    tvRangeTips.setText("请将脸部对准人脸识别区域");
                    tvRangeTest.setText("请对准区域");
                    tvTempTest.setText("");
                    return false;
                }
                tvRangeTest.setText("已对准");
                //都合适，隐藏提示
                tvRangeTips.setText("");

                //判断当前温度，取36度以上再进行识别
                float measuringTemperatureF = InfraredTemperatureUtils.getIns().getMeasuringTemperatureF();
                if (measuringTemperatureF < 36.0f) {
                    tvTempTest.setText("等待温度");
                    return false;
                }
                tvTempTest.setText(measuringTemperatureF + "℃");
            }

            return true;
        }

        @Override
        public void onFaceVerify(CompareResult compareResult) {
            //进入测温流程
            if (isTemperatureEnabled) {
                float measuringTemperatureF = InfraredTemperatureUtils.getIns().getMeasuringTemperatureF();
                measuringTemperatureF = Float.parseFloat(decimalFormat.format(measuringTemperatureF));
                final boolean isWarning = measuringTemperatureF >= 37.3;

                //如果是陌生人
                if (compareResult.getSimilar() == -1) {
                    if (mStrangerTempTrackId != compareResult.getTrackId()) {//判断缓存的Id
                        //还没有播报过
                        if (isWarning) {
                            showTemperatureTips("体温异常 " + measuringTemperatureF + "℃", R.drawable.shape_main_frame_temperature_warning);
                            KDXFSpeechManager.instance().playText("体温异常");
                        } else {
                            showTemperatureTips("体温正常 " + measuringTemperatureF + "℃", R.drawable.shape_main_frame_temperature_normal);
                            KDXFSpeechManager.instance().playText("体温正常");
                        }

//                        Bitmap headImgByte = faceView.getHeadImgByte(compareResult.getTrackId());
//                        SignManager.instance().uploadTemperException(headImgByte, measuringTemperatureF, null);

                        mStrangerTempTrackId = compareResult.getTrackId();
                    } else {
                        //已经播报过了
                    }
                } else {
                    mStrangerTempTrackId = -1;
                    Sign sign = SignManager.instance().checkSignData(compareResult, measuringTemperatureF);
                    if (sign == null) {
                        return;
                    }
//                    Bitmap headImgByte = faceView.getHeadImgByte(compareResult.getTrackId());
//                    SignManager.instance().uploadTemperException(headImgByte, measuringTemperatureF, sign.getEmpId() + "");

                    if (isWarning) {
                        showTemperatureTips("体温异常 " + measuringTemperatureF + "℃", R.drawable.shape_main_frame_temperature_warning);
                        KDXFSpeechManager.instance().playText("体温异常");
                    } else {
                        if (signListFragment != null) {
                            signListFragment.addSignData(sign);
                        }

                        if (SpUtils.getBoolean(SpUtils.FACE_DIALOG, false)) {
                            VipDialogManager.showVipDialog(getActivity(), sign);
                        }

                        showTemperatureTips("体温正常 " + measuringTemperatureF + "℃", R.drawable.shape_main_frame_temperature_normal);
                        KDXFSpeechManager.instance().playText(sign.getName() + "，体温正常");

                        if (sign.getType() == -2) {
                            return;
                        }

                        openDoor();
                    }
                }
            } else {//进入普通识别流程
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
                    VipDialogManager.showVipDialog(getActivity(), sign);
                }

                KDXFSpeechManager.instance().playText(sign.getName());

                if (sign.getType() == -2) {
                    return;
                }

                openDoor();
            }
        }
    };
    //判断陌生人有没有播报过的缓存，如果该Id不等于认证的Id说明没播报过，在非陌生人的地方和检测不到人脸时重置该Id
    private int mStrangerTempTrackId = -1;

    private ReadCardUtils.OnReadSuccessListener readCardListener = new ReadCardUtils.OnReadSuccessListener() {
        @Override
        public void onScanSuccess(String barcode) {
            Log.e(TAG, "barcode: " + barcode);
            Sign sign = SignManager.instance().checkSignForCard(barcode);

            if (signListFragment != null) {
                signListFragment.addSignData(sign);
            }

            if (SpUtils.getBoolean(SpUtils.FACE_DIALOG, false)) {
                VipDialogManager.showVipDialog(getActivity(), sign);
            }

            KDXFSpeechManager.instance().playText(sign.getName());

            if (sign.getType() == -2) {
                return;
            }

            openDoor();
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent event) {
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
        closeUpdateTemperatureRunnable();
        faceView.destory();
        destoryXmpp();

        EventBus.getDefault().unregister(this);
        SyncManager.instance().destory();
        KDXFSpeechManager.instance().destroy();
        LocateManager.instance().destory();
    }
}