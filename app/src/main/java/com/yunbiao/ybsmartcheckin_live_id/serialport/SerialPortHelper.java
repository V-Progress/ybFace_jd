package com.yunbiao.ybsmartcheckin_live_id.serialport;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;

/**
 * Created by chen on 2019/4/3.
 */

public class SerialPortHelper {

    public static final int BAUDRATE_DEFAULT = 9600;

    public static final int PARITY_NONE = 78;
    public static final int PARITY_EVEN = 101;


    public static final String KEY_GATE = "PLC_GATE";

    public static final String ACTION_GATE_RECEIVE = "com.hsd.smart.gate";

    public static SerialPort newSerialPort (Context ct, String portPath, int baudrate) throws IOException, SecurityException, InvalidParameterException {
        if (portPath.length() == 0) {
            portPath = "/dev/ttyS0";
        }
        if (baudrate == -1) {
            baudrate = BAUDRATE_DEFAULT;
        }
        return new SerialPort(new File(portPath), baudrate, 0);
    }

}
