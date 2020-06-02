package com.yunbiao.ybsmartcheckin_live_id.business;

import android.util.Log;

import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoClean {
    private static final String TAG = "AutoClean";
    private final ScheduledExecutorService scheduledExecutorService;
    private int INITIAL_TIME = 2;
    private int PERIOD_TIME = 5;

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

            int total = 0;
            for (Sign sign : signList) {
                long diffValue = date.getTime() - sign.getTime();
                if (diffValue < offset) {
                    continue;
                }
                String headPath = sign.getHeadPath();
                String hotImgPath = sign.getHotImgPath();
                File headFile = new File(headPath);
                File hotImgFile = new File(hotImgPath);
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
        },INITIAL_TIME,PERIOD_TIME, TimeUnit.MINUTES);
    }

}
