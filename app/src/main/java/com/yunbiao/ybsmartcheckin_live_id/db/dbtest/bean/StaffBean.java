package com.yunbiao.ybsmartcheckin_live_id.db.dbtest.bean;

/**
 * Created by Administrator on 2017/7/28.
 */

import android.graphics.Bitmap;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Staff")
public class StaffBean {


    public StaffBean() {
    }

    public StaffBean(String sex, String age, Bitmap bitmap) {
        this.sex = sex;
        this.age = age;
        this.bitmap = bitmap;
    }

    public StaffBean(String employNum, String name, String depart, String job ) {
        this.name = name;
        this.job = job;
        this.depart = depart;
        this.employNum = employNum;
    }

    public StaffBean(String name, String sex, String age, String job, String depart, long time) {
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.job = job;
        this.depart = depart;
        this.time = time;
    }

    public StaffBean(String name, String sex, String age, long time) {
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.time = time;
    }

    public StaffBean(int faceId, String name, String sex, String age, String job) {

        this.faceId = faceId;
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.job = job;
    }

    public StaffBean(int faceId, String name, String sex, String age, String job, String imgUrl) {
        this.faceId = faceId;
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.job = job;
        this.imgUrl = imgUrl;
    }

    public StaffBean(int faceId, String name, String sex, String age, String job, String imgUrl, long time) {
        this.faceId = faceId;
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.job = job;
        this.imgUrl = imgUrl;
        this.time = time;
    }

    //签到
    public StaffBean(String name, String sex, String age, String job, String imgUrl, long time, String depart, String signature) {
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.job = job;
        this.imgUrl = imgUrl;
        this.time = time;
        this.depart = depart;
        this.Signature = signature;
    }

    //添加新员工
    public StaffBean(int departId, int empId, int faceId, String sex, String age, String name, String depart, String job, String employNum, String birthday, String signature, String imgUrl) {
        this.departId = departId;
        this.empId = empId;
        this.faceId = faceId;
        this.sex = sex;
        this.age = age;
        this.name = name;
        this.job = job;
        this.employNum = employNum;
        this.imgUrl = imgUrl;
        this.depart = depart;
        this.birthday = birthday;
        Signature = signature;
    }

    @DatabaseField(columnName = "empId",unique = true,generatedId = true)
    private int empId;//员工id

    @DatabaseField(columnName = "departId",foreign = true)
    private int departId;//部门id

    @DatabaseField(columnName = "faceId")
    private int faceId;//图片id


    @DatabaseField(columnName = "name")
    private String name;//名字


    @DatabaseField(columnName = "sex")
    private String sex;//性别

    @DatabaseField(columnName = "age")
    private String age;//年龄

    @DatabaseField(columnName = "job")
    private String job;//职位

     @DatabaseField(columnName = "imgUrl")
    private String imgUrl;//头像文件

    @DatabaseField(columnName = "time")
    private long time;//时间

    @DatabaseField(columnName = "depart")
    private String depart;//员工部门

    @DatabaseField(columnName = "employNum")
    private String employNum;//员工编号

     @DatabaseField(columnName = "birthday")
    private String birthday;//员工生日

     @DatabaseField(columnName = "Signature")
    private String Signature;//个性签名

    @DatabaseField(columnName = "downloadTag")
    private boolean downloadTag;//个性签名

    public boolean getDownloadTag() {
        return downloadTag;
    }

    public void setDownloadTag(boolean downloadTag) {
        this.downloadTag = downloadTag;
    }

    public String getSignature() {
        return Signature;
    }

    public void setSignature(String signature) {
        Signature = signature;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getEmployNum() {
        return employNum;
    }

    public void setEmployNum(String employNum) {
        this.employNum = employNum;
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

    private Bitmap bitmap;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public int getDepartId() {
        return departId;
    }

    public void setDepartId(int departId) {
        this.departId = departId;
    }
}
