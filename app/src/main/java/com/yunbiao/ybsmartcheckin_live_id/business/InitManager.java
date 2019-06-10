package com.yunbiao.ybsmartcheckin_live_id.business;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.alibaba.fastjson.JSON;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.bean.CompanyBean;
import com.yunbiao.ybsmartcheckin_live_id.heartbeat.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyXutils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2019/3/20.
 */

public class InitManager {
    private final String TAG = getClass().getSimpleName();
    private static InitManager instance;
    private View layout_head;
    private View layout_wel;
    private View iv_yuan;
    private View iv_line;
    private InitListener initListener;
    private Activity mAct;

    public static InitManager instance() {
        if (instance == null) {
            synchronized (InitManager.class) {
                if (instance == null) {
                    instance = new InitManager();
                }
            }
        }
        return instance;
    }

    private InitManager() {
    }

    public interface InitListener {
        void onBinded(boolean isBinded);
    }

    public InitManager init(final Activity act, final InitListener initListener) {
        this.initListener = initListener;
        if (this.initListener == null) {
            this.initListener = new Listener() {
            };
        }
        mAct = act;
        layout_head = mAct.findViewById(R.id.layout_head);
        layout_wel = mAct.findViewById(R.id.layout_wel);
        iv_yuan = mAct.findViewById(R.id.iv_yuan);
        iv_line = mAct.findViewById(R.id.iv_line);

        startAnim();

        KDXFSpeechManager.instance().welcome(new KDXFSpeechManager.VoicePlayListener() {
            @Override
            public void playComplete(String uttId) {
                closeInitView();
            }
        });
        return instance();
    }

    private void startAnim() {
        RotateAnimation rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        LinearInterpolator lin = new LinearInterpolator();
        rotate.setInterpolator(lin);
        rotate.setDuration(4000);//设置动画持续周期
        rotate.setRepeatCount(-1);//设置重复次数
        rotate.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        rotate.setStartOffset(10);//执行前的等待时间
        iv_yuan.setAnimation(rotate);

        AnimationSet animationSet = new AnimationSet(true);//共用动画补间
        TranslateAnimation ta = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0f,
                Animation.RELATIVE_TO_PARENT, 0f,
                Animation.RELATIVE_TO_PARENT, -0.2f,
                Animation.RELATIVE_TO_PARENT, 0.35f);
        ScaleAnimation bigToSmallAnim = new ScaleAnimation(1, 0.6f, 1, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);//x轴0倍，x轴1倍，y轴0倍，y轴1倍

        animationSet.setDuration(4000);
        animationSet.setRepeatCount(-1);
        animationSet.setRepeatMode(Animation.REVERSE);
        animationSet.addAnimation(ta);
        animationSet.addAnimation(bigToSmallAnim);
        // 动画是作用到某一个控件上
        iv_line.startAnimation(animationSet);
    }

    public void closeInitView() {

        TranslateAnimation ta1 = new TranslateAnimation(0, 0, 0, -layout_head.getHeight());
        ta1.setInterpolator(new LinearInterpolator());
        ta1.setDuration(800);
        layout_head.setAnimation(ta1);

        AnimationSet as = new AnimationSet(true);
        TranslateAnimation ta2 = new TranslateAnimation(0, 0, 0, layout_wel.getHeight());
        AlphaAnimation a = new AlphaAnimation(1f, 0.5f);
        as.setInterpolator(new LinearInterpolator());
        as.setDuration(800);
        as.addAnimation(ta2);
        as.addAnimation(a);
        layout_wel.setAnimation(as);

        ta2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                layout_head.setVisibility(View.GONE);
                layout_wel.setVisibility(View.GONE);
//                mAct.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        final int companyid = SpUtils.getInt(mAct, SpUtils.COMPANYID, 0);
//                        initListener.onBinded(companyid != 0);
//                    }
//                });
                loadCompany();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    abstract class Listener implements InitListener {
        @Override
        public void onBinded(boolean isBinded) {

        }
    }

    private void loadCompany() {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("deviceNo", HeartBeatClient.getDeviceNo());
        Log.e(TAG, "deviceNo--------------->" + HeartBeatClient.getDeviceNo());
        MyXutils.getInstance().post(ResourceUpdate.COMPANYINFO, map, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {
                final CompanyBean bean = JSON.parseObject(result, CompanyBean.class);
                mAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initListener.onBinded(bean.getStatus() == 1);
                    }
                });
            }

            @Override
            public void onError(Throwable ex) {
                mAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final int companyid = SpUtils.getInt(SpUtils.COMPANYID);
                        initListener.onBinded(companyid != 0);
                    }
                });
            }

            @Override
            public void onFinish() {

            }
        });
    }
}
