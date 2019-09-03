package com.yunbiao.ybsmartcheckin_live_id;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.WelComeActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.jdcn.WelComeSmallActivity;
import com.yunbiao.ybsmartcheckin_live_id.db.CompBean;
import com.yunbiao.ybsmartcheckin_live_id.db.DepartBean;
import com.yunbiao.ybsmartcheckin_live_id.db.SignBean;
import com.yunbiao.ybsmartcheckin_live_id.db.VIPDetail;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Depart;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.db2.User;
import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import org.xbill.DNS.SIG0;

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
        Config.deviceType = CommonUtils.getBroadType();
        ybPermission = new YBPermission(new YBPermission.PermissionListener(){
            @Override
            public void onPermissionFailed(String[] objects) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(objects,YBPermission.PERMISSION_REQUEST_CODE);
                }
            }

            @Override
            public void onFinish(boolean isComplete) {
                if(isComplete){
                    transferDBData(new Runnable() {
                        @Override
                        public void run() {
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
        if(true){
            runnable.run();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("提示");
        progressDialog.show();

        final List<CompBean> compBeans = APP.getCompDao().selectAll();
        final List<VIPDetail> vipDetails = APP.getUserDao().selectAll();
        final List<DepartBean> departBeans = APP.getDepartDao().selectAll();
        final List<SignBean> signBeans = APP.getSignDao().selectAll();
        if(compBeans.size() <= 0 && vipDetails.size() <= 0 && departBeans.size() <= 0 && signBeans.size() <= 0){
            runnable.run();
            return;
        }

        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                d("正在迁移公司数据------------");

                e.onNext(0);
                for (CompBean compBean : compBeans) {
                    Company company = new Company();
                    company.setAbbname(compBean.getAbbName());
                    company.setBottomtitle(compBean.getBottomTitle());
                    company.setComid(compBean.getComid());
                    company.setId(compBean.getComid());
                    company.setComlogo(compBean.getIconUrl());
                    company.setDevicePwd(compBean.getDevicePwd());
                    company.setToptitle(compBean.getTopTitle());
                    company.setSlogan(compBean.getSlogan());
                    company.setGotips(compBean.getGotips());
                    company.setGotime(compBean.getGotime());
                    company.setDowntips(compBean.getDowntips());
                    company.setDowntime(compBean.getDowntime());
                    company.setComname(compBean.getCompName());
                    long l = DaoManager.get().addOrUpdate(company);
                    APP.getCompDao().delete(compBean);
                }
                d("迁移公司数据结束------------");


                Thread.sleep(2000);

                d("正在迁移员工信息------------");

                e.onNext(1);
                for (VIPDetail vipDetail : vipDetails) {
                    User user = new User();
                    user.setSex(Integer.parseInt(vipDetail.getSex()));
                    user.setAge(Integer.parseInt(vipDetail.getAge()));
                    user.setAutograph(vipDetail.getSignature());
                    user.setHeadPath(vipDetail.getImgUrl());
                    user.setPosition(vipDetail.getJob());
                    user.setFaceId(vipDetail.getFaceId());
                    user.setId(vipDetail.getEmpId());
                    user.setNumber(vipDetail.getEmployNum());
                    user.setName(vipDetail.getName());
                    long l = DaoManager.get().addOrUpdate(user);
                    d(user.toString());
                    d("迁移结果：" + l);
                    APP.getUserDao().delete(vipDetail);
                }
                d("迁移员工结束------------");

                Thread.sleep(2000);

                d("正在迁移部门数据------------");

                e.onNext(2);
                for (DepartBean departBean : departBeans) {
                    Depart depart = new Depart();
                    depart.setDepId(departBean.getDepartId());
                    depart.setDepName(departBean.getName());
                    depart.setId(departBean.getDepartId());
                    long l = DaoManager.get().addOrUpdate(depart);

                    d(depart.toString());
                    d("迁移结果："  + l);
                    APP.getDepartDao().delete(departBean);
                }
                d("迁移部门数据结束------------");

                Thread.sleep(2000);

                d("正在迁移签到数据------------");

                e.onNext(3);
                for (SignBean signBean : signBeans) {
                    Sign sign = new Sign();

                    if (!TextUtils.isEmpty(signBean.getAge())) {
                        sign.setAge(Integer.parseInt(signBean.getAge()));
                    }
                    sign.setAutograph(signBean.getSignature());
                    if (!TextUtils.isEmpty(signBean.getSex())) {
                        sign.setSex(Integer.parseInt(signBean.getSex()));
                    }
                    sign.setBirthday(signBean.getBirthday());
                    sign.setDate(signBean.getDate());
                    sign.setDepart(signBean.getDepart());
                    sign.setEmpId(signBean.getEmpId());
                    sign.setEmployNum(signBean.getEmployNum());
                    sign.setFaceId(signBean.getFaceId());
                    sign.setUpload(signBean.isUpload());
                    sign.setPosition(signBean.getJob());
                    sign.setTime(signBean.getTime());
                    sign.setName(signBean.getName());
                    sign.setHeadPath(signBean.getImgUrl());
                    long l = DaoManager.get().addOrUpdate(sign);
                    d(sign.toString());
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
                if(o == 0){
                    progressDialog.setMessage("正在进行数据迁移，请不要退出软件或重启设备\n正在处理公司信息...");
                } else if(o == 1){
                    progressDialog.setMessage("正在进行数据迁移，请不要退出软件或重启设备\n正在处理员工数据...");
                } else if(0 == 2){
                    progressDialog.setMessage("正在进行数据迁移，请不要退出软件或重启设备\n正在处理部门数据...");
                } else if(0 == 3){
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
