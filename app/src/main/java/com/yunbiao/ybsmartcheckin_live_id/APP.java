package com.yunbiao.ybsmartcheckin_live_id;

import android.app.Application;
import android.app.smdt.SmdtManager;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;
import com.tencent.bugly.beta.upgrade.UpgradeListener;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.yunbiao.ybsmartcheckin_live_id.activity.WelComeActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.db.DatabaseHelper;
import com.yunbiao.ybsmartcheckin_live_id.db.DepartDao;
import com.yunbiao.ybsmartcheckin_live_id.db.SignDao;
import com.yunbiao.ybsmartcheckin_live_id.db.UserDao;
import com.yunbiao.ybsmartcheckin_live_id.db.dbtest.CompDao;
import com.yunbiao.ybsmartcheckin_live_id.exception.CrashHandler2;
import com.yunbiao.ybsmartcheckin_live_id.utils.PropsUtil;
import com.yunbiao.ybsmartcheckin_live_id.utils.RestartAPPTool;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.zhy.http.okhttp.OkHttpUtils;

import org.xutils.x;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;


public class APP extends Application {
    private static APP instance;
    private static SmdtManager smdt;
    public static boolean isLiveness = true;
    private static UserDao userDao;
    private static SignDao signDao;
    private static DepartDao departDao;
    private static CompDao companyDao;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        initDB();

        cauchException();

        initBugly();

        initUM();

        initUtils();

    }

    private void initDB(){
        DatabaseHelper.createDatabase(this);
        userDao = new UserDao(this);
        signDao = new SignDao(this);
        departDao = new DepartDao(this);
        companyDao = new CompDao(this);
    }

    public static CompDao getCompanyDao() {
        return companyDao;
    }

    public static UserDao getUserDao() {
        return userDao;
    }

    public static SignDao getSignDao() {
        return signDao;
    }

    public static DepartDao getDepartDao() {
        return departDao;
    }

    // -------------------异常捕获-----捕获异常后重启系统-----------------//
    private void cauchException() {
        CrashHandler2.CrashUploader uploader = new CrashHandler2.CrashUploader() {
            @Override
            public void uploadCrashMessage(ConcurrentHashMap<String, Object> info, Throwable ex) {
                Log.e("APP", "uploadCrashMessage: -------------------");
                CrashReport.postCatchedException(ex);
                MobclickAgent.reportError(APP.getContext(), ex);

                RestartAPPTool.restartAPP(APP.getContext());
            }
        };
        CrashHandler2.getInstance().init(this, uploader, null);
    }

    private void initUM() {
        UMConfigure.init(this, "5cbe87a60cafb210460006b3", "self", UMConfigure.DEVICE_TYPE_BOX, null);
        UMConfigure.setLogEnabled(false);
        MobclickAgent.setCatchUncaughtExceptions(true);
    }

    private void initUtils() {
//        Log2FileUtil.startLogcatManager(this);
        //初始化host参数
        PropsUtil.instance().init(this);
        Constants.init();
        Integer boardType = PropsUtil.instance().getBoardType();
        isLiveness = boardType == 0 || boardType == 2;

        //初始化xutils 3.0
        x.Ext.init(this);
        smdt = SmdtManager.create(this);

        OkHttpClient build = new OkHttpClient.Builder()
                .connectTimeout(60 * 1000, TimeUnit.SECONDS)
                .writeTimeout(60 * 1000, TimeUnit.SECONDS)
                .readTimeout(60 * 1000, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        OkHttpUtils.initClient(build);
    }

    private void initBugly() {
        // 获取当前包名
        String packageName = this.getPackageName();
        // 获取当前进程名
        String processName = getProcessName(android.os.Process.myPid());
        // 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(this);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        //设置渠道号
        strategy.setAppChannel("self");
        strategy.setCrashHandleCallback(new CrashReport.CrashHandleCallback() {
            @Override
            public synchronized Map<String, String> onCrashHandleStart(int crashType, String errorType, String errorMessage, String errorStack) {
                Log.e("APP", "onCrashHandleStart:11111111111111111111 ");
                return super.onCrashHandleStart(crashType, errorType, errorMessage, errorStack);
            }

            @Override
            public synchronized byte[] onCrashHandleStart2GetExtraDatas(int crashType, String errorType, String errorMessage, String errorStack) {
                Log.e("APP", "onCrashHandleStart:22222222222222222222 ");
                return super.onCrashHandleStart2GetExtraDatas(crashType, errorType, errorMessage, errorStack);
            }
        });
        //设置用户ID
//        String deviceSernum = SpUtils.getString(APP.getContext(), SpUtils.DEVICE_NUMBER, "");
        String deviceSernum = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        Bugly.setUserId(this, deviceSernum);
        // 初始化Bugly
        Bugly.init(this, "7ab7381010", false, strategy);

        //设置更新规则
        setUpgrade();
        //自动检测一次更新
        Beta.checkUpgrade(false, true);
    }

    private void setUpgrade() {
        /**** Beta高级设置*****/
        Beta.autoInit = true;//是否自动启动初始化
        Beta.autoCheckUpgrade = false;//是否自动检查升级
        Beta.initDelay = 1 * 1000;//检查周期
        Beta.largeIconId = R.mipmap.ic_launcher;//通知栏大图标
        Beta.smallIconId = R.mipmap.ic_launcher;//通知栏小图标
        Beta.defaultBannerId = R.mipmap.ic_launcher;
        Beta.storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);//更新资源保存目录
        Beta.showInterruptedStrategy = false;//点击过确认的弹窗在APP下次启动自动检查更新时会再次显示
        Beta.autoDownloadOnWifi = true;//WIFI自动下载
        /**
         * 自定义Activity参考，通过回调接口来跳转到你自定义的Actiivty中。
         */
        Beta.upgradeListener = new UpgradeListener() {
            @Override
            public void onUpgrade(int ret, UpgradeInfo strategy, boolean isManual, boolean isSilence) {
                if (strategy != null) {
                    Intent i = new Intent();
                    i.setClass(getApplicationContext(), WelComeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                } else {
                    Toast.makeText(getApplicationContext(), "没有更新", Toast.LENGTH_SHORT).show();
                }
            }
        };

    }

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    public static APP getContext() {
        return instance;
    }

    public static SmdtManager getSmdt() {
        return smdt;
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(this).clearMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).trimMemory(level);
    }

    public static void exit() {
        //关闭整个应用
        System.exit(0);
    }
}