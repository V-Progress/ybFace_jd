package com.yunbiao.ybsmartcheckin_live_id.db;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;


/**
 * Created by Administrator on 2018/10/8.
 */

public class UserDao {
    private Context context;
    // ORMLite提供的DAO类对象，第一个泛型是要操作的数据表映射成的实体类；第二个泛型是这个实体类中ID的数据类型
    private Dao<VIPDetail, Integer> dao;

    public UserDao() {
    }

    public UserDao(Context context) {
        this.context = context;
        try {
            this.dao = DatabaseHelper.getInstance(context).getDao(VIPDetail.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 向user表中添加一条数据
    public void insert(VIPDetail data) {
        try {
            dao.create(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 删除user表中的一条数据
    public void delete(VIPDetail data) {
        try {
            dao.delete(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 删除user表中的一条数据
    public void deleteByFaceId(int faceId) {
        List<VIPDetail> mlist = null;
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
    public void update(VIPDetail data) {
        try {
            dao.update(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 查询user表中的所有数据
    public List<VIPDetail> selectAll() {
        List<VIPDetail> users = null;
        try {
            users = dao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // 根据ID取出用户信息
    public VIPDetail queryById(int id) {
        VIPDetail user = null;
        try {
            user = dao.queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    // 根据ID取出用户信息
    public List<VIPDetail> queryByFaceId(int faceId) {
        List<VIPDetail> mlist = null;
        try {
            mlist = dao.queryBuilder().where().eq("faceId", faceId).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mlist;
    }

    //根据depart取出人脸信息
    public List<VIPDetail> queryByDepart(String depart) {
        List<VIPDetail> mlist = null;
        try {
            mlist = dao.queryBuilder().where().eq("depart", depart).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mlist;
    }

    //根据depart取出人脸信息
    public List<VIPDetail> queryByDepartAndName(String depart,String name) {
        List<VIPDetail> mlist = null;
        try {
            mlist = dao.queryBuilder().where().eq("depart", depart).and().eq("name", name).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mlist;
    }
}
