package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in_smt;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;

import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;

public interface SMTViewInterface {
    /***
     * 设置区
     * ========================================================================
     */
    void onFaceViewReady();

    /**
     * UI刷新区
     * ========================================================================
     * */
    /***
     * 模式发生改变
     * @param mode
     */
    void onModeChanged(int mode);

    /***
     * 更新热成像信息
     * @param bitmap
     * @param temper
     * @param hasFace
     */
    void updateHotImage(Bitmap bitmap, float temper, boolean hasFace);

    /***
     * 进行距离提示
     * @param tip
     * @param stableTipsId
     */
    void showTips(String tip, int stableTipsId);

    /***
     * 隐藏距离提示
     */
    void dismissTips();

    /***
     * 显示温度提示
     * @param tip
     * @param id
     */
    void showResult(String tip, int id);

    /***
     * 隐藏温度提示
     */
    void dismissResult();

    /***
     * 隐藏所有UI
     */
    void clearAllUI();

    /**
     * 更新签到列表
     */
    void updateSignList(Sign sign);

    /**
     * 内容获取
     * ========================================================================
     * */
    /***
     * 是否有人
     * @param hasFace
     */
    void hasFace(boolean hasFace);

    /***
     * 获取距离限制区
     * @return
     */
    View getDistanceView();

    /***
     * 获取头像图片
     */
    Bitmap getFacePicture();

    /***
     * 获取真实坐标
     * @param faceRect
     * @return
     */
    Rect getRealRect(Rect faceRect);

    /***
     * 提示是否正在显示
     */
    boolean isTipsShown();

    boolean isMaskTipsShown();

    /***
     * 结果是否正在显示
     */
    boolean isResultShown();

    void showMaskTip(String tip);

    void clearMaskTip();
}
