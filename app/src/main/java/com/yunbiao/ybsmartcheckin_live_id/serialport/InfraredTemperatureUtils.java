package com.yunbiao.ybsmartcheckin_live_id.serialport;

import android.os.AsyncTask;
import android.text.TextUtils;

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
 *
 * 7、TN905元件没有环境温度数据，现以测量温度同时作为环境温度，在没人的时获取环境温度为正确的环境温度
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
    //测量温度校正值，默认为0.0
    private float mCorrectionValue = 0.0f;

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
            if ((!readStr.equals("BB") && !readStr.equals("CC")) && lastReceive == "") {
                return;
            }
            String allReceive = lastReceive + readStr;
            lastReceive += readStr;
            if (allReceive.startsWith("BB") && allReceive.endsWith("0D0A")) {
                if (allReceive.length() == 84) {
                    analyticalTemperatureNLX90614(allReceive);
                    lastReceive = "";
                } else {
                    //无效数据
                    lastReceive = "";
                }
            }
            if (allReceive.startsWith("CC") && allReceive.endsWith("0D0A")) {
                if (allReceive.length() == 20) {
                    analyticalTemperatureTN905(allReceive);
                    lastReceive = "";
                } else {
                    //无效数据
                    lastReceive = "";
                }
            }
        }
    }

    //解析温度 - NLX90614元件
    private void analyticalTemperatureNLX90614(String tData) {
//        Log.i(LOGTAG, "tData = " + tData);
        String[] tempDatas = tData.split("20A1E3430A");

        String ambientTemperatureStr = tempDatas[0].substring(20, 30);
        ambientTemperatureF = Float.valueOf(new String(HexUtil.hexStr2Bytes(ambientTemperatureStr)));
//        Log.i(LOGTAG, "ambientTemperatureF = " + ambientTemperatureF);

        String measuringTemperatureStr = tempDatas[1].substring(20, 30);
        measuringTemperatureF = Float.valueOf(new String(HexUtil.hexStr2Bytes(measuringTemperatureStr)));
        if (measuringTemperatureF < 31.00f) {
            measuringTemperatureF = Float.valueOf(new BigDecimal(measuringTemperatureF).add(new BigDecimal(5.00f)).toString());
        } else if (31.00f <= measuringTemperatureF && measuringTemperatureF <= 31.90f) {
            measuringTemperatureF = Float.valueOf(new BigDecimal(measuringTemperatureF).add(new BigDecimal(4.90f)).toString());
        } else if (32.00f <= measuringTemperatureF && measuringTemperatureF <= 32.50f) {
            measuringTemperatureF = Float.valueOf(new BigDecimal(measuringTemperatureF).add(new BigDecimal(4.05f)).toString());
        } else if (32.60f <= measuringTemperatureF && measuringTemperatureF <= 32.90f) {
            measuringTemperatureF = Float.valueOf(new BigDecimal(measuringTemperatureF).add(new BigDecimal(3.75f)).toString());
        } else if (33.00f <= measuringTemperatureF && measuringTemperatureF <= 33.50f) {
            measuringTemperatureF = Float.valueOf(new BigDecimal(measuringTemperatureF).add(new BigDecimal(3.30f)).toString());
        } else if (33.60f <= measuringTemperatureF) {
            measuringTemperatureF = Float.valueOf(new BigDecimal(measuringTemperatureF).add(new BigDecimal(2.87f)).toString());
        }
//        measuringTemperatureF = Float.valueOf(new BigDecimal(measuringTemperatureF).add(new BigDecimal(mCorrectionValue)).toString());
//        Log.i(LOGTAG, "measuringTemperatureF = " + measuringTemperatureF);
    }

    //解析温度 - TN905元件
    private void analyticalTemperatureTN905(String tData) {
//        Log.i(LOGTAG, "tData = " + tData);
        String measuringTemperatureStr = tData.substring(2, 12);
        ambientTemperatureF = Float.valueOf(new String(HexUtil.hexStr2Bytes(measuringTemperatureStr)));
        measuringTemperatureF = Float.valueOf(new String(HexUtil.hexStr2Bytes(measuringTemperatureStr)));

        /*if (measuringTemperatureF < 29.f) {
            measuringTemperatureF = Float.valueOf(new BigDecimal(measuringTemperatureF).add(new BigDecimal(7.00f)).toString());
        } else if (29.00f <= measuringTemperatureF && measuringTemperatureF <= 29.50f) {
            measuringTemperatureF = Float.valueOf(new BigDecimal(measuringTemperatureF).add(new BigDecimal(6.80f)).toString());
        } else if (29.60f <= measuringTemperatureF && measuringTemperatureF <= 29.90f) {
            measuringTemperatureF = Float.valueOf(new BigDecimal(measuringTemperatureF).add(new BigDecimal(6.60f)).toString());
        } else if (30.00f <= measuringTemperatureF && measuringTemperatureF <= 30.40f) {
            measuringTemperatureF = Float.valueOf(new BigDecimal(measuringTemperatureF).add(new BigDecimal(6.10f)).toString());
        } else if (31.00f <= measuringTemperatureF && measuringTemperatureF <= 31.40f) {
            measuringTemperatureF = Float.valueOf(new BigDecimal(measuringTemperatureF).add(new BigDecimal(5.40f)).toString());
        } else if (31.50f <= measuringTemperatureF && measuringTemperatureF <= 31.90f) {
            measuringTemperatureF = Float.valueOf(new BigDecimal(measuringTemperatureF).add(new BigDecimal(5.20f)).toString());
        } else if (measuringTemperatureF >= 32.0) {
            measuringTemperatureF = Float.valueOf(new BigDecimal(measuringTemperatureF).add(new BigDecimal(4.20f)).toString());
        }*/
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
        if (aCorrectionValue > 0) {
            return new BigDecimal(ambientTemperatureF).add(new BigDecimal(aCorrectionValue)).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
        } else {
            return new BigDecimal(ambientTemperatureF).subtract(new BigDecimal(Math.abs(aCorrectionValue))).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
        }
    }

    //获取测量温度
    public float getMeasuringTemperatureF() {
        if (mCorrectionValue > 0) {
            return new BigDecimal(measuringTemperatureF).add(new BigDecimal(mCorrectionValue)).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
        } else {
            return new BigDecimal(measuringTemperatureF).subtract(new BigDecimal(Math.abs(mCorrectionValue))).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
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
