package com.yunbiao.ybsmartcheckin_live_id.activity.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.AdsStateEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.InfoTouchEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.child.VideoFragment;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.child.WebFragment;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyXutils;
import com.yunbiao.ybsmartcheckin_live_id.views.InfoViewPager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

public class InformationFragment extends Fragment {
    private static final String TAG = "InformationFragment";
    private static final int TYPE_IMAGE_VIDEO = 1;
    private static final int TYPE_URL = 2;

    private int mainIndex = 0;//下载流程主循环索引
    private int childIndex = 0;//下载子循环索引
    private ViewPager vpInfomation;
    private List<Fragment> fragments = new ArrayList<>();
    private MyAdapter vpPagerAdapter;
    private View ivBg;
    private View pbLoad;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        View rootView = inflater.inflate(R.layout.fragment_information, container, false);
        vpInfomation = rootView.findViewById(R.id.vp_information);
        ivBg = rootView.findViewById(R.id.iv_bg);
        pbLoad = rootView.findViewById(R.id.pb_load);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vpPagerAdapter = new MyAdapter(getChildFragmentManager(),fragments);
        vpInfomation.setOffscreenPageLimit(3);
        vpInfomation.setAdapter(vpPagerAdapter);
        initData();
    }

    private class MyAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;

        public MyAdapter(FragmentManager fm,List<Fragment> fragmentList) {
            super(fm);
            fragments = fragmentList;
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    private void initData(){
        pbLoad.setVisibility(View.VISIBLE);
        d("加载缓存数据... ");
        String cacheData = SpUtils.getStr(SpUtils.COMPANY_INFO);
        if(!TextUtils.isEmpty(cacheData)){
            InfoBean infoBean = new Gson().fromJson(cacheData, InfoBean.class);
            if(infoBean != null && infoBean.status == 1){
                final List<InfoBean.Propa> propaArray = infoBean.getPropaArray();
                if (propaArray != null && propaArray.size() > 0) {
                    handleData(propaArray);
                } else {
                    d("无数据3... ");
                }
            } else{
                d("无数据2... ");
            }
        } else {
            d("无数据1... ");
        }

        loadCompanyInfo(cacheData);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(InformationUpdateEvent event){
        Log.e(TAG, "update: 收到更新信息事件");
        loadCompanyInfo(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    //从网络加载开始
    public void loadCompanyInfo(final String cacheData) {
        pbLoad.setVisibility(View.VISIBLE);
        d("加载网络数据... ");
        d( "loadCompanyInfo:  ----------------- " + ResourceUpdate.getCompInfo);
        int compId = SpUtils.getInt(SpUtils.COMPANYID);
        OkHttpUtils.post().url(ResourceUpdate.getCompInfo).addParams("comId", compId + "").build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                d("请求失败... " + e != null ? e.getMessage() : "NULL");
                pbLoad.setVisibility(View.GONE);
            }

            @Override
            public void onResponse(String response, int id) {
                d( "onResponse: --------------- " + response);
                if (TextUtils.isEmpty(response)) {
                    pbLoad.setVisibility(View.GONE);
                    return;
                }
                //如果缓存数据不为空并且与之相同，则不再处理数据
                if(!TextUtils.isEmpty(cacheData) && TextUtils.equals(cacheData,response)){
                    pbLoad.setVisibility(View.GONE);
                    d("数据无变化，不继续处理... ");
                    return;
                }
                InfoBean infoBean = new Gson().fromJson(response, InfoBean.class);
                if (infoBean == null || infoBean.status != 1) {
                    pbLoad.setVisibility(View.GONE);
                    return;
                }
                final List<InfoBean.Propa> propaArray = infoBean.getPropaArray();
                if (propaArray == null || propaArray.size() <= 0) {
                    pbLoad.setVisibility(View.GONE);
                    return;
                }

                SpUtils.saveStr(SpUtils.COMPANY_INFO,response);

                handleData(propaArray);
            }
        });
    }

    private void handleData(List<InfoBean.Propa> propaArray){
        final List<PlayBean> playList = getPlayList(propaArray);

        fragments.clear();
        vpPagerAdapter.notifyDataSetChanged();
        loadResource(playList, new LoadListener() {
            @Override
            public void getSingle(PlayBean bean) {
                Log.e(TAG, "getSingle: " + bean.toString());
                if(bean.type == TYPE_URL){
                    fragments.add(WebFragment.newInstance(bean.url));
                } else if(bean.type == TYPE_IMAGE_VIDEO){
                    fragments.add(VideoFragment.newInstance(bean.time,bean.pathList));
                }
                Log.e(TAG, "fragments: " + fragments.size());
                Log.e(TAG, "fragment: " + fragments.toString());
                vpPagerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFinished() {
                d("全部处理结束... ");
                pbLoad.setVisibility(View.GONE);
                ivBg.setVisibility(fragments.size() <= 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private List<PlayBean> getPlayList(List<InfoBean.Propa> propaArray) {
        List<PlayBean> playList = new ArrayList<>();
        for (InfoBean.Propa propa : propaArray) {
            PlayBean playBean = new PlayBean();
            playBean.name = propa.name;
            playBean.type = propa.type;
            if (propa.type == TYPE_URL) {
                playBean.url = propa.url;
            } else {
                playBean.time = propa.time;
                ArrayList <PlayBean.PathBean> pathList = new ArrayList<>();
                List<String> imgArray = propa.getImgArray();
                if (imgArray != null) {
                    for (String url : imgArray) {
                        String localPath = Constants.INFO_PATH + url.substring(url.lastIndexOf("/") + 1);
                        pathList.add(new PlayBean.PathBean(url,localPath, PlayBean.PathBean.TYPE_IMG));
                    }
                }

                List<String> videoArray = propa.getVideoArray();
                if (videoArray != null) {
                    for (String url : videoArray) {
                        String localPath = Constants.INFO_PATH + url.substring(url.lastIndexOf("/") + 1);
                        pathList.add(new PlayBean.PathBean(url,localPath, PlayBean.PathBean.TYPE_VIDEO));
                    }
                }
                playBean.pathList = pathList;
            }
            playList.add(playBean);
        }
        return playList;
    }

    interface LoadListener{
        void getSingle(PlayBean bean);
        void onFinished();
    }

    private void loadResource(final List<PlayBean> playBeans, final LoadListener listener) {
        if (mainIndex > playBeans.size() - 1) {
            mainIndex = 0;
            listener.onFinished();
            return;
        }
        final PlayBean bean = playBeans.get(mainIndex);
        d( "检查... " + mainIndex + " --- " + bean.toString());
        mainIndex++;
        //是URL就跳过
        if (bean.type == TYPE_URL) {
            listener.getSingle(bean);
            loadResource(playBeans, listener);
            return;
        }
        //是图片视频就下载
        d( "准备... " + bean.toString());
        List<PlayBean.PathBean> pathList = bean.pathList;
        download(pathList, new Runnable() {
            @Override
            public void run() {
                d( "下载完毕... ");
                listener.getSingle(bean);
                loadResource(playBeans, listener);
            }
        });
    }

    private void download(final List<PlayBean.PathBean> list, final Runnable finishRunnable) {
        if (childIndex > list.size() - 1) {
            childIndex = 0;
            finishRunnable.run();
            return;
        }
        final PlayBean.PathBean pathBean = list.get(childIndex);
        String url = pathBean.url;
        String localPath = pathBean.localPath;
        childIndex++;
        if(!TextUtils.isEmpty(localPath) && new File(localPath).exists()){
            d( "文件存在... " + localPath);
            download(list, finishRunnable);
            return;
        }

        d( "开始下载... " + url);
        MyXutils.getInstance().downLoadFile(url, localPath, false, new MyXutils.XDownLoadCallBack() {
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
            }

            @Override
            public void onSuccess(File result) {
                d( "下载成功... " + result.getPath());
                pathBean.localPath = result.getPath();
            }

            @Override
            public void onError(Throwable ex) {
                d( "下载失败... " + ex != null ? ex.getMessage() : "NULL");
            }

            @Override
            public void onFinished() {
                download(list, finishRunnable);
            }
        });
    }

    class BtnAdapter extends BaseAdapter {
        private List<PlayBean> mList;

        public BtnAdapter(List<PlayBean> propaList) {
            mList = propaList;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public PlayBean getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(parent.getContext(), R.layout.item_info_btn, null);
                viewHolder.tvBtn = convertView.findViewById(R.id.btn_info_list);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            PlayBean playBean = mList.get(position);
            viewHolder.tvBtn.setText(playBean.name);
            return convertView;
        }

        class ViewHolder {
            TextView tvBtn;
        }
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
        String name;
        int time;
        int type;
        String url;
        ArrayList<PathBean> pathList;

        public static class PathBean implements Serializable {
            public static final int TYPE_IMG = 0;
            public static final int TYPE_VIDEO = 1;
            int type;
            String url;
            String localPath;

            public int getType() {
                return type;
            }

            public String getUrl() {
                return url;
            }

            public String getLocalPath() {
                return localPath;
            }

            public PathBean(String url, String localPath,int type) {
                this.url = url;
                this.localPath = localPath;
                this.type = type;
            }

            @Override
            public String toString() {
                return "PathBean{" +
                        "type=" + type +
                        ", url='" + url + '\'' +
                        ", localPath='" + localPath + '\'' +
                        '}';
            }
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

    private void d(String msg){
        Log.d(TAG, msg);
    }

    public static class InformationUpdateEvent{

    }
}
