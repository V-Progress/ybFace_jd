package com.yunbiao.ybsmartcheckin_live_id.views.mixplayer;

public class PlayModel {
    private String path;
    private int type;

    public PlayModel(String path, int type) {
        this.path = path;
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "PlayModel{" +
                "path='" + path + '\'' +
                ", type=" + type +
                '}';
    }
}