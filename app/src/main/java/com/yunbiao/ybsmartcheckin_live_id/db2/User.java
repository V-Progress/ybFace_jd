package com.yunbiao.ybsmartcheckin_live_id.db2;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class User {

    @Id
    private long id;

    @Unique
    private long faceId;

    private long departId;

    private String departName;

    private int companyId;


    private String name;
    private int age;
    private int sex;
    private String autograph;
    private String position;
    private String number;
    private int lateNum;
    private String cardId;
    private String birthday;

    private String head;
    private String headPath;

    private int addTag;

    @Generated(hash = 1381060079)
    public User(long id, long faceId, long departId, String departName,
            int companyId, String name, int age, int sex, String autograph,
            String position, String number, int lateNum, String cardId,
            String birthday, String head, String headPath, int addTag) {
        this.id = id;
        this.faceId = faceId;
        this.departId = departId;
        this.departName = departName;
        this.companyId = companyId;
        this.name = name;
        this.age = age;
        this.sex = sex;
        this.autograph = autograph;
        this.position = position;
        this.number = number;
        this.lateNum = lateNum;
        this.cardId = cardId;
        this.birthday = birthday;
        this.head = head;
        this.headPath = headPath;
        this.addTag = addTag;
    }

    @Generated(hash = 586692638)
    public User() {
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", faceId=" + faceId +
                ", departId=" + departId +
                ", departName='" + departName + '\'' +
                ", companyId=" + companyId +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", sex=" + sex +
                ", autograph='" + autograph + '\'' +
                ", position='" + position + '\'' +
                ", number='" + number + '\'' +
                ", lateNum=" + lateNum +
                ", cardId='" + cardId + '\'' +
                ", birthday='" + birthday + '\'' +
                ", head='" + head + '\'' +
                ", headPath='" + headPath + '\'' +
                ", addTag=" + addTag +
                '}';
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getFaceId() {
        return faceId;
    }

    public void setFaceId(long faceId) {
        this.faceId = faceId;
    }

    public long getDepartId() {
        return departId;
    }

    public void setDepartId(long departId) {
        this.departId = departId;
    }

    public String getDepartName() {
        return departName;
    }

    public void setDepartName(String departName) {
        this.departName = departName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getAutograph() {
        return autograph;
    }

    public void setAutograph(String autograph) {
        this.autograph = autograph;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getLateNum() {
        return lateNum;
    }

    public void setLateNum(int lateNum) {
        this.lateNum = lateNum;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getHeadPath() {
        return headPath;
    }

    public void setHeadPath(String headPath) {
        this.headPath = headPath;
    }

    public int getAddTag() {
        return addTag;
    }

    public void setAddTag(int addTag) {
        this.addTag = addTag;
    }
}
