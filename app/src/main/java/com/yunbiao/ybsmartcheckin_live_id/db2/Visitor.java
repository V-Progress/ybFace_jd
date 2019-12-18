package com.yunbiao.ybsmartcheckin_live_id.db2;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class Visitor {
    @Id
    protected long id;
    @Unique
    protected String faceId;

    private int comId;

    public int getComId() {
        return comId;
    }

    public void setComId(int comId) {
        this.comId = comId;
    }

    private String unit;

    private String phone;

    private int sex;

    private String currEnd;

    private String reason;

    private String name;

    private String head;

    private String currStart;

    private String headPath;

    private int addTag;

    private long visEntryId;

    @Generated(hash = 22565682)
    public Visitor(long id, String faceId, int comId, String unit, String phone,
            int sex, String currEnd, String reason, String name, String head,
            String currStart, String headPath, int addTag, long visEntryId) {
        this.id = id;
        this.faceId = faceId;
        this.comId = comId;
        this.unit = unit;
        this.phone = phone;
        this.sex = sex;
        this.currEnd = currEnd;
        this.reason = reason;
        this.name = name;
        this.head = head;
        this.currStart = currStart;
        this.headPath = headPath;
        this.addTag = addTag;
        this.visEntryId = visEntryId;
    }

    @Generated(hash = 382853925)
    public Visitor() {
    }

    public long getVisEntryId() {
        return visEntryId;
    }

    public void setVisEntryId(long visEntryId) {
        this.visEntryId = visEntryId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getCurrEnd() {
        return currEnd;
    }

    public void setCurrEnd(String currEnd) {
        this.currEnd = currEnd;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getCurrStart() {
        return currStart;
    }

    public void setCurrStart(String currStart) {
        this.currStart = currStart;
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

    @Override
    public String toString() {
        return "Visitor{" +
                "id=" + id +
                ", faceId='" + faceId + '\'' +
                ", comId=" + comId +
                ", unit='" + unit + '\'' +
                ", phone='" + phone + '\'' +
                ", sex=" + sex +
                ", currEnd='" + currEnd + '\'' +
                ", reason='" + reason + '\'' +
                ", name='" + name + '\'' +
                ", head='" + head + '\'' +
                ", currStart='" + currStart + '\'' +
                ", headPath='" + headPath + '\'' +
                ", addTag=" + addTag +
                '}';
    }
}
