package com.yunbiao.ybsmartcheckin_live_id;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import com.yunbiao.ybsmartcheckin_live_id.activity.WelComeActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.jdcn.WelComeSmallActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.db.SignBean;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SplashActivity extends BaseActivity {
    private static final String TAG = "SplashActivity";

    public static String[] PERMISSONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE
            ,android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ,android.Manifest.permission.ACCESS_FINE_LOCATION
            ,android.Manifest.permission.ACCESS_COARSE_LOCATION
            ,android.Manifest.permission.READ_PHONE_STATE
            ,android.Manifest.permission.CAMERA
            /*,Manifest.permission.SYSTEM_ALERT_WINDOW*/};
    private YBPermission ybPermission;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_splash;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initData() {
        APP.bindProtectService();

        Config.deviceType = CommonUtils.getBroadType();
        ybPermission = new YBPermission(new YBPermission.PermissionListener(){
            @Override
            public void onPermissionFailed(String[] objects) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(objects, YBPermission.PERMISSION_REQUEST_CODE);
                }
            }

            @Override
            public void onFinish(boolean isComplete) {
                if(isComplete){
                    transferDBData(new Runnable() {
                        @Override
                        public void run() {
                            Constants.initStorage();
                            SpUtils.init();
                            APP.getContext().cauchException();
                            APP.getContext().initDB();

                            if(Config.deviceType == Config.DEVICE_SMALL_FACE){
                                startActivity(new Intent(SplashActivity.this, WelComeSmallActivity.class));
                            } else {
                                startActivity(new Intent(SplashActivity.this, WelComeActivity.class));
                            }
                            overridePendingTransition(0,0);
                            finish();
                        }
                    });
                    return;
                } else {
                    UIUtils.showTitleTip(SplashActivity.this,"权限申请失败");
                }
            }
        });
        ybPermission.checkPermission(this,PERMISSONS);
    }

    private void transferDBData(final Runnable runnable){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("提示");
        progressDialog.show();

        final List<SignBean> signBeans = APP.getSignDao().selectAll();
        if(signBeans.size() <= 0){
            progressDialog.dismiss();
            runnable.run();
            return;
        }

        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                d("正在迁移签到数据------------");
                e.onNext(3);
                int anInt = SpUtils.getInt(SpUtils.COMPANYID);
                for (SignBean signBean : signBeans) {
                    Sign sign = new Sign();
                    sign.setComid(anInt);
                    sign.setEmpId(signBean.getEmpId());
                    sign.setHeadPath(signBean.getImgUrl());
                    sign.setTime(signBean.getTime());
                    sign.setUpload(signBean.isUpload());
                    sign.setName(signBean.getName());
                    sign.setDate(signBean.getDate());
                    d(sign.toString());
                    long l = DaoManager.get().addOrUpdate(sign);
                    d("迁移结果："  + l);
                    APP.getSignDao().delete(signBean);
                }

                Thread.sleep(2000);

                d("迁移签到数据结束------------");
                e.onNext(4);

                Thread.sleep(2000);
                e.onNext(5);
            }
        }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer o) throws Exception {
                if(0 == 3){
                    progressDialog.setMessage("正在进行数据迁移，请不要退出软件或重启设备\n正在处理签到数据...");
                } else if(0 == 4){
                    progressDialog.setMessage("迁移完成");
                } else{
                    progressDialog.dismiss();
                    runnable.run();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ybPermission.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    public static class YBPermission{
        public static final int PERMISSION_REQUEST_CODE = 101;
        private PermissionListener permissionListener;
        public YBPermission(PermissionListener permissionListener) {
            this.permissionListener = permissionListener;
        }

        interface PermissionListener{
            void onPermissionFailed(String[] objects);
            void onFinish(boolean isComplete);
        }

        public void checkPermission(Activity activity,String[] permissions){
            if(permissions == null || permissions.length <= 0){
                if(permissionListener != null){
                    permissionListener.onFinish(false);
                }
                return;
            }

            List<String> deniedPermissionList = new ArrayList<>();
            for (String permission : permissions) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissionList.add(permission);
                    }
                }
            }

            if(deniedPermissionList .size() > 0){
                String[] strings = deniedPermissionList.toArray(new String[deniedPermissionList.size()]);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity.requestPermissions(strings,PERMISSION_REQUEST_CODE);
                }
            } else {
                if(permissionListener != null){
                    permissionListener.onFinish(true);
                }
            }
        }

        public void onRequestPermissionsResult(int requestCode, String[] permissions,  int[] grantResults){
            if(requestCode != PERMISSION_REQUEST_CODE){
                return;
            }

            if (permissions == null || permissions.length<=0) {
                if(permissionListener != null){
                    permissionListener.onFinish(false);
                }
                return;
            }

            List<String> permiList = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    permiList.add(permissions[i]);
                }
            }
            if(permiList.size() <= 0){
                if(permissionListener != null){
                    permissionListener.onFinish(true);
                }
                return;
            }

            if(permissionListener != null){
                permissionListener.onPermissionFailed(permiList.toArray(new String[permiList.size()]));
            }
        }
    }
}
