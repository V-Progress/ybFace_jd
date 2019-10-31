package com.yunbiao.ybsmartcheckin_live_id.db2;

import android.util.Log;

import com.yunbiao.ybsmartcheckin_live_id.APP;

import org.greenrobot.greendao.database.Database;

import java.util.List;

public class DaoManager {
    private static final String TAG = "DaoManager";
    private static DaoManager daoManager = new DaoManager();
    private final String DB_NAME = "yb_meeting_db";
    private DaoMaster daoMaster;
    private DaoSession daoSession;

    public static final long FAILURE = -1;
    public static final long SUCCESS = 0;

    public static DaoManager get(){
        return daoManager;
    }

    private DaoManager(){
    }

    public void initDb(){
        Log.e(TAG, "initDb: ");
        MySQLiteHelper helper =new MySQLiteHelper(APP.getContext(),DB_NAME,null);
        Log.e(TAG, "initDb: " + helper);
        Database db = helper.getWritableDb();
        Log.e(TAG, "initDb: " + db);
        daoMaster = new DaoMaster(db);
        Log.e(TAG, "initDb: " + daoMaster);
        daoSession = daoMaster.newSession();
        Log.e(TAG, "initDb: " + daoSession);
        daoSession.clear();
        daoSession.getUserDao().detachAll();
        daoSession.getDepartDao().detachAll();
        daoSession.getSignDao().detachAll();
        daoSession.getCompanyDao().detachAll();
    }

    public DaoSession getDaoSession(){
        return daoSession;
    }

    public DaoMaster getDaoMaster(){
        return daoMaster;
    }

    public <T> long add(T clazz){
        if(daoSession == null){
            return FAILURE;
        }
        return daoSession.insert(clazz);
    }

    public <T> long addOrUpdate(T clazz){
        if(daoSession == null){
            return FAILURE;
        }
       return daoSession.insertOrReplace(clazz);
    }

    public <T>long update(T t){
        if(daoSession == null){
            return FAILURE;
        }
        daoSession.update(t);
        return SUCCESS;
    }

    public <T>List<T> queryAll(Class<T> clazz){
        if(daoSession == null){
            return null;
        }
        return daoSession.loadAll(clazz);
    }

    public <T>long delete(T t){
        if(daoSession == null){
            return FAILURE;
        }
        daoSession.delete(t);
        return SUCCESS;
    }

    /***
     * 查询某公司下某天的所有打卡记录
     * @param comId
     * @param date
     * @return
     */
    public List<Sign> querySignByComIdAndDate(int comId,String date){
        if(daoSession == null){
            return null;
        }
        return daoSession.getSignDao().queryBuilder().where(SignDao.Properties.Comid.eq(comId),SignDao.Properties.Date.eq(date)).list();
    }

    /***
     * 查询所有未上传的数据
     * @param isUp
     * @return
     */
    public List<Sign> querySignByUpload(boolean isUp){
        if(daoSession == null){
            return null;
        }
        return daoSession.getSignDao().queryBuilder().where(SignDao.Properties.IsUpload.eq(isUp)).list();
    }

    /***
     * 通过faceId查询员工
     * @param id
     * @return
     */
    public User queryUserByFaceId(long id){
        if(daoSession == null){
            return null;
        }
        return daoSession.getUserDao().queryBuilder().where(UserDao.Properties.FaceId.eq(id)).unique();
    }

    /***
     * 通过ID查询员工
     * @param id
     * @return
     */
    public User queryUserById(long id){
        if(daoSession == null){
            return null;
        }
        return daoSession.getUserDao().queryBuilder().where(UserDao.Properties.Id.eq(id)).unique();
    }

    /***
     * 查询某公司下某部门的所有员工
     * @param compId
     * @param depId
     * @return
     */
    public List<User> queryUserByCompIdAndDepId(int compId,long depId){
        if(daoSession == null){
            return null;
        }
        return daoSession.getUserDao().queryBuilder().where(UserDao.Properties.CompanyId.eq(compId),UserDao.Properties.DepartId.eq(depId)).list();
    }

    public List<Depart> queryDepartByCompId(int compId){
        if(daoSession == null){
            return null;
        }
        return daoSession.getDepartDao().queryBuilder().where(DepartDao.Properties.CompId.eq(compId)).list();
    }

    /***
     * 通过部门Id查询员工
     * @param id
     * @return
     */
    public List<User> queryUserByDepId(long id){
        if(daoSession == null){
            return null;
        }
        return daoSession.getUserDao().queryBuilder().where(UserDao.Properties.DepartId.eq(id)).list();
    }
}
