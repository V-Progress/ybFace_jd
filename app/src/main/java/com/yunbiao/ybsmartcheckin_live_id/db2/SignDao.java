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
 * DAO for table "SIGN".
*/
public class SignDao extends AbstractDao<Sign, Long> {

    public static final String TABLENAME = "SIGN";

    /**
     * Properties of entity Sign.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property FaceId = new Property(1, String.class, "faceId", false, "FACE_ID");
        public final static Property Name = new Property(2, String.class, "name", false, "NAME");
        public final static Property Position = new Property(3, String.class, "position", false, "POSITION");
        public final static Property Time = new Property(4, long.class, "time", false, "TIME");
        public final static Property IsUpload = new Property(5, boolean.class, "isUpload", false, "IS_UPLOAD");
        public final static Property EmpId = new Property(6, long.class, "empId", false, "EMP_ID");
        public final static Property Date = new Property(7, String.class, "date", false, "DATE");
        public final static Property Depart = new Property(8, String.class, "depart", false, "DEPART");
        public final static Property Sex = new Property(9, int.class, "sex", false, "SEX");
        public final static Property Age = new Property(10, int.class, "age", false, "AGE");
        public final static Property EmployNum = new Property(11, String.class, "employNum", false, "EMPLOY_NUM");
        public final static Property Birthday = new Property(12, String.class, "birthday", false, "BIRTHDAY");
        public final static Property Autograph = new Property(13, String.class, "autograph", false, "AUTOGRAPH");
        public final static Property Comid = new Property(14, int.class, "comid", false, "COMID");
        public final static Property Type = new Property(15, int.class, "type", false, "TYPE");
        public final static Property VisEntryId = new Property(16, long.class, "visEntryId", false, "VIS_ENTRY_ID");
        public final static Property Temperature = new Property(17, float.class, "temperature", false, "TEMPERATURE");
        public final static Property HeadPath = new Property(18, String.class, "headPath", false, "HEAD_PATH");
        public final static Property HotImgPath = new Property(19, String.class, "hotImgPath", false, "HOT_IMG_PATH");
    }


    public SignDao(DaoConfig config) {
        super(config);
    }
    
    public SignDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"SIGN\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"FACE_ID\" TEXT," + // 1: faceId
                "\"NAME\" TEXT," + // 2: name
                "\"POSITION\" TEXT," + // 3: position
                "\"TIME\" INTEGER NOT NULL ," + // 4: time
                "\"IS_UPLOAD\" INTEGER NOT NULL ," + // 5: isUpload
                "\"EMP_ID\" INTEGER NOT NULL ," + // 6: empId
                "\"DATE\" TEXT," + // 7: date
                "\"DEPART\" TEXT," + // 8: depart
                "\"SEX\" INTEGER NOT NULL ," + // 9: sex
                "\"AGE\" INTEGER NOT NULL ," + // 10: age
                "\"EMPLOY_NUM\" TEXT," + // 11: employNum
                "\"BIRTHDAY\" TEXT," + // 12: birthday
                "\"AUTOGRAPH\" TEXT," + // 13: autograph
                "\"COMID\" INTEGER NOT NULL ," + // 14: comid
                "\"TYPE\" INTEGER NOT NULL ," + // 15: type
                "\"VIS_ENTRY_ID\" INTEGER NOT NULL ," + // 16: visEntryId
                "\"TEMPERATURE\" REAL NOT NULL ," + // 17: temperature
                "\"HEAD_PATH\" TEXT," + // 18: headPath
                "\"HOT_IMG_PATH\" TEXT);"); // 19: hotImgPath
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"SIGN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Sign entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String faceId = entity.getFaceId();
        if (faceId != null) {
            stmt.bindString(2, faceId);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(3, name);
        }
 
        String position = entity.getPosition();
        if (position != null) {
            stmt.bindString(4, position);
        }
        stmt.bindLong(5, entity.getTime());
        stmt.bindLong(6, entity.getIsUpload() ? 1L: 0L);
        stmt.bindLong(7, entity.getEmpId());
 
        String date = entity.getDate();
        if (date != null) {
            stmt.bindString(8, date);
        }
 
        String depart = entity.getDepart();
        if (depart != null) {
            stmt.bindString(9, depart);
        }
        stmt.bindLong(10, entity.getSex());
        stmt.bindLong(11, entity.getAge());
 
        String employNum = entity.getEmployNum();
        if (employNum != null) {
            stmt.bindString(12, employNum);
        }
 
        String birthday = entity.getBirthday();
        if (birthday != null) {
            stmt.bindString(13, birthday);
        }
 
        String autograph = entity.getAutograph();
        if (autograph != null) {
            stmt.bindString(14, autograph);
        }
        stmt.bindLong(15, entity.getComid());
        stmt.bindLong(16, entity.getType());
        stmt.bindLong(17, entity.getVisEntryId());
        stmt.bindDouble(18, entity.getTemperature());
 
        String headPath = entity.getHeadPath();
        if (headPath != null) {
            stmt.bindString(19, headPath);
        }
 
        String hotImgPath = entity.getHotImgPath();
        if (hotImgPath != null) {
            stmt.bindString(20, hotImgPath);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Sign entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String faceId = entity.getFaceId();
        if (faceId != null) {
            stmt.bindString(2, faceId);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(3, name);
        }
 
        String position = entity.getPosition();
        if (position != null) {
            stmt.bindString(4, position);
        }
        stmt.bindLong(5, entity.getTime());
        stmt.bindLong(6, entity.getIsUpload() ? 1L: 0L);
        stmt.bindLong(7, entity.getEmpId());
 
        String date = entity.getDate();
        if (date != null) {
            stmt.bindString(8, date);
        }
 
        String depart = entity.getDepart();
        if (depart != null) {
            stmt.bindString(9, depart);
        }
        stmt.bindLong(10, entity.getSex());
        stmt.bindLong(11, entity.getAge());
 
        String employNum = entity.getEmployNum();
        if (employNum != null) {
            stmt.bindString(12, employNum);
        }
 
        String birthday = entity.getBirthday();
        if (birthday != null) {
            stmt.bindString(13, birthday);
        }
 
        String autograph = entity.getAutograph();
        if (autograph != null) {
            stmt.bindString(14, autograph);
        }
        stmt.bindLong(15, entity.getComid());
        stmt.bindLong(16, entity.getType());
        stmt.bindLong(17, entity.getVisEntryId());
        stmt.bindDouble(18, entity.getTemperature());
 
        String headPath = entity.getHeadPath();
        if (headPath != null) {
            stmt.bindString(19, headPath);
        }
 
        String hotImgPath = entity.getHotImgPath();
        if (hotImgPath != null) {
            stmt.bindString(20, hotImgPath);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public Sign readEntity(Cursor cursor, int offset) {
        Sign entity = new Sign( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // faceId
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // name
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // position
            cursor.getLong(offset + 4), // time
            cursor.getShort(offset + 5) != 0, // isUpload
            cursor.getLong(offset + 6), // empId
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // date
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // depart
            cursor.getInt(offset + 9), // sex
            cursor.getInt(offset + 10), // age
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // employNum
            cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12), // birthday
            cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13), // autograph
            cursor.getInt(offset + 14), // comid
            cursor.getInt(offset + 15), // type
            cursor.getLong(offset + 16), // visEntryId
            cursor.getFloat(offset + 17), // temperature
            cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18), // headPath
            cursor.isNull(offset + 19) ? null : cursor.getString(offset + 19) // hotImgPath
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Sign entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setFaceId(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setPosition(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setTime(cursor.getLong(offset + 4));
        entity.setIsUpload(cursor.getShort(offset + 5) != 0);
        entity.setEmpId(cursor.getLong(offset + 6));
        entity.setDate(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setDepart(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setSex(cursor.getInt(offset + 9));
        entity.setAge(cursor.getInt(offset + 10));
        entity.setEmployNum(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setBirthday(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setAutograph(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
        entity.setComid(cursor.getInt(offset + 14));
        entity.setType(cursor.getInt(offset + 15));
        entity.setVisEntryId(cursor.getLong(offset + 16));
        entity.setTemperature(cursor.getFloat(offset + 17));
        entity.setHeadPath(cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18));
        entity.setHotImgPath(cursor.isNull(offset + 19) ? null : cursor.getString(offset + 19));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Sign entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Sign entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Sign entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
