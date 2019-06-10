package com.yunbiao.ybsmartcheckin_live_id.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Administrator on 2018/8/16.
 */
@DatabaseTable(tableName = "VisitBean")
public class VisitBean {

    @DatabaseField(generatedId = true)
    private int id;

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

    public VisitBean(String name, String sex, String age, String job, String imgUrl, long time) {
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.job = job;
        this.imgUrl = imgUrl;
        this.time = time;
    }
}
