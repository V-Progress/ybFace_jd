package com.yunbiao.ybsmartcheckin_live_id.db;

import android.graphics.Bitmap;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Administrator on 2018/10/10.
 */
@DatabaseTable(tableName = "SignBean")
public class SignBean {

    public SignBean() {
    }

    public SignBean(int faceId, int empId,String name, String job, String imgUrl, long time, boolean isUpload,String signature) {
        this.faceId = faceId;
        this.empId = empId;
        this.name = name;
        this.job = job;
        this.imgUrl = imgUrl;
        this.time = time;
        this.isUpload = isUpload;
        this.Signature = signature;
    }

    public SignBean(int empId, String name, String job, String imgUrl, long time, String depart, String date, String sex, boolean isUpload) {
        this.empId = empId;
        this.name = name;
        this.job = job;
        this.imgUrl = imgUrl;
        this.time = time;
        this.depart = depart;
        this.date = date;
        this.sex = sex;
        this.isUpload = isUpload;
    }

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = "faceId")
    private int faceId;//图片id

    @DatabaseField(columnName = "name")
    private String name;//名字

    @DatabaseField(columnName = "job")
    private String job;//职位

    @DatabaseField(columnName = "imgUrl")
    private String imgUrl;//头像文件

    @DatabaseField(columnName = "time")
    private long time;//时间

    @DatabaseField(columnName = "isUpload")
    private boolean isUpload;//是否上传

    @DatabaseField(columnName = "empId")
    private int empId;//员工id

    @DatabaseField(columnName = "date")
    private String date;//时间

    @DatabaseField(columnName = "depart")
    private String depart;//员工部门

    @DatabaseField(columnName = "sex")
    private String sex;//性别

    private String age;//年龄

    private String employNum;//员工编号

    private String birthday;//员工生日

    private String Signature;//个性签名

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean upload) {
        isUpload = upload;
    }

    public int getFaceId() {
        return faceId;
    }

    public void setFaceId(int faceId) {
        this.faceId = faceId;
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

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getDepart() {
        return depart;
    }

    public void setDepart(String depart) {
        this.depart = depart;
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

    public String getSignature() {
        return Signature;
    }

    public void setSignature(String signature) {
        Signature = signature;
    }

    @Override
    public String toString() {
        return "SignBean{" +
                "id=" + id +
                ", faceId=" + faceId +
                ", name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", age='" + age + '\'' +
                ", job='" + job + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", time=" + time +
                ", date='" + date + '\'' +
                ", depart='" + depart + '\'' +
                ", employNum='" + employNum + '\'' +
                ", birthday='" + birthday + '\'' +
                ", Signature='" + Signature + '\'' +
                ", isUpload=" + isUpload +
                ", empId=" + empId +
                '}';
    }
}
