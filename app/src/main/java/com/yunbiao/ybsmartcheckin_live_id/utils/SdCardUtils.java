package com.yunbiao.ybsmartcheckin_live_id.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.format.Formatter;


import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;

import java.io.File;
import java.math.BigDecimal;

/**
 * Created by LiuShao on 2016/4/6.
 */
public class SdCardUtils {
    private static final String TAG = "SdCardUtils";
    public static String getUsbDiskPath(Context context){
        String pathStr = "";
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            String[] paths = (String[]) StorageManager.class.getMethod("getVolumePaths", new Class[0]).invoke(storageManager, new Object[0]);
            for (String path : paths) {
                String state = (String) StorageManager.class.getMethod("getVolumeState", String.class).invoke(storageManager, path);
                if (state.equals(Environment.MEDIA_MOUNTED) && !path.contains("emulated")) {
                    pathStr = path;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pathStr;
    }
    /**
     * 判断sdcard是否可用
     * @return true为可用，否则为不可用
     */
    public static boolean sdCardIsAvailable() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);
    }

    public static String getAvailaleSize(String path) {
        StatFs stat = new StatFs(path);    /*获取block的SIZE*/
        long blockSize = stat.getBlockSize();    /*空闲的Block的数量*/
        long availableBlocks = stat.getAvailableBlocks();   /* 返回bit大小值*/
        long totalBlocks = stat.getBlockCount();
        long uedBlocks = totalBlocks - availableBlocks;
        return "已用:"+ Formatter.formatFileSize(APP.getContext(), uedBlocks * blockSize) + "/可用:"+ Formatter.formatFileSize(APP.getContext(),totalBlocks * blockSize);
    }


    public static String getMemoryTotalSize(){
        File innerCardFile = Environment.getExternalStorageDirectory();
        long totalSize = innerCardFile.getTotalSpace();
        return String.valueOf(totalSize/1024/1024);
    }

    public static String getMemoryUsedSize(){
        File innerCardFile = Environment.getExternalStorageDirectory();
        long totalSize = innerCardFile.getTotalSpace();
        long freeSize = innerCardFile.getFreeSpace();
        long usedSize = totalSize - freeSize;
        return String.valueOf(usedSize / 1024 / 1024);
    }


    public static final String[] extsdsNoUsb = new String[]{"extsd","external_sd"};

    public static String getNoUsbSdcardPath() {
        for (String string : extsdsNoUsb) {
            File file = new File("mnt/" + string);
            if (file.exists()) {
                if (file.canExecute() && file.canWrite() && file.canRead()) {
                    return file.getPath();
                }
            }
        }
        return "";
    }
    /**
     * 获取文件夹大小(递归)
     *
     * @param file File实例
     * @return long
     */
    public static long getFolderSize(java.io.File file) {

        long size = 0;
        try {
            java.io.File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);

                } else {
                    size = size + fileList[i].length();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    public static class Capacity{
        private double used_kb;
        private double all_kb;
        private double used_mb;
        private double all_mb;
        private double used_gb;
        private double all_gb;

        public Capacity(double used, double all) {
            this.used_kb = used;
            this.used_mb = used_kb == 0 ? 0 : (used_kb / 1024);
            this.used_gb = used_kb == 0 ? 0 : (used_kb / 1024 / 1024);

            this.all_kb = all;
            this.all_mb = all_kb == 0 ? 0 : (all_kb / 1024);
            this.all_gb = all_kb == 0 ? 0 : (all_kb / 1024 /1024);
        }

        public double getUsed_mb() {
            return used_mb;
        }

        public double getAll_mb() {
            return all_mb;
        }

        public double getUsed_gb() {
            return used_gb;
        }

        public double getAll_gb() {
            return all_gb;
        }

        public double getUsed_kb() {
            return used_kb;
        }

        public double getAll_kb() {
            return all_kb;
        }

        @Override
        public String toString() {
            return "Capacity{" +
                    "used_kb=" + formatD(used_kb) +
                    ", all_kb=" + formatD(all_kb) +
                    ", used_mb=" + formatD(used_mb) +
                    ", all_mb=" + formatD(all_mb) +
                    ", used_gb=" + formatD(used_gb) +
                    ", all_gb=" + formatD(all_gb) +
                    '}';
        }

        public double formatD(double fValue) {
            return (double) (Math.round(fValue * 100)) / 100;
        }
    }

    public static Capacity getUsedCapacity(){
        Capacity capacity = null;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            double blockSize = sf.getBlockSize();
            double blockCount = sf.getBlockCount();
            double availCount = sf.getAvailableBlocks();
            Double level = (availCount * blockSize / 1024);
            Double all = (blockSize * blockCount / 1024);
            Double use = all - level;
            capacity = new Capacity(use,all);
        }
        if(capacity == null){
            capacity = new Capacity(0,0);
        }
        return capacity;
    }


    public static String getSDDiskCon() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            double blockSize = sf.getBlockSize();
            double blockCount = sf.getBlockCount();
            double availCount = sf.getAvailableBlocks();

            Double level = (availCount * blockSize / 1024);
            Double all = (blockSize * blockCount / 1024);

            Double use = all - level;

            Double useDou = use / 1024 / 1024;
//            Double levelDou = level / 1024 / 1024;
            Double allDou = all / 1024 / 1024;

            BigDecimal useB = new BigDecimal(useDou);
            double useF = useB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

//            BigDecimal levelB = new BigDecimal(levelDou);
//            double levelF = levelB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

