package com.yunbiao.ybsmartcheckin_live_id.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/10/8.
 */

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    public static final String DATABASE_PATH = Constants.DATA_PATH;
    // 数据库名称
    public static final String DATABASE_NAME = "faceDB.db";
    // 本类的单例实例
    private static DatabaseHelper instance;

    // 存储APP中所有的DAO对象的Map集合
    private Map<String, Dao> daos = new HashMap<>();

    // 获取本类单例对象的方法
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null) {
                    instance = new DatabaseHelper(context);
                }
            }
        }
        return instance;
    }

    // 私有的构造方法
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 3);
    }

    /**
     * 创建数据库
     * @param context
     */
    public static void createDatabase(Context context){
        File f = new File(DATABASE_PATH);
        if(!f.exists() || (!f.isDirectory())){
            f.mkdirs();
        }
        f = new File(f,DATABASE_NAME);
        if (f.exists()) {
            if(f.isDirectory()){
                f.delete();
            } else {
                return;
            }
        }
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(
                f.getPath(),null);
        DatabaseHelper orm = new DatabaseHelper(context);
        orm.onCreate(db);
        db.close();
    }

    //修改读写数据库的方法
    @Override
    public SQLiteDatabase getWritableDatabase() {
        return SQLiteDatabase.openDatabase(DATABASE_PATH + DATABASE_NAME, null,
                SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        return SQLiteDatabase.openDatabase(DATABASE_PATH + DATABASE_NAME, null,
                SQLiteDatabase.OPEN_READONLY);
    }

    // 根据传入的DAO的路径获取到这个DAO的单例对象（要么从daos这个Map中获取，要么新创建一个并存入daos）
    public synchronized Dao getDao(Class clazz) throws SQLException {
        Dao dao = null;
        String className = clazz.getSimpleName();
        if (daos.containsKey(className)) {
            dao = daos.get(className);
        }
        if (dao == null) {
            dao = super.getDao(clazz);
            daos.put(className, dao);
        }
        return dao;
    }

    @Override // 创建数据库时调用的方法
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, VIPDetail.class);
            TableUtils.createTable(connectionSource, DepartBean.class);
            TableUtils.createTable(connectionSource, SignBean.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override // 数据库版本更新时调用的方法
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, VIPDetail.class, true);
            TableUtils.dropTable(connectionSource, DepartBean.class, true);
            TableUtils.dropTable(connectionSource, SignBean.class, true);
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 释放资源
    @Override
    public void close() {
        super.close();
        for (String key : daos.keySet()) {
            Dao dao = daos.get(key);
            dao = null;
        }
    }
}
