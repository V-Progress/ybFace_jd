package com.yunbiao.ybsmartcheckin_live_id;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import timber.log.Timber;

public class CrashTree extends Timber.DebugTree {

    public boolean isLog() {
        return isLog;
    }

    private boolean isLog;
    public CrashTree(Context context) {
        super();
        isLog = context.getApplicationInfo() != null && (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE ) != 0;
        Timber.d("是否debug模式：" + isLog);
    }

    @Override
    protected boolean isLoggable(@Nullable String tag, int priority) {
        return true;
    }
}
