package com.yunbiao.ybsmartcheckin_live_id.db;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Administrator on 2018/10/8.
 */

public class DepartDao extends BaseDao<DepartBean>{
    public DepartDao(Context context) {
        super(context, DepartBean.class);
    }

    // 根据name取出用户信息
    public List<DepartBean> queryByName(String name) {
        List<DepartBean> mlist = null;
        try {
            mlist = dao.queryBuilder().where().eq("name", name).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mlist;
    }
}
