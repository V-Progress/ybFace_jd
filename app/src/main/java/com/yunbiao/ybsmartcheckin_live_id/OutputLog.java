package com.yunbiao.ybsmartcheckin_live_id;

import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class OutputLog {

    private StringBuffer stringBuffer = new StringBuffer();
    private static OutputLog outputLog;
    private File logFile;

    public static synchronized OutputLog getInstance(){
        if(outputLog == null){
            outputLog = new OutputLog();
        }
        return outputLog;
    }

    public void initFile(String path){
        File dirFile = new File(path);
        if(!dirFile.exists()){
            dirFile.mkdirs();
        }

        logFile = new File(dirFile,"log.txt");
        if(!logFile.exists()){
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileUtils.write(logFile,"第一次写入",true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final String TAG = "OutputLog";
    private OutputLog(){
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            if(logFile != null && logFile.exists() && stringBuffer.length() > 0){
                try {
                    Log.e(TAG, "OutputLog: 写入长度：" + stringBuffer.length());
                    FileUtils.write(logFile,stringBuffer,true);
                    stringBuffer.setLength(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        },1,1, TimeUnit.MINUTES);
    }

    public void addLog(String s){
        stringBuffer.append(s).append("\n");
    }
}
