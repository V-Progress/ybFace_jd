package com.yunbiao.ybsmartcheckin_live_id.activity.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stx.xhb.xbanner.transformers.CubePageTransformer;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateMediaEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.child.VideoFragment;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.child.WebFragment;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.data.IntroLoader;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.data.LoadListener;
import com.yunbiao.ybsmartcheckin_live_id.business.NoticeManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class InformationFragment extends Fragment {
    private static final String TAG = "InformationFragment";

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

        NoticeManager.getInstance().init(rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vpPagerAdapter = new MyAdapter(getChildFragmentManager(), fragments);
        vpInfomation.setOffscreenPageLimit(2);
        vpInfomation.setPageTransformer(false, new CubePageTransformer(18F));//也可自定义动画范围大小new CubePageTransformer(90f)
        vpInfomation.setAdapter(vpPagerAdapter);

        NoticeManager.getInstance().initSignData();
    }

    private class MyAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;

        public MyAdapter(FragmentManager fm, List<Fragment> fragmentList) {
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

    private void initData() {
        IntroLoader.loadData(loadListener);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateMediaEvent event) {
        initData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(InformationUpdateEvent event) {
        Log.e(TAG, "update: 收到更新信息事件");
        IntroLoader.loadData(loadListener);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent event) {
        NoticeManager.getInstance().initSignData();
    }

    private void runOnUiThread(Runnable runnable){
        getActivity().runOnUiThread(runnable);
    }

    private LoadListener loadListener = new LoadListener(){
        @Override
        public void before() {
            Log.e(TAG, "before: ");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pbLoad.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public void loadCacheComplete() {
            Log.e(TAG, "loadCacheComplete: " );
        }

        @Override
        public void requestFailed() {
            Log.e(TAG, "requestFailed: " );
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pbLoad.setVisibility(View.GONE);
                    ivBg.setVisibility(fragments.size() <= 0 ? View.VISIBLE : View.GONE);
                }
            });
        }

        @Override
        public void loadBefore() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fragments.clear();
                    vpPagerAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void loadSingle(final IntroLoader.PlayBean bean) {
            Log.e(TAG, "getSingle: " + bean.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (bean.getType() == IntroLoader.TYPE_URL) {
                        fragments.add(WebFragment.newInstance(bean.getUrl()));
                    } else if (bean.getType() == IntroLoader.TYPE_IMAGE_VIDEO) {
                        fragments.add(VideoFragment.newInstance(bean.getName(), bean.getTime(), bean.getPathList()));
                    }
                    Log.e(TAG, "fragments: " + fragments.size());
                    Log.e(TAG, "fragment: " + fragments.toString());
                    vpPagerAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void loadFinish() {
            Log.e(TAG, "loadFinish: " );
            d("全部处理结束... ");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pbLoad.setVisibility(View.GONE);
                    ivBg.setVisibility(fragments.size() <= 0 ? View.VISIBLE : View.GONE);
                }
            });
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void d(String msg) {
        Log.d(TAG, msg);
    }

    public static class InformationUpdateEvent {

    }
}
