package com.yunbiao.ybsmartcheckin_live_id.business;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.db.SignBean;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyXutils;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2019/3/15.
 */

public class VipDialogManager {
    private static final String TAG = "VipDialogManager";
    private static Dialog vipDialog;

    private static VipDialogManager instance;

    private Activity mAct;

    public static VipDialogManager instance(){
        if(instance == null){
            synchronized(VipDialogManager.class){
                if(instance == null){
                    instance = new VipDialogManager();
                }
            }
        }
        return instance;
    }

    private VipDialogManager(){}

    public void initBG(Activity activity){
        mAct = activity;
        getHoliRemBG();
        getJoinRemBG();
    }
    private void getHoliRemBG(){
        int companyId = SpUtils.getInt(SpUtils.COMPANYID);
        Map<String,String> params = new HashMap<>();
        params.put("comId",String.valueOf(companyId));
        MyXutils.getInstance().post(ResourceUpdate.HOLIREM, params, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {
                Log.e(TAG, "onSuccess: -----" + result);
            }

            @Override
            public void onError(Throwable ex) {
                ex.printStackTrace();
            }

            @Override
            public void onFinish() {
            }
        });
    }

    private void getJoinRemBG(){
        int companyId = SpUtils.getInt(SpUtils.COMPANYID);
        Map<String,String> params = new HashMap<>();
        params.put("comId",String.valueOf(companyId));
        MyXutils.getInstance().post(ResourceUpdate.JOINREM, params, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {
                Log.e(TAG, "onSuccess: -----" + result);
            }

            @Override
            public void onError(Throwable ex) {
                ex.printStackTrace();
            }

            @Override
            public void onFinish() {

            }
        });
    }



    public static void dismissVipDialog() {
        if (vipDialog != null && vipDialog.isShowing()) {
            vipDialog.dismiss();
        }
    }

    private static void init(Context context) {
        if (vipDialog == null) {
            vipDialog = new Dialog(context);
            //去掉标题线
            vipDialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
            //背景透明
            vipDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        } else {
            vipDialog.dismiss();
        }

        Window window = vipDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER; // 居中位置
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
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

    public static void showVipDialog(final Activity context, final String today, final SignBean signBean) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                init(context);

                if (today.contains("3月8日") && signBean.getSex().equals("女")) {
                    vipDialog.setContentView(R.layout.dialog_vip_38);
                } else {
                    vipDialog.setContentView(R.layout.dialog_vip);
                }

                CircleImageView civ_userPhoto = (CircleImageView) vipDialog.findViewById(R.id.civ_userPhoto);
                TextView tv_nameAndJob = (TextView) vipDialog.findViewById(R.id.tv_nameAndJob);
                TextView tv_sign = (TextView) vipDialog.findViewById(R.id.tv_sign);
                if(!TextUtils.isEmpty(signBean.getImgUrl())){
                    Bitmap bitmap = BitmapFactory.decodeFile(signBean.getImgUrl());
                    civ_userPhoto.setImageBitmap(bitmap);
                }

                tv_nameAndJob.setText(signBean.getName() + "  " + signBean.getJob());
                tv_sign.setText(signBean.getSignature());

                if(!context.isFinishing()){
                    vipDialog.show();
                }

                handler.removeMessages(0);
                handler.sendEmptyMessageDelayed(0,2000);
            }
        });
    }

    static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            dismissVipDialog();
        }
    };


    public static void showBuluDialog(final Activity context, final String imgPath, final boolean makeUpSuccess) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                init(context);

                vipDialog.setContentView(R.layout.dialog_vip);

                CircleImageView civ_userPhoto = (CircleImageView) vipDialog.findViewById(R.id.civ_userPhoto);
                TextView tv_nameAndJob = (TextView) vipDialog.findViewById(R.id.tv_nameAndJob);
                TextView tv_sign = (TextView) vipDialog.findViewById(R.id.tv_sign);
                if(!TextUtils.isEmpty(imgPath)){
                    Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
                    civ_userPhoto.setImageBitmap(bitmap);
                }
                tv_nameAndJob.setText(makeUpSuccess ?"补录成功！":"补录失败！");
                vipDialog.show();

                handler.removeMessages(0);
                handler.sendEmptyMessageDelayed(0,3000);
            }
        });
    }

}
