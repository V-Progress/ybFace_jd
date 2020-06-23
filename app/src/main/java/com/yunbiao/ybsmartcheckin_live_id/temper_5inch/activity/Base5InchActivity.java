package com.yunbiao.ybsmartcheckin_live_id.temper_5inch.activity;

import com.ys.rkapi.MyManager;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;

public abstract class Base5InchActivity extends BaseActivity {

    @Override
    protected void onResume() {
        super.onResume();
        setFullscreen(false, false);
        MyManager.getInstance(this).setSlideShowNotificationBar(false);
        MyManager.getInstance(this).hideNavBar(true);
    }
}
