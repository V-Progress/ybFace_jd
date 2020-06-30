package com.yunbiao.ybsmartcheckin_live_id.temper_5inch.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.callback.MLX90614GgTempCallBack;
import com.ys.rkapi.MyManager;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.business.LocateManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.databinding.Activity5inchMainBinding;
import com.yunbiao.ybsmartcheckin_live_id.printer.T;
import com.yunbiao.ybsmartcheckin_live_id.printer.Utils;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.ConfigurationFileManager;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.SpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.Temper5InchConst;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.databean.MainDataBean;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.utils.BigDecimalUtils;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.utils.FileUtils;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.utils.ResourceUtils;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.utils.TemperatureUnitUtils;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.view.MeasureResultDialog;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.xmpp.ServiceManager;

import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;

public class Main5InchActivity extends Base5InchActivity {

    // xmpp推送服务
    private ServiceManager serviceManager;

    private Activity5inchMainBinding activity5inchMainBinding;
    private MainDataBean mainDataBean;
    private ImageOptions adOptions = new ImageOptions.Builder().setFadeIn(true).setImageScaleType(ImageView.ScaleType.FIT_CENTER).build();

    private int position;
    private List<File> mAdList;
    private MediaPlayer mediaPlayer;

    public static int temperatureUnit = 1;
    private float calibrationValue = 0;
    private int carouselInterval = 0;
    private int subtitleSpeed = 0;
    private float warningValue = 0;

    private int normalNum = 0;
    private int abnormalNum = 0;

