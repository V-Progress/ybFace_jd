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

    public static DaoManager get() {
        return daoManager;
    }

    private DaoManager() {
    }

    public void initDb(Object uniqueId) {
        initDb("db_" + uniqueId);
    }

    public void initDb() {
        initDb(DB_NAME);
    }


    public void initDb(String name) {
        Log.e(TAG, "initDb: ");
        MySQLiteHelper helper = new MySQLiteHelper(APP.getContext(), name, null);
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
        daoSession.getVisitorDao().detachAll();
        daoSession.getMultiTotalDao().detachAll();
        daoSession.getWhiteDao().detachAll();
        daoSession.getVertifyRecordDao().detachAll();
        daoSession.getCertificatesUserDao().detachAll();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public DaoMaster getDaoMaster() {
        return daoMaster;
    }

    public <T> long add(T clazz) {
        if (daoSession == null) {
            return FAILURE;
        }
        return daoSession.insert(clazz);
    }

    public White queryWhiteByTopSixNum(String num1, String num2, String num3) {
        if (daoSession == null) {
            return null;
        }
        return daoSession.getWhiteDao().queryBuilder().whereOr(WhiteDao.Properties.Num.eq(num1), WhiteDao.Properties.Num.eq(num1 + num2), WhiteDao.Properties.Num.eq(num1 + num2 + num3)).unique();

    }

    public <T> long addOrUpdate(T clazz) {
        if (daoSession == null) {
            return FAILURE;
        }
        return daoSession.insertOrReplace(clazz);
    }

    public <T> long update(T t) {
        if (daoSession == null) {
            return FAILURE;
        }
        daoSession.update(t);
        return SUCCESS;
    }

    public <T> List<T> queryAll(Class<T> clazz) {
        if (daoSession == null) {
            return null;
        }
        return daoSession.loadAll(clazz);
    }

    public boolean deleteAll(Class clazz) {
        if (daoSession == null) {
            return false;
        }
        daoSession.deleteAll(clazz);
        return true;
    }

    public <T> long delete(T t) {
        if (daoSession == null) {
            return FAILURE;
        }
        daoSession.delete(t);
        return SUCCESS;
    }

    public void deleteSign(Sign sign) {
        if (daoSession == null) {
            return;
        }
        daoSession.getSignDao().delete(sign);
    }

    /***
     * 查询某公司下某天的所有打卡记录
     * @param comId
     * @param date
     * @return
     */
    public List<Sign> querySignByComIdAndDate(int comId, String date) {
        if (daoSession == null) {
            return null;
        }
        return daoSession.getSignDao().queryBuilder().where(SignDao.Properties.Comid.eq(comId), SignDao.Properties.Date.eq(date)).list();
    }

    /***
     * 查询某公司下某天的所有打卡记录
     * @param comId
     * @return
     */
    public List<Sign> querySignByComId(int comId) {
        if (daoSession == null) {
            return null;
        }
        return daoSession.getSignDao().queryBuilder().where(SignDao.Properties.Comid.eq(comId)).list();
    }

    public List<Sign> querySignByComIdAndUpload(int comId, boolean isUpload) {
        if (daoSession == null) {
            return null;
        }
        return daoSession.getSignDao().queryBuilder().where(SignDao.Properties.Comid.eq(comId), SignDao.Properties.IsUpload.eq(isUpload)).list();
    }

    public List<Sign> querySignByComIdAndDateWithLimit(int comid, String date, int limit) {
        if (daoSession == null) {
            return null;
        }
        return daoSession.getSignDao().queryBuilder().where(SignDao.Properties.Comid.eq(comid), SignDao.Properties.Date.eq(date)).limit(limit).list();
    }

    /***
     * 查询所有未上传的数据
     * @param isUp
     * @return
     */
    public List<Sign> querySignByUpload(boolean isUp) {
        if (daoSession == null) {
            return null;
        }
        return daoSession.getSignDao().queryBuilder().where(SignDao.Properties.IsUpload.eq(isUp)).list();
    }

    public Sign querySignByTime(long time) {
        if (daoSession == null) {
            return null;
        }
        return daoSession.getSignDao().queryBuilder().where(SignDao.Properties.Time.eq(time)).unique();
    }

    /***
     * 通过faceId查询员工
     * @param faceId
     * @return
     */
    public User queryUserByFaceId(String faceId) {
        if (daoSession == null) {
            return null;
        }
        return daoSession.getUserDao().queryBuilder().where(UserDao.Properties.FaceId.eq(faceId)).unique();
    }

    public User queryUserByComIdAndFaceId(int comId, String faceId) {
        if (daoSession == null) {
            return null;
        }
        return daoSession.getUserDao().queryBuilder().where(UserDao.Properties.CompanyId.eq(comId), UserDao.Properties.FaceId.eq(faceId)).unique();
    }

    /***
     * 通过公司Id和日期查数据统计
     */
    public MultiTotal queryTotalByCompIfAndDate(long compId, String date) {
        if (daoSession == null) {
            return null;
        }
        return daoSession.getMultiTotalDao().queryBuilder().where(MultiTotalDao.Properties.CompId.eq(compId), MultiTotalDao.Properties.Date.eq(date)).unique();
    }

    /***
     * 通过ID查询员工
     * @param id
     * @return
     */
    public User queryUserById(long id) {
        if (daoSession == null) {
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
    public List<User> queryUserByCompIdAndDepId(int compId, long depId) {
        if (daoSession == null) {
            return null;
        }
        return daoSession.getUserDao().queryBuilder().where(UserDao.Properties.CompanyId.eq(compId), UserDao.Properties.DepartId.eq(depId)).list();
    }

    /***
     * 查询某公司下的所有员工
     * @param compId
     * @return
     */
    public List<User> queryUserByCompId(int compId) {
        if (daoSession == null) {
            return null;
        }
        return daoSession.getUserDao().queryBuilder().where(UserDao.Properties.CompanyId.eq(compId)).list();
    }

    public List<Depart> queryDepartByCompId(int compId) {
        if (daoSession == null) {
            return null;
        }
        return daoSession.getDepartDao().queryBuilder().where(DepartDao.Properties.CompId.eq(compId)).list();
    }

    /***
     * 通过部门Id查询员工
     * @param id
     * @return
     */
    public List<User> queryUserByDepId(long id) {
        if (daoSession == null) {
            return null;
        }
        return daoSession.getUserDao().queryBuilder().where(UserDao.Properties.DepartId.eq(id)).list();
    }

    public Visitor queryVisitorByFaceId(String userId) {
        if (daoSession == null) {
            return null;
        }
        return daoSession.getVisitorDao().queryBuilder().where(VisitorDao.Properties.FaceId.eq(userId)).unique();
    }

    public Visitor queryVisitorByComIdAndFaceId(int comId, String userId) {
        if (daoSession == null) {
            return null;
        }
        return daoSession.getVisitorDao().queryBuilder().where(VisitorDao.Properties.ComId.eq(comId), VisitorDao.Properties.FaceId.eq(userId)).unique();
    }

    public List<Visitor> queryVisitorsByCompId(int compId) {
        if (daoSession == null) {
            return null;
        }
        return daoSession.getVisitorDao().queryBuilder().where(VisitorDao.Properties.ComId.eq(compId)).list();
    }

    public User queryUserByCardId(String cardId) {
        if (daoSession == null) {
            return null;
        }
        return daoSession.getUserDao().queryBuilder().where(UserDao.Properties.CardId.eq(cardId)).unique();
    }

    public void deleteSignByTime(long time) {
        if (daoSession == null) {
            return;
        }
        daoSession.getSignDao().queryBuilder().where(SignDao.Properties.Time.eq(time)).buildDelete().executeDeleteWithoutDetachingEntities();
    }

    public List<VertifyRecord> queryVertifyRecordByDate(String date){
        if(daoSession == null){
            return null;
        }
        return daoSession.getVertifyRecordDao().queryBuilder().where(VertifyRecordDao.Properties.Date.eq(date)).list();
    }

    public CertificatesUser queryCertiUserByCardNum(String cardNum) {
        if(daoSession == null){
            return null;
        }
        return daoSession.getCertificatesUserDao().queryBuilder().where(CertificatesUserDao.Properties.Num.eq(cardNum)).unique();
    }
}
