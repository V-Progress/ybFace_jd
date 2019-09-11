package com.yunbiao.ybsmartcheckin_live_id.db2;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;

import java.util.List;

@Entity
public class Company {

    @Id
    private long id;

    @Unique
    private int comid;

    @Transient
    private List<Depart> deparray;

    private String slogan;
    private String comname;
    private String abbname;
    private String downtime;
    private String devicePwd;
    private String gotime;
    private String gotips;
    private String comlogo;

    private int themeid;
    private String toptitle;
    private String downtips;
    private String bottomtitle;

    @Generated(hash = 381826835)
    public Company(long id, int comid, String slogan, String comname,
            String abbname, String downtime, String devicePwd, String gotime,
            String gotips, String comlogo, int themeid, String toptitle,
            String downtips, String bottomtitle) {
        this.id = id;
        this.comid = comid;
        this.slogan = slogan;
        this.comname = comname;
        this.abbname = abbname;
        this.downtime = downtime;
        this.devicePwd = devicePwd;
        this.gotime = gotime;
        this.gotips = gotips;
        this.comlogo = comlogo;
        this.themeid = themeid;
        this.toptitle = toptitle;
        this.downtips = downtips;
        this.bottomtitle = bottomtitle;
    }

    @Generated(hash = 1096856789)
    public Company() {
    }

    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", comid=" + comid +
                ", deparray=" + deparray +
                ", slogan='" + slogan + '\'' +
                ", comname='" + comname + '\'' +
                ", abbname='" + abbname + '\'' +
                ", downtime='" + downtime + '\'' +
                ", devicePwd='" + devicePwd + '\'' +
                ", gotime='" + gotime + '\'' +
                ", gotips='" + gotips + '\'' +
                ", comlogo='" + comlogo + '\'' +
                ", themeid=" + themeid +
                ", toptitle='" + toptitle + '\'' +
                ", downtips='" + downtips + '\'' +
                ", bottomtitle='" + bottomtitle + '\'' +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getComid() {
        return comid;
    }

    public void setComid(int comid) {
        this.comid = comid;
    }

    public List<Depart> getDeparray() {
        return deparray;
    }

    public void setDeparray(List<Depart> deparray) {
        this.deparray = deparray;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public String getComname() {
        return comname;
    }

    public void setComname(String comname) {
        this.comname = comname;
    }

    public String getAbbname() {
        return abbname;
    }

    public void setAbbname(String abbname) {
        this.abbname = abbname;
    }

    public String getDowntime() {
        return downtime;
    }

    public void setDowntime(String downtime) {
        this.downtime = downtime;
    }

    public String getDevicePwd() {
        return devicePwd;
    }

    public void setDevicePwd(String devicePwd) {
        this.devicePwd = devicePwd;
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

    public String getComlogo() {
        return comlogo;
    }

    public void setComlogo(String comlogo) {
        this.comlogo = comlogo;
    }

    public int getThemeid() {
        return themeid;
    }

    public void setThemeid(int themeid) {
        this.themeid = themeid;
    }

    public String getToptitle() {
        return toptitle;
    }

    public void setToptitle(String toptitle) {
        this.toptitle = toptitle;
    }

    public String getDowntips() {
        return downtips;
    }

    public void setDowntips(String downtips) {
        this.downtips = downtips;
    }

    public String getBottomtitle() {
        return bottomtitle;
    }

    public void setBottomtitle(String bottomtitle) {
        this.bottomtitle = bottomtitle;
    }
    // TODO: 2019/9/3 公司信息下不需要，要改到员工信息下
    /*private List<Late> late;
    public class Late {
        private int lateNum;
        private int entryId;

        public int getLateNum() {
            return lateNum;
        }

        public void setLateNum(int lateNum) {
            this.lateNum = lateNum;
        }

        public int getEntryId() {
            return entryId;
        }

        public void setEntryId(int entryId) {
            this.entryId = entryId;
        }
    }*/
}
