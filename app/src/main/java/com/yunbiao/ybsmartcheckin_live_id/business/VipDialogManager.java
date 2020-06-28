package com.yunbiao.ybsmartcheckin_live_id.business;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.db2.User;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

/**
 * Created by Administrator on 2019/3/15.
 */

public class VipDialogManager {
    private static final String TAG = "VipDialogManager";
    private static Dialog vipDialog;

    private static VipDialogManager instance;

    public static VipDialogManager instance() {
        if (instance == null) {
            synchronized (VipDialogManager.class) {
                if (instance == null) {
                    instance = new VipDialogManager();
                }
            }
        }
        return instance;
    }

    private VipDialogManager() {
    }

    public static void dismissVipDialog() {
        if (vipDialog != null && vipDialog.isShowing()) {
            vipDialog.dismiss();
        }
    }

    private static void init(Activity context) {
        if (vipDialog != null && vipDialog.isShowing()) {
            vipDialog.dismiss();
            vipDialog = null;
        }

        vipDialog = new Dialog(context);
        //去掉标题线
        vipDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //背景透明
        vipDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Window window = vipDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER; // 居中位置
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0
            lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        } else {
            lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        window.setGravity(Gravity.CENTER);
        window.setWindowAnimations(R.style.mystyle);  //添加动画
        window.setAttributes(lp);
    }

    public static void showVipDialog(final Activity activity, final Sign sign) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                init(activity);

                vipDialog.setContentView(R.layout.dialog_vip_item_test);
                ImageView ivHead = vipDialog.findViewById(R.id.civ_userPhoto);
                TextView tvName = vipDialog.findViewById(R.id.tv_nameAndJob);
                TextView tvSign = vipDialog.findViewById(R.id.tv_sign);
                int isShowJob = SpUtils.getInt(SpUtils.DISPLAYPOSITION);
                String name = sign.getName();
                Glide.with(activity).load(sign.getHeadPath()).asBitmap().into(ivHead);
                int type = sign.getType();

                //访客
                if (type == -2 || type == -1) {
                    tvName.setText(sign.getName());
                    tvSign.setText("\n" + sign.getAutograph());

                    if(type == -2){
                        tvSign.setTextColor(Color.RED);
                    } else {
                        tvSign.setTextColor(Color.GREEN);
                        String signStr = isShowJob == 0
                                ? TextUtils.isEmpty(sign.getPosition()) ? "" : sign.getPosition()
                                : TextUtils.isEmpty(sign.getAutograph()) ? "" : sign.getAutograph();
                        long visEntryId = sign.getVisEntryId();
                        String content = "\n" + signStr;
                        User user = DaoManager.get().queryUserById(visEntryId);
                        if (user != null) {
                            content += "\n" + user.getName();
                        }
                        tvSign.setText(content);
                    }
                } else if(type == -9){//陌生人
                    tvName.setText(APP.getContext().getResources().getString(R.string.System_Visitor));
                    tvSign.setText("");
                } else {//签到
                    tvName.setText(name);
                    tvSign.setTextColor(Color.WHITE);
                    String signStr = isShowJob == 0
                            ? TextUtils.isEmpty(sign.getPosition()) ? "" : sign.getPosition()
                            : TextUtils.isEmpty(sign.getAutograph()) ? "" : sign.getAutograph();
                    tvSign.setText(signStr);
                }

                if (!activity.isFinishing()) {
                    vipDialog.show();
                }

                handler.removeMessages(0);
                handler.sendEmptyMessageDelayed(0, 1500);
            }
        });
    }

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            dismissVipDialog();
        }
    };
}
