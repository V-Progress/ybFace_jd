package com.yunbiao.ybsmartcheckin_live_id;

import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class OutputLog {

    private StringBuffer stringBuffer = new StringBuffer();
    private StringBuffer exportStringBuffer = new StringBuffer();
    private StringBuffer multipleStringBuffer = new StringBuffer();
    private static OutputLog outputLog;
    private File logFile;
    private File excelLogFile;
    private File multipleLogFile;

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

        excelLogFile = new File(dirFile,"export_log");
        if(!excelLogFile.exists()){
            if(!excelLogFile.getParentFile().exists()){
                excelLogFile.getParentFile().mkdirs();
            }
            try {
                boolean newFile = excelLogFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        multipleLogFile = new File(dirFile,"multiple_log");
        if(!multipleLogFile.exists()){
            try {
                multipleLogFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static final String TAG = "OutputLog";
    private OutputLog(){
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            if(logFile != null && logFile.exists() && stringBuffer.length() > 0){
                try {
                    Log.e(TAG, "OutputLog: 写入长度：" + stringBuffer.length());
                    FileUtils.write(logFile,stringBuffer,multipleLogFile.length() < 10 * 1024 * 1024);
                    stringBuffer.setLength(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(excelLogFile != null && excelLogFile.exists() && exportStringBuffer.length() > 0){
                try {
                    FileUtils.write(excelLogFile,exportStringBuffer,multipleLogFile.length() < 10 * 1024 * 1024);
                    exportStringBuffer.setLength(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(multipleLogFile != null && multipleLogFile.exists() && multipleStringBuffer.length() > 0){
                try {
                    FileUtils.write(multipleLogFile,multipleStringBuffer,multipleLogFile.length() < 10 * 1024 * 1024);
                    multipleStringBuffer.setLength(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        },1,1, TimeUnit.MINUTES);
    }

    public void addLog(String s){
        stringBuffer.append(s).append("\n");
    }

    public void addMultipleLog(String log){
        multipleStringBuffer.append(log).append("\n");
    }

    public void addExportLog(String s){
        exportStringBuffer.append(s).append("\n");
    }
}
