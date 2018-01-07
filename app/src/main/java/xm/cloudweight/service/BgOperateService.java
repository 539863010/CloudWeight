package xm.cloudweight.service;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.xmzynt.storm.service.user.merchant.Merchant;
import com.xmzynt.storm.service.wms.allocate.AllocateRecord;
import com.xmzynt.storm.service.wms.inventory.InventoryRecord;
import com.xmzynt.storm.service.wms.stockin.StockInRecord;
import com.xmzynt.storm.service.wms.stockout.StockOutRecord;
import com.xmzynt.storm.util.GsonUtil;
import com.xmzynt.storm.util.IMGType;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import xm.cloudweight.api.ApiManager;
import xm.cloudweight.api.ApiSubscribe;
import xm.cloudweight.api.ResponseEntity;
import xm.cloudweight.api.TransformerHelper;
import xm.cloudweight.app.App;
import xm.cloudweight.bean.CustomSortOutData;
import xm.cloudweight.bean.PBaseInfo;
import xm.cloudweight.comm.BrocastFilter;
import xm.cloudweight.comm.Common;
import xm.cloudweight.comm.ServerConstant;
import xm.cloudweight.utils.LogUtils;
import xm.cloudweight.utils.bussiness.BeanUtil;
import xm.cloudweight.utils.bussiness.LocalSpUtil;
import xm.cloudweight.utils.bussiness.UploadPhotoUtil;
import xm.cloudweight.utils.dao.DBManager;
import xm.cloudweight.utils.dao.bean.DbImageUpload;

/**
 * @author wyh
 * @description: 用来请求保存图片url接口
 * @create 2017/11/20
 */
public class BgOperateService extends Service {

    //保存图片类型-入库
    public static final String SAVE_IMAGE_TYPE_IN = "stockInRecord";
    //保存图片类型-出库
    public static final String SAVE_IMAGE_TYPE_OUT = "stockOutRecord";

    private DBManager mDBManager;
    private ApiManager mApiManager;
    private Merchant mMerchant;
    //当前是否在上传图片
    private boolean isCurrentUploadImage;
    //当前是否在保存图片
    private boolean isCurrentSaveImage;
    //当前是否在分拣
    private boolean isCurrentSortOutStoreOut;
    //当前是否在验收-入库
    private boolean isCurrentCheckInStoreIn;
    //当前是否在验收-越库
    private boolean isCurrentCheckInCrossOut;
    //当前是否在验收-越库调拨
    private boolean isCurrentCheckInCrossAllocate;
    //当前是否在出库
    private boolean isCurrentStoreOut;
    //当前是否在调拨
    private boolean isCurrentAllocate;
    //当前是否在盘点
    private boolean isCurrentCheck;

