package com.yunbiao.ybsmartcheckin_live_id.bean;

import com.yunbiao.ybsmartcheckin_live_id.db2.Company;

public class CompanyResponse extends BaseResponse {

    private Company company;

    @Override
    public String toString() {
        return "CompanyResponse{" +
                "company=" + company +
                '}';
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
