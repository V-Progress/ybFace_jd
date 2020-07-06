package com.yunbiao.ybsmartcheckin_live_id.business;

import android.util.Log;

import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Record5Inch;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class AutoClean {
    private static final String TAG = "AutoClean";
    private final ScheduledExecutorService scheduledExecutorService;
    private int INITIAL_TIME = 10;
    private int PERIOD_TIME = 60;

    public AutoClean(){
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public AutoClean(int initialTime,int periodTime){
        INITIAL_TIME = initialTime;
        PERIOD_TIME = periodTime;
        scheduledExecutorService = Executors.newScheduledThreadPool(3);
    }

    public void startAutoClear(){
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            Log.e(TAG, "startAutoClear: 执行自动清除");
            long policy;
            switch (SpUtils.getIntOrDef(Constants.Key.CLEAR_POLICY,Constants.Default.CLEAR_POLICY)) {
                case 0:
                    policy = 7;
                    break;
                case 1:
                    policy = 15;
                    break;
                case 3:
                    policy = SpUtils.getIntOrDef(Constants.Key.CLEAR_POLICY_CUSTOM,Constants.Default.CLEAR_POLICY_CUSTOM);
                    break;
                default:
                    policy = 30;
                    break;
            }
            Date date = new Date();
            List<Sign> signList = DaoManager.get().queryAll(Sign.class);
            if(signList == null || signList.size() <= 0){
                Log.e(TAG, "startAutoClear: 暂无数据");
                return;
            }
            long offset = policy * 24 * 60 * 60 * 1000;

            Log.e(TAG, "startAutoClear: 清除策略：" + policy);
            Log.e(TAG, "startAutoClear: 时间差： " + offset);

            List<String> fileList = new ArrayList<>();
            int total = 0;
            for (Sign sign : signList) {
                String headPath = sign.getHeadPath();
                String hotImgPath = sign.getHotImgPath();
                File headFile = new File(headPath);
                File hotImgFile = new File(hotImgPath);
                fileList.add(headFile.getName());
                fileList.add(hotImgFile.getName());

                long diffValue = date.getTime() - sign.getTime();
                if (diffValue < offset) {
                    continue;
                }
                if(headFile.exists()){
                    headFile.delete();
                }
                if(hotImgFile.exists()){
                    hotImgFile.delete();
                }
                DaoManager.get().deleteSign(sign);
                total ++;
            }
            Log.e(TAG, "startAutoClear: 总共已清除：" + total);

            if(fileList.size() > 0){
                long deleteNumber = 0;
                File cacheDir = new File(Constants.CACHE_PATH);
                if(!cacheDir.exists()){
                    return;
                }
                Timber.d(cacheDir.getName());
                //cache目录
                File[] compaDirs = cacheDir.listFiles();
                if(compaDirs == null || compaDirs.length <= 0){
                    return;
                }
                for (File compDir : compaDirs) {
                    if(!compDir.isDirectory()){
                        continue;
                    }

                    //各公司目录
                    File[] rcdDirs = compDir.listFiles(pathname -> pathname.isDirectory() && pathname.getName().contains("rcd"));
                    if(rcdDirs == null || rcdDirs.length <= 0){
                        continue;
                    }
                    for (File rcd : rcdDirs) {
                        if(!rcd.isDirectory()){
                            continue;
                        }

                        File[] dateDirs = rcd.listFiles();
                        if(dateDirs == null || dateDirs.length <= 0){
                            continue;
                        }
                        for (File dateDir : dateDirs) {
                            if(!dateDir.isDirectory()){
                                continue;
                            }

                            File[] imgFiles = dateDir.listFiles();
                            if(imgFiles == null || imgFiles.length <= 0){
                                continue;
                            }
                            for (File imgFile : imgFiles) {
                                if(imgFile.isDirectory()){
                                    continue;
                                }
                                if(fileList.contains(imgFile.getName())){
                                    continue;
                                }
                                imgFile.delete();
                                deleteNumber++;
                            }
                        }
                    }
                }
                Timber.d("总共删除垃圾文件：" + deleteNumber);
            }
        },INITIAL_TIME,PERIOD_TIME, TimeUnit.MINUTES);
    }

    private void checkGarbageFiles(File file,List<String> fileList){
        //检查是否是日期目录
        boolean isDateDir = file.getParent().contains("rcd");
        //检查是否包含rcd目录
        String[] list = file.list();
        boolean hasRCD =  list != null && Arrays.asList(list).contains("rcd");
        Timber.d("当前目录：--- " + file.getPath() + " ---是否是日期目录---" + isDateDir + " ---是否包含rcd目录---" + hasRCD);

        File[] files = file.listFiles();
        Timber.d("当前目录下的文件：" + (files == null ? 0 : files.length));
        if(files == null){
            return;
        }
        for (File childFile : files) {
            //如果是日期目录
            if(isDateDir){
                if (fileList.contains(childFile.getName())) {
                    continue;
                }
                Timber.d(childFile.getName());
                childFile.delete();
            } else

            //如果包含rcd目录
            if(hasRCD){
                //如果是rcd目录，则继续遍历
                if(childFile.getName().contains("rcd")){
                    checkGarbageFiles(childFile,fileList);
                }
            } else

            //以上两个都不成立，则判断是否是文件夹
            if(childFile.isDirectory()){
                checkGarbageFiles(childFile,fileList);
            }
        }
    }

    public void startAutoClear5InchRecord(){
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            Log.e(TAG, "startAutoClear: 执行自动清除");
            long policy;
            switch (SpUtils.getIntOrDef(Constants.Key.CLEAR_POLICY,Constants.Default.CLEAR_POLICY)) {
                case 0:
                    policy = 7;
                    break;
                case 1:
                    policy = 15;
                    break;
                case 3:
                    policy = SpUtils.getIntOrDef(Constants.Key.CLEAR_POLICY_CUSTOM,Constants.Default.CLEAR_POLICY_CUSTOM);
                    break;
                default:
                    policy = 30;
                    break;
            }
            Date date = new Date();
            List<Record5Inch> recordList = DaoManager.get().queryAll(Record5Inch.class);
            if(recordList == null || recordList.size() <= 0){
                Log.e(TAG, "startAutoClear5InchRecord: 暂无数据");
                return;
            }
            long offset = policy * 24 * 60 * 60 * 1000;

            Log.e(TAG, "startAutoClear5InchRecord: 清除策略：" + policy);
            Log.e(TAG, "startAutoClear5InchRecord: 时间差： " + offset);

            int total = 0;
            for (Record5Inch record : recordList) {
                long diffValue = date.getTime() - record.getTime();
                if (diffValue < offset) {
                    continue;
                }
                String imgPath = record.getImgPath();
                File imgFile = new File(imgPath);
                if(imgFile.exists()){
                    imgFile.delete();
                }
                DaoManager.get().delete5InchRecord(record);
                total ++;
            }
            Log.e(TAG, "startAutoClear5InchRecord: 总共已清除：" + total);
        },INITIAL_TIME,PERIOD_TIME, TimeUnit.MINUTES);
    }

}
