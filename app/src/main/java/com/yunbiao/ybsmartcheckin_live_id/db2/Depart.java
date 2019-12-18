package com.yunbiao.ybsmartcheckin_live_id.db2;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;

import java.util.List;

@Entity
public class Depart {

    @Id
    private long id;

    @Unique
    private long depId;

    private String depName;

    private int compId;

    public int getCompId() {
        return compId;
    }

    public void setCompId(int compId) {
        this.compId = compId;
    }

    @Transient
    private List<User> entry;

    @Generated(hash = 85264584)
    public Depart(long id, long depId, String depName, int compId) {
        this.id = id;
        this.depId = depId;
        this.depName = depName;
        this.compId = compId;
    }

    @Generated(hash = 1469698209)
    public Depart() {
    }

    @Override
    public String toString() {
        return "Depart{" +
                "id=" + id +
                ", depId=" + depId +
                ", depName='" + depName + '\'' +
                ", compId=" + compId +
                ", entry=" + entry +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDepId() {
        return depId;
    }

    public void setDepId(long depId) {
        this.depId = depId;
    }

    public String getDepName() {
        return depName;
    }

    public void setDepName(String depName) {
        this.depName = depName;
    }

    public List<User> getEntry() {
        return entry;
    }

    public void setEntry(List<User> entry) {
        this.entry = entry;
    }
}
