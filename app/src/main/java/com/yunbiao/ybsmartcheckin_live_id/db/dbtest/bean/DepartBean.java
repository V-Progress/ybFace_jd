package com.yunbiao.ybsmartcheckin_live_id.db.dbtest.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Administrator on 2018/10/8.
 */
@DatabaseTable(tableName = "Department")
public class DepartBean {
    public DepartBean() {
    }

    public DepartBean(String name, int departId) {
        this.name = name;
        this.departId = departId;
    }

    public DepartBean(int companyId, String name, int departId) {
        this.companyId = companyId;
        this.name = name;
        this.departId = departId;
    }

    @DatabaseField(columnName = "companyId",foreign = true)
    private int companyId;//名字

    @DatabaseField(columnName = "name")
    private String name;//名字

    @DatabaseField(generatedId = true,columnName = "departId")
    private int departId;//部门id

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDepartId() {
        return departId;
    }

    public void setDepartId(int departId) {
        this.departId = departId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    @Override
    public String toString() {
        return "{name=" + name + ", departId=" + departId + '}';
    }
}
