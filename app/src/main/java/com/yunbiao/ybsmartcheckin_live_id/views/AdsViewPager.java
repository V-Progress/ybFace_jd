package com.yunbiao.ybsmartcheckin_live_id.views;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.yunbiao.ybsmartcheckin_live_id.utils.FileUtils;

import java.util.List;

/**
 * Created by Administrator on 2019/5/24.
 */

public class AdsViewPager extends ViewPager {

    private ImageView iv;
    private VideoView vv;
    private List<String> mList;
    private Context mCtx;

    public final int TAG_START = 0;
    public final int TAG_STOP = -1;
    private int playTime = 10;
    private int TAG = TAG_START;

    private Handler timerHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TAG_START:

                    break;
                case TAG_STOP:

                    break;
            }

            timerHandler.sendEmptyMessageDelayed(TAG,playTime * 1000);
        }
    };

    public AdsViewPager(Context context) {
        super(context);
        init(context);
    }

    public AdsViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setData(List<String> adsList){
        mList = adsList;
        if(pagerAdapter != null){
            pagerAdapter.notifyDataSetChanged();
        }
    }

    public void setPlayTime(int time){
        playTime = time;
    }

    private void init(Context context){
        mCtx = context;
        iv = new ImageView(context);
        iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        vv = new VideoView(context);
        setOffscreenPageLimit(1);
        setNestedScrollingEnabled(false);
        setAdapter(pagerAdapter);
        timerHandler.sendEmptyMessage(TAG);
    }

    public void startPlay(){
        TAG = TAG_START;
        timerHandler.sendEmptyMessageDelayed(TAG,playTime * 1000);
    }

    public void stopPlay(){
        TAG = TAG_STOP;
        timerHandler.sendEmptyMessage(TAG);
    }

    PagerAdapter pagerAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view;
            String adsPath = mList.get(position);
            if(FileUtils.isVideo(adsPath)){
                vv.setVideoPath(adsPath);
                vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                    }
                });
                vv.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        return false;
                    }
                });
                view = vv;
            } else {
                Glide.with(mCtx).load(adsPath).asBitmap().into(iv);
                view = iv;
            }
            return view;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    };

}
