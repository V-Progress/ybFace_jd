package com.yunbiao.ybsmartcheckin_live_id.printer;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.Iterator;

public class UsbDriver {
    public static final int BAUD9600 = 9600;
    public static final int BAUD19200 = 19200;
    public static final int BAUD38400 = 38400;
    public static final int BAUD57600 = 57600;
    public static final int BAUD115200 = 115200;
    private UsbManager a;
    private PendingIntent b;
    private int[] c = new int[4];
    private UsbDevice[] d = new UsbDevice[2];
    private UsbInterface[] e = new UsbInterface[2];
    private UsbDeviceConnection[] f = new UsbDeviceConnection[2];
    private int g = -1;
    private UsbEndpoint[] h = new UsbEndpoint[2];
    private UsbEndpoint[] i = new UsbEndpoint[2];
    public boolean PID2013 = false;
    public boolean PID2015 = false;
    public boolean PID2017 = false;

    public UsbDriver(UsbManager var1, Context var2) {
        this.a = var1;

        for(int var3 = 0; var3 < 4; ++var3) {
            this.c[var3] = 8;
        }

    }

    public void setPermissionIntent(PendingIntent var1) {
        this.b = var1;
    }

    public boolean usbAttached(Intent var1) {
        UsbDevice var2 = (UsbDevice)var1.getParcelableExtra("device");
        return this.usbAttached(var2);
    }

    public boolean usbAttached(UsbDevice var1) {
        this.g = a(var1);
        this.d[this.g] = var1;
        if (this.g < 0) {
            Log.i("UsbDriver", "Not support device : " + var1.toString());
            return false;
        } else if (this.a.hasPermission(this.d[this.g])) {
            return true;
        } else {
            this.a.requestPermission(this.d[this.g], this.b);
            return false;
        }
    }

    public boolean openUsbDevice() {
        if (this.g < 0) {
            Iterator var2 = this.a.getDeviceList().values().iterator();

            while(var2.hasNext()) {
                UsbDevice var1 = (UsbDevice)var2.next();
                Log.i("UsbDriver", "Devices : " + var1.toString());
                this.g = a(var1);
                if (this.g >= 0) {
                    this.d[this.g] = var1;
                    break;
                }
            }
        }

        return this.g < 0 ? false : this.openUsbDevice(this.d[this.g]);
    }

    public boolean openUsbDevice(UsbDevice var1) {
        this.g = a(var1);
        if (this.g < 0) {
            return false;
        } else {
            int var2 = this.d[this.g].getInterfaceCount();
            Log.i("UsbDriver", " m_Device[m_UsbDevIdx].getInterfaceCount():" + var2);
            if (var2 == 0) {
                return false;
            } else {
                if (var2 > 0) {
                    this.e[this.g] = this.d[this.g].getInterface(0);
                }

                if (this.e[this.g].getEndpoint(1) != null) {
                    this.i[this.g] = this.e[this.g].getEndpoint(1);
                }

                if (this.e[this.g].getEndpoint(0) != null) {
                    this.h[this.g] = this.e[this.g].getEndpoint(0);
                }

                this.f[this.g] = this.a.openDevice(this.d[this.g]);
                if (this.f[this.g] == null) {
                    return false;
                } else if (this.f[this.g].claimInterface(this.e[this.g], true)) {
                    return true;
                } else {
                    this.f[this.g].close();
                    return false;
                }
            }
        }
    }

    public void closeUsbDevice() {
        if (this.g >= 0) {
            this.closeUsbDevice(this.d[this.g]);
        }
    }

    public boolean closeUsbDevice(UsbDevice var1) {
        try {
            this.g = a(var1);
            if (this.g < 0) {
                return false;
            }

            if (this.f[this.g] != null && this.e[this.g] != null) {
                this.f[this.g].releaseInterface(this.e[this.g]);
                this.e[this.g] = null;
                this.f[this.g].close();
                this.f[this.g] = null;
                this.d[this.g] = null;
                this.h[this.g] = null;
                this.i[this.g] = null;
            }
        } catch (Exception var2) {
            Log.i("UsbDriver", "closeUsbDevice exception: " + var2.getMessage().toString());
        }

        return true;
    }

    public boolean usbDetached(Intent var1) {
        UsbDevice var2 = (UsbDevice)var1.getParcelableExtra("device");
        return this.closeUsbDevice(var2);
    }

    public int write(byte[] var1) {
        return this.write(var1, var1.length);
    }

    public int write(byte[] var1, int var2) {
        return this.g < 0 ? -1 : this.write(var1, var1.length, this.d[this.g]);
    }

    public int read(byte[] var1, byte[] var2) {
        return this.g < 0 ? -1 : this.read(var1, var2, this.d[this.g]);
    }

    public int write(byte[] var1, UsbDevice var2) {
        return this.write(var1, var1.length, var2);
    }

    public int write(byte[] var1, int var2, UsbDevice var3) {
        this.g = a(var3);
        if (this.g < 0) {
            return -1;
        } else {
            int var6 = 0;

            int var4;
            for(byte[] var5 = new byte[4096]; var6 < var2; var6 += var4) {
                var4 = 4096;
                if (var6 + 4096 > var2) {
                    var4 = var2 - var6;
                }

                System.arraycopy(var1, var6, var5, 0, var4);
                var4 = this.f[this.g].bulkTransfer(this.h[this.g], var5, var4, 3000);
                Log.i("UsbDriver", "-----Length--------" + String.valueOf(var4));
                if (var4 < 0) {
                    return -1;
                }
            }

            return var6;
        }
    }

    public int read(byte[] var1, byte[] var2, UsbDevice var3) {
        if (this.write(var2, var2.length, var3) < 0) {
            return -1;
        } else {
            int var4 = this.f[this.g].bulkTransfer(this.i[this.g], var1, var1.length, 100);
            Log.i("UsbDriver", "mFTDIEndpointOUT:" + var4);
            return var4;
        }
    }

    private static int a(UsbDevice var0) {
        if (var0 == null) {
            return -1;
        } else {
            try {
                if (var0.getProductId() == 8211 && var0.getVendorId() == 1305) {
                    return 0;
                }

                if (var0.getProductId() == 8213 && var0.getVendorId() == 1305 || var0.getProductId() == 8215 && var0.getVendorId() == 1305) {
                    return 1;
                }
            } catch (Exception var2) {
                Log.i("UsbDriver", "getUsbDevIndex exception: " + var2.getMessage().toString());
            }

            Log.i("UsbDriver", "Not support device : " + var0.toString());
            return -1;
        }
    }

    public boolean isUsbPermission() {
        boolean var1 = false;

        try {
            if (this.g < 0) {
                return false;
            }

            if (this.a != null) {
                var1 = this.a.hasPermission(this.d[this.g]);
            }
        } catch (Exception var2) {
        }

        return var1;
    }

    public boolean isConnected() {
        if (this.g < 0) {
            return false;
        } else {
            return this.d[this.g] != null && this.h[this.g] != null && this.i[this.g] != null;
        }
    }
}
