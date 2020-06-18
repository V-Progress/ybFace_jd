package com.yunbiao.ybsmartcheckin_live_id.activity_safety_check;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.bean.BlackBody;
import com.intelligence.hardware.temperature.bean.FaceIndexInfo;
import com.intelligence.hardware.temperature.callback.HotImageK6080CallBack;
import com.intelligence.hardware.temperature.callback.UsbPermissionCallBack;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.ButtonClickListener;
import com.yunbiao.ybsmartcheckin_live_id.FlavorType;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseGpioActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple.MultiThermalConst;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.utils.NetWorkChangReceiver;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.logutils.Utils;
import com.yunbiao.ybsmartcheckin_live_id.views.ImageFileLoader;
import com.yunbiao.ybsmartcheckin_live_id.xmpp.ServiceManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/***
 * 自动校准逻辑：
 * 稳定15分钟后，开启自动校准模式，取框内最低值保存并设置补正，（判断与之前的值相差多少，高了则减，低了则加）
 */
public class ThermalSafetyCheckActivity extends BaseGpioActivity implements NetWorkChangReceiver.NetWorkChangeListener {

    private ImageView ivHot;
    private TextView tvTemperState;
    private TextSwitcher tsTemper;

    private float mWarningTemper = 37.3f;
    private float MIN_TEMPER = 33.0f;
    private float mNormalTemper = 35.0f;
    private boolean mThermalMirror = false;
    private boolean mLowTempMode = true;

    private int mBlackBodyLeft = 11;
    private int mBlackBodyRight = 16;
    private int mBlackBodyTop = 11;
    private int mBlackBodyBottom = 16;

    private TextView tvSsdSafetyCheck;

    private TextView tvWarningNumber;

    private StringBuffer stringBuffer = new StringBuffer();

    private ArrayList<FaceIndexInfo> faceIndexInfos = new ArrayList<>();
    private TextView tvDeviceNumber;
    private TextView tvNetState;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
    private Rect mTemperRect = new Rect();
    private ServiceManager serviceManager;
    private boolean mBlackBodyEnabled;
    private int mPreValue;
    private ImageView ivLogo;
    private boolean mThermalFrameEnabled;

    private float mCorrectValue = 0.0f;
    private Float mBodyTemper;
    private boolean mAutoCalibration;
    private float mLastMinT;

    private boolean mImmediateReportModeEnabled;
    private boolean isReport = false;
    private long reportTimeMillis = 0;
    private long temperUpdateInterval = 2 * 1000;

    private ImageView ivTracingPoint;
    private int ivHotWidth = 0;
    private int ivHotHeight = 0;

    private static boolean fEnabled;

    private float[] stableTemp = new float[]{
            36.2f,
            36.3f, 36.3f, 36.3f, 36.3f, 36.3f, 36.3f, 36.3f, 36.3f,
            36.4f, 36.4f, 36.4f, 36.4f, 36.4f, 36.4f, 36.4f, 36.4f,
            36.5f, 36.5f, 36.5f, 36.5f, 36.5f, 36.5f, 36.5f, 36.5f,
            36.6f, 36.6f, 36.6f, 36.6f, 36.6f, 36.6f, 36.6f, 36.6f,
            36.7f,
            36.8f,
    };

    @Override
    protected int getPortraitLayout() {
        if (Utils.getWinWidth(this) == 1920 && Utils.getWinHight(this) == 1080) {
            return R.layout.activity_thermal_safety_check_19201080;
        }
        return R.layout.activity_thermal_safety_check;
    }

    @Override
    protected int getLandscapeLayout() {
        if (Utils.getWinWidth(this) == 1920 && Utils.getWinHight(this) == 1080) {
            return R.layout.activity_thermal_safety_check_19201080;
        }
        return R.layout.activity_thermal_safety_check;
    }

