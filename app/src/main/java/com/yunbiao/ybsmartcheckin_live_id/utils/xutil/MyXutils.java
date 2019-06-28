package com.yunbiao.ybsmartcheckin_live_id.utils.xutil;

import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;


import com.yunbiao.ybsmartcheckin_live_id.R;

import org.xutils.http.RequestParams;
import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.io.File;
import java.util.Map;

public class MyXutils {
    private volatile static MyXutils instance;
    private Handler handler;
    private ImageOptions options;

    private MyXutils() {
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * 单例模式
     *
     * @return
     */
    public static MyXutils getInstance() {
        if (instance == null) {
            synchronized (MyXutils.class) {
                if (instance == null) {
                    instance = new MyXutils();
                }
            }
        }
        return instance;
    }

    /**
     * 异步get请求
     *
     * @param url
     * @param callBack
     */
    public void get(String url, final XCallBack callBack) {
        RequestParams params = new RequestParams(url);
        x.http().get(params, new MyCallBack<String>() {
            @Override
            public void onSuccess(final String result) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callBack != null) {
                            callBack.onSuccess(result);
                        }
                    }
                });
            }

            @Override
            public void onError(final Throwable ex, boolean isOnCallback) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callBack != null) {
                            callBack.onError(ex);
                        }
                    }
                });
            }
        });
    }

    /**
     * 异步post请求
     *
     * @param url
     * @param maps
     * @param callBack
     */
    public void post(String url, Map<String, String> maps, final XCallBack callBack) {
        RequestParams params = new RequestParams(url);
        if (maps != null && !maps.isEmpty()) {
            for (Map.Entry<String, String> entry : maps.entrySet()) {
                params.addBodyParameter(entry.getKey(), entry.getValue());
            }
        }
        params.setConnectTimeout(15 * 1000);
        x.http().post(params, new MyCallBack<String>() {
            @Override
            public void onSuccess(final String result) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callBack != null) {
                            callBack.onSuccess(result);
                        }
                    }
                });
            }

            @Override
            public void onError(final Throwable ex, boolean isOnCallback) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callBack != null) {
                            callBack.onError(ex);
                        }
                    }
                });
            }

            @Override
            public void onFinished() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callBack != null) {
                            callBack.onFinish();
                        }
                    }
                });
            }
        });
    }

    /**
     * 正常图片显示
     *
     * @param iv
     * @param url
     * @param option
     */
    public void bindCommonImage(ImageView iv, String url, boolean option) {
        if (option) {
            options = new ImageOptions.Builder()
                    .setImageScaleType(ImageView.ScaleType.FIT_XY)
                    .setUseMemCache(false)
                    .build();
            x.image().bind(iv, url, options);
        } else {
            x.image().bind(iv, url);
        }
    }

    /**
     * 圆形图片显示
     *
     * @param iv
     * @param url
     * @param option
     */
    public void bindCircularImage(ImageView iv, String url, boolean option) {
        if (option) {
            options = new ImageOptions.Builder().setLoadingDrawableId(R.mipmap.logo_bluebg).setFailureDrawableId(R.mipmap
                    .logo_bluebg).setCircular(true).build();
            x.image().bind(iv, url, options);
        } else {
            x.image().bind(iv, url);
        }
    }


    /**
     * 文件上传
     *
     * @param url
     * @param file
     * @param callBack
     */
    public void upLoadFile(String url, Map<String, File> file, final XCallBack callBack) {
        RequestParams params = new RequestParams(url);

        if (file != null) {
            for (Map.Entry<String, File> entry : file.entrySet()) {
                params.addBodyParameter(entry.getKey(), entry.getValue().getAbsoluteFile());
            }
        }
        // 有上传文件时使用multipart表单, 否则上传原始文件流.
        params.setMultipart(true);
        x.http().post(params, new MyCallBack<String>() {
            @Override
            public void onSuccess(final String result) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callBack != null) {
                            callBack.onSuccess(result);
                        }
                    }
                });
            }
        });

    }

    /**
     * 文件下载
     *
     * @param url
     * @param destF
     * @param callBack
     */
    public void downLoadFile(String url, String destF, final boolean isMainThread, final XDownLoadCallBack callBack) {
        RequestParams params = new RequestParams(url);// mDownloadUrl为JSON从服务器端解析出来的下载地址
        params.setSaveFilePath(destF);// 为RequestParams设置文件下载后的保存路径
        params.setAutoResume(true);// 断点续传
        params.setAutoRename(false);// 下载完成后自动为文件命名
        params.setConnectTimeout(15 * 1000);

        x.http().get(params, new MyProgressCallBack<File>() {
            @Override
            public void onLoading(final long total, final long current, final boolean isDownloading) {
                if (callBack != null) {
                    callBack.onLoading(total, current, isDownloading);
                }
            }

            @Override
            public void onSuccess(final File result) {
                if (callBack != null) {
                    callBack.onSuccess(result);
                }
            }

            @Override
            public void onError(final Throwable ex, boolean isOnCallback) {
                if (callBack != null) {
                    callBack.onError(ex);
                }
            }

            @Override
            public void onFinished() {
                if (callBack != null) {
                    callBack.onFinished();
                }
            }
        });
    }

    public interface XCallBack {
        void onSuccess(String result);

        void onError(Throwable ex);

        void onFinish();
    }

    public interface XDownLoadCallBack {
        void onLoading(long total, long current, boolean isDownloading);

        void onSuccess(File result);

        void onError(Throwable ex);

        void onFinished();
    }
}
