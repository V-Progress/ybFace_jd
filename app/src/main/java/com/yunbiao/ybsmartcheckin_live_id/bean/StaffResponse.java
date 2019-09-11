package com.yunbiao.ybsmartcheckin_live_id.bean;

import com.yunbiao.ybsmartcheckin_live_id.db2.Depart;

import java.util.List;

public class StaffResponse extends BaseResponse {
    private List<Depart> dep;

    @Override
    public String toString() {
        return "StaffResponse{" +
                "dep=" + dep +
                '}';
    }

    public List<Depart> getDep() {
        return dep;
    }

    public void setDep(List<Depart> dep) {
        this.dep = dep;
    }
}
