package com.yunbiao.ybsmartcheckin_live_id.business.sign;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;

import java.util.LinkedList;

public class MultipleSignDialog {
    private static final String TAG = "MultipleSignDialog";
    private static MultipleSignDialog instance = new MultipleSignDialog();
    private static LinkedList<Sign> signList = new LinkedList<>();
    private final int MAX_SIGN_TIME = 2;
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

        if(vipDialog == null){
            vipDialog = new Dialog(context, R.style.DialogStyle);
            //去掉标题线
            vipDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            //背景透明
            vipDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        } else {
            vipDialog.dismiss();
        }
        Window window = vipDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.LEFT; // 居中位置
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.dimAmount = 0f;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//6.0
            lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        }else {
            lp.type =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        window.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        window.setWindowAnimations(R.style.miniDialogStyle);  //添加动画
        window.setAttributes(lp);

        vipDialog.setCancelable(false);
        vipDialog.setContentView(R.layout.dialog_vip_test);

        setStyle();

        rlvVip = vipDialog.findViewById(R.id.rlv_vip);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(OrientationHelper.HORIZONTAL);
        rlvVip.setLayoutManager(layoutManager);
        rlvVip.setAdapter(vipAdapter2);
        rlvVip.setItemAnimator(new DefaultItemAnimator());
        vipDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                timeHandler.sendEmptyMessage(0);
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

    public void sign(Sign signBean) {
        if(vipDialog != null && vipDialog.isShowing()){
            signOffTime = MAX_SIGN_TIME;
        } else {
            showDialog();
        }

        signList.addFirst(signBean);
        if(signList.size() > 4){
            while (true){
                signList.removeLast();
                vipAdapter2.notifyItemRemoved(vipAdapter2.getItemCount()-1);
                if(signList.size()<= 4){
                    break;
                }
            }
        }

        vipAdapter2.notifyItemInserted(0);
        rlvVip.scrollToPosition(0);
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