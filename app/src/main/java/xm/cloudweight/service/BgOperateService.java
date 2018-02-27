package xm.cloudweight.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import com.xmzynt.storm.service.process.StockInData;
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
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import rx.Observable;
import rx.Subscriber;
import xm.cloudweight.api.ApiManager;
import xm.cloudweight.api.ApiSubscribe;
import xm.cloudweight.api.ResponseEntity;
import xm.cloudweight.api.TransformerHelper;
import xm.cloudweight.bean.CustomSortOutData;
import xm.cloudweight.bean.PBaseInfo;
import xm.cloudweight.comm.BrocastFilter;
import xm.cloudweight.comm.ServerConstant;
import xm.cloudweight.net.RetrofitUtil;
import xm.cloudweight.utils.LogUtils;
import xm.cloudweight.utils.bussiness.BeanUtil;
import xm.cloudweight.utils.bussiness.BuglyUtil;
import xm.cloudweight.utils.bussiness.GetImageFile;
import xm.cloudweight.utils.bussiness.RefreshMerchantHelper;
import xm.cloudweight.utils.bussiness.UploadPhotoUtil;
import xm.cloudweight.utils.dao.DBManager;
import xm.cloudweight.utils.dao.bean.DbImageUpload;

/**
 * @author wyh
 * @description: 用来请求保存图片url接口
 * @create 2017/11/20
 */
public class BgOperateService extends Service implements RefreshMerchantHelper.onRefreshMerchantListener {

    private DBManager mDBManager;
    private ApiManager mApiManager;
    private Merchant mMerchant;
    /**
     * 当前是否在上传图片
     */
    private boolean isCurrentUploadImage;
    /**
     * 当前是否在分拣
     */
    private boolean isCurrentSortOutStoreOut;
    /**
     * 当前是否在验收-入库
     */
    private boolean isCurrentCheckInStoreIn;
    /**
     * 当前是否在验收-越库
     */
    private boolean isCurrentCheckInCrossOut;
    /**
     * 当前是否在验收-越库调拨
     */
    private boolean isCurrentCheckInCrossAllocate;
    /**
     * 当前是否在出库
     */
    private boolean isCurrentStoreOut;
    /**
     * 当前是否在调拨
     */
    private boolean isCurrentAllocate;
    /**
     * 当前是否在盘点
     */
    private boolean isCurrentCheck;
    /**
     * 当前是否在加工入库
     */
    private boolean isCurrentProcessStoreIn;
    /**
     * 当前是否调拨验收
     */
    private boolean isCurrentAllocateAccept;

    private RefreshMerchantHelper mRefreshMerchantHelper;

    public BgOperateService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mRefreshMerchantHelper = new RefreshMerchantHelper();
        mRefreshMerchantHelper.regist(this, this);

