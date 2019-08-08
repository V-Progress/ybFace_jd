package com.yunbiao.ybsmartcheckin_live_id.activity.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.InfoUpdateEvent;
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
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;

public class InformationFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "InformationFragment";
    private ImageView ivRight;
    private ListView rlvBtnList;

    private static final int TYPE_IMAGE_VIDEO = 1;
    private static final int TYPE_URL = 2;

    private WebView webView;

    private List<PlayBean> mList = new ArrayList<>();
    private BtnAdapter mAdapter;

    private int mCurrPosistion = -1;//当前指针
    private int mainIndex = 0;//下载流程主循环索引
    private int childIndex = 0;//下载子循环索引
    private View progressView;
    private InfoViewPager infoViewPager;
    private View btnListView;
    private View flInfo;
    private boolean isOverLoad = false;//是否重新加载

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        View rootView = inflater.inflate(R.layout.fragment_information, container, false);
        rlvBtnList = rootView.findViewById(R.id.rlv_information_button_list);
        ivRight = rootView.findViewById(R.id.iv_information_right);
        webView = rootView.findViewById(R.id.wv_web);
        progressView = rootView.findViewById(R.id.ll_progress_info);
        infoViewPager = rootView.findViewById(R.id.vp_info);
        btnListView = rootView.findViewById(R.id.ll_btn_list);
        flInfo = rootView.findViewById(R.id.fl_info);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ivRight.setOnClickListener(this);
        rlvBtnList.setOnItemClickListener(this);
        webView.setOnTouchListener(onTouchListener);
        infoViewPager.setOnTouchListener(onTouchListener);

        mAdapter = new BtnAdapter(mList);
        rlvBtnList.setAdapter(mAdapter);

        initWebView();
        initData();
    }

    private View.OnTouchListener onTouchListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v.getId() == R.id.vp_info && event.getAction() == MotionEvent.ACTION_DOWN) {
                closeBtnList();
                infoViewPager.startAutoPlay();
                EventBus.getDefault().postSticky(new InfoTouchEvent());
            } else if(v.getId() == R.id.wv_web && event.getAction() == MotionEvent.ACTION_DOWN) {
                closeBtnList();
                EventBus.getDefault().postSticky(new InfoTouchEvent());
            }
            return false;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        webView.getSettings().setJavaScriptEnabled(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        webView.getSettings().setJavaScriptEnabled(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initWebView() {
        WebSettings settings = webView.getSettings();
        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        settings.setJavaScriptEnabled(true);
        // 若加载的 html 里有JS 在执行动画等操作，会造成资源浪费（CPU、电量）
        // 在 onStop 和 onResume 里分别把 setJavaScriptEnabled() 给设置成 false 和 true 即可

        //支持插件
        settings.setPluginState(WebSettings.PluginState.ON);
        //设置自适应屏幕，两者合用
        settings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        settings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        //缩放操作
        settings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        settings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        settings.setDisplayZoomControls(false); //隐藏原生的缩放控件

        //其他细节操作
        settings.setCacheMode(WebSettings.LOAD_DEFAULT); //默认缓存
        settings.setAllowFileAccess(true); //设置可以访问文件
        settings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        settings.setLoadsImagesAutomatically(true); //支持自动加载图片
        settings.setDefaultTextEncodingName("utf-8");//设置编码格式

        webView.setWebViewClient(new WebViewClient() {
            //覆盖shouldOverrideUrlLoading 方法
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        EventBus.getDefault().postSticky(new InfoTouchEvent());
        if (rlvBtnList.isShown()) {
            closeBtnList();
        } else {
            openBtnList();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        closeBtnList();
        EventBus.getDefault().postSticky(new InfoTouchEvent());
        if (mCurrPosistion == position) {
            return;
        }
        mCurrPosistion = position;
        loadPlayData(mCurrPosistion);
    }

    private void openBtnList(){
        if(!rlvBtnList.isShown()){
            rlvBtnList.setVisibility(View.VISIBLE);
            ivRight.setVisibility(View.GONE);
            startAnim(200,rlvBtnList,rlvBtnList.getWidth(),0,null);
        }
    }

    private void closeBtnList(){
        if(rlvBtnList.isShown()){
            startAnim(100,rlvBtnList, 0, rlvBtnList.getWidth(), new Runnable() {
                @Override
                public void run() {
                    ivRight.setVisibility(View.VISIBLE);
                    rlvBtnList.setVisibility(View.GONE);
                }
            });
        }
    }

    private ObjectAnimator objectAnimator;
    private PropertyValuesHolder animX;
    private void startAnim(int duration,final View view, float fromX, float toX, final Runnable runnable){
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);//开始动画前开启硬件加速
        animX = PropertyValuesHolder.ofFloat("translationX", fromX, toX);//生成值动画
        //加载动画Holder
        objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, animX);
        objectAnimator.setDuration(duration);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setLayerType(View.LAYER_TYPE_NONE, null);//动画结束时关闭硬件加速
                if(runnable != null){
                    runnable.run();
                }
            }
        });
        objectAnimator.start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(AdsStateEvent adsStateEvent) {
        if (adsStateEvent.state == AdsStateEvent.STATE_OPENED) {
            infoViewPager.onAdsOpened();
        } else {
            infoViewPager.onAdsClosed();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(InfoUpdateEvent infoUpdateEvent) {
        isOverLoad = true;
        Log.e(TAG, "update: 收到宣传信息更新事件");
        loadCompanyInfo(null);
    }

    private void initData(){
        progressView.setVisibility(View.VISIBLE);
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

    //从网络加载开始
    public void loadCompanyInfo(final String cacheData) {
        progressView.setVisibility(View.VISIBLE);
        d("加载网络数据... ");
        d( "loadCompanyInfo:  ----------------- " + ResourceUpdate.getCompInfo);
        int compId = SpUtils.getInt(SpUtils.COMPANYID);
        OkHttpUtils.post().url(ResourceUpdate.getCompInfo).addParams("comId", compId + "").build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                d("请求失败... " + e != null ? e.getMessage() : "NULL");
                progressView.setVisibility(View.GONE);
            }

            @Override
            public void onResponse(String response, int id) {
                d( "onResponse: --------------- " + response);
                if (TextUtils.isEmpty(response)) {
                    progressView.setVisibility(View.GONE);
                    return;
                }
                //如果缓存数据不为空并且与之相同，则不再处理数据
                if(!TextUtils.isEmpty(cacheData) && TextUtils.equals(cacheData,response)){
                    progressView.setVisibility(View.GONE);
                    d("数据无变化，不继续处理... ");
                    return;
                }
                InfoBean infoBean = new Gson().fromJson(response, InfoBean.class);
                if (infoBean == null || infoBean.status != 1) {
                    progressView.setVisibility(View.GONE);
                    return;
                }
                final List<InfoBean.Propa> propaArray = infoBean.getPropaArray();
                if (propaArray == null || propaArray.size() <= 0) {
                    progressView.setVisibility(View.GONE);
                    return;
                }

                SpUtils.saveStr(SpUtils.COMPANY_INFO,response);

                handleData(propaArray);
            }
        });
    }

    private void handleData(List<InfoBean.Propa> propaArray){
        final List<PlayBean> playList = getPlayList(propaArray);

        mList.clear();
        loadResource(playList, new LoadListener() {
            @Override
            public void getSingle(PlayBean bean) {
                ivRight.setVisibility(View.VISIBLE);
                mList.add(bean);
                mAdapter.notifyDataSetChanged();

                if(mCurrPosistion == -1 || isOverLoad){
                    mCurrPosistion = 0;
                    loadPlayData(mCurrPosistion);
                }
            }

            @Override
            public void onFinished() {
                isOverLoad = false;
                d("全部处理结束... ");
                progressView.setVisibility(View.GONE);
            }
        });
    }

    private void loadPlayData(int position){
        if(mList.size() > 0){
            PlayBean playBean = mList.get(position);
            if (playBean.type == TYPE_IMAGE_VIDEO) {
                webView.setVisibility(View.GONE);
                infoViewPager.setVisibility(View.VISIBLE);
                infoViewPager.setData(playBean.pathList,playBean.time);
            } else {
                showWeb(playBean.url);
            }
        }
    }

    private void showWeb(String url) {
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }
        infoViewPager.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        webView.loadUrl(url);
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
                List<PlayBean.PathBean> pathList = new ArrayList<>();
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
        List<PathBean> pathList;

        public static class PathBean {
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

}
