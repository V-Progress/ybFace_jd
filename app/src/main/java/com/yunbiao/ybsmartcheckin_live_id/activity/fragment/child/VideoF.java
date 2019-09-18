package com.yunbiao.ybsmartcheckin_live_id.activity.fragment.child;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.views.TextureVideoView;

public class VideoF extends BaseF {
    private static final String KEY = "url";

    private String mUrl;
    private TextureVideoView videoView;

    public VideoF() {
    }

    public static VideoF newInstance(String url) {
        VideoF fragment = new VideoF();
        Bundle args = new Bundle();
        args.putString(KEY, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        videoView = getView().findViewById(R.id.video_view);

        if (getArguments() != null) {
            mUrl = getArguments().getString(KEY);
            videoView.setVideoPath(mUrl);
        }
    }

    @Override
    public void start() {
        videoView.resume();
        videoView.start();
        setVideoListener(true);
    }

    @Override
    public void stop() {
        videoView.stopPlayback();
        setVideoListener(false);
    }

    private void setVideoListener(boolean is){
        MediaPlayer.OnCompletionListener onCompletionListener = null;
        MediaPlayer.OnErrorListener onErrorListener = null;
        if(is){
            onCompletionListener = new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(switchRunnable != null){
                        switchRunnable.run();
                    }
                }
            };
            onErrorListener = new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if(switchRunnable != null){
                        switchRunnable.run();
                    }
                    return true;
                }
            };
        }
        videoView.setOnCompletionListener(onCompletionListener);
        videoView.setOnErrorListener(onErrorListener);
    }

}
