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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.util.Log;


import com.yunbiao.ybsmartcheckin_live_id.activity.Event.XmppConnectEvent;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.utils.CheckXmppOnline;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.provider.ProviderManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * This class is to manage the XMPP connection between client and server.
 *
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class XmppManager {
    private static final String LOGTAG = LogUtil.makeLogTag(XmppManager.class);

    private static final String XMPP_RESOURCE_NAME = "AndroidpnClient";

    private Context context;

    private NotificationService.TaskSubmitter taskSubmitter;

    private NotificationService.TaskTracker taskTracker;

    private SharedPreferences sharedPrefs;

    private String xmppHost;

    private int xmppPort;

    private XMPPConnection connection;

    private String username;

    private String password;

    private PersistentConnectionListener connectionListener;

    private PacketListener notificationPacketListener;

    private Handler handler;

    private List<Runnable> taskList;

    private boolean running = false;

    private Future<?> futureTask;

    private Thread reconnection;

    public XmppManager(NotificationService notificationService) {
        context = notificationService;
        taskSubmitter = notificationService.getTaskSubmitter();
        taskTracker = notificationService.getTaskTracker();
        sharedPrefs = notificationService.getSharedPreferences();

        xmppHost = sharedPrefs.getString(Constants.XMPP_HOST, "localhost");
        xmppPort = sharedPrefs.getInt(Constants.XMPP_PORT, 5222);
        username = sharedPrefs.getString(Constants.XMPP_USERNAME, "");
        password = sharedPrefs.getString(Constants.XMPP_PASSWORD, "");

        connectionListener = new PersistentConnectionListener(this);
        notificationPacketListener = new NotificationPacketListener(this);
        CheckXmppOnline.getInstance(this).start();

        handler = new Handler();
        taskList = new ArrayList<>();
    }

    public Context getContext() {
        return context;
    }

    public void connect() {
        Log.e(LOGTAG, "connect()...");
        submitLoginTask();
    }

    public void disconnect() {
        Log.e(LOGTAG, "disconnect()...");
        EventBus.getDefault().post(new XmppConnectEvent(false));
        terminatePersistentConnection();
    }

    public void terminatePersistentConnection() {
        Log.d(LOGTAG, "terminatePersistentConnection()...");
        Runnable runnable = new Runnable() {

            final XmppManager xmppManager = XmppManager.this;

            public void run() {
                if (xmppManager.isConnected()) {
                    Log.d(LOGTAG, "terminatePersistentConnection()... run()");
                    xmppManager.getConnection().removePacketListener(
                            xmppManager.getNotificationPacketListener());
                    xmppManager.getConnection().disconnect();
                }
                xmppManager.runTask();
            }

        };
        addTask(runnable);
    }

    public XMPPConnection getConnection() {
        return connection;
    }

    public void setConnection(XMPPConnection connection) {
        this.connection = connection;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ConnectionListener getConnectionListener() {
        return connectionListener;
    }

    public PacketListener getNotificationPacketListener() {
        return notificationPacketListener;
    }

    public void startReconnectionThread() {
        Thread reconnection = new ReconnectionThread(this);
        reconnection.setName("Xmpp Reconnection Thread");
        reconnection.start();
    }

    public Handler getHandler() {
        return handler;
    }

    public void reregisterAccount() {
        removeAccount();
        submitLoginTask();
        runTask();
    }

    public List<Runnable> getTaskList() {
        return taskList;
    }

    public Future<?> getFutureTask() {
        return futureTask;
    }

    public void runTask() {
        Log.d(LOGTAG, "runTask()...");
        synchronized (taskList) {
            running = false;
            futureTask = null;
            if (!taskList.isEmpty()) {
                Runnable runnable = taskList.get(0);
                taskList.remove(0);
                running = true;
                futureTask = taskSubmitter.submit(runnable);
                if (futureTask == null) {
                    taskTracker.decrease();
                }
            }
        }
        taskTracker.decrease();
        Log.d(LOGTAG, "runTask()...done");
    }


    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    private boolean isAuthenticated() {
        return connection != null && connection.isConnected()
                && connection.isAuthenticated();
    }

    private boolean isRegistered() {
        return sharedPrefs.contains(Constants.XMPP_USERNAME)
                && sharedPrefs.contains(Constants.XMPP_PASSWORD);
    }

    //3
    private void submitConnectTask() {
        Log.e(LOGTAG, "submitConnectTask()...");
        addTask(new ConnectTask());
    }

    //2
    private void submitRegisterTask() {
        Log.e(LOGTAG, "submitRegisterTask()...");
        submitConnectTask();
        addTask(new RegisterTask());
    }

    //1
    private void submitLoginTask() {
        Log.e(LOGTAG, "submitLoginTask()...");
        submitRegisterTask();
        addTask(new LoginTask());
    }

    private void addTask(Runnable runnable) {
        Log.d(LOGTAG, "addTask(runnable)...");
        taskTracker.increase();
        synchronized (taskList) {
            if (taskList.isEmpty() && !running) {
                running = true;
                futureTask = taskSubmitter.submit(runnable);
                if (futureTask == null) {
                    taskTracker.decrease();
                }
            } else {
                Log.d("taskList.size:", "taskList.size():" + taskList.size());
                runTask();//TODO  //解决服务器端重启后,客户端不能成功连接androidpn服务器 未确认是否成功
                taskList.add(runnable);
            }
        }
        Log.d(LOGTAG, "addTask(runnable)... done");
    }

    private void removeAccount() {
        Editor editor = sharedPrefs.edit();
        editor.remove(Constants.XMPP_USERNAME);
        editor.remove(Constants.XMPP_PASSWORD);
        editor.commit();
    }

    /**
     * A runnable task to connect the server.
     */
    private class ConnectTask implements Runnable {

        final XmppManager xmppManager;

        private ConnectTask() {
            this.xmppManager = XmppManager.this;
        }

        public void run() {
            Log.e(LOGTAG, "ConnectTask.run()...");

            if (!xmppManager.isConnected()) {
                // Create the configuration for this new connection
                ConnectionConfiguration connConfig = new ConnectionConfiguration(xmppHost, xmppPort);
                // connConfig.setSecurityMode(SecurityMode.disabled);
                connConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.required/*disabled*/);
                connConfig.setSASLAuthenticationEnabled(false);
                connConfig.setCompressionEnabled(false);

                XMPPConnection connection = new XMPPConnection(connConfig);
                xmppManager.setConnection(connection);
                try {
                    // Connect to the server
                    connection.connect();
                    Log.e(LOGTAG, "XMPP connected successfully");

                    // packet provider
                    ProviderManager.getInstance().addIQProvider("notification", "androidpn:iq:notification", new NotificationIQProvider());
                } catch (XMPPException e) {
                    Log.e(LOGTAG, "XMPP connection failed");
                }
                xmppManager.runTask();
            } else {
                Log.e(LOGTAG, "XMPP connected already");
                xmppManager.runTask();
            }
        }
    }

    /**
     * A runnable task to register a new user onto the server.
     */

    private class RegisterTask implements Runnable {

        final XmppManager xmppManager;
        String newPassword;

        private RegisterTask() {
            xmppManager = XmppManager.this;
        }

        public void run() {
            Log.e(LOGTAG, "RegisterTask.run()...");
            if (!xmppManager.isRegistered()) {
                String newUsername = HeartBeatClient.getDeviceNo();
                String android_model = android.os.Build.MODEL.replaceAll("_", "");
                newPassword = newUsername + "_" + android_model;

                switch (com.yunbiao.ybsmartcheckin_live_id.afinel.Constants.DEVICE_TYPE) {
                    case com.yunbiao.ybsmartcheckin_live_id.afinel.Constants.DeviceType.HT_TEMPERATURE_CHECK_IN:
                    case com.yunbiao.ybsmartcheckin_live_id.afinel.Constants.DeviceType.HT_TEMPERATURE_CHECK_IN_SMT:
                    case com.yunbiao.ybsmartcheckin_live_id.afinel.Constants.DeviceType.HT_TEMPERATURE_CERTIFICATES:
                    case com.yunbiao.ybsmartcheckin_live_id.afinel.Constants.DeviceType.HT_MULTIPLE_THERMAL:
                    case com.yunbiao.ybsmartcheckin_live_id.afinel.Constants.DeviceType.HT_TEMPER_SAFETY_CHECK:
                        newPassword += "HT";
                        break;
                }

                Registration registration = new Registration();

                PacketFilter packetFilter = new AndFilter(new PacketIDFilter(
                        registration.getPacketID()), new PacketTypeFilter(
                        IQ.class));

                Log.d(LOGTAG, "username=" + newUsername + " password=" + newPassword);

                final String finalNewUsername = newUsername;
                PacketListener packetListener = new PacketListener() {

                    public void processPacket(Packet packet) {
                        if (packet instanceof IQ) {
                            IQ response = (IQ) packet;
                            if (response.getType() == IQ.Type.ERROR) {
                                if (!response.getError().toString().contains("409")) {
                                    Log.e(LOGTAG, "Unknown error while registering XMPP account! " + response.getError().getCondition());
                                }
                            } else if (response.getType() == IQ.Type.RESULT) {

                                xmppManager.setUsername(finalNewUsername);
                                xmppManager.setPassword(newPassword);

                                Editor editor = sharedPrefs.edit();
                                editor.putString(Constants.XMPP_USERNAME, finalNewUsername);
                                editor.putString(Constants.XMPP_PASSWORD, newPassword);
                                editor.commit();
                                Log.e(LOGTAG, "Account registered successfully");
                                xmppManager.runTask();
                            }
                        }
                    }
                };

                connection.addPacketListener(packetListener, packetFilter);

                registration.setType(IQ.Type.SET);
                registration.addAttribute("username", newUsername);
                registration.addAttribute("password", newPassword);
                connection.sendPacket(registration);

            } else {
                Log.e(LOGTAG, "Account registered already");
                xmppManager.runTask();
            }
        }
    }

    /**
     * A runnable task to log into the server.
     */
    private class LoginTask implements Runnable {
        XmppManager xmppManager;

        private LoginTask() {
            this.xmppManager = XmppManager.this;
        }

        public void run() {
            Log.e(LOGTAG, "LoginTask.run()...");
            if (!xmppManager.isAuthenticated()) {
                Log.d("99999", "username=" + username);
                Log.d(LOGTAG, "username=" + username);
                Log.d(LOGTAG, "password=" + password);
                if (xmppManager.getUsername().equals("-1")) {//如果用户名为 -1 就需要重新设置 设备编号，直到编号正确为止
                    final String newUsername = HeartBeatClient.getDeviceNo();
                    String android_model = android.os.Build.MODEL.replaceAll("_", "");
                    final String newPassword = newUsername + "_" + android_model;
                    xmppManager.setUsername(newUsername);
                    xmppManager.setPassword(newPassword);
                }
                try {
                    xmppManager.getConnection().login(xmppManager.getUsername(), xmppManager.getPassword(), XMPP_RESOURCE_NAME);
                    Log.e(LOGTAG, "Loggedn in successfully");

                    EventBus.getDefault().post(new XmppConnectEvent(true));

                    // connection listener
                    if (xmppManager.getConnectionListener() != null) {
                        xmppManager.getConnection().addConnectionListener(xmppManager.getConnectionListener());
                    }
                    // packet filter
                    PacketFilter packetFilter = new PacketTypeFilter(NotificationIQ.class);
                    // packet listener
                    PacketListener packetListener = xmppManager.getNotificationPacketListener();
                    connection.addPacketListener(packetListener, packetFilter);

                    getConnection().startKeepAliveThread(xmppManager);
                } catch (XMPPException e) {
                    Log.e(LOGTAG, "LoginTask.run()... xmpp error");
                    Log.e(LOGTAG, "Failed to login to xmpp server. Caused by: " + e.getMessage());
                    String INVALID_CREDENTIALS_ERROR_CODE = "401";
                    String errorMessage = e.getMessage();
                    if (errorMessage != null && errorMessage.contains(INVALID_CREDENTIALS_ERROR_CODE)) {
                        xmppManager.reregisterAccount();
                        return;
                    }
                    xmppManager.startReconnectionThread();
                } catch (Exception e) {
                    Log.e(LOGTAG, "LoginTask.run()... other error");
                    Log.e(LOGTAG, "Failed to login to xmpp server. Caused by: " + e.getMessage());
                    xmppManager.startReconnectionThread();
                }
                xmppManager.runTask();
            } else {
                Log.d(LOGTAG, "Logged in already");
                xmppManager.runTask();
            }
        }
    }

}
