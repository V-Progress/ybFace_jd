package com.yunbiao.ybsmartcheckin_live_id;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.xhapimanager.XHApiManager;
import com.bumptech.glide.Glide;
import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.ConsolePrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy;
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;
import com.tencent.bugly.beta.download.DownloadListener;
import com.tencent.bugly.beta.download.DownloadTask;
import com.tencent.bugly.beta.upgrade.UpgradeListener;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.yunbiao.ybsmartcheckin_live_id.activity.WelComeActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.exception.CrashHandler2;
import com.yunbiao.ybsmartcheckin_live_id.receiver.MyProtectService;
import com.yunbiao.ybsmartcheckin_live_id.utils.RestartAPPTool;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.zhy.http.okhttp.OkHttpUtils;

import org.xutils.x;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import skin.support.SkinCompatManager;
import skin.support.app.SkinAppCompatViewInflater;
import skin.support.constraint.app.SkinConstraintViewInflater;
import skin.support.design.app.SkinMaterialViewInflater;
import skin.support.utils.Slog;
import timber.log.Timber;


public class APP extends Application {
    private static final String TAG = "APP";
    private static APP instance;
    private static Activity mainActivity;
    private static List<Activity> activityList = new ArrayList<>();

    /**
     * 获得Activity
     */
    public static Activity getForegroundActivity() {
        for (Activity activity : activityList) {
            if (isForeground(activity, activity.getClass().getSimpleName())) {
                return activity;
            }
        }
        return activityList.get(activityList.size() - 1);
    }

    public static boolean isForeground(Context context, String className) {
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

    public static void addActivity(Activity activity) {
        if (!activityList.contains(activity)) {
            activityList.add(activity);
        }
    }

    public static void removeActivity(Activity activity) {
        if (activityList.contains(activity)) {
            activityList.remove(activity);
        }
    }

    public static void finishAllActivity() {
        for (Activity activity : activityList) {
            activity.finish();
        }
    }

    public static Activity getMainActivity() {
        return mainActivity;
    }

    private static XHApiManager xhApiManager;

    public static void setMainActivity(Activity activity) {
        APP.mainActivity = activity;
    }

    public static XHApiManager getXHApi() {
        return xhApiManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        Constants.Model.initModels(APP.getContext());

        Timber.plant(new Timber.DebugTree());

        initGpio();

        DaoManager.get().initDb();

        cauchException();

//        initBugly();

        initUM();

        initUtils();

//        initSkinManager();
    }

    private void initSkinManager() {
        // 框架换肤日志打印
        Slog.DEBUG = true;
        SkinCompatManager.withoutActivity(this)
                .addStrategy(new CustomSDCardLoader())          // 自定义加载策略，指定SDCard路径
//                .addStrategy(new ZipSDCardLoader())             // 自定义加载策略，获取zip包中的资源
                .addInflater(new SkinAppCompatViewInflater())   // 基础控件换肤
                .addInflater(new SkinMaterialViewInflater())    // material design
                .addInflater(new SkinConstraintViewInflater())  // ConstraintLayout
//                .addInflater(new SkinCardViewInflater())        // CardView v7
//                .addInflater(new SkinCircleImageViewInflater()) // hdodenhof/CircleImageView
//                .addInflater(new SkinFlycoTabLayoutInflater())  // H07000223/FlycoTabLayout
                .setSkinStatusBarColorEnable(true)              // 关闭状态栏换肤
//                .setSkinWindowBackgroundEnable(false)           // 关闭windowBackground换肤
//                .setSkinAllActivityEnable(false)                // true: 默认所有的Activity都换肤; false: 只有实现SkinCompatSupportable接口的Activity换肤
                .loadSkin();
    }

    //IO引脚
    private int dir_set_io[] = {1, 2, 3, 4};
    //IO口方向，0：输入，1：输出
    private int dir_set_import = 0;
    private int dir_set_export = 1;
    //高低电平，0：低电平，1：高电平
    private int dir_set_value = 0;

    private void initGpio() {
        /*try {
            smdt = SmdtManager.create(this);
            //设置gpio为输出
            if (smdt != null) {
                for (int i = 0; i < dir_set_io.length; i++) {
                    int dirToTemp = smdt.smdtSetGpioDirection(dir_set_io[i], dir_set_export, dir_set_value);
                    int result = smdt.smdtSetExtrnalGpioValue(dir_set_io[i], true);
                    if (dirToTemp == 0) {
                        Log.e(TAG, "initUtils: ----- 设置为输出成功");
                    } else {
                        Log.e(TAG, "initUtils: ----- 设置为输出失败");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        /*try{
            xhApiManager = new XHApiManager();
        }catch (Exception e){
            e.printStackTrace();
        }*/
    }

    // -------------------异常捕获-----捕获异常后重启系统-----------------//
    public void cauchException() {
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

        //初始化xutils 3.0
        x.Ext.init(this);

        OkHttpClient build = new OkHttpClient.Builder()
                .connectTimeout(60 * 3, TimeUnit.SECONDS)
                .writeTimeout(60 * 3, TimeUnit.SECONDS)
                .readTimeout(60 * 3, TimeUnit.SECONDS)
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
                return super.onCrashHandleStart(crashType, errorType, errorMessage, errorStack);
            }

            @Override
            public synchronized byte[] onCrashHandleStart2GetExtraDatas(int crashType, String errorType, String errorMessage, String errorStack) {
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
//        Beta.storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);//更新资源保存目录
        Beta.storageDir = new File(Constants.CACHE_PATH);//更新资源保存目录
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
        Beta.registerDownloadListener(new DownloadListener() {
            @Override
            public void onReceive(DownloadTask downloadTask) {

            }

            @Override
            public void onCompleted(DownloadTask downloadTask) {
                File saveFile = downloadTask.getSaveFile();
                Log.e("APPPPP", "onCompleted: 1111111111111111 ----- " + saveFile + " --- " + saveFile.length());
            }

            @Override
            public void onFailed(DownloadTask downloadTask, int i, String s) {

            }
        });

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

    public static void restart() {
        exit();
        RestartAPPTool.restartAPP(getContext());
    }

    public static void bindProtectService() {
        //开启看门狗,只会在开机是启动一次
        getContext().startService(new Intent(APP.getContext(), MyProtectService.class));
    }

    public static void unbindProtectService() {
        getContext().stopService(new Intent(APP.getContext(), MyProtectService.class));
    }

    public static void exit() {
        unbindProtectService();
        finishAllActivity();
        //关闭整个应用
        System.exit(0);
    }
}