package com.yunbiao.ybsmartcheckin_live_id.business;

import android.text.TextUtils;

import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ResourceCleanManager {
    private static final String TAG = "ResourceCleanManager";
    private static ResourceCleanManager resourceCleanManager = new ResourceCleanManager();
    private final ScheduledExecutorService cleanThread;
    private final int CLEAN_TIME_OFFSET = 4;
    private final int MAX_SIZE = 10000;
    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    private ResourceCleanManager() {
        cleanThread = Executors.newSingleThreadScheduledExecutor();
    }

    public static ResourceCleanManager instance() {
        return resourceCleanManager;
    }

    public void startAutoCleanService() {
        cleanThread.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                String currentFaceCachePath = Constants.RECORD_PATH;
                File currFaceDir = new File(currentFaceCachePath);
                if (!currFaceDir.exists()) {
                    return;
                }
                File[] files = currFaceDir.listFiles();
                if (files == null) {
                    return;
                }

                int fileCount = 0;
                long fileTotal = 0;
                for (File file : files) {
                    if (file.isDirectory()) {
                        File[] files1 = file.listFiles();
                        for (File file1 : files1) {
                            fileCount++;
                            fileTotal += file1.length();
                        }
                    }
                }

                long gb = fileTotal / 1024 / 1024 / 1024;
                if (fileCount > MAX_SIZE || gb > 2) {
                    String tempPath = null;
                    Date tempDate = new Date();
                    try {
                        for (File file : files) {
                            Date parse = dateFormat.parse(file.getName());
                            if (parse.before(tempDate)) {
                                tempDate = parse;
                                tempPath = file.getPath();
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return;
                    }

                    if(TextUtils.isEmpty(tempPath)){
                        new File(tempPath).delete();
                    }
                }
            }
        }, 1, CLEAN_TIME_OFFSET, TimeUnit.HOURS);
    }
}
