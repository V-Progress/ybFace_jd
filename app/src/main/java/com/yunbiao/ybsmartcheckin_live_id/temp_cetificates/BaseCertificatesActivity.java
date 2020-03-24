package com.yunbiao.ybsmartcheckin_live_id.temp_cetificates;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.callback.HotImageK3232CallBack;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseGpioActivity;
import com.yunbiao.ybsmartcheckin_live_id.utils.IDCardReader;
import com.yunbiao.ybsmartcheckin_live_id.utils.IdCardMsg;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseCertificatesActivity extends BaseGpioActivity {

    private boolean mThermalImgMirror;
    private Float mMinThreshold;
    private Float mWarningThreshold;
    private Float mCorrectValue;
    private boolean mLowTemp;
    private int mMode;

    @Override
    protected void initData() {
        super.initData();
        IDCardReader.getInstance().startReaderThread(this, readListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mThermalImgMirror = SpUtils.getBoolean(CertificatesConst.Key.THERMAL_MIRROR, CertificatesConst.Default.THERMAL_MIRROR);
        mMinThreshold = SpUtils.getFloat(CertificatesConst.Key.MIN_THRESHOLD, CertificatesConst.Default.MIN_THRESHOLD);
        mWarningThreshold = SpUtils.getFloat(CertificatesConst.Key.WARNING_THRESHOLD, CertificatesConst.Default.WARNING_THRESHOLD);
        mCorrectValue = SpUtils.getFloat(CertificatesConst.Key.CORRECT_VALUE, CertificatesConst.Default.CORRECT_VALUE);

        mLowTemp = SpUtils.getBoolean(CertificatesConst.Key.LOW_TEMP, CertificatesConst.Default.LOW_TEMP);
        mMode = SpUtils.getIntOrDef(CertificatesConst.Key.MODE, CertificatesConst.Default.MODE);

        String port;
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            port = "/dev/ttyS1";
        } else {
            port = "/dev/ttyS4";
        }
        int baudRate = 115200;
        if (mMode == CertificatesConst.Mode.CERTIFICATES_THERMAL) {
            TemperatureModule.getIns().initSerialPort(this, port, baudRate);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    TemperatureModule.getIns().startHotImageK3232(mThermalImgMirror, mLowTemp, hotImageK3232CallBack);
                }
            }, 1500);
        }

        TemperatureModule.getIns().setmCorrectionValue(mCorrectValue);
    }

    @Override
    protected void onStop() {
        super.onStop();
        TemperatureModule.getIns().closeHotImageK3232();
    }

    /*===关键数据====================================================================*/
    /*===关键数据====================================================================*/
    /*===关键数据====================================================================*/
    /*===关键数据====================================================================*/
    //温度收集
    private List<Float> mDetectTemperList = new ArrayList<>();
    private HotImageK3232CallBack hotImageK3232CallBack = new HotImageK3232CallBack() {
        @Override
        public void newestHotImageData(Bitmap bitmap, float sensorT, float afterT, float v2, float v3, boolean b, int i) {
            sendUpdateHotImageMessage(bitmap, sensorT);

            if (mHasFace) {
                if (mDetectTemperList.size() > 10) {
                    mDetectTemperList.remove(0);
                }
                mDetectTemperList.add(afterT);
            } else {
                if (mDetectTemperList.size() > 0) {
                    mDetectTemperList.clear();
                }
            }
        }
    };

    private IdCardMsg idCardMsg;
    private IDCardReader.ReadListener readListener = new IDCardReader.ReadListener() {
        @Override
        public void getCardInfo(IdCardMsg msg) {
            clearAllUIMesasge();
            idCardMsg = msg;
            sendCardInfoMeesage(msg);
        }
    };

    /*===消息交互区=============================================================================*/
    /*===消息交互区=============================================================================*/
    /*===消息交互区=============================================================================*/
    /*===消息交互区=============================================================================*/
    interface What {
        int UPDATE_HOT_IMAGE = 0;
        int SET_ID_CARD_INFO = 1;
        int SEND_TIP = 3;
        int CLEAR_UI_TIP = 4;
    }

    //热图更新
    private void sendUpdateHotImageMessage(Bitmap bitmap, float temper) {
        Message message = Message.obtain();
        message.obj = What.UPDATE_HOT_IMAGE;
        message.obj = bitmap;
        Bundle bundle = new Bundle();
        bundle.putFloat("temper", temper);
        message.setData(bundle);
        handler.sendMessage(message);
    }
    //设置卡信息
    private void sendCardInfoMeesage(IdCardMsg msg) {
        Message message = Message.obtain();
        message.what = What.SET_ID_CARD_INFO;
        message.obj = msg;
        handler.sendMessage(message);
    }
    //发送提示消息
    private void sendTipMessage(String tip){
        Message message = Message.obtain();
        message.obj = tip;
        message.what = What.SEND_TIP;
        handler.sendMessage(message);
    }
    //清除UI显示
    private void clearAllUIMesasge(){
        handler.removeMessages(What.CLEAR_UI_TIP);
        handler.sendEmptyMessage(What.CLEAR_UI_TIP);
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case What.UPDATE_HOT_IMAGE://更新热图
                    Bitmap bitmap = (Bitmap) msg.obj;
                    float temper = msg.getData().getFloat("temper");
                    updateHotImage(bitmap, temper);
                    break;
                case What.SET_ID_CARD_INFO://设置卡信息
                    IdCardMsg idCardMsg = (IdCardMsg) msg.obj;
                    Bitmap mIdCardBitmap = null;
                    if (idCardMsg != null) {
                        byte[] bytes = IDCardReader.getInstance().decodeToBitmap(idCardMsg.ptoto);
                        mIdCardBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    }
                    setIdCardInfo(idCardMsg, mIdCardBitmap);
                    break;
                case What.SEND_TIP://显示普通提示
                    String tip = (String) msg.obj;
                    showTips(tip);
                    break;


            }

            return true;
        }
    });

    /*==标签更新===================================================================*/
    /*==标签更新===================================================================*/
    /*==标签更新===================================================================*/
    /*==标签更新===================================================================*/
    //是否有人
    private boolean mHasFace = false;

    protected void updateHasFace(boolean hasFace) {
        mHasFace = hasFace;
    }


    /*==子类UI更新方法区====================================================================*/
    /*==子类UI更新方法区====================================================================*/
    /*==子类UI更新方法区====================================================================*/
    /*==子类UI更新方法区====================================================================*/
    protected abstract void updateHotImage(Bitmap bitmap, float temper);

    protected abstract void setIdCardInfo(IdCardMsg msg, Bitmap bitmap);

    protected abstract void showTips(String tip);
}
