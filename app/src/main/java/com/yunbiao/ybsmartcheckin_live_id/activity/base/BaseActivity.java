package com.yunbiao.ybsmartcheckin_live_id.activity.base;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
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
import com.yunbiao.ybsmartcheckin_live_id.utils.SoftKeyBoardListener;

import org.greenrobot.eventbus.EventBus;

import java.io.InputStream;

import butterknife.ButterKnife;

public abstract class BaseActivity extends FragmentActivity {
    protected boolean isLog = true;
    protected int mCurrentOrientation;
    protected FragmentManager mFragmentManager;
    private boolean isSupportTouch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullscreen(false, false);
        APP.addActivity(this);
        mCurrentOrientation = getResources().getConfiguration().orientation;

        //判断是否支持触屏
        isSupportTouch = getResources().getConfiguration().touchscreen == Configuration.TOUCHSCREEN_FINGER;

        mFragmentManager = getSupportFragmentManager();

        int portraitLayout = getPortraitLayout();
        int landscapeLayout = getLandscapeLayout();

        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (portraitLayout == 0) {
                APP.exit();
                return;
            }
            setContentView(portraitLayout);
        } else {
            if (landscapeLayout == 0) {
                APP.exit();
                return;
            }
            setContentView(landscapeLayout);
        }

        ButterKnife.bind(this);

        initView();

        initData();

        View backView = findViewById(R.id.iv_back);
        if (backView != null) {
            backView.setOnClickListener(v -> finish());
        }

        SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                setFullscreen(false, false);
            }

            @Override
            public void keyBoardHide(int height) {
                setFullscreen(false, false);
            }
        });
    }

    public void setFullscreen(boolean isShowStatusBar, boolean isShowNavigationBar) {
        //专门设置一下状态栏导航栏背景颜色为透明，凸显效果。
        setNavigationStatusColor(Color.TRANSPARENT);

        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;


        if (!isShowStatusBar) {
            uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        if (!isShowNavigationBar) {
            uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }

    public void setNavigationStatusColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setNavigationBarColor(color);
            getWindow().setStatusBarColor(color);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    protected void replaceFragment(int id, Fragment fragment) {
        if (mFragmentManager == null) {
            return;
        }
        mFragmentManager.beginTransaction().replace(id, fragment).commit();
    }

    protected void addFragment(int id, Fragment fragment) {
        if (mFragmentManager == null) {
            return;
        }
        mFragmentManager.beginTransaction().add(id, fragment).commit();
    }

    protected void removeFragment(Fragment fragment) {
        if (fragment != null && fragment.isAdded()) {
            mFragmentManager.beginTransaction().remove(fragment).commit();
        }
    }

    protected String getResString(int id){
        return APP.getContext().getResources().getString(id);
    }

    public void onBack(View view) {
        finish();
    }

    /***
     * 选择布局
     * @return
     */
    protected abstract int getPortraitLayout();

    protected abstract int getLandscapeLayout();

    protected void bindImageView(String urlOrPath, final ImageView iv) {
        if (TextUtils.isEmpty(urlOrPath)) {
            return;
        }
        Glide.with(this).load(urlOrPath).skipMemoryCache(true).crossFade(500).into(iv);
    }

    /***
     * 初始化View
     */
    protected void initView() {
    }

    /***
     * 初始化数据
     */
    protected void initData() {
    }

    protected void registerEventBus(Object object) {
        if (!EventBus.getDefault().isRegistered(object)) {
            EventBus.getDefault().register(object);
        }
    }

    protected void unRegisterEventBus(Object object) {
        if (EventBus.getDefault().isRegistered(object)) {
            EventBus.getDefault().unregister(object);
        }
    }

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
        APP.removeActivity(this);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    protected void d(String log) {
        if (isLog) {
            Log.d(this.getClass().getSimpleName(), log);
        }
    }

    protected void e(String log) {
        if (isLog) {
            Log.e(this.getClass().getSimpleName(), log);
        }
    }

    public static Bitmap readBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // 获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    public void setImageByResId(ImageView imgView, int resourceID) {
        Bitmap bitmap = readBitMap(this, resourceID);
        imgView.setImageBitmap(bitmap);
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

    protected float formatF(float fValue) {
        return (float) (Math.round(fValue * 10)) / 10;
    }
}
