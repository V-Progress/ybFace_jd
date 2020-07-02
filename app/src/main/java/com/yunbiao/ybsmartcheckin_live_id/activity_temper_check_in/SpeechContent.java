package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;


import android.content.Context;
import android.text.TextUtils;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.yunbiao.ybsmartcheckin_live_id.BR;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

public class SpeechContent extends BaseObservable {
    private String exampleNormalTemper = "36.5℃";
    private String exampleWarningTemper = "37.5℃";
    private float speechSpeed;

    private long tipDelay;

    private String welcomeText;
    private boolean welcomeTextEnabled;

    private boolean isShowDetaile = true;

    private String maskContent;
    private boolean maskEnabled;

    private String distanceTip;
    private boolean distanceTipEnabled;
    private String frameTip;
    private boolean frameTipEnabled;

    private String normalContent;
    private boolean normalShow;
    private int normalTemperLocation;
    private boolean normalEnabled;

    private String warningContent;
    private boolean warningShow;
    private int warningTemperLocation;
    private boolean warningEnabled;

    private String centigrade;
    private String fahrenheit;

    private String welcomeTextExample;
    private String maskTipExample;
    private String distanceTipExample;
    private String frameTipExample;
    private String normalExample;
    private String warningExample;
    private LastStatus lastStatus;

    public void init(Context context) {
        isShowDetaile = Constants.DEVICE_TYPE == Constants.DeviceType.TEMPERATURE_CHECK_IN || Constants.DEVICE_TYPE == Constants.DeviceType.HT_TEMPERATURE_CHECK_IN;

        speechSpeed = SpUtils.getFloat(ThermalConst.Key.VOICE_SPEED, ThermalConst.Default.VOICE_SPEED);

        tipDelay = SpUtils.getLong(ThermalConst.Key.TIP_DELAY,ThermalConst.Default.TIP_DELAY);

        welcomeText = SpUtils.getStr(ThermalConst.Key.WELCOME_TIP_CONTENT, ThermalConst.Default.WELCOME_TIP_CONTENT);
        welcomeTextEnabled = SpUtils.getBoolean(ThermalConst.Key.WELCOME_TIP_ENABLED, ThermalConst.Default.WELCOME_TIP_ENABLED);

        maskContent = SpUtils.getStr(ThermalConst.Key.MASK_TIP, context.getResources().getString(R.string.no_mask_tip));
        maskEnabled = SpUtils.getBoolean(ThermalConst.Key.MASK_DETECT_ENABLED, ThermalConst.Default.MASK_DETECT_ENABLED);

        distanceTip = SpUtils.getStr(ThermalConst.Key.DISTANCE_TIP_CONTENT, context.getResources().getString(R.string.main_tips_please_close));
        distanceTipEnabled = SpUtils.getBoolean(ThermalConst.Key.DISTANCE_TIP_ENABLED, ThermalConst.Default.DISTANCE_TIP_ENABLED);

        frameTip = SpUtils.getStr(ThermalConst.Key.FRAME_TIP_CONTENT,context.getResources().getString(R.string.main_temp_tips_please_in_range));
        frameTipEnabled = SpUtils.getBoolean(ThermalConst.Key.FRAME_TIP_ENABLED, ThermalConst.Default.FRAME_TIP_ENABLED);

        normalContent = SpUtils.getStr(ThermalConst.Key.NORMAL_BROADCAST,  context.getResources().getString(R.string.main_temp_normal_tips));
        normalShow = SpUtils.getBoolean(ThermalConst.Key.NORMAL_TEMPER_SHOW, ThermalConst.Default.NORMAL_TEMPER_SHOW);
        normalEnabled = SpUtils.getBoolean(ThermalConst.Key.NORMAL_BROADCAST_ENABLED, ThermalConst.Default.NORMAL_BROADCAST_ENABLED);
        normalTemperLocation = SpUtils.getIntOrDef(ThermalConst.Key.NORMAL_TEMPER_LOCATION, ThermalConst.Default.NORMAL_TEMPER_LOCATION);

        warningContent = SpUtils.getStr(ThermalConst.Key.WARNING_BROADCAST, context.getResources().getString(R.string.main_temp_warning_tips));
        warningShow = SpUtils.getBoolean(ThermalConst.Key.WARNING_TEMPER_SHOW,  ThermalConst.Default.WARNING_TEMPER_SHOW);
        warningEnabled = SpUtils.getBoolean(ThermalConst.Key.WARNING_BROAD_ENABLED, ThermalConst.Default.WARNING_BROAD_ENABLED);
        warningTemperLocation = SpUtils.getIntOrDef(ThermalConst.Key.WARNING_TEMPER_LOCATION, ThermalConst.Default.WARNING_TEMPER_LOCATION);

        centigrade = SpUtils.getStr(ThermalConst.Key.CENTIGRADE, context.getResources().getString(R.string.temper_tips_centigrade));
        fahrenheit = SpUtils.getStr(ThermalConst.Key.FAHRENHEIT, context.getResources().getString(R.string.temper_tips_fahrenheit));

        lastStatus = new LastStatus(
                speechSpeed,
                tipDelay,
                maskContent,maskEnabled,
                welcomeText,welcomeTextEnabled,
                distanceTip,distanceTipEnabled,
                frameTip,frameTipEnabled,
                normalContent,normalShow,normalEnabled,normalTemperLocation,
                warningContent,warningShow,warningEnabled,warningTemperLocation,
                centigrade,fahrenheit);

        setWelcomeTextExample();
        setDistanceTipExample();
        setFrameTipExample();
        setNormalExample();
        setWarningExample();

        notifyChange();
    }

