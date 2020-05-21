package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;


import android.content.Context;
import android.text.TextUtils;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.yunbiao.ybsmartcheckin_live_id.BR;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

public class SpeechContent extends BaseObservable {
    private String exampleNormalTemper = "36.5℃";
    private String exampleWarningTemper = "37.5℃";
    private float speechSpeed;

    private String welcomeText;
    private boolean welcomeTextEnabled;

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
    private String distanceTipExample;
    private String frameTipExample;
    private String normalExample;
    private String warningExample;

    private Default aDefault = new Default();

    public void init(Context context) {
        aDefault.init(context);
        speechSpeed = SpUtils.getFloat(ThermalConst.Key.VOICE_SPEED, aDefault.getSpeechSpeed());

        welcomeText = SpUtils.getStr(ThermalConst.Key.WELCOME_TIP_CONTENT, aDefault.getWelcomeText());
        welcomeTextEnabled = SpUtils.getBoolean(ThermalConst.Key.WELCOME_TIP_ENABLED, aDefault.isWelcomeTextEnabled());

        distanceTip = SpUtils.getStr(ThermalConst.Key.DISTANCE_TIP_CONTENT, aDefault.getDistanceTip());
        distanceTipEnabled = SpUtils.getBoolean(ThermalConst.Key.DISTANCE_TIP_ENABLED, aDefault.isDistanceTipEnabled());

        frameTip = SpUtils.getStr(ThermalConst.Key.FRAME_TIP_CONTENT, aDefault.getFrameTip());
        frameTipEnabled = SpUtils.getBoolean(ThermalConst.Key.FRAME_TIP_ENABLED, aDefault.isFrameTipEnabled());

        normalContent = SpUtils.getStr(ThermalConst.Key.NORMAL_BROADCAST, aDefault.getNormalContent());
        normalShow = SpUtils.getBoolean(ThermalConst.Key.NORMAL_TEMPER_SHOW, aDefault.isNormalShow());
        normalEnabled = SpUtils.getBoolean(ThermalConst.Key.NORMAL_BROADCAST_ENABLED, aDefault.isNormalEnabled());
        normalTemperLocation = SpUtils.getIntOrDef(ThermalConst.Key.NORMAL_TEMPER_LOCATION, aDefault.getNormalTemperLocation());

        warningContent = SpUtils.getStr(ThermalConst.Key.WARNING_BROADCAST, aDefault.getWarningContent());
        warningShow = SpUtils.getBoolean(ThermalConst.Key.WARNING_TEMPER_SHOW, aDefault.isWarningShow());
        warningEnabled = SpUtils.getBoolean(ThermalConst.Key.WARNING_BROAD_ENABLED, aDefault.isWarningEnabled());
        warningTemperLocation = SpUtils.getIntOrDef(ThermalConst.Key.WARNING_TEMPER_LOCATION, aDefault.getWarningTemperLocation());

        centigrade = SpUtils.getStr(ThermalConst.Key.CENTIGRADE, aDefault.getCentigrade());
        fahrenheit = SpUtils.getStr(ThermalConst.Key.FAHRENHEIT, aDefault.getFahrenheit());

        setWelcomeTextExample();
        setDistanceTipExample();
        setFrameTipExample();
        setNormalExample();
        setWarningExample();

        notifyChange();
    }

