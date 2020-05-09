package com.yunbiao.ybsmartcheckin_live_id.printer;

import android.content.Context;

public class PrinterUtils {

    private static PrinterUtils printerUtils;
    private Context mContext;
    private UsbPrinter usbPrinter;

    public static synchronized PrinterUtils getInstance(){
        if(printerUtils == null){
            printerUtils = new PrinterUtils();
        }
        return printerUtils;
    }

    public boolean openDevice(Context context){
        if(context == null)
            throw new RuntimeException("context can not be null!");
        mContext = context;
        usbPrinter = new UsbPrinter(mContext);
        return usbPrinter.OpenDevice();
    }

    public int getPrinterStatus(){
        return usbPrinter.getPrinterStatus();
    }

    public void printText(String data) {
        usbPrinter.printText(data);
    }

    public void closeDevice(){
        if(usbPrinter != null){
            usbPrinter.CloseDevice();
        }
    }
}
