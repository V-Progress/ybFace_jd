package com.yunbiao.ybsmartcheckin_live_id.db2;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DEPART".
*/
public class DepartDao extends AbstractDao<Depart, Long> {

    public static final String TABLENAME = "DEPART";

    /**
     * Properties of entity Depart.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, long.class, "id", true, "_id");
        public final static Property DepId = new Property(1, long.class, "depId", false, "DEP_ID");
        public final static Property DepName = new Property(2, String.class, "depName", false, "DEP_NAME");
        public final static Property CompId = new Property(3, int.class, "compId", false, "COMP_ID");
    }


    public DepartDao(DaoConfig config) {
        super(config);
    }
    
    public DepartDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DEPART\" (" + //
                "\"_id\" INTEGER PRIMARY KEY NOT NULL ," + // 0: id
                "\"DEP_ID\" INTEGER NOT NULL UNIQUE ," + // 1: depId
                "\"DEP_NAME\" TEXT," + // 2: depName
                "\"COMP_ID\" INTEGER NOT NULL );"); // 3: compId
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DEPART\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Depart entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
        stmt.bindLong(2, entity.getDepId());
 
        String depName = entity.getDepName();
        if (depName != null) {
            stmt.bindString(3, depName);
        }
        stmt.bindLong(4, entity.getCompId());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Depart entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
        stmt.bindLong(2, entity.getDepId());
 
        String depName = entity.getDepName();
        if (depName != null) {
            stmt.bindString(3, depName);
        }
        stmt.bindLong(4, entity.getCompId());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.getLong(offset + 0);
    }    

    @Override
    public Depart readEntity(Cursor cursor, int offset) {
        Depart entity = new Depart( //
            cursor.getLong(offset + 0), // id
            cursor.getLong(offset + 1), // depId
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // depName
            cursor.getInt(offset + 3) // compId
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Depart entity, int offset) {
        entity.setId(cursor.getLong(offset + 0));
        entity.setDepId(cursor.getLong(offset + 1));
        entity.setDepName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setCompId(cursor.getInt(offset + 3));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Depart entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Depart entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Depart entity) {
        throw new UnsupportedOperationException("Unsupported for entities with a non-null key");
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}