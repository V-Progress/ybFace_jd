package com.yunbiao.ybsmartcheckin_live_id.serialport;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.serialport.utils.HexUtil;
import com.yunbiao.ybsmartcheckin_live_id.serialport.utils.ThreadManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;

/**
 * Created by chen on 2020/2/15.
 *
 * 说明：
 * 1、提前调用
 *    InfraredTemperatureUtils.getIns.init(float aCorrectionValue, float mCorrectionValue)
 *    或
 *    InfraredTemperatureUtils.getIns.initSerialPort()
 *    进行初始化，比如在 MainActivity 的 onCreate() 中。
 *
 * 2、在退出程序时调用
 *    InfraredTemperatureUtils.getIns.closeSerialPort()
 *    关闭串口。
 *
 * 3、设置环境温度校正值
 *    InfraredTemperatureUtils.getIns.setaCorrectionValue(float aCorrectionValue)
 *
 * 4、设置测量温度校正值
 *    InfraredTemperatureUtils.getIns.setmCorrectionValue(float mCorrectionValue)
 *
 * 5、获取环境温度
 *    InfraredTemperatureUtils.getIns.getAmbientTemperatureF()
 *
 * 6、获取测量温度
 *    InfraredTemperatureUtils.getIns.getMeasuringTemperatureF()
 */

public class InfraredTemperatureUtils {

    private static final String LOGTAG = "InfraredTemperature";

    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private FileInputStream mInputStream;
    private FileChannel mInputChannel;

    private ReadThread mReadThread;

    private static InfraredTemperatureUtils instance;

    private long readSpeed = 0;

    //环境温度
    private float ambientTemperatureF = 0.0f;
    //测量温度
    private float measuringTemperatureF = 0.0f;

    //环境温度校正值，默认为0.0
    private float aCorrectionValue = 0.0f;
    //测量温度校正值，默认为1.5
    private float mCorrectionValue = 1.5f;

    public static InfraredTemperatureUtils getIns() {
        synchronized (InfraredTemperatureUtils.class) {
            if (instance == null) {
                instance = new InfraredTemperatureUtils();
            }
            return instance;
        }
    }

    public void init(float aCorrectionValue, float mCorrectionValue) {
        initSerialPort();
        setaCorrectionValue(aCorrectionValue);
        setmCorrectionValue(mCorrectionValue);
    }

    public void initSerialPort() {
        if (mSerialPort != null) {
            closeSerialPort();
        }
        ThreadManager.getInstance().executeAsyncTask(new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    mSerialPort = SerialPortHelper.newSerialPort(APP.getContext(), "/dev/ttyS4", 9600);
                    mOutputStream = mSerialPort.getOutputStream();
                    mInputStream = mSerialPort.getInputStream();
                    mInputChannel = mInputStream.getChannel();
                    //串口打开成功
                    startReadThread();
                } catch (IOException e) {
                    //串口打开失败
                    closeSerialPort();
                    e.printStackTrace();
                } catch (SecurityException e) {
                    //串口打开失败，串口被占用
                    closeSerialPort();
                    e.printStackTrace();
                } catch (InvalidParameterException ipe){
                    //串口打开失败，无效参数
                    closeSerialPort();
                    ipe.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        });
    }

    //指定串口是否已打开
    public boolean isOpen() {
        return null != mSerialPort;
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
            try {
                while (isOpen()) {
                    if (isInterrupted()) {
                        break;
                    }
                    read();
                    Thread.sleep(readSpeed);
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
                    handleReadData(readStr);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        return "";
    }

    private String lastReceive = "";
    private void handleReadData(String readStr) {
        if (!TextUtils.isEmpty(readStr)) {
            if (lastReceive == "" && !readStr.equals("BB")) {
                return;
            }
            String allReceive = lastReceive + readStr;
            lastReceive += readStr;
            if (allReceive.endsWith("0D0A")) {
                if (allReceive.length() == 84) {
                    analyticalTemperature(allReceive);
                    lastReceive = "";
                } else {
                    //无效数据
                    lastReceive = "";
                }
            }
        }
    }

    //解析温度
    private void analyticalTemperature(String tData) {
//        Log.i(LOGTAG, "tData = " + tData);
        String[] tempDatas = tData.split("20A1E3430A");

        String ambientTemperatureStr = tempDatas[0].substring(20, 30);
        ambientTemperatureF = Float.valueOf(new BigDecimal(new String(HexUtil.hexStr2Bytes(ambientTemperatureStr))).add(new BigDecimal(String.valueOf(aCorrectionValue))).toString());
//        Log.i(LOGTAG, "ambientTemperatureF = " + ambientTemperatureF);

        String measuringTemperatureStr = tempDatas[1].substring(20, 30);
        measuringTemperatureF = Float.valueOf(new String(HexUtil.hexStr2Bytes(measuringTemperatureStr)));
        if (measuringTemperatureF < 31.00f) {
            measuringTemperatureF = Float.valueOf(new BigDecimal(String.valueOf(measuringTemperatureF)).add(new BigDecimal(String.valueOf(5.00f))).toString());
        } else if (31.00f <= measuringTemperatureF && measuringTemperatureF <= 31.90f) {
            measuringTemperatureF = Float.valueOf(new BigDecimal(String.valueOf(measuringTemperatureF)).add(new BigDecimal(String.valueOf(4.90f))).toString());
        } else if (32.00f <= measuringTemperatureF && measuringTemperatureF <= 32.50f) {
            measuringTemperatureF = Float.valueOf(new BigDecimal(String.valueOf(measuringTemperatureF)).add(new BigDecimal(String.valueOf(4.05f))).toString());
        } else if (32.60f <= measuringTemperatureF && measuringTemperatureF <= 32.90f) {
            measuringTemperatureF = Float.valueOf(new BigDecimal(String.valueOf(measuringTemperatureF)).add(new BigDecimal(String.valueOf(3.75f))).toString());
        } else if (33.00f <= measuringTemperatureF && measuringTemperatureF <= 33.50f) {
            measuringTemperatureF = Float.valueOf(new BigDecimal(String.valueOf(measuringTemperatureF)).add(new BigDecimal(String.valueOf(3.30f))).toString());
        } else if (33.60f <= measuringTemperatureF) {
            measuringTemperatureF = Float.valueOf(new BigDecimal(String.valueOf(measuringTemperatureF)).add(new BigDecimal(String.valueOf(2.87f))).toString());
        }
//        measuringTemperatureF = Float.valueOf(new BigDecimal(new String(HexUtil.hexStr2Bytes(measuringTemperatureStr))).add(new BigDecimal(String.valueOf(mCorrectionValue))).toString());
//        Log.i(LOGTAG, "measuringTemperatureF = " + measuringTemperatureF);
    }

    //设置环境温度校正值
    public void setaCorrectionValue(float aCorrectionValue) {
        this.aCorrectionValue = aCorrectionValue;
    }

    //设置测量温度校正值
    public void setmCorrectionValue(float mCorrectionValue) {
        this.mCorrectionValue = mCorrectionValue;
    }

    //获取环境温度
    public float getAmbientTemperatureF() {
        return ambientTemperatureF;
    }

    //获取测量温度
    public float getMeasuringTemperatureF() {
        return measuringTemperatureF;
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
