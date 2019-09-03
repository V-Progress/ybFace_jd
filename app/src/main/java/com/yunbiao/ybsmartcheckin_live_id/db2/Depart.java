package com.yunbiao.ybsmartcheckin_live_id.db2;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.util.List;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Depart {

    @Id
    private long id;

    private int depId;

    private String depName;

    @Transient
    private List<User> entry;

    @Generated(hash = 666930247)
    public Depart(long id, int depId, String depName) {
        this.id = id;
        this.depId = depId;
        this.depName = depName;
    }

    @Generated(hash = 1469698209)
    public Depart() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDepId() {
        return depId;
    }

    public void setDepId(int depId) {
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
