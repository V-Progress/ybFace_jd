package com.yunbiao.vertical_scroll_player.model;

public class PlayElement {
    private long id;

    private String resourcePath;
    private int resourceType;

    private long goodNum;
    private long watchNum;

    public PlayElement(String resourcePath, int resourceType) {
        this.resourcePath = resourcePath;
        this.resourceType = resourceType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public int getResourceType() {
        return resourceType;
    }

    public void setResourceType(int resourceType) {
        this.resourceType = resourceType;
    }

    public long getGoodNum() {
        return goodNum;
    }

    public void setGoodNum(long goodNum) {
        this.goodNum = goodNum;
    }

    public long getWatchNum() {
        return watchNum;
    }

    public void setWatchNum(long watchNum) {
        this.watchNum = watchNum;
    }

    @Override
    public String toString() {
        return "PlayElement{" +
                "id=" + id +
                ", resourcePath='" + resourcePath + '\'' +
                ", resourceType=" + resourceType +
                ", goodNum=" + goodNum +
                ", watchNum=" + watchNum +
                '}';
    }
}
