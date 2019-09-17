package com.yunbiao.ybsmartcheckin_live_id.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.yunbiao.ybsmartcheckin_live_id.R;

public class SyncDialog {
    private static final String TAG = "SyncDialog";
    private static SyncDialog syncDialog;
    private static Activity act;
    private SDialog dialog;
    private TextView tvInfo;
    private TextView tvProgress;
    private View rootView;

    public synchronized static SyncDialog instance(){
        if(syncDialog == null){
            syncDialog = new SyncDialog();
        }
        return syncDialog;
    }

    private SyncDialog(){
    }

    public void init(Activity activity){
        act = activity;
        initDialog();
        initView();
    }

    private void initDialog(){
        dialog = new SDialog(act);
    }

    private void initView(){
        if(dialog == null){
            return;
        }
        rootView = dialog.findViewById(R.id.ll_root);
        tvInfo = dialog.findViewById(R.id.tv_info);
        tvProgress = dialog.findViewById(R.id.tv_progress);
    }

    public void show(){
        if(act == null){
            Log.e(TAG, "未进行初始化" );
            return;
        }
        if(dialog == null){
            return;
        }
        if(dialog.isShowing()){
            dialog.dismiss();
        }
        dialog.show();
    }

    public boolean isShown(){
        return dialog != null && dialog.isShowing();
    }

    public void dismiss(){
        if(act == null){
            Log.e(TAG, "未进行初始化" );
            return;
        }

        if(dialog == null){
            return;
        }

        setStep("同步完成");
        setProgress(0,0);

        if(dialog.isShowing()){
            if(rootView != null){
                rootView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                },2 * 1000);
            } else {
                dialog.dismiss();
            }
        }
    }

    public void setStep(final String step){
        if(tvInfo != null){
            tvInfo.post(new Runnable() {
                @Override
                public void run() {
                    tvInfo.setText(step);
                }
            });
        }
    }

    public void setProgress(final int progress, final int max){
        if(tvProgress != null){
            tvProgress.post(new Runnable() {
                @Override
                public void run() {
                    if(progress == 0 && max == 0){
                        tvProgress.setText("");
                        return;
                    }
                    tvProgress.setText(progress + " / " + max);
                }
            });
        }
    }
}