            BigDecimal allB = new BigDecimal(allDou);
            double allF = allB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

            String useStr = useF + "G";
            if (useF < 1) {
                useF = useF * 1024;
                useStr = useF + "M";
            }
//            String levelStr = levelF + "G";
//            if (levelF < 1) {
//                levelF = levelF * 1024;
//                levelStr = levelF + "M";
//            }
            String allStr = allF + "G";
            if (allF < 1) {
                allF = allF * 1024;
                allStr = allF + "M";
            }
            return "已用:" + (useStr) + "/可用:" + allStr;
        } else {
            return "";
        }
    }

    public static String getEnSDDiskCon(Context context) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            double blockSize = sf.getBlockSize();
            double blockCount = sf.getBlockCount();
            double availCount = sf.getAvailableBlocks();

            Double level = (availCount * blockSize / 1024);
            Double all = (blockSize * blockCount / 1024);

            Double use = all - level;

            Double useDou = use / 1024 / 1024;
//            Double levelDou = level / 1024 / 1024;
            Double allDou = all / 1024 / 1024;

            BigDecimal useB = new BigDecimal(useDou);
            double useF = useB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

//            BigDecimal levelB = new BigDecimal(levelDou);
//            double levelF = levelB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

            BigDecimal allB = new BigDecimal(allDou);
            double allF = allB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

            String useStr = useF + "G";
            if (useF < 1) {
                useF = useF * 1024;
                useStr = useF + "M";
            }
//            String levelStr = levelF + "G";
//            if (levelF < 1) {
//                levelF = levelF * 1024;
//                levelStr = levelF + "M";
//            }
            String allStr = allF + "G";
            if (allF < 1) {
                allF = allF * 1024;
                allStr = allF + "M";
            }
            return context.getResources().getString(R.string.used) + (useStr) + context.getResources().getString(R.string
                    .no_used) + allStr;
        } else {
            return "";
        }
    }

    public static String getMoneryDiskCon() {
        File root = Environment.getRootDirectory();
        StatFs sf = new StatFs(root.getPath());
        long blockSize = sf.getBlockSize();
        long blockCount = sf.getBlockCount();
        long availCount = sf.getAvailableBlocks();

        long level = (availCount * blockSize / 1024);
        long all = (blockSize * blockCount / 1024);
        long use = all - level;
        Double useDou = Double.valueOf(use / 1024 / 1024 / 1024);
        Double allDou = Double.valueOf(all / 1024 / 1024 / 1024);

        BigDecimal useB = new BigDecimal(useDou);
        double useF = useB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        BigDecimal allB = new BigDecimal(allDou);
        double allF = allB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return all + "G" + "/" + (level) + "G";
    }

    /**
     * 获取外部存储，不包括sdcard内部存储
     */
    public static final String[] outers = new String[]{"extsd", "external_sd", "usb_storage/USB_DISK1/udisk0",
            "usb_storage/USB_DISK0/udisk0",
            "usb_storage/USB_DISK2", "usb_storage/USB_DISK2/udisk0", "usbhost", "usbhost0", "usbhost1", "usbhost2", "usbhost3",
            "sda1", "usb_storage",
            "media_rw/usbotg-sda", "media_rw/usbotg-sda0", "media_rw/usbotg-sda1", "media_rw/usbotg-sda2", "mmcblk1p1"};

    public static String getOuterPath() {
        for (String string : outers) {
            File file = new File("mnt/" + string);
            if (file.exists()) {
                if (file.canExecute() && file.canWrite() && file.canRead()) {
                    return file.getPath();
                }
            }
        }
        return "";
    }

    /**
     * 判断SDCard是否可用
     */
    private static boolean isSDCardEnable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡路径
     */
    private static String getSDCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    }

    /**
     * 获取SD卡的剩余容量 单位byte
     *
     * @return
     */
    public static long getSDCardAllSize() {
        if (isSDCardEnable()) {
            StatFs stat = new StatFs(getSDCardPath());
            // 获取空闲的数据块的数量
            long availableBlocks = (long) stat.getAvailableBlocks() - 4;
            // 获取单个数据块的大小（byte）
            long freeBlocks = stat.getAvailableBlocks();
            return freeBlocks * availableBlocks;
        }
        return 0;
    }


    /**
     * 获取SDpath路径
     *
     * @return String 返回SD卡路径
     */
    public static String getSDPath() {
        File sdDir = null;
        String model = Environment.MEDIA_MOUNTED;
        boolean sdCardExist = Environment.getExternalStorageState().equals(model); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            return sdDir.toString();
        } else {
            return "";
        }
    }

    public static final String[] extsds = new String[]{"extsd", "external_sd", "usb_storage/USB_DISK1/udisk0",
            "usb_storage/USB_DISK0/udisk0",
            "usb_storage/USB_DISK2/udisk0", "usb_storage/USB_DISK2", "usbhost", "usbhost0", "usbhost1", "usbhost2", "usbhost3",
            "sda1", "usb_storage",
            "media_rw/usbotg-sda", "media_rw/usbotg-sda0", "media_rw/usbotg-sda1", "media_rw/usbotg-sda2", "mmcblk1p1", "sdcard"};

    public static String getSdcardPath() {
        for (String string : extsds) {
            File file = new File("mnt/" + string);
            if (file.exists()) {
                if (file.canExecute() && file.canWrite() && file.canRead()) {
                    return file.getPath();
                }
            }
        }
        return "";
    }

}
