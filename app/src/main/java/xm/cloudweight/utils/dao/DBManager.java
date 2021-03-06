package xm.cloudweight.utils.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import xm.cloudweight.comm.Common;
import xm.cloudweight.utils.dao.bean.DaoMaster;
import xm.cloudweight.utils.dao.bean.DaoSession;
import xm.cloudweight.utils.dao.bean.DbImageUpload;
import xm.cloudweight.utils.dao.bean.DbImageUploadDao;

/**
 * @author wyh
 * @Description:
 * @creat 2017/8/1
 */
public class DBManager {

    private static DBManager mInstance = null;
    private DaoMaster.DevOpenHelper mDevOpenHelper;
    private final Context mContext;
    public final static String DB_NAME = "db";

    public DBManager(Context context) {
        mContext = context;
        mDevOpenHelper = new DaoMaster.DevOpenHelper(context, DB_NAME, null);
    }

    /**
     * 获取可读数据库
     */
    private SQLiteDatabase getReadableDatabase() {
        if (mDevOpenHelper == null) {
            mDevOpenHelper = new DaoMaster.DevOpenHelper(mContext, DB_NAME, null);
        }
        return mDevOpenHelper.getReadableDatabase();
    }

    /**
     * 获取可写数据库
     */
    private SQLiteDatabase getWritableDatabase() {
        if (mDevOpenHelper == null) {
            mDevOpenHelper = new DaoMaster.DevOpenHelper(mContext, DB_NAME, null);
        }
        return mDevOpenHelper.getWritableDatabase();
    }

    /**
     * 插入上传图片DbImageUpload
     */
    public void insertDbImageUpload(DbImageUpload dbImageUpload) {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        DbImageUploadDao dao = daoSession.getDbImageUploadDao();
        dao.insert(dbImageUpload);
    }

    /**
     * 删除全部
     */
    public void deleteAll() {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        DbImageUploadDao userDao = daoSession.getDbImageUploadDao();
        userDao.deleteAll();
    }

    /**
     * 更新DbImageUpload
     */
    public void updateDbImageUpload(DbImageUpload data) {
        if (data == null) {
            return;
        }
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        DbImageUploadDao userDao = daoSession.getDbImageUploadDao();
        userDao.update(data);
    }

    /**
     * 删除一条DbImageUpload记录
     */
    public void deleteDbImageUpload(DbImageUpload data) {
        if (data == null) {
            return;
        }
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        DbImageUploadDao dao = daoSession.getDbImageUploadDao();
        dao.delete(data);
    }

    /**
     * 获取数据库中 未通过图片path获取url的列表
     */
    public List<DbImageUpload> getDbListUnUploadImage() {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        return daoSession.getDbImageUploadDao()
                .queryBuilder()
                .where(DbImageUploadDao.Properties.ImagePath.isNotNull(),
                        DbImageUploadDao.Properties.ImageUrl.isNull(),
                        DbImageUploadDao.Properties.IsRequestSuccess.eq(true))
                .list();
    }

    /**
     * 获取数据库中  分拣历史
     */
    public List<DbImageUpload> getDbListSortOutStoreOutAll(String date) {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        return daoSession.getDbImageUploadDao()
                .queryBuilder()
                .where(
                        DbImageUploadDao.Properties.Type.eq(Common.DbType.TYPE_SORT_OUT_STORE_OUT),
                        DbImageUploadDao.Properties.Line.isNotNull(),
                        DbImageUploadDao.Properties.ErrorString.isNull(),
                        DbImageUploadDao.Properties.Date.eq(date))
                .list();
    }