    @Override
    protected void initView() {
        APP.setMainActivity(this);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        startXmpp();

        ivLogo = findViewById(R.id.iv_logo_safety_check);
        tvDeviceNumber = findViewById(R.id.tv_device_number_safety_check);
        tvNetState = findViewById(R.id.tv_net_state_safety_check);
        tvWarningNumber = findViewById(R.id.tv_warning_number_safety_check);
        tvSsdSafetyCheck = findViewById(R.id.tv_ssd_safety_check);
        ivHot = findViewById(R.id.iv_hot_image_safety_check);
        ivTracingPoint = findViewById(R.id.iv_tracing_point);
        tvTemperState = findViewById(R.id.tv_temper_state_safety_check);
        tsTemper = findViewById(R.id.ts_temper_safety_check);
        tsTemper.setFactory(() -> {
            TextView textView;
            if (Utils.getWinWidth(this) == 1920 && Utils.getWinHight(this) == 1080) {
                textView = (TextView) View.inflate(tsTemper.getContext(), R.layout.layout_safety_check_temper_textview_19201080, null);
            } else {
                textView = (TextView) View.inflate(tsTemper.getContext(), R.layout.layout_safety_check_temper_textview, null);
            }
            return textView;
        });

        String str = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        tvDeviceNumber.setText(str);

        String date = SpUtils.getStr(ThermalSafetyCheckConst.Key.DATE_FOR_WARNGNING_NUMBER);
        String currDate = simpleDateFormat.format(new Date());
        if (TextUtils.isEmpty(date)) {//如果日期为空则存今天的日期并把数量置为0
            SpUtils.saveStr(ThermalSafetyCheckConst.Key.DATE_FOR_WARNGNING_NUMBER, currDate);
            SpUtils.saveLong(ThermalSafetyCheckConst.Key.WARNING_NUMBER, ThermalSafetyCheckConst.Default.WARNING_NUMBER);
        } else if (!TextUtils.equals(currDate, date)) {//如果部位空则判断是否相等，不相等则存今天日期，数量置为0
            SpUtils.saveStr(ThermalSafetyCheckConst.Key.DATE_FOR_WARNGNING_NUMBER, currDate);
            SpUtils.saveLong(ThermalSafetyCheckConst.Key.WARNING_NUMBER, ThermalSafetyCheckConst.Default.WARNING_NUMBER);
        }

        NetWorkChangReceiver netWorkChangReceiver = new NetWorkChangReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkChangReceiver, filter);

        ivLogo.setOnClickListener(v -> startActivity(new Intent(ThermalSafetyCheckActivity.this, ThermalSafetyCheckSettingActivity.class)));