    @Bindable
    public boolean isShowDetaile() {
        return isShowDetaile;
    }

    public void setShowDetaile(boolean showDetaile) {
        isShowDetaile = showDetaile;
        notifyPropertyChanged(BR.showDetaile);
    }

    @Bindable
    public String getMaskTipExample() {
        return maskTipExample;
    }

    @Bindable
    public String getMaskContent() {
        return maskContent;
    }

    public void setMaskContent(String maskContent) {
        this.maskContent = maskContent;
        notifyPropertyChanged(BR.maskContent);
    }

    @Bindable
    public boolean isMaskEnabled() {
        return maskEnabled;
    }

    public void setMaskEnabled(boolean maskEnabled) {
        this.maskEnabled = maskEnabled;
        notifyPropertyChanged(BR.maskEnabled);
    }

    public void save() {
        //速度
        if(speechSpeed != lastStatus.speechSpeed){
            SpUtils.saveFloat(ThermalConst.Key.VOICE_SPEED, speechSpeed);
        }
        if(tipDelay != lastStatus.tipDelay){
            SpUtils.saveLong(ThermalConst.Key.TIP_DELAY,tipDelay);
        }
        //欢迎语
        if(!TextUtils.equals(lastStatus.welcomeText,welcomeText)){
            if (TextUtils.isEmpty(welcomeText)) {
                SpUtils.remove(ThermalConst.Key.WELCOME_TIP_CONTENT);
            } else {
                SpUtils.saveStr(ThermalConst.Key.WELCOME_TIP_CONTENT, welcomeText);
            }
        }
        //欢迎语开关
        if(welcomeTextEnabled != lastStatus.welcomeTextEnabled){
            SpUtils.saveBoolean(ThermalConst.Key.WELCOME_TIP_ENABLED, welcomeTextEnabled);
        }

        /*//口罩提示
        if (!TextUtils.equals(aDefault.getMaskTip(), maskContent)) {
            if (TextUtils.isEmpty(maskContent)) {
                SpUtils.remove(ThermalConst.Key.MASK_TIP);
            } else {
                SpUtils.saveStr(ThermalConst.Key.MASK_TIP, maskContent);
            }
        }
        //口罩提示开关
        SpUtils.saveBoolean(ThermalConst.Key.MASK_TIP_ENABLED, maskEnabled);
*/
        //距离提示
        if(!TextUtils.equals(lastStatus.distanceTip,distanceTip)){
            if (TextUtils.isEmpty(distanceTip)) {
                SpUtils.remove(ThermalConst.Key.DISTANCE_TIP_CONTENT);
            } else {
                SpUtils.saveStr(ThermalConst.Key.DISTANCE_TIP_CONTENT, distanceTip);
            }
        }
        //距离提示开关
        if(distanceTipEnabled != lastStatus.distanceTipEnbaled){
            SpUtils.saveBoolean(ThermalConst.Key.DISTANCE_TIP_ENABLED, distanceTipEnabled);
        }
        //对框提示
        if(!TextUtils.equals(lastStatus.frameTip,frameTip)){
            if (TextUtils.isEmpty(frameTip)) {
                SpUtils.remove(ThermalConst.Key.FRAME_TIP_CONTENT);
            } else {
                SpUtils.saveStr(ThermalConst.Key.FRAME_TIP_CONTENT, frameTip);
            }
        }
        //对框开关
        if(frameTipEnabled != lastStatus.frameTipEnabled){
            SpUtils.saveBoolean(ThermalConst.Key.FRAME_TIP_ENABLED, frameTipEnabled);
        }
        //正常提示
        if(!TextUtils.equals(lastStatus.normalContent,normalContent)){
            if (TextUtils.isEmpty(normalContent)) {
                SpUtils.remove(ThermalConst.Key.NORMAL_BROADCAST);
            } else {
                SpUtils.saveStr(ThermalConst.Key.NORMAL_BROADCAST, normalContent);
            }
        }
        //正常提示开关
        if(normalShow != lastStatus.normalShow){
            SpUtils.saveBoolean(ThermalConst.Key.NORMAL_TEMPER_SHOW, normalShow);
        }
        //温度显示
        if(normalEnabled != lastStatus.normalEnabled){
            SpUtils.saveBoolean(ThermalConst.Key.NORMAL_BROADCAST_ENABLED, normalEnabled);
        }
        //异常提示
        if(!TextUtils.equals(lastStatus.warningContent,warningContent)){
            if (TextUtils.isEmpty(warningContent)) {
                SpUtils.remove(ThermalConst.Key.WARNING_BROADCAST);
            } else {
                SpUtils.saveStr(ThermalConst.Key.WARNING_BROADCAST, warningContent);
            }
        }
        //异常温度显示
        if(warningShow != lastStatus.warningShow){
            SpUtils.saveBoolean(ThermalConst.Key.WARNING_TEMPER_SHOW, warningShow);
        }
        //异常开关
        if(warningEnabled != lastStatus.warningEnabled){
            SpUtils.saveBoolean(ThermalConst.Key.WARNING_BROAD_ENABLED, warningEnabled);
        }
        //正常温度位置
        if(normalTemperLocation != lastStatus.normalTemperLocation){
            SpUtils.saveInt(ThermalConst.Key.NORMAL_TEMPER_LOCATION, normalTemperLocation);
        }
        //异常温度位置
        if(warningTemperLocation != lastStatus.warningTemperLocation){
            SpUtils.saveInt(ThermalConst.Key.WARNING_TEMPER_LOCATION, warningTemperLocation);
        }
        //摄氏度
        if(!TextUtils.equals(lastStatus.centigrade,centigrade)){
            if (TextUtils.isEmpty(centigrade)) {
                SpUtils.remove(ThermalConst.Key.CENTIGRADE);
            } else {
                SpUtils.saveStr(ThermalConst.Key.CENTIGRADE, centigrade);
            }
        }
        //华氏度
        if(!TextUtils.equals(lastStatus.fahrenheit,fahrenheit)){
            if (TextUtils.isEmpty(fahrenheit)) {
                SpUtils.remove(ThermalConst.Key.FAHRENHEIT);
            } else {
                SpUtils.saveStr(ThermalConst.Key.FAHRENHEIT, fahrenheit);
            }
        }
    }

