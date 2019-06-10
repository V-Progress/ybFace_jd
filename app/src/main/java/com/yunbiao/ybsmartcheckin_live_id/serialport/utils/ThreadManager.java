package com.yunbiao.ybsmartcheckin_live_id.serialport.utils;

import android.os.AsyncTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chen on 2019/4/3.
 */

public class ThreadManager {

    //固定的线程数
    private static final int FIXED_NUMBER = 3;

    private static ThreadManager instance;

    private final ExecutorService mAsyncTaskPool;

    //门控
    private final ExecutorService gatePool;

    private ThreadManager() {
        mAsyncTaskPool = Executors.newCachedThreadPool();
        gatePool = Executors.newFixedThreadPool(FIXED_NUMBER);
    }

    synchronized public static ThreadManager getInstance() {
        if (instance == null) {
            instance = new ThreadManager();
        }
        return instance;
    }

    public void addToGateThread(Runnable command) {
        if (command == null || gatePool == null) {
            return;
        }
        gatePool.execute(command);
    }

    public void shutDownGateThread (boolean now) {
        if (gatePool == null) {
            return;
        }
        if (now) {
            gatePool.shutdownNow();
        } else {
            gatePool.shutdown();
        }
    }

    public void executeAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (task == null || mAsyncTaskPool == null) {
            return;
        }
        task.executeOnExecutor(mAsyncTaskPool);
    }

    public void clearThreadResource() {
        shutDownGateThread(false);

        toNull();
    }

    private static void toNull() {
        instance = null;
    }

}
