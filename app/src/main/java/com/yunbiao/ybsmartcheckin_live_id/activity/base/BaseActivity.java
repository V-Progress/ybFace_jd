package com.yunbiao.ybsmartcheckin_live_id.activity.base;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Looper;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.umeng.analytics.MobclickAgent;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class BaseActivity extends FragmentActivity {
    protected boolean isLog = true;
    private static final String TAG = "BaseActivity";
    private static List<Activity> activities = new ArrayList<Activity>();
    protected int mCurrentOrientation;
    protected FragmentManager mFragmentManager;
    private boolean isSupportTouch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏navigation
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activities.add(this);

        mCurrentOrientation = getResources().getConfiguration().orientation;

        //判断是否支持触屏
        isSupportTouch = getResources().getConfiguration().touchscreen == Configuration.TOUCHSCREEN_FINGER;

        mFragmentManager = getSupportFragmentManager();

        int portraitLayout = getPortraitLayout();
        int landscapeLayout = getLandscapeLayout();

        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if(portraitLayout == 0){
                UIUtils.showShort(this,getString(R.string.act_base_zwspqqhhp));
                APP.exit();
                return;
            }
            setContentView(portraitLayout);
        } else {
            if(landscapeLayout == 0){
                UIUtils.showShort(this,getString(R.string.act_base_zwhpqqhhp));
                APP.exit();
                return;
            }
            setContentView(landscapeLayout);
        }

        initView();

        initData();
    }

    protected void replaceFragment(int id, Fragment fragment){
        if(mFragmentManager == null){
            return;
        }
        mFragmentManager.beginTransaction().replace(id,fragment).commit();
    }

    protected void addFragment(int id, Fragment fragment){
        if(mFragmentManager == null){
            return;
        }
        mFragmentManager.beginTransaction().add(id,fragment).commit();
    }

    public void onBack(View view){
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /***
     * 选择布局
     * @return
     */
    protected abstract int getPortraitLayout();
    protected abstract int getLandscapeLayout();

    protected void bindImageView(String urlOrPath, final ImageView iv){
        if(TextUtils.isEmpty(urlOrPath)){
            return;
        }
        Glide.with(this).load(urlOrPath).skipMemoryCache(true).crossFade(500).into(iv);
    }

    /***
     * 初始化View
     */
    protected void initView(){};

    /***
     * 初始化数据
     */
    protected void initData(){}

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        activities.remove(this);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 获得Activity
     */
    public static Activity getActivity() {
        for (Activity activity : activities) {
            if (isForeground(activity,activity.getClass().getSimpleName())) {
                return activity;
            }
        }
        return activities.get(activities.size() - 1);
    }

    public static boolean  isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }
        return false;

    }

    /**
     * finish所有Activity
     */
    public static void finishAll() {
        finish(null);
    }

    /**
     * finish所有其它Activity
     */
    public static void finishOthers(Class<? extends Activity> activity) {
        finish(activity);
    }

    protected void d(String log){
        if(isLog){
            Log.d(this.getClass().getSimpleName(),log);
        }
    }

    public static void finish(Class<? extends Activity> currentActivity) {
        for (Iterator<Activity> iterator = activities.iterator(); iterator.hasNext(); ) {
            Activity activity = iterator.next();
            if (activity.getClass() == currentActivity) {
                continue;
            }
            iterator.remove();
            activity.finish();
        }
    }

    /**
     * 清除图片内存缓存
     */
    public void clearImageMemoryCache(Context context) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) { //只能在主线程执行
                Glide.get(context).clearMemory();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
