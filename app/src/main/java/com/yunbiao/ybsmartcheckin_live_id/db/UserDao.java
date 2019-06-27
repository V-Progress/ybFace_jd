package com.yunbiao.ybsmartcheckin_live_id.db;

import android.content.Context;

import java.sql.SQLException;
import java.util.List;


/**
 * Created by Administrator on 2018/10/8.
 */

public class UserDao extends BaseDao<VIPDetail>{
    public UserDao(Context context) {
        super(context, VIPDetail.class);
    }

    // 删除user表中的一条数据
    public int deleteByFaceId(int faceId) {
        int result = 0;
//        List<VIPDetail> mlist = null;
        try {
//            mlist = dao.queryBuilder().where().eq("faceId", faceId).query();
//            if (mlist!=null&&mlist.size()>0){
//                dao.delete(mlist.get(0));
//            }

            List<VIPDetail> userBeans = queryByFaceId(faceId);
            if(userBeans != null && userBeans.size() > 0){
                result = dao.delete(userBeans.get(0));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    // 根据ID取出用户信息
    public List<VIPDetail> queryByFaceId(int faceId) {
        return queryByInt("faceId",faceId);
//        List<VIPDetail> mlist = null;
//        try {
//            mlist = dao.queryBuilder().where().eq("faceId", faceId).query();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return mlist;
    }

    //根据depart取出人脸信息
    public List<VIPDetail> queryByDepart(String depart) {
        return queryByString("depart",depart);
//        List<VIPDetail> mlist = null;
//        try {
//            mlist = dao.queryBuilder().where().eq("depart", depart).query();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return mlist;
    }

    //根据depart取出人脸信息
    public List<VIPDetail> queryByDepartAndName(String depart, String name) {
        List<VIPDetail> mlist = null;
        try {
            mlist = dao.queryBuilder().where().eq("depart", depart).and().eq("name", name).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mlist;
    }
}
