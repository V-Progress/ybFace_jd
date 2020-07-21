package com.yunbiao.ybsmartcheckin_live_id.activity;

import androidx.core.content.ContextCompat;

import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.common.power.PowerOffTool;
import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.ThreadUitls;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PowerOnOffActivity extends BaseActivity {
    private static final String TAG = "SMTPowerOnOffActivity";

    private List<CheckBox> onBoxs=new ArrayList<>();

    private TextView tvOn;
    private TextView tvOff;

    private  int onHTime=9;
    private int onMTime=0;
    private int offHTime=20;
    private int offMTime=0;


    private List<Integer> onWeek;
    private List<Integer> offWeek;

    private Switch sw_open;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_power_on_off;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_power_on_off;
    }

    private String switchDay(String i){
        String day = "";
        switch (i) {
            case "1":
                day = getResources().getString(R.string.act_power_monday);
                break;
            case "2":
                day = getResources().getString(R.string.act_power_tuesday);
                break;
            case "3":
                day = getResources().getString(R.string.act_power_wednesday);
                break;
            case "4":
                day = getResources().getString(R.string.act_power_thursday);
                break;
            case "5":
                day = getResources().getString(R.string.act_power_friday);
                break;
            case "6":
                day = getResources().getString(R.string.act_power_saturday);
                break;
            case "7":
                day = getResources().getString(R.string.act_power_sunday);
                break;
            default:
                day = i;
                break;
        }
        return day;
    }

    private String getPowerString(String powerParams){
        StringBuffer stringBuffer = new StringBuffer();
        String[] split = powerParams.split(",");
        for (int i = 0; i < split.length; i++) {
            String day = split[i];
            if(TextUtils.isEmpty(day)){
                continue;
            }
            String strDay = switchDay(day);
            if(TextUtils.isEmpty(strDay)){
                continue;
            }
            if(!strDay.contains(":")){
                stringBuffer.append(strDay).append(",");
                continue;
            }

            if(strDay.contains(";")){
                strDay = strDay.replace(";", "");
            }
            stringBuffer.append(strDay);
        }
        return stringBuffer.toString();
    }

    @Override
    protected void initView() {
        ImageView ivBack = findViewById(R.id.iv_back_power);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (CommonUtils.getBroadType() == 4) {
            findViewById(R.id.tv_power_time_tips_ys).setVisibility(View.VISIBLE);
        }

        String powerOnParams = PowerOffTool.getPowerOffTool().getPowerParam(PowerOffTool.POWER_ON);
        String powerOffParams = PowerOffTool.getPowerOffTool().getPowerParam(PowerOffTool.POWER_OFF);
        tvOn = findViewById(R.id.tv_on_params);
        tvOff = findViewById(R.id.tv_off_params);
        setOnOffTv();

        String powerOnDate[] = null;
        String powerOnTime[] = null;
        if (!TextUtils.isEmpty(powerOnParams)) {
            powerOnDate = powerOnParams.split(";")[0].split(",");
            powerOnTime = powerOnParams.split(";")[1].split(":");
        }
        String powerOffDate[] = null;
        String powerOffTime[] = null;
        if (!TextUtils.isEmpty(powerOffParams)) {
            powerOffDate = powerOffParams.split(";")[0].split(",");
            powerOffTime = powerOffParams.split(";")[1].split(":");
        }

        NumberPicker onHPicker = findViewById(R.id.number_power_on_h_smt);
        onHPicker.setMaxValue(23);
        onHPicker.setMinValue(0);
        if (powerOnTime != null && powerOnTime.length == 2) {
            onHTime = Integer.parseInt(powerOnTime[0]);
        } else {
            onHTime = 9;
        }
        onHPicker.setValue(onHTime);
        onHPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Log.e(TAG, "onValueChange: " + oldVal + " ----- " + newVal);
                onHTime=newVal;
            }
        });

        NumberPicker onMPicker = findViewById(R.id.number_power_on_m_smt);
        onMPicker.setMaxValue(59);
        onMPicker.setMinValue(0);
        if (powerOnTime != null && powerOnTime.length == 2) {
            onMTime = Integer.parseInt(powerOnTime[1]);
        } else {
            onMTime = 0;
        }
        onMPicker.setValue(onMTime);

        onMPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Log.e(TAG, "onValueChange: " + oldVal + " ----- " + newVal);
                onMTime=newVal;
            }
        });

        NumberPicker offHPicker = findViewById(R.id.number_power_off_h_smt);
        offHPicker.setMaxValue(23);
        offHPicker.setMinValue(0);
        if (powerOffTime != null && powerOffTime.length == 2) {
            offHTime = Integer.parseInt(powerOffTime[0]);
        } else {
            offHTime = 20;
        }
        offHPicker.setValue(offHTime);
        offHPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Log.e(TAG, "onValueChange: " + oldVal + " ----- " + newVal);
                offHTime=newVal;
            }
        });

        NumberPicker offMPicker = findViewById(R.id.number_power_off_m_smt);
        offMPicker.setMaxValue(59);
        offMPicker.setMinValue(0);
        if (powerOffTime != null && powerOffTime.length == 2) {
            offMTime = Integer.parseInt(powerOffTime[1]);
        } else {
            offMTime = 0;
        }
        offMPicker.setValue(offMTime);
        offMPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Log.e(TAG, "onValueChange: " + oldVal + " ----- " + newVal);
                offMTime=newVal;
            }
        });



        CheckBox cb_on_1=findViewById(R.id.cb_on_1);
        CheckBox cb_on_2=findViewById(R.id.cb_on_2);
        CheckBox cb_on_3=findViewById(R.id.cb_on_3);
        CheckBox cb_on_4=findViewById(R.id.cb_on_4);
        CheckBox cb_on_5=findViewById(R.id.cb_on_5);
        CheckBox cb_on_6=findViewById(R.id.cb_on_6);
        CheckBox cb_on_7=findViewById(R.id.cb_on_7);
        CheckBox cb_on_all=findViewById(R.id.cb_on_all);

        onBoxs.add(cb_on_1);
        onBoxs.add(cb_on_2);
        onBoxs.add(cb_on_3);
        onBoxs.add(cb_on_4);
        onBoxs.add(cb_on_5);
        onBoxs.add(cb_on_6);
        onBoxs.add(cb_on_7);

        if (powerOnDate != null && powerOnDate.length > 0) {
            for (int i = 0; i < onBoxs.size(); i++) {
                onBoxs.get(i).setChecked(false);
            }
            for (int i = 0; i < powerOnDate.length; i++) {
                onBoxs.get(Integer.parseInt(powerOnDate[i]) - 1).setChecked(true);
            }
            if (powerOnDate.length == 7) {
                cb_on_all.setChecked(true);
            } else {
                cb_on_all.setChecked(false);
            }
        }

        cb_on_all.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){

                    for (int i = 0; i < onBoxs.size(); i++) {
                        onBoxs.get(i).setChecked(true);
                    }
                }else {
                    for (int i = 0; i < onBoxs.size(); i++) {
                        onBoxs.get(i).setChecked(false);
                    }
                }
            }
        });

        sw_open = findViewById(R.id.sw_open);
        sw_open.setChecked(SpUtils.getBoolean(SpUtils.POWER_ON_OFF_SWITCH, SpUtils.powerOnOffDef));
        sw_open.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String powerOnParams = PowerOffTool.getPowerOffTool().getPowerParam(PowerOffTool.POWER_ON);
                    String powerOffParams = PowerOffTool.getPowerOffTool().getPowerParam(PowerOffTool.POWER_OFF);
                    if (TextUtils.isEmpty(powerOnParams) || TextUtils.isEmpty(powerOffParams)) {
                        sw_open.setChecked(false);
                        return;
                    }
                    SpUtils.saveBoolean(SpUtils.POWER_ON_OFF_SWITCH, true);
                    PowerOffTool.getPowerOffTool().setPowerRestartTime();
                } else {
                    SpUtils.saveBoolean(SpUtils.POWER_ON_OFF_SWITCH, false);
                    PowerOffTool.getPowerOffTool().cancelPowerRestartTime();
                }
            }
        });
    }

    private void setOnOffTv() {
        String powerOnParams = PowerOffTool.getPowerOffTool().getPowerParam(PowerOffTool.POWER_ON);
        String powerOffParams = PowerOffTool.getPowerOffTool().getPowerParam(PowerOffTool.POWER_OFF);
        tvOn = findViewById(R.id.tv_on_params);
        tvOff = findViewById(R.id.tv_off_params);
        if(TextUtils.isEmpty(powerOnParams)){
            tvOn.setText(getResources().getString(R.string.act_power_power_on_strategy2) + "：" + getResources().getString(R.string.act_power_none));
        } else {
            tvOn.setText(getResources().getString(R.string.act_power_power_on_strategy2) + "：" + getPowerString(powerOnParams));
        }
        if(TextUtils.isEmpty(powerOffParams)){
            tvOff.setText(getResources().getString(R.string.act_power_power_off_strategy2) + "：" + getResources().getString(R.string.act_power_none));
        } else {
            tvOff.setText(getResources().getString(R.string.act_power_power_off_strategy2) + "：" + getPowerString(powerOffParams));
        }
    }

    @Override
    protected void initData() {
    }

    private void setNumberPickerDivider(NumberPicker numberPicker) {
        NumberPicker picker = numberPicker;
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {  //设置颜色
                pf.setAccessible(true);
                ColorDrawable colorDrawable = new ColorDrawable(
                        ContextCompat.getColor(this, R.color.white)); //选择自己喜欢的颜色
                try {
                    pf.set(numberPicker, colorDrawable);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            if (pf.getName().equals("mSelectionDividerHeight")) {   //设置高度
                pf.setAccessible(true);
                try {
                    int result = 3;  //要设置的高度
                    pf.set(picker, result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            picker.invalidate();
        }
    }

    public void savePower(View view) {
        boolean onChecked = false;
        for (CheckBox onBox : onBoxs) {
            if(onBox.isChecked()){
                onChecked = true;
                break;
            }
        }

        if(!onChecked){
            UIUtils.showShort(this,getResString(R.string.act_power_set_failed_not_check_on));
            return;
        }

        onWeek=new ArrayList<>();
        offWeek=new ArrayList<>();
        //添加 周期
        for (int i = 0; i < onBoxs.size(); i++) {
            if (onBoxs.get(i).isChecked()){
                int tag=Integer.parseInt((String) onBoxs.get(i).getTag());
                onWeek.add(tag);
                offWeek.add(tag);
            }
        }
        PowerBean onBean=new PowerBean();
        onBean.setStatus(0);
        onBean.setRunType(0);
        String onRundate="";
        for (int i = 0; i <onWeek.size() ; i++) {
            onRundate= onRundate+onWeek.get(i)+"," ;
        }
        if (onWeek.size()>6){//每一天就是all
            onBean.setRunDate("all:"+onRundate);
        }else {//少一天就是 user
            onBean.setRunDate("user:"+onRundate);
        }
        onBean.setRunTime(onHTime+":"+onMTime);

        PowerBean offBean=new PowerBean();
        offBean.setStatus(0);
        offBean.setRunType(1);
        String offRundate="";
        for (int i = 0; i <offWeek.size() ; i++) {
            offRundate= offRundate+offWeek.get(i)+"," ;
        }

        if (offWeek.size()>6){
            offBean.setRunDate("all:"+offRundate);
        }else {
            offBean.setRunDate("user:"+offRundate);
        }
        offBean.setRunTime(offHTime+":"+offMTime);

        Log.e(TAG, "sonBean------>"+onBean );
        Log.e(TAG, "offBean------>"+offBean );

        List<PowerBean> powerBeanList=new ArrayList<>();
        powerBeanList.add(onBean);
        powerBeanList.add(offBean);


       final String powerJson=new Gson().toJson(powerBeanList);
        Log.e(TAG, "savePower: 设置开关机的规则：" + powerJson);

        SpUtils.saveBoolean(SpUtils.POWER_ON_OFF_SWITCH, true);
        sw_open.setChecked(true);
        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {// 开关机时间设置
                PowerOffTool.getPowerOffTool().putParam(powerJson);
            }
        });
        UIUtils.showShort(this,getResources().getString(R.string.act_power_set_success));
        setOnOffTv();
    }

    public class PowerBean{

//        {"deviceId":"ffffffff-c3b8-24cd-0000-00002cff158d",
//                "id":10,
//                "key":"user:1,2,3,4,6,7,_10:18_1_0",
//                "runDate":"user:1,2,3,4,6,7,",
//                "runTime":"10:18",
//                "runType":1,
//                "status":0
//        }

        private  String deviceId;
        private  String runDate;//开关机周期
        private  String runTime;//开关机时间
        private  int runType;//0是开机  1是关机
        private  int status;//0是成功


        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getRunDate() {
            return runDate;
        }

        public void setRunDate(String runDate) {
            this.runDate = runDate;
        }

        public String getRunTime() {
            return runTime;
        }

        public void setRunTime(String runTime) {
            this.runTime = runTime;
        }

        public int getRunType() {
            return runType;
        }

        public void setRunType(int runType) {
            this.runType = runType;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return "PowerBean{" +
                    "runDate='" + runDate + '\'' +
                    ", runTime='" + runTime + '\'' +
                    ", runType=" + runType +
                    ", status=" + status +
                    '}';
        }
    }
}
