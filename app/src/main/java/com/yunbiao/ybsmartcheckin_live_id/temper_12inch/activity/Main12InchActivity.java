package com.yunbiao.ybsmartcheckin_live_id.temper_12inch.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.KeyEvent;
import android.view.View;

import com.intelligence.hardware.temperature.TOF10120Module;
import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.callback.MLX90621YsTempCallBack;
import com.intelligence.hardware.temperature.callback.TOF10120CallBack;
import com.ys.rkapi.MyManager;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.InformationFragment;
import com.yunbiao.ybsmartcheckin_live_id.business.LocateManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.databinding.Activity12inchMainBinding;
import com.yunbiao.ybsmartcheckin_live_id.printer.T;
import com.yunbiao.ybsmartcheckin_live_id.printer.Utils;
import com.yunbiao.ybsmartcheckin_live_id.temper_12inch.Temper12InchConst;
import com.yunbiao.ybsmartcheckin_live_id.temper_12inch.databean.Main12InchDataBean;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.SpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.utils.BigDecimalUtils;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.utils.TemperatureUnitUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.xmpp.ServiceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.databinding.DataBindingUtil;

public class Main12InchActivity extends Base12InchActivity {

    // xmpp推送服务
    private ServiceManager serviceManager;

    private MyManager ysManager;
    private int gp_io1 = 139;

    private Activity12inchMainBinding activity12inchMainBinding;
    private Main12InchDataBean main12InchDataBean;

    public static int temperatureUnit = 1;
    private float calibrationValue = 0;
    private float warningValue = 0;

    private boolean isStop = false;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_12inch_main;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_12inch_main;
    }

    @Override
    protected void initView() {
        activity12inchMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_12inch_main);

        activity12inchMainBinding.setEventListener(new EventListener());
        main12InchDataBean = new Main12InchDataBean();
        activity12inchMainBinding.setMain12InchDataBean(main12InchDataBean);
    }

    @Override
    protected void initData() {
        initGPIO();

        InformationFragment informationFragment = new InformationFragment();
        replaceFragment(R.id.fl_ad, informationFragment);

        SpeechManager.getInstance().init();
        //开启Xmpp
        startXmpp();
        //初始化定位工具
        LocateManager.instance().init(this);

        SyncManager.instance().requestOnlyCompany();

        TemperatureModule.getIns().setHotImageColdMode(false);
        TemperatureModule.getIns().setHotImageHotMode(false, 45.0f);
        TemperatureModule.getIns().setmCorrectionValue(calibrationValue);
        TemperatureModule.getIns().startMLX90621YsI2C(false, 16 * 30, 4 * 40, mlx90621YsTempCallBack);
        TOF10120Module.getIns().initModule(this, "/dev/ttyS3", tof10120CallBack);
    }

    private void initGPIO() {
        ysManager = MyManager.getInstance(this);
        ysManager.upgradeRootPermissionForExport();
        ysManager.exportGpio(gp_io1);
        ysManager.upgradeRootPermissionForGpio(gp_io1);
        ysManager.setGpioDirection(gp_io1, 0);
        ysManager.writeGpioValue(gp_io1, "0");
    }

    private void updateConfiguration() {
        temperatureUnit = SpUtils.getIntOrDef(Temper12InchConst.Key.TEMPERATURE_UNIT, Temper12InchConst.Default.temperatureUnitDef);
        calibrationValue = SpUtils.getFloat(Temper12InchConst.Key.CALIBRATION_VALUE, Temper12InchConst.Default.calibrationValueDef);
        warningValue = SpUtils.getFloat(Temper12InchConst.Key.WARNING_VALUE, Temper12InchConst.Default.warningValueDef);

        TemperatureModule.getIns().setmCorrectionValue(calibrationValue);
    }

    private boolean isCover = false;
    private long coverTime = 0;
    private boolean isReport = false;
    private List<Float> tempValueList = new ArrayList<>();

    private long showResultTime = 0;

    private TOF10120CallBack tof10120CallBack = new TOF10120CallBack() {
        @Override
        public void initResult(int i) { }
        @Override
        public void newestDistance(int i) {
            if (System.currentTimeMillis() - showResultTime >= 3 * 1000 && ysManager.getGpioValue(gp_io1).equals("1")) {
                ysManager.writeGpioValue(gp_io1, "0");
            }
            if (i < 500) {
                coverTime = System.currentTimeMillis();
                if (!isCover) {
                    isCover = true;
                    isReport = true;
                }
            } else if (main12InchDataBean.isShowResult.get()) {
                main12InchDataBean.isShowResult.set(false);
            }
        }
    };

    private MLX90621YsTempCallBack mlx90621YsTempCallBack = new MLX90621YsTempCallBack() {
        @Override
        public void newestHotImageData(Bitmap bitmap, final float originalMaxT, final float afterTreatmentF, final float minT) {
            if (System.currentTimeMillis() - coverTime > 150) {
                isCover = false;
            }
            if (isStop || !isReport) {
                tempValueList.clear();
                return;
            }
            tempValueList.add(afterTreatmentF);
            if (tempValueList.size() < 3) {
                return;
            }
            //开始处理温度数据
            Collections.sort(tempValueList);
            float temp = tempValueList.get(1);
            isReport = false;
            tempValueList.clear();
            if (!isCover) {
                return;
            }
            float tempC = temp;
            String tempUnit = "℃";
            if (temperatureUnit == 2) {
                temp = TemperatureUnitUtils.c2f(temp);
                tempUnit = "℉";
            }
            if (BigDecimalUtils.compare(temp, warningValue)) {
                speechText(temp, false);
                main12InchDataBean.resultBgRes.set(R.mipmap.inch12_abnormal_bg);
                main12InchDataBean.resultContentBgRes.set(R.mipmap.inch12_red_strip);
                main12InchDataBean.resultIconRes.set(R.mipmap.inch12_ban_icon);
                main12InchDataBean.resultTipStr.set("Vous ne pouvez pas passer");
            } else {
                speechText(temp, true);
                main12InchDataBean.resultBgRes.set(R.mipmap.inch12_normal_bg);
                main12InchDataBean.resultContentBgRes.set(R.mipmap.inch12_green_strip);
                main12InchDataBean.resultIconRes.set(R.mipmap.inch12_pass_icon);
                main12InchDataBean.resultTipStr.set("Vous pouvez passer");
                ysManager.writeGpioValue(gp_io1, "1");
            }
            main12InchDataBean.resultTempStr.set("température " + temp + tempUnit);
            main12InchDataBean.isShowResult.set(true);
            showResultTime = System.currentTimeMillis();
        }

        @Override
        public void dataRecoveryFailed() {

        }
    };

    private void speechText(float temperature, boolean isNormal) {
        StringBuffer stringBuffer = new StringBuffer();
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
            if (!isNormal) {
                SpeechManager.getInstance().stopCurrent();
                SpeechManager.getInstance().playWaningRing();
            }
        }
    }

    public class EventListener {
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(Main12InchActivity.this, Setting12InchActivity.class);
            startActivity(intent);
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
        updateConfiguration();
        TemperatureModule.getIns().setMLX90621YsI2CCallBack(mlx90621YsTempCallBack);
        TOF10120Module.getIns().setTOF10120CallBack(tof10120CallBack);
        isStop = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isStop = true;
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
        TemperatureModule.getIns().closeMLX90621YsI2C();
        TOF10120Module.getIns().closeModule();
        SpeechManager.getInstance().destroy();
        destroyXmpp();
        LocateManager.instance().destory();
    }
}
