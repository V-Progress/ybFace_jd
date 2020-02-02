package com.yunbiao.ybsmartcheckin_live_id.activity.fragment.data;

public abstract class  LoadListener {
    /***
     * 开始加载
     */
    public void onStart(){}

    /***
     * 加载失败
     * @param e
     */
    public void onFailed(Exception e){}

    public void onFinish(){}

    /***
     * 加载缓存
     */
    public void onStarLoadCache(){}

    /***
     * 加载成功（不论是缓存还是网络）
     * @param bean
     */
    public void onLoadSuccess(IntroLoader.PlayBean bean){}

    /***
     * 结束
     */
    public void onLoadFinish(){}

    /***
     * 没有数据
     */
    public void onNoData(){}
}
