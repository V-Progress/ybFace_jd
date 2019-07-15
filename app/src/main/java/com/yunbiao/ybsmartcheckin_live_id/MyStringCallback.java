package com.yunbiao.ybsmartcheckin_live_id;

import com.zhy.http.okhttp.callback.StringCallback;

import okhttp3.Call;
import okhttp3.Request;

public abstract class MyStringCallback extends StringCallback {

    @Override
    public void onBefore(Request request, int id) {
    }

    @Override
    public void onResponse(String response, int id) {
    }

    @Override
    public void onError(Call call, Exception e, int id) {
    }

    @Override
    public void onAfter(int id) {
    }
}
