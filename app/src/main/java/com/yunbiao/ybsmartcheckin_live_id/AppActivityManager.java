package com.yunbiao.ybsmartcheckin_live_id;

import android.app.Activity;
import android.util.Log;

import java.util.Stack;

public class AppActivityManager {

    private static final String TAG = "AppActivityManager";
    private static Stack<Activity> activityStack;

    private AppActivityManager(){}

    private static volatile AppActivityManager appActivityManager;
    public static AppActivityManager getAppActivityManager() {
        if (appActivityManager == null) {
            synchronized (AppActivityManager.class) {
                if (appActivityManager == null) {
                    appActivityManager = new AppActivityManager();
                }
            }
        }
        return appActivityManager;
    }

    /**
     * 添加Activity到堆栈
     */
    public int getActivitySize(){
        if(activityStack==null){
            activityStack = new Stack<>();
            Log.e(TAG, "############################################" );
        }

     return activityStack.size();
    }
    /**
     * 添加Activity到堆栈
     */
    public void addActivity(Activity activity){
        if(activityStack==null){
            activityStack = new Stack<>();
        }
        activityStack.add(activity);
        Log.e(TAG, "getActivitySize--------------> "+activityStack.size());
    }
    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    public Activity currentActivity(){
        Activity activity=activityStack.lastElement();
        return activity;
    }
    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */
    public void finishActivity(){
        Activity activity=activityStack.lastElement();
        finishActivity(activity);
    }

    /**
     * 结束指定的Activity
     */
    public void finishActivity(Activity activity){
        if(activity!=null){
            Log.e("activitymanager","appmanager --- finishActivity");
            activityStack.remove(activity);
            activity.finish();
        }
    }

    /**
     * 结束指定类名的Activity
     */
    public void finishActivity(Class<?> cls){
        for (Activity activity : activityStack) {
            if(activity.getClass().equals(cls) ){
                finishActivity(activity);
            }
        }
    }

    /**
     * 获取指定类名activity
     */
    public Activity getActivity(Class<?> cls){
        for (Activity activity : activityStack) {
            if(activity.getClass().equals(cls) ){
                return activity;
            }
        }
        return null;
    }

    /**
     * 退出应用程序
     */
    public void AppExit() {
        try {
            finishAllActivity();
            // 杀死该应用进程
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } catch (Exception e) {

        }
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity(){
        for (int i = 0, size = activityStack.size(); i < size; i++){
            if (null != activityStack.get(i)){
                activityStack.get(i).finish();
            }
        }

        activityStack.clear();
        Log.e(TAG, "getActivitySize--------------> "+activityStack.size());
        System.gc();
    }
}
