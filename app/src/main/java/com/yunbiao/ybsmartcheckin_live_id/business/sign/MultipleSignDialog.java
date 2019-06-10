package com.yunbiao.ybsmartcheckin_live_id.business.sign;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.db.SignBean;

import java.util.LinkedList;

public class MultipleSignDialog {
    private static final String TAG = "MultipleSignDialog";
    private static MultipleSignDialog instance = new MultipleSignDialog();
    private static LinkedList<SignBean> signList = new LinkedList<>();
    private final int MAX_SIGN_TIME = 5;
    private int signOffTime = MAX_SIGN_TIME;//多人签到延时
    private Dialog vipDialog;
    private RecyclerView rlvVip;
    private static Context mContext;
    private VipAdapter2 vipAdapter2;

    public static MultipleSignDialog instance(){
        return instance;
    }

    public void init(Context context){
        mContext = context;

        vipAdapter2 = new VipAdapter2(context,signList);

        vipDialog = new Dialog(context);
        //去掉标题线
        vipDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //背景透明
        vipDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        vipDialog.setCancelable(false);
        vipDialog.setContentView(R.layout.dialog_vip_test);

        setStyle();

        rlvVip = vipDialog.findViewById(R.id.rlv_vip);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(OrientationHelper.HORIZONTAL);
        rlvVip.setLayoutManager(layoutManager);

//        rlvVip.setAdapter(vipAdapter);
        rlvVip.setAdapter(vipAdapter2);
        rlvVip.setItemAnimator(new DefaultItemAnimator());
        vipDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                timeHandler.sendEmptyMessageDelayed(0,1000);
            }
        });
    }

    private void setStyle(){
        Window window = vipDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER; // 居中位置
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//6.0
            lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        }else {
            lp.type =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        window.setGravity(Gravity.CENTER);
        window.setWindowAnimations(R.style.mystyle);  //添加动画
        window.setAttributes(lp);
    }

    private MultipleSignDialog(){

    }

    public void sign(SignBean signBean) {
        if(vipDialog != null && vipDialog.isShowing()){
            signOffTime = MAX_SIGN_TIME;
        } else {
            showDialog();
        }

        signList.addLast(signBean);
        if(signList.size() > 3){
            while (true){
                signList.removeFirst();
                vipAdapter2.notifyItemRemoved(0);
                if(signList.size()<= 3){
                    break;
                }
            }
        }

        vipAdapter2.notifyItemInserted(vipAdapter2.getItemCount());
        rlvVip.scrollToPosition(vipAdapter2.getItemCount()-1);
    }

    private void showDialog(){
        if(vipDialog != null && vipDialog.isShowing()){
            dismiss();
        }

        vipDialog.show();
    }

    private void dismiss(){
        if(vipDialog != null){
            vipDialog.dismiss();
        }
    }

    private Handler timeHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(signOffTime <= 0){
                signOffTime = MAX_SIGN_TIME;
                signList.clear();
                dismiss();
                return;
            }
            signOffTime--;
            timeHandler.sendEmptyMessageDelayed(0,1000);
        }
    };

}