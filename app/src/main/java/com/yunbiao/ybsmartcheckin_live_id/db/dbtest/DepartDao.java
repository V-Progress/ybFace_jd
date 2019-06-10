package com.yunbiao.ybsmartcheckin_live_id.db.dbtest;

import android.content.Context;

import com.yunbiao.ybsmartcheckin_live_id.db.DepartBean;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Administrator on 2019/5/23.
 */

public class DepartDao extends BaseDao<DepartBean> {
    public DepartDao(Context context) {
        super(context, DepartBean.class);
    }

    // 根据name取出用户信息
    public List<DepartBean> queryByName(String name) {
        return queryByString("name", name);
    }

    public List<DepartBean> queryById(int depId){
        return queryByInt("departId",depId);
    }

    public boolean isExists(int id){
        List<DepartBean> departBeen = queryById(id);
        return departBeen != null && departBeen.size()>0;
    }
}
