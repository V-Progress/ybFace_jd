package com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class MultiTemperBean {
    private String faceId;
    private Bitmap headImage;
    private Bitmap hotImage;
    private String name;
    private float temper;
    private long time;
    private Rect hotRect;
    private long entryId;
    private int compId;

    private String headPath;
    private String hotPath;

    private int trackId;

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public MultiTemperBean() {
    }

    public MultiTemperBean(String faceId, Bitmap headImage, Bitmap hotImage, String name, float temper, long time, Rect hotRect, long entryId, int compId, String headPath, String hotPath, int trackId) {
        this.faceId = faceId;
        this.headImage = headImage;
        this.hotImage = hotImage;
        this.name = name;
        this.temper = temper;
        this.time = time;
        this.hotRect = hotRect;
        this.entryId = entryId;
        this.compId = compId;
        this.headPath = headPath;
        this.hotPath = hotPath;
        this.trackId = trackId;
    }

    public String getHeadPath() {
        return headPath;
    }

    public void setHeadPath(String headPath) {
        this.headPath = headPath;
    }

    public String getHotPath() {
        return hotPath;
    }

    public void setHotPath(String hotPath) {
        this.hotPath = hotPath;
    }

    public int getCompId() {
        return compId;
    }

    public void setCompId(int compId) {
        this.compId = compId;
    }

    public long getEntryId() {
        return entryId;
    }

    public void setEntryId(long entryId) {
        this.entryId = entryId;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public Rect getHotRect() {
        return hotRect;
    }

    public void setHotRect(Rect hotRect) {
        this.hotRect = hotRect;
    }

    public Bitmap getHeadImage() {
        return headImage;
    }

    public void setHeadImage(Bitmap headImage) {
        this.headImage = headImage;
    }

    public Bitmap getHotImage() {
        return hotImage;
    }

    public void setHotImage(Bitmap hotImage) {
        this.hotImage = hotImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getTemper() {
        return temper;
    }

    public void setTemper(float temper) {
        this.temper = temper;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "MultiTemperBean{" +
                "faceId='" + faceId + '\'' +
                ", headImage=" + (headImage != null ? ("宽：" + headImage.getWidth() + "高：" + headImage.getHeight()) : "NULL") +
                ", hotImage=" + (hotImage != null ? ("宽：" + hotImage.getWidth() + "高：" + hotImage.getHeight()) : "NULL") +
                ", name='" + name + '\'' +
                ", temper=" + temper +
                ", time=" + time +
                ", hotRect=" + hotRect +
                '}';
    }

    public MultiTemperBean copy(){
        return new MultiTemperBean(faceId,headImage,hotImage,name,temper,time,hotRect,entryId,compId,headPath,hotPath,trackId);
    }
}
