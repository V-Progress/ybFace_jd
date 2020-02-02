package com.yunbiao.ybsmartcheckin_live_id.activity.fragment.data;


import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.bean.PathBean;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyXutils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

public class IntroLoader {
    public static final int TYPE_IMAGE_VIDEO = 1;
    public static final int TYPE_URL = 2;
    private static int mainIndex = 0;//下载流程主循环索引
    private static int childIndex = 0;//下载子循环索引
    private static final String TAG = "IntroLoader";

    public static void loadData(final LoadListener loadListener) {
        loadListener.onStart();

        final String cacheData = SpUtils.getStr(SpUtils.COMPANY_INFO);
        int compId = SpUtils.getInt(SpUtils.COMPANYID);
        OkHttpUtils.post().url(ResourceUpdate.getCompInfo).addParams("comId", compId + "").build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.d(TAG, "请求失败... " + e != null ? e.getMessage() : "NULL");
                loadListener.onFailed(e);
                if (!TextUtils.isEmpty(cacheData)) {
                    loadCacheData(cacheData, loadListener);
                }
            }

            @Override
            public void onResponse(String response, int id) {
                Log.d(TAG, "onResponse: --------------- " + response);
                if (TextUtils.isEmpty(response)) {
                    loadListener.onFailed(new Exception("Response is null"));
                    if (!TextUtils.isEmpty(cacheData)) {
                        loadListener.onStarLoadCache();
                    }
                    return;
                }

                //如果缓存数据不为空并且与之相同，则不再处理数据
                if (TextUtils.equals(response, cacheData)) {
                    Log.d(TAG, "数据无变化，不继续处理... ");
                    //直接加载缓存 todo
                    loadCacheData(cacheData, loadListener);
                    return;
                }

                InfoBean infoBean = new Gson().fromJson(response, InfoBean.class);
                if (infoBean == null) {
                    loadListener.onFailed(new Exception("Format InfoBean failed"));
                    return;
                }

                final List<InfoBean.Propa> propaArray = infoBean.getPropaArray();
                if (propaArray == null || propaArray.size() <= 0) {
                    loadListener.onNoData();
                    SpUtils.saveStr(SpUtils.COMPANY_INFO, "");
                    return;
                }

                SpUtils.saveStr(SpUtils.COMPANY_INFO, response);

                handleData(propaArray, loadListener);
            }

