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

import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.callback.HotImageK3232CallBack;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
    public static FaceView faceView;
    private AdsFragment adsFragment;
    private SignFragment signListFragment;
    private TextView tvTemperature;
    private TextView tvRangeTips;
    private TextView tvAmbient;
    private TextView tvTempTips;

    //判断是否开启测温
    private View viewDistance;
    private View tempDetectionDot;
    private boolean isPosterEnabled;//大屏海报是否开启
    private float mAmbCorrValue;
    private float mTempCorrValue;
    private ImageView ivThermalImaging;
    private TextView tvThermalPercent;
    private View llThermalArea;

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
        APP.setMainActivity(this);
        EventBus.getDefault().register(this);
        faceView = findViewById(R.id.face_view);
        faceView.setCallback(faceCallback);
        ivMainLogo = findViewById(R.id.iv_main_logo);
        tvMainAbbName = findViewById(R.id.tv_main_abbname);
        tvMainTopTitle = findViewById(R.id.tv_main_topTitle);
        tvMainBottomTitle = findViewById(R.id.tv_main_bottomTitle);

        tempDetectionDot = findViewById(R.id.iv_temp_detection_dot_main);
        viewDistance = findViewById(R.id.view_face_distance);//人脸限制区域
        tvAmbient = findViewById(R.id.tv_ambient_temperature_main);//实时环境温度
        tvTemperature = findViewById(R.id.tv_temperature_main);//实时检测温度
        tvRangeTips = findViewById(R.id.tv_range_tips_main);//人脸距离提示
        tvTempTips = findViewById(R.id.tv_temp_tips_main);//温度提示
        ivThermalImaging = findViewById(R.id.iv_thermal_imaging_main);
        tvThermalPercent = findViewById(R.id.tv_thermal_percent_main);
        llThermalArea = findViewById(R.id.ll_thermal_area_main);

        //加载签到列表Fragment
        signListFragment = new SignFragment();
        replaceFragment(R.id.ll_list_container, signListFragment);

