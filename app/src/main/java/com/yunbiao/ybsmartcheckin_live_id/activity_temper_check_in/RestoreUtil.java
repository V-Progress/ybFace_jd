package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.io.File;

import timber.log.Timber;

public class RestoreUtil {

    public static void resetAllData(){
        boolean clear = SpUtils.clear();
        Timber.d("清除SP缓存：" + clear);

        File file = new File(Constants.LOCAL_ROOT_PATH);
        if(file.exists()){
            deleteDirWithFile(file);
            Timber.d("删除目录成功");
        }

        DaoManager.get().clearAll();
        Timber.d("清除数据库完毕");

        APP.exit();
    }

    //删除文件夹和文件夹里面的文件
    static void deleteDirWithFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile()){
                file.delete(); // 删除所有文件
            } else if (file.isDirectory()){
                deleteDirWithFile(file); // 递规的方式删除文件夹
            }
        }
        dir.delete();// 删除目录本身
    }
}
