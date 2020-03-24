package com.yunbiao.ybsmartcheckin_live_id.db2;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class MultiTotal {

    @Id(autoincrement = true)
    private Long Id;

    private long compId;

    @Unique
    private String date;

    private int totalNum;

    private int normalNum;

    private int warningNum;

    private int staffNum;

    private int visitorNum;

    @Generated(hash = 1116989140)
    public MultiTotal(Long Id, long compId, String date, int totalNum,
            int normalNum, int warningNum, int staffNum, int visitorNum) {
        this.Id = Id;
        this.compId = compId;
        this.date = date;
        this.totalNum = totalNum;
        this.normalNum = normalNum;
        this.warningNum = warningNum;
        this.staffNum = staffNum;
        this.visitorNum = visitorNum;
    }

    @Generated(hash = 688006462)
    public MultiTotal() {
    }

    public Long getId() {
        return this.Id;
    }

    public void setId(Long Id) {
        this.Id = Id;
    }

    public long getCompId() {
        return this.compId;
    }

    public void setCompId(long compId) {
        this.compId = compId;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getTotalNum() {
        return this.totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    public int getNormalNum() {
        return this.normalNum;
    }

    public void setNormalNum(int normalNum) {
        this.normalNum = normalNum;
    }

    public int getWarningNum() {
        return this.warningNum;
    }

    public void setWarningNum(int warningNum) {
        this.warningNum = warningNum;
    }

    public int getStaffNum() {
        return this.staffNum;
    }

    public void setStaffNum(int staffNum) {
        this.staffNum = staffNum;
    }

    public int getVisitorNum() {
        return this.visitorNum;
    }

    public void setVisitorNum(int visitorNum) {
        this.visitorNum = visitorNum;
    }
}
