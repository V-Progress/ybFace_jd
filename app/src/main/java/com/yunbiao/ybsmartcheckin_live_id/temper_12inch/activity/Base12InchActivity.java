package com.yunbiao.ybsmartcheckin_live_id.temper_12inch.activity;

import com.ys.rkapi.MyManager;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;

public abstract class Base12InchActivity extends BaseActivity {

    @Override
    protected void onResume() {
        super.onResume();
        setFullscreen(false, false);
        MyManager.getInstance(this).setSlideShowNotificationBar(false);
        MyManager.getInstance(this).hideNavBar(true);
    }

}
