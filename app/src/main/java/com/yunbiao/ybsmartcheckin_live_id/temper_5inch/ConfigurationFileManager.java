package com.yunbiao.ybsmartcheckin_live_id.temper_5inch;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.utils.FileUtils;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.utils.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationFileManager {

    public static List<File> getAd() {
        File adFile = new File(ResourceUtils.AD_PATH);
        if (!FileUtils.isFileExist(adFile)) {
            FileUtils.createDirs(adFile);
        }
        File[] files = adFile.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        List<File> mAdList = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            if (FileUtils.isVideo(files[i].toString()) || FileUtils.isImage(files[i].toString())) {
                mAdList.add(files[i]);
            }
        }
        return mAdList;
    }

    public static String getSubtitle() {
        File subtitleFile = new File(ResourceUtils.SUBTITLE_PATH);
        String contentStr = ResourceUtils.getStringResource(R.string.examples_subtitle_content);
        try {
            if (!FileUtils.isFileExist(subtitleFile.getParentFile())) {
                FileUtils.createDirs(subtitleFile.getParentFile());
            }
            if (!FileUtils.isFileExist(subtitleFile)) {
                subtitleFile.createNewFile();
            } else {
                FileInputStream fileInputStream = new FileInputStream(subtitleFile);
                int length = fileInputStream.available();
                if (length > 0) {
                    byte[] buffer = new byte[length];
                    fileInputStream.read(buffer);
                    contentStr = new String(buffer);
                }
                fileInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentStr;
    }

    public static File getLogoFile() {
        File logoFile = new File(ResourceUtils.LOGO_PATH);
        if (!FileUtils.isFileExist(logoFile.getParentFile())) {
            FileUtils.createDirs(logoFile.getParentFile());
        }
        if (FileUtils.isFileExist(logoFile)) {
            return logoFile;
        }
        return null;
    }

}
