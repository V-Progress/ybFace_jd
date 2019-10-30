package com.yunbiao.ybsmartcheckin_live_id;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;

import java.io.File;

import skin.support.load.SkinSDCardLoader;

public class CustomSDCardLoader extends SkinSDCardLoader {
    public static final int SKIN_LOADER_STRATEGY_SDCARD = Integer.MAX_VALUE;
    private static final String TAG = "CustomSDCardLoader";
    @Override
    protected String getSkinPath(Context context, String skinName) {
        File skinDir = new File(Constants.SKIN_PATH, skinName);
        if(!skinDir.exists()){
            skinDir.mkdirs();
        }
        Log.e(TAG, "getSkinPath: " + skinDir.getAbsolutePath());
        return skinDir.getAbsolutePath();
    }

    @Override
    public int getType() {
        return SKIN_LOADER_STRATEGY_SDCARD;
    }
}
