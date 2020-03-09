package com.yunbiao.ybsmartcheckin_live_id.smdt_portrait;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.intelligence.hardware.temperature.TemperatureModule;
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
import com.yunbiao.ybsmartcheckin_live_id.activity.SystemActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.AdsFragment;
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
import com.yunbiao.ybsmartcheckin_live_id.utils.RestartAPPTool;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.views.ImageFileLoader;
import com.yunbiao.ybsmartcheckin_live_id.xmpp.ServiceManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SMTMainActivity extends SMTBaseActivity {
    private static final String TAG = "SMTMainActivity";
    private ImageView ivMainLogo;//公司logo

    // xmpp推送服务
    private ServiceManager serviceManager;

    //摄像头分辨率
    public static FaceView faceView;
    private SMTSignFragment smtSignFragment;
    private TextView tvTemperature;
    private TextView tvRangeTips;
    private TextView tvTempTips;
    private View flTempArea;
    private int smtModel;

    @Override
    protected int getLayout() {
        return R.layout.activity_smt_device;
    }

    @Override
    protected void initView() {
        APP.setMainActivity(this);
        EventBus.getDefault().register(this);
        faceView = findViewById(R.id.face_view);
        faceView.setCallback(faceCallback);
        ivMainLogo = findViewById(R.id.iv_main_logo);

        //测温点
        flTempArea = findViewById(R.id.fl_temp_area);

        tvTemperature = findViewById(R.id.tv_temperature_main);//实时检测温度
        tvRangeTips = findViewById(R.id.tv_range_tips_main);//人脸距离提示
        tvTempTips = findViewById(R.id.tv_temp_tips_main);//温度提示

        //加载签到列表Fragment
        smtSignFragment = new SMTSignFragment();
        replaceFragment(R.id.ll_list_container, smtSignFragment);

        /*红外模块是9600，热成像模块是115200*/
        TemperatureModule.getIns().initSerialPort(this, "/dev/ttyS3", 115200);
        TemperatureModule.getIns().closeSmt3232Temp();
        TemperatureModule.getIns().startSmt3232Temp(100);
    }

    @Override
    protected void onResume() {
        super.onResume();

        faceView.resume();

        mTempMinThreshold = SpUtils.getFloat(SpUtils.TEMP_MIN_THRESHOLD, Constants.DEFAULT_TEMP_MIN_THRESHOLD_VALUE); //测温最小阈值
        mTempWarningThreshold = SpUtils.getFloat(SpUtils.TEMP_WARNING_THRESHOLD, Constants.DEFAULT_TEMP_WARNING_THRESHOLD_VALUE); //测温报警阈值

        smtModel = SpUtils.getIntOrDef(SpUtils.SMT_MODEL_SETTING, SMTModelConst.DEFAULT_SMT_MODEL);
        if (smtModel == SMTModelConst.SMT_FACE_ONLY) {
            flTempArea.setVisibility(View.GONE);
            tvTempTips.setVisibility(View.GONE);
        } else {
            flTempArea.setVisibility(View.VISIBLE);
            tvTemperature.setVisibility(View.VISIBLE);
            closeInfraedTemperature();
            startInfraredTemperature();
        }

        if (smtSignFragment != null) {
            smtSignFragment.setModelText(SMTModelConst.models[smtModel]);
        }

        //设置活体开关
        boolean livenessEnabled = SpUtils.getBoolean(SpUtils.LIVENESS_ENABLED, false);
        faceView.setLiveness(livenessEnabled);

        //根据模式选择启动逻辑
        if (smtModel == Constants.Model.MODEL_FACE_ONLY) {
            flTempArea.setVisibility(View.GONE);
            tvTempTips.setVisibility(View.GONE);
        } else {//热成像模式
            flTempArea.setVisibility(View.VISIBLE);
        }
    }

    private long mCacheTime = 0;
    private int mCacheFaceId = -1;
    /*****识别相关回调******************************************************************************************/
    private FaceView.FaceCallback faceCallback = new FaceView.FaceCallback() {
        @Override
        public void onReady() {
            SyncManager.instance().requestCompany();
        }

        @Override
        public void onFaceDetection(Boolean hasFace, List<FacePreviewInfo> facePreviewInfoList) {

        }

        @Override
        public boolean onFaceDetection(boolean hasFace, FacePreviewInfo facePreviewInfo) {
            canDetection = hasFace;
            if (!hasFace) {//如果没有人脸
                mCacheFaceId = -1;
                mCacheTime = 0;
                return false;
            }

            //检测到人后开灯
            onLight();
            if (smtModel == SMTModelConst.SMT_TEMP_ONLY) {
                return false;
            } else
                //如果是红外模式和热成像模式则不走识别逻辑
                if (smtModel == SMTModelConst.SMT_FACE_TEMP) {//如果是人脸+测温则判断模式
                    if (mCurrTemp <= 0.0F || mCurrTemp < mTempMinThreshold) {
                        return false;
                    }
                    if (mCacheTime == 0) {
                        mCacheTime = System.currentTimeMillis();
                        return false;
                    } else if (System.currentTimeMillis() - mCacheTime < 500) {
                        return false;
                    }
                }
            return true;
        }

        @Override
        public void onFaceVerify(CompareResult compareResult) {
            if (smtModel == SMTModelConst.SMT_FACE_TEMP) {
                if (mCurrTemp == 0.0f) {
                    return;
                }

                if (mCacheFaceId == compareResult.getTrackId()) {
                    return;
                }
                mCacheFaceId = compareResult.getTrackId();
                Bitmap bitmap = faceView.takePicture();
                if (bitmap == null) {
                    return;
                }

                boolean isWarning = mCurrTemp >= mTempWarningThreshold;
                Sign sign = null;
                if (compareResult.getSimilar() == -1) {
                    //直接上报温度
                    sign = SignManager.instance().getTemperatureSign(mCurrTemp);
                } else {
                    sign = SignManager.instance().checkSignData(compareResult, mCurrTemp);
                    if (sign == null) {
                        return;
                    }
                }

                playTips(isWarning, sign.getName(), sign.getTemperature());

                sign.setImgBitmap(bitmap);
                sign.setHotImageBitmap(mCacheHotImage);
                if (smtSignFragment != null) {
                    smtSignFragment.addSignData(sign);
                }

                SignManager.instance().uploadTemperatureSign(sign);
                //如果是过期则结束，陌生人则判断是否体温正常
                if (sign.getType() == -2 || sign.getType() == -9 || isWarning) {
                    return;
                }
                openDoor();
                return;
            }

            //======以下是普通识别流程====================================
            if (compareResult == null || compareResult.getSimilar() == -1) {
                return;
            }
            Sign sign = SignManager.instance().checkSignData(compareResult, 0f);
            if (sign == null) {
                return;
            }
            if (smtSignFragment != null) {
                smtSignFragment.addSignData(sign);
            }

            if (SpUtils.getBoolean(SpUtils.FACE_DIALOG, false)) {
                VipDialogManager.showVipDialog(SMTMainActivity.this, sign);
            }

            KDXFSpeechManager.instance().playText(sign.getName());

            if (sign.getType() == -2) {
                return;
            }

            openDoor();
        }
    };
    //========================================================
    private Handler infraredHandler;

    //开始红外测温逻辑
    private void startInfraredTemperature() {
        closeInfraedTemperature();
        if (infraredHandler == null) {
            infraredHandler = new Handler();
        }
        infraredHandler.post(temperatureUpdateRunnable);
    }

    //关闭
    private void closeInfraedTemperature() {
        if (infraredHandler != null) {
            infraredHandler.removeCallbacks(temperatureUpdateRunnable);
        }
        infraredHandler = null;
    }

    private Runnable temperatureUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            //获取检测温度
            float measuringTemperatureF = formatF(TemperatureModule.getIns().getMeasuringTemperatureF());
            tvTemperature.setText(measuringTemperatureF + "℃");

            if (smtModel == SMTModelConst.SMT_FACE_TEMP) {
                if (canDetection) {
                    mCurrTemp = measuringTemperatureF;
                } else {
                    mCurrTemp = 0.0f;
                }
            } else if (smtModel == SMTModelConst.SMT_TEMP_ONLY) {
                //有人
                if (measuringTemperatureF > 0.0F) {
                    if (measuringTemperatureF < mTempMinThreshold) {
                        isBroadCasted = true;
                        showTemperatureTips("请靠近", R.drawable.shape_main_frame_temperature_ing, 3000);
                    } else {
                        if (!isBroadCasted) {
                            isBroadCasted = true;
                            boolean isWarning = measuringTemperatureF >= mTempWarningThreshold;

                            //直接上报温度
                            Sign sign = SignManager.instance().getTemperatureSign(mCurrTemp);

                            Bitmap currCameraFrame = faceView.getCurrCameraFrame();
                            sign.setImgBitmap(currCameraFrame);
                            sign.setHotImageBitmap(mCacheHotImage);
                            if (smtSignFragment != null) {
                                smtSignFragment.addSignData(sign);
                            }
                            playTipsAddOpenDoor(isWarning, "", measuringTemperatureF);
                        }
                    }
                } else {
                    dismissTemperatureTips(300);
                    isBroadCasted = false;
                }
            }

            infraredHandler.postDelayed(temperatureUpdateRunnable, 400);
        }
    };

    private ReadCardUtils.OnReadSuccessListener readCardListener = new ReadCardUtils.OnReadSuccessListener() {
        @Override
        public void onScanSuccess(String barcode) {
            Log.e(TAG, "barcode: " + barcode);
            Sign sign = SignManager.instance().checkSignForCard(barcode);

            if (smtSignFragment != null) {
                smtSignFragment.addSignData(sign);
            }

            if (SpUtils.getBoolean(SpUtils.FACE_DIALOG, false)) {
                VipDialogManager.showVipDialog(SMTMainActivity.this, sign);
            }

            KDXFSpeechManager.instance().playText(sign.getName());

            if (sign.getType() == -2) {
                return;
            }

            openDoor();
        }
    };

    /****温控检测区域************************************************************************************************/
    /****温控检测区域************************************************************************************************/
    /****温控检测区域************************************************************************************************/
    /****温控检测区域************************************************************************************************/
    /****温控检测区域************************************************************************************************/
    /****温控检测区域************************************************************************************************/
    /****温控检测区域************************************************************************************************/
    /****温控检测区域************************************************************************************************/
    private boolean canDetection = false;//是否可以采集温度
    private float mTempMinThreshold = 36.0f;//最小阈值
    private float mTempWarningThreshold = 37.3f;//报警值
    private float mCurrTemp = 0.0f;
    private boolean isBroadCasted = false;

    private float formatF(float fValue) {
        return (float) (Math.round(fValue * 10)) / 10;
    }

    private Bitmap mCacheHotImage = null;

    private Message tipsMessage;

    //设置温度提示框的内容
    private void showTemperatureTips(final String tips, final int backgroundId, int time) {
        //如果未显示，则显示
        if (!tvTempTips.isShown()) {
            tempTipsHandler.sendEmptyMessage(1);
        }

        tempTipsHandler.removeMessages(2);
        Bundle bundle = new Bundle();
        bundle.putString("tips", tips);
        bundle.putInt("background", backgroundId);
        tipsMessage = new Message();
        tipsMessage.what = 2;
        tipsMessage.setData(bundle);
        tempTipsHandler.sendMessage(tipsMessage);

        if (time > 0) {
            dismissTemperatureTips(time);
        }
    }

    private void dismissTemperatureTips(int time) {
        tempTipsHandler.removeMessages(0);
        boolean b = tempTipsHandler.sendEmptyMessageDelayed(0, time);
    }

    //计时器
    private Handler tempTipsHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (tvTempTips.isShown()) {
                        tvTempTips.setVisibility(View.INVISIBLE);
                    }
                    break;
                case 1:
                    tvTempTips.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    Bundle data = msg.getData();
                    String tips = data.getString("tips");
                    int bgId = data.getInt("background");
                    tvTempTips.setText(tips);
                    tvTempTips.setBackgroundResource(bgId);
                    break;
            }

        }
    };

    private void playTipsAddOpenDoor(boolean isWarning, String signName, float temperature) {
        if (!isWarning) {
            openDoor();
        }
        playTips(isWarning, signName, temperature);
    }

    private void playTips(boolean isWarning, String signName, float temperature) {
        String speechTips;
        String textTips;
        Runnable warningRunnable;
        int bgId;
        //体温正常
        if (isWarning) {
            String warningTips = SpUtils.getStr(SpUtils.WARNING_TIPS);
            speechTips = TextUtils.isEmpty(warningTips)
                    ? getResources().getString(R.string.temperature_tips_warning_main) + temperature + "℃"
                    : warningTips;
            textTips = getResources().getString(R.string.temperature_tips_warning_main) + temperature + "℃";

            bgId = R.drawable.shape_main_frame_temperature_warning;
            warningRunnable = new Runnable() {
                @Override
                public void run() {
                    KDXFSpeechManager.instance().playWaningRing();
                    resetLedDelay(3000);//5秒后重置灯光为蓝色
                    dismissTemperatureTips(3000);
                }
            };
            ledRed();
        } else {
            String normalTips = SpUtils.getStr(SpUtils.NORMAL_TIPS);
            speechTips = TextUtils.isEmpty(normalTips)
                    ? getResources().getString(R.string.temperature_tips_normal_main) + temperature + "℃"
                    : normalTips;
            textTips = getResources().getString(R.string.temperature_tips_normal_main) + temperature + "℃";

            bgId = R.drawable.shape_main_frame_temperature_normal;
            KDXFSpeechManager.instance().stopNormal();
            KDXFSpeechManager.instance().stopWarningRing();
            ledGreen();//显示绿灯
            warningRunnable = new Runnable() {
                @Override
                public void run() {
                    dismissTemperatureTips(0);
                    resetLedDelay(0);//5秒后重置灯光为蓝色
                }
            };
            openDoor();
        }

        showTemperatureTips(textTips, bgId, -1);
        KDXFSpeechManager.instance().playNormal((TextUtils.isEmpty(signName) ? "" : (signName + "，")) + speechTips, warningRunnable);
    }

    /*=======测试配置================================*/
    private float mTestCorrValue = 0.0f;

    private TextView tvTempMinCache;
    private TextView tvTempMaxCache;
    private TextView tvSensorTTest;
    private TextView tvMaxTest;
    private TextView tvMinTest;
    private TextView tvBodyMaxT;
    private TextView tvCallbackTimeTest;

    private void initTest() {
        tvTempMinCache = findViewById(R.id.tv_temperature_cache_min_main);
        tvTempMaxCache = findViewById(R.id.tv_temperature_cache_max_main);

        Button btnFar = findViewById(R.id.btn_test_distance_far);
        Button btnOk = findViewById(R.id.btn_test_distance_ok);
        Button btnReset = findViewById(R.id.btn_test_reset);
        Button btnNo = findViewById(R.id.btn_test_no);
        Button btnWarning = findViewById(R.id.btn_test_distance_ok_warning);
        Button btnJiangWen = findViewById(R.id.btn_jiang_wen);
        btnFar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTestCorrValue = 25.0f;
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTestCorrValue = 35.0f;
            }
        });
        btnWarning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTestCorrValue = 37.0f;
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTestCorrValue = 0.0f;
            }
        });
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTestCorrValue = 0.0f;
            }
        });
        btnJiangWen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                int i = random.nextInt(13);
                mTestCorrValue = i;
            }
        });

        tvSensorTTest = findViewById(R.id.tv_sensorT_test);
        tvMaxTest = findViewById(R.id.tv_maxT_test);
        tvMinTest = findViewById(R.id.tv_minT_test);
        tvBodyMaxT = findViewById(R.id.tv_bodyMaxT_test);
        tvCallbackTimeTest = findViewById(R.id.tv_callback_time_test);
    }
    /****************************************************************************************************/
    /****************************************************************************************************/
    /****************************************************************************************************/
    /****************************************************************************************************/
    /****************************************************************************************************/
    /****************************************************************************************************/
    /****************************************************************************************************/

    @Override
    protected void initData() {
        KDXFSpeechManager.instance().init(this).welcome();

        //开启Xmpp
        startXmpp();
        //初始化定位工具
        LocateManager.instance().init(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "run: ------- ");
                UpdateVersionControl.getInstance().checkUpdate(SMTMainActivity.this);
            }
        }, 5 * 1000);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent event) {
        Company company = SpUtils.getCompany();

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

    /**
     * 读卡器初始化==========================================================================================
     * 读卡器初始化==========================================================================================
     * 读卡器初始化==========================================================================================
     */
    //U口读卡器,类似于外接键盘
    private ReadCardUtils readCardUtils;

    private void initCardReader() {
        //读卡器声明
        readCardUtils = new ReadCardUtils();
        readCardUtils.setReadSuccessListener(readCardListener);
    }

    private void closeCardReader() {
        if (readCardUtils != null) {
            readCardUtils.removeScanSuccessListener();
            readCardUtils = null;
        }
    }

    private void handleKeyEvent(KeyEvent event) {
        if (ReadCardUtils.isInputFromReader(this, event)) {
            if (readCardUtils != null) {
                readCardUtils.resolveKeyEvent(event);
            }
        }
    }

    //密码弹窗
    private void inputPwd(final Runnable runnable) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_input_pwd);

        final Animation animation = AnimationUtils.loadAnimation(SMTMainActivity.this, R.anim.anim_edt_shake);
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
                    startActivity(new Intent(SMTMainActivity.this, SMTSystemActivity.class));
                }
            });
            return;
        }
        startActivity(new Intent(SMTMainActivity.this, SMTSystemActivity.class));
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        handleKeyEvent(event);
        return super.dispatchKeyEvent(event);
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
        closeInfraedTemperature();
        faceView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        TemperatureModule.getIns().closeSerialPort();
        closeInfraedTemperature();
        faceView.destory();
        destoryXmpp();

        EventBus.getDefault().unregister(this);
        SyncManager.instance().destory();
        KDXFSpeechManager.instance().destroy();
        LocateManager.instance().destory();
    }
}
