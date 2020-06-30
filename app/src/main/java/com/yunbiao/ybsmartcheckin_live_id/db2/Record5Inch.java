package com.yunbiao.ybsmartcheckin_live_id.db2;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Record5Inch {

    @Id(autoincrement = true)
    private Long id = null;

    private long time;

    private boolean isUpload;

    private String date;

    private int comid;

    private float temperature;

    private String imgPath;

    @Generated(hash = 2040791891)
    public Record5Inch(Long id, long time, boolean isUpload, String date, int comid,
            float temperature, String imgPath) {
        this.id = id;
        this.time = time;
        this.isUpload = isUpload;
        this.date = date;
        this.comid = comid;
        this.temperature = temperature;
        this.imgPath = imgPath;
    }

    @Generated(hash = 392846291)
    public Record5Inch() {
    }

    public Long getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public String getDate() {
        return date;
    }

    public int getComid() {
        return comid;
    }

    public float getTemperature() {
        return temperature;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setUpload(boolean upload) {
        isUpload = upload;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setComid(int comid) {
        this.comid = comid;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public boolean getIsUpload() {
        return this.isUpload;
    }

    public void setIsUpload(boolean isUpload) {
        this.isUpload = isUpload;
    }
}
