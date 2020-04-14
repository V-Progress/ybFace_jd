package com.yunbiao.ybsmartcheckin_live_id.activity_certificates;

import android.graphics.Bitmap;

import com.yunbiao.ybsmartcheckin_live_id.utils.IdCardMsg;

public interface CertificatesViewInterface {

    /*状态改变区*/
    void onModeChanged(int mode);
    void onNetStateChanged(boolean isNet);
    //人脸引擎初始化成功
    void onFaceViewReady();
    /*数据刷新*/
    void updateFaceImage(Bitmap bitmap);
    void updateHotImage(Bitmap hotImage, float temper);
    void updateIdCardInfo(IdCardMsg idCardMsg,Bitmap bitmap);
    void updateResultTip(String resultTip, IdCardMsg idCardMsg, float finalTemper, int similarInt, boolean isAlike, boolean isNormal);
    void updateTips(String tip);

    void resetAllUI();

    void updateRealTimeTemper(float temperature, boolean isTempNormal);
    void clearRealTimeTemper();
}
