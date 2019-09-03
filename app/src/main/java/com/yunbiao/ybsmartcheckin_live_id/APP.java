package com.yunbiao.ybsmartcheckin_live_id;

import android.app.Application;
import android.app.smdt.SmdtManager;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


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
import com.yunbiao.ybsmartcheckin_live_id.db.CompBean;
import com.yunbiao.ybsmartcheckin_live_id.db.CompDao;
import com.yunbiao.ybsmartcheckin_live_id.db.DatabaseHelper;
import com.yunbiao.ybsmartcheckin_live_id.db.DepartDao;
import com.yunbiao.ybsmartcheckin_live_id.db.SignDao;
import com.yunbiao.ybsmartcheckin_live_id.db.UserDao;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.exception.CrashHandler2;
import com.yunbiao.ybsmartcheckin_live_id.utils.RestartAPPTool;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.zhy.http.okhttp.OkHttpUtils;

import org.xutils.x;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;


public class APP extends Application {
    private static final String TAG = "APP";
    private static APP instance;
    private static SmdtManager smdt;
    private static UserDao userDao;
    private static SignDao signDao;
    private static DepartDao departDao;
    private static CompDao compDao;
    private static CompBean compBean;
    private static int companyId;

    public static void initCompBean(){
        companyId = SpUtils.getInt(SpUtils.COMPANYID);
        Log.e("APPPPPP", "initCompBean:  ----- " + companyId);
        compBean = compDao.queryByCompId(companyId);
        Log.e("APPPPPP", "initCompBean:  ----- " + compBean);
    }

    public static int getCompanyId() {
        return companyId;
    }

    public static void initCompanyId() {
        companyId = SpUtils.getInt(SpUtils.COMPANYID);
        Constants.DATA_PATH = Constants.CACHE_PATH + companyId + "/data/";
        Constants.ADS_PATH = Constants.CACHE_PATH + companyId +"/ads/";
        Constants.HEAD_PATH = Constants.CACHE_PATH + companyId +"/img/";
        Constants.RECORD_PATH = Constants.CACHE_PATH + companyId +"/rcd/";
        Constants.MEETING_PATH = Constants.CACHE_PATH + companyId +"/meet/";
        Constants.INFO_PATH = Constants.CACHE_PATH + companyId +"/info/";
    }

    public static CompBean getCompBean(){
        return compBean;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initCompanyId();

        initGpio();

        DaoManager.get().initDb();

        initDB();

        cauchException();

        initBugly();

        initUM();

        initUtils();
    }

    //IO引脚
    private int dir_set_io[] = {1, 2, 3, 4};
    //IO口方向，0：输入，1：输出
    private int dir_set_import = 0;
    private int dir_set_export = 1;
    //高低电平，0：低电平，1：高电平
    private int dir_set_value = 0;

    private void initGpio(){
        smdt = SmdtManager.create(this);
        //设置gpio为输出
        if(smdt != null){
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
    }

    private void initXLog(){
        LogConfiguration config = new LogConfiguration.Builder()
                .tag("MY_TAG")                                         // 指定 TAG，默认为 "X-LOG"
                .t()                                                   // 允许打印线程信息，默认禁止
                .st(2)                                                 // 允许打印深度为2的调用栈信息，默认禁止
                .b()                                                   // 允许打印日志边框，默认禁止
//                .jsonFormatter(new MyJsonFormatter())                  // 指定 JSON 格式化器，默认为 DefaultJsonFormatter
//                .xmlFormatter(new MyXmlFormatter())                    // 指定 XML 格式化器，默认为 DefaultXmlFormatter
//                .throwableFormatter(new MyThrowableFormatter())        // 指定可抛出异常格式化器，默认为 DefaultThrowableFormatter
//                .threadFormatter(new MyThreadFormatter())              // 指定线程信息格式化器，默认为 DefaultThreadFormatter
//                .stackTraceFormatter(new MyStackTraceFormatter())      // 指定调用栈信息格式化器，默认为 DefaultStackTraceFormatter
//                .borderFormatter(new MyBoardFormatter())               // 指定边框格式化器，默认为 DefaultBorderFormatter
//                .addObjectFormatter(AnyClass.class,                    // 为指定类添加格式化器
//                        new AnyClassObjectFormatter())                 // 默认使用 Object.toString()
                .build();

        Printer androidPrinter = new AndroidPrinter();             // Printer that print the log using android.util.Log
        Printer consolePrinter = new ConsolePrinter();             // Printer that print the log to console using System.out
        Printer filePrinter = new FilePrinter                      // Printer that print the log to the file system
                .Builder(Environment.getExternalStorageDirectory().getPath() + "/ybLog")                              // Specify the path to save log file
                .fileNameGenerator(new DateFileNameGenerator())        // Default: ChangelessFileNameGenerator("log")
                .backupStrategy(new NeverBackupStrategy())             // Default: FileSizeBackupStrategy(1024 * 1024)
//                .cleanStrategy(new FileLastModifiedCleanStrategy(MAX_TIME))     // Default: NeverCleanStrategy()
//                .flattener(new MyFlattener())                          // Default: DefaultFlattener
                .build();

        XLog.init(                                                 // Initialize XLog
                LogLevel.ALL,                                                // Specify the log configuration, if not specified, will use new LogConfiguration.Builder().build()
                androidPrinter,                                        // Specify printers, if no printer is specified, AndroidPrinter(for Android)/ConsolePrinter(for java) will be used.
                consolePrinter,
                filePrinter);
    }

    public void initDB(){
        try {
            DatabaseHelper.createDatabase(this);
            userDao = new UserDao(this);
            signDao = new SignDao(this);
            departDao = new DepartDao(this);
            compDao = new CompDao(this);
            Log.e("APP", "11111111111111111111111111111");
        } catch (Exception e){
            e.printStackTrace();
            Log.e("APP", "3333333333333333333333333333333");
        }
        Log.e("APP", "222222222222222222222222222 ");
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

    public static CompDao getCompDao() {
        return compDao;
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

    public static void restart(){
        RestartAPPTool.restartAPP(getContext());
    }

    public static void exit() {
        //关闭整个应用
        System.exit(0);
    }
}