    private boolean isStop = false;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_5inch_main;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_5inch_main;
    }

    @Override
    protected void initView() {
        activity5inchMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_5inch_main);
        updateConfiguration(false);
        mainDataBean = new MainDataBean(
                new ObservableField<>(temperatureUnit == 1 ? "℃" : "℉"),
                new ObservableField<>(normalNum),
                new ObservableField<>(abnormalNum)
        );

        activity5inchMainBinding.tsTemperature.setFactory(() -> {
            TextView textView = (TextView) View.inflate(activity5inchMainBinding.tsTemperature.getContext(), R.layout.layout_5inch_temperature_textview, null);
            return textView;
        });
        activity5inchMainBinding.setMainDataBean(mainDataBean);
        activity5inchMainBinding.setEventListener(new EventListener());
    }

    @Override
    protected void initData() {
        SpeechManager.getInstance().init();
        loadConfigurationFile();
        MeasureResultDialog.getInstance().init();
        //开启Xmpp
        startXmpp();
        //初始化定位工具
        LocateManager.instance().init(this);

        SyncManager.instance().requestOnlyCompany();
        SignManager.instance();

        TemperatureModule.getIns().setHotImageColdMode(false);
        TemperatureModule.getIns().setHotImageHotMode(false, 45.0f);
        TemperatureModule.getIns().setmCorrectionValue(calibrationValue);
        TemperatureModule.getIns().initSerialPort(this, "/dev/ttyS0", 9600);
        TemperatureModule.getIns().setMlx90614GgTempCallBack(mlx90614GgTempCallBack);
    }

    private boolean isCover = false;
    private long coverTime = 0;
    private boolean isReport = false;
    private List<Float> tempValueList = new ArrayList<>();
    List<Float> hTempList = new ArrayList<>();
    List<Float> lTempList = new ArrayList<>();
    private MLX90614GgTempCallBack mlx90614GgTempCallBack = new MLX90614GgTempCallBack() {
        @Override
        public void newestInfraredTemp(float measuringTempF, float afterTreatmentF) {
            if (System.currentTimeMillis() - coverTime > 150) {
                isCover = false;
            }
            if (isStop || !isReport || MeasureResultDialog.getInstance().isShowing()) {
                tempValueList.clear();
                hTempList.clear();
                lTempList.clear();
                return;
            }
            tempValueList.add(afterTreatmentF);
            if (afterTreatmentF > 37.3) {
                hTempList.add(afterTreatmentF);
            } else {
                lTempList.add(afterTreatmentF);
            }
            if (tempValueList.size() < 5) {
                return;
            }
            //开始处理温度数据
            Collections.sort(tempValueList);
            Collections.sort(hTempList);
            Collections.sort(lTempList);
            float temp = 0;
            boolean isSatisfy = false;
            if (tempValueList.size() == 5) {
                if (hTempList.size() >= 3) {
                    temp = hTempList.get(0);
                } else {
                    temp = lTempList.get(lTempList.size() - 1);
                }
                if (temp >= 35.0f) {
                    isSatisfy = true;
                }
            }
            if (tempValueList.size() == 8) {
                if (hTempList.size() >= 3) {
                    temp = hTempList.get(0);
                } else {
                    temp = lTempList.get(lTempList.size() - 1);
                }
                isSatisfy = true;
            }
            if (!isSatisfy) {
                return;
            }
            isReport = false;
            tempValueList.clear();
            hTempList.clear();
            lTempList.clear();
            if (!isCover) {
                return;
            }
            if (!mainDataBean.isTest.get() && temp >= Temper5InchConst.stableLowerLimit && temp <= Temper5InchConst.stableUpperLimit) {
                temp = Temper5InchConst.stableTemp[(int)(Math.random() * Temper5InchConst.stableTemp.length)];
            }
            float tempC = temp;
            if (temperatureUnit == 2) {
                temp = TemperatureUnitUtils.c2f(temp);
            }
            if (mainDataBean.isTest.get()) {
                cmSetMeasurementValue(temp);
                return;
            }
            TextView tvTempNext = (TextView) activity5inchMainBinding.tsTemperature.getNextView();
            if (BigDecimalUtils.compare(temp, warningValue)) {
                mainDataBean.getTempStatus().set(2);
                tvTempNext.setTextColor(ResourceUtils.getColorResource(R.color.temperature_abnormal_text));
                MeasureResultDialog.getInstance().showResultDialog(Main5InchActivity.this, activity5inchMainBinding.cvMainCamera.getCurrCameraFrame(), tempC, false);
                speechText(temp, false);
                abnormalNum += 1;
                mainDataBean.getAbnormalNum().set(abnormalNum);
            } else {
                speechText(temp, true);
                if ((temperatureUnit == 1 && temp >= 35.0f) || (temperatureUnit == 2 && temp >= 95.0f)) {
                    MeasureResultDialog.getInstance().showResultDialog(Main5InchActivity.this, activity5inchMainBinding.cvMainCamera.getCurrCameraFrame(), tempC, true);
                    tvTempNext.setTextColor(ResourceUtils.getColorResource(R.color.temperature_normal_text));
                    mainDataBean.getTempStatus().set(1);
                    normalNum += 1;
                    mainDataBean.getNormalNum().set(normalNum);
                } else {
                    tvTempNext.setTextColor(ResourceUtils.getColorResource(R.color.temperature_how_low_text));
                    mainDataBean.getTempStatus().set(3);
                }
            }
            mainDataBean.getTemperature().set(String.valueOf(temp));
        }

        @Override
        public void distanceResponse() {
            coverTime = System.currentTimeMillis();
            if (!isCover) {
                isCover = true;
                isReport = true;
            }
        }
    };

    private void updateConfiguration(boolean isRefreshCamera) {
        temperatureUnit = SpUtils.getIntOrDef(Temper5InchConst.Key.TEMPERATURE_UNIT, Temper5InchConst.Default.temperatureUnitDef);
        calibrationValue = SpUtils.getFloat(Temper5InchConst.Key.CALIBRATION_VALUE, Temper5InchConst.Default.calibrationValueDef);
        carouselInterval = SpUtils.getIntOrDef(Temper5InchConst.Key.CAROUSEL_INTERVAL, Temper5InchConst.Default.carouselIntervalDef);
        subtitleSpeed = SpUtils.getIntOrDef(Temper5InchConst.Key.SUBTITLE_SPEED, Temper5InchConst.Default.subtitleSpeedDef);
        activity5inchMainBinding.mtvAnnouncement.setScrollSpeed(subtitleSpeed);
        warningValue = SpUtils.getFloat(Temper5InchConst.Key.WARNING_VALUE, Temper5InchConst.Default.warningValueDef);
        if (mainDataBean != null) {
            mainDataBean.getTemperatureUnit().set(temperatureUnit == 1 ? "℃" : "℉");
        }
        if (isRefreshCamera) {
            activity5inchMainBinding.cvMainCamera.refresh();
        }
    }

    private void loadConfigurationFile() {
        File logoFile = ConfigurationFileManager.getLogoFile();
        if (logoFile != null) {
            x.image().bind(activity5inchMainBinding.ivMainLogo, logoFile.toString(), adOptions);
        } else {
            activity5inchMainBinding.ivMainLogo.setImageResource(R.mipmap.yb_logo);
        }
        loadAD();
        loadSubtitle();
    }

    private void loadAD() {
        mAdList = ConfigurationFileManager.getAd();
        if (mAdList == null) {
            activity5inchMainBinding.ivAd.setVisibility(View.VISIBLE);
            activity5inchMainBinding.vvAd.setVisibility(View.GONE);
            activity5inchMainBinding.ivAd.setImageResource(R.mipmap.examples_ad);
        } else {
            Message msg = new Message();
            msg.what = 1;
            msg.arg1 = 0;
            mainHandler.sendMessage(msg);
        }
    }

    private void loadSubtitle() {
        String subtitleContent = ConfigurationFileManager.getSubtitle();
        mainDataBean.getSubtitleContent().set(subtitleContent);
        activity5inchMainBinding.mtvAnnouncement.setScrollSpeed(subtitleSpeed);
        activity5inchMainBinding.mtvAnnouncement.startScroll();
    }

    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            Main5InchActivity.this.mediaPlayer = mediaPlayer;
            activity5inchMainBinding.vvAd.start();
        }
    };
    private MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            Message msg = new Message();
            msg.what = 1;
            msg.arg1 = position + 1;
            mainHandler.sendMessage(msg);
            return true;
        }
    };
    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            Message msg = new Message();
            msg.what = 1;
            msg.arg1 = position + 1;
            mainHandler.sendMessage(msg);
        }
    };

    private Handler mainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    position = msg.arg1;
                    position = position % mAdList.size();

                    File file = mAdList.get(position);
                    if (FileUtils.isVideo(file.toString())) {
                        activity5inchMainBinding.ivAd.setVisibility(View.GONE);
                        activity5inchMainBinding.vvAd.setVisibility(View.VISIBLE);
                        activity5inchMainBinding.vvAd.setVideoPath(file.toString());
                        activity5inchMainBinding.vvAd.setOnPreparedListener(onPreparedListener);
                        activity5inchMainBinding.vvAd.setOnErrorListener(onErrorListener);
                        activity5inchMainBinding.vvAd.setOnCompletionListener(onCompletionListener);
                    } else {
                        activity5inchMainBinding.ivAd.setVisibility(View.VISIBLE);
                        activity5inchMainBinding.vvAd.setVisibility(View.GONE);
                        x.image().bind(activity5inchMainBinding.ivAd, file.toString(), adOptions);

                        Message msg2 = new Message();
                        msg2.what = 1;
                        msg2.arg1 = position + 1;
                        this.sendMessageDelayed(msg2, carouselInterval * 1000);
                    }
                    break;
                case 2:
                    setFullscreen(false, false);
                    MyManager.getInstance(Main5InchActivity.this).setSlideShowNotificationBar(false);
                    MyManager.getInstance(Main5InchActivity.this).hideNavBar(true);
                    updateConfiguration(true);
                    TemperatureModule.getIns().setmCorrectionValue(calibrationValue);
                    break;
                case 3:
                    isCover = false;
                    tempValueList.clear();
                    hTempList.clear();
                    lTempList.clear();
                    break;
                case 4:
