package com.yunbiao.ybsmartcheckin_live_id.utils;

import android.text.TextUtils;
import android.util.Log;

import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;

import java.io.File;

/**
 * Created by LiuShao on 2016/4/6.
 */
public class FileUtils {
    private static final String TAG = "FileUtils";

    private static long size = 0;

    public interface OnSizeCallback{
        void getSize(long size);
    }

    public interface OnClearCallback{
        void clearFinish(long size);
    }

    public static void clearData(OnClearCallback onClearCallback){
        size = 0;
        File file = new File(Constants.LOCAL_ROOT_PATH);
        if(!file.exists() || (!file.isDirectory())){
            if(onClearCallback != null){
                onClearCallback.clearFinish(size);
            }
            return;
        }

        File[] files = file.listFiles();
        if(files == null || files.length<=0){
            if(onClearCallback != null){
                onClearCallback.clearFinish(size);
            }
            return;
        }

        clearFile(files);
        if(onClearCallback != null){
            onClearCallback.clearFinish(size);
        }
    }

    public static boolean clearData(){
        File file = new File(Constants.LOCAL_ROOT_PATH);
        if(file!=null ){
            Log.e(TAG, "clearData: 11111111111111");
            if(file.exists()){
                boolean delete = file.delete();
                System.gc();
                Log.e(TAG, "clearData: 22222222222222222"+delete);
                return delete;
            } else {
                Log.e(TAG, "clearData: 3333333333333");
                return true;
            }
        }
        return false;
    }

    public static void getDataSize(OnSizeCallback onSizeCallback){
        size = 0;
        File file = new File(Constants.LOCAL_ROOT_PATH);
        if(!file.exists() || (!file.isDirectory())){
            if(onSizeCallback != null){
                onSizeCallback.getSize(size);
            }
            return;
        }

        File[] files = file.listFiles();
        if(files == null || files.length<=0){
            if(onSizeCallback != null){
                onSizeCallback.getSize(size);
            }
            return;
        }

        getSize(files);

        if(onSizeCallback != null){
            onSizeCallback.getSize(size);
        }
    }

    private static void clearFile(File[] fileArr){
        if (null == fileArr || fileArr.length <= 0){//部分文件夹 无权限访问，返回null
            return;
        }
        for (File file : fileArr) {
            if (file.isFile()) {
                long length = file.length();
                boolean delete = file.delete();
                if(delete){
                    size += length;
                }
            }
            if (file.isDirectory()) {
                getSize(file.listFiles());
            }
        }
    }

    private static void getSize(File[] fileArr) {
        if (null == fileArr || fileArr.length <= 0){//部分文件夹 无权限访问，返回null
            return;
        }
        for (File file : fileArr) {
            if (file.isFile()) {
                size += file.length();
            }
            if (file.isDirectory()) {
                getSize(file.listFiles());
            }
        }
    }

    /**
     * 文件是否存在
     * @param file
     * @return
     */
    public static boolean isFileExist(File file) {
        return file != null && file.exists();
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

        return getFileType(path) == FileType.VIDEO;
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
}
