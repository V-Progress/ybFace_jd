package com.yunbiao.ybsmartcheckin_live_id.activity_safety_check_double_light;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arcsoft.face.FaceInfo;
import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.bean.BlackBody;
import com.intelligence.hardware.temperature.bean.FaceIndexInfo;
import com.intelligence.hardware.temperature.callback.HotImageK6080CallBack;
import com.intelligence.hardware.temperature.callback.UsbPermissionCallBack;
import com.yunbiao.faceview.CompareResult;
import com.yunbiao.faceview.FaceManager;
import com.yunbiao.faceview.FacePreviewInfo;
import com.yunbiao.faceview.FaceView;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.ButtonClickListener;
import com.yunbiao.ybsmartcheckin_live_id.FlavorType;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateMediaEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.SystemActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity_safety_check.ThermalSafetyCheckActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.utils.NetWorkChangReceiver;
import com.yunbiao.ybsmartcheckin_live_id.utils.RestartAPPTool;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.yunbiao.ybsmartcheckin_live_id.views.GradientColorTextView;
import com.yunbiao.ybsmartcheckin_live_id.views.ImageFileLoader;
import com.yunbiao.ybsmartcheckin_live_id.xmpp.ServiceManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import butterknife.BindView;

public class SafetyCheckDoubleLightActivity extends BaseSafetyCheckDoubleLightActivity {

    @BindView(R.id.iv_logo_scdl)
    ImageView ivLogo;
    @BindView(R.id.gctv_title_scdl)
    GradientColorTextView tvTitle;
    @BindView(R.id.face_view_scdl)
    FaceView faceView;
    @BindView(R.id.iv_hot_image_scdl)
    ImageView ivHotImage;
    @BindView(R.id.tv_device_no_double_light)
    TextView tvDeviceNo;
    @BindView(R.id.tv_net_state_double_light)
    TextView tvNetState;
    @BindView(R.id.tv_temperature_scdl)
    TextView tvTemper;
    @BindView(R.id.tv_temper_state_scdl)
    TextView tvTemperStatus;
    @BindView(R.id.tv_warning_number_scdl)
    TextView tvWarningNumber;
    @BindView(R.id.rlv_record_scdl)
    RecyclerView rlvRecord;
    @BindView(R.id.ll_calibration_scdl)
    View llCalibration;
    @BindView(R.id.rl_device_info_scdl)
    View rlDeviceInfo;
    @BindView(R.id.tv_originT_scdl)
    TextView tvOriginTScdl;
    @BindView(R.id.btn_correct_sub_scdl)
    Button btnCorrectSubScdl;
    @BindView(R.id.edt_correct_scdl)
    EditText edtCorrectScdl;
    @BindView(R.id.btn_correct_plus_scdl)
    Button btnCorrectPlusScdl;
    @BindView(R.id.btn_body_temper_sub_scdl)
    Button btnBodyTemperSubScdl;
    @BindView(R.id.edt_body_temper_scdl)
    EditText edtBodyTemperScdl;
    @BindView(R.id.btn_body_temper_plus_scdl)
    Button btnBodyTemperPlusScdl;
    @BindView(R.id.btn_cancel_correction_scdl)
    Button btnCancelCorrectionScdl;
    @BindView(R.id.btn_confirm_correction_scdl)
    Button btnConfirmCorrectionScdl;
    @BindView(R.id.tv_bind_code_double_light)
    TextView tvBindCode;

    private boolean mThermalMirror;
    private boolean mLowTemp;
    private boolean mBlackBodyEnabled;
    private int mPreValue;
    private float mCorrectValue;
    private DoubleLightRecordAdapter doubleLightRecordAdapter;
    private float mWarningThreshold;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
    private long mWarningNumber = 0;
    private List<Sign> signList;
    private ServiceManager serviceManager;
    private float mBodyTemper;
    private float mMinThreshold;
    private int mLeft;
    private int mRight;
    private int mTop;
    private int mBottom;
    private boolean mBlackBodyFrame;

