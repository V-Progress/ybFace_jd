package com.yunbiao.ybsmartcheckin_live_id.serialport.plcgate;

import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.serialport.ComResponse;
import com.yunbiao.ybsmartcheckin_live_id.serialport.SerialPortHelper;
import com.yunbiao.ybsmartcheckin_live_id.serialport.utils.CCountDownTimer;
import com.yunbiao.ybsmartcheckin_live_id.serialport.utils.HexUtil;
import com.yunbiao.ybsmartcheckin_live_id.serialport.utils.ThreadManager;
import com.yunbiao.ybsmartcheckin_live_id.xmpp.LogUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;

/**
 * Created by chen on 2019/4/3.
 */

public class GateConnection {

    private static final String LOGTAG = LogUtil.makeLogTag(GateConnection.class);

    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private FileInputStream mInputStream;
    private FileChannel mInputChannel;
    private String mLastRequest;

    private ReadThread mReadThread;
    private CCountDownTimer mEditionTimer;

    private static GateConnection ins;

    private GateConnection() {}

    public static GateConnection getIns() {
        if (ins == null) {
            ins = new GateConnection();
        }
        return ins;
    }

    public void initSerialPort(Context ct) {
        try {
            mSerialPort = SerialPortHelper.newSerialPort(ct, "/dev/ttyS4", 9600);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
            mInputChannel = mInputStream.getChannel();

            startReadThread();
        } catch (IOException e) {
            closeSerialPort();
            e.printStackTrace();
        }catch (InvalidParameterException ipe){
            //无效参数
            closeSerialPort();
            ipe.printStackTrace();
        }catch (SecurityException se){
            //串口被占用
            closeSerialPort();
            se.printStackTrace();
        }
    }

    public boolean isOpen() {
        return null != mSerialPort;
    }

    public void writeCom(String hexString) {
        mLastRequest = hexString;
        byte[] hexBytes = HexUtil.hexStr2Bytes(hexString);
        writeCom(hexBytes);
    }

    public void writeCom(final byte[] data) {
        mLastRequest = HexUtil.bytes2HexStr(data, data.length);
        Log.i(LOGTAG, "---allReceive--- writeStr -----  " + mLastRequest);
        if (isOpen()) {
            ThreadManager.getInstance().addToGateThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mOutputStream == null) {
                            Log.e(LOGTAG, "mOutputStream is null");
                        } else {
                            mOutputStream.write(data);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(LOGTAG, "plc1 error" + e);
                    }
                }
            });
        } else {
            Log.e(LOGTAG, "plc1 serial port is closed");
        }
    }

    private void startReadThread() {
        if (mReadThread != null && mReadThread.isAlive()) {

        } else {
            mReadThread = new ReadThread();
            mReadThread.start();
        }
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
//            while (isOpen()) {
//                if (isInterrupted()) {
//                    break;
//                }
//                read();
//            }
            try {
                while (isOpen()) {
                    if (isInterrupted()) {
                        break;
                    }
                    read();
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String read() {
        try {
            if (mInputStream != null) {
                int msglen_rec = 1;
                ByteBuffer byteBuffer = ByteBuffer.allocate(msglen_rec);
                int bytesRead = mInputChannel.read(byteBuffer);
                byte[] buffer = byteBuffer.array();
                String readStr;
                if (bytesRead > 0) {
                    readStr = HexUtil.bytes2HexStr(buffer, bytesRead);
                    Log.i("readStr", "readStr = " + readStr);

                    ComResponse response = new ComResponse();
                    response.setReadSuccess(true);
                    response.setRequest(mLastRequest);
                    response.setResponse(readStr);
                    sendBroadCast(response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        return "";
    }

    private void sendBroadCast(ComResponse comResponse) {
        Intent intent = new Intent();
        intent.setAction(SerialPortHelper.ACTION_GATE_RECEIVE);
        intent.putExtra("comResponse", comResponse);
        LocalBroadcastManager.getInstance(APP.getContext()).sendBroadcast(intent);
    }

    public void startTimeOutTimer(final String command, long time) {
        mEditionTimer = new CCountDownTimer(time, time) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                Log.e(LOGTAG, "read market time out");
                if (!TextUtils.isEmpty(command)) {
                    ComResponse response = new ComResponse();
                    response.setRequest(command);
                    response.setReadSuccess(false);
                    response.setResponse("");
                    response.setStatus(GateCommands.STATUS_READ_TIMEOUT);
                    sendBroadCast(response);
                }
            }
        };
        mEditionTimer.start();
    }

    public void cancelEditionTimer() {
        if (mEditionTimer != null) {
            mEditionTimer.cancel();
        }
    }

    public void closeSerialPort() {
        try {
            if (mOutputStream != null) {
                mOutputStream.close();
            }
            if (mInputStream != null) {
                mInputChannel.close();
            }
            if (mInputChannel != null) {
                mInputChannel.close();
            }
            if (mSerialPort != null) {
                mSerialPort.close();
                mSerialPort = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
