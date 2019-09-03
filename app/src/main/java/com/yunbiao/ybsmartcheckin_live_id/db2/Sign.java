package com.yunbiao.ybsmartcheckin_live_id.db2;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Sign {

    @Id
    private Long id;

    private long faceId;

    private String name;

    private String position;

    private String headPath;

    private long time;

    private boolean isUpload;

    private int empId;

    private String date;

    private String depart;

    private int sex;

    private int age;

    private String employNum;

    private String birthday;

    private String autograph;

    @Generated(hash = 1811910173)
    public Sign(Long id, long faceId, String name, String position, String headPath,
            long time, boolean isUpload, int empId, String date, String depart,
            int sex, int age, String employNum, String birthday, String autograph) {
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
    }

    @Generated(hash = 2025164192)
    public Sign() {
    }

    @Override
    public String toString() {
        return "Sign{" +
                "id=" + id +
                ", faceId=" + faceId +
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
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getFaceId() {
        return faceId;
    }

    public void setFaceId(long faceId) {
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

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
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
}
