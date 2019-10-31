package com.yunbiao.ybsmartcheckin_live_id.views.mixplayer;

import android.text.TextUtils;

public class FileType {

    public final static int TEXT = 0;
    public final static int IMAGE = 1;
    public final static int GIF = 2;
    public final static int VOICE = 3;
    public final static int VIDEO = 4;
    public final static int APP = 10;
    public final static int TEXT_FILE = 11;
    public final static int PDF = 101;

    public static int getFileType(String file) {
        String mime = getMIMEType(file);
        if (mime.indexOf("image") >= 0) {
            return IMAGE;
        } else if (mime.indexOf("audio") >= 0) {
            return VOICE;
        } else if (mime.indexOf("video") >= 0) {
            return VIDEO;
        } else if (mime.indexOf("application") >= 0) {
            return APP;
        } else if (mime.indexOf("text") >= 0) {
            return TEXT_FILE;
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

    public final static String[][] MIME_MapTable = {{".amr", "audio/amr"},
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"}, {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"}, {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"}, {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".exe", "application/octet-stream"},
            {".gtar", "application/x-gtar"}, {".gz", "application/x-gzip"},
            {".h", "text/plain"}, {".htm", "text/html"},
            {".html", "text/html"}, {".jar", "application/java-archive"},
            {".java", "text/plain"}, {".bmp", "image/bmp"},
            {".gif", "image/gif"}, {".png", "image/png"},
            {".jpeg", "image/jpeg"}, {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"}, {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"}, {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"}, {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"}, {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"}, {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"}, {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"}, {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"}, {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"}, {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".prop", "text/plain"},
            {".rar", "application/x-rar-compressed"},
            {".rc", "text/plain"}, {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"}, {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"}, {".txt", "text/plain"},
            {".wav", "audio/x-wav"}, {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"}, {".wmv", "video/wmv"},
            {".wps", "application/vnd.ms-works"}, {".xml", "text/xml"},
            {".xml", "text/plain"}, {".z", "application/x-compress"},
            {".zip", "application/zip"}, {"", "*/*"}};
}
