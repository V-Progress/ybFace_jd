package com.yunbiao.ybsmartcheckin_live_id.db.dbtest;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.yunbiao.ybsmartcheckin_live_id.db.DatabaseHelper;
import com.yunbiao.ybsmartcheckin_live_id.db.VIPDetail;

import java.sql.SQLException;
import java.util.List;


/**
 * Created by Administrator on 2018/10/8.
 */

public class StaffDao extends BaseDao<VIPDetail>{

    public StaffDao(Context context) {
        super(context, VIPDetail.class);
    }

    public void deleteByFaceId(int faceId){
        List<VIPDetail> faces = queryByInt("faceId", faceId);
        if(faces != null && faces.size() > 0){
            for (VIPDetail face : faces) {
                delete(face);
            }
        }
    }
}
