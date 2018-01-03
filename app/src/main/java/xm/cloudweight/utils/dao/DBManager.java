package xm.cloudweight.utils.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
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
    private final static String dbName = "db";

    private DBManager(Context context) {
        mContext = context;
        mDevOpenHelper = new DaoMaster.DevOpenHelper(context, dbName, null);
    }

    public static DBManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (DBManager.class) {
                if (mInstance == null) {
                    mInstance = new DBManager(context);
                }
            }
        }
        return mInstance;
    }

    /**
     * 获取可读数据库
     */
    private SQLiteDatabase getReadableDatabase() {
        if (mDevOpenHelper == null) {
            mDevOpenHelper = new DaoMaster.DevOpenHelper(mContext, dbName, null);
        }
        return mDevOpenHelper.getReadableDatabase();
    }

    /**
     * 获取可写数据库
     */
    private SQLiteDatabase getWritableDatabase() {
        if (mDevOpenHelper == null) {
            mDevOpenHelper = new DaoMaster.DevOpenHelper(mContext, dbName, null);
        }
        return mDevOpenHelper.getWritableDatabase();
    }

    /**
     * 插入上传图片DbImageUpload
     */
    public void insertDbImageUpload(DbImageUpload photo) {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        DbImageUploadDao dao = daoSession.getDbImageUploadDao();
        dao.insert(photo);
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
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        DbImageUploadDao userDao = daoSession.getDbImageUploadDao();
        userDao.update(data);
    }

    /**
     * 删除一条DbImageUpload记录
     */
    public void deleteDbImageUpload(DbImageUpload data) {
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
                        DbImageUploadDao.Properties.ImageUrl.isNull())
                .list();
    }

    /**
     * 获取数据库中  未成功 保存图片 的列表
     * //url不为空且（StockInUuid 或 StockOutUuid）不为空，请求保存图片接口
     */
    public List<DbImageUpload> getDbListUnSaveImage() {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        List<DbImageUpload> list = daoSession.getDbImageUploadDao()
                .queryBuilder()
                .where(DbImageUploadDao.Properties.ImageUrl.isNotNull())
                .whereOr(DbImageUploadDao.Properties.StockInUuid.isNotNull(),
                        DbImageUploadDao.Properties.StockOutUuid.isNotNull())
                .list();
        //已分拣成功的没有删除本地（用于本历史显示）， 以下代码为过滤掉上传分拣接口成功的数据
        List<DbImageUpload> newList = new ArrayList<>();
        for (DbImageUpload dbImageUpload : list) {
            if (!(dbImageUpload.getType() == Common.DbType.TYPE_SORT_OUT_STORE_OUT
                    && dbImageUpload.getIsUploadStockOutImage())) {
                newList.add(dbImageUpload);
            }
        }
        return newList;
    }

    /**
     * 获取数据库中  分拣历史
     */
    public List<DbImageUpload> getDbListSortOutStoreOutHistory(String date) {
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
     * 获取数据库中  未成功上传的 验收  入库  的列表
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
                        DbImageUploadDao.Properties.StockInUuid.isNull())
                .list();
    }

    /**
     * 获取数据库中  未成功上传的 验收  越库  的列表
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
                        DbImageUploadDao.Properties.StockOutUuid.isNull()
                )
                .list();
    }

    /**
     * 获取数据库中  未成功上传的 验收  越库调拨  的列表
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
                        DbImageUploadDao.Properties.StockInUuid.isNull()
                )
                .list();
    }

    /**
     * 获取数据库中  未成功上传的 分拣  的列表
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
                        DbImageUploadDao.Properties.StockOutUuid.isNull()
                )
                .list();
    }

    /**
     * 获取数据库中  未成功上传的 出库  的列表
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
                        DbImageUploadDao.Properties.StockOutUuid.isNull()
                )
                .list();
    }

    /**
     * 获取数据库中  未成功上传的 调拨  的列表 （接口请求成功后更新db数据）
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
                        DbImageUploadDao.Properties.StockOutUuid.isNull()
                )
                .list();
    }

    /**
     * 获取数据库中  未成功上传的 盘点  的列表
     */
    public List<DbImageUpload> getDbListCheck() {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        return daoSession.getDbImageUploadDao()
                .queryBuilder()
                .where(
                        DbImageUploadDao.Properties.Type.eq(Common.DbType.TYPE_CHECK),
                        DbImageUploadDao.Properties.ErrorString.isNull(),
                        DbImageUploadDao.Properties.Line.isNotNull()
                )
                .list();
    }
}