    /**
     * 获取数据库中  验收历史  三种情况   越库调拨，入库，越库
     */
    public List<DbImageUpload> getDbListCheckInHistory(String date) {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        return daoSession.getDbImageUploadDao()
                .queryBuilder()
                .whereOr(DbImageUploadDao.Properties.Type.eq(Common.DbType.TYPE_ChECK_IN_STORE_IN),
                        DbImageUploadDao.Properties.Type.eq(Common.DbType.TYPE_ChECK_IN_CROSS_OUT),
                        DbImageUploadDao.Properties.Type.eq(Common.DbType.TYPE_ChECK_IN_CROSS_ALLCOCATE))
                .where(
                        DbImageUploadDao.Properties.Line.isNotNull(),
                        DbImageUploadDao.Properties.ErrorString.isNull(),
                        DbImageUploadDao.Properties.Date.eq(date))
                .list();
    }

    /**
     * 获取数据库中  出库历史
     */
    public List<DbImageUpload> getDbListSimilarStoreOutHistory() {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        return daoSession.getDbImageUploadDao()
                .queryBuilder()
                .where(
                        DbImageUploadDao.Properties.Type.eq(Common.DbType.TYPE_STORE_OUT),
                        DbImageUploadDao.Properties.Line.isNotNull(),
                        DbImageUploadDao.Properties.ErrorString.isNull())
                .list();
    }

    /**
     * 获取数据库中  调拨历史
     */
    public List<DbImageUpload> getDbListSimilarAllocateHistory() {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        return daoSession.getDbImageUploadDao()
                .queryBuilder()
                .where(
                        DbImageUploadDao.Properties.Type.eq(Common.DbType.TYPE_ALLOCATE),
                        DbImageUploadDao.Properties.Line.isNotNull(),
                        DbImageUploadDao.Properties.ErrorString.isNull())
                .list();
    }

    /**
     * 获取数据库中  调拨验收历史
     */
    public List<DbImageUpload> getDbListAllocateAcceptHistory(String date) {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        return daoSession.getDbImageUploadDao()
                .queryBuilder()
                .where(
                        DbImageUploadDao.Properties.Type.eq(Common.DbType.TYPE_ALLOCATE_ACCEPT),
                        DbImageUploadDao.Properties.Line.isNotNull(),
                        DbImageUploadDao.Properties.ErrorString.isNull(),
                        DbImageUploadDao.Properties.Date.eq(date))
                .list();
    }

    /**
     * 获取数据库中  加工入库历史
     */
    public List<DbImageUpload> getDbListProcessStorageHistory(String date) {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        return daoSession.getDbImageUploadDao()
                .queryBuilder()
                .where(
                        DbImageUploadDao.Properties.Type.eq(Common.DbType.TYPE_PROCESS_STORE_IN),
                        DbImageUploadDao.Properties.Line.isNotNull(),
                        DbImageUploadDao.Properties.ErrorString.isNull(),
                        DbImageUploadDao.Properties.Date.eq(date))
                .list();
    }

    /**
     * 验收  入库
     */
    public List<DbImageUpload> getDbListCheckInStoreIn() {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        return daoSession.getDbImageUploadDao()
                .queryBuilder()
                .where(
                        DbImageUploadDao.Properties.Type.eq(Common.DbType.TYPE_ChECK_IN_STORE_IN),
                        DbImageUploadDao.Properties.Line.isNotNull(),
                        DbImageUploadDao.Properties.ErrorString.isNull(),
                        DbImageUploadDao.Properties.StockInUuid.isNull(),
                        DbImageUploadDao.Properties.IsRequestSuccess.eq(false))
                .list();
    }

    /**
     * 验收  越库
     */
    public List<DbImageUpload> getDbListCheckInCrossOut() {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        return daoSession.getDbImageUploadDao()
                .queryBuilder()
                .where(
                        DbImageUploadDao.Properties.Type.eq(Common.DbType.TYPE_ChECK_IN_CROSS_OUT),
                        DbImageUploadDao.Properties.Line.isNotNull(),
                        DbImageUploadDao.Properties.ErrorString.isNull(),
                        DbImageUploadDao.Properties.StockOutUuid.isNull(),
                        DbImageUploadDao.Properties.IsRequestSuccess.eq(false)
                )
                .list();
    }

