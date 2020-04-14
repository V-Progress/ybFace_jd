package com.yunbiao.ybsmartcheckin_live_id.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadManager {

    private static ThreadManager instance;
    private int MAX_CORE_THREAD_NUMBER = 5;
    private final ThreadPoolExecutor threadPoolExecutor;

    public static ThreadManager getInstance() {
        if (instance == null) {
            synchronized (ThreadManager.class) {
                if (instance == null) {
                    instance = new ThreadManager();
                }
            }
        }
        return instance;
    }

    private ThreadManager(){
        LinkedBlockingQueue linkedBlockingQueue = new LinkedBlockingQueue(MAX_CORE_THREAD_NUMBER);
        threadPoolExecutor = new ThreadPoolExecutor(MAX_CORE_THREAD_NUMBER,MAX_CORE_THREAD_NUMBER,1000, TimeUnit.MICROSECONDS,linkedBlockingQueue);
    }

    public void execute(){
        BlockingQueue<Runnable> queue = threadPoolExecutor.getQueue();

    }

}
