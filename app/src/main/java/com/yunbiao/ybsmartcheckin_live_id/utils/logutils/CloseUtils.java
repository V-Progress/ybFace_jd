package com.yunbiao.ybsmartcheckin_live_id.utils.logutils;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;

class CloseUtils {
    private static final String TAG = "CloseUtils";
    
    public static void close(InputStream is) {
        if (is != null) {
            try {
                is.close();
            }
            catch (Exception e) {
                Log.e(TAG, "close exception: " + e.toString());
            }
        }
    }

    public static void close(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            }
            catch (Exception e) {
                Log.e(TAG, "close exception: " + e.toString());
            }
        }
    }

    public static void close(RandomAccessFile randomAccessFile) {
        if (randomAccessFile != null) {
            try {
                randomAccessFile.close();
            }
            catch (Exception e) {
                Log.e(TAG, "close exception: " + e.toString());
            }
        }
    }
    
    public static void close(Writer writer) {
        if (writer != null) {
            try {
                writer.close();
            }
            catch (Exception e) {
                Log.e(TAG, "close exception: " + e.toString());
            }
        }
    }

    public static void close(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            }
            catch (Exception e) {
                Log.e(TAG, "close exception: " + e.toString());
            }
        }
    }
}
