package com.yunbiao.ybsmartcheckin_live_id.utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.common.power.PowerOffTool;

/**
 * Created by Administrator on 2019/4/18.
 */

public class UIUtils {

    private static android.widget.Toast mToast;

    public static void show(final Activity context, final String message, final int time){
        if(context == null){
            return;
        }

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mToast != null){
                    mToast.cancel();
                }

                mToast = new android.widget.Toast(context);
                View toastView = View.inflate(context, R.layout.layout_toast, null);
                TextView tvToast = (TextView) toastView.findViewById(R.id.tv_toast);
                tvToast.setText(message);
                mToast.setView(toastView);
                mToast.setGravity(Gravity.CENTER,0,0);
                mToast.setDuration(time);
                mToast.show();
            }
        });
    }

    public static void showShort(Activity context,String message){
        show(context,message, 5 * 1000);
    }

    public static void showLong(Activity context,String message){
        show(context,message, 10 * 1000);
    }

    private static Toast mTipsToast;
    public static void showTitleTip(Context context,String title) {
        if(mTipsToast != null){
            mTipsToast.cancel();
            mTipsToast = null;
        }
        int padding = 20;
        TextView textView = new TextView(context);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setBackgroundColor(Color.WHITE);
        textView.setTextSize(36);
        textView.setText(title);
        textView.setPadding(padding,padding,padding,padding);
        textView.setGravity(Gravity.CENTER);
        textView.setElevation(10);

        mTipsToast = new android.widget.Toast(context);
        mTipsToast.setDuration(android.widget.Toast.LENGTH_LONG);
        mTipsToast.setGravity(Gravity.CENTER, 0, 0);
        mTipsToast.setView(textView);
        mTipsToast.show();
    }


    public static ProgressDialog pd;
    // CoreInfoHandler 关机重启三秒等待
    public static ProgressDialog coreInfoShow3sDialog(Context context) {
        ProgressDialog pd = new ProgressDialog(context);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setIndeterminate(false);
        pd.setCancelable(false); // 设置ProgressDialog 是否可以按退回键取消
        return pd;
    }

    /**
     * 软件升级
     */
    public static void updatePd(Context context) {
        pd = new ProgressDialog(context);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setTitle("通知");
        pd.setMessage("正在安装相关应用，请耐心等待！");
        pd.show();

        Window window = pd.getWindow();
        window.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.TOP);
        window.setWindowAnimations(R.style.mystyle);
    }

    public static CountDownTimer restart = new CountDownTimer(3 * 1000, 1000) {//3秒
        @Override public void onTick(long millisUntilFinished) {}
        @Override public void onFinish() {
            // 重启
            PowerOffTool.getPowerOffTool().restart();
        }
    };

    public static CountDownTimer powerShutDown = new CountDownTimer(3 * 1000, 1000) {//3秒
        @Override public void onTick(long millisUntilFinished) {}
        @Override public void onFinish() {
            // 关机
            PowerOffTool.getPowerOffTool().powerShutdown();
        }
    };


    private static Dialog dialog;
    public static void showNetLoading(Context context){
        dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(Color.TRANSPARENT);
        AVLoadingIndicatorView avLoadingIndicatorView = new AVLoadingIndicatorView(context);
        avLoadingIndicatorView.setBackgroundColor(Color.TRANSPARENT);
        frameLayout.addView(avLoadingIndicatorView,new FrameLayout.LayoutParams(300,200, Gravity.CENTER));
        dialog.setContentView(frameLayout,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog.setCancelable(false);
        dialog.show();
    }

    public static void dismissNetLoading(){
        if(dialog != null){
            dialog.dismiss();
        }
    }

}
