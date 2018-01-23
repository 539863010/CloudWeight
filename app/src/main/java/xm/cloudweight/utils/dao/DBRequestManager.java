package xm.cloudweight.utils.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import xm.cloudweight.utils.dao.bean.DaoMaster;
import xm.cloudweight.utils.dao.bean.DaoSession;
import xm.cloudweight.utils.dao.bean.DbRequestData;
import xm.cloudweight.utils.dao.bean.DbRequestDataDao;

/**
 * @author wyh
 * @Description:
 * @creat 2017/8/1
 */
public class DBRequestManager {

    private static DBRequestManager mInstance = null;
    private DaoMaster.DevOpenHelper mDevOpenHelper;
    private final Context mContext;
    public final static String DB_NAME_REQUEST_DATA = "db_name_request_data";

    public DBRequestManager(Context context) {
        mContext = context;
        mDevOpenHelper = new DaoMaster.DevOpenHelper(context, DB_NAME_REQUEST_DATA, null);
    }

    /**
     * 获取可读数据库
     */
    private SQLiteDatabase getReadableDatabase() {
        if (mDevOpenHelper == null) {
            mDevOpenHelper = new DaoMaster.DevOpenHelper(mContext, DB_NAME_REQUEST_DATA, null);
        }
        return mDevOpenHelper.getReadableDatabase();
    }

    /**
     * 获取可写数据库
     */
    private SQLiteDatabase getWritableDatabase() {
        if (mDevOpenHelper == null) {
            mDevOpenHelper = new DaoMaster.DevOpenHelper(mContext, DB_NAME_REQUEST_DATA, null);
        }
        return mDevOpenHelper.getWritableDatabase();
    }

    /**
     * 插入上传图片DbImageUpload
     */
    public void insertOrReplace(DbRequestData photo) {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        DbRequestDataDao dao = daoSession.getDbRequestDataDao();
        dao.insertOrReplace(photo);
    }

    /**
     * 获取数据库中 未通过图片path获取url的列表
     */
    public List<DbRequestData> getDbRequestData(long type) {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        return daoSession.getDbRequestDataDao()
                .queryBuilder()
                .where(DbRequestDataDao.Properties.Id.eq(type))
                .list();
    }

}