//                    controlVideoAdVolume(1.0f);
                    playVideoSwitch(true);
                    break;
            }
        }
    };

    private void controlVideoAdVolume (float volume) {
        if (activity5inchMainBinding.vvAd.isPlaying() && mediaPlayer != null) {
            mediaPlayer.setVolume(volume, volume);
        }
    }

    private void playVideoSwitch(boolean isPlay) {
        if (activity5inchMainBinding.vvAd.isPlaying() && !isPlay) {
            activity5inchMainBinding.vvAd.pause();
        } else if (activity5inchMainBinding.vvAd.getVisibility() == View.VISIBLE && isPlay) {
            activity5inchMainBinding.vvAd.start();
        }
    }

    private void speechText(float temperature, boolean isNormal) {
        playVideoSwitch(false);
        StringBuffer stringBuffer = new StringBuffer();
        if((temperatureUnit == 1 && temperature > 45.0f) || (temperatureUnit == 2 && temperature > 113.0f)){
            stringBuffer.append(getResString(R.string.main_temp_error_tips));
        } else if ((temperatureUnit == 1 && temperature < 35.0f) || (temperatureUnit == 2 && temperature < 95.0f)) {
            stringBuffer.append(getResString(R.string.sp_temp_check_again));
        } else {
            if (isNormal) {
                stringBuffer.append(getResString(R.string.sp_temp_normal_tips));
            } else {
                stringBuffer.append(getResString(R.string.sp_temp_warning_tips));
            }
            boolean isZh = Utils.isZh(this);
            stringBuffer.append(" " + temperature);
            if (isZh) {
                SpeechManager.getInstance().setSpeed(2.2f);
            } else {
                SpeechManager.getInstance().setSpeed(1.8f);
            }
            if (temperatureUnit == 1) {
                stringBuffer.append(isZh ? "摄氏度" : getResString(R.string.celsius_text));
            } else {
                stringBuffer.append(isZh ? "华氏度" : getResString(R.string.fahrenheit_text));
            }
        }
//        controlVideoAdVolume(0.1f);
        SpeechManager.getInstance().stopCurrent();
        SpeechManager.getInstance().playNormal(stringBuffer.toString(), isNormal, new SpeechCallback(isNormal));
    }

    private class SpeechCallback implements Runnable {
        private boolean isNormal;
        public SpeechCallback(boolean isNormal) {
            this.isNormal = isNormal;
        }
        @Override
        public void run() {
            long delayMillis = 800;
            if (!isNormal) {
                SpeechManager.getInstance().stopCurrent();
                SpeechManager.getInstance().playWaningRing();
                delayMillis = 2800;
            }
            mainHandler.removeMessages(4);
            mainHandler.sendEmptyMessageDelayed(4, delayMillis);
        }
    }

    private void cmSetCalibrationValue() {
        float measurementValue = mainDataBean.cmMeasurementValue.get();
        if (measurementValue != 0) {
            float bodyTemperature = mainDataBean.cmBodyTemperatureValue.get();
            mainDataBean.cmCalibrationValue.set(BigDecimalUtils.sub(bodyTemperature, measurementValue, 1).floatValue());
        }
    }

    private void cmSetMeasurementValue(float temp) {
        mainDataBean.cmMeasurementValue.set(temp);
        cmSetCalibrationValue();
    }

    public class EventListener {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_main_logo:
                    Intent intent = new Intent();
                    intent.setClass(Main5InchActivity.this, Setting5InchActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_cm_confirm:
                    mainDataBean.isTest.set(false);
                    calibrationValue = mainDataBean.cmCalibrationValue.get();
                    if (temperatureUnit == 2) {
                        calibrationValue = BigDecimalUtils.div(calibrationValue, 1.8f, 1).floatValue();
                    }
                    SpUtils.saveFloat(Temper5InchConst.Key.CALIBRATION_VALUE, calibrationValue);
                    TemperatureModule.getIns().setmCorrectionValue(calibrationValue);
                    T.showShort(Main5InchActivity.this, getResString(R.string.cm_save_tip));
                    break;
                case R.id.btn_cm_cancel:
                    mainDataBean.isTest.set(false);
                    TemperatureModule.getIns().setmCorrectionValue(calibrationValue);
                    break;
                case R.id.fl_body_add_btn:
                    float addBodyTemperature = mainDataBean.cmBodyTemperatureValue.get();
                    addBodyTemperature = BigDecimalUtils.add(addBodyTemperature, 0.1f, 1).floatValue();
                    mainDataBean.cmBodyTemperatureValue.set(addBodyTemperature);
                    cmSetCalibrationValue();
                    break;
                case R.id.fl_body_sub_btn:
                    float subBodyTemperature = mainDataBean.cmBodyTemperatureValue.get();
                    subBodyTemperature = BigDecimalUtils.sub(subBodyTemperature, 0.1f, 1).floatValue();
                    mainDataBean.cmBodyTemperatureValue.set(subBodyTemperature);
                    cmSetCalibrationValue();
                    break;
            }
        }

        public boolean onLongClick(View v) {
            switch (v.getId()) {
                case R.id.ll_temperature_container:
                    if (mainDataBean.isTest.get()) {
                        mainDataBean.isTest.set(false);
                    } else {
                        mainDataBean.isTest.set(true);
                        TemperatureModule.getIns().setmCorrectionValue(0);
                        mainDataBean.cmMeasurementValue.set(0f);
                        if (temperatureUnit == 1) {
                            mainDataBean.cmCalibrationValue.set(calibrationValue);
                            mainDataBean.cmBodyTemperatureValue.set(Temper5InchConst.defaultBodyValue);
                        } else {
                            mainDataBean.cmCalibrationValue.set(BigDecimalUtils.mul(calibrationValue, 1.8f, 1).floatValue());
                            mainDataBean.cmBodyTemperatureValue.set(TemperatureUnitUtils.c2f(Temper5InchConst.defaultBodyValue));
                        }
                    }
                    break;
            }
            return false;
        }
    }

    private long firstTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                T.showShort(this, getResString(R.string.click_to_exit_tip));
                firstTime = secondTime;
                return true;
            } else {
                APP.exit();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void startXmpp() {//开启xmpp
        serviceManager = new ServiceManager(this);
        serviceManager.startService();
    }

    private void destroyXmpp() {
        if (serviceManager != null) {
            serviceManager.stopService();
            serviceManager = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainHandler.sendEmptyMessage(2);
        isStop = false;
        playVideoSwitch(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isStop = true;
        playVideoSwitch(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        releaseResources();
        setFullscreen(true, true);
        MyManager.getInstance(this).setSlideShowNotificationBar(true);
        MyManager.getInstance(this).hideNavBar(false);
    }

    private void releaseResources() {
        TemperatureModule.getIns().closeSerialPort();
        SpeechManager.getInstance().destroy();
        activity5inchMainBinding.cvMainCamera.destroy();
        destroyXmpp();
        LocateManager.instance().destory();
    }
}
