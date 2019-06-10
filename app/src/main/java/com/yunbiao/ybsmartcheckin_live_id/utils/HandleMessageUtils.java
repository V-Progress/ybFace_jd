package com.yunbiao.ybsmartcheckin_live_id.utils;

import android.os.Handler;
import android.os.Message;

public class HandleMessageUtils {
	
	public void sendHandler(int what, Handler handler, Object key){
		handler.removeMessages(what);
		Message msg = new Message();
		msg.what = what;
		handler.obtainMessage(what,key).sendToTarget();
	}
	
	public  void runInThread(Runnable r) {
		new Thread(r).start();
	}

    private static HandleMessageUtils handleMessageUtils;
    public static HandleMessageUtils getInstance(){
        if(handleMessageUtils == null){
            handleMessageUtils = new HandleMessageUtils();
        }
        return handleMessageUtils;
    }


}
