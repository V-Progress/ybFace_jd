package com.yunbiao.ybsmartcheckin_live_id.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.ThermalBaseActivity;

public class TestActivity extends ThermalBaseActivity {

    private ImageView ivTemp;
    private TextView tvTemp;
    private static final String TAG = "TestActivity";
    @Override
    protected void updateHotImageBitmap(Bitmap bitmap, float bodyMaxT) {
        ivTemp.setImageBitmap(bitmap);
        tvTemp.setText(bodyMaxT + "℃");
    }

    @Override
    protected void onMeasuring() {
        Toast.makeText(this, "正在测温", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onReMeasuring() {
        Toast.makeText(this, "测温失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onMeasureResult(Bitmap hotImageBitmap, float resultTemp, boolean tempOk) {
        Toast.makeText(this, "结果：" + resultTemp + " --- " + tempOk, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "onMeasureResult: " + "结果：" + resultTemp + " --- " + tempOk );
    }

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_test;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_test;
    }

    @Override
    protected void initView() {
        ivTemp = findViewById(R.id.iv_temp);
        tvTemp = findViewById(R.id.tv_temp);
    }
}
