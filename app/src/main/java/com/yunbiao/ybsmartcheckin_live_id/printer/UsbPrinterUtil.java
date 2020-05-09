package com.yunbiao.ybsmartcheckin_live_id.printer;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.text.TextUtils;

import com.printsdk.cmd.PrintCmd;

import java.util.Collection;

public class UsbPrinterUtil {
    private static final String TAG = "UsbPrinterUtil";
    private static UsbPrinterUtil instance;
    private Context mContext;
    private UsbManager mUsbManager;
    private UsbDriver mUsbDriver;
    private static final String ACTION_USB_PERMISSION =  "com.usb.sample.USB_PERMISSION";
    private UsbDevice mUsbDev1;
    private UsbDevice mUsbDev;
    private UsbDevice mUsbDev2;
    private UsbReceiver mUsbReceiver;

    private final static int PID11 = 8211;
    private final static int PID13 = 8213;
    private final static int PID15 = 8215;
    private final static int VENDORID = 1305;

    public static synchronized UsbPrinterUtil getInstance(){
        if(instance == null)
            instance = new UsbPrinterUtil();
        return instance;
    }

    public void initUsbPrinter(Context context){
        if(context == null)
            throw new RuntimeException("context can not be null!");
        mContext = context;
        initDriverService();
        getPrinterConnectStatus();
    }

    private void initDriverService(){
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        mUsbDriver = new UsbDriver(mUsbManager, mContext);
        PendingIntent permissionIntent1 = PendingIntent.getBroadcast(mContext, 0,
                new Intent(ACTION_USB_PERMISSION), 0);
        mUsbDriver.setPermissionIntent(permissionIntent1);
        // Broadcast listen for new devices

        mUsbReceiver = new UsbReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mContext.registerReceiver(mUsbReceiver, filter);
    }

    /*
     *  BroadcastReceiver when insert/remove the device USB plug into/from a USB port
     *  创建一个广播接收器接收USB插拔信息：当插入USB插头插到一个USB端口，或从一个USB端口，移除装置的USB插头
     */
    class UsbReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                if(mUsbDriver.usbAttached(intent))
                {
                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if ((device.getProductId() == PID11 && device.getVendorId() == VENDORID)
                            || (device.getProductId() == PID13 && device.getVendorId() == VENDORID)
                            || (device.getProductId() == PID15 && device.getVendorId() == VENDORID))
                    {
                        if(mUsbDriver.openUsbDevice(device))
                        {
                            if(device.getProductId()==PID11){
                                mUsbDev1 = device;
                                mUsbDev = mUsbDev1;
                            } else {
                                mUsbDev2 = device;
                                mUsbDev = mUsbDev2;
                            }
                        }
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice) intent
                        .getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if ((device.getProductId() == PID11 && device.getVendorId() == VENDORID)
                        || (device.getProductId() == PID13 && device.getVendorId() == VENDORID)
                        || (device.getProductId() == PID15 && device.getVendorId() == VENDORID))
                {
                    mUsbDriver.closeUsbDevice(device);
                    if(device.getProductId()==PID11)
                        mUsbDev1 = null;
                    else
                        mUsbDev2 = null;
                }
            } else if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this)
                {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
                    {
                        if ((device.getProductId() == PID11 && device.getVendorId() == VENDORID)
                                || (device.getProductId() == PID13 && device.getVendorId() == VENDORID)
                                || (device.getProductId() == PID15 && device.getVendorId() == VENDORID))
                        {
                            if (mUsbDriver.openUsbDevice(device)) {
                                if (device.getProductId() == PID11) {
                                    mUsbDev1 = device;
                                    mUsbDev = mUsbDev1;
                                } else {
                                    mUsbDev2 = device;
                                    mUsbDev = mUsbDev2;
                                }
                            }
                        }
                    }
                    else {
                        //Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    }

    public boolean getPrinterConnectStatus(){
        boolean blnRtn = false;
        if (!mUsbDriver.isConnected()) {
            Collection<UsbDevice> values = mUsbManager.getDeviceList().values();
            for (UsbDevice device : values) {
                if((device.getProductId() == PID11 && device.getVendorId() == VENDORID)
                        || (device.getProductId() == PID13 && device.getVendorId() == VENDORID)
                        || (device.getProductId() == PID15 && device.getVendorId() == VENDORID)){
                    blnRtn = mUsbDriver.usbAttached(device);
                    if (blnRtn == false) {
                        break;
                    }
                    blnRtn = mUsbDriver.openUsbDevice(device);
                    if (blnRtn == false) {
                        break;
                    }
                    blnRtn = mUsbDriver.openUsbDevice(device);

                    // 打开设备
                    if (blnRtn) {
                        if (device.getProductId() == PID11) {
                            mUsbDev1 = device;
                            mUsbDev = mUsbDev1;
                        } else {
                            mUsbDev2 = device;
                            mUsbDev = mUsbDev2;
                        }
                        break;
                    }
                }
            }
        } else {
            blnRtn = true;
        }
        return blnRtn;
    }

    public int getPrinterStatus(){
        if(mUsbDev1 == null){
            return -1;
        }
        return getPrinterStatus(mUsbDev1);
    }

    // 检测打印机状态
    private int getPrinterStatus(UsbDevice usbDev) {
        int iRet = -1;

        byte[] bRead1 = new byte[1];
        byte[] bWrite1 = PrintCmd.GetStatus1();
        if(mUsbDriver.read(bRead1,bWrite1,usbDev)>0)
        {
            iRet = PrintCmd.CheckStatus1(bRead1[0]);
        }

        if(iRet!=0)
            return iRet;

        byte[] bRead2 = new byte[1];
        byte[] bWrite2 = PrintCmd.GetStatus2();
        if(mUsbDriver.read(bRead2,bWrite2,usbDev)>0)
        {
            iRet = PrintCmd.CheckStatus2(bRead2[0]);
        }

        if(iRet!=0)
            return iRet;

        byte[] bRead3 = new byte[1];
        byte[] bWrite3 = PrintCmd.GetStatus3();
        if(mUsbDriver.read(bRead3,bWrite3,usbDev)>0)
        {
            iRet = PrintCmd.CheckStatus3(bRead3[0]);
        }

        if(iRet!=0)
            return iRet;

        byte[] bRead4 = new byte[1];
        byte[] bWrite4 = PrintCmd.GetStatus4();
        if(mUsbDriver.read(bRead4,bWrite4,usbDev)>0)
        {
            iRet = PrintCmd.CheckStatus4(bRead4[0]);
        }
        return iRet;
    }


    private int align = 0;        // 默认为:1, 0 靠左、1  居中、2:靠右
    private int cutter = 0;       // 默认0，  0 全切、1 半切
    String iline = "4";

    /**
     * 1.1.文本打印
     */
    public void printText(String data) {
        if(!TextUtils.isEmpty(data)){
            mUsbDriver.write(PrintCmd.SetAlignment(align));
            mUsbDriver.write(PrintCmd.PrintString(data, 0));
            setFeedCut(cutter, Integer.valueOf(iline));
        }
    }

    // 指定---走纸换行数量、切纸类型
    private void setFeedCut(int iMode,int num) {
        mUsbDriver.write(PrintCmd.PrintFeedline(num));   // 走纸换行
        mUsbDriver.write(PrintCmd.PrintCutpaper(iMode)); // 切纸类型
        mUsbDriver.write(PrintCmd.SetClean());           // 清除缓存,初始化
    }
}
