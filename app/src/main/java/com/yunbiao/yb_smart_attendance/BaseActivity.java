package com.yunbiao.yb_smart_attendance;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.WelComeActivity;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseActivity extends Activity {

    private int mCurrentOrientation;
    private List<Activity> mActList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentOrientation = getResources().getConfiguration().orientation;
        if(needToRegisterEvents()){
            registerEvent();
        }

        addToList(this);
        setContentView(getLayoutId());

        initView();
        initData();
    }

    //获取布局ID
    private int getLayoutId(){
        int layoutId = 0;
        int landscape = setLandscapeLayout();
        int portrait = setPortraitLayout();
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT){
            if(portrait > 0){//如果没有竖屏布局则指向横屏布局
                layoutId = portrait;
            } else {
                layoutId = landscape;
            }
        } else {
            if(landscape > 0){//如果没有横屏布局就指向竖屏布局
                layoutId = landscape;
            } else {
                layoutId = portrait;
            }
        }
        return layoutId;
    }

    /***
     * 是否需要注册事件
     * @return
     */
    protected abstract boolean needToRegisterEvents();

    /***
     * 设置横屏布局
     * @return
     */
    public abstract int setLandscapeLayout();

    /***
     * 设置竖屏布局
     * @return
     */
    public abstract int setPortraitLayout();

    /***
     * 初始化控件
     */
    public abstract void initView();

    /***
     * 初始化数据
     */
    public abstract void initData();

    /***
     * 获取横竖屏标识
     * @return
     */
    protected int getOrientation(){
        return mCurrentOrientation;
    }

    private void addToList(Activity activity){
        if(!mActList.contains(activity)){
            mActList.add(activity);
        }
    }

    private void removeThis(Activity activity){
        if(mActList.contains(activity)){
            mActList.add(activity);
        }
    }

    /***
     * 注册EventBus
     */
    protected void registerEvent(){
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    /***
     * 解绑EventBus
     */
    protected void unregisterEvent(){
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeThis(this);
    }


    //密码弹窗
    protected void inputPwd(final Runnable runnable) {
        final String password = CommonData.getPassword();
        if(TextUtils.isEmpty(password)){
            if (runnable != null) {
                runnable.run();
            }
            return;
        }

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_input_pwd);

        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_edt_shake);
        final View rootView = dialog.findViewById(R.id.ll_input_pwd);
        Button btnConfirm = (Button) dialog.findViewById(R.id.btn_input_confirm);
        final EditText edtPwd = (EditText) dialog.findViewById(R.id.edt_input_pwd);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = edtPwd.getText().toString();
                if (TextUtils.isEmpty(pwd)) {
                    edtPwd.setError(getString(R.string.act_wel_error_bywjsrmmo));
                    rootView.startAnimation(animation);
                    return;
                }

                if (!TextUtils.equals(pwd, password)) {
                    edtPwd.setError(getString(R.string.act_wel_error_mmclqcxsrb));
                    rootView.startAnimation(animation);
                    return;
                }
                if (runnable != null) {
                    runnable.run();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int width = dm.widthPixels;
        final Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.mystyle);  //添加动画
        window.setLayout(width / 2, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);
    }
}
