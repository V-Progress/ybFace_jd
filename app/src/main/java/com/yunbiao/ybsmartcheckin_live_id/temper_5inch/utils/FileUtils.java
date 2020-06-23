package com.yunbiao.ybsmartcheckin_live_id.temper_5inch.utils;

import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

public class FileUtils {

    /**
     * 文件是否存在
     * @param file
     * @return
     */
    public static boolean isFileExist(File file) {
        if (file != null && file.exists()) {
            return true;
        }
        return false;
    }


    /**
     * 创建目录
     * @param path
     */
    public static void createDirs(File path) {
        if (path != null && !path.exists()) {
            path.mkdirs();
        }
    }



    public static boolean isVideo(String path) {
        if (getFileType(path) == FileType.VIDEO) {
            return true;
        }
        return false;
    }

    public static boolean isImage(String path) {
        if (getFileType(path) == FileType.IMAGE) {
            return true;
        }
        return false;
    }

    public static boolean isTxt(String path) {
        if (!TextUtils.isEmpty(path)) {
            int dotIndex = path.lastIndexOf(".");
            if (dotIndex < 0) {
                return false;
            }
            String end = path.substring(dotIndex, path.length()).toLowerCase();
            if (end == "")
                return false;
            if (end.equals(".txt"))
                return true;
        }
        return false;
    }

    public class FileType {

        public final static int TEXT = 0;

        public final static int IMAGE = 1;

        public final static int GIF = 2;

        public final static int VOICE = 3;

        public final static int VIDEO = 4;

        public final static int APP = 10;

        public final static int TEXT_FILE = 11;

        public final static int PDF = 101;
    }

    private static int getFileType(String file) {

        String mime = getMIMEType(file);
        if (mime.indexOf("image") >= 0) {
            return FileType.IMAGE;
        } else if (mime.indexOf("audio") >= 0) {
            return FileType.VOICE;
        } else if (mime.indexOf("video") >= 0) {
            return FileType.VIDEO;
        } else if (mime.indexOf("application") >= 0) {
            return FileType.APP;
        } else if (mime.indexOf("text") >= 0) {
            return FileType.TEXT_FILE;
        } else {
            return 0;
        }
    }


    public static String getMIMEType(String fName) {
        Log.i("getMIMEType", "getMIMEType:" + fName);
        String type = "*/*";
        if (!TextUtils.isEmpty(fName)) {
            int dotIndex = fName.lastIndexOf(".");
            if (dotIndex < 0) {
                return type;
            }
            String end = fName.substring(dotIndex, fName.length()).toLowerCase();
            if (end == "")
                return type;
            for (int i = 0; i < MIME_MapTable.length; i++) {
                if (end.equals(MIME_MapTable[i][0]))
                    type = MIME_MapTable[i][1];
            }
        }
        return type;
    }

    private static String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String SCREEN_BASE_PATH = sdPath + "/mnt/sdcard/photo/";

    /**
     * 随机生产文件名
     *
     * @return
     */
    private static String generateFileName() {
        return UUID.randomUUID().toString();
    }

    /**
     * 保存bitmap到本地
     *
     *
     * @param mBitmap
     * @return
     */
    public static String saveBitmap(Bitmap mBitmap) {
        String savePath;
        File filePic;

        try {
            filePic = new File(SCREEN_BASE_PATH + generateFileName() + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return filePic.getAbsolutePath();
    }

    public final static String[][] MIME_MapTable = { { ".amr", "audio/amr" },
            { ".3gp", "video/3gpp" },
            { ".apk", "application/vnd.android.package-archive" },
            { ".asf", "video/x-ms-asf" }, { ".avi", "video/x-msvideo" },
            { ".bin", "application/octet-stream" }, { ".c", "text/plain" },
            { ".class", "application/octet-stream" },
            { ".conf", "text/plain" }, { ".cpp", "text/plain" },
            { ".doc", "application/msword" },
            { ".exe", "application/octet-stream" },
            { ".gtar", "application/x-gtar" }, { ".gz", "application/x-gzip" },
            { ".h", "text/plain" }, { ".htm", "text/html" },
            { ".html", "text/html" }, { ".jar", "application/java-archive" },
            { ".java", "text/plain" }, { ".bmp", "image/bmp" },
            { ".gif", "image/gif" }, { ".png", "image/png" },
            { ".jpeg", "image/jpeg" }, { ".jpg", "image/jpeg" },
            { ".js", "application/x-javascript" }, { ".log", "text/plain" },
            { ".m3u", "audio/x-mpegurl" }, { ".m4a", "audio/mp4a-latm" },
            { ".m4b", "audio/mp4a-latm" }, { ".m4p", "audio/mp4a-latm" },
            { ".m4u", "video/vnd.mpegurl" }, { ".m4v", "video/x-m4v" },
            { ".mov", "video/quicktime" }, { ".mp2", "audio/x-mpeg" },
            { ".mp3", "audio/x-mpeg" }, { ".mp4", "video/mp4" },
            { ".mpc", "application/vnd.mpohun.certificate" },
            { ".mpe", "video/mpeg" }, { ".mpeg", "video/mpeg" },
            { ".mpg", "video/mpeg" }, { ".mpg4", "video/mp4" },
            { ".mpga", "audio/mpeg" },
            { ".msg", "application/vnd.ms-outlook" }, { ".ogg", "audio/ogg" },
            { ".pdf", "application/pdf" },
            { ".pps", "application/vnd.ms-powerpoint" },
            { ".ppt", "application/vnd.ms-powerpoint" },
            { ".prop", "text/plain" },
            { ".rar", "application/x-rar-compressed" },
            { ".rc", "text/plain" }, { ".rmvb", "audio/x-pn-realaudio" },
            { ".rtf", "application/rtf" }, { ".sh", "text/plain" },
            { ".tar", "application/x-tar" },
            { ".tgz", "application/x-compressed" }, { ".txt", "text/plain" },
            { ".wav", "audio/x-wav" }, { ".wma", "audio/x-ms-wma" },
            { ".wmv", "audio/x-ms-wmv" },{".wmv","video/wmv"},
            { ".wps", "application/vnd.ms-works" }, { ".xml", "text/xml" },
            { ".xml", "text/plain" }, { ".z", "application/x-compress" },
            { ".zip", "application/zip" }, { "", "*/*" } };


    public static void writeTxtFile(File file, String contentStr) {
        //每次写入时，都换行写
        String strContent = contentStr + "\n";
        try {
            if (!file.exists()) {
                return;
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File.\n" + e);
        }
    }

}
