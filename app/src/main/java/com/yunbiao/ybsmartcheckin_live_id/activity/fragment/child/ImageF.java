package com.yunbiao.ybsmartcheckin_live_id.activity.fragment.child;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.yunbiao.ybsmartcheckin_live_id.R;

public class ImageF extends BaseF {
    private static final String KEY = "url";

    private String mUrl;
    private ImageView imageView;

    public ImageF() {
    }

    public static ImageF newInstance(String url) {
        ImageF fragment = new ImageF();
        Bundle args = new Bundle();
        args.putString(KEY, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageView = getView().findViewById(R.id.image_view);
        if (getArguments() != null) {
            mUrl = getArguments().getString(KEY);
            Glide.with(getActivity()).load(mUrl).asBitmap().into(imageView);
        }
    }

    private static final String TAG = "ImageF";

    @Override
    public void start() {
        Log.e(TAG, "onResume: ----- 已设置定时任务");
        if(imageView != null){
            imageView.removeCallbacks(switchRunnable);
            imageView.postDelayed(switchRunnable,getTime() * 1000);
        }
    }

    @Override
    public void stop() {
        Log.e(TAG, "onPause: ----- 已清除定时任务");
        if(imageView != null){
            imageView.removeCallbacks(switchRunnable);
        }
    }
}
