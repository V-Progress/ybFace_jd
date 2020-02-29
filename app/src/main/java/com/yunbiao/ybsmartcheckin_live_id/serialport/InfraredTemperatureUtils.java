package com.yunbiao.ybsmartcheckin_live_id.serialport;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
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
import android_serialport_api.SerialPortFinder;

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
 *
 * -------------------- 2020-2-26 更新 --------------------
 * 1、初始化方法增加串口地址参数portPath，波特率参数baudrate
 *      init(float aCorrectionValue, float mCorrectionValue, String portPath, int baudrate)
 *      initSerialPort(final String portPath, final int baudrate)
 * 2、增加静态方法getAllPortPath()，获取所有串口地址
 * 3、开启热成像32*32模块数据，speed必须大于等于500
 *    startHotImage3232(long speed, HotImageDataCallBack hotImageDataCallBack)
 * 4、关闭热成像3232模块数据
 *    closeHotImage3232()
 *
 * -------------------- 2020-2-27 更新 --------------------
 * 1、startHotImage3232(long speed, boolean isMirror, HotImageDataCallBack hotImageDataCallBack)
 *      增加boolean isMirror参数：true表示热成像画面需要左右镜像翻转
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
    private int msglen_rec = 1;

    //环境温度
    private float ambientTemperatureF = 0.0f;
    //测量温度
    private float measuringTemperatureF = 0.0f;

    //环境温度校正值，默认为0.0
    private float aCorrectionValue = 0.0f;
    //测量温度校正值，默认为0.0
    private float mCorrectionValue = 0.0f;

    //获取图像与温度数据
    private final String GET_IMAGE_DATA = "110000040198";
    private boolean isMirror = true;
    private HotImageDataCallBack hotImageDataCallBack;
    private int minBodyT = 350;
    private int maxBodyT = 400;
    private int bodyPercentage = 8;

    private int[] colorArray = {
            /* 0   */ 	0xFF1E1E22,
            /* 1   */ 	0xFF1E1E26,
            /* 2   */ 	0xFF1E1E2A,
            /* 3   */ 	0xFF1E1E2E,
            /* 4   */ 	0xFF1E1E32,
            /* 5   */ 	0xFF1E1E36,
            /* 6   */ 	0xFF1E1E3A,
            /* 7   */ 	0xFF1E1E3E,
            /* 8   */ 	0xFF1E1E42,
            /* 9   */ 	0xFF1E1E46,
            /* 10  */ 	0xFF1E1E4A,
            /* 11  */ 	0xFF1E1E4E,
            /* 12  */ 	0xFF1E1E52,
            /* 13  */ 	0xFF1E1E56,
            /* 14  */ 	0xFF1E1E5A,
            /* 15  */ 	0xFF1E1E5E,
            /* 16  */ 	0xFF1E1E62,
            /* 17  */ 	0xFF1E1E66,
            /* 18  */ 	0xFF1E1E6A,
            /* 19  */ 	0xFF1E1E6E,
            /* 20  */ 	0xFF1E1E72,
            /* 21  */ 	0xFF1E1E76,
            /* 22  */ 	0xFF1E1E7A,
            /* 23  */ 	0xFF1E1E7E,
            /* 24  */ 	0xFF1E1E82,
            /* 25  */ 	0xFF1E1E86,
            /* 26  */ 	0xFF1E1E8A,
            /* 27  */ 	0xFF1E1E8E,
            /* 28  */ 	0xFF1E1E92,
            /* 29  */ 	0xFF1E1E96,
            /* 30  */ 	0xFF1E1E9A,
            /* 31  */ 	0xFF1E1E9E,
            /* 32  */ 	0xFF1E1EA2,
            /* 33  */ 	0xFF1E1EA6,
            /* 34  */ 	0xFF1E1EAA,
            /* 35  */ 	0xFF1E1EAE,
            /* 36  */ 	0xFF1E1EB2,
            /* 37  */ 	0xFF1E1EB6,
            /* 38  */ 	0xFF1E1EBA,
            /* 39  */ 	0xFF1E1EBE,
            /* 40  */ 	0xFF1E1EC2,
            /* 41  */ 	0xFF1E1EC6,
            /* 42  */ 	0xFF1E1ECA,
            /* 43  */ 	0xFF1E1ECE,
            /* 44  */ 	0xFF1E1ED2,
            /* 45  */ 	0xFF1E1ED6,
            /* 46  */ 	0xFF1E1EDA,
            /* 47  */ 	0xFF1E1EDE,
            /* 48  */ 	0xFF1E1EE2,
            /* 49  */ 	0xFF1E1EE6,
            /* 50  */ 	0xFF1E1EEA,
            /* 51  */ 	0xFF1E1EEE,
            /* 52  */ 	0xFF1E1EF2,
            /* 53  */ 	0xFF1E1EF6,
            /* 54  */ 	0xFF1E1EFA,
            /* 55  */ 	0xFF1E1EFE,
            /* 56  */ 	0xFF201EFE,
            /* 57  */ 	0xFF221EFE,
            /* 58  */ 	0xFF241EFE,
            /* 59  */ 	0xFF261EFE,
            /* 60  */ 	0xFF281EFE,
            /* 61  */ 	0xFF2A1EFE,
            /* 62  */ 	0xFF2C1EFE,
            /* 63  */ 	0xFF2E1EFE,
            /* 64  */ 	0xFF301EFE,
            /* 65  */ 	0xFF321EFE,
            /* 66  */ 	0xFF341EFE,
            /* 67  */ 	0xFF361EFE,
            /* 68  */ 	0xFF381EFE,
            /* 69  */ 	0xFF3A1EFE,
            /* 70  */ 	0xFF3C1EFE,
            /* 71  */ 	0xFF3E1EFE,
            /* 72  */ 	0xFF401EFE,
            /* 73  */ 	0xFF421EFE,
            /* 74  */ 	0xFF441EFE,
            /* 75  */ 	0xFF461EFE,
            /* 76  */ 	0xFF481EFE,
            /* 77  */ 	0xFF4A1EFE,
            /* 78  */ 	0xFF4C1EFE,
            /* 79  */ 	0xFF4E1EFE,
            /* 80  */ 	0xFF501EFE,
            /* 81  */ 	0xFF521EFE,
            /* 82  */ 	0xFF541EFE,
            /* 83  */ 	0xFF561EFE,
            /* 84  */ 	0xFF581EFE,
            /* 85  */ 	0xFF5A1EFE,
            /* 86  */ 	0xFF5C1EFE,
            /* 87  */ 	0xFF5E1EFE,
            /* 88  */ 	0xFF601EFE,
            /* 89  */ 	0xFF621EFE,
            /* 90  */ 	0xFF641EFE,
            /* 91  */ 	0xFF661EFE,
            /* 92  */ 	0xFF681EFE,
            /* 93  */ 	0xFF6A1EFE,
            /* 94  */ 	0xFF6C1EFE,
            /* 95  */ 	0xFF6E1EFE,
            /* 96  */ 	0xFF701EFE,
            /* 97  */ 	0xFF721EFE,
            /* 98  */ 	0xFF741EFE,
            /* 99  */ 	0xFF761EFE,
            /* 100 */ 	0xFF781EFE,
            /* 101 */ 	0xFF7A1EFE,
            /* 102 */ 	0xFF7C1EFE,
            /* 103 */ 	0xFF7E1EFE,
            /* 104 */ 	0xFF801EFE,
            /* 105 */ 	0xFF821EFE,
            /* 106 */ 	0xFF841EFE,
            /* 107 */ 	0xFF861EFE,
            /* 108 */ 	0xFF881EFE,
            /* 109 */ 	0xFF8A1EFE,
            /* 110 */ 	0xFF8C1EFE,
            /* 111 */ 	0xFF8E1EFE,
            /* 112 */ 	0xFF9022FC,
            /* 113 */ 	0xFF9226FA,
            /* 114 */ 	0xFF942AF8,
            /* 115 */ 	0xFF962EF6,
            /* 116 */ 	0xFF9832F4,
            /* 117 */ 	0xFF9A36F2,
            /* 118 */ 	0xFF9C3AF0,
            /* 119 */ 	0xFF9E3EEE,
            /* 120 */ 	0xFFA042EC,
            /* 121 */ 	0xFFA246EA,
            /* 122 */ 	0xFFA44AE8,
            /* 123 */ 	0xFFA64EE6,
            /* 124 */ 	0xFFA852E4,
            /* 125 */ 	0xFFAA56E2,
            /* 126 */ 	0xFFAC5AE0,
            /* 127 */ 	0xFFAE5EDE,
            /* 128 */ 	0xFFB062DC,
            /* 129 */ 	0xFFB266DA,
            /* 130 */ 	0xFFB46AD8,
            /* 131 */ 	0xFFB66ED6,
            /* 132 */ 	0xFFB872D4,
            /* 133 */ 	0xFFBA76D2,
            /* 134 */ 	0xFFBC7AD0,
            /* 135 */ 	0xFFBE7ECE,
            /* 136 */ 	0xFFC082CC,
            /* 137 */ 	0xFFC286CA,
            /* 138 */ 	0xFFC48AC8,
            /* 139 */ 	0xFFC68EC6,
            /* 140 */ 	0xFFC892C4,
            /* 141 */ 	0xFFCA96C2,
            /* 142 */ 	0xFFCC9AC0,
            /* 143 */ 	0xFFCE9EBE,
            /* 144 */ 	0xFFD0A2BC,
            /* 145 */ 	0xFFD2A6BA,
            /* 146 */ 	0xFFD4AAB8,
            /* 147 */ 	0xFFD6AEB6,
            /* 148 */ 	0xFFD8B2B4,
            /* 149 */ 	0xFFDAB6B2,
            /* 150 */ 	0xFFDCBAB0,
            /* 151 */ 	0xFFDEBEAE,
            /* 152 */ 	0xFFE0C2AC,
            /* 153 */ 	0xFFE2C6AA,
            /* 154 */ 	0xFFE4CAA8,
            /* 155 */ 	0xFFE6CEA6,
            /* 156 */ 	0xFFE8D2A4,
            /* 157 */ 	0xFFEAD6A2,
            /* 158 */ 	0xFFECDAA0,
            /* 159 */ 	0xFFEEDE9E,
            /* 160 */ 	0xFFF0E29C,
            /* 161 */ 	0xFFF2E69A,
            /* 162 */ 	0xFFF4EA98,
            /* 163 */ 	0xFFF6EE96,
            /* 164 */ 	0xFFF8F294,
            /* 165 */ 	0xFFFAF692,
            /* 166 */ 	0xFFFCFA90,
            /* 167 */ 	0xFFFEFE8E,
            /* 168 */ 	0xFFFEFA8C,
            /* 169 */ 	0xFFFEF68A,
            /* 170 */ 	0xFFFEF288,
            /* 171 */ 	0xFFFEEE86,
            /* 172 */ 	0xFFFEEA84,
            /* 173 */ 	0xFFFEE682,
            /* 174 */ 	0xFFFEE280,
            /* 175 */ 	0xFFFEDE7E,
            /* 176 */ 	0xFFFEDA7C,
            /* 177 */ 	0xFFFED67A,
            /* 178 */ 	0xFFFED278,
            /* 179 */ 	0xFFFECE76,
            /* 180 */ 	0xFFFECA74,
            /* 181 */ 	0xFFFEC672,
            /* 182 */ 	0xFFFEC270,
            /* 183 */ 	0xFFFEBE6E,
            /* 184 */ 	0xFFFEBA6C,
            /* 185 */ 	0xFFFEB66A,
            /* 186 */ 	0xFFFEB268,
            /* 187 */ 	0xFFFEAE66,
            /* 188 */ 	0xFFFEAA64,
            /* 189 */ 	0xFFFEA662,
            /* 190 */ 	0xFFFEA260,
            /* 191 */ 	0xFFFE9E5E,
            /* 192 */ 	0xFFFE9A5C,
            /* 193 */ 	0xFFFE965A,
            /* 194 */ 	0xFFFE9258,
            /* 195 */ 	0xFFFE8E56,
            /* 196 */ 	0xFFFE8A54,
            /* 197 */ 	0xFFFE8652,
            /* 198 */ 	0xFFFE8250,
            /* 199 */ 	0xFFFE7E4E,
            /* 200 */ 	0xFFFE7A4C,
            /* 201 */ 	0xFFFE764A,
            /* 202 */ 	0xFFFE7248,
            /* 203 */ 	0xFFFE6E46,
            /* 204 */ 	0xFFFE6A44,
            /* 205 */ 	0xFFFE6642,
            /* 206 */ 	0xFFFE6240,
            /* 207 */ 	0xFFFE5E3E,
            /* 208 */ 	0xFFFE5A3C,
            /* 209 */ 	0xFFFE563A,
            /* 210 */ 	0xFFFE5238,
            /* 211 */ 	0xFFFE4E36,
            /* 212 */ 	0xFFFE4A34,
            /* 213 */ 	0xFFFE4632,
            /* 214 */ 	0xFFFE4230,
            /* 215 */ 	0xFFFE3E2E,
            /* 216 */ 	0xFFFE3A2C,
            /* 217 */ 	0xFFFE362A,
            /* 218 */ 	0xFFFE3228,
            /* 219 */ 	0xFFFE2E26,
            /* 220 */ 	0xFFFE2A24,
            /* 221 */ 	0xFFFE2622,
            /* 222 */ 	0xFFFE2220,
            /* 223 */ 	0xFFFE1E1E,
            /* 224 */ 	0xFFFE2222,
            /* 225 */ 	0xFFFE2626,
            /* 226 */ 	0xFFFE2A2A,
            /* 227 */ 	0xFFFE2E2E,
            /* 228 */ 	0xFFFE3232,
            /* 229 */ 	0xFFFE3636,
            /* 230 */ 	0xFFFE3A3A,
            /* 231 */ 	0xFFFE3E3E,
            /* 232 */ 	0xFFFE4242,
            /* 233 */ 	0xFFFE4646,
            /* 234 */ 	0xFFFE4A4A,
            /* 235 */ 	0xFFFE4E4E,
            /* 236 */ 	0xFFFE5252,
            /* 237 */ 	0xFFFE5656,
            /* 238 */ 	0xFFFE5A5A,
            /* 239 */ 	0xFFFE5E5E,
            /* 240 */ 	0xFFFE6262,
            /* 241 */ 	0xFFFE6666,
            /* 242 */ 	0xFFFE6A6A,
            /* 243 */ 	0xFFFE6E6E,
            /* 244 */ 	0xFFFE7272,
            /* 245 */ 	0xFFFE7676,
            /* 246 */ 	0xFFFE7A7A,
            /* 247 */ 	0xFFFE7E7E,
            /* 248 */ 	0xFFFE8282,
            /* 249 */ 	0xFFFE8686,
            /* 250 */ 	0xFFFE8A8A,
            /* 251 */ 	0xFFFE8E8E,
            /* 252 */ 	0xFFFE9292,
            /* 253 */ 	0xFFFE9696,
            /* 254 */ 	0xFFFE9A9A,
            /* 255 */ 	0xFFFE9E9E,
            /* 256 */ 	0xFFFEA2A2,
            /* 257 */ 	0xFFFEA6A6,
            /* 258 */ 	0xFFFEAAAA,
            /* 259 */ 	0xFFFEAEAE,
            /* 260 */ 	0xFFFEB2B2,
            /* 261 */ 	0xFFFEB6B6,
            /* 262 */ 	0xFFFEBABA,
            /* 263 */ 	0xFFFEBEBE,
            /* 264 */ 	0xFFFEC2C2,
            /* 265 */ 	0xFFFEC6C6,
            /* 266 */ 	0xFFFECACA,
            /* 267 */ 	0xFFFECECE,
            /* 268 */ 	0xFFFED2D2,
            /* 269 */ 	0xFFFED6D6,
            /* 270 */ 	0xFFFEDADA,
            /* 271 */ 	0xFFFEDEDE,
            /* 272 */ 	0xFFFEE2E2,
            /* 273 */ 	0xFFFEE6E6,
            /* 274 */ 	0xFFFEEAEA,
            /* 275 */ 	0xFFFEEEEE,
            /* 276 */ 	0xFFFEF2F2,
            /* 277 */ 	0xFFFEF6F6,
            /* 278 */ 	0xFFFEFAFA,
            /* 279 */ 	0xFFFEFEFE
    };

    public static InfraredTemperatureUtils getIns() {
        synchronized (InfraredTemperatureUtils.class) {
            if (instance == null) {
                instance = new InfraredTemperatureUtils();
            }
            return instance;
        }
    }

    public void init(float aCorrectionValue, float mCorrectionValue, String portPath, int baudrate) {
        initSerialPort(portPath, baudrate);
        setaCorrectionValue(aCorrectionValue);
        setmCorrectionValue(mCorrectionValue);
    }

    public void initSerialPort(final String portPath, final int baudrate) {
        if (mSerialPort != null) {
            closeSerialPort();
        }
        ThreadManager.getInstance().executeAsyncTask(new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    mSerialPort = SerialPortHelper.newSerialPort(APP.getContext(), portPath, baudrate);
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

    public static String[] getAllPortPath() {
        return new SerialPortFinder().getAllDevicesPath();
    }

    //开启热成像32*32模块数据，speed必须大于等于500
    public void startHotImage3232(boolean isMirror, int minBodyT, int maxBodyT, int bodyPercentage, HotImageDataCallBack hotImageDataCallBack) {
        this.isMirror = isMirror;
        this.minBodyT = minBodyT;
        this.maxBodyT = maxBodyT;
        this.bodyPercentage = bodyPercentage;
        if (hotImageDataCallBack != null) {
            this.hotImageDataCallBack = hotImageDataCallBack;
            msglen_rec = 1024 * 2;
            writeCom(GET_IMAGE_DATA);
        }
    }

    //关闭热成像3232模块数据
    public void closeHotImage3232() {
        hotImageDataCallBack = null;
        msglen_rec = 1;
    }

    //指定串口是否已打开
    public boolean isOpen() {
        return null != mSerialPort;
    }

    public void writeCom(String hexString) {
        byte[] hexBytes = HexUtil.hexStr2Bytes(hexString);
        writeCom(hexBytes);
    }

    private void writeCom(final byte[] data) {
        if (isOpen()) {
            ThreadManager.getInstance().addToSPWriteThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mOutputStream == null) {
                            //串口数据发送错误
                            Log.e(LOGTAG, "---------- 串口数据发送错误 ----------");
                        } else {
                            mOutputStream.write(data);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
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
            if ((!readStr.startsWith("BB") && !readStr.startsWith("CC") && !readStr.startsWith("16")) && lastReceive == "") {
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
            if (allReceive.length() == 12 && allReceive.startsWith("1698") && allReceive.endsWith("1A9C")) {
                //只读传感器温度
                lastReceive = "";
            } else if (allReceive.length() == 2056 && allReceive.startsWith("1698") && allReceive.endsWith("1A9C")) {
                //0~511像素读取温度
                lastReceive = "";
            } else if (allReceive.length() == 4108 && allReceive.startsWith("1698") && allReceive.endsWith("1A9C")) {
                //获取图像与温度数据
                handleImageData(allReceive);
                lastReceive = "";
            } else if (allReceive.length() >= 4108) {
                //其他指令不处理
                lastReceive = "";
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
        if (31.00f <= measuringTemperatureF && measuringTemperatureF <= 31.90f) {
            measuringTemperatureF = new BigDecimal(measuringTemperatureF).add(new BigDecimal(4.90f)).floatValue();
        } else if (32.00f <= measuringTemperatureF && measuringTemperatureF <= 32.50f) {
            measuringTemperatureF = new BigDecimal(measuringTemperatureF).add(new BigDecimal(4.05f)).floatValue();
        } else if (32.60f <= measuringTemperatureF && measuringTemperatureF <= 32.90f) {
            measuringTemperatureF = new BigDecimal(measuringTemperatureF).add(new BigDecimal(3.75f)).floatValue();
        } else if (33.00f <= measuringTemperatureF && measuringTemperatureF <= 33.50f) {
            measuringTemperatureF = new BigDecimal(measuringTemperatureF).add(new BigDecimal(3.30f)).floatValue();
        } else if (33.60f <= measuringTemperatureF) {
            measuringTemperatureF = new BigDecimal(measuringTemperatureF).add(new BigDecimal(2.87f)).floatValue();
        }
//        Log.i(LOGTAG, "measuringTemperatureF = " + measuringTemperatureF);
    }

    //解析温度 - TN905元件
    private void analyticalTemperatureTN905(String tData) {
//        Log.i(LOGTAG, "tData = " + tData);
        String measuringTemperatureStr = tData.substring(2, 12);
        ambientTemperatureF = 0;

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

    //处理图像与温度数据
    private void handleImageData(String imageDataStr) {
        String tempDataStr = imageDataStr.substring(4, 4104);

        //传感器温度
        String sensorTemperatureStr = tempDataStr.substring(0, 4);
        int sensorT = temperatureCalculation(sensorTemperatureStr);

        //图像数据
        String imageStr = tempDataStr.substring(4, 4100);
        Bitmap bmp = Bitmap.createBitmap(32,32, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();

        int maxT = 0;
        int minT = 0;
        int bodyMaxT = 0;
        int bodyNum = 0;

        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                String pixelStr = imageStr.substring((i * 32 * 4) + (j * 4), (i * 32 * 4) + (j * 4) + 4);
                int tempT = temperatureCalculation(pixelStr);
                if (i == 0 && (j == 0 || j == 1 || j == 2 || j == 3 || j == 28 || j == 29 || j == 30 | j == 31)) {

                }
                if (tempT >= minBodyT && tempT <= maxBodyT) {
                    bodyNum += 1;
                    if (tempT > bodyMaxT) {
                        bodyMaxT = tempT;
                    }
                }
                if (i == 0 && j == 0) {
                    maxT = tempT;
                    minT = tempT;
                } else {
                    if (tempT > maxT) {
                        maxT = tempT;
                    }
                    if (tempT < minT) {
                        minT = tempT;
                    }
                }
                paint.setColor(getColor(tempT));
                paint.setStrokeWidth(1);
                if (isMirror) {
                    canvas.drawRect(32 - i, j, 32 - i - 1, j + 1, paint);
                } else {
                    canvas.drawRect(i, j, i + 1, j + 1, paint);
                }
            }
        }
        if (hotImageDataCallBack != null) {
            float sensorF = new BigDecimal(sensorT).divide(new BigDecimal(10), 1, BigDecimal.ROUND_HALF_UP).floatValue();
            float maxF = new BigDecimal(maxT).divide(new BigDecimal(10), 1, BigDecimal.ROUND_HALF_UP).floatValue();
            float minF = new BigDecimal(minT).divide(new BigDecimal(10), 1, BigDecimal.ROUND_HALF_UP).floatValue();
            float bodyMaxF = new BigDecimal(bodyMaxT).divide(new BigDecimal(10), 1, BigDecimal.ROUND_HALF_UP).floatValue();

            if (bodyMaxF >= 35.0 && bodyMaxF <= 35.5) {
                bodyMaxF = new BigDecimal(bodyMaxF).add(new BigDecimal(0.5f)).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
            }
            if (bodyMaxF > 35.5 && bodyMaxF <= 35.7) {
                bodyMaxF = new BigDecimal(bodyMaxF).add(new BigDecimal(0.3f)).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
            }
            if (mCorrectionValue >= 0) {
                bodyMaxF = new BigDecimal(bodyMaxF).add(new BigDecimal(mCorrectionValue)).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
            } else {
                bodyMaxF = new BigDecimal(bodyMaxF).subtract(new BigDecimal(Math.abs(mCorrectionValue))).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
            }
            int tempBodyPercentage = new BigDecimal(bodyNum).divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
            if (tempBodyPercentage >= bodyPercentage) {
                //符合条件的人体温度信息
                hotImageDataCallBack.newestHotImageData(bmp, sensorF, maxF, minF, bodyMaxF, true, tempBodyPercentage);
            } else {
                hotImageDataCallBack.newestHotImageData(bmp, sensorF, maxF, minF, bodyMaxF, false, tempBodyPercentage);
            }
            writeCom(GET_IMAGE_DATA);
        }
    }

    private int temperatureCalculation(String temperatureHexStr) {
        if (temperatureHexStr.startsWith("FF")) {
            //零下温度
            return 0 - (Integer.valueOf(HexUtil.parseHex2Opposite(temperatureHexStr), 16) + 1);
        } else {
            //零上温度
            return Integer.valueOf(temperatureHexStr, 16);
        }
    }

    private int getColor (int temperature) {
        int temp = temperature - 82;
        if (temp <= 0) {
            temp = 0;
        } else if (temp > 279) {
            temp = 279;
        }
        int color = colorArray[temp];
        return color;
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
        if (ambientTemperatureF == 0) {
            return ambientTemperatureF;
        }
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

    public abstract static class HotImageDataCallBack {
        //最新的热成像数据，imageBmp正方形图像数据，sensorT传感器温度，maxT最高温度，minT最低温度，bodyMaxT人体最高温度，isBody是否有人，bodyPercentage人体范围温度占比
        //bodyMaxT作为人体测量温度
        abstract public void newestHotImageData (Bitmap imageBmp, float sensorT, float maxT, float minT, float bodyMaxT, boolean isBody, int bodyPercentage);
    }
}
