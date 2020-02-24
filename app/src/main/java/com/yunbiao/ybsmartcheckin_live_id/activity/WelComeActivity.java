package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
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

import com.yunbiao.faceview.CompareResult;
import com.yunbiao.faceview.FacePreviewInfo;
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
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
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
    public static com.yunbiao.faceview.FaceView faceView;
    private AdsFragment adsFragment;
    private SignFragment signListFragment;
    private TextView tvTemperature;
    private TextView tvRangeTips;
    private TextView tvAmbient;
    private TextView tvTempTips;

    //判断是否开启测温
    private View viewDistance;
    private View personFrame;

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
        APP.setActivity(this);
        EventBus.getDefault().register(this);
        faceView = findViewById(R.id.face_view);
        faceView.setCallback(faceCallback);
        ivMainLogo = findViewById(R.id.iv_main_logo);
        tvMainAbbName = findViewById(R.id.tv_main_abbname);
        tvMainTopTitle = findViewById(R.id.tv_main_topTitle);
        tvMainBottomTitle = findViewById(R.id.tv_main_bottomTitle);

        personFrame = findViewById(R.id.iv_person_frame);
        viewDistance = findViewById(R.id.view_face_distance);//人脸限制区域
        tvAmbient = findViewById(R.id.tv_ambient_temperature_main);//实时环境温度
        tvTemperature = findViewById(R.id.tv_temperature_main);//实时检测温度
        tvRangeTips = findViewById(R.id.tv_range_tips_main);//人脸距离提示
        tvTempTips = findViewById(R.id.tv_temp_tips_main);//温度提示

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

        initTest();
    }

    private Button btnTest0;

    private void initTest() {
        btnTest0 = findViewById(R.id.btn_test_0);
        btnTest0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCacheTemperatureHighestValue = 0f;
                mCacheValueForTempModel = 0f;
                mCacheTime = 0;
            }
        });
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        //获取模式标识
        mCurrModel = SpUtils.getIntOrDef(SpUtils.MODEL_SETTING, 0);
        boolean isTemperatureEnabled = mCurrModel != Constants.Model.MODEL_FACE_ONLY;
        if (isTemperatureEnabled) {
            personFrame.setVisibility(View.VISIBLE);
            tvAmbient.setVisibility(View.VISIBLE);
            tvRangeTips.setVisibility(View.VISIBLE);
            tvTempTips.setVisibility(View.VISIBLE);
            startUpdateTemperatureRunnable();
        } else {
            personFrame.setVisibility(View.GONE);
            tvAmbient.setVisibility(View.GONE);
            tvRangeTips.setVisibility(View.GONE);
            tvTempTips.setVisibility(View.GONE);
            temperatureUpdateHandler.removeCallbacks(temperatureUpdateRunnable);
        }

        //设置测温补正值
        InfraredTemperatureUtils.getIns().setaCorrectionValue(SpUtils.getFloat(SpUtils.AMB_CORRECT_VALUE, 0.0f));
        InfraredTemperatureUtils.getIns().setmCorrectionValue(SpUtils.getFloat(SpUtils.TEMP_CORRECT_VALUE, 1.5f));
        //设置活体开关
        boolean livenessEnabled = SpUtils.getBoolean(SpUtils.LIVENESS_ENABLED, false);
        faceView.setLiveness(livenessEnabled);
        //设置测温延时
        mGetTempDelayTime = SpUtils.getIntOrDef(SpUtils.GET_TEMP_DELAY_TIME, 1000);

        //测温最小阈值
        mTempMinThreshold = SpUtils.getFloat(SpUtils.TEMP_MIN_THRESHOLD, 36.0f);
        //测温报警阈值
        mTempWarningThreshold = SpUtils.getFloat(SpUtils.TEMP_WARNING_THRESHOLD, 37.3f);
        faceView.resume();
    }

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
            canDetection = hasFace;
            if (!hasFace) {//如果没有人脸
                mStrangerTempTrackId = -1;
                setRangeTips("");
                return false;
            }

            //检测到人后开灯
            onLight();
            //收起海报界面
            if (adsFragment != null) {
                adsFragment.detectFace();
            }

            //如果是测温模式则不走识别逻辑
            if (mCurrModel == Constants.Model.MODEL_TEMPERATURE_ONLY) {
                return false;
            } else if (mCurrModel == Constants.Model.MODEL_FACE_TEMPERATURE) {//如果是人脸+测温则判断模式
                //取出人脸的rect
                Rect rect = facePreviewInfo.getFaceInfo().getRect();
                //如果未初始化完成
                if (tvRangeTips == null || rect == null) {
                    return false;
                }
                //判断人脸距离是否合适
                if (!faceView.checkFaceInDistance(rect, viewDistance.getMeasuredWidth())) {
                    mCacheTime = 0;
                    setRangeTips("距离太远，请靠近");
                    return false;
                }
                //检测人脸是否在框内
                if (!faceView.checkFaceInFrame2(rect, viewDistance)) {
                    mCacheTime = 0;
                    setRangeTips("请将脸部对准人脸识别区域");
                    return false;
                }
                //判断当前温度，取36度以上再进行识别
                if (mCacheTemperatureHighestValue <= mTempMinThreshold) {
                    return false;
                }
                //如果当前缓存时间是0则重置
                if (mCacheTime == 0) {
                    mCacheTime = System.currentTimeMillis();
                    return false;
                } else if (System.currentTimeMillis() - mCacheTime < mGetTempDelayTime) {
                    setRangeTips("正在测温");
                    return false;
                }
                //都合适，隐藏提示
                setRangeTips("");
            }

            return true;
        }

        @Override
        public void onFaceVerify(CompareResult compareResult) {
            if (mCurrModel == Constants.Model.MODEL_FACE_TEMPERATURE) {
                Bitmap bitmap = faceView.takePicture();
                if (bitmap == null) {
                    return;
                }

                boolean isWarning = mCacheTemperatureHighestValue >= mTempWarningThreshold;
                Sign sign = null;
                if (compareResult.getSimilar() == -1) {
                    //代表已经播报过
                    if (mStrangerTempTrackId == compareResult.getTrackId()) {
                        return;
                    }
                    mStrangerTempTrackId = compareResult.getTrackId();
                    //直接上报温度
                    sign = SignManager.instance().getTemperatureSign(mCacheTemperatureHighestValue);
                } else {
                    mStrangerTempTrackId = -1;
                    sign = SignManager.instance().checkSignData(compareResult, mCacheTemperatureHighestValue);
                    if (sign == null) {
                        return;
                    }
                }

                playTips(isWarning, sign.getName());

                sign.setImgBitmap(bitmap);
                if (signListFragment != null) {
                    signListFragment.addSignData(sign);
                }

                SignManager.instance().uploadTemperatureSign(sign);
                //如果是过期或陌生人则结束
                if (sign.getType() == -2 || sign.getType() == -9) {
                    return;
                }
                openDoor();
                return;
            } else if (mCurrModel == Constants.Model.MODEL_FACE_ONLY) {
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

    /****温控检测区域************************************************************************************************/
    private boolean canDetection = false;//是否可以采集温度
    //判断陌生人有没有播报过的缓存，如果该Id不等于认证的Id说明没播报过，在非陌生人的地方和检测不到人脸时重置该Id
    private int mStrangerTempTrackId = -1;//ID缓存，用于判断该用户有没有被提示过
    private int mGetTempDelayTime = 0;//采集温度延时
    private float mTempMinThreshold = 36.0f;//最小阈值
    private float mTempWarningThreshold = 37.3f;//报警值
    private int mCurrModel = 0;//当前模式

    private long mCacheTime = 0;//时间缓存
    private float mCacheTemperatureHighestValue = 0f;//最高温度值缓存
    private float mCacheValueForTempModel = 0f;//专为测温模式缓存的数值,用于判断该次有没有被播报,防止多次播报

    private void setTemperatureModule(boolean isOpen) {
        int show;
        if (isOpen) {
            show = View.VISIBLE;
        } else {
            show = View.GONE;
        }
        personFrame.setVisibility(show);
        tvAmbient.setVisibility(show);
        tvRangeTips.setVisibility(show);
        tvTempTips.setVisibility(show);
    }

    private void setRangeTips(String tips) {
        String s = tvRangeTips.getText().toString();
        if (TextUtils.isEmpty(tips) && TextUtils.isEmpty(s)) {
            return;
        }
        if (TextUtils.equals(tips, s)) {
            return;
        }
        tvRangeTips.setText(tips);
    }

    private float formatF(float fValue) {
        return (float) (Math.round(fValue * 10)) / 10;
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
            //获取环境温度
            float ambientTemperatureF = formatF(InfraredTemperatureUtils.getIns().getAmbientTemperatureF());
            if (ambientTemperatureF <= 0f) {
                if (tvAmbient.isShown()) {
                    tvAmbient.setVisibility(View.GONE);
                }
            } else {
                tvAmbient.setText(ambientTemperatureF + "℃");
            }

            //获取检测温度
            float measuringTemperatureF = formatF(InfraredTemperatureUtils.getIns().getMeasuringTemperatureF());
            tvTemperature.setText(measuringTemperatureF + " ℃");

            if (measuringTemperatureF < mTempWarningThreshold) {
                tvTemperature.setTextColor(Color.GREEN);
            } else {
                tvTemperature.setTextColor(Color.RED);
            }
            /*
             * 检测温度
             * 缓存温度
             * 如果检测温度大于阈值则说明有人,开始存最大值
             * 如果检测温度小于阈值说明没有人,存值归0
             * 如果存值也大于阈值,则判断时间,时间到,提示,时间不到,提示正在测温
             *
             *
             * */
            //如果是仅测温模式的话,则判断数值是否超过阈值,如果超过则开始判断延迟时间
            if (mCurrModel == Constants.Model.MODEL_TEMPERATURE_ONLY) {
                //如果大于阈值则开始存最高值
                if (measuringTemperatureF >= mTempMinThreshold) {
                    //收起海报界面
                    if (adsFragment != null) {
                        adsFragment.detectFace();
                    }
                    //如果检测温度大于当前缓存的最高温度则保存
                    if (measuringTemperatureF > mCacheTemperatureHighestValue) {
                        mCacheTemperatureHighestValue = formatF(measuringTemperatureF);
                    }
                    //如果当前缓存时间是0则重置
                    if (mCacheTime == 0) {
                        mCacheTime = System.currentTimeMillis();
                        temperatureUpdateHandler.postDelayed(temperatureUpdateRunnable, 400);
                        return;
                    } else if (System.currentTimeMillis() - mCacheTime < mGetTempDelayTime) {
                        UIUtils.showShort(WelComeActivity.this, "正在测温");
                        temperatureUpdateHandler.postDelayed(temperatureUpdateRunnable, 400);
                        return;
                    }
                } else {//如果温度小于阈值则归零
                    mCacheTemperatureHighestValue = 0f;
                    mCacheValueForTempModel = 0f;
                    mCacheTime = 0;
                }

                //在缓存值大于
                if (mCacheTemperatureHighestValue >= mTempMinThreshold) {
                    if (mCacheValueForTempModel == 0f) {
                        Bitmap currCameraFrame = faceView.getCurrCameraFrame();
                        if (currCameraFrame != null) {
                            mCacheValueForTempModel = mCacheTemperatureHighestValue;
                            boolean isWarning = mCacheTemperatureHighestValue >= mTempWarningThreshold;

                            Sign temperatureSign = SignManager.instance().getTemperatureSign(mCacheTemperatureHighestValue);
                            temperatureSign.setImgBitmap(currCameraFrame);

                            //提示
                            playTips(isWarning, "");
                            //更新记录
                            if (signListFragment != null) {
                                signListFragment.addSignData(temperatureSign);
                            }
                            //上传记录
                            SignManager.instance().uploadTemperatureSign(temperatureSign);
                        }
                    }
                } else {
                    mCacheTemperatureHighestValue = 0f;
                    mCacheValueForTempModel = 0f;
                    mCacheTime = 0;
                }

            } else if (mCurrModel == Constants.Model.MODEL_FACE_TEMPERATURE) {
                if (canDetection) {
                    if (measuringTemperatureF > mCacheTemperatureHighestValue) {
                        mCacheTemperatureHighestValue = formatF(measuringTemperatureF);
                    }
                } else {
                    if (mCacheTemperatureHighestValue != 0.0f) {
                        mCacheTemperatureHighestValue = 0.0f;
                    }
                }
            }

            temperatureUpdateHandler.postDelayed(temperatureUpdateRunnable, 400);
        }
    };

    //关闭测温串口
    private void closeUpdateTemperatureRunnable() {
        temperatureUpdateHandler.removeCallbacks(temperatureUpdateRunnable);
        InfraredTemperatureUtils.getIns().closeSerialPort();
    }

    private Message tipsMessage;

    //设置温度提示框的内容
    private void showTemperatureTips(final String tips, final int backgroundId, int time) {
        //如果未显示，则显示
        if (!tvTempTips.isShown()) {
            tempTipsHandler.sendEmptyMessage(1);
        }

        Bundle bundle = new Bundle();
        bundle.putString("tips", tips);
        bundle.putInt("background", backgroundId);
        tipsMessage = new Message();
        tipsMessage.what = 2;
        tipsMessage.setData(bundle);
        tempTipsHandler.sendMessage(tipsMessage);

        tempTipsHandler.sendEmptyMessageDelayed(0, time);
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

    private void playTips(boolean isWarning, String signName) {
        String tip;
        Runnable warningRunnable;
        int bgId;
        //体温正常
        if (isWarning) {
            tip = "体温异常";
            bgId = R.drawable.shape_main_frame_temperature_warning;
            warningRunnable = new Runnable() {
                @Override
                public void run() {
                    KDXFSpeechManager.instance().playWaningRing();
                }
            };
            ledRed();
        } else {
            tip = "体温正常";
            bgId = R.drawable.shape_main_frame_temperature_normal;
            warningRunnable = null;
            KDXFSpeechManager.instance().stopNormal();
            KDXFSpeechManager.instance().stopWarningRing();
            ledGreen();//显示绿灯
        }

        tip += mCacheTemperatureHighestValue + "℃";

        int time = SpUtils.getIntOrDef(SpUtils.TEMP_TIPS_TIME, 7000);
        Log.e(TAG, "playTips: 延迟关闭时间：" + time);
        resetLedDelay(time);//5秒后重置灯光为蓝色
        showTemperatureTips(tip, bgId, time);
        KDXFSpeechManager.instance().playNormal((TextUtils.isEmpty(signName) ? "" : signName) + tip, warningRunnable);
    }

    /****温控检测区域************************************************************************************************/

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