//        只有竖屏情况下加载信息展示Fragment
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            InformationFragment informationFragment = new InformationFragment();
            replaceFragment(R.id.layout_h, informationFragment);
        }

        initTest();

        /*红外模块是9600，热成像模块是115200*/
        mCurrPortPath = SpUtils.getStr(SpUtils.PORT_PATH, Constants.DEFAULT_PORT_PATH);
        mCurrBaudRate = SpUtils.getIntOrDef(SpUtils.BAUD_RATE, Constants.DEFAULT_BAUD_RATE);
        InfraredTemperatureUtils.getIns().initSerialPort(mCurrPortPath, mCurrBaudRate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCurrModel = SpUtils.getIntOrDef(SpUtils.MODEL_SETTING, Constants.DEFAULT_TEMP_MODEL);//当前模式
        isPosterEnabled = SpUtils.getBoolean(SpUtils.POSTER_ENABLED, Constants.DEFAULT_POSTER_ENABLED);//大屏海报开关
        distanceTipsEnabled = SpUtils.getBoolean(SpUtils.DISTANCE_TIPS_ENABLED, Constants.DEFAULT_DISTANCE_TIPS_ENABLED_VALUE);//距离提示开关
        mGetTempDelayTime = SpUtils.getIntOrDef(SpUtils.GET_TEMP_DELAY_TIME, Constants.DEFAULT_GET_TEMP_DELAY_TIME_VALUE);//设置测温延时
        mTempMinThreshold = SpUtils.getFloat(SpUtils.TEMP_MIN_THRESHOLD, Constants.DEFAULT_TEMP_MIN_THRESHOLD_VALUE); //测温最小阈值
        mTempDValue = SpUtils.getFloat(SpUtils.TEMP_D_VALUE, Constants.DEFAULT_TEMP_D_VALUE_VALUE);//高低温差值（用于判断高度）
        mAmbCorrValue = SpUtils.getFloat(SpUtils.AMB_CORRECT_VALUE, Constants.DEFAULT_AMB_CORRECT_VALUE);//环境温度补正
        mTempCorrValue = SpUtils.getFloat(SpUtils.TEMP_CORRECT_VALUE, Constants.DEFAULT_TEMP_CORRECT_VALUE);//体温检测补正
        mThermalImgMirror = SpUtils.getBoolean(SpUtils.THERMAL_IMAGE_MIRROR, Constants.DEFAULT_THERMAL_IMAGE_MIRROR);//热成像图像镜像
        mTempWarningThreshold = SpUtils.getFloat(SpUtils.TEMP_WARNING_THRESHOLD, Constants.DEFAULT_TEMP_WARNING_THRESHOLD_VALUE); //测温报警阈值
        mCurrBodyMinT = SpUtils.getIntOrDef(SpUtils.BODY_MIN_T, Constants.DEFAULT_BODY_MIN_T_VALUE);//最低体温值
        mCurrBodyMaxT = SpUtils.getIntOrDef(SpUtils.BODY_MAX_T, Constants.DEFAULT_BODY_MAX_T_VALUE);//最高体温值
        mCurrBodyPercent = SpUtils.getIntOrDef(SpUtils.BODY_PERCENT, Constants.DEFAULT_BODY_PERCENT_VALUE);//身体占比

        //设置人脸间隔
        SignManager.instance().setVerifyDelay(mCurrModel == Constants.Model.MODEL_FACE_ONLY ? 10000 : 0);

        if (signListFragment != null) {
            signListFragment.setModelText(Constants.Model.models[mCurrModel]);
        }

        //初始化测温模块
        String portPath = SpUtils.getStr(SpUtils.PORT_PATH, Constants.DEFAULT_PORT_PATH);
        int baudRate = SpUtils.getIntOrDef(SpUtils.BAUD_RATE, Constants.DEFAULT_BAUD_RATE);
        if (!TextUtils.equals(portPath, mCurrPortPath) || baudRate != mCurrBaudRate) {
            mCurrPortPath = portPath;
            mCurrBaudRate = baudRate;
            InfraredTemperatureUtils.getIns().initSerialPort(mCurrPortPath, mCurrBaudRate);
        }

        //设置测温补正值
        InfraredTemperatureUtils.getIns().setaCorrectionValue(mAmbCorrValue);
        InfraredTemperatureUtils.getIns().setmCorrectionValue(mTempCorrValue);

        //设置活体开关
        boolean livenessEnabled = SpUtils.getBoolean(SpUtils.LIVENESS_ENABLED, false);
        faceView.setLiveness(livenessEnabled);

        faceView.resume();
        //根据模式选择启动逻辑
        if (mCurrModel == Constants.Model.MODEL_FACE_ONLY) {
            tempDetectionDot.setVisibility(View.GONE);
            tvAmbient.setVisibility(View.GONE);
            tvRangeTips.setVisibility(View.GONE);
            tvTempTips.setVisibility(View.GONE);
            llThermalArea.setVisibility(View.GONE);
            closeInfraedTemperature();
            closeThermalImaging();
        } else if (mCurrModel == Constants.Model.MODEL_THERMAL_IMAGING_ONLY || mCurrModel == Constants.Model.MODEL_FACE_THERMAL_IMAGING) {//热成像模式
            tempDetectionDot.setVisibility(View.VISIBLE);
            tvAmbient.setVisibility(View.GONE);
            tvRangeTips.setText("");
            tvRangeTips.setVisibility(View.VISIBLE);
            llThermalArea.setVisibility(View.VISIBLE);
            closeInfraedTemperature();
            startThermalImaging();
        } else {//红外测温模式
            tempDetectionDot.setVisibility(View.VISIBLE);
            tvAmbient.setVisibility(View.VISIBLE);
            tvRangeTips.setText("");
            tvRangeTips.setVisibility(View.VISIBLE);
            llThermalArea.setVisibility(View.GONE);

            closeThermalImaging();
            startInfraredTemperature();
        }

        initAds(mCurrModel);

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
                mCacheStrangerTempTrackId = -1;
                if (mCurrModel == Constants.Model.MODEL_FACE_TEMPERATURE) {
                    dismissTemperatureTips(0);
                    setRangeTips("");
                }
                return false;
            }

            //检测到人后开灯
            onLight();
            //收起海报界面
            if (adsFragment != null) {
                adsFragment.detectFace();
            }

            //如果是红外模式和热成像模式则不走识别逻辑
            if (mCurrModel == Constants.Model.MODEL_TEMPERATURE_ONLY || mCurrModel == Constants.Model.MODEL_THERMAL_IMAGING_ONLY) {
                return false;
            } else if (mCurrModel == Constants.Model.MODEL_FACE_TEMPERATURE) {//如果是人脸+测温则判断模式
                //取出人脸的rect
                Rect rect = facePreviewInfo.getFaceInfo().getRect();
                //如果未初始化完成
                if (tvRangeTips == null || rect == null) {
                    return false;
                }

                Rect realRect = faceView.getRealRect(rect);
                int minWidth = viewDistance.getMeasuredWidth();
                int maxHeight = faceView.getHeight();

                //如果人脸距离太远，则无所谓
                if (faceView.checkFaceToFar(realRect, minWidth / 2)) {
                    return false;
                } else if (faceView.checkFaceToFar(realRect, minWidth)) {
                    mCacheTime = 0;
                    setRangeTips("请靠近点");
                    if (distanceTipsEnabled) {
//                        KDXFSpeechManager.instance().playNormal(getResources().getString(R.string.distance_so_far_tips_main));
                    }
                    return false;
                } else if (faceView.checkFaceTooClose(realRect, maxHeight)) {
                    mCacheTime = 0;
//                    setRangeTips(getResources().getString(R.string.distance_so_close_text_tips_main));
                    if (distanceTipsEnabled) {
//                        KDXFSpeechManager.instance().playNormal(getResources().getString(R.string.distance_so_close_tips_main));
                    }
                    return false;
                }
                setRangeTips("");

                //检测人脸是否在框内
                if (!faceView.checkFaceInFrame2(realRect, viewDistance)) {
                    mCacheTime = 0;
//                    setRangeTips(getResources().getString(R.string.please_range_tips_main));
                    return false;
                }
                setRangeTips("");

                //判断当前温度，取36度以上再进行识别
                if (mCacheTemperatureHighestValue < mTempMinThreshold) {
                    return false;
                }
                if (getTempDelayEnabled) {
                    //如果当前缓存时间是0则重置
                    if (mCacheTime == 0) {
                        showTemperatureTips(getResources().getString(R.string.temp_measuring_setting), R.drawable.shape_main_frame_temperature_ing, 3000);
                        mCacheTime = System.currentTimeMillis();
                        return false;
                    } else if (System.currentTimeMillis() - mCacheTime < mGetTempDelayTime) {
                        showTemperatureTips(getResources().getString(R.string.temp_measuring_setting), R.drawable.shape_main_frame_temperature_ing, 3000);
                        return false;
                    }
                }
            } else if (mCurrModel == Constants.Model.MODEL_FACE_THERMAL_IMAGING) {
                Rect rect = facePreviewInfo.getFaceInfo().getRect();
                if (tvRangeTips == null || rect == null) {
                    return false;
                }
                Rect realRect = faceView.getRealRect(rect);
                int distanceWidth = viewDistance.getMeasuredWidth();
                if (faceView.checkFaceToFar(realRect, distanceWidth / 2)) {
//                    setRangeTips(getResources().getString(R.string.distance_so_far_text_tips_main));
                    if (distanceTipsEnabled) {
//                        KDXFSpeechManager.instance().playNormal(getResources().getString(R.string.distance_so_far_tips_main));
                    }
                    return false;
                }
                setRangeTips("");

                //判断时间
                if (getTempDelayEnabled) {
                    if (mCacheTime_TI == 0) {
                        showTemperatureTips(getResources().getString(R.string.temp_measuring_setting), R.drawable.shape_main_frame_temperature_ing, 3000);
                        mCacheTime_TI = System.currentTimeMillis();
                        return false;
                    } else if (System.currentTimeMillis() - mCacheTime_TI < mGetTempDelayTime) {
                        showTemperatureTips(getResources().getString(R.string.temp_measuring_setting), R.drawable.shape_main_frame_temperature_ing, 3000);
                        return false;
                    }
                }

                if (mTemperatureCacheList == null || mTemperatureCacheList.size() <= 0) {
                    mBroadCastFlag = false;
                    mTemperatureCacheList.clear();
                    mCacheTime_TI = 0;
                    return false;
                }

                Float maxValue = Collections.max(mTemperatureCacheList);
                if (maxValue < mTempMinThreshold) {
//                    showTemperatureTips(getResources().getString(R.string.please_waiting_main), R.drawable.shape_main_frame_temperature_ing, 3000);
                    mBroadCastFlag = false;
                    mTemperatureCacheList.clear();
                    mCacheTime_TI = 0;
                    return false;
                }

                if (mBroadCastFlag) {
                    return false;
                }
                mBroadCastFlag = true;
                return true;
            }

            return true;
        }

        @Override
        public void onFaceVerify(CompareResult compareResult) {
            if (mCurrModel == Constants.Model.MODEL_FACE_THERMAL_IMAGING) {
                Bitmap facePicture = faceView.takePicture();
                //如果三要素不存在，则重置状态
                if (facePicture == null || mCacheHotImage == null || mTemperatureCacheList.size() <= 0) {
//                    showTemperatureTips(getResources().getString(R.string.please_waiting_main), R.drawable.shape_main_frame_temperature_ing, 3000);
                    mBroadCastFlag = false;
                    mTemperatureCacheList.clear();
                    mCacheTime_TI = 0;
                    return;
                }

                Float maxValue = Collections.max(mTemperatureCacheList);
                boolean isWarning = maxValue >= mTempWarningThreshold;
                Sign sign = null;
                if (compareResult.getSimilar() == -1) {
                    //直接上报温度
                    sign = SignManager.instance().getTemperatureSign(maxValue);
                } else {
                    sign = SignManager.instance().checkSignData(compareResult, maxValue);
                    if (sign == null) {
                        return;
                    }
                }

                sign.setImgBitmap(facePicture);
                sign.setHotImageBitmap(mCacheHotImage);

                //提示
                playTips(isWarning, sign.getName(), sign.getTemperature());
                //更新记录
                if (signListFragment != null) {
                    signListFragment.addSignData(sign);
                }
                //上传记录
                SignManager.instance().uploadTemperatureSign(sign);

                //如果是过期或陌生人则结束
                if (sign.getType() == -2 || sign.getType() == -9 || isWarning) {
                    return;
                }
                openDoor();
            } else if (mCurrModel == Constants.Model.MODEL_FACE_TEMPERATURE) {
                Bitmap bitmap = faceView.takePicture();
                if (bitmap == null) {
                    return;
                }

                boolean isWarning = mCacheTemperatureHighestValue >= mTempWarningThreshold;
                Sign sign = null;
                if (compareResult.getSimilar() == -1) {
                    //代表已经播报过
                    if (mCacheStrangerTempTrackId == compareResult.getTrackId()) {
                        return;
                    }
                    mCacheStrangerTempTrackId = compareResult.getTrackId();
                    //直接上报温度
                    sign = SignManager.instance().getTemperatureSign(mCacheTemperatureHighestValue);
                } else {
                    mCacheStrangerTempTrackId = -1;
                    sign = SignManager.instance().checkSignData(compareResult, mCacheTemperatureHighestValue);
                    if (sign == null) {
                        return;
                    }
                }

                playTips(isWarning, sign.getName(), sign.getTemperature());

                sign.setImgBitmap(bitmap);
                sign.setHotImageBitmap(mCacheHotImage);

                if (signListFragment != null) {
                    signListFragment.addSignData(sign);
                }

                SignManager.instance().uploadTemperatureSign(sign);


                // TODO: 2020/3/1 处理这部分逻辑 如果体温不正常不让进门
                //如果是过期或陌生人则结束
                if (sign.getType() == -2 || sign.getType() == -9 || isWarning) {
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
                    VipDialogManager.showVipDialog(WelComeActivity.this, sign);
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
                VipDialogManager.showVipDialog(WelComeActivity.this, sign);
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
    //开关类
    private boolean distanceTipsEnabled = true;//距离提示语音播报
    private boolean mThermalImgMirror = false;
    private int mCurrModel = 0;//当前模式
    private String mCurrPortPath = "";//当前端口号
    private int mCurrBaudRate;//当前波特率
    private boolean getTempDelayEnabled = true;//延时取温
    //设置类
    private int mGetTempDelayTime;//采集温度延时
    private float mTempMinThreshold;//最小阈值
    private float mTempWarningThreshold;//报警值
    private float mTempDValue;
    private int mCurrBodyMinT;//当前最低体温
    private int mCurrBodyMaxT;//当前最高体温
    private int mCurrBodyPercent;//当前身体占比

    //缓存值类
    private float mCacheTemperatureHighestValue = 0f;//最高温度值缓存
    private float mCacheValueForTempModel = 0f;//专为测温模式缓存的数值,用于判断该次有没有被播报,防止多次播报
    private int mCacheStrangerTempTrackId = -1;//判断陌生人有没有播报过的缓存，如果该Id不等于认证的Id说明没播报过，在非陌生人的地方和检测不到人脸时重置该Id

    private float mCacheDetectionValue = 0f;//检测值缓存，用于检测是否有人进来和有人离开
    private long mCacheTime = 0;//时间缓存

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

    //=热成像测温逻辑==================================================
    private void startThermalImaging() {
        Log.e(TAG, "startThermalImaging: 开始执行热成像逻辑");
        InfraredTemperatureUtils.getIns().closeHotImage3232();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                TemperatureModule.getIns().startHotImageK3232(mThermalImgMirror, false, );
            }
        }, 1000);
    }

    private List<Float> mTemperatureCacheList = new ArrayList<>();
    private boolean mBroadCastFlag = false;
    private long mCacheTime_TI = 0;

    HotImageK3232CallBack hotImageK3232CallBack = new HotImageK3232CallBack() {
        @Override
        public void newestHotImageData(Bitmap bitmap, float v, float v1, float v2, float v3, boolean b, int i) {

        }
    };

    private InfraredTemperatureUtils.HotImageDataCallBack hotImageDataCallBack = new InfraredTemperatureUtils.HotImageDataCallBack() {
        @Override
        public void newestHotImageData(final Bitmap imageBmp, final float sensorT, final float maxT, final float minT, final float bodyMaxT, final boolean isBody, final int bodyPercentage) {
            mCacheHotImage = imageBmp;

            if (ivThermalImaging != null) {
                ivThermalImaging.post(new Runnable() {
                    @Override
                    public void run() {
                        ivThermalImaging.setImageBitmap(imageBmp);
                        if (tvThermalPercent != null) {
//                            tvThermalPercent.setText(getResources().getString(R.string.thermal_update_main) + bodyMaxT + "℃");
                        }
                    }
                });
            }

            //仅热成像模式
            if (mCurrModel == Constants.Model.MODEL_THERMAL_IMAGING_ONLY) {
                //如果有人
                if (isBody) {
                    if (bodyMaxT <= 0) {
                        return;
                    }

                    //如果缓存集合种包含这个温度则不再添加
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

                    if (getTempDelayEnabled) {
                        if (mCacheTime_TI == 0) {
                            showTemperatureTips(getResources().getString(R.string.temp_measuring_setting), R.drawable.shape_main_frame_temperature_ing, 3000);
                            mCacheTime_TI = System.currentTimeMillis();
                            return;
                        } else if (System.currentTimeMillis() - mCacheTime_TI < mGetTempDelayTime) {
                            showTemperatureTips(getResources().getString(R.string.temp_measuring_setting), R.drawable.shape_main_frame_temperature_ing, 3000);
                            return;
                        }
                    }

                    Float maxValue = Collections.max(mTemperatureCacheList);

                    if (maxValue < mTempMinThreshold) {
                        /*showTemperatureTips(getResources().getString(R.string.please_waiting_main), R.drawable.shape_main_frame_temperature_ing, 3000);
                        mTemperatureCacheList.clear();
                        mBroadCastFlag = false;
                        mCacheTime_TI = 0;*/
                        return;
                    }

                    //判断缓存标记
                    if (mBroadCastFlag) {
                        return;
                    }
                    mBroadCastFlag = true;

                    //截取摄像头画面并提示
                    Bitmap currCameraFrame = faceView.getCurrCameraFrame();
                    if (currCameraFrame == null) {
                        return;
                    }

                    boolean isWarning = maxValue >= mTempWarningThreshold;
                    final Sign temperatureSign = SignManager.instance().getTemperatureSign(maxValue);
                    temperatureSign.setImgBitmap(currCameraFrame);
                    temperatureSign.setHotImageBitmap(imageBmp);

                    //提示
                    playTipsAddOpenDoor(isWarning, "", temperatureSign.getTemperature());
                    //更新记录
                    if (signListFragment != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                signListFragment.addSignData(temperatureSign);
                            }
                        });
                    }
                    //上传记录
                    SignManager.instance().uploadTemperatureSign(temperatureSign);
                } else {
                    mBroadCastFlag = false;
                    mTemperatureCacheList.clear();
                    mCacheTime_TI = 0;
                }
            }

            //人脸+热成像模式
            else if (mCurrModel == Constants.Model.MODEL_FACE_THERMAL_IMAGING) {
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
                    mBroadCastFlag = false;
                    mTemperatureCacheList.clear();
                    mCacheTime_TI = 0;
                }
            }
        }
    };

    private Bitmap mCacheHotImage = null;

    private void closeThermalImaging() {
        InfraredTemperatureUtils.getIns().closeHotImage3232();
    }

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

