package com.yunbiao.ybsmartcheckin_live_id.db.dbtest;

import android.content.Context;

import com.yunbiao.ybsmartcheckin_live_id.db.dbtest.bean.CompDBBean;

import java.util.List;

/**
 * Created by Administrator on 2019/5/23.
 */

public class CompDao extends BaseDao<CompDBBean> {

    public CompDao(Context context) {
        super(context, CompDBBean.class);
    }

    public boolean isExists(int id){
        List<CompDBBean> companyBeen = queryByComId(id);
        return companyBeen != null && companyBeen.size()>0;
    }

    public List<CompDBBean> queryByComId(int id){
        return queryByInt("companyId",id);
    }
}
