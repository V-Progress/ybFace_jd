package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.os.Bundle;
import android.view.View;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.heartbeat.BaseGateActivity;

public class TestActivity extends BaseGateActivity {

    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }

    public void addSign(View view){
    }
}


