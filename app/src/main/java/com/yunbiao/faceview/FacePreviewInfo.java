package com.yunbiao.faceview;

import com.arcsoft.face.FaceInfo;

public class FacePreviewInfo {
    private FaceInfo faceInfo;
    private int trackId;
    private float temper;
    private float oringinTemper;

    public float getOringinTemper() {
        return oringinTemper;
    }

    public void setOringinTemper(float oringinTemper) {
        this.oringinTemper = oringinTemper;
    }

    public float getTemper() {
        return temper;
    }

    public void setTemper(float temper) {
        this.temper = temper;
    }

    public FacePreviewInfo(FaceInfo faceInfo, int trackId) {
        this.faceInfo = faceInfo;
        this.trackId = trackId;
    }

    public FaceInfo getFaceInfo() {
        return faceInfo;
    }

    public void setFaceInfo(FaceInfo faceInfo) {
        this.faceInfo = faceInfo;
    }


    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

}
