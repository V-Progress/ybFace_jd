package com.yunbiao.ybsmartcheckin_live_id.activity.fragment.data;

import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.bean.PathBean;

public abstract class  LoadListener {
    public void before(){}

    public void loadCacheComplete(){}

    public void noCache(){}

    public void startRequest(){}

    public void requestFailed(){}

    public void requestComplete(){}

    public void loadBefore(){}

    public void loadSingle(IntroLoader.PlayBean bean){}

    public void loadFinish(){}


    public void ads_start(){}
    public void ads_loadSignle(PathBean pathBean){}
    public void ads_loadTime(int time){}
    public void ads_loadFinish(){}
}
