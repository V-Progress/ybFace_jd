package com.yunbiao.ybsmartcheckin_live_id.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;

/**
 * 此工具类用来重启APP，只是单纯的重启，不做任何处理。
 * Created by 13itch on 2016/8/5.
 */
public class RestartAPPTool {

    public static void showExitDialog(Activity act,DialogInterface.OnClickListener cancel, DialogInterface.OnClickListener confirm){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(act);
        alertDialog.setCancelable(true);
        alertDialog.setMessage(act.getString(R.string.restartAPPTool_sftcyy));
        alertDialog.setNegativeButton(act.getString(R.string.restartAPPTool_ycdht), cancel);
        alertDialog.setPositiveButton(act.getString(R.string.restartAPPTool_exit),confirm);
        AlertDialog alertDialog1 = alertDialog.create();
        alertDialog1.show();
    }

    /**
     * 重启整个APP
     * @param context
     */
    public static void restartAPP(Context context){

        /**开启一个新的服务，用来重启本APP*/
        Intent intent1=new Intent(context,killSelfService.class);
        intent1.putExtra("PackageName",context.getPackageName());
        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent1.putExtra("Delayed",10000);
        context.startService(intent1);

        /**杀死整个进程**/
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}