    public void save() {
        //速度
        if (speechSpeed != aDefault.getSpeechSpeed()) {
            SpUtils.saveFloat(ThermalConst.Key.VOICE_SPEED, speechSpeed);
        }
        //欢迎语
        if (!TextUtils.equals(aDefault.getWelcomeText(), welcomeText)) {
            if (TextUtils.isEmpty(welcomeText)) {
                SpUtils.remove(ThermalConst.Key.WELCOME_TIP_CONTENT);
            } else {
                SpUtils.saveStr(ThermalConst.Key.WELCOME_TIP_CONTENT, welcomeText);
            }
        }
        //欢迎语开关
        if (welcomeTextEnabled != aDefault.isWelcomeTextEnabled()) {
            SpUtils.saveBoolean(ThermalConst.Key.WELCOME_TIP_ENABLED, welcomeTextEnabled);
        }
        //距离提示
        if (!TextUtils.equals(aDefault.getDistanceTip(), distanceTip)) {
            if (TextUtils.isEmpty(distanceTip)) {
                SpUtils.remove(ThermalConst.Key.DISTANCE_TIP_CONTENT);
            } else {
                SpUtils.saveStr(ThermalConst.Key.DISTANCE_TIP_CONTENT, distanceTip);
            }
        }
        //距离提示开关
        if(distanceTipEnabled != aDefault.isDistanceTipEnabled()){
            SpUtils.saveBoolean(ThermalConst.Key.DISTANCE_TIP_ENABLED, distanceTipEnabled);
        }
        //对框提示
        if(!TextUtils.equals(aDefault.getFrameTip(),frameTip)){
            if (TextUtils.isEmpty(frameTip)) {
                SpUtils.remove(ThermalConst.Key.FRAME_TIP_CONTENT);
            } else {
                SpUtils.saveStr(ThermalConst.Key.FRAME_TIP_CONTENT, frameTip);
            }
        }
        //对框开关
        if(frameTipEnabled != aDefault.isFrameTipEnabled()){
            SpUtils.saveBoolean(ThermalConst.Key.FRAME_TIP_ENABLED, frameTipEnabled);
        }
        //正常提示
        if(!TextUtils.equals(aDefault.getNormalContent(),normalContent)){
            if (TextUtils.isEmpty(normalContent)) {
                SpUtils.remove(ThermalConst.Key.NORMAL_BROADCAST);
            } else {
                SpUtils.saveStr(ThermalConst.Key.NORMAL_BROADCAST, normalContent);
            }
        }
        //正常提示开关
        if(normalShow != aDefault.isNormalShow()){
            SpUtils.saveBoolean(ThermalConst.Key.NORMAL_TEMPER_SHOW, normalShow);
        }
        //温度显示
        if(normalShow == aDefault.isNormalShow()){
            SpUtils.saveBoolean(ThermalConst.Key.NORMAL_BROADCAST_ENABLED, normalEnabled);
        }
        //异常提示
        if(!TextUtils.equals(aDefault.getWarningContent(),warningContent)){
            if (TextUtils.isEmpty(warningContent)) {
                SpUtils.remove(ThermalConst.Key.WARNING_BROADCAST);
            } else {
                SpUtils.saveStr(ThermalConst.Key.WARNING_BROADCAST, warningContent);
            }
        }
        //异常温度显示
        if(warningShow != aDefault.isWarningShow()){
            SpUtils.saveBoolean(ThermalConst.Key.WARNING_TEMPER_SHOW, warningShow);
        }
        //异常开关
        if(warningEnabled != aDefault.isWarningEnabled()){
            SpUtils.saveBoolean(ThermalConst.Key.WARNING_BROAD_ENABLED, warningEnabled);
        }
        //正常温度位置
        if(normalTemperLocation != aDefault.getNormalTemperLocation()){
            SpUtils.saveInt(ThermalConst.Key.NORMAL_TEMPER_LOCATION, normalTemperLocation);
        }
        //异常温度位置
        if(warningTemperLocation != aDefault.getWarningTemperLocation()){
            SpUtils.saveInt(ThermalConst.Key.WARNING_TEMPER_LOCATION, warningTemperLocation);
        }
        //摄氏度
        if(!TextUtils.equals(aDefault.getCentigrade(),centigrade)){
            if (TextUtils.isEmpty(centigrade)) {
                SpUtils.remove(ThermalConst.Key.CENTIGRADE);
            } else {
                SpUtils.saveStr(ThermalConst.Key.CENTIGRADE, centigrade);
            }
        }
        //华氏度
        if(!TextUtils.equals(aDefault.getFahrenheit(),fahrenheit)){
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

    class Default {
        private float speechSpeed;

        private String welcomeText;
        private boolean welcomeTextEnabled;

        private String distanceTip;
        private boolean distanceTipEnabled;

        private String frameTip;
        private boolean frameTipEnabled;

        private String normalContent;
        private boolean normalShow;
        private boolean normalEnabled;
        private int normalTemperLocation;

        private String warningContent;
        private boolean warningShow;
        private int warningTemperLocation;
        private boolean warningEnabled;

        private String centigrade;
        private String fahrenheit;

        public void init(Context context) {
            speechSpeed = ThermalConst.Default.VOICE_SPEED;

            welcomeText = context.getResources().getString(R.string.setting_default_welcome_tip);
            welcomeTextEnabled = ThermalConst.Default.WELCOME_TIP_ENABLED;

            distanceTip = context.getResources().getString(R.string.main_tips_please_close);
            distanceTipEnabled = ThermalConst.Default.DISTANCE_TIP_ENABLED;

            frameTip = context.getResources().getString(R.string.main_temp_tips_please_in_range);
            frameTipEnabled = ThermalConst.Default.FRAME_TIP_ENABLED;

            normalContent = context.getResources().getString(R.string.main_temp_normal_tips);
            normalShow = ThermalConst.Default.NORMAL_TEMPER_SHOW;
            normalEnabled = ThermalConst.Default.NORMAL_BROADCAST_ENABLED;
            normalTemperLocation = ThermalConst.Default.NORMAL_TEMPER_LOCATION;

            warningContent = context.getResources().getString(R.string.main_temp_warning_tips);
            warningShow = ThermalConst.Default.WARNING_TEMPER_SHOW;
            warningEnabled = ThermalConst.Default.WARNING_BROAD_ENABLED;
            warningTemperLocation = ThermalConst.Default.WARNING_TEMPER_LOCATION;

            centigrade = context.getResources().getString(R.string.temper_tips_centigrade);
            fahrenheit = context.getResources().getString(R.string.temper_tips_fahrenheit);
        }

        public float getSpeechSpeed() {
            return speechSpeed;
        }

        public String getWelcomeText() {
            return welcomeText;
        }

        public boolean isWelcomeTextEnabled() {
            return welcomeTextEnabled;
        }

        public String getDistanceTip() {
            return distanceTip;
        }

        public boolean isDistanceTipEnabled() {
            return distanceTipEnabled;
        }

        public String getFrameTip() {
            return frameTip;
        }

        public boolean isFrameTipEnabled() {
            return frameTipEnabled;
        }

        public String getNormalContent() {
            return normalContent;
        }

        public boolean isNormalShow() {
            return normalShow;
        }

        public boolean isNormalEnabled() {
            return normalEnabled;
        }

        public int getNormalTemperLocation() {
            return normalTemperLocation;
        }

        public String getWarningContent() {
            return warningContent;
        }

        public boolean isWarningShow() {
            return warningShow;
        }

        public int getWarningTemperLocation() {
            return warningTemperLocation;
        }

        public boolean isWarningEnabled() {
            return warningEnabled;
        }

        public String getCentigrade() {
            return centigrade;
        }

        public String getFahrenheit() {
            return fahrenheit;
        }
    }
}