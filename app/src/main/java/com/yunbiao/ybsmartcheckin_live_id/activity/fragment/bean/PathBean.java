package com.yunbiao.ybsmartcheckin_live_id.activity.fragment.bean;


import java.io.Serializable;

public class PathBean implements Serializable {
    public static final int TYPE_IMG = 0;
    public static final int TYPE_VIDEO = 1;

    int type;
    String url;
    String localPath;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public PathBean(String url, String localPath, int type) {
        this.url = url;
        this.localPath = localPath;
        this.type = type;
    }

    @Override
    public String toString() {
        return "PathBean{" +
                "type=" + type +
                ", url='" + url + '\'' +
                ", localPath='" + localPath + '\'' +
                '}';
    }
}
