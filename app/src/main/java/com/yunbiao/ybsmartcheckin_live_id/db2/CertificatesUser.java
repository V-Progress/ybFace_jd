package com.yunbiao.ybsmartcheckin_live_id.db2;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class CertificatesUser {

    @Id(autoincrement = true)
    private Long _Id;

    private String name;

    @Unique
    private String num;

    private String depart;

    private String headPath;

    @Generated(hash = 244883290)
    public CertificatesUser(Long _Id, String name, String num, String depart,
            String headPath) {
        this._Id = _Id;
        this.name = name;
        this.num = num;
        this.depart = depart;
        this.headPath = headPath;
    }

    @Generated(hash = 1972363754)
    public CertificatesUser() {
    }

    public Long get_Id() {
        return _Id;
    }

    public void set_Id(Long _Id) {
        this._Id = _Id;
    }

    public String getDepart() {
        return depart;
    }

    public void setDepart(String depart) {
        this.depart = depart;
    }

    public String getHeadPath() {
        return headPath;
    }

    public void setHeadPath(String headPath) {
        this.headPath = headPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }
}
