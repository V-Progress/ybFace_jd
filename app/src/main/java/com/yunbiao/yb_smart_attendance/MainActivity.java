package com.yunbiao.yb_smart_attendance;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends BaseActivity {

    @Override
    protected boolean needToRegisterEvents() {
        return true;
    }

    @Override
    public int setLandscapeLayout() {
        return R.layout.activity_main;
    }

    @Override
    public int setPortraitLayout() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {

    }

    @Override
    public void initData() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(CommonData.CommonDataUpdateEvent event){

    }

    @Override
    public void onBackPressed() {

        inputPwd(new Runnable() {
            @Override
            public void run() {
                APP.exit();
            }
        });
    }
}
