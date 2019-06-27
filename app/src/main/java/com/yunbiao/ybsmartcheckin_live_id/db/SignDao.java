package com.yunbiao.ybsmartcheckin_live_id.db;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;


/**
 * Created by Administrator on 2018/10/8.
 */

public class SignDao extends BaseDao<SignBean>{

    private static final String TAG = "SignDao";

    public SignDao(Context context) {
        super(context, SignBean.class);
    }

    //根据年月日取出签到信息
    public List<SignBean> queryByDate(String date) {
        List<SignBean> mlist = null;
        try {
            mlist = dao.queryBuilder().orderBy("id", false).where().eq("date", date).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mlist;
    }

    //根据是否上传取出签到信息
    public List<SignBean> queryByIsUpload(boolean isUpload) {
        List<SignBean> mlist = null;
        try {
            mlist = dao.queryBuilder().where().eq("isUpload", isUpload).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mlist;
    }
}
