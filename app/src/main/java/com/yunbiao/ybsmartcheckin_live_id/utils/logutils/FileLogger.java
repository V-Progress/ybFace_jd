package com.yunbiao.ybsmartcheckin_live_id.utils.logutils;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * 输出log日志到文件夹
 * Created by LiuShao on 2016/3/28.
 */
class FileLogger implements Runnable {
    private static FileLogger inst = new FileLogger();
    private String logPath;// 日志存放的路径

    private final ArrayList<String> logList = new ArrayList<String>();

    public static FileLogger getInstance() {
        return inst;
    }

    void setLogPath(String logPath) {
        if (logPath != null && !logPath.endsWith(File.separator)) {
            this.logPath = logPath + File.separator;
        } else {
            this.logPath = logPath;
        }
    }

    private boolean isSDCardAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    void addLog(String TAG, String message) {
        if (!isSDCardAvailable() || logPath == null) {
            return;
        }
        synchronized (this) {
            logList.add("[" + getTime() + "][" + TAG + "]  " + message);
            notifyWrite();
        }
    }

    void addLog(String TAG, Throwable e) {
        if (!isSDCardAvailable() || logPath == null) {
            return;
        }
        synchronized (this) {
            addLog(TAG, e.toString());
            StackTraceElement[] elements = e.getStackTrace();
            for (StackTraceElement element : elements) {
                logList.add("    " + element.toString() + "/n");
            }
            Throwable cause = e.getCause();
            if (cause != null) {
                logList.add("    Caused by: " + cause.toString() + "/n");
                elements = cause.getStackTrace();
                for (StackTraceElement element : elements) {
                    logList.add("    " + element.toString());
                }
            }
            notifyWrite();
        }
    }

    void addLog(String TAG, String message, Throwable e) {
        if (!isSDCardAvailable() || logPath == null) {
            return;
        }
        synchronized (this) {
            addLog(TAG, message);
            addLog(TAG, e);
        }
    }

    private String getLog() {
        synchronized (this) {
            if (logList.size() > 0) {
                return logList.remove(0);
            }
            return null;
        }
    }

    private Thread thread;
    private boolean isRunning;

    private void notifyWrite() {
        if (!isRunning && isSDCardAvailable()) {
            isRunning = true;
            thread = new Thread(this);
            thread.start();
        }
    }

    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM-dd HH:mm:ss.SSS",
            Locale.getDefault());
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
            Locale.getDefault());

    public static String getTime() {
        return dateTimeFormat.format(new Date());
    }

    private String getLogFileName() {
        return dateFormat.format(new Date()) + ".txt";
    }

    public void stop() {
        isRunning = false;
    }

    @Override
    public void run() {
        FileOutputStream fos = null;
        BufferedWriter writer = null;
        int count = 0;

        while (true) {
            String log = getLog();
            if (!isRunning) {
                break;
            }
            if (log == null) {
                if (count > 5) {
                    break;
                } else {
                    ++count;
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ignored) {
                    }
                    continue;
                }
            }

            if (!isSDCardAvailable() || logPath == null) {
                break;
            }
            try {

                String logDir = logPath;
                Utils.createDirs(new File(logDir));
                File file = new File(logDir + getLogFileName());

                if (!file.exists()) {

                    file.createNewFile();
                }

                fos = new FileOutputStream(file, true);
                writer = new BufferedWriter(new OutputStreamWriter(fos));
                // 写文件
                writer.write(log);
                writer.newLine();
            } catch (Throwable e) {
                e.printStackTrace();
                break;
            } finally {
                CloseUtils.close(writer);
                CloseUtils.close(fos);
            }
        }
        isRunning = false;
        thread = null;
    }
}

