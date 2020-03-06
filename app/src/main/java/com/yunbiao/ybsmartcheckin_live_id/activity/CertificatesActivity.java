package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
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

import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.LivenessInfo;
import com.yunbiao.faceview.CertificatesView;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.ReadCardUtils;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.DisplayOrientationEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.ResetLogoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateMediaEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseGpioActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.business.VipDialogManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.serialport.InfraredTemperatureUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.IDCardReader;
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

public class CertificatesActivity extends BaseGpioActivity {

    private static final String TAG = "CertificatesActivity";
    private ImageView ivFace;
    private ImageView ivIdCard;
    private CertificatesView certificatesView;
    private String mCurrPortPath;
    private int mCurrBaudRate;
    private Float mAmbCorrValue;
    private Float mTempCorrValue;
    private ImageView ivHotImage;
    private Float mTempWarningThreshold;
    private int mCurrBodyMinT;
    private int mCurrBodyMaxT;
    private int mCurrBodyPercent;
    private boolean mThermalImgMirror;
    private TextView tvName;
    private TextView tvOrigin;
    private TextView tvTip;
    private TextView tvSimilar;
    private TextView tvTemp;
    private TextView tvOStatus;
    private ImageView ivStatus;
    private View verifyStatusTip;
    private TextView tvVerifyInfo;
    private ImageView ivVerifyStatus;
    private ServiceManager serviceManager;
    private ImageView ivLogo;
    private View llBgVerifyStatus;
    private ReadCardUtils readCardUtils;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_ai_temp_verify;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_ai_temp_verify;
    }

    @Override
    protected void initView() {
        APP.setMainActivity(this);
        EventBus.getDefault().register(this);
        ivLogo = findViewById(R.id.iv_logo);
        ivVerifyStatus = findViewById(R.id.iv_verifyStatus);
        tvVerifyInfo = findViewById(R.id.iv_verifyInfo);
        llBgVerifyStatus = findViewById(R.id.ll_bg_verifyStatus);
        tvTemp = findViewById(R.id.tv_tem);
        tvOStatus = findViewById(R.id.tv_ostatus);
        ivStatus = findViewById(R.id.iv_status);
        verifyStatusTip = findViewById(R.id.view_temp_verify);
        tvSimilar = findViewById(R.id.tv_alike);
        tvTip = findViewById(R.id.tv_tip);
        tvName = findViewById(R.id.tv_name);
        tvOrigin = findViewById(R.id.tv_origin);
        ivHotImage = findViewById(R.id.iv_hot_image);
        certificatesView = findViewById(R.id.certificates_view);
        ivFace = findViewById(R.id.iv_face);
        ivIdCard = findViewById(R.id.iv_idCard);

        ivLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goSetting();
            }
        });
    }

    @Override
    protected void initData() {
        initCardReader();

        KDXFSpeechManager.instance().init(this);

        startXmpp();

        certificatesView.setCallback(faceCallback);
        IDCardReader.getInstance().startReaderThread(this, readListener);
        /*红外模块是9600，热成像模块是115200*/
        mCurrPortPath = SpUtils.getStr(SpUtils.PORT_PATH, Constants.DEFAULT_PORT_PATH);
        mCurrBaudRate = SpUtils.getIntOrDef(SpUtils.BAUD_RATE, Constants.BaudRate.THERMAL_IMAGING_BAUD_RATE);
        InfraredTemperatureUtils.getIns().initSerialPort(mCurrPortPath, mCurrBaudRate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAmbCorrValue = SpUtils.getFloat(SpUtils.AMB_CORRECT_VALUE, Constants.DEFAULT_AMB_CORRECT_VALUE);//环境温度补正
        mTempCorrValue = SpUtils.getFloat(SpUtils.TEMP_CORRECT_VALUE, Constants.DEFAULT_TEMP_CORRECT_VALUE);//体温检测补正
        mTempWarningThreshold = SpUtils.getFloat(SpUtils.TEMP_WARNING_THRESHOLD, Constants.DEFAULT_TEMP_WARNING_THRESHOLD_VALUE); //测温报警阈值
        mCurrBodyMinT = SpUtils.getIntOrDef(SpUtils.BODY_MIN_T, Constants.DEFAULT_BODY_MIN_T_VALUE);//最低体温值
        mCurrBodyMaxT = SpUtils.getIntOrDef(SpUtils.BODY_MAX_T, Constants.DEFAULT_BODY_MAX_T_VALUE);//最高体温值
        mCurrBodyPercent = SpUtils.getIntOrDef(SpUtils.BODY_PERCENT, Constants.DEFAULT_BODY_PERCENT_VALUE);//身体占比
        mThermalImgMirror = SpUtils.getBoolean(SpUtils.THERMAL_IMAGE_MIRROR, Constants.DEFAULT_THERMAL_IMAGE_MIRROR);//热成像图像镜像

        //初始化测温模块
        String portPath = SpUtils.getStr(SpUtils.PORT_PATH, Constants.DEFAULT_PORT_PATH);
        if (!TextUtils.equals(portPath, mCurrPortPath)) {
            mCurrPortPath = portPath;
            InfraredTemperatureUtils.getIns().initSerialPort(mCurrPortPath, mCurrBaudRate);
        }


        //设置测温补正值
        InfraredTemperatureUtils.getIns().setaCorrectionValue(mAmbCorrValue);
        InfraredTemperatureUtils.getIns().setmCorrectionValue(mTempCorrValue);

        certificatesView.resume();

        startThermalImaging();

        certificatesView.resume();
    }

    private long mCacheSetImageTime = 0;
    private CertificatesView.FaceCallback faceCallback = new CertificatesView.FaceCallback() {

        @Override
        public void onReady() {
            SyncManager.instance().requestCompany();
        }

        @Override
        public void onFaceBitmap(Bitmap faceBitmap) {
            if (faceBitmap == null) {
                ivFace.setImageResource(R.mipmap.icon_face_moren);
                ivIdCard.setImageResource(R.mipmap.icon_idcard_moren);
                return;
            }
            ivFace.setImageBitmap(faceBitmap);
        }

        @Override
        public boolean onFaceDetection(boolean hasFace, FaceInfo FaceInfo) {
            if (hasFace) {
                if (mCacheSetImageTime == 0 || System.currentTimeMillis() - mCacheSetImageTime > 2000) {
                    mCacheSetImageTime = System.currentTimeMillis();
                    Bitmap faceBitmap = certificatesView.getFaceBitmap(FaceInfo);
                    ivFace.setImageBitmap(faceBitmap);
                }
                sendFaceCompare();
            } else {
                isPlayFace = false;
                mCacheSetImageTime = 0;
                ivFace.setImageResource(R.mipmap.icon_face_moren);
            }
            return false;
        }

        @Override
        public void onCompareResult(boolean hasIdCard, float similar, boolean isPass) {

        }
    };

    private FaceFeature idCardFeature = null;
    private IdCardMsg mIdCardMsg = null;
    private IDCardReader.ReadListener readListener = new IDCardReader.ReadListener() {
        @Override
        public void getCardInfo(IdCardMsg msg) {
            if (msg.name == null || msg.ptoto == null) {
                Log.e(TAG, "getCardInfo: 请重新读卡");
                return;
            }
            mIdCardMsg = msg;
            byte[] bytes = IDCardReader.getInstance().decodeToBitmap(msg.ptoto);
            final Bitmap bp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            ivIdCard.setImageBitmap(bp);

            tvName.setText(msg.name);


            String nativeplace = IDCardReader.getNativeplace(msg.id_num);
            tvOrigin.setText(nativeplace);


            idCardFeature = certificatesView.inputIdCard(bp);
            sendIdCardCompareMessage();
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1://人脸识别请求对比
                    if (idCardFeature == null) {
                        tvTip.setText("请将身份证置于刷卡区");
                    } else {
                        sendCompareMessage(1000);
                    }
                    break;
                case 2://刷卡器请求对比
                    if (!certificatesView.hasFace()) {
                        //如果一直没有人脸，则3秒后清除证件信息
                        tvTip.setText("请面向摄像头");
                        playText("请面向摄像头", null);
                        sendResetMessage(3000);
                    } else {
                        sendCompareMessage(1000);
                    }
                    break;
                case -2:
                    reset();
                    break;
                default:
                    boolean live = certificatesView.getLivenessInfo() == LivenessInfo.ALIVE;
                    if (!live) {
                        ledRed();
                        playText("活体认证失败，请重试", null);
                        tvTip.setText("活体检测失败");
                        sendResetMessage(300);
                        break;
                    }
                    FaceFeature faceFeature = certificatesView.getFaceFeature();
                    FaceSimilar compare = certificatesView.compare(faceFeature, idCardFeature);
                    int similar = (int) (compare.getScore() * 100);
                    boolean isPass = compare.getScore() >= certificatesView.SIMILAR_THRESHOLD;
                    tvSimilar.setText("相似度" + similar + "%");
                    if (!isPass) {
                        ledRed();
                        showVerifyTips(false);
                        tvTip.setText("人证不匹配，请重试");
                        playText("人证不匹配", new Runnable() {
                            @Override
                            public void run() {
                                sendResetMessage(2000);
                            }
                        });
                        break;
                    }

                    if (mTemperatureCacheList.size() <= 0) {
                        ledRed();
                        showVerifyTips(false);
                        tvTip.setText("测温失败，请重试");
                        playText("测温失败，请重试", new Runnable() {
                            @Override
                            public void run() {
                                sendResetMessage(500);
                            }
                        });
                        break;
                    }

                    Float maxValue = Collections.max(mTemperatureCacheList);
                    boolean isWarning = maxValue >= mTempWarningThreshold;
                    setTempTextTip(maxValue, isWarning);

                    Bitmap faceBitmap = ((BitmapDrawable) ivFace.getDrawable()).getBitmap();
                    Bitmap idCardBitmap = ((BitmapDrawable) ivIdCard.getDrawable()).getBitmap();

                    Runnable runnable = null;
                    String tip;
                    if (isWarning) {
                        ledRed();
                        tip = "人证匹配通过，体温异常";
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                KDXFSpeechManager.instance().playWaningRing();
                                sendResetMessage(500);
                            }
                        };
                    } else {
                        ledGreen();
                        tip = "人证匹配通过，体温正常";
                        openDoor();
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                sendResetMessage(500);
                            }
                        };
                    }
                    tvTip.setText(tip);
                    playText(tip, runnable);


                    SignManager.instance().uploadIdCardAndReImage(maxValue, mIdCardMsg, similar, (isPass && !isWarning ? 0 : 1), idCardBitmap, faceBitmap, mCacheHotImage);
                    break;

            }
        }
    };

    //发送对比命令
    private void sendCompareMessage(int delay) {
        handler.removeMessages(-2);
        tvTip.setText("正在验证");
        playText("正在验证", null);
        handler.removeMessages(0);
        handler.sendEmptyMessageDelayed(0, delay);
    }

    //发送重置命令
    private void sendResetMessage(int delay) {
        handler.removeMessages(-2);
        handler.sendEmptyMessageDelayed(-2, delay);
    }

    private void showTempTips(float temp, boolean isWarning) {
        Log.e(TAG, "showTempTips: 11111111111111111111111");
        tvTemp.setText(temp + "℃");
        if (!isWarning) {
            tvOStatus.setText("正常");
            tvOStatus.setTextColor(Color.GREEN);
            tvTemp.setTextColor(Color.GREEN);
            ivStatus.setImageResource(R.mipmap.icon_normal);
        } else {
            tvOStatus.setText("异常");
            tvOStatus.setTextColor(Color.RED);
            tvTemp.setTextColor(Color.RED);
            ivStatus.setImageResource(R.mipmap.icon_warning);
        }
    }

    private void showVerifyTips(boolean isPass) {
        if (!verifyStatusTip.isShown()) {
            verifyStatusTip.setVisibility(View.VISIBLE);
        }
        llBgVerifyStatus.setBackgroundResource(isPass ? R.mipmap.bg_verify_pass : R.mipmap.bg_verify_nopass);
        ivVerifyStatus.setImageResource(isPass ? R.mipmap.icon_verify_pass : R.mipmap.icon_verify_nopass);
        tvVerifyInfo.setText(isPass ? "可以通行" : "禁止通行");
    }

    private void reset() {
        resetLedDelay(0);
        idCardFeature = null;
        mIdCardMsg = null;
        ivIdCard.setImageResource(R.mipmap.icon_idcard_moren);
        ivFace.setImageResource(R.mipmap.icon_face_moren);
        tvTip.setText("");
        tvSimilar.setText("");
        tvName.setText("");
        tvOrigin.setText("");
        tvTemp.setText("");
        tvOStatus.setText("");
        ivStatus.setImageBitmap(null);
        if (verifyStatusTip.isShown()) {
            verifyStatusTip.setVisibility(View.GONE);
        }
    }

    //发送人脸对比命令
    private boolean isPlayFace = false;

    private void sendFaceCompare() {
        if (isPlayFace) {
            return;
        }
        isPlayFace = true;
        handler.sendEmptyMessage(1);
    }

    //发送身份证对比命令
    private void sendIdCardCompareMessage() {
        handler.sendEmptyMessage(2);
    }

    //设置文字状态提示
    private void setTempTextTip(float temp, boolean isWarning) {
        showTempTips(temp, isWarning);
        showVerifyTips(!isWarning);
    }

    //播放语音文字
    private void playText(String text, Runnable runnable) {
        KDXFSpeechManager.instance().stopNormal();
        KDXFSpeechManager.instance().playNormal(text, runnable);
    }

    //=热成像测温逻辑==================================================
    private void startThermalImaging() {
        Log.e(TAG, "startThermalImaging: 开始执行热成像逻辑");
        InfraredTemperatureUtils.getIns().closeHotImage3232();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InfraredTemperatureUtils.getIns().startHotImage3232(mThermalImgMirror, mCurrBodyMinT, mCurrBodyMaxT, mCurrBodyPercent, hotImageDataCallBack);
            }
        }, 1000);
    }

    private List<Float> mTemperatureCacheList = new ArrayList<>();
    private Bitmap mCacheHotImage = null;
    private InfraredTemperatureUtils.HotImageDataCallBack hotImageDataCallBack = new InfraredTemperatureUtils.HotImageDataCallBack() {
        @Override
        public void newestHotImageData(final Bitmap imageBmp, final float sensorT, final float maxT, final float minT, final float bodyMaxT, final boolean isBody, final int bodyPercentage) {
            mCacheHotImage = imageBmp;

            if (ivHotImage != null) {
                ivHotImage.post(new Runnable() {
                    @Override
                    public void run() {
                        ivHotImage.setImageBitmap(imageBmp);
                    }
                });
            }

            if (isBody) {
                if (bodyMaxT <= 0) {
                    return;
                }

                //如果缓存集合中包含这个温度则不再添加
                if (!mTemperatureCacheList.contains(bodyMaxT)) {
                    if (mTemperatureCacheList.size() > 0) {
                        Float maxValue = Collections.max(mTemperatureCacheList);
                        if (bodyMaxT > maxValue) {
                            mTemperatureCacheList.add(bodyMaxT);
                        }
                    } else {
                        mTemperatureCacheList.add(bodyMaxT);
                    }
                }
            } else {
                mTemperatureCacheList.clear();
            }
        }
    };

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

    @Override
    protected void onPause() {
        super.onPause();
        certificatesView.pause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent event) {

        Log.e(TAG, "update: 11111111111111");
        Company company = SpUtils.getCompany();

        EventBus.getDefault().post(new UpdateMediaEvent());

        ImageFileLoader.i().loadAndSave(this, company.getComlogo(), Constants.DATA_PATH, ivLogo);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(DisplayOrientationEvent event) {
        Log.e(TAG, "update: 收到摄像头更新事件");
        certificatesView.changeAngle();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(ResetLogoEvent event) {
        Company company = SpUtils.getCompany();
        ImageFileLoader.i().loadAndSave(this, company.getComlogo(), Constants.DATA_PATH, ivLogo);
    }

    //密码弹窗
    private void inputPwd(final Runnable runnable) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_input_pwd);

        final Animation animation = AnimationUtils.loadAnimation(CertificatesActivity.this, R.anim.anim_edt_shake);
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
        certificatesView.setSimilarThreshold();
    }

    private void goSetting() {
        String pwd = SpUtils.getStr(SpUtils.MENU_PWD);
        if (!TextUtils.isEmpty(pwd)) {
            inputPwd(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(CertificatesActivity.this, SystemActivity.class));
                }
            });
            return;
        }
        startActivity(new Intent(CertificatesActivity.this, SystemActivity.class));
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
    protected void onDestroy() {
        super.onDestroy();
        closeCardReader();
        InfraredTemperatureUtils.getIns().closeSerialPort();
        destoryXmpp();
        certificatesView.destory();
        EventBus.getDefault().unregister(this);
        SyncManager.instance().destory();
        KDXFSpeechManager.instance().destroy();
    }

    /**
     * 读卡器初始化
     */
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

    private ReadCardUtils.OnReadSuccessListener readCardListener = new ReadCardUtils.OnReadSuccessListener() {
        @Override
        public void onScanSuccess(String barcode) {
            Log.e(TAG, "onScanSuccess: " + barcode);

        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (ReadCardUtils.isInputFromReader(this, event)) {
            if (readCardUtils != null) {
                readCardUtils.resolveKeyEvent(event);
            }
        }
        return super.dispatchKeyEvent(event);
    }

}
