package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in_smt;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;

import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;

class SMTViewInterAbsImp implements SMTViewInterface {
    @Override
    public void onFaceViewReady() {

    }

    @Override
    public void onModeChanged(int mode) {

    }

    @Override
    public void updateHotImage(Bitmap bitmap, float temper, boolean hasFace) {

    }

    @Override
    public void showTips(String tip, int stableTipsId) {

    }

    @Override
    public void dismissTips() {

    }

    @Override
    public void showResult(String tip, int id) {

    }

    @Override
    public void dismissResult() {

    }

    @Override
    public void clearAllUI() {

    }

    @Override
    public void updateSignList(Sign sign) {

    }

    @Override
    public void hasFace(boolean hasFace) {

    }

    @Override
    public View getDistanceView() {
        return null;
    }

    @Override
    public Bitmap getFacePicture() {
        return null;
    }

    @Override
    public Rect getRealRect(Rect faceRect) {
        return null;
    }

    @Override
    public boolean isTipsShown() {
        return false;
    }

    @Override
    public boolean isMaskTipsShown() {
        return false;
    }

    @Override
    public boolean isResultShown() {
        return false;
    }

    @Override
    public void showMaskTip(String tip) {

    }

    @Override
    public void clearMaskTip() {

    }
}
