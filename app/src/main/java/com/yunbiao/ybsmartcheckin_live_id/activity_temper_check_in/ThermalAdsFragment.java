package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.FlavorType;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.AdsStateEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.AdsUpdateEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.InfoTouchEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.ResetLogoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateMediaEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.data.AdsListener;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.bean.AdvertBean;
import com.yunbiao.ybsmartcheckin_live_id.business.WeatherManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.ThreadUitls;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyXutils;
import com.yunbiao.ybsmartcheckin_live_id.views.ImageFileLoader;
import com.yunbiao.ybsmartcheckin_live_id.views.mixplayer.MixedPlayerLayout;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import okhttp3.Call;

public class ThermalAdsFragment extends Fragment implements AdsListener {
    private static final String TAG = "AdsFragment";
    private int mCurrentOrientation;
    private View rootView;
    private View adsView;
    private View headView;
    private ImageView ivLogo;
    private PropertyValuesHolder animY;

    private long MAX_TIME = 10;
    private long onTime = MAX_TIME;
    private ObjectAnimator objectAnimator;
    private MixedPlayerLayout mixedPlayer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        mCurrentOrientation = getActivity().getResources().getConfiguration().orientation;
        rootView = inflater.inflate(R.layout.fragment_ads_thermal, container, false);

        //公司信息
        ivLogo = rootView.findViewById(R.id.iv_ads_logo);

        //广告内容
        mixedPlayer = rootView.findViewById(R.id.mpl_ads);
        mixedPlayer.setDefaultImgId(R.mipmap.splash);

