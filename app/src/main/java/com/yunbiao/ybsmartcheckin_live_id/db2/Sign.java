package com.yunbiao.ybsmartcheckin_live_id.db2;

import android.graphics.Bitmap;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;
import java.util.Arrays;

@Entity
public class Sign {

    @Id(autoincrement = true)
    private Long id = null;

    private String faceId;

    private String name;

    private String position;

    private String headPath;

    private long time;

    private boolean isUpload;

    private long empId;

    private String date;

    private String depart;

    private int sex;

    private int age;

    private String employNum;

    private String birthday;

    private String autograph;

    private int comid;

    private int type;

    private long visEntryId;

    private float temperature;

    @Transient
    private byte[] imgBytes;

    @Transient
    private Bitmap imgBitmap;


    @Generated(hash = 1996633570)
    public Sign(Long id, String faceId, String name, String position,
            String headPath, long time, boolean isUpload, long empId, String date,
            String depart, int sex, int age, String employNum, String birthday,
            String autograph, int comid, int type, long visEntryId,
            float temperature) {
        this.id = id;
        this.faceId = faceId;
        this.name = name;
        this.position = position;
        this.headPath = headPath;
        this.time = time;
        this.isUpload = isUpload;
        this.empId = empId;
        this.date = date;
        this.depart = depart;
        this.sex = sex;
        this.age = age;
        this.employNum = employNum;
        this.birthday = birthday;
        this.autograph = autograph;
        this.comid = comid;
        this.type = type;
        this.visEntryId = visEntryId;
        this.temperature = temperature;
    }

    @Generated(hash = 2025164192)
    public Sign() {
    }


    public Bitmap getImgBitmap() {
        return imgBitmap;
    }

    public void setImgBitmap(Bitmap imgBitmap) {
        this.imgBitmap = imgBitmap;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getComid() {
        return comid;
    }

    public void setComid(int comid) {
        this.comid = comid;
    }

    public byte[] getImgBytes() {
        return imgBytes;
    }

    public void setImgBytes(byte[] imgBytes) {
        this.imgBytes = imgBytes;
    }

    public long getVisEntryId() {
        return visEntryId;
    }

    public void setVisEntryId(long visEntryId) {
        this.visEntryId = visEntryId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getHeadPath() {
        return headPath;
    }

    public void setHeadPath(String headPath) {
        this.headPath = headPath;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean upload) {
        isUpload = upload;
    }

    public long getEmpId() {
        return empId;
    }

    public void setEmpId(long empId) {
        this.empId = empId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDepart() {
        return depart;
    }

    public void setDepart(String depart) {
        this.depart = depart;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmployNum() {
        return employNum;
    }

    public void setEmployNum(String employNum) {
        this.employNum = employNum;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAutograph() {
        return autograph;
    }

    public void setAutograph(String autograph) {
        this.autograph = autograph;
    }

    public boolean getIsUpload() {
        return this.isUpload;
    }

    public void setIsUpload(boolean isUpload) {
        this.isUpload = isUpload;
    }

    @Override
    public String toString() {
        return "Sign{" +
                "id=" + id +
                ", faceId='" + faceId + '\'' +
                ", name='" + name + '\'' +
                ", position='" + position + '\'' +
                ", headPath='" + headPath + '\'' +
                ", time=" + time +
                ", isUpload=" + isUpload +
                ", empId=" + empId +
                ", date='" + date + '\'' +
                ", depart='" + depart + '\'' +
                ", sex=" + sex +
                ", age=" + age +
                ", employNum='" + employNum + '\'' +
                ", birthday='" + birthday + '\'' +
                ", autograph='" + autograph + '\'' +
                ", comid=" + comid +
                ", type=" + type +
                ", visEntryId=" + visEntryId +
                ", imgBytes=" + Arrays.toString(imgBytes) +
                '}';
    }
}
