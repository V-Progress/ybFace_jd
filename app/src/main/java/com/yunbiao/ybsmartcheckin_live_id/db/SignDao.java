package com.yunbiao.ybsmartcheckin_live_id.db;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;


/**
 * Created by Administrator on 2018/10/8.
 */

public class SignDao {

    private static final String TAG = "SignDao";
    private Context context;
    // ORMLite提供的DAO类对象，第一个泛型是要操作的数据表映射成的实体类；第二个泛型是这个实体类中ID的数据类型
    private Dao<SignBean, Integer> dao;

    public SignDao() {
    }

    public SignDao(Context context) {
        this.context = context;
        try {
            this.dao = DatabaseHelper.getInstance(context).getDao(SignBean.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 向user表中添加一条数据
    public void insert(SignBean data) {
        try {
            dao.create(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 删除user表中的一条数据
    public void delete(SignBean data) {
        try {
            dao.delete(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 删除user表中的一条数据
    public void deleteByFaceId(int faceId) {
        List<SignBean> mlist = null;
        try {
            mlist = dao.queryBuilder().where().eq("faceId", faceId).query();
            if (mlist!=null&&mlist.size()>0){
                dao.delete(mlist.get(0));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // 修改user表中的一条数据
    public void update(SignBean data) {
        try {
            dao.update(data);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "update: ----------------------------" );
        }
    }

    // 查询user表中的所有数据
    public List<SignBean> selectAll() {
        List<SignBean> users = null;
        try {
            users = dao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // 根据ID取出用户信息
    public SignBean queryById(int id) {
        SignBean user = null;
        try {
            user = dao.queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
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

    //根据时间戳取出签到信息
    public List<SignBean> queryByTime(long time) {
        List<SignBean> mlist = null;
        try {
            mlist = dao.queryBuilder().where().eq("time", time).query();
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