    @Bindable
    public String getCentigrade() {
        return centigrade;
    }

    public void setCentigrade(String centigrade) {
        this.centigrade = centigrade;
        notifyPropertyChanged(BR.centigrade);
    }

    @Bindable
    public String getFahrenheit() {
        return fahrenheit;
    }

    public void setFahrenheit(String fahrenheit) {
        this.fahrenheit = fahrenheit;
        notifyPropertyChanged(BR.fahrenheit);
    }

    @Bindable
    public boolean isNormalEnabled() {
        return normalEnabled;
    }

    public void setNormalEnabled(boolean normalEnabled) {
        this.normalEnabled = normalEnabled;
        notifyPropertyChanged(BR.normalEnabled);
    }

    @Bindable
    public boolean isWarningEnabled() {
        return warningEnabled;
    }

    public void setWarningEnabled(boolean warningEnabled) {
        this.warningEnabled = warningEnabled;
        notifyPropertyChanged(BR.warningEnabled);
    }

    @Bindable
    public float getSpeechSpeed() {
        return speechSpeed;
    }

    public void setSpeechSpeed(float speechSpeed) {
        this.speechSpeed = speechSpeed;
        notifyPropertyChanged(BR.speechSpeed);
    }

    @Bindable
    public long getTipDelay() {
        return tipDelay;
    }