//    private List<Float> infaredCacheList = new ArrayList<>();
//    private long mCacheTime_IF = 0;
//    private boolean mBroadCastFlag_IN = false;
//    private boolean checkBody(float measuringTemperatureF) {
//        Log.e(TAG, "checkBody: 检测值：" + measuringTemperatureF);
//        if (measuringTemperatureF <= mTempCorrValue) {
//            return false;
//        }
//
//        if (mCacheDetectionValue == 0f) {
//            mCacheDetectionValue = measuringTemperatureF;
//            return false;
//        }
//
//        if (measuringTemperatureF >= mTempMinThreshold) {
//            return true;
//        }
//
//        //检测对比缓存小于高低差值说明没人
//        if ((measuringTemperatureF > mCacheDetectionValue && measuringTemperatureF - mCacheDetectionValue < mTempDValue) || (mCacheDetectionValue > measuringTemperatureF && mCacheDetectionValue - measuringTemperatureF < mTempDValue)) {
////            mCacheDetectionValue = measuringTemperatureF;
//            Log.e(TAG, "checkBody: 温度波动：缓存值：" + mCacheDetectionValue);
//            return false;
//        }
//
//        if (measuringTemperatureF - mCacheDetectionValue >= mTempDValue) {
//            return true;
//        }
//
//        return false;
//    }

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

            measuringTemperatureF += mTestCorrValue;
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

