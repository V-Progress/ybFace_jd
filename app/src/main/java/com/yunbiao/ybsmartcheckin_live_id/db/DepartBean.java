package com.yunbiao.ybsmartcheckin_live_id.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Administrator on 2018/10/8.
 */
@DatabaseTable(tableName = "DepartBean")
public class DepartBean {
    public DepartBean() {
    }

    public DepartBean(String name, int departId) {
        this.name = name;
        this.departId = departId;
    }

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = "name")
    private String name;//名字

    @DatabaseField(columnName = "departId")
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

    @Override
    public String toString() {
        return "{name=" + name + ", departId=" + departId + '}';
    }
}
