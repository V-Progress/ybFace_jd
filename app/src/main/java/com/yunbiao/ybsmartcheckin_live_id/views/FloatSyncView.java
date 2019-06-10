package com.yunbiao.ybsmartcheckin_live_id.views;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.SystemClock;
import android.support.annotation.StyleableRes;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;

/**
 * Created by Administrator on 2019/5/15.
 */

public class FloatSyncView{

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private View rootView;
    private Context mCtx;
    private View llLoadingRoot;
    private View rlCountRoot;
    private View pbLoad;
    private TextView tvInfoLoad;
    private TextView tvStepLoad;
    private TextView tvStaffCount;
    private TextView tvErrLoad;
//    private ProgressBar pbDownload;

    public FloatSyncView(Context context) {
        mCtx = context;
        init();
    }

    private void init(){
        // 获取WindowManager服务
        windowManager = (WindowManager) APP.getContext().getSystemService(Context.WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.gravity = Gravity.LEFT|Gravity.BOTTOM;
        layoutParams.windowAnimations = R.style.load_pop_anim;
        initView();
    }

    private void initView(){
        rootView = View.inflate(mCtx, R.layout.layout_load_pop, null);
        rootView.setFocusable(false);
        rootView.setElevation(10f);
        llLoadingRoot = rootView.findViewById(R.id.ll_loading_root);
        rlCountRoot = rootView.findViewById(R.id.rl_count_root);
        tvStaffCount = (TextView) rootView.findViewById(R.id.tv_staff_count);
        tvErrLoad = (TextView) rootView.findViewById(R.id.tv_err_load);
        pbLoad = rootView.findViewById(R.id.pb_load);
        tvInfoLoad = (TextView) rootView.findViewById(R.id.tv_info_load);
        tvStepLoad = (TextView) rootView.findViewById(R.id.tv_step_load);
//        pbDownload = (ProgressBar) rootView.findViewById(R.id.pb_download);
        initUIState();
    }

    public void initUIState(){
        showLoadingView();//显示加载
        hideCount();//隐藏统计
        setStep("");//步骤
        setInfo("");//详情
        setErr("",false);//隐藏错误
    }

    public void hideCount(){
        rlCountRoot.setVisibility(View.GONE);
    }

    public void showCount(int localCount,int remoteCount){
        if(rlCountRoot != null && llLoadingRoot != null &&
                tvStaffCount != null){
                    rlCountRoot.setVisibility(View.VISIBLE);
                    tvStaffCount.setText(localCount + "/" + remoteCount);
        }
    }

    public void show(){
        try {
            windowManager.addView(rootView, layoutParams);
        }catch (Exception e){
            windowManager.removeView(rootView);
            show();
        }
    }

    public void dismiss(){
        if(rootView.isAttachedToWindow()){
            windowManager.removeView(rootView);
        }
    }

    public void hideLoadingView(){
        if(pbLoad != null){
            pbLoad.setVisibility(View.INVISIBLE);
        }
    }

    public void showLoadingView(){
        if(pbLoad != null){
            pbLoad.setVisibility(View.VISIBLE);
        }
    }

    public void setStep(String step){
        if(tvStepLoad != null){
            tvStepLoad.setText(step);
        }
    }

    public void setInfo(String info){
        if(tvInfoLoad != null){
            tvInfoLoad.setText(info);
        }
    }

    public void setErr(String err,boolean show){
        if(tvErrLoad != null){
            tvErrLoad.setVisibility(show?View.VISIBLE:View.GONE);
            tvErrLoad.setText(err);
        }
    }

    public void showDownloadView(boolean show){
//        if(pbDownload != null){
//            pbDownload.setVisibility(show?View.VISIBLE:View.GONE);
//        }
    }


    public void setP(int max,int p) {
//        if(pbDownload != null){
//            pbDownload.setMax(max);
//            pbDownload.setProgress(p);
//        }
    }
}
