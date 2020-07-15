package com.yunbiao.ybsmartcheckin_live_id.activity.base;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.umeng.analytics.MobclickAgent;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.common.UpdateVersionControl;
import com.yunbiao.ybsmartcheckin_live_id.utils.SoftKeyBoardListener;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.w3c.dom.Text;

import java.io.InputStream;
import java.util.List;

import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Request;

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
                d("显示虚拟键盘");
                setFullscreen(false, false);
            }

            @Override
            public void keyBoardHide(int height) {
                d("隐藏虚拟键盘");
                setFullscreen(false, false);
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            setFullscreen(false, false);
            handler.sendEmptyMessageDelayed(0, 10000);
        }
    };

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

    protected String getResString(int id) {
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

        // TODO: 2020/4/18 如果此处未能有效隐藏导航键，就放到onResume中
        handler.removeMessages(0);
        handler.sendEmptyMessageDelayed(0, 3000);
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

    protected float getMean(List<Float> array) {
        float result = 0.0f;
        if (array.size() == 0) {
            return result;
        }
        for (float anArray : array) {
            result += anArray;
        }
        result = result / array.size();
        result = formatF(result);
        return result;
    }

    protected float formatF(float fValue) {
        return (float) (Math.round(fValue * 10)) / 10;
    }

    private AlertDialog mAlertDialog;
    protected void dismissAlert(){
        if(mAlertDialog != null && mAlertDialog.isShowing()){
            mAlertDialog.dismiss();
        }
    }
    protected void showRestartAlert(String message, String negative, Runnable runnable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setTitle(getResString(R.string.alert_title_warning));
        builder.setNegativeButton(getResString(R.string.temper_error_btn_cancel), (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(negative, (dialog, which) -> {
            dialog.dismiss();
            if (runnable != null) {
                runnable.run();
            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    protected void checkUpgrade(CheckUpgradeCallback callback) {
        String currVersionName = UpdateVersionControl.getVersionName();
        String url = ResourceUpdate.GET_VERSION_INFO;
        d("检测更新");
        d("地址：" + url);
        OkHttpUtils.post().url(url).addParams("type", String.valueOf(Constants.DEVICE_TYPE)).build().execute(new StringCallback() {
            @Override
            public void onBefore(Request request, int id) {
                super.onBefore(request, id);
                if (callback != null) {
                    callback.onStart();
                }
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                d("请求失败：" + (e == null ? "NULL" : e.getMessage()));
                if (callback != null) {
                    callback.onError(currVersionName, (e == null ? "NULL" : e.getMessage()));
                    callback.onFinish();
                }
            }

            @Override
            public void onResponse(String response, int id) {
                d("请求成功：" + response);
                if (TextUtils.isEmpty(response)) {
                    if (callback != null) {
                        callback.noUpgrade(currVersionName);
                        callback.onFinish();
                    }
                    return;
                }

                JSONObject jsonObject = JSONObject.parseObject(response);
                String versionName = jsonObject.getString("serverVersion");
                //如果新版本为空
                if (TextUtils.isEmpty(versionName) ) {
                    if (callback != null) {
                        callback.noUpgrade(currVersionName);
                        callback.onFinish();
                    }
                    return;
                }
                //如果版本相同
                if(TextUtils.equals(currVersionName,versionName)){
                    if (callback != null) {
                        callback.noUpgrade(currVersionName);
                        callback.onFinish();
                    }
                    return;
                }

                //如果新版本int值小于等于当前版本
                try {
                    String currV = currVersionName.replaceAll("\\.", "");
                    String newV = versionName.replaceAll("\\.","");
                    int currI = Integer.parseInt(currV);
                    int newI = Integer.parseInt(newV);
                    if(newI <= currI){
                        if (callback != null) {
                            callback.noUpgrade(currVersionName);
                            callback.onFinish();
                        }
                        return;
                    }
                }catch (Exception e){
                    if (callback != null) {
                        callback.noUpgrade(currVersionName);
                        callback.onFinish();
                    }
                    e.printStackTrace();
                    return;
                }

                String versionInfo = jsonObject.getString("versionDesc");
                if (callback != null) {
                    callback.haveNewVersion(versionName, versionInfo);
                    callback.onFinish();
                }
            }
        });
    }

    public interface CheckUpgradeCallback {
        void onStart();

        void noUpgrade(String currVersionName);

        void haveNewVersion(String newVersionName, String versionInfo);

        void onError(String currVersionName, String s);

        void onFinish();

    }


}
