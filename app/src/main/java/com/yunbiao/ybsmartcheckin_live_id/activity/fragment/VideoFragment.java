package com.yunbiao.ybsmartcheckin_live_id.activity.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.AdsStateEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.InfoTouchEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class VideoFragment extends Fragment {
    private static final String TAG = "VideoFragment";
    private static final String KEY_PATHLIST = "pathList";
    private static final String KEY_TIME = "time";

    private int mTime = 10;
    private ArrayList<IntroLoader.PlayBean.PathBean> pathBeans;
    private ViewPager2 vp2;
    private VideoAdapter videoAdapter;
    private TextView tvPosition;

    public VideoFragment() {
    }

    public static VideoFragment newInstance(String name,int time, ArrayList<IntroLoader.PlayBean.PathBean> pathList) {
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
        EventBus.getDefault().register(this);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
        vp2 = getView().findViewById(R.id.vp2);
        tvPosition = getView().findViewById(R.id.tv_position);
        vp2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        vp2.setOffscreenPageLimit(1);
        vp2.registerOnPageChangeCallback(onPageChangeCallback);
        vp2.setOnTouchListener(onTouchListener);
    }

    private void initData() {
        mTime = getArguments().getInt(KEY_TIME);
        pathBeans = (ArrayList<IntroLoader.PlayBean.PathBean>) getArguments().getSerializable(KEY_PATHLIST);
        loadData(pathBeans);
    }

    private void loadData(ArrayList<IntroLoader.PlayBean.PathBean> pathBeans) {
        videoAdapter = new VideoAdapter(this, pathBeans);
        vp2.setAdapter(videoAdapter);
    }

    private ViewPager2.OnPageChangeCallback onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(final int position) {
            if(videoAdapter!= null){
                tvPosition.setText((position+1) + " / " + videoAdapter.getItemCount());
            }
            BaseF fragment = videoAdapter.getFragment(position);
            fragment.setSwitcher(new BaseF.Switcher() {
                @Override
                public void onSwitch() {
                    switchPage(position);
                }
            });
        }
    };

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            startPlay();
        } else {
            stopPlay();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(AdsStateEvent adsStateEvent){
        if(adsStateEvent.state == AdsStateEvent.STATE_CLOSED){
            Log.e(TAG, "update: ----- 广告打开");
            stopPlay();
        } else {
            Log.e(TAG, "update: ----- 广告关闭");
            startPlay();
        }
    }

    private void stopPlay(){
        Log.e(TAG, "stopPlay: ----- 停止播放" );
        if(vp2 != null && videoAdapter != null){
            int currentItem = vp2.getCurrentItem();
            BaseF fragment = videoAdapter.getFragment(currentItem);
            fragment.stop();
        }
    }

    private void startPlay(){
        Log.e(TAG, "stopPlay: ----- 开始播放" );
        if(vp2 != null && videoAdapter != null){
            int currentItem = vp2.getCurrentItem();
            BaseF fragment = videoAdapter.getFragment(currentItem);
            fragment.start();
        }
    }

    private void switchPage(int currPosition) {
        if (videoAdapter != null) {
            int itemCount = videoAdapter.getItemCount();
            int nextPosition = currPosition + 1;
            if (nextPosition >= itemCount) {
                nextPosition = 0;
            }
            if (nextPosition == 0) {
                vp2.setCurrentItem(nextPosition);
            } else {
                vp2.setCurrentItem(nextPosition, true);
            }
        }
    }

    class VideoAdapter extends FragmentStateAdapter {
        private List<BaseF> fragmentList = new ArrayList<>();

        public VideoAdapter(@NonNull Fragment fragment, ArrayList<IntroLoader.PlayBean.PathBean> pathBeans) {
            super(fragment);
            if (pathBeans == null) {
                return;
            }
            for (IntroLoader.PlayBean.PathBean pathBean : pathBeans) {
                BaseF baseF;
                if (pathBean.getType() == IntroLoader.PlayBean.PathBean.TYPE_VIDEO) {
                    baseF = VideoF.newInstance(pathBean.getLocalPath());
                } else {
                    baseF = ImageF.newInstance(pathBean.getLocalPath());
                }
                baseF.setTime(mTime);
                baseF.setType(pathBean.getType());
                fragmentList.add(baseF);
            }
        }

        public BaseF getFragment(int position) {
            return fragmentList.get(position);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getItemCount() {
            return fragmentList.size();
        }
    }


    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                EventBus.getDefault().postSticky(new InfoTouchEvent());
            }
            return false;
        }
    };
}
