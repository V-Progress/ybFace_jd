package com.yunbiao.yb_smart_attendance;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PathManager {
    private static final String EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory().getPath();
    private static final String BASE_PATH = "yb_face";
    private static final String SKIN_PATH = "skins";

    private static final String CHILD_PHOTO_PATH = "photos";
    private static final String CHILD_RECORDS_PATH = "records";
    private static final String CHILD_RESOURCE_PATH = "resource";
    private static File photosDir;
    private static File recordsDir;
    private static File resourceDir;
    private static File skinsDir;

    public static String getSkinPath(){
        return skinsDir.getPath();
    }

    public static String getPhotoDir() {
        return photosDir.getPath();
    }

    public static String getRecordDir() {
        return recordsDir.getPath();
    }

    public static String getResourceDir() {
        return resourceDir.getPath();
    }

    private static final String TAG = "PathManager";
    public static void initPath(String uniqueId, PathInitCallback callback) {
        List<File> errFiles = new ArrayList<>();

        File baseDir = new File(EXTERNAL_STORAGE_PATH, BASE_PATH);
        if (!baseDir.exists()) {
            boolean mkdirs = baseDir.mkdirs();
            if(!mkdirs){
                errFiles.add(baseDir);
            }
        }

        skinsDir = new File(baseDir,SKIN_PATH);
        if (!skinsDir.exists()) {
            boolean mkdirs = skinsDir.mkdirs();
            if(!mkdirs){
                errFiles.add(skinsDir);
            }
        }

        File compDir = new File(baseDir, uniqueId);
        if (!compDir.exists()) {
            boolean mkdirs = compDir.mkdirs();
            if(!mkdirs){
                errFiles.add(compDir);
            }
        }


        photosDir = new File(compDir, CHILD_PHOTO_PATH);
        if (!photosDir.exists()) {
            boolean mkdirs = photosDir.mkdirs();
            if(!mkdirs){
                errFiles.add(compDir);
            }
        }

        recordsDir = new File(compDir, CHILD_RECORDS_PATH);
        if (!recordsDir.exists()) {
            boolean mkdirs = recordsDir.mkdirs();
            if(!mkdirs){
                errFiles.add(compDir);
            }
        }

        resourceDir = new File(compDir, CHILD_RESOURCE_PATH);
        if (!resourceDir.exists()) {
            boolean mkdirs = resourceDir.mkdirs();
            if(!mkdirs){
                errFiles.add(compDir);
            }
        }

        if(callback != null){
            callback.onPathInitialized(errFiles);
        }
    }

    public interface PathInitCallback {
        void onPathInitialized(List<File> errFiles);
    }
}
