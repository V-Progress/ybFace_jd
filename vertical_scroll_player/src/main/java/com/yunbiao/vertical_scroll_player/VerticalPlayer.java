package com.yunbiao.vertical_scroll_player;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.yunbiao.vertical_scroll_player.adapter.PlayViewAdapter;
import com.yunbiao.vertical_scroll_player.custom.TextureVideoView;
import com.yunbiao.vertical_scroll_player.holder.MyViewHolder;
import com.yunbiao.vertical_scroll_player.holder.VideoHolder;
import com.yunbiao.vertical_scroll_player.listener.InteractionListener;
import com.yunbiao.vertical_scroll_player.listener.InteractionUpdateListener;
import com.yunbiao.vertical_scroll_player.listener.OnViewPagerListener;
import com.yunbiao.vertical_scroll_player.listener.PlaySet;
import com.yunbiao.vertical_scroll_player.listener.UpdateWatchListener;
import com.yunbiao.vertical_scroll_player.manager.PagerLayoutManager;
import com.yunbiao.vertical_scroll_player.model.PlayElement;

import java.util.ArrayList;
import java.util.List;

public class VerticalPlayer extends FrameLayout {
    private static final String TAG = "VerticalPlayer";

    public static int TYPE_IMG = 0;
    public static int TYPE_VIDEO = 1;

    private int playTime = 5;//播放时长
    private int mPlayIndex = -1;//播放索引
    private int mStopIndex = -1;//停止索引（上一个item）
    private List<PlayElement> playList = new ArrayList<>();
    private PlayViewAdapter playViewAdapter;
    private RecyclerView recyclerView;
    private InteractionListener interactionListener;
    private UpdateWatchListener updateWatchListener;
    private ImageSet imageSet;

    private int backgroundColor = Color.TRANSPARENT;

    public VerticalPlayer(Context context) {
        this(context, null);
    }

    public VerticalPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        this.setBackgroundColor(backgroundColor);
        PagerLayoutManager layoutManager = new PagerLayoutManager(getContext(), RecyclerView.VERTICAL);
        if (onViewPagerListener != null) {
            layoutManager.setOnViewPagerListener(onViewPagerListener);
        }

