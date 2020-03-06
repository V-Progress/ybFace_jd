package com.yunbiao.ybsmartcheckin_live_id.activity.fragment.child;

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
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.bean.PathBean;

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
    private ArrayList<PathBean> pathBeans;
    private ViewPager2 vp2;
    private VideoAdapter videoAdapter;
    private TextView tvPosition;
//    private VerticalPlayer verticalPlayer;

    public VideoFragment() {
    }

    public static VideoFragment newInstance(String name, int time, ArrayList<PathBean> pathList) {
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
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
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
//        verticalPlayer = getView().findViewById(R.id.vertical_player);

        vp2 = getView().findViewById(R.id.vp2);
        tvPosition = getView().findViewById(R.id.tv_position);
        vp2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        vp2.setOffscreenPageLimit(1);
        vp2.registerOnPageChangeCallback(onPageChangeCallback);
        vp2.setOnTouchListener(onTouchListener);
    }

    private void initData() {
        mTime = getArguments().getInt(KEY_TIME);
        pathBeans = (ArrayList<PathBean>) getArguments().getSerializable(KEY_PATHLIST);
        loadData(pathBeans);

        if (videoAdapter != null) {
            tvPosition.setText(1 + " / " + videoAdapter.getItemCount());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(Object o){}

    private void loadData(ArrayList<PathBean> pathBeans) {
        videoAdapter = new VideoAdapter(this, pathBeans);
        vp2.setAdapter(videoAdapter);

        /*List<PlayElement> playElements = new ArrayList<>();
        for (PathBean pathBean : pathBeans) {
            PlayElement playElement = new PlayElement(pathBean.getLocalPath(),pathBean.getType());
            playElements.add(playElement);
        }
        verticalPlayer.setData(playElements);
        verticalPlayer.setImageSet(new VerticalPlayer.ImageSet() {
            @Override
            public void set(String path, ImageView imageView) {
                Glide.with(getMainActivity()).load(path).asBitmap().into(imageView);
            }
        });
        verticalPlayer.setInteractionListener(new InteractionListener() {
            @Override
            public void clickView(PlayElement element, View playView) {
                Toast.makeText(getMainActivity(), "点击了主屏幕", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void clickThumbsUp(PlayElement element, View thumbsUpView, TextView thumbsUpNumber) {
                element.setGoodNum(element.getGoodNum() + 1);
                thumbsUpNumber.setText("" + element.getGoodNum());
            }

            @Override
            public void clickWatch(PlayElement element, View ivWatch) {
                Toast.makeText(getMainActivity(), "点击了播放量", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void clickShare(PlayElement element, View thumbsUpView) {
                Toast.makeText(getMainActivity(), "分享一下", Toast.LENGTH_SHORT).show();
            }
        });
        verticalPlayer.setUpdateWatchListener(new UpdateWatchListener() {
            @Override
            public void update(PlayElement element, TextView tvWatchNumber) {
                element.setWatchNum(element.getWatchNum() + 1);
                tvWatchNumber.setText("" + element.getWatchNum());
            }
        });*/
    }

    @Override
    public void onResume() {
        super.onResume();
//        verticalPlayer.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
//        verticalPlayer.pause();
    }


        private ViewPager2.OnPageChangeCallback onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(final int position) {
            if (videoAdapter != null) {
                tvPosition.setText((position + 1) + " / " + videoAdapter.getItemCount());
            }
            Log.e(TAG, "onPageSelected: 切换到 " + position);
            if (isUserVisibleHint) {
                Log.e(TAG, "onPageSelected: 页面可见，开启定时切换");
                BaseF fragment = videoAdapter.getFragment(position);
                fragment.start();
            } else {
                Log.e(TAG, "onPageSelected: 页面不可见，不做任何操作");
            }
        }
    };

    private boolean isUserVisibleHint = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isUserVisibleHint = isVisibleToUser;
        Log.e(TAG, "setUserVisibleHint: 切换到 " + isVisibleToUser);
        if (isVisibleToUser) {
            Log.e(TAG, "setUserVisibleHint: 页面可见，开启定时切换");
            startPlay();
        } else {
            Log.e(TAG, "setUserVisibleHint: 页面不可见，关闭定时切换");
            stopPlay();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(AdsStateEvent adsStateEvent) {
        Log.e(TAG, "update: 广告状态改变 " + (adsStateEvent.state == AdsStateEvent.STATE_OPENED ? "开启" : "关闭"));
        if(isUserVisibleHint){
            Log.e(TAG, "update: 页面可见，准备处理");
            if (adsStateEvent.state == AdsStateEvent.STATE_CLOSED) {
                Log.e(TAG, "update: 开启定时任务");
                startPlay();
            } else {
                Log.e(TAG, "update: 关闭定时任务");
                stopPlay();
            }
        } else {
            Log.e(TAG, "update: 页面不可见，不做任何处理");
        }
    }

    private void stopPlay() {
        if (vp2 != null && videoAdapter != null) {
            int currentItem = vp2.getCurrentItem();
            BaseF fragment = videoAdapter.getFragment(currentItem);
            fragment.stop();
        }
    }

    private void startPlay() {
        if (vp2 != null && videoAdapter != null) {
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
            vp2.setCurrentItem(nextPosition,nextPosition != 0);
        }
    }

    private BaseF.Switcher switcher = new BaseF.Switcher() {
        @Override
        public void onSwitch() {
            switchPage(vp2.getCurrentItem());
        }
    };

    class VideoAdapter extends FragmentStateAdapter {
        private List<BaseF> fragmentList = new ArrayList<>();

        public VideoAdapter(@NonNull Fragment fragment, ArrayList<PathBean> pathBeans) {
            super(fragment);
            if (pathBeans == null) {
                return;
            }
            for (PathBean pathBean : pathBeans) {
                BaseF baseF;
                if (pathBean.getType() == PathBean.TYPE_VIDEO) {
                    baseF = VideoF.newInstance(pathBean.getLocalPath());
                } else {
                    baseF = ImageF.newInstance(pathBean.getLocalPath());
                }
                baseF.setTime(mTime);
                baseF.setType(pathBean.getType());
                baseF.setSwitcher(switcher);
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