        mApiManager = RetrofitUtil.getApiManager(this);
        mDBManager = new DBManager(this);
        //创建计时器定期轮询
        createTimer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRefreshMerchantHelper.unregist(this);
    }

    @Override
    public void get(Merchant merchant) {
        mMerchant = merchant;
    }

    private void createTimer() {
        Observable.interval(5, TimeUnit.SECONDS)
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        BuglyUtil.uploadCrash(e);
                    }

                    @Override
                    public void onNext(Long aLong) {
                        upLoad();
                    }
                });

    }

    private void upLoad() {
        if (mMerchant != null) {
            //根据本地path获取url
            if (!isCurrentUploadImage) {
                getUnUploadImageList();
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
            //请求加工入库
            if (!isCurrentProcessStoreIn) {
                getUnProcessStoreInList();
            }
            //请求调拨验收
            if (!isCurrentAllocateAccept) {
                getUnAllocateAcceptList();
            }
            LogUtils.e("----",
                    "正在获取url = " + isCurrentUploadImage
                            + "\n 正在入库 = " + isCurrentCheckInStoreIn
                            + "\n 正在越库 = " + isCurrentCheckInCrossOut
                            + "\n 正在越库调拨 = " + isCurrentCheckInCrossAllocate
                            + "\n 正在分拣 = " + isCurrentSortOutStoreOut
                            + "\n 正在出库 = " + isCurrentStoreOut
                            + "\n 正在调拨 = " + isCurrentAllocate
                            + "\n 正在盘点 = " + isCurrentCheck
                            + "\n 正在加工入库 = " + isCurrentProcessStoreIn
                            + "\n 正在调拨验收 = " + isCurrentAllocateAccept
            );
        }
    }

    /**
     * 调拨验收
     */
    private void getUnAllocateAcceptList() {
        List<DbImageUpload> list = mDBManager.getDbListAllocateAccept();
        if (list != null && list.size() > 0) {
            isCurrentAllocateAccept = true;
            DbImageUpload data = list.get(0);
            doUnAllocateAcceptList(data);
        } else {
            isCurrentAllocateAccept = false;
        }
    }

    private void doUnAllocateAcceptList(final DbImageUpload data) {
        final StockInRecord stockInRecord = GsonUtil.getGson().fromJson(data.getLine(), StockInRecord.class);
        PBaseInfo pBaseInfo = BeanUtil.accept(mMerchant, stockInRecord);
        mApiManager.accept(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<String>>().get())
                .subscribe(new ApiSubscribe<String>() {
                    @Override
                    protected void onResult(String result) {
                        data.setStockInUuid(result);
                        data.setIsRequestSuccess(true);
                        mDBManager.updateDbImageUpload(data);
                        sendBroadcast(new Intent(BrocastFilter.FILTER_REFRESH_HISTORY));
                        getUnAllocateAcceptList();
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        isCurrentAllocateAccept = false;
                        LogUtils.e("----------调拨验收失败-------------", failString);
                        doResultFaild(errorType, failString, data);
                    }

                });
    }

    /**
     * 加工入库
     */
    private void getUnProcessStoreInList() {
        List<DbImageUpload> list = mDBManager.getDbListProcessStoreIn();
        if (list != null && list.size() > 0) {
            isCurrentProcessStoreIn = true;
            DbImageUpload data = list.get(0);
            doUnProcessStoreInList(data);
        } else {
            isCurrentProcessStoreIn = false;
        }
    }

    private void doUnProcessStoreInList(final DbImageUpload data) {
        final StockInData stockInData = GsonUtil.getGson().fromJson(data.getLine(), StockInData.class);
        PBaseInfo pBaseInfo = BeanUtil.stockInForProcess(mMerchant, stockInData);
        mApiManager.stockInForProcess(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<String>>().get())
                .subscribe(new ApiSubscribe<String>() {
                    @Override
                    protected void onResult(String result) {
                        data.setStockInUuid(result);
                        data.setIsRequestSuccess(true);
                        mDBManager.updateDbImageUpload(data);
                        sendBroadcast(new Intent(BrocastFilter.FILTER_REFRESH_HISTORY));
                        getUnProcessStoreInList();
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        isCurrentProcessStoreIn = false;
                        LogUtils.e("----------加工入库失败-------------", failString);
                        doResultFaild(errorType, failString, data);
                    }

                });
    }

    /**
     * 验收-入库
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
                        data.setStockInUuid(result);
                        data.setIsRequestSuccess(true);
                        mDBManager.updateDbImageUpload(data);
                        sendBroadcast(new Intent(BrocastFilter.FILTER_REFRESH_HISTORY));
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
     * 验收-越库调拨
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

    private void doCheckInCrossAllocate(final DbImageUpload data) {
        StockInRecord record = GsonUtil.getGson().fromJson(data.getLine(), StockInRecord.class);
        PBaseInfo pBaseInfo = BeanUtil.crossAllocate(mMerchant, record);
        mApiManager.crossAllocate(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<String>>().get())
                .subscribe(new ApiSubscribe<String>() {
                    @Override
                    protected void onResult(String result) {
                        data.setStockInUuid(result);
                        data.setIsRequestSuccess(true);
                        mDBManager.updateDbImageUpload(data);
                        sendBroadcast(new Intent(BrocastFilter.FILTER_REFRESH_HISTORY));
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
     * 验收 - 越库
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
                        data.setStockOutUuid(result);
                        data.setIsRequestSuccess(true);
                        mDBManager.updateDbImageUpload(data);
                        sendBroadcast(new Intent(BrocastFilter.FILTER_REFRESH_HISTORY));
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
     * 出库
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
                        data.setStockOutUuid(result);
                        data.setIsRequestSuccess(true);
                        mDBManager.updateDbImageUpload(data);
                        sendBroadcast(new Intent(BrocastFilter.FILTER_REFRESH_HISTORY));
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
     * 调拨
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
                        data.setStockOutUuid(result);
                        data.setIsRequestSuccess(true);
                        //调拨成功更新数据库
                        mDBManager.updateDbImageUpload(data);
                        sendBroadcast(new Intent(BrocastFilter.FILTER_REFRESH_HISTORY));
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
     * 盘点
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
                        data.setIsRequestSuccess(true);
                        //盘点成功删除数据库
                        mDBManager.updateDbImageUpload(data);
                        //有历史时添加通知
                        //                       sendBroadcast(new Intent(BrocastFilter.FILTER_REFRESH_HISTORY));
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
     * 分拣
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

    private void doSortOutStoreOut(final DbImageUpload data) {
        final CustomSortOutData sortOutData = GsonUtil.getGson().fromJson(data.getLine(), CustomSortOutData.class);
        PBaseInfo pBaseInfo = BeanUtil.sortOut(mMerchant, sortOutData);
        mApiManager.sortOut(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<List<String>>>().get())
                .subscribe(new ApiSubscribe<List<String>>() {
                    @Override
                    protected void onResult(List<String> result) {
                        //修改数据库信息
                        //用于删除
                        sortOutData.setStockOutRecordUuids(result);
                        //保存出库uuid
                        data.setLine(GsonUtil.getGson().toJson(sortOutData));
                        data.setStockOutUuid(result.get(0));
                        data.setIsRequestSuccess(true);
                        mDBManager.updateDbImageUpload(data);
                        //通知SortOutActivity更新（当popwindow打开时更新）
                        sendBroadcast(new Intent(BrocastFilter.FILTER_REFRESH_HISTORY));
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
        final String imagePath = dbImageUpload.getImagePath();
        if (TextUtils.isEmpty(imagePath) || !new File(imagePath).exists()) {
            isCurrentUploadImage = false;
            return;
        }
        String uuid = mMerchant.getUuid();
        HashMap<String, Object> map = new HashMap<>();
        map.put("uuid", uuid);
        String type = IMGType.GOODS_IMG.toString();
        map.put("IMG-TYPE", type);
        // 取文件名
        String fileName = GetImageFile.getName(imagePath);
        map.put("fileName", fileName);
        RequestBody requestBody =
                RequestBody.create(MediaType.parse("multipart/form-data;boundary=android"), UploadPhotoUtil.getFileByte(imagePath));
        mApiManager.uploadPhotoFromAndroid(map, requestBody)
                .compose(new TransformerHelper<ResponseEntity<String>>().get())
                .subscribe(new ApiSubscribe<String>() {
                    @Override
                    protected void onResult(String result) {
                        dbImageUpload.setImageUrl(result);
                        dbImageUpload.setIsRequestSuccess(true);
                        mDBManager.updateDbImageUpload(dbImageUpload);
                        //删除图片
                        deleteImage(imagePath);
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
        return new BgOperateServiceImpl();
    }

    public class BgOperateServiceImpl extends xm.cloudweight.IBgOperateService.Stub {

    }

}
