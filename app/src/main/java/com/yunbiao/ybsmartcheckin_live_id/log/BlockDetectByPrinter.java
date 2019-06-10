package com.yunbiao.ybsmartcheckin_live_id.log;

import android.os.Looper;
import android.util.Printer;

/**
 * Created by Administrator on 2018/12/28.
 */

public class BlockDetectByPrinter {
    private static final boolean isCheck = true;
    public static void start() {
        if(isCheck){
            Looper.getMainLooper().setMessageLogging(new Printer() {
                //分发和处理消息开始前的log
                private static final String START = ">>>>> Dispatching";
                //分发和处理消息结束后的log
                private static final String END = "<<<<< Finished";

                @Override
                public void println(String x) {
                    if (x.startsWith(START)) {
                        //开始计时
                        LogMonitor.getInstance().startMonitor();
                    }
                    if (x.startsWith(END)) {
                        //结束计时，并计算出方法执行时间
                        LogMonitor.getInstance().removeMonitor();
                    }
                }
            });
        }
    }
}
