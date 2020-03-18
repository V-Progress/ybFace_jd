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
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
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
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.serialport.InfraredTemperatureUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.IDCardReader;
import com.yunbiao.ybsmartcheckin_live_id.utils.IdCardMsg;
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
import java.util.concurrent.ConcurrentLinkedQueue;

import okhttp3.Call;
import okhttp3.Request;

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
    private ConcurrentLinkedQueue<FaceFeature> featureCacheList;
    private TextView tvOriginT;
    private TextView tvLeftTopTemp;
    private Button btnNoIdCard;
    private float mTempMinThreshold;

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
        btnNoIdCard.setOnClickListener(onClickListener);
        ivLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goSetting();
            }
        });
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.setEnabled(false);
            tvTip.setText("正在测温，请稍等");
            getTempHandler.sendEmptyMessageDelayed(0, 100);
        }
    };


    private final int DEFAULT_FACE_IMAGE = R.mipmap.icon_face_moren;
    private final int DEFAULT_IDCARD_IMAGE = R.mipmap.icon_idcard_moren;

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

        featureCacheList = new ConcurrentLinkedQueue();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAmbCorrValue = SpUtils.getFloat(SpUtils.AMB_CORRECT_VALUE, Constants.DEFAULT_AMB_CORRECT_VALUE);//环境温度补正
        mTempCorrValue = SpUtils.getFloat(SpUtils.TEMP_CORRECT_VALUE, Constants.DEFAULT_TEMP_CORRECT_VALUE);//体温检测补正
        mTempMinThreshold = SpUtils.getFloat(SpUtils.TEMP_MIN_THRESHOLD, Constants.DEFAULT_TEMP_MIN_THRESHOLD_VALUE);
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

    private IDCardReader.ReadListener readListener = new IDCardReader.ReadListener() {
        @Override
        public void getCardInfo(IdCardMsg msg) {
            if (isCode) {
                tvTip.setText("请等待");
                return;
            }
            if (msg.name == null || msg.ptoto == null) {
                tvTip.setText("证件读取失败,请重试");
                KDXFSpeechManager.instance().playNormal("证件读取失败,请重试");
                return;
            }
            setIdCardImage(msg);
        }
    };

    private CertificatesView.FaceCallback faceCallback = new CertificatesView.FaceCallback() {

        @Override
        public void onReady() {
            SyncManager.instance().requestCompany();
        }

        @Override
        public void onFaceDetection(boolean hasFace, FaceInfo faceInfo) {
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
        if (currFaceImageId != DEFAULT_FACE_IMAGE) {
            currFaceImageId = DEFAULT_FACE_IMAGE;
            featureCacheList.clear();
        }
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
            sendSpeechTip("请正视摄像头");
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
        tvOriginT.setText("籍贯:");
        tvOrigin.setText(nativeplace);
    }

    //对比线程
    private boolean isRunning = false;
    private Thread compareThread = null;

    private String TIP_READ_ID_CARD = "请将身份证置于刷卡区";

    private void startCompareThread() {
        if (compareThread != null && compareThread.isAlive()) {
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

                    sendSpeechTip("正在验证");

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

                    sendResult(finalSimilar);

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

    private void sendResult(float similar) {
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
                    ivIdCard.setImageResource(DEFAULT_IDCARD_IMAGE);
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
                    float similar = (float) msg.obj;

                    int intSimilar = (int) (similar * 100);//取出相似度
                    Float maxTemp = 0f;
                    if (mTemperatureCacheList != null && mTemperatureCacheList.size() > 0) {
                        maxTemp = Collections.max(mTemperatureCacheList);//取出最大温度
                    }
                    boolean isVerify = similar >= CertificatesView.SIMILAR_THRESHOLD;
                    boolean isNormal = maxTemp != 0f && maxTemp < mTempWarningThreshold;//是否正常

                    final boolean isPass = isVerify && isNormal;

                    setUITips(isVerify, isNormal, isPass, maxTemp, intSimilar);

                    Bitmap faceBitmap = ((BitmapDrawable) ivFace.getDrawable()).getBitmap();
                    Bitmap idCardBitmap = ((BitmapDrawable) ivIdCard.getDrawable()).getBitmap();
                    SignManager.instance().uploadIdCardAndReImage(maxTemp, mIdCardMsg, intSimilar, (isPass ? 0 : 1), idCardBitmap, faceBitmap, mCacheHotImage);

                    resultHandler.removeMessages(2);
                    resultHandler.sendEmptyMessageDelayed(2, 10000);
                    break;
                case 3://扫码结果提示
                    String entryId = (String) msg.obj;
                    int color = msg.arg1;

                    float temp = mCodeTempList.size() <= 0 ? 0.0f : Collections.max(mCodeTempList);

                    boolean isOk = temp != 0f && temp >= 36.0f && temp < mTempWarningThreshold;//是否正常

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
        ivIdCard.setImageResource(DEFAULT_IDCARD_IMAGE);
        tvName.setText("");
        tvOrigin.setText("");
        ivFace.setImageResource(DEFAULT_FACE_IMAGE);
        KDXFSpeechManager.instance().stopNormal();
        KDXFSpeechManager.instance().stopWarningRing();
        resetLedDelay(0);
    }

    private void setUITips(boolean isVerify, boolean isNormal, final boolean isPass, float maxTemp, int intSimilar) {
        String message;
        if (isVerify) {
            message = "验证通过,";
        } else {
            message = "人证不匹配,";
        }

        if (isNormal) {
            message += "体温正常";
            tvOStatus.setText("正常");
            tvOStatus.setTextColor(Color.GREEN);
            tvTemp.setTextColor(Color.GREEN);
            ivStatus.setImageResource(R.mipmap.icon_normal);
        } else {
            message += "体温异常";
            tvOStatus.setText("异常");
            tvOStatus.setTextColor(Color.RED);
            tvTemp.setTextColor(Color.RED);
            ivStatus.setImageResource(R.mipmap.icon_warning);
        }

        if (intSimilar != -1) {
            tvSimilar.setText("相似度" + intSimilar + "%");
        }
        tvTemp.setText(maxTemp + "℃");
        tvTip.setText(message);

        if (!verifyStatusTip.isShown()) {
            verifyStatusTip.setVisibility(View.VISIBLE);
        }

        if (isPass) {
            message += ",请通行";
            llBgVerifyStatus.setBackgroundResource(R.mipmap.bg_verify_pass);
            ivVerifyStatus.setImageResource(R.mipmap.icon_verify_pass);
            tvVerifyInfo.setText("可以通行");
            ledGreen();
        } else {
            message += ",禁止通行";
            llBgVerifyStatus.setBackgroundResource(R.mipmap.bg_verify_nopass);
            ivVerifyStatus.setImageResource(R.mipmap.icon_verify_nopass);
            tvVerifyInfo.setText("禁止通行");
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
        tvName.setText("访客");
        String message = "";
        if (isNormal) {
            message += "体温正常";
            tvOStatus.setText("正常");
            tvOStatus.setTextColor(Color.GREEN);
            tvTemp.setTextColor(Color.GREEN);
            ivStatus.setImageResource(R.mipmap.icon_normal);
        } else {
            message += "体温异常";
            tvOStatus.setText("异常");
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
            message += ",请通行";
            llBgVerifyStatus.setBackgroundResource(R.mipmap.bg_verify_pass);
            ivVerifyStatus.setImageResource(R.mipmap.icon_verify_pass);
            tvVerifyInfo.setText("可以通行");
            ledGreen();
        } else {
            message += ",禁止通行";
            llBgVerifyStatus.setBackgroundResource(R.mipmap.bg_verify_nopass);
            ivVerifyStatus.setImageResource(R.mipmap.icon_verify_nopass);
            tvVerifyInfo.setText("禁止通行");
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
                        tvLeftTopTemp.setText("温度：" + bodyMaxT + "℃");
                    }
                });
            }

            if (isCode) {
                mCodeTempList.add(bodyMaxT);
                return;
            }
            mCodeTempList.clear();

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

    private Handler getTempHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                isCode = true;
                sendEmptyMessageDelayed(1, 1000);
                return;
            } else if (msg.what == 1) {

                if (mCodeTempList.size() <= 0) {
                    tvTip.setText("测温失败，请重试");
                    isCode = false;
                    KDXFSpeechManager.instance().playNormal("测温失败，请重试");
                    btnNoIdCard.setEnabled(true);
                    getTempHandler.removeMessages(2);
                    getTempHandler.sendEmptyMessageDelayed(2, 3000);
                    return;
                }

                isCode = false;
                Float mean = getMean(mCodeTempList);
                if (mean < mTempMinThreshold) {
                    isCode = false;
                    tvTip.setText("测温失败，请重试");
                    KDXFSpeechManager.instance().playNormal("测温失败，请重试");
                    btnNoIdCard.setEnabled(true);
                    getTempHandler.removeMessages(2);
                    getTempHandler.sendEmptyMessageDelayed(2, 3000);
                    return;
                }

                tvTip.setText("");
                boolean isWarning = mean >= mTempWarningThreshold;

                setUITips2(!isWarning, !isWarning, mean);
                btnNoIdCard.setEnabled(true);

                Bitmap currCameraFrame = certificatesView.getCurrCameraFrame();
                SignManager.instance().uploadNoIdCardResult(isWarning ? 1 : 0, currCameraFrame, mean, mCacheHotImage);

                getTempHandler.removeMessages(2);
                getTempHandler.sendEmptyMessageDelayed(2, 5000);
            } else if (msg.what == 2) {
                clearUITips();
            }
        }
    };

    private float formatF(float fValue) {
        return (float) (Math.round(fValue * 10)) / 10;
    }

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
                }
            });
            return;
        }
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

                tvOriginT.setText("部门:");

                String dept = getUserInfo.dept;
                tvOrigin.setText(TextUtils.isEmpty(dept) ? "" : dept);
                tvName.setText(getUserInfo.name);

                tvTip.setText("正在测温 ");

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


    class Verifier extends Thread {
        private static final String TAG = "Verifier";
        private IdCardMsg idCardMsg;
        private Bitmap idCardBitmap;
        private static final int MAX_FACE_FEATURE_NUM = 5;
        private List<FaceFeature> faceFeatureList = new ArrayList<>();
        private Thread compareThread = null;
        private boolean isComparing = true;
        private Thread collectThread = null;
        private boolean isCollecting = false;
        private long NO_FACE_CLEAR_DELAY = 5000;
        private OnCompareCallback compareCallback;
        private long mCacheTime = 0;
        private int mFaceId = -1;

        /***
         * 输入身份证的时候开启对比线程，并将时间重置为0
         * @param msg
         * @param bitmap
         * @param compareCallback
         */
        public void inputIDcard(IdCardMsg msg, Bitmap bitmap, OnCompareCallback compareCallback) {
            idCardMsg = msg;
            idCardBitmap = bitmap;
            mCacheTime = 0;
            if (compareCallback != null) {
                compareCallback.getIdCardMsg(idCardMsg, idCardBitmap);
            }
            startCompareThread();
        }

        public void startCollectFaceFeature(int faceId) {
            if (mFaceId != faceId) {
                mFaceId = faceId;
                faceFeatureList.clear();
                closeCollectThread();
            }
            if (faceFeatureList.size() > MAX_FACE_FEATURE_NUM) {
                return;
            }
            if (collectThread != null && collectThread.isAlive()) {
                return;
            }
            Log.e(TAG, "startCollectFaceFeature: 开启收集线程");
            isCollecting = true;
            collectThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isCollecting) {
                        if (faceFeatureList.size() <= MAX_FACE_FEATURE_NUM) {
                            if (!certificatesView.hasFace()) {
                                closeCollectThread();
                                break;
                            }
                            Log.e(TAG, "run:当前特征数：" + faceFeatureList.size());
                            FaceFeature faceFeature = certificatesView.getFaceFeature();
                            if (faceFeature != null) {
                                faceFeatureList.add(faceFeature);
                            }
                        } else {
                            closeCollectThread();
                            break;
                        }
                    }
                }
            });
            collectThread.start();
        }

        public void closeCollectThread() {
            if (isCollecting) {
                Log.e(TAG, "closeCollectThread: 结束收集线程");
                isCollecting = false;
                collectThread = null;
            }
        }

        private void reset() {
            Log.e(TAG, "reset: 重置状态");
            closeCollectThread();
            closeCompareThread();
            mCacheTime = 0;
            idCardMsg = null;
            idCardBitmap = null;
            compareCallback = null;
            faceFeatureList.clear();
            if (compareCallback != null) {
                compareCallback.onShutdown();
            }
        }

        public void startCompareThread() {
            Log.e(TAG, "startCompareThread: 欲开启对比线程");
            if (compareThread != null && compareThread.isAlive()) {
                Log.e(TAG, "startCompareThread: 对比线程正在运行");
                return;
            }
            Log.e(TAG, "startCompareThread: 开启对比线程");
            isComparing = true;
            compareThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isComparing) {
                        if (!certificatesView.hasFace()) {
                            if (mCacheTime == 0) {
                                mCacheTime = System.currentTimeMillis();
                            } else if (System.currentTimeMillis() - mCacheTime >= NO_FACE_CLEAR_DELAY) {
                                Log.e(TAG, "run: " + NO_FACE_CLEAR_DELAY + "毫秒内没有人脸进入，清除身份信息");
                                reset();
                                closeCompareThread();
                                break;
                            }
                            continue;
                        }

                        if (faceFeatureList.size() <= MAX_FACE_FEATURE_NUM) {
                            continue;
                        }

                        Log.e(TAG, "run: 开始对比");
                        FaceFeature idCardFeature = certificatesView.inputIdCard(idCardBitmap);

                        float finalSimilar = 0;
                        for (FaceFeature faceFeature : faceFeatureList) {
                            FaceSimilar compare = certificatesView.compare(faceFeature, idCardFeature);
                            float comapareSimilar = compare.getScore();
                            Log.e(TAG, "run: 对比:" + comapareSimilar);
                            if (comapareSimilar > finalSimilar) {
                                finalSimilar = comapareSimilar;
                            }
                        }

                        Log.e(TAG, "run: 结果回调：" + finalSimilar);

                        if (compareCallback != null) {
                            compareCallback.compareFinish(finalSimilar);
                        }

                        reset();
                        break;
                    }
                }
            });
            compareThread.start();
        }

        public void closeCompareThread() {
            if (isComparing) {
                Log.e(TAG, "startCompareThread: 结束对比线程");
                isComparing = false;
                compareThread = null;
            }
        }

    }

    public interface OnCompareCallback {
        void getIdCardMsg(IdCardMsg msg, Bitmap bitmap);

        void onShutdown();

        void compareFinish(float similar);
    }
    /*
     * 由身份证开启验证线程
     * 验证线程中循环检测人脸，如果没有人脸，则几秒后清除身份证缓存和信息
     * 有人脸以后跳过清除判断，并且开启另一个线程判断人脸特征采集情况
     * 人脸特征采集完毕，开始对比，结果返回
     *
     *
     * */

    private OnCompareCallback onCompareCallback = new OnCompareCallback() {
        @Override
        public void getIdCardMsg(IdCardMsg msg, Bitmap bitmap) {
            Log.e(TAG, "getIdCardMsg: 收到卡号：" + msg.name);
        }

        @Override
        public void onShutdown() {
            Log.e(TAG, "onShutdown: 线程被中断，清除身份证信息");
        }

        @Override
        public void compareFinish(float similar) {
            Log.e(TAG, "compareFinish: 对比结果：" + similar);
        }
    };

}
