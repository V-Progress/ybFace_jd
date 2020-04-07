package com.yunbiao.ybsmartcheckin_live_id.utils;

import android.text.TextUtils;
import android.util.Log;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.CustomSDCardLoader;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.ResetLogoEvent;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyXutils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import skin.support.SkinCompatManager;

public class SkinLoader {
    private static final String TAG = "SkinLoader";

    public static void setSkin(final String skinName) {
        SkinCompatManager.getInstance().loadSkin(skinName, new SkinCompatManager.SkinLoaderListener() {
            @Override
            public void onStart() {
                Log.e(TAG, "onStart: 开始加载皮肤");
                UIUtils.showTitleTip(APP.getContext(), "正在切换：" + skinName);
            }

            @Override
            public void onSuccess() {
                UIUtils.showTitleTip(APP.getContext(), "切换皮肤成功：" + skinName);
                Log.e(TAG, "onSuccess: 加载皮肤成功");
                EventBus.getDefault().post(new ResetLogoEvent());
            }

            @Override
            public void onFailed(String errMsg) {
                UIUtils.showTitleTip(APP.getContext(), "切换皮肤失败：" + skinName);
                Log.e(TAG, "onSuccess: 加载皮肤失败：" + errMsg);
            }
        }, CustomSDCardLoader.SKIN_LOADER_STRATEGY_SDCARD);
    }

    public static File checkSkinFile(int fileId) {
        return new File(Constants.SKIN_PATH, "skin_" + fileId + ".skin");
    }

    public interface LoadSkinCallback {
        void loadResult(boolean isSuccess);
    }

    public static void loadSkin(String url, int skinId, String skinDirPath, final LoadSkinCallback callback) {
        if(skinId == -1){
            callback.loadResult(true);
            return;
        }

        final SkinCompatManager.SkinLoaderListener listener = new SkinCompatManager.SkinLoaderListener() {
            @Override
            public void onStart() {
                Log.e(TAG, "onStart: 开始加载皮肤");
            }

            @Override
            public void onSuccess() {
                Log.e(TAG, "onSuccess: 加载皮肤成功");
                callback.loadResult(true);
            }

            @Override
            public void onFailed(String errMsg) {
                Log.e(TAG, "onSuccess: 加载皮肤失败：" + errMsg);
                callback.loadResult(false);
            }
        };

        final File skinFile = new File(skinDirPath, "skin_" + skinId + ".skin");
        //检查皮肤地址
        if (skinFile != null && skinFile.exists()) {
            SkinCompatManager.getInstance().loadSkin(skinFile.getName(), listener, CustomSDCardLoader.SKIN_LOADER_STRATEGY_SDCARD);
            return;
        }

        //皮肤不存在，检查url合法性
        if (TextUtils.isEmpty(url) || !url.startsWith("http://")) {
            Log.e(TAG, "loadSkin: url地址不合法：" + url);
            callback.loadResult(false);
            return;
        }

        //开始下载皮肤
        MyXutils.getInstance().downLoadFile(url, skinFile.getPath(), false, new MyXutils.XDownLoadCallBack() {
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                Log.e(TAG, "onLoading: 下载进度：" + total + "-" + current);
            }

            @Override
            public void onSuccess(File result) {
                SkinCompatManager.getInstance().loadSkin(result.getName(), listener, CustomSDCardLoader.SKIN_LOADER_STRATEGY_SDCARD);
            }

            @Override
            public void onError(Throwable ex) {
                Log.e(TAG, "onError: " + (ex == null ? "NULL" : ex.getMessage()));
                callback.loadResult(false);
            }

            @Override
            public void onFinished() {
                Log.e(TAG, "onFinished: 下载结束");
            }
        });
    }

    public static void loadSkin(String url, int fileId) {
        if (TextUtils.isEmpty(url) || !url.startsWith("http://")) {
            Log.e(TAG, "loadSkin: url地址不合法：" + url);
            return;
        }
        File skinFile = checkSkinFile(fileId);
        if (skinFile != null && skinFile.exists()) {
            setSkin(skinFile.getName());
            return;
        }

        MyXutils.getInstance().downLoadFile(url, skinFile.getPath(), false, new MyXutils.XDownLoadCallBack() {
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                Log.e(TAG, "onLoading: 下载进度：" + total + "-" + current);
            }

            @Override
            public void onSuccess(File result) {
                setSkin(result.getName());
            }

            @Override
            public void onError(Throwable ex) {
                Log.e(TAG, "onError: " + (ex == null ? "NULL" : ex.getMessage()));
            }

            @Override
            public void onFinished() {
                Log.e(TAG, "onFinished: 下载结束");
            }
        });
    }
}
