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
 * DAO for table "COMPANY".
*/
public class CompanyDao extends AbstractDao<Company, Long> {

    public static final String TABLENAME = "COMPANY";

    /**
     * Properties of entity Company.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Comid = new Property(1, int.class, "comid", false, "COMID");
        public final static Property DepId = new Property(2, int.class, "depId", false, "DEP_ID");
        public final static Property BindType = new Property(3, int.class, "bindType", false, "BIND_TYPE");
        public final static Property Slogan = new Property(4, String.class, "slogan", false, "SLOGAN");
        public final static Property Comname = new Property(5, String.class, "comname", false, "COMNAME");
        public final static Property Abbname = new Property(6, String.class, "abbname", false, "ABBNAME");
        public final static Property Downtime = new Property(7, String.class, "downtime", false, "DOWNTIME");
        public final static Property DevicePwd = new Property(8, String.class, "devicePwd", false, "DEVICE_PWD");
        public final static Property Gotime = new Property(9, String.class, "gotime", false, "GOTIME");
        public final static Property Gotips = new Property(10, String.class, "gotips", false, "GOTIPS");
        public final static Property Comlogo = new Property(11, String.class, "comlogo", false, "COMLOGO");
        public final static Property CodeUrl = new Property(12, String.class, "codeUrl", false, "CODE_URL");
        public final static Property Themeid = new Property(13, int.class, "themeid", false, "THEMEID");
        public final static Property Toptitle = new Property(14, String.class, "toptitle", false, "TOPTITLE");
        public final static Property Downtips = new Property(15, String.class, "downtips", false, "DOWNTIPS");
        public final static Property Bottomtitle = new Property(16, String.class, "bottomtitle", false, "BOTTOMTITLE");
        public final static Property Notice = new Property(17, String.class, "notice", false, "NOTICE");
        public final static Property DisplayPosition = new Property(18, int.class, "displayPosition", false, "DISPLAY_POSITION");
    }


    public CompanyDao(DaoConfig config) {
        super(config);
    }
    
    public CompanyDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"COMPANY\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"COMID\" INTEGER NOT NULL ," + // 1: comid
                "\"DEP_ID\" INTEGER NOT NULL ," + // 2: depId
                "\"BIND_TYPE\" INTEGER NOT NULL ," + // 3: bindType
                "\"SLOGAN\" TEXT," + // 4: slogan
                "\"COMNAME\" TEXT," + // 5: comname
                "\"ABBNAME\" TEXT," + // 6: abbname
                "\"DOWNTIME\" TEXT," + // 7: downtime
                "\"DEVICE_PWD\" TEXT," + // 8: devicePwd
                "\"GOTIME\" TEXT," + // 9: gotime
                "\"GOTIPS\" TEXT," + // 10: gotips
                "\"COMLOGO\" TEXT," + // 11: comlogo
                "\"CODE_URL\" TEXT," + // 12: codeUrl
                "\"THEMEID\" INTEGER NOT NULL ," + // 13: themeid
                "\"TOPTITLE\" TEXT," + // 14: toptitle
                "\"DOWNTIPS\" TEXT," + // 15: downtips
                "\"BOTTOMTITLE\" TEXT," + // 16: bottomtitle
                "\"NOTICE\" TEXT," + // 17: notice
                "\"DISPLAY_POSITION\" INTEGER NOT NULL );"); // 18: displayPosition
        // Add Indexes
        db.execSQL("CREATE UNIQUE INDEX " + constraint + "IDX_COMPANY_COMID_DESC_DEP_ID_DESC ON \"COMPANY\"" +
                " (\"COMID\" DESC,\"DEP_ID\" DESC);");
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"COMPANY\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Company entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getComid());
        stmt.bindLong(3, entity.getDepId());
        stmt.bindLong(4, entity.getBindType());
 
        String slogan = entity.getSlogan();
        if (slogan != null) {
            stmt.bindString(5, slogan);
        }
 
        String comname = entity.getComname();
        if (comname != null) {
            stmt.bindString(6, comname);
        }
 
        String abbname = entity.getAbbname();
        if (abbname != null) {
            stmt.bindString(7, abbname);
        }
 
        String downtime = entity.getDowntime();
        if (downtime != null) {
            stmt.bindString(8, downtime);
        }
 
        String devicePwd = entity.getDevicePwd();
        if (devicePwd != null) {
            stmt.bindString(9, devicePwd);
        }
 
        String gotime = entity.getGotime();
        if (gotime != null) {
            stmt.bindString(10, gotime);
        }
 
        String gotips = entity.getGotips();
        if (gotips != null) {
            stmt.bindString(11, gotips);
        }
 
        String comlogo = entity.getComlogo();
        if (comlogo != null) {
            stmt.bindString(12, comlogo);
        }
 
        String codeUrl = entity.getCodeUrl();
        if (codeUrl != null) {
            stmt.bindString(13, codeUrl);
        }
        stmt.bindLong(14, entity.getThemeid());
 
        String toptitle = entity.getToptitle();
        if (toptitle != null) {
            stmt.bindString(15, toptitle);
        }
 
        String downtips = entity.getDowntips();
        if (downtips != null) {
            stmt.bindString(16, downtips);
        }
 
        String bottomtitle = entity.getBottomtitle();
        if (bottomtitle != null) {
            stmt.bindString(17, bottomtitle);
        }
 
        String notice = entity.getNotice();
        if (notice != null) {
            stmt.bindString(18, notice);
        }
        stmt.bindLong(19, entity.getDisplayPosition());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Company entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getComid());
        stmt.bindLong(3, entity.getDepId());
        stmt.bindLong(4, entity.getBindType());
 
        String slogan = entity.getSlogan();
        if (slogan != null) {
            stmt.bindString(5, slogan);
        }
 
        String comname = entity.getComname();
        if (comname != null) {
            stmt.bindString(6, comname);
        }
 
        String abbname = entity.getAbbname();
        if (abbname != null) {
            stmt.bindString(7, abbname);
        }
 
        String downtime = entity.getDowntime();
        if (downtime != null) {
            stmt.bindString(8, downtime);
        }
 
        String devicePwd = entity.getDevicePwd();
        if (devicePwd != null) {
            stmt.bindString(9, devicePwd);
        }
 
        String gotime = entity.getGotime();
        if (gotime != null) {
            stmt.bindString(10, gotime);
        }
 
        String gotips = entity.getGotips();
        if (gotips != null) {
            stmt.bindString(11, gotips);
        }
 
        String comlogo = entity.getComlogo();
        if (comlogo != null) {
            stmt.bindString(12, comlogo);
        }
 
        String codeUrl = entity.getCodeUrl();
        if (codeUrl != null) {
            stmt.bindString(13, codeUrl);
        }
        stmt.bindLong(14, entity.getThemeid());
 
        String toptitle = entity.getToptitle();
        if (toptitle != null) {
            stmt.bindString(15, toptitle);
        }
 
        String downtips = entity.getDowntips();
        if (downtips != null) {
            stmt.bindString(16, downtips);
        }
 
        String bottomtitle = entity.getBottomtitle();
        if (bottomtitle != null) {
            stmt.bindString(17, bottomtitle);
        }
 
        String notice = entity.getNotice();
        if (notice != null) {
            stmt.bindString(18, notice);
        }
        stmt.bindLong(19, entity.getDisplayPosition());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public Company readEntity(Cursor cursor, int offset) {
        Company entity = new Company( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // comid
            cursor.getInt(offset + 2), // depId
            cursor.getInt(offset + 3), // bindType
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // slogan
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // comname
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // abbname
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // downtime
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // devicePwd
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // gotime
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // gotips
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // comlogo
            cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12), // codeUrl
            cursor.getInt(offset + 13), // themeid
            cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14), // toptitle
            cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15), // downtips
            cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16), // bottomtitle
            cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17), // notice
            cursor.getInt(offset + 18) // displayPosition
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Company entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setComid(cursor.getInt(offset + 1));
        entity.setDepId(cursor.getInt(offset + 2));
        entity.setBindType(cursor.getInt(offset + 3));
        entity.setSlogan(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setComname(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setAbbname(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setDowntime(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setDevicePwd(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setGotime(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setGotips(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setComlogo(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setCodeUrl(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setThemeid(cursor.getInt(offset + 13));
        entity.setToptitle(cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14));
        entity.setDowntips(cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15));
        entity.setBottomtitle(cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16));
        entity.setNotice(cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17));
        entity.setDisplayPosition(cursor.getInt(offset + 18));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Company entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Company entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Company entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}