    /**
     * 验收  越库调拨
     */
    public List<DbImageUpload> getDbListCheckInCrossAllocate() {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        return daoSession.getDbImageUploadDao()
                .queryBuilder()
                .where(
                        DbImageUploadDao.Properties.Type.eq(Common.DbType.TYPE_ChECK_IN_CROSS_ALLCOCATE),
                        DbImageUploadDao.Properties.Line.isNotNull(),
                        DbImageUploadDao.Properties.ErrorString.isNull(),
                        DbImageUploadDao.Properties.StockInUuid.isNull(),
                        DbImageUploadDao.Properties.IsRequestSuccess.eq(false)
                )
                .list();
    }

    /**
     * 分拣
     */
    public List<DbImageUpload> getDbListSortOutStoreOut() {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        return daoSession.getDbImageUploadDao()
                .queryBuilder()
                .where(
                        DbImageUploadDao.Properties.Type.eq(Common.DbType.TYPE_SORT_OUT_STORE_OUT),
                        DbImageUploadDao.Properties.Line.isNotNull(),
                        DbImageUploadDao.Properties.ErrorString.isNull(),
                        DbImageUploadDao.Properties.StockOutUuid.isNull(),
                        DbImageUploadDao.Properties.IsRequestSuccess.eq(false)
                )
                .list();
    }

    /**
     * 出库
     */
    public List<DbImageUpload> getDbListStoreOut() {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        return daoSession.getDbImageUploadDao()
                .queryBuilder()
                .where(
                        DbImageUploadDao.Properties.Type.eq(Common.DbType.TYPE_STORE_OUT),
                        DbImageUploadDao.Properties.Line.isNotNull(),
                        DbImageUploadDao.Properties.ErrorString.isNull(),
                        DbImageUploadDao.Properties.StockOutUuid.isNull(),
                        DbImageUploadDao.Properties.IsRequestSuccess.eq(false)
                )
                .list();
    }

    /**
     * 调拨
     */
    public List<DbImageUpload> getDbListAllocate() {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        return daoSession.getDbImageUploadDao()
                .queryBuilder()
                .where(
                        DbImageUploadDao.Properties.Type.eq(Common.DbType.TYPE_ALLOCATE),
                        DbImageUploadDao.Properties.ErrorString.isNull(),
                        DbImageUploadDao.Properties.Line.isNotNull(),
                        DbImageUploadDao.Properties.StockOutUuid.isNull(),
                        DbImageUploadDao.Properties.IsRequestSuccess.eq(false)
                )
                .list();
    }

    /**
     * 盘点
     */
    public List<DbImageUpload> getDbListCheck() {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        return daoSession.getDbImageUploadDao()
                .queryBuilder()
                .where(
                        DbImageUploadDao.Properties.Type.eq(Common.DbType.TYPE_CHECK),
                        DbImageUploadDao.Properties.ErrorString.isNull(),
                        DbImageUploadDao.Properties.Line.isNotNull(),
                        DbImageUploadDao.Properties.IsRequestSuccess.eq(false)
                )
                .list();
    }

    /**
     * 加工入库
     */
    public List<DbImageUpload> getDbListProcessStoreIn() {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        return daoSession.getDbImageUploadDao()
                .queryBuilder()
                .where(
                        DbImageUploadDao.Properties.Type.eq(Common.DbType.TYPE_PROCESS_STORE_IN),
                        DbImageUploadDao.Properties.ErrorString.isNull(),
                        DbImageUploadDao.Properties.Line.isNotNull(),
                        DbImageUploadDao.Properties.IsRequestSuccess.eq(false)
                )
                .list();
    }

    /**
     * 调拨验收
     */
    public List<DbImageUpload> getDbListAllocateAccept() {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        return daoSession.getDbImageUploadDao()
                .queryBuilder()
                .where(
                        DbImageUploadDao.Properties.Type.eq(Common.DbType.TYPE_ALLOCATE_ACCEPT),
                        DbImageUploadDao.Properties.ErrorString.isNull(),
                        DbImageUploadDao.Properties.Line.isNotNull(),
                        DbImageUploadDao.Properties.IsRequestSuccess.eq(false)
                )
                .list();
    }
}
