package com.yunbiao.yb_smart_attendance;

import android.text.TextUtils;

import com.jdjr.risk.face.local.user.FaceUser;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Depart;
import com.yunbiao.ybsmartcheckin_live_id.db2.User;
import com.yunbiao.ybsmartcheckin_live_id.faceview.face_new.FaceSDK;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyXutils;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class DBSync {
    private static DBSync userSync = new DBSync();

    public static DBSync getInstance(){
        return userSync;
    }

    public void syncCompanyDB(Company company){
        DaoManager.get().addOrUpdate(company);
    }

    /***
     * 同步部门数据库
     * 因一公司一库，因此部门不再需要存公司id
     * @param deparray
     */
    public void syncDepartDB(List<Depart> deparray){
        //如果传入的是null或者是空则表示没有部门，删除全部
        List<Depart> departs = DaoManager.get().queryAll(Depart.class);
        if(deparray == null || deparray.size() <= 0){
            if(departs != null && departs.size() > 0){
                for (Depart depart : departs) {
                    DaoManager.get().delete(depart);
                }
            }
            return;
        }

        //生成map
        Map<Long,Depart> departMap = new HashMap<>();
        for (Depart depart : deparray) {
            departMap.put(depart.getDepId(),depart);
        }

        //删除不存在的部门
        if(departs != null){
            for (Depart depart : departs) {
                if(!departMap.containsKey(depart.getDepId())){
                    DaoManager.get().delete(depart);
                }
            }
        }

        //添加或更新部门
        for (Map.Entry<Long, Depart> entry : departMap.entrySet()) {
            Depart value = entry.getValue();
            DaoManager.get().addOrUpdate(value);
        }
    }

}
