package com.yunbiao.ybsmartcheckin_live_id.activity_certificates;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
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

import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.callback.HotImageK1604CallBack;
import com.intelligence.hardware.temperature.callback.HotImageK3232CallBack;
import com.yunbiao.faceview.CertificatesView;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.ReadCardUtils;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.DisplayOrientationEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.ResetLogoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateMediaEvent;
import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.IdCardMsg;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseGpioActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.serialport.InfraredTemperatureUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.IDCardReader;
import com.yunbiao.ybsmartcheckin_live_id.utils.NetWorkChangReceiver;
import com.yunbiao.ybsmartcheckin_live_id.utils.RestartAPPTool;
import com.yunbiao.ybsmartcheckin_live_id.utils.ScanKeyManager;
import com.yunbiao.ybsmartcheckin_live_id.utils.SoftKeyBoardListener;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.yunbiao.ybsmartcheckin_live_id.views.ImageFileLoader;
import com.yunbiao.ybsmartcheckin_live_id.xmpp.ServiceManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

import okhttp3.Call;
import okhttp3.Request;

public class CertificatesActivity extends BaseGpioActivity {

    private static final String TAG = "CertificatesActivity";
    private ImageView ivFace;
    private ImageView ivIdCard;
    private CertificatesView certificatesView;
    private ImageView ivHotImage;
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
    private ConcurrentLinkedQueue<FaceFeature> featureCacheList;
    private TextView tvOriginT;
    private TextView tvLeftTopTemp;
    private Button btnNoIdCard;
    private boolean mThermalMirror;
    private Float mWarningThreshold;
    private Float mCorrectValue;
    private boolean mLowTemp;
    private int mMode;
    private int mSimilar;
    private float mMinThreshold;
    private View tvBottomTitle;
    private TextView tvNetState;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_ai_temp_verify;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_ai_temp_verify_h;
    }

    @Override
    protected void initView() {
        APP.setMainActivity(this);
        EventBus.getDefault().register(this);
        tvNetState = findViewById(R.id.tv_net_state_certi);
        tvLeftTopTemp = findViewById(R.id.tv_temp_main);
        tvOriginT = findViewById(R.id.tv_origin_t);
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
        btnNoIdCard = findViewById(R.id.btn_no_id_card);
        tvBottomTitle = findViewById(R.id.tv_bottomTitle);
        btnNoIdCard.setOnClickListener(onClickListener);
        ivLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goSetting();
            }
        });

        TIP_READ_ID_CARD = getResources().getString(R.string.act_certificates_please_set_id_card);

        if(Constants.isHT){
            ImageFileLoader.setDefaultLogoId(R.mipmap.logo_icon_horizontal);
            ivLogo.setImageResource(R.mipmap.logo_icon_horizontal);
            tvBottomTitle.setVisibility(View.GONE);
        } else {
            ImageFileLoader.setDefaultLogoId(R.mipmap.logo);
            ivLogo.setImageResource(R.mipmap.logo);
            tvBottomTitle.setVisibility(View.VISIBLE);
        }

        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            btnNoIdCard.setVisibility(View.GONE);
        }

        setDeviceInfo();

        registerNetState();
    }

    private void registerNetState(){
        NetWorkChangReceiver netWorkChangReceiver = new NetWorkChangReceiver(new NetWorkChangReceiver.NetWorkChangeListener() {
            @Override
            public void connect() {
                tvNetState.setText("网络正常");
                tvNetState.setTextColor(Color.GREEN);
            }

            @Override
            public void disConnect() {
                tvNetState.setText("无网络");
                tvNetState.setTextColor(Color.RED);
            }
        });
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkChangReceiver, filter);

    }

    private void setDeviceInfo(){
        TextView tvDeviceNumber = findViewById(R.id.tv_device_number_certi);
        TextView tvBindCode = findViewById(R.id.tv_bind_code_certi);
        TextView tvAppVersion = findViewById(R.id.tv_app_version_certi);
        if(tvDeviceNumber != null){
            String deviceNumber = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
            tvDeviceNumber.setText(deviceNumber);
        }
        if(tvBindCode != null){
            String bindeCode = SpUtils.getStr(SpUtils.BIND_CODE);
            tvBindCode.setText(bindeCode);
        }
        if(tvAppVersion != null){
            String appVersion = CommonUtils.getAppVersion(this);
            tvAppVersion.setText(appVersion);
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.setEnabled(false);
            tvTip.setText(getResources().getString(R.string.act_certificates_testing));
            getTempHandler.sendEmptyMessageDelayed(0, 100);
        }
    };

    @Override
    protected void initData() {
        initCardReader();

        KDXFSpeechManager.instance().init(this);

        startXmpp();

        certificatesView.setCallback(faceCallback);
        IDCardReader.getInstance().startReaderThread(this, readListener);
        featureCacheList = new ConcurrentLinkedQueue();
    }

    @Override
    protected void onResume() {
        super.onResume();

        certificatesView.resume();
        //热成像镜像
        mThermalMirror = SpUtils.getBoolean(CertificatesConst.Key.THERMAL_MIRROR, CertificatesConst.Default.THERMAL_MIRROR);
        //报警值
        mWarningThreshold = SpUtils.getFloat(CertificatesConst.Key.WARNING_THRESHOLD, CertificatesConst.Default.WARNING_THRESHOLD);
        //最小值
        mMinThreshold = SpUtils.getFloat(CertificatesConst.Key.MIN_THRESHOLD,CertificatesConst.Default.MIN_THRESHOLD);
        //温度补偿
        mCorrectValue = SpUtils.getFloat(CertificatesConst.Key.CORRECT_VALUE, CertificatesConst.Default.CORRECT_VALUE);
        //低温模式
        mLowTemp = SpUtils.getBoolean(CertificatesConst.Key.LOW_TEMP, CertificatesConst.Default.LOW_TEMP);
        mMode = SpUtils.getIntOrDef(CertificatesConst.Key.MODE, CertificatesConst.Default.MODE);
        //相似度
        mSimilar = SpUtils.getIntOrDef(CertificatesConst.Key.SIMILAR, CertificatesConst.Default.SIMILAR);

        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            String port = "/dev/ttyS3";
            TemperatureModule.getIns().initSerialPort(this, port, 19200);
            resultHandler.postDelayed(() -> TemperatureModule.getIns().startHotImageK1604(mThermalMirror, mLowTemp, hotImageK1604CallBack),2000);
        } else {
            String port = "/dev/ttyS4";
            resultHandler.postDelayed(() -> {
                TemperatureModule.getIns().initSerialPort(CertificatesActivity.this, port, 115200);
                resultHandler.postDelayed(() -> TemperatureModule.getIns().startHotImageK3232(mThermalMirror, mLowTemp, hotImageK3232CallBack), 2000);
            },2000);
        }

        TemperatureModule.getIns().setmCorrectionValue(mCorrectValue);
    }

    @Override
    protected void onStop() {
        super.onStop();
        TemperatureModule.getIns().closeHotImageK3232();
    }

    private HotImageK1604CallBack hotImageK1604CallBack = new HotImageK1604CallBack() {
        @Override
        public void newestHotImageData(final Bitmap bitmap, final float originalMaxT, final float afterF, final float minT) {
            mCacheHotImage = bitmap;
            if (ivHotImage != null) {
                ivHotImage.post(() -> {
                    ivHotImage.setImageBitmap(bitmap);
                    tvLeftTopTemp.setText(getResources().getString(R.string.act_certificates_temper) + originalMaxT + "℃");
                });
            }

            if (isCode && mHasFace) {
                Log.e(TAG, "newestHotImageData: 正在取温");
                mCodeTempList.add(afterF);
                return;
            }
            if(mCodeTempList.size() > 0){
                mCodeTempList.clear();
            }

            if (certificatesView.hasFace()) {
                //如果缓存集合中包含这个温度则不再添加
                if (mTemperatureCacheList.size() > 10) {
                    mTemperatureCacheList.remove(0);
                }
                mTemperatureCacheList.add(afterF);
            } else {
                mTemperatureCacheList.clear();
            }
        }
    };

    private HotImageK3232CallBack hotImageK3232CallBack = new HotImageK3232CallBack() {
        @Override
        public void newestHotImageData(final Bitmap bitmap, float measureF, final float afterF, float v2, float v3, boolean b, int i) {
            mCacheHotImage = bitmap;

            if (ivHotImage != null) {
                ivHotImage.post(new Runnable() {
                    @Override
                    public void run() {
                        ivHotImage.setImageBitmap(bitmap);
                        tvLeftTopTemp.setText(getResources().getString(R.string.act_certificates_temper) + afterF + "℃");
                    }
                });
            }

            if (isCode && mHasFace) {
                Log.e(TAG, "newestHotImageData: 正在取温");
                mCodeTempList.add(afterF);
                return;
            }
            if(mCodeTempList.size() > 0){
                mCodeTempList.clear();
            }

            if (certificatesView.hasFace()) {
                //如果缓存集合中包含这个温度则不再添加
                if (mTemperatureCacheList.size() > 10) {
                    mTemperatureCacheList.remove(0);
                }
                mTemperatureCacheList.add(afterF);
            } else {
                mTemperatureCacheList.clear();
            }
        }
    };

    private IDCardReader.ReadListener readListener = new IDCardReader.ReadListener() {
        @Override
        public void getCardInfo(IdCardMsg msg) {
            if (isCode) {
                tvTip.setText(getResources().getString(R.string.act_certificates_please_wait));
                return;
            }
            if (msg.name == null || msg.ptoto == null) {
                String tips = getResources().getString(R.string.act_certificates_idcard_failed);
                tvTip.setText(tips);
                KDXFSpeechManager.instance().playNormal(tips);
                return;
            }
            KDXFSpeechManager.instance().playPassRing();
            setIdCardImage(msg);
        }
    };

    private boolean mHasFace = false;
    private CertificatesView.FaceCallback faceCallback = new CertificatesView.FaceCallback() {

        @Override
        public void onReady() {
            SyncManager.instance().requestOnlyCompany();
        }

        @Override
        public void onFaceDetection(boolean hasFace, FaceInfo faceInfo) {
            mHasFace = hasFace;
            if (isCode) {
                return;
            }
            if (hasFace) {
                setFaceImage(faceInfo);
            } else {
                removeFaceImage();
            }
        }
    };


    /*
     * 以mCacheFaceId为判断依据
     * 在刷卡时判断是否有人脸,如果有,则将mCacheFaceId置为-1,代表先刷卡
     * 1.mCacheFaceId为-1时,代表初始化或者先刷卡,不清除身份证信息
     * 2.如果此时人脸出去再进来,此时mCacheFaceId肯定不为-1,此时判断为下一个人,清除身份信息
     *
     * */
    private long mSetFaceImageTime = 0;//缓存时间,两秒刷新一次
    private int mCacheFaceId = -1;//缓存ID
    private int currFaceImageId = -1;//当前设置的ImageId,避免重复绘制消耗性能

    //显示人脸头像
    private void setFaceImage(FaceInfo faceInfo) {
        if (mSetFaceImageTime == 0 || System.currentTimeMillis() - mSetFaceImageTime > 2000) {
            mSetFaceImageTime = System.currentTimeMillis();
            Bitmap faceBitmap = certificatesView.getFaceBitmap(faceInfo);
            ivFace.setImageBitmap(faceBitmap);
            currFaceImageId = -1;


            //如果缓存为-1,代表先刷卡,不清楚身份证信息
            if (mCacheFaceId == -1) {
                mCacheFaceId = faceInfo.getFaceId();
                startCompareThread();
            } else
                //如果缓存ID不为-1,代表后进来的人,则清除身份证信息
                if (mCacheFaceId != faceInfo.getFaceId()) {
                    mCacheFaceId = faceInfo.getFaceId();
                    mSetFaceImageTime = 0;
                    mIdCardBitmap = null;
                    closeCompareThread();
                    resultHandler.removeMessages(2);
                    clearUITips();
                    startCompareThread();
                }
        }
    }

    //人脸移除时关闭对比线程清除缓存时间,不控制mCacheId的变化
    private void removeFaceImage() {
        //清除人脸的时候不清除缓存ID
        mSetFaceImageTime = 0;
        closeCompareThread();
        featureCacheList.clear();
    }

    private Bitmap mIdCardBitmap = null;
    private IdCardMsg mIdCardMsg = null;

    private void setIdCardImage(IdCardMsg idCardMsg) {
        closeCompareThread();
        resultHandler.removeMessages(2);

        mIdCardMsg = idCardMsg;
        byte[] bytes = IDCardReader.getInstance().decodeToBitmap(idCardMsg.ptoto);
        mIdCardBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        String nativeplace = IDCardReader.getNativeplace(idCardMsg.id_num);
        //由此判断是先刷脸还是先刷卡
        if (!certificatesView.hasFace()) {
            sendSpeechTip(getResources().getString(R.string.act_certificates_please_look_camera));
            clearUITips();
            mCacheFaceId = -1;
            resultHandler.removeMessages(-1);
            resultHandler.sendEmptyMessageDelayed(-1, 8000);
        } else {
            mCacheFaceId = -1;
            startCompareThread();
        }

        ivIdCard.setImageBitmap(mIdCardBitmap);
        tvName.setText(idCardMsg.name);
        tvOriginT.setText(getResources().getString(R.string.act_certificates_native_place));
        tvOrigin.setText(nativeplace);
    }

    //对比线程
    private boolean isRunning = false;
    private Thread compareThread = null;

    private String TIP_READ_ID_CARD;

    private void startCompareThread() {
        if (compareThread != null && compareThread.isAlive()  && isRunning) {
            return;
        }
        resultHandler.removeMessages(-1);
        isRunning = true;
        compareThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    if (mIdCardBitmap == null) {
                        String s = tvTip.getText().toString();
                        if (!TextUtils.equals(s, TIP_READ_ID_CARD)) {
                            sendTextTips(TIP_READ_ID_CARD);
                        }
                        closeCompareThread();
                        continue;
                    }

                    sendSpeechTip(getResources().getString(R.string.act_certificates_verify_ing));

                    Log.e(TAG, "run: 正在验证");
                    //人脸进来后添加人脸特征
                    while (featureCacheList.size() < 5) {
                        Log.e(TAG, "run: 添加人脸");
                        FaceFeature faceFeature = certificatesView.getFaceFeature();
                        if (faceFeature != null) {
                            featureCacheList.add(faceFeature);
                        }
                        if (!certificatesView.hasFace()) {
                            break;
                        }
                    }

                    Log.e(TAG, "继续进行");

                    FaceFeature idCardFeature = certificatesView.inputIdCard(mIdCardBitmap);

                    float finalSimilar = 0;
                    for (FaceFeature faceFeature : featureCacheList) {
                        FaceSimilar compare = certificatesView.compare(faceFeature, idCardFeature);
                        float comapareSimilar = compare.getScore();
                        Log.e(TAG, "run: 对比:" + comapareSimilar);
                        if (comapareSimilar > finalSimilar) {
                            finalSimilar = comapareSimilar;
                        }
                    }

                    int similar = (int) (finalSimilar * 100);
                    sendResult(similar);

                    featureCacheList.clear();
                    closeCompareThread();
                    break;
                }
            }
        });
        compareThread.start();
    }

    private void closeCompareThread() {
        if (isRunning) {
            Log.e(TAG, "startCompareThread: 结束");
            isRunning = false;
            compareThread = null;
        }
    }

    private void sendTextTips(String text) {
        Message message = Message.obtain();
        message.what = -2;
        message.obj = text;
        resultHandler.sendMessage(message);
    }

    private void sendResult(int similar) {
        Message message = Message.obtain();
        message.what = 1;
        message.obj = similar;
        resultHandler.sendMessage(message);
    }

    private void sendSpeechTip(String tip) {
        Message message = Message.obtain();
        message.what = 0;
        message.obj = tip;
        resultHandler.sendMessage(message);
    }

    private Handler resultHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case -2://设置提示
                    String text = (String) msg.obj;
                    tvTip.setText(text);
                    break;
                case -1://自动清除身份信息
                    mIdCardBitmap = null;
                    ivIdCard.setImageBitmap(null);
                    tvName.setText("");
                    tvOrigin.setText("");
                    break;
                case 0://语音提醒
                    KDXFSpeechManager.instance().stopNormal();
                    String tip = (String) msg.obj;
                    tvTip.setText(tip);
                    KDXFSpeechManager.instance().playNormal(tip);
                    break;
                case 1://人证结果提示
                    int similar = (int) msg.obj;

                    Float maxTemp = 0f;
                    if (mTemperatureCacheList != null && mTemperatureCacheList.size() > 0) {
                        maxTemp = Collections.max(mTemperatureCacheList);//取出最大温度
                    }
                    if (maxTemp != 0f) {
                        maxTemp += mCorrectValue;
                    }
                    boolean isVerify = similar >= mSimilar;
                    boolean isNormal = maxTemp != 0f && maxTemp < mWarningThreshold;//是否正常

                    final boolean isPass = isVerify && isNormal;

                    setUITips(isVerify, isNormal, isPass, maxTemp, similar);

                    Bitmap faceBitmap = ((BitmapDrawable) ivFace.getDrawable()).getBitmap();
                    Bitmap idCardBitmap = ((BitmapDrawable) ivIdCard.getDrawable()).getBitmap();
                    SignManager.instance().uploadIdCardAndReImage(maxTemp, mIdCardMsg, similar, (isPass ? 0 : 1), idCardBitmap, faceBitmap, mCacheHotImage);

                    resultHandler.removeMessages(2);
                    resultHandler.sendEmptyMessageDelayed(2, 10000);
                    break;
                case 3://扫码结果提示
                    String entryId = (String) msg.obj;
                    int color = msg.arg1;

                    float temp = mCodeTempList.size() <= 0 ? 0.0f : Collections.max(mCodeTempList);
                    if(temp != 0.0f){
                        temp += mCorrectValue;
                    }

                    boolean isOk = temp != 0f && temp >= 36.0f && temp < mWarningThreshold;//是否正常

                    setUITips(true, isOk, isOk, temp, -1);

                    Bitmap currCameraFrame = certificatesView.getCurrCameraFrame();

                    SignManager.instance().uploadCodeVerifyResult(entryId, (isOk && color == 1), currCameraFrame, temp, mCacheHotImage);

                    resultHandler.removeMessages(4);
                    resultHandler.sendEmptyMessageDelayed(4, 5000);

                    resultHandler.removeMessages(2);
                    resultHandler.sendEmptyMessageDelayed(2, 10000);
                    break;
                case 2://清除UI
                    clearUITips();
                    break;
                case 4://清除扫码状态
                    isCode = false;
                    break;
                default:
                    break;
            }
        }
    };

    private void clearUITips() {
        tvOStatus.setText("");
        ivStatus.setImageBitmap(null);
        tvSimilar.setText("");
        tvTemp.setText("");
        verifyStatusTip.setVisibility(View.GONE);
        tvTip.setText("");
        tvName.setText("");
        tvOrigin.setText("");
        ivIdCard.setImageBitmap(null);
        ivFace.setImageBitmap(null);
        KDXFSpeechManager.instance().stopNormal();
        KDXFSpeechManager.instance().stopWarningRing();
        resetLedDelay(0);
    }

    private void setUITips(boolean isVerify, boolean isNormal, final boolean isPass, float maxTemp, int intSimilar) {
        String message;
        if (isVerify) {
            message = getResources().getString(R.string.act_certificates_verify_pass);
        } else {
            message = getResources().getString(R.string.act_certificates_verify_not_pass);
        }

        if (isNormal) {
            message += getResources().getString(R.string.act_certificates_temper_normal);
            tvOStatus.setText(getResources().getString(R.string.act_certificates_normal));
            tvOStatus.setTextColor(Color.GREEN);
            tvTemp.setTextColor(Color.GREEN);
            ivStatus.setImageResource(R.mipmap.icon_normal);
        } else {
            message += getResources().getString(R.string.act_certificates_temper_warning);
            tvOStatus.setText(getResources().getString(R.string.act_certificates_warning));
            tvOStatus.setTextColor(Color.RED);
            tvTemp.setTextColor(Color.RED);
            ivStatus.setImageResource(R.mipmap.icon_warning);
        }

        if (intSimilar != -1) {
            tvSimilar.setText(getResources().getString(R.string.act_certificates_similar) + intSimilar + "%");
        }
        tvTemp.setText(maxTemp + "℃");
        tvTip.setText(message);

        if (!verifyStatusTip.isShown()) {
            verifyStatusTip.setVisibility(View.VISIBLE);
        }

        if (isPass) {
            message += getResources().getString(R.string.act_certificates_please_pass);
            llBgVerifyStatus.setBackgroundResource(R.mipmap.bg_verify_pass);
            ivVerifyStatus.setImageResource(R.mipmap.icon_verify_pass);
            tvVerifyInfo.setText(getResources().getString(R.string.act_certificates_please_can_pass));
            ledGreen();
        } else {
            message += getResources().getString(R.string.act_certificates_please_not_pass);
            llBgVerifyStatus.setBackgroundResource(R.mipmap.bg_verify_nopass);
            ivVerifyStatus.setImageResource(R.mipmap.icon_verify_nopass);
            tvVerifyInfo.setText(getResources().getString(R.string.act_certificates_please_not_pass));
            ledRed();
        }

        KDXFSpeechManager.instance().playNormal(message, new Runnable() {
            @Override
            public void run() {
                if (isPass) {
                    resetLedDelay(0);
                } else {
                    KDXFSpeechManager.instance().playWaningRing();
                    resetLedDelay(3000);
                }
            }
        });
    }

    private void setUITips2(boolean isNormal, final boolean isPass, float maxTemp) {
        tvName.setText(getResources().getString(R.string.act_certificates_visitor));
        String message = "";
        if (isNormal) {
            message += getResources().getString(R.string.act_certificates_temper_normal);
            tvOStatus.setText(getResources().getString(R.string.act_certificates_normal));
            tvOStatus.setTextColor(Color.GREEN);
            tvTemp.setTextColor(Color.GREEN);
            ivStatus.setImageResource(R.mipmap.icon_normal);
        } else {
            message += getResources().getString(R.string.act_certificates_temper_warning);
            tvOStatus.setText(getResources().getString(R.string.act_certificates_warning));
            tvOStatus.setTextColor(Color.RED);
            tvTemp.setTextColor(Color.RED);
            ivStatus.setImageResource(R.mipmap.icon_warning);
        }

        tvTemp.setText(maxTemp + "℃");
        tvTip.setText(message);

        if (!verifyStatusTip.isShown()) {
            verifyStatusTip.setVisibility(View.VISIBLE);
        }

        if (isPass) {
            message += getResources().getString(R.string.act_certificates_please_pass);
            llBgVerifyStatus.setBackgroundResource(R.mipmap.bg_verify_pass);
            ivVerifyStatus.setImageResource(R.mipmap.icon_verify_pass);
            tvVerifyInfo.setText(getResources().getString(R.string.act_certificates_please_can_pass));
            ledGreen();
        } else {
            message += getResources().getString(R.string.act_certificates_please_not_pass);
            llBgVerifyStatus.setBackgroundResource(R.mipmap.bg_verify_nopass);
            ivVerifyStatus.setImageResource(R.mipmap.icon_verify_nopass);
            tvVerifyInfo.setText(getResources().getString(R.string.act_certificates_please_not_pass));
            ledRed();
        }

        KDXFSpeechManager.instance().playNormal(message, new Runnable() {
            @Override
            public void run() {
                if (isPass) {
                    resetLedDelay(0);
                } else {
                    KDXFSpeechManager.instance().playWaningRing();
                    resetLedDelay(3000);
                }
            }
        });
    }

    //=热成像测温逻辑==================================================

    private List<Float> mTemperatureCacheList = new ArrayList<>();
    private Bitmap mCacheHotImage = null;

    private Handler getTempHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                isCode = true;
                sendEmptyMessageDelayed(1, 2000);
                return;
            } else if (msg.what == 1) {
                isCode = false;
                if (mCodeTempList.size() <= 0) {
                    Log.e(TAG, "handleMessage: 没有获取到温度" );
                    tvTip.setText(getResources().getString(R.string.act_certificates_temper_failed));
                    isCode = false;
                    KDXFSpeechManager.instance().playNormal(getResources().getString(R.string.act_certificates_temper_failed));
                    btnNoIdCard.setEnabled(true);
                    getTempHandler.removeMessages(2);
                    getTempHandler.sendEmptyMessageDelayed(2, 3000);
                    return;
                }

                for (Float aFloat : mCodeTempList) {
                    Log.e(TAG, "handleMessage: 取到的温度：" + aFloat);
                }

                Float max = Collections.max(mCodeTempList);

                if (max < mMinThreshold) {
                    isCode = false;
                    tvTip.setText(getResources().getString(R.string.act_certificates_temper_failed));
                    KDXFSpeechManager.instance().playNormal(getResources().getString(R.string.act_certificates_temper_failed));
                    btnNoIdCard.setEnabled(true);
                    getTempHandler.removeMessages(2);
                    getTempHandler.sendEmptyMessageDelayed(2, 3000);
                    return;
                }
                if(max != 0f){
                    max += mCorrectValue;
                }

                tvTip.setText("");
                boolean isWarning = max >= mWarningThreshold;

                Log.e(TAG, "handleMessage: 最高温度：" + max);

                setUITips2(!isWarning, !isWarning, max);
                btnNoIdCard.setEnabled(true);

                Bitmap currCameraFrame = certificatesView.getCurrCameraFrame();
                SignManager.instance().uploadNoIdCardResult(isWarning ? 1 : 0, currCameraFrame, max, mCacheHotImage);

                getTempHandler.removeMessages(2);
                getTempHandler.sendEmptyMessageDelayed(2, 5000);
            } else if (msg.what == 2) {
                clearUITips();
            }
        }
    };

    private float getMean(List<Float> array) {
        float result = 0.0f;
        if (array.size() == 0) {
            return result;
        }
        for (float anArray : array) {
            result += anArray;
        }
        result = result / array.size();
        result = formatF(result);
        return result;
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

        setDeviceInfo();
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
                    startActivity(new Intent(CertificatesActivity.this, CertificatesSystemActivity.class));
                }
            });
            return;
        }
        startActivity(new Intent(CertificatesActivity.this, CertificatesSystemActivity.class));
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
    private ScanKeyManager scanKeyManager;

    private void initCardReader() {
        //读卡器声明
        scanKeyManager = new ScanKeyManager(new ScanKeyManager.OnScanValueListener() {
            @Override
            public void onScanValue(String value) {
                Log.e(TAG, "onScanValue: 检测到扫码：" + value);

                isCode = true;
                getUserInfoByCode(value);
            }
        });
        onKeyBoardListener();
    }

    private boolean isCode = false;
    private List<Float> mCodeTempList = new ArrayList<>();

    private void getUserInfoByCode(String code) {
        Log.e(TAG, "地址:" + ResourceUpdate.GETUSERINFO_BY_CODE);
        Log.e(TAG, "参数：" + code);
        OkHttpUtils.post()
                .url(ResourceUpdate.GETUSERINFO_BY_CODE)
                .addParams("code", code)
                .build().execute(new StringCallback() {
            @Override
            public void onBefore(Request request, int id) {
                UIUtils.showNetLoading(CertificatesActivity.this);
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG, "onError: " + (e == null ? "NULL" : e.getMessage()));
                UIUtils.showLong(CertificatesActivity.this, "获取信息失败，请重试" + "（ " + (e == null ? "NULL" : e.getMessage()) + "）");

                resultHandler.removeMessages(4);
                resultHandler.sendEmptyMessageDelayed(4, 1000);
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG, "onResponse: 请求结果：" + response);
                GetUserInfo getUserInfo = new Gson().fromJson(response, GetUserInfo.class);
                if (getUserInfo.status != 1) {
                    UIUtils.showLong(CertificatesActivity.this, "获取信息失败" + "（ " + getUserInfo.message + "）");
                    return;
                }
                resultHandler.removeMessages(2);
                clearUITips();

                tvOriginT.setText(getResources().getString(R.string.act_certificates_depart));

                String dept = getUserInfo.dept;
                tvOrigin.setText(TextUtils.isEmpty(dept) ? "" : dept);
                tvName.setText(getUserInfo.name);

                tvTip.setText(getResources().getString(R.string.act_certificates_testing));

                Glide.with(CertificatesActivity.this).load(getUserInfo.head).asBitmap().into(ivIdCard);
                resultHandler.removeMessages(3);
                Message message = Message.obtain();
                message.what = 3;
                message.arg1 = TextUtils.equals("绿色", getUserInfo.color) ? 1 : TextUtils.equals("黄色", getUserInfo.color) ? 1 : 0;
                message.obj = getUserInfo.entryId;
                resultHandler.sendMessageDelayed(message, 1000);
            }

            @Override
            public void onAfter(int id) {
                UIUtils.dismissNetLoading();
            }
        });
    }

    class GetUserInfo {
        int status;
        String entryId;
        String message;
        String color;
        String name;
        String dept;
        String head;
    }

    private void closeCardReader() {
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() != KeyEvent.KEYCODE_BACK && !isInput) {
            scanKeyManager.analysisKeyEvent(event);
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    private boolean isInput = false;

    //监听软件盘是否弹起
    private void onKeyBoardListener() {
        SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                Log.e("软键盘", "键盘显示 高度" + height);
                isInput = true;
            }

            @Override
            public void keyBoardHide(int height) {
                Log.e("软键盘", "键盘隐藏 高度" + height);
                isInput = false;
            }
        });
    }


}