    public BgOperateService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApiManager = App.getApiManager(this.getApplicationContext());
        mDBManager = DBManager.getInstance(getApplicationContext());
        //创建计时器定期轮询
        createTimer();
    }

    private void createTimer() {
        CountDownTimer timer = new CountDownTimer(Long.MAX_VALUE, 5000) {
            @Override
            public void onTick(long l) {
                mMerchant = LocalSpUtil.getMerchant(getApplicationContext());
                if (mMerchant != null) {
                    //根据本地path获取url
                    if (!isCurrentUploadImage) {
                        getUnUploadImageList();
                    }
                    //将url和uuid关联
                    if (!isCurrentSaveImage) {
                        getUnSaveImageList();
                    }
                    //请求验收-入库
                    if (!isCurrentCheckInStoreIn) {
                        getUnCheckInStoreInList();
                    }
                    //请求验收-越库
                    if (!isCurrentCheckInCrossOut) {
                        getUnCheckInCrossOutList();
                    }
                    //请求验收-越库调拨
                    if (!isCurrentCheckInCrossAllocate) {
                        getUnCheckInCrossAllocateList();
                    }
                    //请求分拣
                    if (!isCurrentSortOutStoreOut) {
                        getUnSortOutStoreOutList();
                    }
                    //请求出库
                    if (!isCurrentStoreOut) {
                        getUnSortOutList();
                    }
                    //请求调拨
                    if (!isCurrentAllocate) {
                        getUnAllocateList();
                    }
                    //请求盘点
                    if (!isCurrentCheck) {
                        getUnCheckList();
                    }
                    LogUtils.e("----",
                            "正在获取url = " + isCurrentUploadImage
                                    + "\n 正在保存图片 = " + isCurrentSaveImage
                                    + "\n 正在入库 = " + isCurrentCheckInStoreIn
                                    + "\n 正在越库 = " + isCurrentCheckInCrossOut
                                    + "\n 正在越库调拨 = " + isCurrentCheckInCrossAllocate
                                    + "\n 正在分拣 = " + isCurrentSortOutStoreOut
                                    + "\n 正在出库 = " + isCurrentStoreOut
                                    + "\n 正在调拨 = " + isCurrentAllocate
                                    + "\n 正在盘点 = " + isCurrentCheck
                    );
                }
            }

            @Override
            public void onFinish() {

            }
        };
        timer.start();
    }

    /**
     * 获取验收中未入库的列表
     */
    private void getUnCheckInStoreInList() {
        List<DbImageUpload> list = mDBManager.getDbListCheckInStoreIn();
        if (list != null && list.size() > 0) {
            isCurrentCheckInStoreIn = true;
            DbImageUpload data = list.get(0);
            doCheckInStoreInList(data);
        } else {
            isCurrentCheckInStoreIn = false;
        }
    }

    private void doCheckInStoreInList(final DbImageUpload data) {
        StockInRecord record = GsonUtil.getGson().fromJson(data.getLine(), StockInRecord.class);
        PBaseInfo pBaseInfo = BeanUtil.stockIn(mMerchant, record);
        mApiManager.stockIn(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<String>>().get())
                .subscribe(new ApiSubscribe<String>() {
                    @Override
                    protected void onResult(String result) {
                        refreshImageUrl(data);
                        data.setStockInUuid(result);
                        mDBManager.updateDbImageUpload(data);
                        getUnCheckInStoreInList();
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        isCurrentCheckInStoreIn = false;
                        LogUtils.e("----------入库失败-------------", failString);
                        doResultFaild(errorType, failString, data);
                    }

                });
    }

    /**
     * 获取验收中未越库调拨列表
     */
    private void getUnCheckInCrossAllocateList() {
        List<DbImageUpload> list = mDBManager.getDbListCheckInCrossAllocate();
        if (list != null && list.size() > 0) {
            isCurrentCheckInCrossAllocate = true;
            DbImageUpload data = list.get(0);
            doCheckInCrossAllocate(data);
        } else {
            isCurrentCheckInCrossAllocate = false;
        }
    }

    /**
     * 越库调拨
     */
    private void doCheckInCrossAllocate(final DbImageUpload data) {
        StockInRecord record = GsonUtil.getGson().fromJson(data.getLine(), StockInRecord.class);
        PBaseInfo pBaseInfo = BeanUtil.crossAllocate(mMerchant, record);
        mApiManager.crossAllocate(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<String>>().get())
                .subscribe(new ApiSubscribe<String>() {
                    @Override
                    protected void onResult(String result) {
                        refreshImageUrl(data);
                        data.setStockInUuid(result);
                        mDBManager.updateDbImageUpload(data);
                        getUnCheckInCrossAllocateList();
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        isCurrentCheckInCrossAllocate = false;
                        LogUtils.e("----------越库调拨失败-------------", failString);
                        doResultFaild(errorType, failString, data);
                    }

                });
    }

    /**
     * 获取验收中未越库列表
     */
    private void getUnCheckInCrossOutList() {
        List<DbImageUpload> list = mDBManager.getDbListCheckInCrossOut();
        if (list != null && list.size() > 0) {
            isCurrentCheckInCrossOut = true;
            DbImageUpload data = list.get(0);
            doCheckInCrossOut(data);
        } else {
            isCurrentCheckInCrossOut = false;
        }
    }

    private void doCheckInCrossOut(final DbImageUpload data) {
        StockInRecord record = GsonUtil.getGson().fromJson(data.getLine(), StockInRecord.class);
        PBaseInfo pBaseInfo = BeanUtil.crossDocking(mMerchant, record);
        mApiManager.crossDocking(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<String>>().get())
                .subscribe(new ApiSubscribe<String>() {
                    @Override
                    protected void onResult(String result) {
                        refreshImageUrl(data);
                        data.setStockOutUuid(result);
                        mDBManager.updateDbImageUpload(data);
                        getUnCheckInCrossOutList();
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        isCurrentCheckInCrossOut = false;
                        LogUtils.e("----------越库失败-------------", failString);
                        doResultFaild(errorType, failString, data);
                    }

                });
    }

    /**
     * 请求未 出库的数据
     */
    private void getUnSortOutList() {
        List<DbImageUpload> list = mDBManager.getDbListStoreOut();
        if (list != null && list.size() > 0) {
            isCurrentStoreOut = true;
            DbImageUpload data = list.get(0);
            doSortOut(data);
        } else {
            isCurrentStoreOut = false;
        }
    }

    private void doSortOut(final DbImageUpload data) {
        StockOutRecord record = GsonUtil.getGson().fromJson(data.getLine(), StockOutRecord.class);
        PBaseInfo pBaseInfo = BeanUtil.stockOut(mMerchant, record);
        mApiManager.stockOut(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<String>>().get())
                .subscribe(new ApiSubscribe<String>() {
                    @Override
                    protected void onResult(String result) {
                        refreshImageUrl(data);
                        data.setStockOutUuid(result);
                        mDBManager.updateDbImageUpload(data);
                        getUnSortOutList();
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        isCurrentStoreOut = false;
                        LogUtils.e("----------调拨失败-------------", failString);
                        doResultFaild(errorType, failString, data);
                    }
                });
    }

    /**
     * 请求未 调拨的数据
     */
    private void getUnAllocateList() {
        List<DbImageUpload> list = mDBManager.getDbListAllocate();
        if (list != null && list.size() > 0) {
            isCurrentAllocate = true;
            DbImageUpload data = list.get(0);
            doAllocate(data);
        } else {
            isCurrentAllocate = false;
        }
    }

    private void doAllocate(final DbImageUpload data) {
        AllocateRecord record = GsonUtil.getGson().fromJson(data.getLine(), AllocateRecord.class);
        PBaseInfo pBaseInfo = BeanUtil.allocate(mMerchant, record);
        mApiManager.allocate(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<String>>().get())
                .subscribe(new ApiSubscribe<String>() {
                    @Override
                    protected void onResult(String result) {
                        // 需要上传图片时 添加以下代码
//                        refreshImageUrl(data);
                        data.setStockOutUuid(result);
                        //调拨成功更新数据库
                        mDBManager.updateDbImageUpload(data);
                        getUnAllocateList();
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        isCurrentAllocate = false;
                        LogUtils.e("----------调拨失败-------------", failString);
                        doResultFaild(errorType, failString, data);
                    }
                });
    }

    /**
     * 请求未 盘点的数据
     */
    private void getUnCheckList() {
        List<DbImageUpload> list = mDBManager.getDbListCheck();
        if (list != null && list.size() > 0) {
            isCurrentCheck = true;
            DbImageUpload data = list.get(0);
            doCheck(data);
        } else {
            isCurrentCheck = false;
        }
    }

    private void doCheck(final DbImageUpload data) {
        InventoryRecord inventoryRecord = GsonUtil.getGson().fromJson(data.getLine(), InventoryRecord.class);
        PBaseInfo pBaseInfo = BeanUtil.inventory(mMerchant, inventoryRecord);
        mApiManager.inventory(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<String>>().get())
                .subscribe(new ApiSubscribe<String>() {
                    @Override
                    protected void onResult(String result) {
                        //  需要上传图片时 添加以下代码
//                        refreshImageUrl(data);
                        //盘点成功删除数据库
                        mDBManager.deleteDbImageUpload(data);
                        getUnCheckList();
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        isCurrentCheck = false;
                        LogUtils.e("----------盘点失败-------------", failString);
                        doResultFaild(errorType, failString, data);
                    }
                });
    }

    /**
     * 查询数据库中未分拣的列表
     */
    private void getUnSortOutStoreOutList() {
        List<DbImageUpload> list = mDBManager.getDbListSortOutStoreOut();
        if (list != null && list.size() > 0) {
            isCurrentSortOutStoreOut = true;
            DbImageUpload data = list.get(0);
            doSortOutStoreOut(data);
        } else {
            isCurrentSortOutStoreOut = false;
        }
    }

    /**
     * 请求分拣接口
     */
    private void doSortOutStoreOut(final DbImageUpload data) {
        final CustomSortOutData sortOutData = GsonUtil.getGson().fromJson(data.getLine(), CustomSortOutData.class);
        PBaseInfo pBaseInfo = BeanUtil.sortOut(mMerchant, sortOutData);
        mApiManager.sortOut(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<List<String>>>().get())
                .subscribe(new ApiSubscribe<List<String>>() {
                    @Override
                    protected void onResult(List<String> result) {
                        refreshImageUrl(data);
                        //修改数据库信息
                        //用于删除
                        sortOutData.setStockOutRecordUuids(result);
                        //保存出库uuid
                        data.setLine(GsonUtil.getGson().toJson(sortOutData));
                        data.setStockOutUuid(result.get(0));
                        mDBManager.updateDbImageUpload(data);
                        //通知SortOutActivity更新（当popwindow打开时更新）
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(BrocastFilter.FILTER_REFRESH_SORT_OUT_HISTORY));
                        //下一个请求
                        getUnSortOutStoreOutList();
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        isCurrentSortOutStoreOut = false;
                        LogUtils.e(" -------分拣上传失败------", failString);
                        doResultFaild(errorType, failString, data);
                    }
                });
    }

    /**
     * 查询数据库中未上传图片的列表
     */
    private void getUnUploadImageList() {
        List<DbImageUpload> list = mDBManager.getDbListUnUploadImage();
        if (list != null && list.size() > 0) {
            isCurrentUploadImage = true;
            DbImageUpload dbImageUpload = list.get(0);
            doUploadImage(dbImageUpload);
        } else {
            isCurrentUploadImage = false;
        }
    }

    /**
     * 请求图片上传
     */
    private void doUploadImage(final DbImageUpload dbImageUpload) {
        String imagePath = dbImageUpload.getImagePath();
        if (TextUtils.isEmpty(imagePath) || !new File(imagePath).exists()) {
            isCurrentUploadImage = false;
            return;
        }
        String uuid = mMerchant.getUuid();
        HashMap<String, Object> map = new HashMap<>(2);
        map.put("uuid", uuid);
        String type = IMGType.GOODS_IMG.toString();
        map.put("IMG-TYPE", type);
        RequestBody requestBody =
                RequestBody.create(MediaType.parse("multipart/form-data;boundary=android"), UploadPhotoUtil.getFileByte(imagePath));
        mApiManager.uploadPhoto(map, requestBody)
                .compose(new TransformerHelper<ResponseEntity<String>>().get())
                .subscribe(new ApiSubscribe<String>() {
                    @Override
                    protected void onResult(String result) {
                        refreshUUid(dbImageUpload);
                        dbImageUpload.setImageUrl(result);
                        mDBManager.updateDbImageUpload(dbImageUpload);
                        //下一个请求
                        getUnUploadImageList();
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        isCurrentUploadImage = false;
                        LogUtils.e(" -------图片上传失败------", failString);
                    }

                });
    }

    /**
     * 查询数据库中未保存图片的列表
     */
    private void getUnSaveImageList() {
        List<DbImageUpload> list = mDBManager.getDbListUnSaveImage();
        if (list != null && list.size() > 0) {
            DbImageUpload dbImageUpload = list.get(0);
            if (new File(dbImageUpload.getImagePath()).exists()) {
                String imageUrl = dbImageUpload.getImageUrl();
                String stockInUuid = dbImageUpload.getStockInUuid();
                String stockOutUuid = dbImageUpload.getStockOutUuid();
                if (!TextUtils.isEmpty(stockInUuid) && !dbImageUpload.getIsUploadStockInImage()) {
                    isCurrentSaveImage = true;
                    doSaveImage(stockInUuid, imageUrl, SAVE_IMAGE_TYPE_IN, dbImageUpload);
                } else if (!TextUtils.isEmpty(stockOutUuid) && !dbImageUpload.getIsUploadStockOutImage()) {
                    isCurrentSaveImage = true;
                    doSaveImage(stockOutUuid, imageUrl, SAVE_IMAGE_TYPE_OUT, dbImageUpload);
                }
            } else {
                isCurrentSaveImage = false;
            }

//            boolean hasDataToSave = false;
//            for (DbImageUpload upload:list) {
//                String imageUrl = upload.getImageUrl();
//                String stockInUuid = upload.getStockInUuid();
//                String stockOutUuid = upload.getStockOutUuid();
//                if (!TextUtils.isEmpty(stockInUuid) && !upload.getIsUploadStockInImage()) {
//                    isCurrentSaveImage = true;
//                    hasDataToSave = true;
//                    doSaveImage(stockInUuid, imageUrl, SAVE_IMAGE_TYPE_IN, upload);
//                    break;
//                } else if (!TextUtils.isEmpty(stockOutUuid) && !upload.getIsUploadStockOutImage()) {
//                    isCurrentSaveImage = true;
//                    hasDataToSave = true;
//                    doSaveImage(stockOutUuid, imageUrl, SAVE_IMAGE_TYPE_OUT, upload);
//                    break;
//                }
//            }
//            if(!hasDataToSave){
//                isCurrentSaveImage = false;
//            }
        } else {
            isCurrentSaveImage = false;
        }
    }


    /**
     * 请求保存图片接口
     */
    private void doSaveImage(String uuid, String imageUrl, final String type, final DbImageUpload data) {
        PBaseInfo pBaseInfo = BeanUtil.saveImage(mMerchant, uuid, imageUrl, type);
        mApiManager.saveImage(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<String>>().get())
                .subscribe(new ApiSubscribe<String>() {
                    @Override
                    protected void onResult(String result) {
                        switch (type) {
                            case SAVE_IMAGE_TYPE_IN:
                                data.setIsUploadStockInImage(true);
                                break;
                            case SAVE_IMAGE_TYPE_OUT:
                                data.setIsUploadStockOutImage(true);
                                break;
                            default:
                                break;
                        }
                        mDBManager.updateDbImageUpload(data);
                        //如果请求完成   删除数据  1. 验收-入库  2.验收-越库 3.分拣 4.出库 （盘点，调拨暂时不需要上传图片）
                        // 1. 验收-入库
//                        boolean isCheckInStoreIn = data.getType() == Common.DbType.TYPE_ChECK_IN_STORE_IN
//                                && !TextUtils.isEmpty(data.getStockInUuid())
//                                && data.getIsUploadStockInImage();
//                        // 2.验收-越库
//                        boolean isCheckInCrossOut = data.getType() == Common.DbType.TYPE_ChECK_IN_CROSS_OUT
//                                && (!TextUtils.isEmpty(data.getStockInUuid()) && data.getIsUploadStockInImage())
//                                && (!TextUtils.isEmpty(data.getStockOutUuid()) && data.getIsUploadStockOutImage());
//                        // 3.分拣   不删除，历史中使用
//                        boolean isSortOutStoreOut = data.getType() == Common.DbType.TYPE_SORT_OUT_STORE_OUT
//                                && !TextUtils.isEmpty(data.getStockOutUuid())
//                                && data.getIsUploadStockOutImage();
//                        // 4.出库
//                        boolean isStoreOut = data.getType() == Common.DbType.TYPE_STORE_OUT
//                                && !TextUtils.isEmpty(data.getStockOutUuid())
//                                && data.getIsUploadStockOutImage();

//                        if (isCheckInStoreIn || isCheckInCrossOut || isStoreOut) {
                            //删除本地图片
                            String imagePath = data.getImagePath();
                            deleteImage(imagePath);
                            //将该条数据从数据库移除
//                            mDBManager.deleteDbImageUpload(data);
//                        }
                        getUnSaveImageList();
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        isCurrentSaveImage = false;
                        LogUtils.e("-------", "--------保存图片失败-------");
                    }
                });
    }

    /**
     * 删除本地图片
     */
    private void deleteImage(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 图片上传跟接口请求同时获取数据库同一条数据， 导致一方先保存， 另一方为空的情况
     * 比如图片上传请求成功，保存url，继而接口请求成功， 但是此时的DbImageUpload是没有url的
     */
    private void refreshImageUrl(DbImageUpload data) {
        List<DbImageUpload> list = null;
        int type = data.getType();
        switch (type) {
            case Common.DbType.TYPE_ChECK_IN_STORE_IN:
                list = mDBManager.getDbListCheckInStoreIn();
                break;
            case Common.DbType.TYPE_ChECK_IN_CROSS_OUT:
                list = mDBManager.getDbListCheckInCrossOut();
                break;
            case Common.DbType.TYPE_ChECK_IN_CROSS_ALLCOCATE:
                list = mDBManager.getDbListCheckInCrossAllocate();
                break;
            case Common.DbType.TYPE_SORT_OUT_STORE_OUT:
                list = mDBManager.getDbListSortOutStoreOut();
                break;
            case Common.DbType.TYPE_STORE_OUT:
                list = mDBManager.getDbListStoreOut();
                break;
            //当这两个模块需要保存图片时添加
//            case Common.DbType.TYPE_ALLOCATE:
//
//                break;
//            case Common.DbType.TYPE_CHECK:
//                break;
            default:
                break;
        }
        if (list == null) {
            return;
        }
        for (DbImageUpload upload : list) {
            if (data.getId().longValue() == upload.getId().longValue()) {
                data.setImageUrl(upload.getImageUrl());
            }
        }
    }

    /**
     * 注释看 refreshImageUrl
     */
    private void refreshUUid(DbImageUpload dbImageUpload) {
        List<DbImageUpload> list = mDBManager.getDbListUnUploadImage();
        for (DbImageUpload upload : list) {
            if (dbImageUpload.getId().longValue() == upload.getId().longValue()) {
                int t = dbImageUpload.getType();
                switch (t) {
                    case Common.DbType.TYPE_ChECK_IN_STORE_IN:
                        dbImageUpload.setStockInUuid(upload.getStockInUuid());
                        break;
                    case Common.DbType.TYPE_ChECK_IN_CROSS_ALLCOCATE:
                        dbImageUpload.setStockInUuid(upload.getStockInUuid());
                        break;
                    case Common.DbType.TYPE_ChECK_IN_CROSS_OUT:
                        dbImageUpload.setStockInUuid(upload.getStockInUuid());
                        dbImageUpload.setStockOutUuid(upload.getStockOutUuid());
                        break;
                    case Common.DbType.TYPE_SORT_OUT_STORE_OUT:
                        dbImageUpload.setStockOutUuid(upload.getStockOutUuid());
                        break;
                    case Common.DbType.TYPE_STORE_OUT:
                        dbImageUpload.setStockOutUuid(upload.getStockOutUuid());
                        break;
                    //当这两个模块需要保存图片时添加
//                    case Common.DbType.TYPE_ALLOCATE:
//                        break;
//                    case Common.DbType.TYPE_CHECK:
//                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 处理服务器返回的错误码， 用于取消下一次请求
     */
    private void doResultFaild(int errorType, String failString, DbImageUpload data) {
        if (errorType == ServerConstant.ERROR_MESSAGE && !TextUtils.isEmpty(failString)) {
            data.setErrorString(failString);
            data.setErrorType(ServerConstant.ERROR_MESSAGE);
            mDBManager.updateDbImageUpload(data);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