//                if (checkBody(measuringTemperatureF)) {
//                    Log.e(TAG, "run: 有人，温度：" + measuringTemperatureF);
//
//                    if (measuringTemperatureF < mTempMinThreshold) {
//                        if (distanceTipsEnabled) {
//                            KDXFSpeechManager.instance().playNormal("请靠近点");
//                        }
//                        setRangeTips("距离太远，请靠近");
//                        infraredHandler.postDelayed(temperatureUpdateRunnable, 400);
//                        return;
//                    }
//                    setRangeTips("");
//
//                    Log.e(TAG, "run: 正在检测");
//                    if (infaredCacheList.size() <= 0) {
//                        infaredCacheList.add(measuringTemperatureF);
//                    } else {
//                        Float maxValue = Collections.max(infaredCacheList);
//                        if (!infaredCacheList.contains(measuringTemperatureF) && maxValue < measuringTemperatureF) {
//                            infaredCacheList.add(measuringTemperatureF);
//                        }
//                    }
//
//                    if (getTempDelayEnabled) {
//                        if (mCacheTime_IF == 0) {
//                            showTemperatureTips("正在测温", R.drawable.shape_main_frame_temperature_ing, 3000);
//                            mCacheTime_IF = System.currentTimeMillis();
//                            infraredHandler.postDelayed(temperatureUpdateRunnable, 400);
//                            return;
//                        } else if (System.currentTimeMillis() - mCacheTime_IF < mGetTempDelayTime) {
//                            showTemperatureTips("正在测温", R.drawable.shape_main_frame_temperature_ing, 3000);
//                            infraredHandler.postDelayed(temperatureUpdateRunnable, 400);
//                            return;
//                        }
//                    }
//
//                    if (!mBroadCastFlag_IN) {
//                        mBroadCastFlag_IN = true;
//                        //截取摄像头画面并提示
//                        Bitmap currCameraFrame = faceView.getCurrCameraFrame();
//                        if (currCameraFrame != null) {
//                            Float maxValue = Collections.max(infaredCacheList);
//                            boolean isWarning = maxValue >= mTempWarningThreshold;
//                            final Sign temperatureSign = SignManager.instance().getTemperatureSign(maxValue);
//                            temperatureSign.setImgBitmap(currCameraFrame);
//
//                            //提示
//                            playTipsAddOpenDoor(isWarning, "", temperatureSign.getTemperature());
//                            //更新记录
//                            if (signListFragment != null) {
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        signListFragment.addSignData(temperatureSign);
//                                    }
//                                });
//                            }
//                            //上传记录
//                            SignManager.instance().uploadTemperatureSign(temperatureSign);
//
//                        } else {
//                            setRangeTips("");
//                            infaredCacheList.clear();
//                            mBroadCastFlag_IN = true;
//                            mCacheTime_IF = 0;
//                        }
//                    }
//                } else {
//                    setRangeTips("");
//                    infaredCacheList.clear();
//                    mBroadCastFlag_IN = false;
//                    mCacheTime_IF = 0;
//                }

                if (measuringTemperatureF <= mTempCorrValue) {
                    mCacheTime = 0;//取温缓存时间重置
                    mCacheValueForTempModel = 0f;//防重复缓存值重置
                    mCacheTemperatureHighestValue = 0f;//缓存最终值重置
                    setRangeTips("");
                    infraredHandler.postDelayed(temperatureUpdateRunnable, 400);
                    return;
                }

                //如果为0说明是初始化状态
                if (mCacheDetectionValue == 0f) {
                    mCacheTime = 0;//取温缓存时间重置
                    mCacheValueForTempModel = 0f;//防重复缓存值重置
                    mCacheTemperatureHighestValue = 0f;//缓存最终值重置
                    mCacheDetectionValue = measuringTemperatureF;//在w
                    setRangeTips("");
                    infraredHandler.postDelayed(temperatureUpdateRunnable, 400);
                    return;
                }

                //如果检测温度比缓存温度小,或者测温值与缓存值之差小于3度则略过，，说明没人，或者有人走了
                if (measuringTemperatureF <= mCacheDetectionValue) {
                    mCacheTime = 0;
                    mCacheValueForTempModel = 0f;
                    mCacheTemperatureHighestValue = 0f;
                    mCacheDetectionValue = measuringTemperatureF;
                    setRangeTips("");
                    infraredHandler.postDelayed(temperatureUpdateRunnable, 400);
                    return;
                }

                //如果温度差小于这个数说明是温度波动,不用理会
                if (measuringTemperatureF - mCacheDetectionValue < mTempDValue) {
                    setRangeTips("");
                    infraredHandler.postDelayed(temperatureUpdateRunnable, 400);
                    return;
                }

                //如果比缓存温度高过3度，且，检测值小于阈值，说明距离较远
                if (measuringTemperatureF - mCacheDetectionValue >= mTempDValue && measuringTemperatureF < mTempMinThreshold) {
                    mCacheTime = 0;
                    mCacheValueForTempModel = 0f;
                    mCacheTemperatureHighestValue = 0f;
//                    setRangeTips(getResources().getString(R.string.distance_so_far_text_tips_main));
                    if (distanceTipsEnabled) {
//                        KDXFSpeechManager.instance().playNormal(getResources().getString(R.string.distance_so_far_tips_main));
                    }
                    infraredHandler.postDelayed(temperatureUpdateRunnable, 400);
                    return;
                }

                setRangeTips("");
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
                    if (getTempDelayEnabled) {
                        if (mCacheTime == 0) {
                            showTemperatureTips(getResources().getString(R.string.temp_measuring_setting), R.drawable.shape_main_frame_temperature_ing, 3000);
                            mCacheTime = System.currentTimeMillis();
                            infraredHandler.postDelayed(temperatureUpdateRunnable, 400);
                            return;
                        } else if (System.currentTimeMillis() - mCacheTime < mGetTempDelayTime) {
                            showTemperatureTips(getResources().getString(R.string.temp_measuring_setting), R.drawable.shape_main_frame_temperature_ing, 3000);
                            infraredHandler.postDelayed(temperatureUpdateRunnable, 400);
                            return;
                        }
                    }
                } else {//如果温度小于阈值则归零
                    mCacheTemperatureHighestValue = 0f;
                    mCacheValueForTempModel = 0f;
                    mCacheTime = 0;
                }

                //在最终值大于最小值的时候
                if (mCacheTemperatureHighestValue >= mTempMinThreshold) {
                    if (mCacheValueForTempModel == 0f) {
                        Bitmap currCameraFrame = faceView.getCurrCameraFrame();
                        if (currCameraFrame != null) {
                            mCacheValueForTempModel = mCacheTemperatureHighestValue;
                            boolean isWarning = mCacheTemperatureHighestValue >= mTempWarningThreshold;

                            Sign temperatureSign = SignManager.instance().getTemperatureSign(mCacheTemperatureHighestValue);
                            temperatureSign.setImgBitmap(currCameraFrame);

                            //提示
                            playTipsAddOpenDoor(isWarning, "", temperatureSign.getTemperature());
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

                //如果是人脸+红外，就走存最高值的逻辑
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

            infraredHandler.postDelayed(temperatureUpdateRunnable, 400);
        }
    };

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

        if (time > 0) {
            dismissTemperatureTips(time);
        }
    }

    private void dismissTemperatureTips(int time) {
        if (tvTempTips.isShown()) {
            tempTipsHandler.removeMessages(0);
            boolean b = tempTipsHandler.sendEmptyMessageDelayed(0, time);
        }
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
//            speechTips = TextUtils.isEmpty(warningTips)
////                    ? getResources().getString(R.string.temperature_tips_warning_main) + temperature + "℃"
//                    : warningTips;
//            textTips = getResources().getString(R.string.temperature_tips_warning_main) + temperature + "℃";

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
//            speechTips = TextUtils.isEmpty(normalTips)
//                    ? getResources().getString(R.string.temperature_tips_normal_main) + temperature + "℃"
//                    : normalTips;
//            textTips = getResources().getString(R.string.temperature_tips_normal_main) + temperature + "℃";

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

//        showTemperatureTips(textTips, bgId, -1);
//        KDXFSpeechManager.instance().playNormal((TextUtils.isEmpty(signName) ? "" : (signName + "，")) + speechTips, warningRunnable);
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
    /**
     * @param mCurrModel
     **************************************************************************************************/
    private void initAds(int mCurrModel) {
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
        /*if (ReadCardUtils.isInputFromReader(this, event)) {
            if (readCardUtils != null) {
                readCardUtils.resolveKeyEvent(event);
            }
        }*/
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void initData() {
//        initCardReader();

        KDXFSpeechManager.instance().init(this).welcome();

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent event) {

        Log.e(TAG, "update: 11111111111111");
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

        InfraredTemperatureUtils.getIns().closeSerialPort();
        faceView.destory();
        destoryXmpp();

        EventBus.getDefault().unregister(this);
        SyncManager.instance().destory();
        KDXFSpeechManager.instance().destroy();
        LocateManager.instance().destory();
    }
}