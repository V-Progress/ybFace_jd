package com.yunbiao.ybsmartcheckin_live_id.utils.xutil;

import org.xutils.common.Callback;

/**
 * Created by jsx on 2016/5/25.
 */
public abstract class MyProgressCallBack<ResultType> implements Callback.ProgressCallback<ResultType> {
    @Override
    public void onWaiting() {

    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onLoading(long total, long current, boolean isDownloading) {

    }

    @Override
    public void onSuccess(ResultType result) {

    }

    @Override
    public void onError(Throwable ex, boolean isOnCallback) {

    }

    @Override
    public void onCancelled(CancelledException cex) {

    }

    @Override
    public void onFinished() {

    }
}
