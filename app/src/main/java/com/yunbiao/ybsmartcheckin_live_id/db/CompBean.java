package com.yunbiao.ybsmartcheckin_live_id.db;

import android.graphics.Bitmap;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = "CompBean")
public class CompBean implements Serializable {
    public CompBean() {
    }

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = "comid",unique = true)
    private int comid;//名字

    @DatabaseField(columnName = "compName")
    private String compName;//名字

    @DatabaseField(columnName = "abbName")
    private String abbName;//名字

    @DatabaseField(columnName = "topTitle")
    private String topTitle;//名字

    @DatabaseField(columnName = "bottomTitle")
    private String bottomTitle;//名字

    @DatabaseField(columnName = "slogan")
    private String slogan;//名字

    @DatabaseField(columnName = "notice")
    private String notice;//名字

    @DatabaseField(columnName = "devicePwd")
    private String devicePwd;//名字

    @DatabaseField(columnName = "downtime")
    private String downtime;//名字

    @DatabaseField(columnName = "downtips")
    private String downtips;//名字

    @DatabaseField(columnName = "gotime")
    private String gotime;//名字

    @DatabaseField(columnName = "gotips")
    private String gotips;//名字

    @DatabaseField(columnName = "late")
    private String late;

    @DatabaseField(columnName = "lateTotal")
    private int lateTotal;

    @DatabaseField(columnName = "iconPath")
    private String iconPath;

    @DatabaseField(columnName = "iconUrl")
    private String iconUrl;

    @DatabaseField(columnName = "QRCodePath")
    private String QRCodePath;

    public String getQRCodePath() {
        return QRCodePath;
    }

    public void setQRCodePath(String QRCodePath) {
        this.QRCodePath = QRCodePath;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getComid() {
        return comid;
    }

    public void setComid(int comid) {
        this.comid = comid;
    }

    public String getCompName() {
        return compName;
    }

    public void setCompName(String compName) {
        this.compName = compName;
    }

    public String getAbbName() {
        return abbName;
    }

    public void setAbbName(String abbName) {
        this.abbName = abbName;
    }

    public String getTopTitle() {
        return topTitle;
    }

    public void setTopTitle(String topTitle) {
        this.topTitle = topTitle;
    }

    public String getBottomTitle() {
        return bottomTitle;
    }

    public void setBottomTitle(String bottomTitle) {
        this.bottomTitle = bottomTitle;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public String getDevicePwd() {
        return devicePwd;
    }

    public void setDevicePwd(String devicePwd) {
        this.devicePwd = devicePwd;
    }

    public String getDowntime() {
        return downtime;
    }

    public void setDowntime(String downtime) {
        this.downtime = downtime;
    }

    public String getDowntips() {
        return downtips;
    }

    public void setDowntips(String downtips) {
        this.downtips = downtips;
    }

    public String getGotime() {
        return gotime;
    }

    public void setGotime(String gotime) {
        this.gotime = gotime;
    }

    public String getGotips() {
        return gotips;
    }

    public void setGotips(String gotips) {
        this.gotips = gotips;
    }

    public String getLate() {
        return late;
    }

    public void setLate(String late) {
        this.late = late;
    }

    public int getLateTotal() {
        return lateTotal;
    }

    public void setLateTotal(int lateTotal) {
        this.lateTotal = lateTotal;
    }

    @Override
    public String toString() {
        return "CompBean{" +
                "id=" + id +
                ", comid=" + comid +
                ", compName='" + compName + '\'' +
                ", abbName='" + abbName + '\'' +
                ", topTitle='" + topTitle + '\'' +
                ", bottomTitle='" + bottomTitle + '\'' +
                ", slogan='" + slogan + '\'' +
                ", notice='" + notice + '\'' +
                ", devicePwd='" + devicePwd + '\'' +
                ", downtime='" + downtime + '\'' +
                ", downtips='" + downtips + '\'' +
                ", gotime='" + gotime + '\'' +
                ", gotips='" + gotips + '\'' +
                ", late='" + late + '\'' +
                ", lateTotal=" + lateTotal +
                ", iconPath='" + iconPath + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                '}';
    }
}
