package com.yunbiao.ybsmartcheckin_live_id.views.mixplayer;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.yunbiao.ybsmartcheckin_live_id.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class MixedPlayerLayout extends FrameLayout {

    private boolean isPaused = false;

    private List<PlayModel> datas;//总数据

    private int playTime = 10;//播放时长
    private int mCurrIndex = 0;//当前指针

    private GifImageView imageView;//图片
    private TextureVideoView videoView;//视频

    private int defaultImgId;//默认图片 img_wel_h

    public MixedPlayerLayout(Context context) {
        this(context, null);
    }

    public MixedPlayerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MixedPlayerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int mCurrentOrientation = getContext().getResources().getConfiguration().orientation;
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            defaultImgId = R.mipmap.img_wel;
        } else {
            defaultImgId = R.mipmap.img_wel_h;
        }

        initChilds();
    }

    private void initChilds() {
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);

        imageView = new GifImageView(getContext());
        addView(imageView, layoutParams);

        videoView = new TextureVideoView(getContext());
        addView(videoView, layoutParams);

        showDefaultImg();
    }

    public void setDefaultImgId(int resId) {
        if (resId > 0) {
            defaultImgId = resId;
        }
    }

    public void setDatas(List<String> dataPathList) {
        stop();//停止任务
        mCurrIndex = 0;//复位指针
        if (datas != null) {//清除数据
            datas.clear();
            datas = null;
        }
        //判断参数
        if (dataPathList == null || dataPathList.size() <= 0) {
            return;
        }
        //初始化数据
        datas = new ArrayList<>();
        for (String path : dataPathList) {
            int fileType = FileType.getFileType(path);
            if (fileType != FileType.VIDEO && fileType != FileType.IMAGE) {
                continue;
            }
            datas.add(new PlayModel(path, fileType));
        }

        if(!isPaused){
            play(0);//开始播放
        }
    }

    public void clearData() {
        stop();
        mCurrIndex = 0;
        if (datas == null) {
            return;
        }
        datas.clear();
        datas = null;
    }

    public void setPlayTime(int seconds) {
        if (seconds >= 3) {
            playTime = seconds;
        }
    }

    private void play(int playTime) {
        postDelayed(playRunnable, playTime * 1000);
    }

    private void stop() {
        if (videoView != null && videoView.isPlaying()) {
            videoView.stopPlayback();
        }
        removeCallbacks(playRunnable);
        showDefaultImg();
    }

    public void resume(){
        if(isPaused){
            isPaused = false;
            if(datas == null || datas.size() <= 0){
                return;
            }
            removeCallbacks(playRunnable);
            play(0);
        }
    }

    public void pause(){
        if(!isPaused){
            isPaused = true;
            if (videoView != null && videoView.isPlaying()) {
                videoView.pause();
            }
            removeCallbacks(playRunnable);
        }
    }

    private static final String TAG = "MixedPlayer";
    private Runnable playRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "播放-----------------" + mCurrIndex);

            if (datas == null || datas.size() <= 0) {
                Log.e(TAG, "没数据，停止");
                stop();
                return;
            }

            Log.e(TAG, "有数据");
            if (mCurrIndex >= datas.size()) {
                Log.e(TAG, "标签归零");
                mCurrIndex = 0;
            }

            PlayModel playModel = datas.get(mCurrIndex);
            mCurrIndex++;
            Log.e(TAG, "地址: " + playModel.getPath() + "，类型: " + playModel.getType());

            if (playModel.getType() == FileType.IMAGE) {
                Log.e(TAG, "是图片");
                imageView.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.GONE);
                Glide.with(getContext()).load(playModel.getPath()).asBitmap().skipMemoryCache(true).into(imageView);
                if(datas.size() <= 1){
                    Log.e(TAG, "run: 只有一张照片，不继续播放");
                    return;
                }
                play(playTime);
            } else {
                Log.e(TAG, "是视频");
                imageView.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                videoView.setVideoPath(playModel.getPath());
                videoView.start();
                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        play(0);
                    }
                });
                videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {

                        play(0);
                        return true;
                    }
                });
            }
        }
    };

    private void showDefaultImg() {
        post(() -> {
            imageView.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
            try {
                GifDrawable gifDrawable = new GifDrawable(getResources(),defaultImgId);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                gifDrawable.setLoopCount(0);
                gifDrawable.setSpeed(3.0f);
                gifDrawable.start();
                imageView.setImageDrawable(gifDrawable);
            } catch (IOException e) {
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setImageResource(defaultImgId);
            }
        });
    }
}
