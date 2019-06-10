package com.yunbiao.ybsmartcheckin_live_id.common;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.afinel.VersionUpdateConstants;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyXutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class UpdateManager {

    private Context mContext;

    //提示语
    private String updateMsg = "有最新的软件包，请快下载吧~";

    //返回的安装包url
    private static String apkUrl = "http://211.157.160.102/imgserver/hsd.apk";

    private Dialog noticeDialog;

    private Dialog downloadDialog;
    /* 下载包安装路径 */
    private static final String savePath = "/sdcard/mnt/sdcard/hsd/apk/";

    private static final String saveFileName = savePath + "hsd.apk";

    /* 进度条与通知ui刷新的handler和msg常量 */
    private ProgressBar mProgress;

    private static final int DOWN_UPDATE = 1;

    private static final int DOWN_OVER = 2;

    private int progress;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    if (CoreInfoHandler.onReceivedProgressRun != null) {
                        CoreInfoHandler.onReceivedProgressRun.OnProgressRunReceived(progress);
                    }
                    break;
                case DOWN_OVER:
                    installApk();
                    break;
                default:
                    break;
            }
        }
    };

    public UpdateManager(Context context) {
        this.mContext = context;
    }

    //	//外部接口让主Activity调用
    public void checkUpdateInfo(String version) {
        //判断是否需要更新
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("clientVersion", version);
        paramMap.put("type", 1+ "");
        MyXutils.getInstance().post(ResourceUpdate.VERSION_URL, paramMap, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {
                Log.e("UpdateManager", "checkUpdateInfo------------->"+result );
                if (result.startsWith("\"")) {
                    result = result.substring(1, result.length() - 1);
                }
                judgeIsUpdate(result);
            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onFinish() {

            }
        });
    }

    private void judgeIsUpdate(String isUpdate) {
        //返回
        switch (isUpdate) {
            case "1": //不需要更新
                Toast.makeText(mContext, "当前版本为最新版本", Toast.LENGTH_SHORT).show();
                if (UIUtils.pd.isShowing()) {
                    UIUtils.pd.dismiss();
                }
                break;
            case "faile":  //网络不通，或者解析出错
                Toast.makeText(mContext, "网络连接失败，请检查网络", Toast.LENGTH_SHORT).show();
                if (UIUtils.pd.isShowing()) {
                    UIUtils.pd.dismiss();
                }
                break;
            default:
                apkUrl = isUpdate;
                //下载apk
                downloadApk();
                break;
        }
    }

    private Runnable mdownApkRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                URL url = new URL(apkUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setConnectTimeout(30000);
                conn.setReadTimeout(360000);
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();

                File file = new File(savePath);
                if (!file.exists()) {
                    file.mkdir();
                }

                String apkFile = saveFileName;
                File ApkFile = new File(apkFile);
                FileOutputStream fos = new FileOutputStream(ApkFile);

                int count = 0;
                byte buf[] = new byte[1024];
                boolean interceptFlag = false;
                do {
                    int numread = is.read(buf);
                    count += numread;
                    progress = (int) (((float) count / length) * 100);
                    //更新进度
                    mHandler.sendEmptyMessage(DOWN_UPDATE);
                    if (numread <= 0) {
                        //下载完成通知安装
                        mHandler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    fos.write(buf, 0, numread);
                } while (!interceptFlag);//点击取消就停止下载.

                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 下载apk
     *
     * @param
     */
    private void downloadApk() {
        Thread downLoadThread = new Thread(mdownApkRunnable);
        downLoadThread.start();
    }

    /**
     * 安装apk
     *
     * @param
     */
    private void installApk() {
        File apkfile = new File(saveFileName);
        if (!apkfile.exists()) {
            return;
        }
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apkfile), "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }
}
