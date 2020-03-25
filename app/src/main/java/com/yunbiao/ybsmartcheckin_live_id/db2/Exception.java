package com.yunbiao.ybsmartcheckin_live_id.db2;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Exception {

    @org.greenrobot.greendao.annotation.Id(autoincrement = true)
    private Long Id;

    @Unique
    private String crashTime;

    private String crashExeption;

    @Generated(hash = 1867725288)
    public Exception(Long Id, String crashTime, String crashExeption) {
        this.Id = Id;
        this.crashTime = crashTime;
        this.crashExeption = crashExeption;
    }

    @Generated(hash = 357195249)
    public Exception() {
    }

    public Long getId() {
        return this.Id;
    }

    public void setId(Long Id) {
        this.Id = Id;
    }

    public String getCrashTime() {
        return this.crashTime;
    }

    public void setCrashTime(String crashTime) {
        this.crashTime = crashTime;
    }

    public String getCrashExeption() {
        return this.crashExeption;
    }

    public void setCrashExeption(String crashExeption) {
        this.crashExeption = crashExeption;
    }

}