            @Override
            public void onAfter(int id) {
                super.onAfter(id);
                loadListener.onFinish();
            }
        });
    }

    private static void loadCacheData(String cacheData, LoadListener loadListener) {
        InfoBean infoBean = new Gson().fromJson(cacheData, InfoBean.class);
        if (infoBean == null) {
            return;
        }

        final List<InfoBean.Propa> propaArray = infoBean.getPropaArray();
        if (propaArray == null || propaArray.size() <= 0) {
            return;
        }

        loadListener.onStarLoadCache();
        handleData(propaArray, loadListener);
    }

    private static void handleData(List<InfoBean.Propa> propaArray, final LoadListener loadListener) {
        final List<PlayBean> playList = getPlayList(propaArray);
        loadResource(playList, new DownloadListener() {
            @Override
            public void getSingle(PlayBean bean) {
                loadListener.onLoadSuccess(bean);
                Log.e(TAG, "getSingle: " + bean.toString());
            }

            @Override
            public void onFinished() {
                d("全部处理结束... ");
                loadListener.onLoadFinish();
            }
        });
    }

    private static List<PlayBean> getPlayList(List<InfoBean.Propa> propaArray) {
        List<PlayBean> playList = new ArrayList<>();
        for (InfoBean.Propa propa : propaArray) {
            PlayBean playBean = new PlayBean();
            playBean.name = propa.name;
            playBean.type = propa.type;
            if (propa.type == TYPE_URL) {
                playBean.url = propa.url;
            } else {
                playBean.time = propa.time;
                ArrayList<PathBean> pathList = new ArrayList<>();
                List<String> imgArray = propa.getImgArray();
                if (imgArray != null) {
                    for (String url : imgArray) {
                        String localPath = Constants.INFO_PATH + url.substring(url.lastIndexOf("/") + 1);
                        pathList.add(new PathBean(url, localPath, PathBean.TYPE_IMG));
                    }
                }

                List<String> videoArray = propa.getVideoArray();
                if (videoArray != null) {
                    for (String url : videoArray) {
                        String localPath = Constants.INFO_PATH + url.substring(url.lastIndexOf("/") + 1);
                        pathList.add(new PathBean(url, localPath, PathBean.TYPE_VIDEO));
                    }
                }
                playBean.pathList = pathList;
            }
            playList.add(playBean);
        }
        return playList;
    }

    public interface DownloadListener {
        void getSingle(PlayBean bean);

        void onFinished();
    }

    private static void loadResource(final List<PlayBean> playBeans, final DownloadListener listener) {
        if (mainIndex > playBeans.size() - 1) {
            mainIndex = 0;
            listener.onFinished();
            return;
        }
        final PlayBean bean = playBeans.get(mainIndex);
        d("检查... " + mainIndex + " --- " + bean.toString());
        mainIndex++;
        //是URL就跳过
        if (bean.type == TYPE_URL) {
            listener.getSingle(bean);
            loadResource(playBeans, listener);
            return;
        }
        //是图片视频就下载
        d("准备... " + bean.toString());
        List<PathBean> pathList = bean.pathList;
        download(pathList, new Runnable() {
            @Override
            public void run() {
                d("下载完毕... ");
                listener.getSingle(bean);
                loadResource(playBeans, listener);
            }
        });
    }

    private static void download(final List<PathBean> list, final Runnable finishRunnable) {
        if (childIndex > list.size() - 1) {
            childIndex = 0;
            finishRunnable.run();
            return;
        }
        final PathBean pathBean = list.get(childIndex);
        String url = pathBean.getUrl();
        String localPath = pathBean.getLocalPath();
        childIndex++;
        if (!TextUtils.isEmpty(localPath) && new File(localPath).exists()) {
            d("文件存在... " + localPath);
            download(list, finishRunnable);
            return;
        }

        d("开始下载... " + url);
        MyXutils.getInstance().downLoadFile(url, localPath, false, new MyXutils.XDownLoadCallBack() {
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
            }

            @Override
            public void onSuccess(File result) {
                d("下载成功... " + result.getPath());
                pathBean.setLocalPath(result.getPath());
            }

            @Override
            public void onError(Throwable ex) {
                d("下载失败... " + ex != null ? ex.getMessage() : "NULL");
            }

            @Override
            public void onFinished() {
                download(list, finishRunnable);
            }
        });
    }

    private static void d(String log) {
        Log.d(TAG, log);
    }

    class InfoBean {
        String message;
        int status;

        List<Propa> propaArray;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public List<Propa> getPropaArray() {
            return propaArray;
        }

        public void setPropaArray(List<Propa> propaArray) {
            this.propaArray = propaArray;
        }

        class Propa {
            int id;
            int type;
            int time;
            String name;
            String descInfo;
            String url;

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            List<String> imgArray;
            List<String> videoArray;

            public List<String> getImgArray() {
                return imgArray;
            }

            public void setImgArray(List<String> imgArray) {
                this.imgArray = imgArray;
            }

            public List<String> getVideoArray() {
                return videoArray;
            }

            public void setVideoArray(List<String> videoArray) {
                this.videoArray = videoArray;
            }

            public int getTime() {
                return time;
            }

            public void setTime(int time) {
                this.time = time;
            }

            public String getDescInfo() {
                return descInfo;
            }

            public void setDescInfo(String descInfo) {
                this.descInfo = descInfo;
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public int getType() {
                return type;
            }

            public void setType(int type) {
                this.type = type;
            }
        }
    }

    public static class PlayBean {
        private String name;
        private int time;
        private int type;
        private String url;
        private ArrayList<PathBean> pathList;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public ArrayList<PathBean> getPathList() {
            return pathList;
        }

        public void setPathList(ArrayList<PathBean> pathList) {
            this.pathList = pathList;
        }

        @Override
        public String toString() {
            return "PlayBean{" +
                    "name='" + name + '\'' +
                    ", type=" + type +
                    ", url='" + url + '\'' +
                    ", pathList=" + pathList +
                    '}';
        }

    }

}
