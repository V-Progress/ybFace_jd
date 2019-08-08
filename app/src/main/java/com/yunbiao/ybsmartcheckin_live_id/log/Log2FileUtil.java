package com.yunbiao.ybsmartcheckin_live_id.log;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日志输出到文件工具类
 *
 * @author 李超
 * @DateTime 2017/11/6
 * @Version V1.0.0
 * @Description:
 */
public class Log2FileUtil {

    private String TAG = getClass().getSimpleName();

    private static Log2FileUtil INSTANCE;

    private static final int LOG_FILE_MAX_NUM = 2;

    private LogDumper mLogDumper = null;
    private static int mPId;
    private static StringBuilder logHead = new StringBuilder();
//    private static File folder;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

    private static Log2FileUtil getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Log2FileUtil();
        }
        return INSTANCE;
    }
    /**
     * 获取当前版本号
     */
    public static String getVersionName() {
        String version = "";
        try {
            PackageManager packageManager = APP.getContext().getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(APP.getContext().getPackageName(), 0);
            version = packInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }
    private Log2FileUtil() {
        int sdkInt = Build.VERSION.SDK_INT;
        mPId = android.os.Process.myPid();
        logHead.append("APP_VER:"+getVersionName())
                .append("\n")
                .append("SYSTEM_SDK:"+String.valueOf(sdkInt))
                .append("\n")
                .append("BOARD_INFO:"+ CommonUtils.getBroadType())
                .append("\n")
                .append("DEVICE_UNIQUE_NO:" + HeartBeatClient.getDeviceNo())
                .append("\n")
                .append("------------------------------------------------------------------------------------------")
                .append("\n");
    }

    public static void startLogcatManager(Context ctx) {
        getInstance().start();
    }

    private void start() {
        File folder = new File(Environment.getExternalStorageDirectory(), "yunbiao_Log");
        if (!folder.exists()) {
            boolean mkdirs = folder.mkdirs();
            if (!mkdirs) {
                Log.e(TAG, "创建日志存储目录失败");
            }
        }

        //检查过期文件
        checkExpiFile(folder);

        if (mLogDumper == null) {
            mLogDumper = new LogDumper(String.valueOf(mPId), folder);
        }
        mLogDumper.start();
    }

    private static DateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
    /**
     * 检查文件数量
     * 日志文件值保留最近5天的
     */
    private void checkExpiFile(File folder) {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".log");
            }
        };
        File[] logFiles = folder.listFiles(filter);
        if(logFiles == null || logFiles.length <= 0){
            Log.e(TAG,"日志目录中没有文件");
            return;
        }
        for (File logFile : logFiles) {
            String name = logFile.getName().substring(0, 8);
            Date fileDate = new Date();
            Date todayDate = new Date();
            try {
                fileDate = yyyyMMdd.parse(name);
                todayDate = yyyyMMdd.parse(yyyyMMdd.format(new Date()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long day = (todayDate.getTime() - fileDate.getTime()) / (24 * 60 * 60 * 1000);
            if (day > LOG_FILE_MAX_NUM) {
                boolean delete = logFile.delete();
                if (!delete) {
                    Log.e(TAG, "过期日志删除失败");
                }
            }
        }
    }

    /**
     * 写日志文件监控进程
     */
    private class LogDumper extends Thread {
        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        private String cmds = null;
        private String mPID;
        private FileOutputStream out = null;

        public LogDumper(String pid, File dir) {
            mPID = pid;

            try {
                File logFile = new File(dir, yyyyMMdd.format(new Date()) + ".log");
                out = new FileOutputStream(logFile, true);
                if(logFile.length() <= 0){
                    out.write(logHead.toString().getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            /**
             * log level：*:v , *:d , *:w , *:e , *:f , *:s
             * */
            //show log of all level
            cmds = "logcat | grep \"(" + mPID + ")\"";

            //Show the current mPID process level of E and W log.
//            cmds = "logcat *:e | grep \"(" + mPID + ")\"";
//            cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";

            //Print label filtering information
//            cmds = "logcat -s way";
        }

        public void stopLogs() {
            mRunning = false;
        }

        @Override
        public void run() {
            try {
                Log.d(TAG,"\n----------------------日志记录开始------------------------");
                logcatProc = Runtime.getRuntime().exec(cmds);
                mReader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()), 1024);
                String line;
                /**
                 * 写日志文件监控进程。
                 * 日志实时写入到文件中推断进程一直存在，while循环一直进行，但又没有像想象中的一直打印"日志记录.."，而是有日志时才打印
                 * 原因分析：mReader.readLine()读logcatProc流时，未读到结束，内部阻塞，while循序等待，故不会一直执行。这也正式我们
                 * 想要的结果，监控进程一直存在但又不会一直执行。
                 */
                while (mRunning && (line = mReader.readLine()) != null) {
                    if (!mRunning) {
                        break;
                    }
                    if (line.length() == 0) {
                        continue;
                    }
                    if (out != null && line.contains(mPID)) {
                        String time = simpleDateFormat.format(new Date());
                        out.write((time + "---" + line + "\n").getBytes());
                    }
                }
                Log.d(TAG,"-------------------------日志记录结束-----------------------------");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                close(mReader);
                close(out);
            }
        }
    }

    private void stop() {
        if (mLogDumper != null) {
            mLogDumper.stopLogs();
            mLogDumper = null;
        }
    }

    public static void stopLogcatManager() {
        Log2FileUtil.getInstance().stop();
    }

//    public static File[] queryUploadFiles() {
//        FilenameFilter filter = new FilenameFilter() {
//            @Override
//            public boolean accept(File dir, String filename) {
//                return filename.endsWith(".log");
//            }
//        };
//        File[] files = folder.listFiles(filter);
//        return files;
//    }
//
//    public static String getFolerPath() {
//        return folder.getPath();
//    }

    private void close(Closeable closeable){
        if(closeable != null){
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