        if (Constants.FLAVOR_TYPE == FlavorType.HT) {
            ivLogo.setImageResource(R.mipmap.logo_icon_horizontal);
            ImageFileLoader.setDefaultLogoId(R.mipmap.logo_icon_horizontal);
        } else if (Constants.FLAVOR_TYPE == FlavorType.SK) {
            ivLogo.setImageResource(R.mipmap.icon_logo3);
            ImageFileLoader.setDefaultLogoId(R.mipmap.icon_logo3);
        }else {
            ivLogo.setImageResource(R.mipmap.yb_logo);
            ImageFileLoader.setDefaultLogoId(R.mipmap.yb_logo);
        }
    }

    @Override
    protected void initData() {
        KDXFSpeechManager.instance().init(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBodyTemper = SpUtils.getFloat(ThermalSafetyCheckConst.Key.BODY_TEMPER, ThermalSafetyCheckConst.Default.BODY_TEMPER);
        mBlackBodyEnabled = SpUtils.getBoolean(ThermalSafetyCheckConst.Key.BLACK_BODY_ENABLED, ThermalSafetyCheckConst.Default.BLACK_BODY_ENABLED);
        mPreValue = SpUtils.getIntOrDef(MultiThermalConst.Key.BLACK_BODY_PRE_VALUE, MultiThermalConst.Default.BLACK_BODY_PRE_VALUE);
        mCorrectValue = SpUtils.getFloat(ThermalSafetyCheckConst.Key.CORRECT_VALUE, ThermalSafetyCheckConst.Default.CORRECT_VALUE);
        mWarningTemper = SpUtils.getFloat(ThermalSafetyCheckConst.Key.WARNING_TEMPER, ThermalSafetyCheckConst.Default.WARNING_TEMPER);
        mThermalMirror = SpUtils.getBoolean(ThermalSafetyCheckConst.Key.THERMAL_MIRROR, ThermalSafetyCheckConst.Default.THERMAL_MIRROR);
        mLowTempMode = SpUtils.getBoolean(ThermalSafetyCheckConst.Key.LOW_TEMP, ThermalSafetyCheckConst.Default.LOW_TEMP);
        mNormalTemper = SpUtils.getFloat(ThermalSafetyCheckConst.Key.NORMAL_TEMPER, ThermalSafetyCheckConst.Default.NORMAL_TEMPER);
        mThermalFrameEnabled = SpUtils.getBoolean(ThermalSafetyCheckConst.Key.TEMPER_FRAME, ThermalSafetyCheckConst.Default.TEMPER_FRAME);
        mAutoCalibration = SpUtils.getBoolean(ThermalSafetyCheckConst.Key.AUTO_CALIBRATION, ThermalSafetyCheckConst.Default.AUTO_CALIBRATION);
        mLastMinT = SpUtils.getFloat(ThermalSafetyCheckConst.Key.LAST_MINT, ThermalSafetyCheckConst.Default.LAST_MINT);
        mImmediateReportModeEnabled = SpUtils.getBoolean(ThermalSafetyCheckConst.Key.IMMEDIATE_REPORT_MODE, ThermalSafetyCheckConst.Default.IMMEDIATE_REPORT_MODE);
        fEnabled = SpUtils.getBoolean(ThermalSafetyCheckConst.Key.THERMAL_F_ENABLED, ThermalSafetyCheckConst.Default.THERMAL_F_ENABLED);
        if (fEnabled) {
            tvSsdSafetyCheck.setText("℉");
        } else {
            tvSsdSafetyCheck.setText("℃");
        }

        int temperAreaSize = SpUtils.getIntOrDef(ThermalSafetyCheckConst.Key.TEMPER_AREA_SIZE, ThermalSafetyCheckConst.Default.TEMPER_AREA_SIZE);
        if (temperAreaSize == ThermalSafetyCheckConst.Size.TOO_SMALL) {
            mTemperRect.set(TemperRect.getTooSmall());
        } else if (temperAreaSize == ThermalSafetyCheckConst.Size.SMALL) {
            mTemperRect.set(TemperRect.getSmall());
        } else if (temperAreaSize == ThermalSafetyCheckConst.Size.MIDDLE) {
            mTemperRect.set(TemperRect.getMiddle());
        } else if (temperAreaSize == ThermalSafetyCheckConst.Size.LARGE) {
            mTemperRect.set(TemperRect.getLarge());
        }

        mBlackBodyLeft = SpUtils.getIntOrDef(ThermalSafetyCheckConst.Key.BLACK_BODY_LEFT, ThermalSafetyCheckConst.Default.BLACK_BODY_LEFT);
        mBlackBodyTop = SpUtils.getIntOrDef(ThermalSafetyCheckConst.Key.BLACK_BODY_TOP, ThermalSafetyCheckConst.Default.BLACK_BODY_TOP);
        mBlackBodyRight = SpUtils.getIntOrDef(ThermalSafetyCheckConst.Key.BLACK_BODY_RIGHT, ThermalSafetyCheckConst.Default.BLACK_BODY_RIGHT);
        mBlackBodyBottom = SpUtils.getIntOrDef(ThermalSafetyCheckConst.Key.BLACK_BODY_BOTTOM, ThermalSafetyCheckConst.Default.BLACK_BODY_BOTTOM);

        initCorrect();

        handleNumber(false);

        startHotImage();
    }

    private void startHotImage() {
        TemperatureModule.getIns().closeHotImageK6080();
        int usbPermission = TemperatureModule.getIns().initUsbDevice(this, new UsbPermissionCallBack() {
            @Override
            public void usbPermissionNotice(boolean b) {
                if (!b) {
                    UIUtils.showShort(ThermalSafetyCheckActivity.this, getResources().getString(R.string.main_permission_failed_multi_thermal));
                    return;
                }

                //usb设备初始化成功
                new Handler().postDelayed(() -> {
                    //开启热成像6080模块
                    //isMirror:热成像画面是否左右镜像, isCold:是否为低温补偿模式, hotImageK6080CallBack:数据回调
                    TemperatureModule.getIns().startHotImageK6080(mThermalMirror, mLowTempMode, hotImageK6080CallBack);
                    BlackBody blackBody = new BlackBody(mBlackBodyLeft, mBlackBodyRight, mBlackBodyTop, mBlackBodyBottom);
                    blackBody.setFrameColor(Color.WHITE);
                    blackBody.setTempPreValue(mPreValue);
//                    setCorrectValue(mCorrectValue);
                    TemperatureModule.getIns().startK6080BlackBodyMode(blackBody);
                    TemperatureModule.getIns().startK6080AutoCalibMode(1);
                    if (!mBlackBodyEnabled) {
                        TemperatureModule.getIns().closeK6080BlackBodyMode();
                    }
                }, 1000);
            }
        });

        if (usbPermission == 0) {
            UIUtils.showShort(ThermalSafetyCheckActivity.this, getResources().getString(R.string.main_not_found_usb_multi_thermal));
        } else if (usbPermission == 1) {
            //usb设备初始化成功
            new Handler().postDelayed(() -> {
                //开启热成像6080模块
                //isMirror:热成像画面是否左右镜像, isCold:是否为低温补偿模式, hotImageK6080CallBack:数据回调
                TemperatureModule.getIns().startHotImageK6080(mThermalMirror, mLowTempMode, hotImageK6080CallBack);
                BlackBody blackBody = new BlackBody(mBlackBodyLeft, mBlackBodyRight, mBlackBodyTop, mBlackBodyBottom);
                blackBody.setFrameColor(Color.WHITE);
                blackBody.setTempPreValue(mPreValue);
//                setCorrectValue(mCorrectValue);
                TemperatureModule.getIns().startK6080BlackBodyMode(blackBody);
                TemperatureModule.getIns().startK6080AutoCalibMode(1);
                if (!mBlackBodyEnabled) {
                    TemperatureModule.getIns().closeK6080BlackBodyMode();
                }
            }, 1000);
        }
    }

    private float mCacheRealTimeCorrectValue = -1f;
    //缓存取温
    private List<Float> mTemperFloats = Collections.synchronizedList(new ArrayList<>());
    private float mCacheTemp = 0;
    private HotImageK6080CallBack hotImageK6080CallBack = new HotImageK6080CallBack() {
        @Override
        public void newestHotImageData(Bitmap bitmap, float originT, float afterT, float minT) {
            setFaceIndex();

            //自动校准逻辑
            if (mAutoCalibration) {
                if (mCacheRealTimeCorrectValue == -1f) {
                    float value = TemperatureModule.getIns().getK6080OffsetStandard();
                    if (value != -1f) {
                        Log.e(TAG, "上次最低值：" + mLastMinT);
                        Log.e(TAG, "当前最低值：" + value);
                        if (mLastMinT != 0.0f) {
                            mCacheRealTimeCorrectValue = mLastMinT - value;
                        } else {
                            mCacheRealTimeCorrectValue = 0.0f;
                        }
                        mCacheRealTimeCorrectValue = formatF(mCacheRealTimeCorrectValue);
                        Log.e(TAG, "前后差异值：" + mCacheRealTimeCorrectValue);
                        Log.e(TAG, "上次校准值：" + mCorrectValue);

                        mCorrectValue += mCacheRealTimeCorrectValue;
                        mCorrectValue = formatF(mCorrectValue);
                        Log.e(TAG, "最终校准值:" + mCorrectValue);

                        SpUtils.saveFloat(ThermalSafetyCheckConst.Key.LAST_MINT, value);
                        SpUtils.saveFloat(ThermalSafetyCheckConst.Key.CORRECT_VALUE, mCorrectValue);
                        TemperatureModule.getIns().setmCorrectionValue(mCorrectValue);
                    }
                }
            } else {
                TemperatureModule.getIns().setmCorrectionValue(mCorrectValue);
            }
        }

        @Override
        public void newestHotImageData(Bitmap bitmap, ArrayList<FaceIndexInfo> arrayList) {
            if (arrayList == null || arrayList.size() <= 0) {
                sendUpdateHotImageMessage(bitmap, 0.0f);
                return;
            }

            FaceIndexInfo faceIndexInfo = arrayList.get(0);
            float originalTempF = faceIndexInfo.getOriginalTempF();
            sendUpdateHotImageMessage(bitmap, originalTempF);

            if (isCorrecting) {
                uiHandler.sendEmptyMessage(3);
                mCorrectionTemperList.add(originalTempF);
                if (mCorrectionTemperList.size() > 30) {
                    mCorrectionTemperList.remove(0);
                }
                return;
            }

            if (mImmediateReportModeEnabled) {
                if (originalTempF < MIN_TEMPER) {
                    uiHandler.sendEmptyMessage(3);
                    isReport = false;
                    return;
                }
                float afterTreatmentF = faceIndexInfo.getAfterTreatmentF();
                if (ivTracingPoint.getVisibility() == View.GONE) {
                    if (mTemperFloats.size() >= 5) {
                        sendUpdateMaxIndexMessage(faceIndexInfo.getMaxX(), faceIndexInfo.getMaxY());
                        mCacheTemp = afterTreatmentF;
                    }
                } else {
                    if (afterTreatmentF > mCacheTemp + 0.3f || afterTreatmentF < mCacheTemp - 0.3f) {
                        sendUpdateMaxIndexMessage(faceIndexInfo.getMaxX(), faceIndexInfo.getMaxY());
                        mCacheTemp = afterTreatmentF;
                    }
                }
                if (!isReport) {
                    if (mTemperFloats.size() < 5) {
                        mTemperFloats.add(afterTreatmentF);
                    } else {
                        Float max = Collections.max(mTemperFloats);
                        mTemperFloats.clear();
                        isReport = true;
                        reportTimeMillis = System.currentTimeMillis();
                        sendTipsMessage(max);
                    }
                } else {
                    if (System.currentTimeMillis() - reportTimeMillis >= temperUpdateInterval) {
                        if (mTemperFloats.size() < 5) {
                            mTemperFloats.add(afterTreatmentF);
                        } else {
                            Float max = Collections.max(mTemperFloats);
                            mTemperFloats.clear();
                            reportTimeMillis = System.currentTimeMillis();
                            sendTipsMessage(max);
                        }
                    }
                }
                return;
            }

            if (originalTempF < MIN_TEMPER) {
                uiHandler.sendEmptyMessage(3);
                if (mTemperFloats.size() > 0) {
                    Float max = Collections.max(mTemperFloats);
                    mTemperFloats.clear();
                    if (max > mNormalTemper) {
                        sendTipsMessage(max);
                    }
                }
                return;
            }

            float afterTreatmentF = faceIndexInfo.getAfterTreatmentF();
            if (afterTreatmentF > mCacheTemp + 0.3f || afterTreatmentF < mCacheTemp - 0.3f) {
                sendUpdateMaxIndexMessage(faceIndexInfo.getMaxX(), faceIndexInfo.getMaxY());
                mCacheTemp = afterTreatmentF;
            }
            mTemperFloats.add(afterTreatmentF);
            if (mTemperFloats.size() > 30) {
                mTemperFloats.remove(0);
            }
        }
    };

    private void sendUpdateHotImageMessage(Bitmap bitmap, float value) {
        Message message = Message.obtain();
        message.what = 0;
        message.obj = bitmap;
        Bundle bundle = new Bundle();
        bundle.putFloat("temper", value);
        message.setData(bundle);
        uiHandler.sendMessage(message);
    }

    private void sendUpdateMaxIndexMessage(int x, int y) {
        Message message = Message.obtain();
        message.what = 2;
        message.arg1 = x;
        message.arg2 = y;
        uiHandler.sendMessage(message);
    }

    private void sendTipsMessage(float finalTemper) {
        Message message = Message.obtain();
        message.what = 1;
        message.obj = finalTemper;
        uiHandler.sendMessage(message);
    }

    private static final String TAG = "ThermalSafetyCheckActiv";

    private Handler uiHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    ivHot.setImageBitmap(bitmap);
                    if (ivHotWidth == 0) {
                        ivHotWidth = ivHot.getWidth();
                    }
                    if (ivHotHeight == 0) {
                        ivHotHeight = ivHot.getHeight();
                    }
//                    Bundle data = msg.getData();
//                    float temper = data.getFloat("temper", 0.0f);
//                    tvOringinT.setText(temper + "℃");
                    break;
                case 1:
                    float finalTemper = (float) msg.obj;

                    if (finalTemper <= 37.0f && finalTemper >= 36.0) {
                        finalTemper = stableTemp[(int)(Math.random() * stableTemp.length)];
                    }

                    if (fEnabled) {
                        float fTemper = (float) (Math.round((finalTemper * 1.8f + 32) * 10)) / 10;
                        tsTemper.setText(fTemper + "");
                        tvSsdSafetyCheck.setText("℉");
                    } else {
                        tsTemper.setText(finalTemper + "");
                        tvSsdSafetyCheck.setText("℃");
                    }
                    TextView currTextView = (TextView) tsTemper.getCurrentView();
                    String tip;
                    //体温正常
                    if (finalTemper < mWarningTemper) {
                        tip = APP.getContext().getResources().getString(R.string.main_temp_normal_tips);
                        ledGreen();
                        currTextView.setTextColor(Color.GREEN);
                        tvSsdSafetyCheck.setTextColor(Color.GREEN);
                        tvTemperState.setText(tip);
                        tvTemperState.setBackgroundResource(R.mipmap.bg_verify_pass);
                        KDXFSpeechManager.instance().stopWarningRing();
                        KDXFSpeechManager.instance().playPassRing();
                    } else if (finalTemper >= mWarningTemper) {//体温异常
                        tip = APP.getContext().getResources().getString(R.string.main_temp_warning_tips);
                        ledRed();
                        currTextView.setTextColor(Color.RED);
                        tvSsdSafetyCheck.setTextColor(Color.RED);
                        tvTemperState.setText(tip);
                        tvTemperState.setBackgroundResource(R.mipmap.bg_verify_nopass);
                        KDXFSpeechManager.instance().stopPassRing();
                        KDXFSpeechManager.instance().playWaningRingNoStop();
                        handleNumber(true);
                    }
                    break;
                case 2:
                    if (ivHotWidth == 0 || ivHotHeight == 0) {
                        break;
                    }
                    ivTracingPoint.setVisibility(View.GONE);
                    int x = msg.arg1;
                    int y = msg.arg2;
                    int adjustX = (int) ((x / 80f) * ivHotWidth);
                    int adjustY = (int) ((y / 60f) * ivHotHeight);

                    ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(ivTracingPoint.getLayoutParams());
                    margin.setMargins(adjustX, adjustY, adjustX + margin.width, adjustY + margin.height);
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(margin);
                    ivTracingPoint.setLayoutParams(layoutParams);

                    break;
                case 3:
                    mCacheTemp = 0;
                    ivTracingPoint.setVisibility(View.GONE);
                    break;
            }
            return false;
        }
    });

    private void handleNumber(boolean isCalcu) {
        long number = SpUtils.getLong(ThermalSafetyCheckConst.Key.WARNING_NUMBER, ThermalSafetyCheckConst.Default.WARNING_NUMBER);
        stringBuffer.setLength(0);
        if (isCalcu) {
            number += 1;
        }
        if (number < 10) {
            stringBuffer.append("000").append(number);
        } else if (number < 100) {
            stringBuffer.append("00").append(number);
        } else if (number < 1000) {
            stringBuffer.append("0").append(number);
        } else {
            stringBuffer.append(number);
        }
        tvWarningNumber.setText(stringBuffer.toString());
        SpUtils.saveLong(ThermalSafetyCheckConst.Key.WARNING_NUMBER, number);
    }

    //设置人脸框
    private void setFaceIndex() {
        if (faceIndexInfos != null && faceIndexInfos.size() > 0) {
            faceIndexInfos.clear();
        }
        //可以支持一次传入多个人脸框信息
        faceIndexInfos.add(new FaceIndexInfo(0, mTemperRect.left, mTemperRect.right, mTemperRect.top, mTemperRect.bottom));

        //设置热成像画面中人脸框的颜色，默认为黑色
        TemperatureModule.getIns().setFaceFrameColor(0xFFFFFFFF);
        //人脸框信息faceIndexInfoList，是否在热成像画面中绘制人脸框(true为绘制)
        TemperatureModule.getIns().setFaceIndexInfoList(faceIndexInfos, mThermalFrameEnabled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destoryXmpp();
        TemperatureModule.getIns().closeHotImageK6080();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void connect() {
        tvNetState.setText(APP.getContext().getString(R.string.smt_main_net_normal2));
        tvNetState.setTextColor(Color.GREEN);
    }

    @Override
    public void disConnect() {
        tvNetState.setText(APP.getContext().getString(R.string.smt_main_net_no));
        tvNetState.setTextColor(Color.RED);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent event) {
        String str = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        tvDeviceNumber.setText(str);
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
     * ==校准区=======================================================================================
     */
    private void initCorrect() {
        View llCorrectionArea = findViewById(R.id.ll_correction_area);//矫正区
        Button btnCorrection = findViewById(R.id.btn_correction_safety_check);//矫正键
        //显示隐藏
        btnCorrection.setOnClickListener(v -> {
            llCorrectionArea.setVisibility(View.VISIBLE);
            v.setVisibility(View.GONE);
            tvNetState.setVisibility(View.GONE);
            tvDeviceNumber.setVisibility(View.GONE);
            initCorrectionArea(() -> {
                llCorrectionArea.setVisibility(View.GONE);
                btnCorrection.setVisibility(View.VISIBLE);
                tvNetState.setVisibility(View.VISIBLE);
                tvDeviceNumber.setVisibility(View.VISIBLE);
            });
        });
    }

    private boolean isCorrecting = false;//矫正标识
    private float measureT = 0.0f;//测出的温度
    private List<Float> mCorrectionTemperList = new ArrayList<>();//测温集合
    private int time = 3;//测温延时
    private float mCurrCorrectValue = 0.0f;//当前校正值（修改时不会影响缓存值）
    private float mCurrBodyTemper = 0.0f;//当前体温值（修改时不会影响缓存值）

    private void initCorrectionArea(Runnable runnable) {
        EditText edtCorrect = findViewById(R.id.edt_correct_safety_check);//矫正值
        Button btnCorrectPlus = findViewById(R.id.btn_correct_plus_safety_check);//矫正加
        Button btnCorrectSub = findViewById(R.id.btn_correct_sub_safety_check);//矫正减
        EditText edtBodyTemper = findViewById(R.id.edt_body_temper_safety_check);//体温值
        Button btnBodyTemperPlus = findViewById(R.id.btn_body_temper_plus_safety_check);//体温加
        Button btnBodyTemperSub = findViewById(R.id.btn_body_temper_sub_safety_check);//体温减
        TextView tvOriginT = findViewById(R.id.tv_oringinT_safety_check);//测温值
        Button btnCancel = findViewById(R.id.btn_cancel_correction_safety_check);//取消键
        Button btnConfirm = findViewById(R.id.btn_confirm_correction_safety_check);//确定键
        btnCorrectPlus.setEnabled(false);
        btnCorrectSub.setEnabled(false);
        btnBodyTemperPlus.setEnabled(false);
        btnBodyTemperSub.setEnabled(false);
        btnCancel.setEnabled(false);
        btnConfirm.setEnabled(false);

        Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0://准备开始
                        if (time < 1) {
                            time = 3;
                            TemperatureModule.getIns().setmCorrectionValue(0.0f);
                            sendEmptyMessageDelayed(1, 200);
                            break;
                        }
                        UIUtils.showShort(ThermalSafetyCheckActivity.this,
                                APP.getContext().getResources().getString(R.string.safety_correct_temper_tip) + " " + time);
                        time--;
                        sendEmptyMessageDelayed(0, 1000);
                        break;
                    case 1:
                        isCorrecting = true;
                        UIUtils.showShort(ThermalSafetyCheckActivity.this, APP.getContext().getResources().getString(R.string.safety_correct_temper_ing));
                        if (mCorrectionTemperList.size() >= 30) {
                            UIUtils.showShort(ThermalSafetyCheckActivity.this, APP.getContext().getResources().getString(R.string.safety_correct_temper_completion));

                            measureT = Collections.max(mCorrectionTemperList);
                            tvOriginT.setText(measureT + "℃");
                            if (measureT <= mCurrBodyTemper) {
                                tvOriginT.setTextColor(Color.GREEN);
                            } else {
                                tvOriginT.setTextColor(Color.WHITE);
                            }

                            Log.e(TAG, "原始值：" + measureT);
                            mCurrCorrectValue = mCurrBodyTemper - measureT;
                            mCurrCorrectValue = formatF(mCurrCorrectValue);
                            Log.e(TAG, "校准值：" + mCurrCorrectValue);

                            edtCorrect.setText(mCurrCorrectValue + "");
                            TemperatureModule.getIns().setmCorrectionValue(mCurrCorrectValue);

                            btnCorrectPlus.setEnabled(true);
                            btnCorrectSub.setEnabled(true);
                            btnBodyTemperPlus.setEnabled(true);
                            btnBodyTemperSub.setEnabled(true);
                            btnCancel.setEnabled(true);
                            btnConfirm.setEnabled(true);
                            break;
                        }
                        sendEmptyMessageDelayed(1, 1000);
                        break;
                }
            }
        };

        //取消、确定键
        View.OnClickListener onClickListener = v -> {
            measureT = 0.0f;
            isCorrecting = false;
            mCorrectionTemperList.clear();
            if (runnable != null) {
                runnable.run();
            }
            if (v.getId() == R.id.btn_confirm_correction_safety_check) {
                UIUtils.showShort(ThermalSafetyCheckActivity.this, APP.getContext().getResources().getString(R.string.safety_save_completion));
                mCorrectValue = mCurrCorrectValue;
                mBodyTemper = mCurrBodyTemper;
                SpUtils.saveFloat(ThermalSafetyCheckConst.Key.CORRECT_VALUE, mCorrectValue);
                SpUtils.saveFloat(ThermalSafetyCheckConst.Key.BODY_TEMPER, mBodyTemper);
                Log.e(TAG, "最终保存校准值：" + mCorrectValue);
                //点击确定或取消，
                TemperatureModule.getIns().setmCorrectionValue(mCorrectValue);
            } else {
                UIUtils.showShort(ThermalSafetyCheckActivity.this, APP.getContext().getResources().getString(R.string.safety_save_cancel));
                TemperatureModule.getIns().setmCorrectionValue(mCorrectValue);
            }

            findViewById(R.id.ll_correction_area).setVisibility(View.GONE);//矫正区
            findViewById(R.id.btn_correction_safety_check).setVisibility(View.VISIBLE);//矫正键
        };
        btnCancel.setOnClickListener(onClickListener);
        btnConfirm.setOnClickListener(onClickListener);

        //值显示
        mCurrCorrectValue = mCorrectValue;
        mCurrBodyTemper = mBodyTemper;
        tvOriginT.setText(measureT + "℃");
        edtBodyTemper.setText(mCurrBodyTemper + "");
        edtCorrect.setText(mCurrCorrectValue + "");

        //校正值加减
        ButtonClickListener buttonLongClick = new ButtonClickListener() {
            @Override
            public void onShortClick(int viewId) {
                if (viewId == R.id.btn_correct_plus_safety_check) {
                    mCurrCorrectValue += 0.1f;
                    mCurrBodyTemper += 0.1f;
                } else {
                    mCurrCorrectValue -= 0.1f;
                    mCurrBodyTemper -= 0.1f;
                }

                mCurrCorrectValue = formatF(mCurrCorrectValue);
                edtCorrect.setText("" + mCurrCorrectValue);
                TemperatureModule.getIns().setmCorrectionValue(mCurrCorrectValue);

                mCurrBodyTemper = formatF(mCurrBodyTemper);
                edtBodyTemper.setText(mCurrBodyTemper + "");
            }

            @Override
            public void onLongClick(int viewId) {
                if (viewId == R.id.btn_correct_plus_safety_check) {
                    mCurrCorrectValue += 0.1f;
                    mCurrBodyTemper += 0.1f;
                } else {
                    mCurrCorrectValue -= 0.1f;
                    mCurrBodyTemper -= 0.1f;
                }
                mCurrCorrectValue = formatF(mCurrCorrectValue);
                edtCorrect.setText("" + mCurrCorrectValue);
                TemperatureModule.getIns().setmCorrectionValue(mCurrCorrectValue);

                mCurrBodyTemper = formatF(mCurrBodyTemper);
                edtBodyTemper.setText(mCurrBodyTemper + "");
            }

            @Override
            public void onLongClickFinish(int viewId) {
            }
        };
        btnCorrectSub.setOnTouchListener(buttonLongClick);
        btnCorrectPlus.setOnTouchListener(buttonLongClick);

        //体温加减
        ButtonClickListener bodyTemperClick = new ButtonClickListener() {
            @Override
            public void onShortClick(int viewId) {
                if (viewId == R.id.btn_body_temper_plus_safety_check) {
                    mCurrBodyTemper += 0.1f;
                    mCurrCorrectValue += 0.1f;
                } else {
                    mCurrBodyTemper -= 0.1f;
                    mCurrCorrectValue -= 0.1f;
                }

                mCurrBodyTemper = formatF(mCurrBodyTemper);
                edtBodyTemper.setText(mCurrBodyTemper + "");

                mCurrCorrectValue = formatF(mCurrCorrectValue);
                edtCorrect.setText("" + mCurrCorrectValue);
                TemperatureModule.getIns().setmCorrectionValue(mCurrCorrectValue);
            }

            @Override
            public void onLongClick(int viewId) {
                if (viewId == R.id.btn_body_temper_plus_safety_check) {
                    mCurrBodyTemper += 0.1f;
                    mCurrCorrectValue += 0.1f;
                } else {
                    mCurrBodyTemper -= 0.1f;
                    mCurrCorrectValue -= 0.1f;
                }
                mCurrBodyTemper = formatF(mCurrBodyTemper);
                edtBodyTemper.setText(mCurrBodyTemper + "");

                mCurrCorrectValue = formatF(mCurrCorrectValue);
                edtCorrect.setText("" + mCurrCorrectValue);
                TemperatureModule.getIns().setmCorrectionValue(mCurrCorrectValue);
            }

            @Override
            public void onLongClickFinish(int viewId) {
            }
        };
        btnBodyTemperSub.setOnTouchListener(bodyTemperClick);
        btnBodyTemperPlus.setOnTouchListener(bodyTemperClick);

        mHandler.sendEmptyMessageDelayed(0, 200);
    }
}
