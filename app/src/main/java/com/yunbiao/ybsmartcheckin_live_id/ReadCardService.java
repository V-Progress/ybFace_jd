package com.yunbiao.ybsmartcheckin_live_id;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class ReadCardService  extends AccessibilityService {
    private static final String TAG = ReadCardService.class.getSimpleName();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.e(TAG, "onAccessibilityEvent --> " + event);
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "onInterrupt");
    }

    /**
     * 复写这个方法可以捕获按键事件
     *
     * @param event
     * @return
     */
    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        Log.e(TAG, "keyEvent:" + event + "keyCode: " + keyCode + "char: " + KeyEvent.keyCodeToString(keyCode));

        return super.onKeyEvent(event);
    }
}