    public void setTipDelay(long tipDelay) {
        this.tipDelay = tipDelay;
        notifyPropertyChanged(BR.tipDelay);
    }

    @Bindable
    public boolean isWelcomeTextEnabled() {
        return welcomeTextEnabled;
    }

    public void setWelcomeTextEnabled(boolean welcomeTextEnabled) {
        this.welcomeTextEnabled = welcomeTextEnabled;
        notifyPropertyChanged(BR.welcomeTextEnabled);
    }

    @Bindable
    public String getWelcomeText() {
        return welcomeText;
    }

    public void setWelcomeText(String welcomeText) {
        this.welcomeText = welcomeText;
        notifyPropertyChanged(BR.welcomeText);
    }

    @Bindable
    public String getWelcomeTextExample() {
        return welcomeTextExample;
    }

    public void setWelcomeTextExample() {
        this.welcomeTextExample = welcomeText;
        if (!welcomeTextEnabled) {
            this.welcomeTextExample = "";
        }
        notifyPropertyChanged(BR.welcomeTextExample);
    }

    @Bindable
    public String getDistanceTip() {
        return distanceTip;
    }

    public void setDistanceTip(String distanceTip) {
        this.distanceTip = distanceTip;
        notifyPropertyChanged(BR.distanceTip);
    }

    @Bindable
    public boolean isDistanceTipEnabled() {
        return distanceTipEnabled;
    }

    public void setDistanceTipEnabled(boolean distanceTipEnabled) {
        this.distanceTipEnabled = distanceTipEnabled;
        notifyPropertyChanged(BR.distanceTipEnabled);
    }

    @Bindable
    public String getFrameTip() {
        return frameTip;
    }

    public void setFrameTip(String frameTip) {
        this.frameTip = frameTip;
        notifyPropertyChanged(BR.frameTip);
    }

    @Bindable
    public boolean isFrameTipEnabled() {
        return frameTipEnabled;
    }

    public void setFrameTipEnabled(boolean frameTipEnabled) {
        this.frameTipEnabled = frameTipEnabled;
        notifyPropertyChanged(BR.frameTipEnabled);
    }

    @Bindable
    public String getNormalContent() {
        return normalContent;
    }

    public void setNormalContent(String normalContent) {
        this.normalContent = normalContent;
        notifyPropertyChanged(BR.normalContent);
    }

    @Bindable
    public boolean isNormalShow() {
        return normalShow;
    }

    public void setNormalShow(boolean normalShow) {
        this.normalShow = normalShow;
        notifyPropertyChanged(BR.normalShow);
    }

    @Bindable
    public int getNormalTemperLocation() {
        return normalTemperLocation;
    }

    public void setNormalTemperLocation(int normalTemperLocation) {
        this.normalTemperLocation = normalTemperLocation;
        notifyPropertyChanged(BR.normalTemperLocation);
    }

    @Bindable
    public String getWarningContent() {
        return warningContent;
    }

    public void setWarningContent(String warningContent) {
        this.warningContent = warningContent;
        notifyPropertyChanged(BR.warningContent);
    }

    @Bindable
    public boolean isWarningShow() {
        return warningShow;
    }

    public void setWarningShow(boolean warningShow) {
        this.warningShow = warningShow;
        notifyPropertyChanged(BR.warningShow);
    }

    @Bindable
    public int getWarningTemperLocation() {
        return warningTemperLocation;
    }

    public void setWarningTemperLocation(int warningTemperLocation) {
        this.warningTemperLocation = warningTemperLocation;
        notifyPropertyChanged(BR.warningTemperLocation);
    }

    @Bindable
    public String getDistanceTipExample() {
        return distanceTipExample;
    }

