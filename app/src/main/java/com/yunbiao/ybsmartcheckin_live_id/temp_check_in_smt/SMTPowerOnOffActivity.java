package com.yunbiao.ybsmartcheckin_live_id.temp_check_in_smt;

import androidx.core.content.ContextCompat;

import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.lang.reflect.Field;
import java.util.Calendar;

public class SMTPowerOnOffActivity extends SMTBaseActivity {
    private static final String TAG = "SMTPowerOnOffActivity";

    @Override
    protected int getLayout() {
        return R.layout.activity_s_m_t_power_on_off;
    }

    @Override
    protected void initView() {
        NumberPicker onHPicker = findViewById(R.id.number_power_on_h_smt);
        onHPicker.setMaxValue(23);
        onHPicker.setMinValue(0);
        onHPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Log.e(TAG, "onValueChange: " + oldVal + " ----- " + newVal);
            }
        });

        NumberPicker onMPicker = findViewById(R.id.number_power_on_m_smt);
        onMPicker.setMaxValue(59);
        onMPicker.setMinValue(0);
        onMPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Log.e(TAG, "onValueChange: " + oldVal + " ----- " + newVal);
            }
        });
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
}