        //大布局
        headView = rootView.findViewById(R.id.layout_head);
        adsView = rootView.findViewById(R.id.layout_ads);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        adsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeAds();
                onTime = MAX_TIME;
            }
        });
        //先关闭广告
        closeAds();
    }

    @Override
    public void onStart() {
        super.onStart();
        d("onViewCreated: " + getView().getHeight() + " --- " + getView().getWidth());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateMediaEvent event){
        d("收到媒体更新事件");
        //初始化广告数据
        getAdsData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent event){
        //再onResume中判断
        setLogo(ivLogo,null);
    }


    private void setLogo(ImageView logoView,TextView tvName) {
        boolean localPriority = SpUtils.getBoolean(ThermalConst.Key.LOCAL_PRIORITY, ThermalConst.Default.LOCAL_PRIORITY);
        if (localPriority) {
            String logoPath = SpUtils.getStr(ThermalConst.Key.MAIN_LOGO_IMG, ThermalConst.Default.MAIN_LOGO_IMG);
            if (TextUtils.isEmpty(logoPath)) {
                logoView.setImageResource(ThermalConst.Default.DEFAULT_LOGO_ID);
            } else {
                logoView.setImageBitmap(BitmapFactory.decodeFile(logoPath));
            }
            if(tvName != null){
                tvName.setText(SpUtils.getStr(ThermalConst.Key.MAIN_LOGO_TEXT, ThermalConst.Default.MAIN_LOGO_TEXT));
            }
        } else {
            Company company = SpUtils.getCompany();
            String comlogo = company.getComlogo();
            String abbname = company.getAbbname();
            if(company.getComid() == Constants.NOT_BIND_COMPANY_ID || TextUtils.isEmpty(comlogo)){
                logoView.setVisibility(View.GONE);
                logoView.setImageBitmap(null);
            } else {
                Glide.with(getActivity()).load(comlogo).asBitmap().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        logoView.setImageBitmap(resource);
                    }
                });
            }
            if(tvName != null){
                tvName.setText(TextUtils.isEmpty(abbname) ? "" : abbname);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(ResetLogoEvent event){
        //再onResume中判断
        setLogo(ivLogo,null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(AdsUpdateEvent updateEvent) {
        d("update: ----- 收到广告更新事件");
        getAdsData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(InfoTouchEvent updateEvent) {
        d("update: ----- 收到计时重置事件");
        onTime = MAX_TIME;
    }

    private void loadCacheAds(){
        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                d("开始加载本地缓存广告...");
                String ads;
                if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    ads = SpUtils.getStr(SpUtils.COMPANY_AD_SHU);
                } else {
                    ads = SpUtils.getStr(SpUtils.COMPANY_AD_HENG);
                }

                if (!TextUtils.isEmpty(ads)) {
                    AdvertBean advertBean = new Gson().fromJson(ads, AdvertBean.class);
                    checkAds(advertBean.getAdvertObject().getImgArray());
                }
            }
        });
    }

    private void getAdsData() {
        int companyid = SpUtils.getInt(SpUtils.COMPANYID);
        int type = mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 1;
        final Map<String, String> map = new HashMap<>();
        map.put("comId", companyid + "");
        map.put("type", type + "");
        OkHttpUtils.post().url(ResourceUpdate.GETAD).params(map).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                d("请求失败..." + e == null ? "NULL" : e.getMessage());
                loadCacheAds();
            }

            @Override
            public void onResponse(String response, int id) {
                d("请求成功..." + response);
                AdvertBean advertBean = new Gson().fromJson(response, AdvertBean.class);
                if(advertBean == null){
                    return;
                }

                if(advertBean.getStatus() != 1 || advertBean.getAdvertObject() == null){
                    mixedPlayer.setDatas(null);
                    return;
                }

                List<AdvertBean.AdvertObjectEntity.ImgArrayEntity> imgArray = advertBean.getAdvertObject().getImgArray();
                if (imgArray == null || imgArray.size() <= 0) {
                    mixedPlayer.setDatas(null);
                    return;
                }

                if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    SpUtils.saveStr(SpUtils.COMPANY_AD_SHU, response);
                } else {
                    SpUtils.saveStr(SpUtils.COMPANY_AD_HENG, response);
                }

                int advertTime = advertBean.getAdvertObject().getAdvertTime();
                mixedPlayer.setPlayTime(advertTime);

                checkAds(imgArray);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mixedPlayer.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mixedPlayer.pause();
    }

    @Override
    public void detectFace() {
        if (adsView.isShown()) {
            closeAds();
        }

        onTime = MAX_TIME;
    }

    interface AdsCallback {
        void getAds(File file);

        void finish();
    }

    private void checkAds(List<AdvertBean.AdvertObjectEntity.ImgArrayEntity> imgArray) {
        final List<String> adsList = new ArrayList<>();
        Queue<String> urlQueue = new LinkedList<>();
        for (AdvertBean.AdvertObjectEntity.ImgArrayEntity imgArrayEntity : imgArray) {
            String adUrl = imgArrayEntity.getAdvertimg();
            urlQueue.add(adUrl);
        }
        download(urlQueue, new AdsCallback() {
            @Override
            public void getAds(File file) {
                d("getAds: ---------- 下载成功：" + file.getPath());
                adsList.add(file.getPath());
            }

            @Override
            public void finish() {
                d("finish: ---------- 结束");
                mixedPlayer.setDatas(adsList);
            }
        });
    }

    private void download(final Queue<String> fileQueue, final AdsCallback callback) {
        if (fileQueue.size() <= 0) {
            if (callback != null) {
                callback.finish();
            }
            return;
        }
        final String adUrl = fileQueue.poll();
        String adPath = Constants.ADS_PATH + (adUrl.substring(adUrl.lastIndexOf("/") + 1));
        final File file = new File(adPath);
        d("检查广告文件..." + adPath);

        if (file.exists() && file.isFile()) {
            d("广告文件存在..." + file.getPath());
            if (callback != null) {
                callback.getAds(file);
            }
            download(fileQueue, callback);
            return;
        }

        d("广告文件不存在，准备下载..." + adUrl);
        MyXutils.getInstance().downLoadFile(adUrl, adPath, true, new MyXutils.XDownLoadCallBack() {
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                d("百分比---> " + ((float) current / total * 100));
            }

            @Override
            public void onSuccess(File result) {
                d("下载成功: " + result.getName());
                if (callback != null) {
                    callback.getAds(file);
                }
            }

            @Override
            public void onError(Throwable ex) {
                d("下载失败：" + adUrl);
            }

            @Override
            public void onFinished() {
                download(fileQueue, callback);
            }
        });
    }

    private void d(@NonNull String msg) {
        Log.d(TAG, msg);
    }

    private void closeAds() {
        if (objectAnimator != null && objectAnimator.isRunning()) {
            return;
        }
        startAnim(headView, 0, -headView.getHeight(), null);
        startAnim(adsView, 0, adsView.getHeight(), new Runnable() {
            @Override
            public void run() {
                timerHandler.removeMessages(0);
                timerHandler.sendEmptyMessage(0);
                headView.setVisibility(View.GONE);
                adsView.setVisibility(View.GONE);

                mixedPlayer.pause();

                EventBus.getDefault().postSticky(new AdsStateEvent(AdsStateEvent.STATE_CLOSED));
            }
        });
    }

    private void openAds() {
        if (objectAnimator != null && objectAnimator.isRunning()) {
            return;
        }
        headView.setVisibility(View.VISIBLE);
        adsView.setVisibility(View.VISIBLE);
        startAnim(headView, -headView.getHeight(), 0, null);
        startAnim(adsView, adsView.getHeight(), 0, new Runnable() {
            @Override
            public void run() {
                timerHandler.removeMessages(0);
                mixedPlayer.resume();

                EventBus.getDefault().postSticky(new AdsStateEvent(AdsStateEvent.STATE_OPENED));
            }
        });
    }

    private void startAnim(final View view, int formY, int toY, final Runnable runnable) {
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);//开始动画前开启硬件加速
        animY = PropertyValuesHolder.ofFloat("translationY", formY, toY);//生成值动画
        //加载动画Holder
        objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, animY);
        objectAnimator.setDuration(500);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setLayerType(View.LAYER_TYPE_NONE, null);//动画结束时关闭硬件加速
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        objectAnimator.start();
    }

    private Handler timerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (isVisible()) {
                if (onTime <= 0) {
                    onTime = MAX_TIME;
                    if (!adsView.isShown()) {
                        openAds();
                    }
                } else {
                    onTime--;
                }
            }

            timerHandler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