    public void setDistanceTipExample() {
        this.distanceTipExample = distanceTip;
        if (!distanceTipEnabled) {
            distanceTipExample = "";
        }
        notifyPropertyChanged(BR.distanceTipExample);
    }

    @Bindable
    public String getFrameTipExample() {
        return frameTipExample;
    }

    public void setFrameTipExample() {
        this.frameTipExample = frameTip;
        if (!frameTipEnabled) {
            frameTipExample = "";
        }
        notifyPropertyChanged(BR.frameTipExample);
    }

    @Bindable
    public String getNormalExample() {
        return normalExample;
    }

    public void setNormalExample() {
        if (normalEnabled) {
            this.normalExample = this.normalContent;
            if (normalShow) {
                switch (normalTemperLocation) {
                    case 0:
                        normalExample = exampleNormalTemper + normalExample;
                        normalExample = normalExample.replaceAll("#", "");
                        break;
                    case 1:
                        normalExample = normalExample.replaceFirst("#", exampleNormalTemper);
                        normalExample = normalExample.replaceAll("#", "");
                        break;
                    case 2:
                        normalExample = normalExample + exampleNormalTemper;
                        normalExample = normalExample.replaceAll("#", "");
                        break;
                }
            } else {
                normalExample = normalExample.replaceAll("#", "");
            }
        } else {
            this.normalExample = "";
        }

        notifyPropertyChanged(BR.normalExample);
    }

    @Bindable
    public String getWarningExample() {
        return warningExample;
    }

    public void setWarningExample() {
        if (warningEnabled) {
            this.warningExample = this.warningContent;
            if (warningShow) {
                switch (warningTemperLocation) {
                    case 0:
                        warningExample = exampleWarningTemper + warningExample;
                        warningExample = warningExample.replaceAll("#", "");
                        break;
                    case 1:
                        warningExample = warningExample.replaceFirst("#", exampleWarningTemper);
                        warningExample = warningExample.replaceAll("#", "");
                        break;
                    case 2:
                        warningExample = warningExample + exampleWarningTemper;
                        warningExample = warningExample.replaceAll("#", "");
                        break;
                }
            } else {
                warningExample = warningExample.replaceAll("#", "");
            }
        } else {
            this.warningExample = "";
        }
        notifyPropertyChanged(BR.warningExample);
    }

    public void setMaskExample() {
        if(maskEnabled){
            this.maskTipExample = maskContent;
        } else {
            this.maskTipExample = "";
        }
        notifyPropertyChanged(BR.maskTipExample);
    }

     class LastStatus{
        float speechSpeed;
        long tipDelay;
        String maskTip,welcomeText,distanceTip,frameTip,normalContent,warningContent,centigrade,fahrenheit;
        boolean maskEnabled,welcomeTextEnabled,distanceTipEnbaled,frameTipEnabled,normalShow,normalEnabled,warningShow,warningEnabled;
        int warningTemperLocation,normalTemperLocation;

         public LastStatus(float speechSpeed,long tipDelay,
                           String maskTip, boolean maskEnabled,
                           String welcomeText, boolean welcomeTextEnabled,
                           String distanceTip, boolean distanceTipEnbaled,
                           String frameTip, boolean frameTipEnabled,
                           String normalContent, boolean normalShow, boolean normalEnabled, int normalTemperLocation,
                           String warningContent, boolean warningShow, boolean warningEnabled, int warningTemperLocation,
                           String centigrade, String fahrenheit) {
             this.speechSpeed = speechSpeed;
             this.tipDelay = tipDelay;
             this.maskTip = maskTip;
             this.maskEnabled = maskEnabled;
             this.welcomeText = welcomeText;
             this.welcomeTextEnabled = welcomeTextEnabled;
             this.distanceTip = distanceTip;
             this.distanceTipEnbaled = distanceTipEnbaled;
             this.frameTip = frameTip;
             this.frameTipEnabled = frameTipEnabled;
             this.normalContent = normalContent;
             this.normalShow = normalShow;
             this.normalEnabled = normalEnabled;
             this.normalTemperLocation = normalTemperLocation;
             this.warningContent = warningContent;
             this.warningShow = warningShow;
             this.warningEnabled = warningEnabled;
             this.warningTemperLocation = warningTemperLocation;
             this.centigrade = centigrade;
             this.fahrenheit = fahrenheit;
         }
     }
}