package com.yunbiao.ybsmartcheckin_live_id.activity.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.AdsStateEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.AdsUpdateEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.InfoTouchEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateLogoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.data.AdsListener;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.bean.AdvertBean;
import com.yunbiao.ybsmartcheckin_live_id.business.WeatherManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.utils.FileUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.ThreadUitls;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyXutils;
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

public class AdsFragment extends Fragment implements AdsListener {
    private static final String TAG = "AdsFragment";
    private int mCurrentOrientation;
    private View rootView;
    private View adsView;
    private View headView;
    private VideoView videoView;
    private ImageView imageView;
    private TextView tvWeather;
    private ImageView ivWeather;
    private ImageView ivLogo;
    private TextView tvAbbName;
    private TextView tvSlogan;
    private PropertyValuesHolder animY;

    private long MAX_TIME = 30;
    private long onTime = MAX_TIME;
    private int advertTime = 10;
    private List<File> mAdsList = new ArrayList<>();
    private boolean go = true;
    private ObjectAnimator objectAnimator;
    private TextView tvNumber;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        mCurrentOrientation = getActivity().getResources().getConfiguration().orientation;
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            rootView = inflater.inflate(R.layout.fragment_ads, container, false);
        } else {
            rootView = inflater.inflate(R.layout.fragment_ads_h, container, false);
        }

        //公司信息
        ivLogo = rootView.findViewById(R.id.iv_ads_logo);
        tvAbbName = rootView.findViewById(R.id.tv_ads_addname);
        tvSlogan = rootView.findViewById(R.id.tv_ads_slogan);
        tvNumber = rootView.findViewById(R.id.tv_number_ads);

        //天气
        ivWeather = rootView.findViewById(R.id.iv_ads_wea);
        tvWeather = rootView.findViewById(R.id.tv_ads_tem);

        //广告内容
        imageView = rootView.findViewById(R.id.iv_out_ads);
        videoView = rootView.findViewById(R.id.vv_out_ads);
        videoView.setZOrderOnTop(true);

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

        tvNumber.setText(SpUtils.getStr(SpUtils.DEVICE_NUMBER));
        //先关闭广告
        closeAds();

        //初始化广告数据
        initAdsData();

        //开始获取天气
        WeatherManager.instance().start(resultListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        d("onViewCreated: " + getView().getHeight() + " --- " + getView().getWidth());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent event){
        Company company = SpUtils.getCompany();
        tvNumber.setText(SpUtils.getStr(SpUtils.DEVICE_NUMBER));
        tvAbbName.setText(company.getAbbname());
        tvSlogan.setText(company.getSlogan());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateLogoEvent event) {
        Log.e("112233", "22222: 收到更新图标的事件" );
        String logoPath = event.getLogoPath();
        if (!TextUtils.isEmpty(logoPath)) {
            Glide.with(getActivity()).load(logoPath).asBitmap().into(ivLogo);
        }
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

    /*天气请求结果监听*/
    private WeatherManager.ResultListener resultListener = new WeatherManager.ResultListener() {
        @Override
        public void updateWeather(int id, String weatherInfo) {
            ivWeather.setImageResource(R.mipmap.icon_snow);
            tvWeather.setText(weatherInfo);
        }
    };

    private void initAdsData() {
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

                if (TextUtils.isEmpty(ads)) {
                    getAdsData();
                } else {
                    AdvertBean advertBean = new Gson().fromJson(ads, AdvertBean.class);
                    checkAds(advertBean.getAdvertObject().getImgArray());
                }
            }
        });
    }

    private void getAdsData() {
        int companyid = SpUtils.getInt(SpUtils.COMPANYID);
        int type = mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 1;
        final Map<String, String> map = new HashMap<String, String>();
        map.put("comId", companyid + "");
        map.put("type", type + "");
        OkHttpUtils.post().url(ResourceUpdate.GETAD).params(map).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                d("请求失败..." + e == null ? "NULL" : e.getMessage());
            }

            @Override
            public void onResponse(String response, int id) {
                d("请求成功..." + response);
                AdvertBean advertBean = new Gson().fromJson(response, AdvertBean.class);
                if (advertBean == null || advertBean.getStatus() != 1 || advertBean.getAdvertObject() == null) {
                    return;
                }
                List<AdvertBean.AdvertObjectEntity.ImgArrayEntity> imgArray = advertBean.getAdvertObject().getImgArray();
                if (imgArray == null) {
                    return;
                }
                if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    SpUtils.saveStr(SpUtils.COMPANY_AD_SHU, response);
                } else {
                    SpUtils.saveStr(SpUtils.COMPANY_AD_HENG, response);
                }

                advertTime = advertBean.getAdvertObject().getAdvertTime();

                checkAds(imgArray);
            }
        });
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
        go = false;
        mAdsList.clear();
        Queue<String> urlQueue = new LinkedList<>();
        for (AdvertBean.AdvertObjectEntity.ImgArrayEntity imgArrayEntity : imgArray) {
            String adUrl = imgArrayEntity.getAdvertimg();
            urlQueue.add(adUrl);
        }
        download(urlQueue, new AdsCallback() {
            @Override
            public void getAds(File file) {
                d("getAds: ---------- 下载成功：" + file.getPath());
                mAdsList.add(file);
                go = true;
            }

            @Override
            public void finish() {
                d("finish: ---------- 结束");
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

    private int mAdsindex = 0;
    private Handler adsHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (go && isVisible() && mAdsList.size() > 0) {
                if (mAdsindex > mAdsList.size() - 1) {
                    mAdsindex = 0;
                }

                File file = mAdsList.get(mAdsindex);
                d("handleMessage: ------ " + file.getPath());
                if (FileUtils.isVideo(file.getPath())) {
                    imageView.setVisibility(View.GONE);
                    videoView.setVisibility(View.VISIBLE);
                    videoView.setVideoPath(file.getPath());
                    videoView.start();
                    videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mAdsindex++;

                            adsHandler.removeMessages(0);
                            adsHandler.sendEmptyMessage(0);
                        }
                    });
                    videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mp, int what, int extra) {
                            mAdsindex++;

                            adsHandler.removeMessages(0);
                            adsHandler.sendEmptyMessage(0);
                            return true;
                        }
                    });
                } else {
                    imageView.setVisibility(View.VISIBLE);
                    videoView.setVisibility(View.GONE);
                    Glide.with(getActivity()).load(file).crossFade(300).into(imageView);
                    mAdsindex++;
                    if(mAdsList.size() < 2){

                    }

                    adsHandler.removeMessages(0);
                    adsHandler.sendEmptyMessageDelayed(0, advertTime * 1000);
                }
            } else {
                adsHandler.sendEmptyMessageDelayed(0, 1000);
            }
        }
    };


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
                adsHandler.removeMessages(0);

                headView.setVisibility(View.GONE);
                adsView.setVisibility(View.GONE);
                videoView.stopPlayback();

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
                adsHandler.removeMessages(0);
                adsHandler.sendEmptyMessage(0);

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