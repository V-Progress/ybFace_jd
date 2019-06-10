package com.yunbiao.ybsmartcheckin_live_id.db.dbtest.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Administrator on 2019/5/23.
 */

@DatabaseTable(tableName = "Company")
public class CompDBBean {
    public CompDBBean() {
    }

    public CompDBBean(int companyId, String compName) {
        this.companyId = companyId;
        this.compName = compName;
    }

    //generatedId=true：表示是主键
    @DatabaseField(generatedId = true,columnName = "companyId")
    private int companyId;

    @DatabaseField(columnName = "compName")
    private String compName;

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getCompName() {
        return compName;
    }

    public void setCompName(String compName) {
        this.compName = compName;
    }
}
