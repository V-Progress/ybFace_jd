package com.yunbiao.ybsmartcheckin_live_id.views;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;

public class SyncDialog {
    private static final String TAG = "SyncDialog";
    private static SyncDialog syncDialog;
    private static Activity act;
    private SDialog dialog;
    private TextView tvInfo;
    private TextView tvProgress;
    private View rootView;

    private static String mStep;
    private static int mTotal = 0;
    private static int mProgress = 0;

    private final int RUNNING_MESSAGE = 11;

    public synchronized static SyncDialog instance(){
        if(syncDialog == null){
            syncDialog = new SyncDialog();
        }
        return syncDialog;
    }

    private SyncDialog(){
    }

    private Handler updateHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (mStep.equals(APP.getContext().getString(R.string.sync_get_failed)) ||
                    mStep.equals(APP.getContext().getString(R.string.sync_not_bind)) ||
                    mStep.equals(APP.getContext().getString(R.string.sync_not_depart))) {
                tvInfo.setTextColor(Color.RED);
            } else {
                tvInfo.setTextColor(Color.parseColor("#2d2a2a"));
            }
            tvInfo.setText(mStep);
            if(mProgress == 0 && mTotal == 0){
                tvProgress.setText("");
            } else {
                tvProgress.setText(mProgress + " / " + mTotal);
            }
            updateHandler.sendEmptyMessageDelayed(0,2000);
        }
    };

    public void init(Activity activity){
        act = activity;
        initDialog();
        initView();
    }

    private void initDialog(){
        dialog = new SDialog(APP.getContext());
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
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
        if(dialog != null && dialog.isShowing()){
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                }
            });
        }
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });
        updateHandler.sendEmptyMessage(RUNNING_MESSAGE);
    }

    public boolean isShown(){
        return dialog != null && dialog.isShowing();
    }

    public void dismiss(){
        updateHandler.removeMessages(RUNNING_MESSAGE);
        if(act == null){
            Log.e(TAG, "未进行初始化" );
            return;
        }

        rootView.post(new Runnable() {
            @Override
            public void run() {
                tvInfo.setText(act.getString(R.string.dialog_sync_tbjs));
                tvProgress.setText("");
            }
        });

        if(dialog != null && dialog.isShowing()){
            rootView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                }
            },2 * 1000);
        }
    }

    public static void setStep(final String step){
        mStep = step;
    }

    public static void setProgress(final int progress, final int max){
        mTotal = max;
        mProgress = progress;
    }
}
