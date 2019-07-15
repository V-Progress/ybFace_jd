package com.yunbiao.ybsmartcheckin_live_id.db;

import android.content.Context;

import java.util.List;

public class CompDao extends BaseDao<CompBean> {
    public CompDao(Context context) {
        super(context, CompBean.class);
    }

    public CompBean queryByCompId(int compId){
        List<CompBean> list = queryByInt("comid", compId);
        if(list != null && list.size() > 0){
            return list.get(0);
        }
        return null;
    }
}
