package com.yunbiao.ybsmartcheckin_live_id;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;

public class ReadCardUtils {

    private static final String TAG = ReadCardUtils.class.getSimpleName();

    // 若500ms之内无字符输入，则表示扫码完成. (若觉得时间还长，则可以设置成更小的值)
    private final static long MESSAGE_DELAY = 500;

    private boolean mCaps;//大写或小写
    private StringBuilder mResult = new StringBuilder();//扫码内容

    private OnReadSuccessListener mOnReadSuccessListener;
    private Handler mHandler = new Handler();

    private final Runnable mReadingEndRunnable = new Runnable() {
        @Override
        public void run() {
            performScanSuccess();
        }
    };

    //调用回调方法
    private void performScanSuccess() {
        String barcode = mResult.toString();
        //Log.i(TAG, "performScanSuccess -> barcode: "+barcode);
        if (mOnReadSuccessListener != null) {
            mOnReadSuccessListener.onScanSuccess(barcode);
        }
        mResult.setLength(0);
    }

    //key事件处理
    public void resolveKeyEvent(KeyEvent event) {

        int keyCode = event.getKeyCode();

        checkLetterStatus(event);//字母大小写判断

        Log.w(TAG, "keyEvent:" + event + "keyCode: " + keyCode + "char: " + KeyEvent.keyCodeToString(keyCode));
        if (event.getAction() == KeyEvent.ACTION_DOWN) {


            char aChar = getInputCode(event);
            Log.w(TAG, "aChar: " + aChar);

            if (aChar != 0) {
                mResult.append(aChar);
            }

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                //若为回车键，直接返回
                mHandler.removeCallbacks(mReadingEndRunnable);
                mHandler.post(mReadingEndRunnable);
            } else {
                //延迟post，若500ms内，有其他事件
                mHandler.removeCallbacks(mReadingEndRunnable);
                mHandler.postDelayed(mReadingEndRunnable, MESSAGE_DELAY);
            }

        }
    }

    //检查shift键
    private void checkLetterStatus(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT || keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                //按着shift键，表示大写
                mCaps = true;
            } else {
                //松开shift键，表示小写
                mCaps = false;
            }
        }
    }

    //获取扫描内容
    private char getInputCode(KeyEvent event) {
        int keyCode = event.getKeyCode();

        char aChar;
        if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
            //字母
            aChar = (char) ((mCaps ? 'A' : 'a') + keyCode - KeyEvent.KEYCODE_A);
        } else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            //数字
            aChar = (char) ('0' + keyCode - KeyEvent.KEYCODE_0);
        } else {
            //其他符号
            switch (keyCode) {
                case KeyEvent.KEYCODE_PERIOD:
                    aChar = '.';
                    break;
                case KeyEvent.KEYCODE_MINUS:
                    aChar = mCaps ? '_' : '-';
                    break;
                case KeyEvent.KEYCODE_SLASH:
                    aChar = '/';
                    break;
                case KeyEvent.KEYCODE_BACKSLASH:
                    aChar = mCaps ? '|' : '\\';
                    break;
                default:
                    aChar = 0;
                    break;
            }
        }
        return aChar;
    }

    /**
     * 检测输入设备是否是读卡器
     *
     * @param context
     * @return 是的话返回true，否则返回false
     */
    public static boolean isInputFromReader(Context context, KeyEvent event) {
        if (event.getDevice() == null) {
            return false;
        }
//        event.getDevice().getControllerNumber();
//        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
//            //实体按键，若按键为返回、音量加减、返回false
//            return false;
//        }
        if (event.getDevice().getSources() == (InputDevice.SOURCE_KEYBOARD | InputDevice.SOURCE_DPAD | InputDevice.SOURCE_CLASS_BUTTON)) {
            //虚拟按键返回false
            return false;
        }
        Configuration cfg = context.getResources().getConfiguration();
        return cfg.keyboard != Configuration.KEYBOARD_UNDEFINED;
    }


    public interface OnReadSuccessListener {
        void onScanSuccess(String barcode);
    }

    public void setReadSuccessListener(OnReadSuccessListener onReadSuccessListener) {
        mOnReadSuccessListener = onReadSuccessListener;
    }

    public void removeScanSuccessListener() {
        mHandler.removeCallbacks(mReadingEndRunnable);
        mOnReadSuccessListener = null;
    }

}