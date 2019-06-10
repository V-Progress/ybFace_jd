/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yunbiao.ybsmartcheckin_live_id.xmpp;

import android.util.Log;

/**
 * A thread class for recennecting the server.
 *
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class ReconnectionThread extends Thread {

    private static final String LOGTAG = LogUtil.makeLogTag(ReconnectionThread.class);

    private int waiting = 0;

    private final XmppManager xmppManager;

    ReconnectionThread(XmppManager xmppManager) {
        this.xmppManager = xmppManager;
    }

    public void run() {
        try {
            Log.d(LOGTAG, "Trying to reconnect in " + waiting() + " seconds");
            Thread.sleep(waiting() * 1000);
            xmppManager.connect();
            waiting++;

            //bug fixed : 一次断开之后重连它就不断地进行重连，就算连接登录成功也重连
            if (xmppManager.isConnected()) {
                interrupt();
            }
        } catch (final InterruptedException e) {
            Log.d(LOGTAG, "Trying InterruptedException e ");
            xmppManager.getHandler().post(new Runnable() {
                public void run() {
                    xmppManager.getConnectionListener().reconnectionFailed(e);
                }
            });
        }
    }

    private int waiting() {
//        if (waiting > 20) {
//            return 600;
//        }
//        if (waiting > 13) {
//            return 300;
//        }
//        return waiting <= 7 ? 10 : 60;
        return 60;
    }

}
