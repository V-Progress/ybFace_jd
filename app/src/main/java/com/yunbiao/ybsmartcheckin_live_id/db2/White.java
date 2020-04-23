package com.yunbiao.ybsmartcheckin_live_id.db2;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class White {

    @Id
    private Long _id;

    @Unique
    private String num;

    @Generated(hash = 595952750)
    public White(Long _id, String num) {
        this._id = _id;
        this.num = num;
    }

    @Generated(hash = 1575778372)
    public White() {
    }

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "White{" +
                "_id=" + _id +
                ", num='" + num + '\'' +
                '}';
    }
}