    private boolean isFirstCorrected = true;
    private float mLastMinT;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_safety_check_multiple;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_safety_check_multiple;
    }

    @Override
    protected void initView() {
        registerEventBus(this);
        faceView.setCallback(faceCallback);

        //开启多人识别模式
        faceView.enableMutiple(false);
        //设置回调延时
        faceView.setRetryDelayTime(500);
        //关闭多次重试
        faceView.enableMultiRetry(true);
        faceView.setRetryTime(0);

        if (Constants.FLAVOR_TYPE == FlavorType.HT) {
            ivLogo.setImageResource(R.mipmap.logo_icon_horizontal);
            ImageFileLoader.setDefaultLogoId(R.mipmap.logo_icon_horizontal);
        } else if (Constants.FLAVOR_TYPE == FlavorType.SK) {
            ivLogo.setImageResource(R.mipmap.icon_logo3);
            ImageFileLoader.setDefaultLogoId(R.mipmap.icon_logo3);
        } else if (Constants.FLAVOR_TYPE == FlavorType.OSIMLE) {
            ImageFileLoader.setDefaultLogoId(R.mipmap.osimle_logo);
            ivLogo.setImageResource(R.mipmap.osimle_logo);
        } else {
            ivLogo.setImageResource(R.mipmap.logo_hushida);
            ImageFileLoader.setDefaultLogoId(R.mipmap.logo_hushida);
        }

        ivLogo.setOnClickListener(v -> goSetting());
    }

    @Override
    protected void initData() {
        startXmpp();

        KDXFSpeechManager.instance().init(this);

        initRecordData();

        registerNetStateListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        faceView.resume();
        mBodyTemper = SpUtils.getFloat(SafetyCheckDoubleLightConst.Key.BODY_TEMPER, SafetyCheckDoubleLightConst.Default.BODY_TEMPER);
        mMinThreshold = SpUtils.getFloat(SafetyCheckDoubleLightConst.Key.MIN_THRESHOLD, SafetyCheckDoubleLightConst.Default.MIN_THRESHOLD);
        mWarningThreshold = SpUtils.getFloat(SafetyCheckDoubleLightConst.Key.WARNING_THRESHOLD, SafetyCheckDoubleLightConst.Default.WARNING_THRESHOLD);
        mThermalMirror = SpUtils.getBoolean(SafetyCheckDoubleLightConst.Key.THERMAL_MIRROR, SafetyCheckDoubleLightConst.Default.THERMAL_MIRROR);
        mLowTemp = SpUtils.getBoolean(SafetyCheckDoubleLightConst.Key.LOW_TEMP, SafetyCheckDoubleLightConst.Default.LOW_TEMP);
        mPreValue = SpUtils.getIntOrDef(SafetyCheckDoubleLightConst.Key.PRE_VALUE, SafetyCheckDoubleLightConst.Default.PRE_VALUE);
        mCorrectValue = SpUtils.getFloat(SafetyCheckDoubleLightConst.Key.CORRECT_VALUE, SafetyCheckDoubleLightConst.Default.CORRECT_VALUE);
        mLastMinT = SpUtils.getFloat(SafetyCheckDoubleLightConst.Key.LAST_MIN_T, SafetyCheckDoubleLightConst.Default.LAST_MIN_T);

        d("缓存中的校准值：" + mCorrectValue);

        mBlackBodyEnabled = SpUtils.getBoolean(SafetyCheckDoubleLightConst.Key.BLACK_BODY_ENABLED, SafetyCheckDoubleLightConst.Default.BLACK_BODY_ENABLED);
        mBlackBodyFrame = SpUtils.getBoolean(SafetyCheckDoubleLightConst.Key.BLACK_BODY_FRAME, SafetyCheckDoubleLightConst.Default.BLACK_BODY_FRAME);
        mLeft = SpUtils.getIntOrDef(SafetyCheckDoubleLightConst.Key.BLACK_BODY_LEFT, SafetyCheckDoubleLightConst.Default.BLACK_BODY_LEFT);
        mRight = SpUtils.getIntOrDef(SafetyCheckDoubleLightConst.Key.BLACK_BODY_RIGHT, SafetyCheckDoubleLightConst.Default.BLACK_BODY_RIGHT);
        mTop = SpUtils.getIntOrDef(SafetyCheckDoubleLightConst.Key.BLACK_BODY_TOP, SafetyCheckDoubleLightConst.Default.BLACK_BODY_TOP);
        mBottom = SpUtils.getIntOrDef(SafetyCheckDoubleLightConst.Key.BLACK_BODY_BOTTOM, SafetyCheckDoubleLightConst.Default.BLACK_BODY_BOTTOM);

        startHotImage();
    }

    @Override
    protected void onStop() {
        super.onStop();
        faceView.pause();
        TemperatureModule.getIns().closeHotImageK6080();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterEventBus(this);
        faceView.destory();
        destoryXmpp();
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

    private void registerNetStateListener() {
        NetWorkChangReceiver netWorkChangReceiver = new NetWorkChangReceiver(netWorkChangeListener);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkChangReceiver, filter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent event) {
        Company company = SpUtils.getCompany();
        if(!TextUtils.isEmpty(company.getAbbname())){
            tvTitle.setText(company.getAbbname());
        }

        tvDeviceNo.setText(SpUtils.getStr(SpUtils.DEVICE_NUMBER));

        tvBindCode.setText(SpUtils.getStr(SpUtils.BIND_CODE));

        EventBus.getDefault().post(new UpdateMediaEvent());

        ImageFileLoader.i().loadAndSave(this, company.getComlogo(), Constants.DATA_PATH, ivLogo);
    }

    private NetWorkChangReceiver.NetWorkChangeListener netWorkChangeListener = new NetWorkChangReceiver.NetWorkChangeListener() {
        @Override
        public void connect() {
            tvNetState.setText(getResString(R.string.smt_main_net_normal2));
            tvNetState.setTextColor(Color.GREEN);
        }

        @Override
        public void disConnect() {
            tvNetState.setText(getResString(R.string.smt_main_net_no));
            tvNetState.setTextColor(Color.RED);
        }
    };

    private void initRecordData() {
        rlvRecord.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        if (signList == null) {
            signList = new ArrayList<>();
        }
        doubleLightRecordAdapter = new DoubleLightRecordAdapter(signList, this);
        rlvRecord.setAdapter(doubleLightRecordAdapter);

        rlvRecord.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.right = 25;
            }
        });

        mWarningThreshold = SpUtils.getFloat(SafetyCheckDoubleLightConst.Key.WARNING_THRESHOLD, SafetyCheckDoubleLightConst.Default.WARNING_THRESHOLD);
        int comid = SpUtils.getCompany().getComid();
        List<Sign> signs = DaoManager.get().querySignByComIdAndDate(comid, dateFormat.format(new Date()));
        if (signs == null) {
            return;
        }
        for (int i = signs.size() - 1; i >= 0; i--) {
            Sign sign = signs.get(i);
            if (sign.getTemperature() >= mWarningThreshold) {
                if (signList.size() < 30) {
                    signList.add(sign);
                }
                mWarningNumber++;
            }
        }
        doubleLightRecordAdapter.notifyDataSetChanged();
        handleNumber(true);
    }

    private void startHotImage() {
        TemperatureModule.getIns().closeHotImageK6080();

        int usbPermission = TemperatureModule.getIns().initUsbDevice(this, new UsbPermissionCallBack() {
            @Override
            public void usbPermissionNotice(boolean b) {
                if (!b) {
                    UIUtils.showShort(SafetyCheckDoubleLightActivity.this, getResources().getString(R.string.main_permission_failed_multi_thermal));
                    return;
                }

                //usb设备初始化成功
                new Handler().postDelayed(() -> {
                    //开启热成像6080模块
                    //isMirror:热成像画面是否左右镜像, isCold:是否为低温补偿模式, hotImageK6080CallBack:数据回调
                    TemperatureModule.getIns().setmCorrectionValue(mCorrectValue);
                    TemperatureModule.getIns().startHotImageK6080(mThermalMirror, mLowTemp, hotImageK6080CallBack);
                    if (mBlackBodyEnabled) {
                        BlackBody blackBody = new BlackBody(mLeft, mRight, mTop, mBottom);
                        blackBody.setFrameColor(Color.WHITE);
                        blackBody.setTempPreValue(mPreValue);
                        blackBody.setDrawFrame(mBlackBodyFrame);
                        TemperatureModule.getIns().startK6080BlackBodyMode(blackBody);
                    } else {
                        TemperatureModule.getIns().closeK6080BlackBodyMode();
                    }
                    TemperatureModule.getIns().startK6080AutoCalibMode(1);
                }, 1000);
            }
        });

        if (usbPermission == 0) {
            UIUtils.showShort(this, getResources().getString(R.string.main_not_found_usb_multi_thermal));
        } else if (usbPermission == 1) {
            //usb设备初始化成功
            new Handler().postDelayed(() -> {
                //开启热成像6080模块
                //isMirror:热成像画面是否左右镜像, isCold:是否为低温补偿模式, hotImageK6080CallBack:数据回调
                TemperatureModule.getIns().setmCorrectionValue(mCorrectValue);
                TemperatureModule.getIns().startHotImageK6080(mThermalMirror, mLowTemp, hotImageK6080CallBack);
                if (mBlackBodyEnabled) {
                    BlackBody blackBody = new BlackBody(mLeft, mRight, mTop, mBottom);
                    blackBody.setFrameColor(Color.WHITE);
                    blackBody.setTempPreValue(mPreValue);
                    blackBody.setDrawFrame(mBlackBodyFrame);
                    TemperatureModule.getIns().startK6080BlackBodyMode(blackBody);
                } else {
                    TemperatureModule.getIns().closeK6080BlackBodyMode();
                }
                TemperatureModule.getIns().startK6080AutoCalibMode(1);
            }, 1000);
        }
    }

    private boolean mHasFace = false;

    private void showHotBitmap(Bitmap bitmap) {
        mCurrHotBitmap = bitmap;
        ivHotImage.post(() -> ivHotImage.setImageBitmap(bitmap));
    }

    private ArrayList<FaceIndexInfo> faceIndexInfos = new ArrayList<>();

    //设置人脸框
    private void setFaceIndex() {
        if (faceIndexInfos != null && faceIndexInfos.size() > 0) {
            faceIndexInfos.clear();
        }
        //可以支持一次传入多个人脸框信息
        faceIndexInfos.add(new FaceIndexInfo(0, 20, 60, 20, 40));

        //设置热成像画面中人脸框的颜色，默认为黑色
        TemperatureModule.getIns().setFaceFrameColor(0xFFFFFFFF);
        //人脸框信息faceIndexInfoList，是否在热成像画面中绘制人脸框(true为绘制)
        TemperatureModule.getIns().setFaceIndexInfoList(faceIndexInfos, true);
    }

    private Bitmap mCurrHotBitmap;
    private HotImageK6080CallBack hotImageK6080CallBack = new HotImageK6080CallBack() {
        @Override
        public void newestHotImageData(Bitmap bitmap, float v, float v1, float v2) {
            if (isFirstCorrected) {//开机校准
                setFaceIndex();

                float value = TemperatureModule.getIns().getK6080OffsetStandard();
                if (value != -1f) {
                    d("上次最低值：" + mLastMinT);
                    d("当前最低值：" + value);

                    float diffValue = 0.0f;
                    if (mLastMinT != 0.0f) {
                        diffValue = mLastMinT - value;
                    }
                    diffValue = formatF(diffValue);
                    d("前后差异值：" + diffValue);
                    d("上次校准值：" + mCorrectValue);

                    mCorrectValue += diffValue;
                    mCorrectValue = formatF(mCorrectValue);
                    d("最终校准值：" + mCorrectValue);

                    SpUtils.saveFloat(SafetyCheckDoubleLightConst.Key.LAST_MIN_T, value);
                    SpUtils.saveFloat(SafetyCheckDoubleLightConst.Key.CORRECT_VALUE, mCorrectValue);
                    TemperatureModule.getIns().setmCorrectionValue(mCorrectValue);

                    isFirstCorrected = false;
                }
            } else if (isCalibration) {
                setFaceIndex();
            } else {
                if (!mHasFace) showHotBitmap(bitmap);
            }
        }

        @Override
        public void newestHotImageData(Bitmap bitmap, ArrayList<FaceIndexInfo> arrayList) {
            if (isFirstCorrected) {//开机校准
                showHotBitmap(bitmap);
            } else if (isCalibration) {
                showHotBitmap(bitmap);
                if (arrayList == null || arrayList.size() <= 0) {
                    return;
                }
                FaceIndexInfo faceIndexInfo = arrayList.get(0);
                float originalTempF = faceIndexInfo.getOriginalTempF();

                mCalibrationTemperList.add(originalTempF);
                if (mCalibrationTemperList.size() > 30) {
                    mCalibrationTemperList.remove(0);
                }
            } else {
                if (mHasFace) showHotBitmap(bitmap);
                faceView.setTemperList(arrayList);
            }
        }
    };
    private FaceView.FaceCallback faceCallback = new FaceView.FaceCallback() {
        @Override
        public void onReady() {
            SyncManager.instance().requestOnlyCompany();
            FaceManager.getInstance().init(SafetyCheckDoubleLightActivity.this);
        }

        @Override
        public void onFaceDetection(Boolean hasFace, List<FacePreviewInfo> facePreviewInfoList) {
            mHasFace = hasFace;
            if (!hasFace)
                return;

            if (isFirstCorrected)
                return;

            if (isCalibration)
                return;

            setFaceIndex(facePreviewInfoList);
        }

        @Override
        public boolean onFaceDetection(boolean hasFace, FacePreviewInfo facePreviewInfo) {
            if (isFirstCorrected) {
                return false;
            } else if (isCalibration) {
                return false;
            } else if (hasFace) {
                List<FacePreviewInfo> facePreviewInfos = new ArrayList<>();
                facePreviewInfos.add(facePreviewInfo);
                setFaceIndex(facePreviewInfos);
            }
            return true;
        }

        @Override
        public void onFaceVerify(CompareResult faceAuth) {
            float temper = faceView.getTemperByTrackId(faceAuth.getTrackId());
            if (temper <= 0.0f) {
                return;
            }

            Sign temperatureSign = null;
//            if (!TextUtils.equals("-1", faceAuth.getUserName())) {
//                temperatureSign = SignManager.instance().checkSignData(faceAuth, temper);
//            }
            if (temperatureSign == null) {
                temperatureSign = SignManager.instance().getTemperatureSign(temper);
            }

            Bitmap picture = faceView.getPicture(faceAuth.getTrackId());
            if (picture == null) {
                return;
            }
            temperatureSign.setImgBitmap(picture);
            temperatureSign.setHotImageBitmap(mCurrHotBitmap);
            //设置温度显示状态
            setTemperTips(temper);
            //添加记录
            if (temper >= mWarningThreshold) {
                handleNumber(false);
                KDXFSpeechManager.instance().playWaningRing();
                signList.add(0, temperatureSign);
                if (signList.size() >= 30) {
                    signList.remove(0);
                    doubleLightRecordAdapter.notifyItemRemoved(signList.size() - 1);
                }
                doubleLightRecordAdapter.notifyItemInserted(0);
                rlvRecord.scrollToPosition(0);
            } else {
                KDXFSpeechManager.instance().playPassRing();
            }

            FaceInfo faceInfo = faceView.getFaceInfo(faceAuth.getTrackId());

            AddBean addBean = new AddBean();
            addBean.sign = temperatureSign;
            addBean.rect = faceInfo.getRect();
            addDataToQueue(addBean);
        }
    };

    /*插入数据*/
    private ConcurrentLinkedQueue<AddBean> dataQueue = new ConcurrentLinkedQueue<>();
    private Thread mDataThread;

    private void addDataToQueue(AddBean addBean) {
        if (addBean == null) {
            return;
        }
        dataQueue.add(addBean);
        if (mDataThread != null && mDataThread.isAlive()) {

        } else {
            mDataThread = new Thread(mDataRunnable);
            mDataThread.start();
        }
    }

    private Runnable mDataRunnable = () -> {
        while (dataQueue.size() > 0) {
            AddBean addBean = dataQueue.poll();
            Sign sign = addBean.sign;
            Bitmap hotImage = sign.getHotImageBitmap();

            Rect hotRect = null;
            Rect realRect = faceView.getRealRect(addBean.rect);
            if (realRect != null) {//如果真实rect不为null的时候再抓取
                hotRect = adjustRect(faceView.getWidth(), faceView.getHeight(), realRect);
                hotRect.left -= 2;
                hotRect.bottom += 3;
            }

            //裁剪热图
            if (hotRect != null) {
                int x = hotRect.left;
                int y = hotRect.top;
                int width = hotRect.width();
                int height = hotRect.height();
                if (x + width > hotImage.getWidth()) {
                    width = hotImage.getWidth() - x;
                }
                if (y + height > hotImage.getHeight()) {
                    height = hotImage.getHeight() - y;
                }
                if (x <= 0) {
                    x = 1;
                }
                if (y <= 0) {
                    y = 1;
                }
                if (width > 0 && height > 0) {
                    Bitmap bitmap = Bitmap.createBitmap(hotImage, x, y, width, height);
                    sign.setHotImageBitmap(bitmap);
                }
            }
            SignManager.instance().uploadTemperatureSignAndDelete(sign);
        }
    };

    class AddBean {
        Sign sign;
        Rect rect;
    }

    //设置温度提示
    private void setTemperTips(float temper) {
        tvTemper.setText(temper + "℃");
        if (temper >= mWarningThreshold) {
            tvTemperStatus.setText(getResString(R.string.main_temp_warning_tips));
            tvTemperStatus.setBackgroundResource(R.mipmap.bg_verify_nopass);
            tvTemper.setTextColor(Color.RED);
        } else {
            tvTemperStatus.setText(getResString(R.string.main_temp_normal_tips));
            tvTemperStatus.setBackgroundResource(R.mipmap.bg_verify_pass);
            tvTemper.setTextColor(Color.GREEN);
        }
    }

    //统计数据
    private void handleNumber(boolean isInit) {
        if (!isInit) {
            mWarningNumber++;
        }
        String number;
        if (mWarningNumber < 10) {
            number = "000" + mWarningNumber;
        } else if (mWarningNumber < 100) {
            number = "00" + mWarningNumber;
        } else if (mWarningNumber < 1000) {
            number = "0" + mWarningNumber;
        } else {
            number = mWarningNumber + "";
        }
        tvWarningNumber.setText(number);
    }


    //设置人脸框
    private void setFaceIndex(List<FacePreviewInfo> facePreviewInfos) {
        ArrayList<FaceIndexInfo> faceIndexInfoList = new ArrayList<>();

        for (int i = 0; i < facePreviewInfos.size(); i++) {
            FacePreviewInfo facePreviewInfo = facePreviewInfos.get(i);
            FaceInfo faceInfo = facePreviewInfo.getFaceInfo();
            Rect realRect = faceView.getRealRect(faceInfo.getRect());
            Rect hotRect = adjustRect(faceView.getWidth(), faceView.getHeight(), realRect);
            //6080热成像画面以左上角为原点，0 <= left <= right <= 80，0 <= top <= bottom <= 60
            //left, right, top, bottom
            FaceIndexInfo faceIndexInfo = new FaceIndexInfo(facePreviewInfo.getTrackId(), hotRect.left - 2, hotRect.right, hotRect.top, hotRect.bottom + 3);
            //可以支持一次传入多个人脸框信息
            faceIndexInfoList.add(faceIndexInfo);
        }

        //设置热成像画面中人脸框的颜色，默认为黑色
        TemperatureModule.getIns().setFaceFrameColor(0xFFFFFFFF);
        //人脸框信息faceIndexInfoList，是否在热成像画面中绘制人脸框(true为绘制)
        TemperatureModule.getIns().setFaceIndexInfoList(faceIndexInfoList, true);
    }

    protected Rect adjustRect(int width, int height, Rect ftRect) {
        int previewWidth = width;
        int previewHeight = height;
        int canvasWidth = 60;
        int canvasHeight = 80;

        if (ftRect == null) {
            return null;
        }

        Rect rect = new Rect(ftRect);
        float horizontalRatio;
        float verticalRatio;
        horizontalRatio = (float) canvasHeight / (float) previewWidth;
        verticalRatio = (float) canvasWidth / (float) previewHeight;
        rect.left *= horizontalRatio;
        rect.right *= horizontalRatio;
        rect.top *= verticalRatio;
        rect.bottom *= verticalRatio;

        Rect newRect = new Rect();
        newRect.left = rect.left;
        newRect.right = rect.right;
        newRect.top = rect.top;
        newRect.bottom = rect.bottom;
        return newRect;
    }

    public Bitmap horverImage(Bitmap bitmap, boolean H, boolean V) {
        int bmpWidth = bitmap.getWidth();
        int bmpHeight = bitmap.getHeight();
        Matrix matrix = new Matrix();
        if (H)
            matrix.postScale(-1, 1);   //水平翻转H
        if (V)
            matrix.postScale(1, -1);   //垂直翻转V
        if (H && V)
            matrix.postScale(-1, -1);   //水平&垂直翻转HV
        return Bitmap.createBitmap(bitmap, 0, 0, bmpWidth, bmpHeight, matrix, true);
    }

    public void startCalibration(View view) {
        if (rlDeviceInfo.isShown()) {
            rlDeviceInfo.setVisibility(View.GONE);
            llCalibration.setVisibility(View.VISIBLE);

            initCalibration();
        }
    }

    private float mCurrCorrectValue;
    private float mCurrBodyTemper;

    private void initCalibration() {
        mCurrCorrectValue = mCorrectValue;
        mCurrBodyTemper = mBodyTemper;
        edtCorrectScdl.setText(mCurrCorrectValue + "");
        edtBodyTemperScdl.setText(mCurrBodyTemper + "");

        btnCorrectSubScdl.setEnabled(false);
        btnCorrectPlusScdl.setEnabled(false);
        btnBodyTemperSubScdl.setEnabled(false);
        btnBodyTemperPlusScdl.setEnabled(false);
        btnCancelCorrectionScdl.setEnabled(false);
        btnConfirmCorrectionScdl.setEnabled(false);

        ButtonClickListener buttonClickListener = new ButtonClickListener() {
            @Override
            public void onShortClick(int viewId) {
                if (viewId == R.id.btn_correct_sub_scdl) {
                    mCurrCorrectValue -= 0.1f;
                    mCurrBodyTemper -= 0.1f;
                } else {
                    mCurrCorrectValue += 0.1f;
                    mCurrBodyTemper += 0.1f;
                }
                mCurrCorrectValue = formatF(mCurrCorrectValue);
                edtCorrectScdl.setText(mCurrCorrectValue + "");
                TemperatureModule.getIns().setmCorrectionValue(mCurrCorrectValue);

                mCurrBodyTemper = formatF(mCurrBodyTemper);
                edtBodyTemperScdl.setText(mCurrBodyTemper + "");
            }

            @Override
            public void onLongClick(int viewId) {
                if (viewId == R.id.btn_correct_sub_scdl) {
                    mCurrCorrectValue -= 0.1f;
                    mCurrBodyTemper -= 0.1f;
                } else {
                    mCurrCorrectValue += 0.1f;
                    mCurrBodyTemper += 0.1f;
                }
                mCurrCorrectValue = formatF(mCurrCorrectValue);
                edtCorrectScdl.setText(mCurrCorrectValue + "");
                TemperatureModule.getIns().setmCorrectionValue(mCurrCorrectValue);

                mCurrBodyTemper = formatF(mCurrBodyTemper);
                edtBodyTemperScdl.setText(mCurrBodyTemper + "");
            }
        };
        btnCorrectSubScdl.setOnTouchListener(buttonClickListener);
        btnCorrectPlusScdl.setOnTouchListener(buttonClickListener);

        ButtonClickListener clickListener = new ButtonClickListener() {
            @Override
            public void onShortClick(int viewId) {
                if (viewId == R.id.btn_body_temper_sub_scdl) {
                    mCurrBodyTemper -= 0.1f;
                    mCurrCorrectValue -= 0.1f;
                } else {
                    mCurrBodyTemper += 0.1f;
                    mCurrCorrectValue += 0.1f;
                }
                mCurrBodyTemper = formatF(mCurrBodyTemper);
                edtBodyTemperScdl.setText(mCurrBodyTemper + "");

                mCurrCorrectValue = formatF(mCurrCorrectValue);
                edtCorrectScdl.setText(mCurrCorrectValue + "");
                TemperatureModule.getIns().setmCorrectionValue(mCurrCorrectValue);
            }

            @Override
            public void onLongClick(int viewId) {
                if (viewId == R.id.btn_body_temper_sub_scdl) {
                    mCurrBodyTemper -= 0.1f;
                    mCurrCorrectValue -= 0.1f;
                } else {
                    mCurrBodyTemper += 0.1f;
                    mCurrCorrectValue += 0.1f;
                }
                mCurrBodyTemper = formatF(mCurrBodyTemper);
                edtBodyTemperScdl.setText(mCurrBodyTemper + "");

                mCurrCorrectValue = formatF(mCurrCorrectValue);
                edtCorrectScdl.setText(mCurrCorrectValue + "");
                TemperatureModule.getIns().setmCorrectionValue(mCurrCorrectValue);
            }
        };
        btnBodyTemperSubScdl.setOnTouchListener(clickListener);
        btnBodyTemperPlusScdl.setOnTouchListener(clickListener);

        View.OnClickListener onClickListener = v -> {
            if (isCalibration) {
                isCalibration = false;
            }
            if (v.getId() == R.id.btn_confirm_correction_scdl) {
                mCorrectValue = mCurrCorrectValue;
                mBodyTemper = mCurrBodyTemper;
                SpUtils.saveFloat(SafetyCheckDoubleLightConst.Key.CORRECT_VALUE, mCorrectValue);
                SpUtils.saveFloat(SafetyCheckDoubleLightConst.Key.BODY_TEMPER, mBodyTemper);
                UIUtils.showShort(SafetyCheckDoubleLightActivity.this, APP.getContext().getResources().getString(R.string.safety_save_completion));
            } else {
                UIUtils.showShort(SafetyCheckDoubleLightActivity.this, APP.getContext().getResources().getString(R.string.safety_save_cancel));
            }
            TemperatureModule.getIns().setmCorrectionValue(mCorrectValue);

            waitTime = 3;
            mCalibrationTemperList.clear();
            llCalibration.setVisibility(View.GONE);
            rlDeviceInfo.setVisibility(View.VISIBLE);
        };
        btnCancelCorrectionScdl.setOnClickListener(onClickListener);
        btnConfirmCorrectionScdl.setOnClickListener(onClickListener);

        TemperatureModule.getIns().setmCorrectionValue(0.0f);
        mCalibrationHandler.sendEmptyMessage(0);
    }

    private boolean isCalibration = false;
    private int waitTime = 3;
    private List<Float> mCalibrationTemperList = new ArrayList<>();
    private Handler mCalibrationHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 0) {
                if (!isCalibration) {
                    isCalibration = true;
                }
                if (waitTime <= 0) {
                    mCalibrationHandler.sendEmptyMessage(1);
                    return true;
                }
                UIUtils.showShort(SafetyCheckDoubleLightActivity.this, getResString(R.string.safety_correct_temper_tip) + " " + waitTime);
                waitTime--;
                mCalibrationHandler.sendEmptyMessageDelayed(0, 1000);
            } else {
                if (mCalibrationTemperList.size() >= 30) {
                    UIUtils.showShort(SafetyCheckDoubleLightActivity.this, APP.getContext().getResources().getString(R.string.safety_correct_temper_completion));

                    Float max = Collections.max(mCalibrationTemperList);
                    tvOriginTScdl.setText(max + "℃");
                    if (max <= mCurrBodyTemper) {
                        tvOriginTScdl.setTextColor(Color.WHITE);
                    } else {
                        tvOriginTScdl.setTextColor(Color.RED);
                    }

                    mCurrCorrectValue = mCurrBodyTemper - max;
                    mCurrCorrectValue = formatF(mCurrCorrectValue);
                    edtCorrectScdl.setText(mCurrCorrectValue + "");
                    TemperatureModule.getIns().setmCorrectionValue(mCurrCorrectValue);

                    btnCorrectSubScdl.setEnabled(true);
                    btnCorrectPlusScdl.setEnabled(true);
                    btnBodyTemperSubScdl.setEnabled(true);
                    btnBodyTemperPlusScdl.setEnabled(true);
                    btnCancelCorrectionScdl.setEnabled(true);
                    btnConfirmCorrectionScdl.setEnabled(true);
                } else {
                    UIUtils.showShort(SafetyCheckDoubleLightActivity.this, getResString(R.string.safety_correct_temper_ing));
                    mCalibrationHandler.sendEmptyMessageDelayed(0, 500);
                }
            }
            return false;
        }
    });

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
        RestartAPPTool.showExitDialog(this, (dialog, which) -> onBackKeyPressed(() -> moveTaskToBack(true)), (dialog, which) -> onBackKeyPressed(() -> APP.exit()));
    }

    private void goSetting() {
        String pwd = SpUtils.getStr(SpUtils.MENU_PWD);
        if (!TextUtils.isEmpty(pwd)) {
            inputPwd(() -> startActivity(new Intent(SafetyCheckDoubleLightActivity.this, SafetyDoubleLightSettingActivity.class)));
            return;
        }
        startActivity(new Intent(this, SafetyDoubleLightSettingActivity.class));
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

    //密码弹窗
    private void inputPwd(final Runnable runnable) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_input_pwd);

        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_edt_shake);
        final View rootView = dialog.findViewById(R.id.ll_input_pwd);
        Button btnConfirm = dialog.findViewById(R.id.btn_input_confirm);
        final EditText edtPwd = dialog.findViewById(R.id.edt_input_pwd);
        btnConfirm.setOnClickListener(v -> {
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
}
