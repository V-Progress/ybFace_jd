package com.yunbiao.ybsmartcheckin_live_id.activity.fragment.child;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.InfoTouchEvent;

import org.greenrobot.eventbus.EventBus;

public class WebFragment extends Fragment implements View.OnTouchListener {
    private static final String TAG = "WebFragment";
    private static final String KEY_URL = "url";
    private WebView webView;
    private ProgressBar progressBar;
    private View tvGoback;
    private View tvRefresh;

    public WebFragment() {
    }

    public static WebFragment newInstance(String url) {
        WebFragment fragment = new WebFragment();
        Bundle args = new Bundle();
        args.putString(KEY_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_web, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar = getView().findViewById(R.id.pb_progress);
        progressBar.setMax(100);
        webView = getView().findViewById(R.id.wv_web);
        initWebView();

        if (getArguments() != null) {
            String url = getArguments().getString(KEY_URL);
            load(url);
        }
        tvGoback = getView().findViewById(R.id.tv_goback);
        tvGoback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.goBack();
            }
        });
        tvRefresh = getView().findViewById(R.id.tv_refresh);
        tvRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });
        tvGoback.setOnTouchListener(this);
        tvRefresh.setOnTouchListener(this);
    }

    private void initWebView() {
        WebSettings settings = webView.getSettings();
        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        settings.setJavaScriptEnabled(true);
        // 若加载的 html 里有JS 在执行动画等操作，会造成资源浪费（CPU、电量）
        // 在 onStop 和 onResume 里分别把 setJavaScriptEnabled() 给设置成 false 和 true 即可

        //支持插件
        settings.setPluginState(WebSettings.PluginState.ON);
        //设置自适应屏幕，两者合用
        settings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        settings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        //缩放操作
        settings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        settings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        settings.setDisplayZoomControls(false); //隐藏原生的缩放控件

        //其他细节操作
        settings.setCacheMode(WebSettings.LOAD_DEFAULT); //默认缓存
        settings.setAllowFileAccess(true); //设置可以访问文件
        settings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        settings.setLoadsImagesAutomatically(true); //支持自动加载图片
        settings.setDefaultTextEncodingName("utf-8");//设置编码格式
        webView.setWebChromeClient(new WebChromeClient(){
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress < 100){
                    if(!progressBar.isShown()){
                        progressBar.setVisibility(View.VISIBLE);
                    }
                } else {
                    if(progressBar.isShown()){
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            EventBus.getDefault().postSticky(new InfoTouchEvent());
        }
        return false;
    }

    private void load(String url){
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }
        webView.loadUrl(url);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(webView != null){
            webView.onResume();
            webView.resumeTimers();
            webView.reload();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(webView != null){
            webView.onPause();
            webView.pauseTimers();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(webView != null){
            webView.stopLoading();
            webView.destroy();
        }
    }

}
