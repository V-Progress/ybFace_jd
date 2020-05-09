package com.yunbiao.ybsmartcheckin_live_id.db2;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class VertifyRecord {
    @Id(autoincrement = true)
    private Long _id = null;

    private String name;
    private String sex;
    private String nation;
    private String birthDate;
    private String idNum;
    private String address;
    private String termDate;

    private String idCardHeadPath;
    private String personHeadPath;
    private String hotImagePath;

    private String similar;
    private String isPass;
    private String comId;
    private String temper;
    private long time;
    private String date;

    private boolean isUpload = false;

    @Generated(hash = 1042917765)
    public VertifyRecord(Long _id, String name, String sex, String nation,
            String birthDate, String idNum, String address, String termDate,
            String idCardHeadPath, String personHeadPath, String hotImagePath,
            String similar, String isPass, String comId, String temper, long time,
            String date, boolean isUpload) {
        this._id = _id;
        this.name = name;
        this.sex = sex;
        this.nation = nation;
        this.birthDate = birthDate;
        this.idNum = idNum;
        this.address = address;
        this.termDate = termDate;
        this.idCardHeadPath = idCardHeadPath;
        this.personHeadPath = personHeadPath;
        this.hotImagePath = hotImagePath;
        this.similar = similar;
        this.isPass = isPass;
        this.comId = comId;
        this.temper = temper;
        this.time = time;
        this.date = date;
        this.isUpload = isUpload;
    }

    @Generated(hash = 1708888079)
    public VertifyRecord() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean upload) {
        isUpload = upload;
    }

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getIdNum() {
        return idNum;
    }

    public void setIdNum(String idNum) {
        this.idNum = idNum;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTermDate() {
        return termDate;
    }

    public void setTermDate(String termDate) {
        this.termDate = termDate;
    }

    public String getIdCardHeadPath() {
        return idCardHeadPath;
    }

    public void setIdCardHeadPath(String idCardHeadPath) {
        this.idCardHeadPath = idCardHeadPath;
    }

    public String getPersonHeadPath() {
        return personHeadPath;
    }

    public void setPersonHeadPath(String personHeadPath) {
        this.personHeadPath = personHeadPath;
    }

    public String getHotImagePath() {
        return hotImagePath;
    }

    public void setHotImagePath(String hotImagePath) {
        this.hotImagePath = hotImagePath;
    }

    public String getSimilar() {
        return similar;
    }

    public void setSimilar(String similar) {
        this.similar = similar;
    }

    public String getIsPass() {
        return isPass;
    }

    public void setIsPass(String isPass) {
        this.isPass = isPass;
    }

    public String getComId() {
        return comId;
    }

    public void setComId(String comId) {
        this.comId = comId;
    }

    public String getTemper() {
        return temper;
    }

    public void setTemper(String temper) {
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
        return "VertifyRecord{" +
                "_id=" + _id +
                ", name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", nation='" + nation + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", idNum='" + idNum + '\'' +
                ", address='" + address + '\'' +
                ", termDate='" + termDate + '\'' +
                ", idCardHeadPath='" + idCardHeadPath + '\'' +
                ", personHeadPath='" + personHeadPath + '\'' +
                ", hotImagePath='" + hotImagePath + '\'' +
                ", similar='" + similar + '\'' +
                ", isPass='" + isPass + '\'' +
                ", comId='" + comId + '\'' +
                ", temper='" + temper + '\'' +
                ", time='" + time + '\'' +
                '}';
    }

    public boolean getIsUpload() {
        return this.isUpload;
    }

    public void setIsUpload(boolean isUpload) {
        this.isUpload = isUpload;
    }
}
