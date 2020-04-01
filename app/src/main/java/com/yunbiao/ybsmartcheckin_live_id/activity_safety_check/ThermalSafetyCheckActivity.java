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
import android.widget.ImageView;
import android.widget.TextView;

import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.bean.BlackBody;
import com.intelligence.hardware.temperature.bean.FaceIndexInfo;
import com.intelligence.hardware.temperature.callback.HotImageK6080CallBack;
import com.intelligence.hardware.temperature.callback.UsbPermissionCallBack;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseGpioActivity;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.utils.NetWorkChangReceiver;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.yunbiao.ybsmartcheckin_live_id.xmpp.ServiceManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ThermalSafetyCheckActivity extends BaseGpioActivity implements NetWorkChangReceiver.NetWorkChangeListener {

    private ImageView ivHot;
    private TextView tvTemperState;
    private TextView tvTemper;

    private float mWarningTemper = 37.3f;
    private float mNormalTemper = 35.5f;
    private boolean mThermalMirror = false;
    private boolean mLowTempMode = true;

    private int mBlackBodyLeft = 11;
    private int mBlackBodyRight = 16;
    private int mBlackBodyTop = 11;
    private int mBlackBodyBottom = 16;

    private float mCorrectValue = 0.0f;
    private TextView tvSsdSafetyCheck;

    private long mWarningNumber;
    private TextView tvWarningNumber;

    private StringBuffer stringBuffer = new StringBuffer();

    private ArrayList<FaceIndexInfo> faceIndexInfos = new ArrayList<>();
    private TextView tvDeviceNumber;
    private TextView tvNetState;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
    private Rect mTemperRect = new Rect();
    private ServiceManager serviceManager;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_thermal_safety_check;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_thermal_safety_check;
    }

    @Override
    protected void initView() {
        APP.setMainActivity(this);
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        startXmpp();
        ImageView ivLogo = findViewById(R.id.iv_logo_safety_check);
        tvDeviceNumber = findViewById(R.id.tv_device_number_safety_check);
        tvNetState = findViewById(R.id.tv_net_state_safety_check);
        tvWarningNumber = findViewById(R.id.tv_warning_number_safety_check);
        tvSsdSafetyCheck = findViewById(R.id.tv_ssd_safety_check);
        ivHot = findViewById(R.id.iv_hot_image_safety_check);
        tvTemperState = findViewById(R.id.tv_temper_state_safety_check);
        tvTemper = findViewById(R.id.tv_temper_safety_check);

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

        mWarningNumber = SpUtils.getLong(ThermalSafetyCheckConst.Key.WARNING_NUMBER, ThermalSafetyCheckConst.Default.WARNING_NUMBER);

        NetWorkChangReceiver netWorkChangReceiver = new NetWorkChangReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkChangReceiver, filter);

        ivLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ThermalSafetyCheckActivity.this, ThermalSafetyCheckSettingActivity.class));
            }
        });

        KDXFSpeechManager.instance().init(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mWarningTemper = SpUtils.getFloat(ThermalSafetyCheckConst.Key.WARNING_TEMPER, ThermalSafetyCheckConst.Default.WARNING_TEMPER);
        mThermalMirror = SpUtils.getBoolean(ThermalSafetyCheckConst.Key.THERMAL_MIRROR, ThermalSafetyCheckConst.Default.THERMAL_MIRROR);
        mLowTempMode = SpUtils.getBoolean(ThermalSafetyCheckConst.Key.LOW_TEMP, ThermalSafetyCheckConst.Default.LOW_TEMP);
        mNormalTemper = SpUtils.getFloat(ThermalSafetyCheckConst.Key.NORMAL_TEMPER, ThermalSafetyCheckConst.Default.NORMAL_TEMPER);

        int temperAreaSize = SpUtils.getIntOrDef(ThermalSafetyCheckConst.Key.TEMPER_AREA_SIZE,ThermalSafetyCheckConst.Default.TEMPER_AREA_SIZE);
        if(temperAreaSize == ThermalSafetyCheckConst.Size.SMALL){
            mTemperRect.set(TemperRect.getSmall());
        } else if(temperAreaSize == ThermalSafetyCheckConst.Size.MIDDLE){
            mTemperRect.set(TemperRect.getMiddle());
        } else {
            mTemperRect.set(TemperRect.getLarge());
        }

        mBlackBodyLeft = SpUtils.getIntOrDef(ThermalSafetyCheckConst.Key.BLACK_BODY_LEFT, ThermalSafetyCheckConst.Default.BLACK_BODY_LEFT);
        mBlackBodyTop = SpUtils.getIntOrDef(ThermalSafetyCheckConst.Key.BLACK_BODY_TOP, ThermalSafetyCheckConst.Default.BLACK_BODY_TOP);
        mBlackBodyRight = SpUtils.getIntOrDef(ThermalSafetyCheckConst.Key.BLACK_BODY_RIGHT, ThermalSafetyCheckConst.Default.BLACK_BODY_RIGHT);
        mBlackBodyBottom = SpUtils.getIntOrDef(ThermalSafetyCheckConst.Key.BLACK_BODY_BOTTOM, ThermalSafetyCheckConst.Default.BLACK_BODY_BOTTOM);

        startHotImage();
    }

    private void startHotImage() {
        int usbPermission = TemperatureModule.getIns().initUsbDevice(this, new UsbPermissionCallBack() {
            @Override
            public void usbPermissionNotice(boolean b) {
                if (!b) {
                    UIUtils.showShort(ThermalSafetyCheckActivity.this, getResources().getString(R.string.main_permission_failed_multi_thermal));
                    return;
                }

                //usb设备初始化成功
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //开启热成像6080模块
                        //isMirror:热成像画面是否左右镜像, isCold:是否为低温补偿模式, hotImageK6080CallBack:数据回调
                        TemperatureModule.getIns().startHotImageK6080(mThermalMirror, mLowTempMode, hotImageK6080CallBack);
                        BlackBody blackBody = new BlackBody(mBlackBodyLeft, mBlackBodyRight, mBlackBodyTop, mBlackBodyBottom);
                        blackBody.setFrameColor(Color.WHITE);
                        blackBody.setTempPreValue(345);
                        TemperatureModule.getIns().setmCorrectionValue(mCorrectValue);
                        TemperatureModule.getIns().startK6080BlackBodyMode(blackBody);
                    }
                }, 1000);
            }
        });

        if (usbPermission == 0) {
            UIUtils.showShort(ThermalSafetyCheckActivity.this, getResources().getString(R.string.main_not_found_usb_multi_thermal));
        } else if (usbPermission == 1) {
            //usb设备初始化成功
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //开启热成像6080模块
                    //isMirror:热成像画面是否左右镜像, isCold:是否为低温补偿模式, hotImageK6080CallBack:数据回调
                    TemperatureModule.getIns().startHotImageK6080(mThermalMirror, mLowTempMode, hotImageK6080CallBack);
                    BlackBody blackBody = new BlackBody(mBlackBodyLeft, mBlackBodyRight, mBlackBodyTop, mBlackBodyBottom);
                    blackBody.setFrameColor(Color.WHITE);
                    blackBody.setTempPreValue(345);
                    TemperatureModule.getIns().setmCorrectionValue(mCorrectValue);
                    TemperatureModule.getIns().startK6080BlackBodyMode(blackBody);
                }
            }, 1000);
        }
    }

    private HotImageK6080CallBack hotImageK6080CallBack = new HotImageK6080CallBack() {
        @Override
        public void newestHotImageData(Bitmap bitmap, float v, float v1, float v2) {
            setFaceIndex();
        }

        @Override
        public void newestHotImageData(Bitmap bitmap, ArrayList<FaceIndexInfo> arrayList) {
            if (arrayList == null || arrayList.size() <= 0) {
                sendUpdateHotImageMessage(bitmap, 0.0f, 0.0f);
                return;
            }

            FaceIndexInfo faceIndexInfo = arrayList.get(0);
            float originalTempF = faceIndexInfo.getOriginalTempF();
            float afterTreatmentF = faceIndexInfo.getAfterTreatmentF();
            sendUpdateHotImageMessage(bitmap, originalTempF, afterTreatmentF);
        }
    };

    private void sendUpdateHotImageMessage(Bitmap bitmap, float originT, float afterT) {
        Message message = Message.obtain();
        message.what = 0;
        message.obj = bitmap;
        Bundle bundle = new Bundle();
        bundle.putFloat("originT", originT);
        bundle.putFloat("afterT", afterT);
        message.setData(bundle);
        uiHandler.sendMessage(message);
    }

    private int mCurrColor = 0;//0白色,1绿色,2红色

    private Handler uiHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    ivHot.setImageBitmap(bitmap);

                    Bundle data = msg.getData();
                    float originT = data.getFloat("originT", 0.0f);
                    float afterT = data.getFloat("afterT", 0.0f);
                    if (originT < mNormalTemper) {
                        afterT = 0.0f;
                    }
                    tvTemper.setText(afterT + "");
                    if (afterT == 0.0f && mCurrColor != 0) {
                        mCurrColor = 0;
                        tvTemper.setTextColor(Color.WHITE);
                        tvSsdSafetyCheck.setTextColor(Color.WHITE);
                        tvTemperState.setText("体温正常");
                        tvTemperState.setBackgroundResource(R.mipmap.bg_verify_pass);
                        KDXFSpeechManager.instance().stopWarningRing();
                        ledInit();
                    } else if (originT >= mNormalTemper && afterT < mWarningTemper && mCurrColor != 1) {
                        mCurrColor = 1;
                        tvTemper.setTextColor(Color.GREEN);
                        tvSsdSafetyCheck.setTextColor(Color.GREEN);
                        tvTemperState.setText("体温正常");
                        tvTemperState.setBackgroundResource(R.mipmap.bg_verify_pass);
                        KDXFSpeechManager.instance().stopWarningRing();
                        ledGreen();
                    } else if (afterT >= mWarningTemper && mCurrColor != 2) {
                        mCurrColor = 2;
                        tvTemper.setTextColor(Color.RED);
                        tvSsdSafetyCheck.setTextColor(Color.RED);
                        tvTemperState.setText("体温异常");
                        tvTemperState.setBackgroundResource(R.mipmap.bg_verify_nopass);

                        KDXFSpeechManager.instance().playWaningRing();
                        ledRed();

                        stringBuffer.setLength(0);
                        mWarningNumber += 1;
                        if (mWarningNumber < 10) {
                            stringBuffer.append("000").append(mWarningNumber);
                        } else if (mWarningNumber < 100) {
                            stringBuffer.append("00").append(mWarningNumber);
                        } else if (mWarningNumber < 1000) {
                            stringBuffer.append("0").append(mWarningNumber);
                        } else {
                            stringBuffer.append(mWarningNumber);
                        }
                        tvWarningNumber.setText(stringBuffer.toString());
                    }

                    break;
            }
            return false;
        }
    });

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
        TemperatureModule.getIns().setFaceIndexInfoList(faceIndexInfos, true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpUtils.saveLong(ThermalSafetyCheckConst.Key.WARNING_NUMBER, mWarningNumber);
        TemperatureModule.getIns().closeHotImageK6080();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destoryXmpp();
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
        tvDeviceNumber.setText("设备编号：" + str);
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
}
