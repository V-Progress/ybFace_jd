package com.yunbiao.ybsmartcheckin_live_id.activity_certificates;

import android.graphics.Bitmap;

import com.yunbiao.ybsmartcheckin_live_id.utils.IdCardMsg;

public class CertificatesViewImpl implements CertificatesViewInterface {
    /***
     * 模式发生改变
     * @param mode
     */
    @Override
    public void onModeChanged(int mode) {

    }

    /***
     * 网络状态发生改变
     * @param isNet
     */
    @Override
    public void onNetStateChanged(boolean isNet) {

    }

    /***
     * 人脸引擎已初始化完成
     */
    @Override
    public void onFaceViewReady() {

    }

    /***
     * 更新人脸照片
     * @param bitmap
     */
    @Override
    public void updateFaceImage(Bitmap bitmap) {

    }

    /***
     * 更新热成像图像
     * @param hotImage
     * @param temper
     * @param mHasFace
     */
    @Override
    public void updateHotImage(Bitmap hotImage, float temper, boolean mHasFace) {

    }

    /***
     * 更新卡信息
     * @param idCardMsg
     * @param bitmap
     */
    @Override
    public void updateIdCardInfo(IdCardMsg idCardMsg, Bitmap bitmap) {

    }

    @Override
    public void updateResultTip(String resultTip, IdCardMsg idCardMsg, float finalTemper, int similarInt, boolean isAlike, boolean isNormal, boolean isInWhite) {

    }

    //更新提示
    @Override
    public void updateTips(String tip) {

    }

    //重置所有UI
    @Override
    public void resetAllUI() {

    }

    //更新实时测温结果
    @Override
    public void updateRealTimeTemper(float temperature, boolean isTempNormal) {

    }

    // 清除实时测温结果
    @Override
    public void clearRealTimeTemper() {

    }

    @Override
    public void getUserInfoByCode(BaseCertificatesActivity.UserInfo userInfo) {

    }

    @Override
    public void updateCodeTemperResult(float finalTemper, boolean normal) {

    }

}
