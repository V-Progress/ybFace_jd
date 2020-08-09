package com.yunbiao.ybsmartcheckin_live_id.activity_certificates;

import android.graphics.Bitmap;

import com.csht.netty.entry.IdCard;
import com.yunbiao.ybsmartcheckin_live_id.utils.IdCardMsg;

public interface CertificatesViewInterface {

    /***
     * 模式发生改变
     * @param mode
     * @param temperEnabled
     */
    void onModeChanged(int mode, boolean temperEnabled);
    /***
     * 网络状态发生改变
     * @param isNet
     */
    void onNetStateChanged(boolean isNet);
    /***
     * 人脸引擎已初始化完成
     */
    void onFaceViewReady();
    /***
     * 更新人脸照片
     * @param bitmap
     */
    void updateFaceImage(Bitmap bitmap);
    /***
     * 更新热成像图像
     * @param hotImage
     * @param temper
     * @param mHasFace
     */
    void updateHotImage(Bitmap hotImage, float temper, boolean mHasFace);
    /***
     * 更新卡信息
     * @param idCardMsg
     * @param bitmap
     * @param temperEnabled
     */
    void updateIdCardInfo(IdCardMsg idCardMsg, Bitmap bitmap, boolean icCardMode, boolean temperEnabled);

    /**
     * 更新卡信息，网络读卡器
     * @param idCard
     */
    void updateIdCardInfoByNetReader(IdCard idCard);

    //更新人证测温结果
    void updateResultTip(String resultTip, IdCardMsg idCardMsg, IdCard mIdCard, float finalTemper, int similarInt, boolean isAlike, boolean isNormal, boolean isInWhite, boolean icCardMode, boolean temperEnabled);
    //更新提示
    void updateTips(String tip);
    //重置所有UI
    void resetAllUI();
    //更新实时测温结果
    void updateRealTimeTemper(float temperature, boolean isTempNormal);
    // 清除实时测温结果
    void clearRealTimeTemper();
    /***
     * 通过扫码获取用户信息
     * @param userInfo
     */
    void getUserInfoByCode(BaseCertificatesActivity.UserInfo userInfo);

    void updateCodeTemperResult(float finalTemper,boolean normal);
}
