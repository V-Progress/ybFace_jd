package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in_smt;

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
