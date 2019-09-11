package com.yunbiao.ybsmartcheckin_live_id.activity.fragment.child;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import com.dingmouren.layoutmanagergroup.viewpager.OnViewPagerListener;
import com.dingmouren.layoutmanagergroup.viewpager.ViewPagerLayoutManager;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.InfoTouchEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.InformationFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class VideoFragment extends Fragment implements View.OnTouchListener {
    private static final String TAG = "VideoFragment";
    private static final String KEY_PATHLIST = "pathList";
    private static final String KEY_TIME = "time";
    private RecyclerView rlv;
    private MediaAdapter myAdapter;

    private int mTime = 10;
    private boolean mIsVisibleToUser = false;
    private ArrayList<InformationFragment.PlayBean.PathBean> pathBeans;
    private ViewPagerLayoutManager vlManager;

    public VideoFragment() {
    }

    public static VideoFragment newInstance(int time, ArrayList<InformationFragment.PlayBean.PathBean> pathList) {
        VideoFragment fragment = new VideoFragment();
        Bundle args = new Bundle();
        args.putSerializable(KEY_PATHLIST, pathList);
        args.putInt(KEY_TIME, time);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();

        if (getArguments() == null) {
            return;
        }

        initData();
    }

    private void initView() {
        rlv = getView().findViewById(R.id.rlv);
        vlManager = new ViewPagerLayoutManager(getContext(), OrientationHelper.VERTICAL, false);
        rlv.setLayoutManager(vlManager);
        rlv.setOnTouchListener(this);
    }

    private void initData() {
        mTime = getArguments().getInt(KEY_TIME);
        pathBeans = (ArrayList<InformationFragment.PlayBean.PathBean>) getArguments().getSerializable(KEY_PATHLIST);
        loadData(pathBeans);
    }

    private void loadData(ArrayList<InformationFragment.PlayBean.PathBean> pathBeans) {
        myAdapter = new MediaAdapter(getActivity(), pathBeans, rlv, mTime);
        rlv.setAdapter(myAdapter);
        vlManager.setOnViewPagerListener(onViewPagerListener);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsVisibleToUser = isVisibleToUser;
    }

    private OnViewPagerListener onViewPagerListener = new OnViewPagerListener() {
        @Override
        public void onInitComplete() {
            if(mIsVisibleToUser){
                play(0);
            }
        }

        @Override
        public void onPageRelease(boolean isNext, int position) {
            Log.e(TAG, "释放位置:" + position + " 下一页:" + isNext);
            int index;
            if (isNext) {
                index = 0;
            } else {
                index = 1;
            }
            stop(index);
        }

        @Override
        public void onPageSelected(int position, boolean isBottom) {
            Log.e(TAG, "当前界面:" + position + " 是否底部:" + isBottom);
            switchRunnable.setPosition(position);
            play(0);
        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            EventBus.getDefault().postSticky(new InfoTouchEvent());
        }
        return false;
    }

    private void play(int position) {
        //开始，如果是视频则开始播放，如果是图片则开始定时任务
        View itemView = rlv.getChildAt(position);
        VideoView vvInfo = itemView.findViewById(R.id.vv_info);
        if(vvInfo.isShown()){
            vvInfo.start();
            vvInfo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    rlv.post(switchRunnable);
                }
            });
            vvInfo.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    rlv.post(switchRunnable);
                    return true;
                }
            });
        } else {
            rlv.removeCallbacks(switchRunnable);
            rlv.postDelayed(switchRunnable, mTime * 1000);
        }
    }

    private void stop(int position){
        View itemView = rlv.getChildAt(position);
        //开始，如果是视频则开始播放，如果是图片则开始定时任务
        VideoView vvInfo = itemView.findViewById(R.id.vv_info);
        Object tag = vvInfo.getTag();
        if(tag != null){
            vvInfo.stopPlayback();
        } else {

        }
    }

    //自动切换的操作
    private SwitchRunnable switchRunnable = new SwitchRunnable();

    class SwitchRunnable implements Runnable {
        private int mPosition = 0;
        public void setPosition(int position){
            mPosition = position;
        }
        @Override
        public void run() {
            int nextPosition = getNextPosition(mPosition);
            Log.e(TAG, "跳转到：" + nextPosition);
            if (nextPosition == 0) {
                rlv.scrollToPosition(nextPosition);
            } else {
                rlv.smoothScrollToPosition(nextPosition);
            }
        }
    }

    /***
     * 获取下一个指针
     * @param position
     * @return
     */
    public int getNextPosition(int position) {
        position += 1;
        if (position >= pathBeans.size()) {
            position = 0;
        }
        return position;
    }

}
