package com.yunbiao.ybsmartcheckin_live_id.db2;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class User {

    @Id
    private long id;

    @Unique
    private int faceId;

    private int departId;

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

    private String head;
    private String headPath;

    private int addTag;

    @Generated(hash = 1121530789)
    public User(long id, int faceId, int departId, String departName, int companyId,
            String name, int age, int sex, String autograph, String position,
            String number, int lateNum, String cardId, String head, String headPath,
            int addTag) {
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
        this.head = head;
        this.headPath = headPath;
        this.addTag = addTag;
    }

    @Generated(hash = 586692638)
    public User() {
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", faceId=" + faceId +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", sex=" + sex +
                ", autograph='" + autograph + '\'' +
                ", position='" + position + '\'' +
                ", number='" + number + '\'' +
                ", lateNum=" + lateNum +
                ", cardId='" + cardId + '\'' +
                ", head='" + head + '\'' +
                ", headPath='" + headPath + '\'' +
                ", addTag=" + addTag +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public int getDepartId() {
        return this.departId;
    }

    public void setDepartId(int departId) {
        this.departId = departId;
    }

    public String getDepartName() {
        return this.departName;
    }

    public void setDepartName(String departName) {
        this.departName = departName;
    }

    public int getCompanyId() {
        return this.companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }
}
