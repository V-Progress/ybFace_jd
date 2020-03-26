package com.yunbiao.ybsmartcheckin_live_id.common;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyXutils;

import java.io.File;
import java.util.HashMap;

public class UpdateManager {
    private static final String TAG = "UpdateManager";
    private Activity mContext;

    private static final int DOWN_SHOW = 0;

    private static final int DOWN_UPDATE = 1;

    private static final int DOWN_DISMISS = -1;

    private static final int DOWN_TIP = 3;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_SHOW://显示
                    if (mContext != null) {
                        UIUtils.updatePd(mContext);
                    }
                    break;
                case DOWN_UPDATE://更新
                    if (UIUtils.pd != null) {
                        int progress = (int) msg.obj;
                        UIUtils.pd.setProgress(progress);//给进度条设置数值
                    }
                    break;
                case DOWN_DISMISS://隐藏
                    if (UIUtils.pd != null && UIUtils.pd.isShowing()) {
                        UIUtils.pd.dismiss();
                    }
                    break;
                case DOWN_TIP://提示
                    if (mContext != null) {
                        String tips = (String) msg.obj;
                        UIUtils.showShort(mContext, tips);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public UpdateManager(Activity context) {
        this.mContext = context;
    }

    //获取新版本
    public void checkUpdateInfo(String version) {
        //判断是否需要更新
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("clientVersion", version);
        paramMap.put("type", Constants.DEVICE_TYPE + "");

        Log.e(TAG, "检查更新：" + ResourceUpdate.VERSION_URL);
        Log.e(TAG, "参数：" + paramMap.toString());
        MyXutils.getInstance().post(ResourceUpdate.VERSION_URL, paramMap, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {
                Log.e(TAG, "onSuccess: " + result);
                if (result.startsWith("\"")) {
                    result = result.substring(1, result.length() - 1);
                }
                judgeIsUpdate(result);
            }

            @Override
            public void onError(Throwable ex) {
                Log.e(TAG, "onError: " + (ex == null ? "NULL" : ex.getMessage()));
                Message message = Message.obtain();
                message.what = DOWN_TIP;
                message.obj = (ex == null ? "NULL" : ex.getMessage());
                mHandler.sendMessage(message);
            }

            @Override
            public void onFinish() {
            }
        });
    }

    //解析返回值
    private void judgeIsUpdate(String isUpdate) {
        Message message = Message.obtain();
        //返回
        switch (isUpdate) {
            case "1": //不需要更新
                message.what = DOWN_TIP;
                message.obj = mContext.getString(R.string.updateManager_dqbbwzxbb);
                mHandler.sendMessage(message);
                mHandler.sendEmptyMessage(DOWN_DISMISS);
                break;
            case "faile":  //网络不通，或者解析出错
                message.what = DOWN_TIP;
                message.obj = mContext.getString(R.string.updateManager_wlljsbqjcwl);
                mHandler.sendMessage(message);
                mHandler.sendEmptyMessage(DOWN_DISMISS);
                break;
            default:
                mHandler.sendEmptyMessage(DOWN_SHOW);
                //下载apk
                download2(isUpdate, Constants.APK_PATH);
                break;
        }
    }

    //开始下载
    private void download2(String url, String destDir) {
        File destFile = new File(destDir);
        if (!destFile.exists()) {
            destFile.mkdirs();
        }

        String filename = url.substring(url.lastIndexOf('/') + 1);
        destFile = new File(destFile, filename);

        Log.e(TAG, "开始下载：" + url);
        Log.e(TAG, "存储目录：" + destFile.getPath());
        MyXutils.getInstance().downLoadFile(url, destFile.getPath(), false, new MyXutils.XDownLoadCallBack() {
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                //更新进度
                Message message = Message.obtain();
                message.what = DOWN_UPDATE;
                message.obj = (int) (((float) current / total) * 100);
                mHandler.sendMessage(message);
            }

            @Override
            public void onSuccess(File result) {
                if (!result.exists()) {
                    Message message = Message.obtain();
                    message.what = DOWN_TIP;
                    message.obj = "文件不存在";
                    mHandler.sendMessage(message);
                    return;
                }
                //下载完成通知安装
                installApk(result);
            }

            @Override
            public void onError(final Throwable ex) {
                Message message = Message.obtain();
                message.what = DOWN_TIP;
                message.obj = mContext.getString(R.string.updateManager_xzsb) + "（" + ex.getMessage() + "）";
                mHandler.sendMessage(message);
            }

            @Override
            public void onFinished() {
                mHandler.sendEmptyMessage(DOWN_DISMISS);
            }
        });
    }

    /**
     * 安装apk
     *
     * @param
     */
    private void installApk(File file) {
        Log.e(TAG, "installApk: 开始安装：" + file.getPath());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }
}
