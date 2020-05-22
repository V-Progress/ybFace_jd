package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.yunbiao.ybsmartcheckin_live_id.BR;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

public class SpeechContentActivity extends Activity {
    private static final String TAG = "SpeechContentActivity";

    private SpeechContent speechContent = new SpeechContent();
    private ActionPresenter actionPresenter = new ActionPresenter();
    private ViewDataBinding viewDataBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_speech_content);
        viewDataBinding.setVariable(BR.speechContent, speechContent);
        viewDataBinding.setVariable(BR.actionPresenter, actionPresenter);
        speechContent.init(this);
    }

    public class ActionPresenter {
        public void onBackButtonClick(View view){
            finish();
        }
        public void onSpeedButtonClick(View view){
            EditText edtSpeed = viewDataBinding.getRoot().findViewById(R.id.edt_speed_setting);
            float speed = Float.parseFloat(edtSpeed.getText().toString());
            if (view.getId() == R.id.btn_speed_sub_setting) {
                speed -= 0.1f;
            } else {
                speed += 0.1f;
            }
            speed = formatF(speed);
            speechContent.setSpeechSpeed(speed);
        }

        public void onSpeedChanged(CharSequence s, int start, int before, int count){
            float speed = Float.parseFloat(s.toString());
            speechContent.setSpeechSpeed(speed);
        }

        //欢迎语
        public void onWelcomTextChanged(CharSequence s, int start, int before, int count){
            String content = s.toString();
            speechContent.setWelcomeText(content);
            speechContent.setWelcomeTextExample();
        }
        //欢迎语开关
        public void onWelcomeEnableChanged(CompoundButton buttonView, boolean isChecked){
            speechContent.setWelcomeTextEnabled(isChecked);
            speechContent.setWelcomeTextExample();
        }
        //口罩提示
        public void onMaskTipChanged(CharSequence s, int start, int before, int count){
            speechContent.setMaskContent(s.toString());
            speechContent.setMaskExample();
        }
        //正常提示温度位置
        public void onMaskEnabledChanged(CompoundButton buttonView, boolean isChecked){
            speechContent.setMaskEnabled(isChecked);
            speechContent.setMaskExample();
        }
        //距离提示
        public void onDistanceTextChanged(CharSequence s, int start, int before, int count) {
            String content = s.toString();
            speechContent.setDistanceTip(content);
            speechContent.setDistanceTipExample();
        }
        //距离提示开关
        public void onDistanceEnableChanged(CompoundButton buttonView, boolean isChecked) {
            speechContent.setDistanceTipEnabled(isChecked);
            speechContent.setDistanceTipExample();
        }
        //对框提示
        public void onFrameTextChanged(CharSequence s, int start, int before, int count) {
            String content = s.toString();
            speechContent.setFrameTip(content);
            speechContent.setFrameTipExample();
        }
        //对框提示开关
        public void onFrameEnableChanged(CompoundButton buttonView, boolean isChecked) {
            speechContent.setFrameTipEnabled(isChecked);
            speechContent.setFrameTipExample();
        }
        //正常提示
        public void onNormalTextChanged(CharSequence s, int start, int before, int count) {
            String content = s.toString();
            speechContent.setNormalContent(content);
            speechContent.setNormalExample();
        }
        //正常提示温度开关
        public void onNormalTemperEnableChanged(CompoundButton buttonView, boolean isChecked) {
            speechContent.setNormalShow(isChecked);
            speechContent.setNormalExample();
        }
        public void onNormalEnableChanged(CompoundButton buttonView, boolean isChecked){
            Log.e(TAG, "onNormalEnableChanged: ---------" + isChecked);
            speechContent.setNormalEnabled(isChecked);
            speechContent.setNormalExample();
        }
        public void onWarningEnableChanged(CompoundButton buttonView, boolean isChecked){
            Log.e(TAG, "onWarningEnableChanged: ---------" + isChecked);
            speechContent.setWarningEnabled(isChecked);
            speechContent.setWarningExample();
        }
        //异常提示
        public void onWarningTextChanged(CharSequence s, int start, int before, int count) {
            String content = s.toString();
            speechContent.setWarningContent(content);
            speechContent.setWarningExample();
        }
        //异常提示温度开关
        public void onWarningTemperEnableChanged(CompoundButton buttonView, boolean isChecked) {
            speechContent.setWarningShow(isChecked);
            speechContent.setWarningExample();
        }
        //正常提示温度位置
        public void onNormalLocationCheckedChanged(RadioGroup group, int checkedId){
            int location = checkedId == R.id.rb_start_normal ? 0 : checkedId == R.id.rb_center_normal ? 1 : 2;
            speechContent.setNormalTemperLocation(location);
            speechContent.setNormalExample();
        }
        //异常提示温度位置
        public void onWarningLocationCheckedChanged(RadioGroup group, int checkedId){
            int location = checkedId == R.id.rb_start_warning ? 0 : checkedId == R.id.rb_center_warning ? 1 : 2;
            speechContent.setWarningTemperLocation(location);
            speechContent.setWarningExample();
        }
        //摄氏度单位更改
        public void onCentigradeTextChanged(CharSequence s, int start, int before, int count){
            speechContent.setCentigrade(s.toString());
        }
        //华氏度单位更改
        public void onFahrenheitTextChanged(CharSequence s, int start, int before, int count){
            String content = s.toString();
            speechContent.setFahrenheit(content);
        }

        //欢迎语试听
        public void onWelcomePlay(View view){
            KDXFSpeechManager.instance().playNormalForSpeed(speechContent.getWelcomeTextExample(),speechContent.getSpeechSpeed());
        }
        //靠近试听
        public void onClosePlay(View view){
            KDXFSpeechManager.instance().playNormalForSpeed(speechContent.getDistanceTipExample(),speechContent.getSpeechSpeed());
        }
        //对框试听
        public void onFramePlay(View view){
            KDXFSpeechManager.instance().playNormalForSpeed(speechContent.getFrameTipExample(),speechContent.getSpeechSpeed());
        }
        //正常试听
        public void onNormalPlay(View view){
            String speech = speechContent.getNormalExample().replace("℃",speechContent.getCentigrade());
            KDXFSpeechManager.instance().playNormalForSpeed(speech,speechContent.getSpeechSpeed());
        }
        //异常试听
        public void onWarningPlay(View view){
            String speech = speechContent.getWarningExample().replace("℃",speechContent.getCentigrade());
            KDXFSpeechManager.instance().playNormalForSpeed(speech,speechContent.getSpeechSpeed());
        }
        public void onMaskPlay(View view){
            String speech = speechContent.getMaskTipExample();
            KDXFSpeechManager.instance().playNormalForSpeed(speech,speechContent.getSpeechSpeed());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        speechContent.save();
    }

    private float formatF(float fValue) {
        return (float) (Math.round(fValue * 10)) / 10;
    }
}
