package com.yunbiao.ybsmartcheckin_live_id.utils;

import android.os.Handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUitls {
    private static ExecutorService executorService = Executors.newFixedThreadPool(5);


    public static void runInThread(Runnable r) {
        executorService.execute(r);
    }

    public static Handler handler = new Handler();

    public static void runInUIThread(Runnable r) {
        handler.post(r);
    }

}