package com.yunbiao.ybsmartcheckin_live_id.temp_check_in_smt;

import android.os.Bundle;
import android.view.View;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseGpioActivity;

public abstract class SMTBaseActivity extends BaseGpioActivity {
    @Override
    protected int getPortraitLayout() {
        return getLayout();
    }

    @Override
    protected int getLandscapeLayout() {
        return getLayout();
    }

    protected abstract int getLayout();
}
