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

import com.stx.xhb.xbanner.transformers.CubePageTransformer;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.child.VideoFragment;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.child.WebFragment;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.data.IntroLoader;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.data.LoadListener;
import com.yunbiao.ybsmartcheckin_live_id.business.NoticeManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class InformationFragment extends Fragment {
    private static final String TAG = "InformationFragment";

    private ViewPager vpInfomation;
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
        vpInfomation.setOffscreenPageLimit(2);
        vpInfomation.setPageTransformer(false, new CubePageTransformer(18F));//也可自定义动画范围大小new CubePageTransformer(90f)

        vpPagerAdapter = new MyAdapter(getChildFragmentManager(), fragments);
        vpInfomation.setAdapter(vpPagerAdapter);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent event) {
        NoticeManager.getInstance().initSignData();

        IntroLoader.loadData(loadListener);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(InformationUpdateEvent event) {
        Log.e(TAG, "update: 收到宣传目录更新事件");

        IntroLoader.loadData(loadListener);
    }

    private List<Fragment> fragments = new ArrayList<>();
    private LoadListener loadListener = new LoadListener() {
        @Override
        public void onStart() {
            Log.e(TAG, "onStart: ");
            fragments.clear();
            vpPagerAdapter.notifyDataSetChanged();

            pbLoad.setVisibility(View.VISIBLE);
            ivBg.setVisibility(View.VISIBLE);
        }

        @Override
        public void onFailed(Exception e) {
            Log.e(TAG, "onFailed: " + (e == null ? "NULL" : e.getMessage()));
            pbLoad.setVisibility(View.GONE);
        }

        @Override
        public void onFinish(){
            pbLoad.setVisibility(View.GONE);
        }

        @Override
        public void onStarLoadCache() {
            Log.e(TAG, "onStarLoadCache: " );
        }

        @Override
        public void onLoadSuccess(IntroLoader.PlayBean bean) {
            Log.e(TAG, "getSingle: " + bean.toString());
            if (bean.getType() == IntroLoader.TYPE_URL) {
                fragments.add(WebFragment.newInstance(bean.getUrl()));
            } else if (bean.getType() == IntroLoader.TYPE_IMAGE_VIDEO) {
                fragments.add(VideoFragment.newInstance(bean.getName(), bean.getTime(), bean.getPathList()));
            }
        }

        @Override
        public void onLoadFinish() {
            Log.e(TAG, "loadFinish: " );
            d("全部处理结束... ");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    vpPagerAdapter.notifyDataSetChanged();
                    pbLoad.setVisibility(View.GONE);
                    ivBg.setVisibility(fragments.size() <= 0 ? View.VISIBLE : View.GONE);
                }
            });
        }

        @Override
        public void onNoData() {
            Log.e(TAG, "onNoData: ");
        }
    };

    private class MyAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;

        public MyAdapter(FragmentManager fm, List<Fragment> fragmentList) {
            super(fm);
            fragments = fragmentList;
        }

        @Override
        public long getItemId(int position) {
            return fragments.get(position).hashCode();
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    private void runOnUiThread(Runnable runnable) {
        getActivity().runOnUiThread(runnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private void d(String msg) {
        Log.d(TAG, msg);
    }

    public static class InformationUpdateEvent {

    }
}