        recyclerView = new RecyclerView(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        this.addView(recyclerView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        playViewAdapter = new PlayViewAdapter(playList);
        recyclerView.setAdapter(playViewAdapter);
    }

    public void setImageSet(ImageSet imageSet) {
        this.imageSet = imageSet;
    }

    public void setBackground(int backgroundColor){
        this.backgroundColor = backgroundColor;
        setBackgroundColor(backgroundColor);
    }

    /***
     * 暂停，获取当前播放的内容并停止
     */
    public void pause(){
        MyViewHolder viewHolderByPosition = getViewHolderByPosition(mPlayIndex);
        if(viewHolderByPosition == null){
            return;
        }
        viewHolderByPosition.stop();
        removeCallbacks(finishRunnable);
    }

    /***
     * 返回，获取当前内容并开始播放
     */
    public void resume(){
        MyViewHolder viewHolderByPosition = getViewHolderByPosition(mPlayIndex);
        if(viewHolderByPosition == null){
            return;
        }
        viewHolderByPosition.play(playSet);
    }

    /***
     * 释放资源
     */
    public void release(){
        playList.clear();
        playViewAdapter.notifyDataSetChanged();
        playViewAdapter = null;
    }

    /***
     * 设置播放数据，为空时则不播放
     * @param es
     */
    public void setData(List<PlayElement> es) {
        playList.clear();
        mPlayIndex = -1;
        mStopIndex = -1;
        if (es == null) {
            return;
        }
        playList.addAll(es);
        playViewAdapter.notifyDataSetChanged();
    }

    /***
     * 设置图片播放时长
     * @param seconds
     */
    public void setPlayTime(int seconds) {
        if (seconds > 2) {
            playTime = seconds;
        }
    }

    /***
     * 设置互动按钮监听
     * @param listener
     */
    public void setInteractionListener(InteractionListener listener) {
        this.interactionListener = listener;
    }

    public void setUpdateWatchListener(UpdateWatchListener updateWatchListener) {
        this.updateWatchListener = updateWatchListener;
    }

    private OnViewPagerListener onViewPagerListener = new OnViewPagerListener() {
        @Override
        public void onInitComplete() {
            //防止重复调用
            if (mPlayIndex != -1) {
                return;
            }
            mPlayIndex = 0;
            startPlay(0);
        }

        @Override
        public void onPageSelected(int position, boolean isBottom) {
            //防止重复调用
            if (mPlayIndex == position) {
                return;
            }
            mPlayIndex = position;
            startPlay(position);
        }

        @Override
        public void onPageRelease(boolean isNext, int position) {
            //防止重复调用
            if (mStopIndex == position) {
                return;
            }
            mStopIndex = position;
            stopPlay(position);
        }
    };

    /***
     * 开始播放
     * @param position
     */
    private void startPlay(int position) {
        MyViewHolder childViewHolder = getViewHolderByPosition(position);
        childViewHolder.bindInteractionListener(updateListener);
        childViewHolder.play(playSet);
    }

    /***
     * 播放内容设置
     */
    private PlaySet playSet = new PlaySet() {
        @Override
        public void set(PlayElement element, View view,TextView tvWatchNum) {
            //开始前先清除定时事件
            removeCallbacks(finishRunnable);
            if (element.getResourceType() == TYPE_IMG) {//图片播放
                if(imageSet != null){
                    //图片需要外部设置
                    imageSet.set(element.getResourcePath(), (ImageView) view);
                }
                postDelayed(finishRunnable, playTime * 1000);//开始定时切换
            } else {//视频播放
                TextureVideoView videoView = (TextureVideoView) view;
                videoView.setVideoPath(element.getResourcePath());
                videoView.start();
                //设置播放完毕监听
                OkListener okListener = new OkListener(finishRunnable);
                videoView.setOnCompletionListener(okListener);
                videoView.setOnErrorListener(okListener);
            }
            if(updateWatchListener != null){
                //更新播放数量
                updateWatchListener.update(element,tvWatchNum);
            }
        }
    };

    /***
     * 更新监听，页面各事件的点击监听
     */
    private InteractionUpdateListener updateListener = new InteractionUpdateListener() {
        @Override
        public void updateView(final PlayElement element, final View playView, final View good, final TextView gooNum,final View ivWatch,final View share) {
            OnTouchListener touchListener = new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        int i = v.getId();
                        if (i == R.id.play_view) {//点击屏幕
                            interactionListener.clickView(element, playView);
                        } else if (i == R.id.iv_good) {//点赞
                            interactionListener.clickThumbsUp(element, good, gooNum);
                        }else if(i == R.id.iv_watch){//点击播放按钮
                            interactionListener.clickWatch(element,ivWatch);
                        } else if (i == R.id.iv_share) {//点击分享
                            interactionListener.clickShare(element, share);
                        }
                    }
                    return true;
                }
            };
            playView.setOnTouchListener(touchListener);
            good.setOnTouchListener(touchListener);
            ivWatch.setOnTouchListener(touchListener);
            share.setOnTouchListener(touchListener);
        }
    };

    /**
     * 停止播放
     * @param releaseIndex
     */
    private void stopPlay(int releaseIndex) {
        MyViewHolder holder = getViewHolderByPosition(releaseIndex);
        if (holder == null) {
            return;
        }
        if (holder.getElement().getResourceType() == TYPE_VIDEO) {
            VideoHolder videoHolder = (VideoHolder) holder;
            videoHolder.stop();
        }
    }

    /**
     * 获取某个position下的ViewHolder
     */
    private MyViewHolder getViewHolderByPosition(int position) {
        View currChildView = recyclerView.getLayoutManager().findViewByPosition(position);
        try {
            return (MyViewHolder) recyclerView.getChildViewHolder(currChildView);
        } catch (Exception e) {
            return null;
        }
    }

    /***
     * 切换
     */
    private Runnable finishRunnable = new Runnable() {
        @Override
        public void run() {
            if (playList.size() <= 0) {
                return;
            }
            int nextIndex = (mPlayIndex + 1) >= playViewAdapter.getItemCount() ? 0 : mPlayIndex + 1;
            recyclerView.smoothScrollToPosition(nextIndex);
        }
    };

    class OkListener implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener{
        private Runnable finishRunnable;

        public OkListener(Runnable runnable) {
            this.finishRunnable = runnable;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            if (this.finishRunnable != null) {
                this.finishRunnable.run();
            }
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            if (this.finishRunnable != null) {
                this.finishRunnable.run();
            }
            return true;
        }
    }

    public interface ImageSet{
        void set(String path, ImageView imageView);
    }

    private void d(String log) {
        Log.d(TAG, log);
    }

}
