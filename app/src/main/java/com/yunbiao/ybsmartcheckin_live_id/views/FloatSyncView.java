package com.yunbiao.ybsmartcheckin_live_id.views;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    private TextView tvInfo;
    private TextView tvProgress;
    private ProgressBar pbDownload;

    public FloatSyncView(Context context) {
        mCtx = context;
        init();
    }

    private void init(){
        // 获取WindowManager服务
        windowManager = (WindowManager) APP.getContext().getSystemService(Context.WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
//        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        if (Build.VERSION.SDK_INT >= 24) { /*android7.0不能用TYPE_TOAST*/
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        } else { /*以下代码块使得android6.0之后的用户不必再去手动开启悬浮窗权限*/
            String packname =  APP.getContext().getPackageName();
            PackageManager pm =  APP.getContext().getPackageManager();
            boolean permission = (PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.SYSTEM_ALERT_WINDOW", packname));
            if (permission) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            }
        }

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
        tvInfo = rootView.findViewById(R.id.tv_info);
        tvProgress = rootView.findViewById(R.id.tv_progress);
//        pbDownload = rootView.findViewById(R.id.pb_download);
    }

    public void show(){
        try {
            windowManager.addView(rootView, layoutParams);
        }catch (Exception e){
//            windowManager.removeView(rootView);
//            show();
            e.printStackTrace();
        }
    }

    public void dismiss(){
        if(rootView.isAttachedToWindow()){
            windowManager.removeView(rootView);
        }
    }

    public void setNormalInfo(String info){
        if(tvInfo != null){
            tvInfo.setTextColor(Color.parseColor("#2d2a2a"));
            tvInfo.setText(info);
        }
    }

    public void setErrInfo(String err){
        if(tvInfo != null){
            tvInfo.setTextColor(Color.parseColor("#ff0000"));
            tvInfo.setText(err);
        }
    }

    public void setTvProgress(String progress){
        if(tvProgress != null){
            tvProgress.setText(progress);
        }
    }

    public void setDownloadProgress(int curr,int total){
        if(pbDownload != null){
            pbDownload.setMax(total);
            pbDownload.setProgress(curr);
        }
    }

    public void showProgress(boolean isVisible){
        if(pbDownload != null){
            pbDownload.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
        if(tvProgress != null){
            tvProgress.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }
}
