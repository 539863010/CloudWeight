package xm.cloudweight.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.xmzynt.storm.basic.idname.IdName;
import com.xmzynt.storm.service.goods.GoodsCategory;
import com.xmzynt.storm.service.process.ForecastProcessPlan;
import com.xmzynt.storm.service.purchase.PurchaseData;
import com.xmzynt.storm.service.user.merchant.Merchant;
import com.xmzynt.storm.service.wms.allocate.AllocateRecord;
import com.xmzynt.storm.service.wms.stock.Stock;
import com.xmzynt.storm.service.wms.warehouse.Warehouse;
import com.xmzynt.storm.util.GsonUtil;
import com.xmzynt.storm.util.query.PageData;

import java.util.List;
import java.util.Map;

import rx.Observable;
import xm.cloudweight.IRequestDataService;
import xm.cloudweight.OnRequestDataListener;
import xm.cloudweight.api.ApiManager;
import xm.cloudweight.api.ApiSubscribe;
import xm.cloudweight.api.ResponseEntity;
import xm.cloudweight.api.TransformerHelper;
import xm.cloudweight.bean.CustomSortOutData;
import xm.cloudweight.bean.PBaseInfo;
import xm.cloudweight.comm.Common;
import xm.cloudweight.net.RetrofitUtil;
import xm.cloudweight.utils.bussiness.BeanUtil;
import xm.cloudweight.utils.bussiness.RefreshMerchantHelper;
import xm.cloudweight.utils.dao.DBRequestManager;
import xm.cloudweight.utils.dao.bean.DbRequestData;

/**
 * @author wyh
 * @description: 个个界面数据请求
 * @create 2018/1/17
 */
public class RequestDataService extends Service implements RefreshMerchantHelper.onRefreshMerchantListener {

    private ApiManager mApiManager;
    private RefreshMerchantHelper mRefreshMerchantHelper;
    private Merchant mMerchant;
    private DBRequestManager mDBRequestManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mRefreshMerchantHelper = new RefreshMerchantHelper();
        mRefreshMerchantHelper.regist(this, this);

        mApiManager = RetrofitUtil.getApiManager(this);

        mDBRequestManager = new DBRequestManager(this);

    }

    @Override
    public void get(Merchant merchant) {
        mMerchant = merchant;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRefreshMerchantHelper != null) {
            mRefreshMerchantHelper.unregist(this);
        }
    }

    private final IRequestDataService.Stub mBinder = new IRequestDataService.Stub() {

        @Override
        public void onGetDataListener(long type, Map params, OnRequestDataListener listener) throws RemoteException {
            // type 请求数据类型   listener 返回监听
            if (type == -1) {
                return;
            }
            if (mMerchant == null) {
                if (listener != null) {
                    long failedType = getFailedType(type);
                    listener.onError(failedType, "RequestDataService中未获取用户信息");
                }
                return;
            }
            if (type == TYPE_SORT_OUT_COUNT || type == TYPE_SORT_OUT_WEIGHT) {
                getListSortOut(type, params, listener);
            } else if (type == TYPE_SORT_OUT_CANCEL) {
                sortOutCancel(params, listener);
            } else if (type == TYPE_CHECK_IN_GET_DROPDOWN_OPERATOR) {
                getDropdownOperator(params, listener);
            } else if (type == TYPE_CHECK_IN_QUERY_PURCHASE_DATA) {
                queryPurchaseData(params, listener);
            } else if (type == TYPE_SIMILAR_GET_DROPDOWN_LEAFCATEGORY) {
                getDropdownLeafCategory(listener);
            } else if (type == TYPE_SIMILAR_QUERY_STOCK) {
                queryStock(params, listener);
            } else if (type == TYPE_CHECK_IN_CANCEL) {
                cancelStockIn(params, listener);
            } else if (type == TYPE_SIMILAR_CANCEL) {
                cancelSimilar(params, listener);
            } else if (type == TYPE_SCAN_BY_TRACE_CODE) {
                scanByTraceCode(params, listener);
            } else if (type == TYPE_ALLOCATE_ACCEPT_WARE_HOUSE_CHECK) {
                getWareHouseCheck(listener);
            } else if (type == TYPE_ALLOCATE_ACCEPT_QUERY_NOT_ACCEPT_DATA) {
                queryNotAcceptData(params, listener);
            } else if (type == TYPE_PROCESS_STORAGE_QUERY_PROCESS_DATA) {
                queryProcessData(params, listener);
            } else if (type == TYPE_ALLOCATE_ACCEPT_CANCEL) {
                cancelAccept(params, listener);
            } else if (type == TYPE_PROCESS_STORAGE_CANCEL) {
                cancelStockInForProcess(params, listener);
            }
        }

    };

    /**
     * 撤销加工入库
     */
    private void cancelStockInForProcess(Map params, final OnRequestDataListener listener) {
        String uuid = (String) params.get("uuid");
        PBaseInfo pBaseInfo = BeanUtil.cancelStockInForProcess(mMerchant, uuid);
        mApiManager.cancelStockInForProcess(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<String>>().get())
                .subscribe(new ApiSubscribe<String>() {
                    @Override
                    protected void onResult(String result) {
                        try {
                            listener.onReceive(TYPE_PROCESS_STORAGE_CANCEL);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        try {
                            listener.onError(TYPE_PROCESS_STORAGE_CANCEL_FAILED, failString);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 撤销调拨验收
     */
    private void cancelAccept(Map params, final OnRequestDataListener listener) {
        String uuid = (String) params.get("uuid");
        PBaseInfo pBaseInfo = BeanUtil.cancelAccept(mMerchant, uuid);
        mApiManager.cancelAccept(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<String>>().get())
                .subscribe(new ApiSubscribe<String>() {
                    @Override
                    protected void onResult(String result) {
                        try {
                            listener.onReceive(TYPE_ALLOCATE_ACCEPT_CANCEL);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        try {
                            listener.onError(TYPE_ALLOCATE_ACCEPT_CANCEL_FAILED, failString);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 查询加工数据
     */
    private void queryProcessData(Map params, final OnRequestDataListener listener) {
        String createdTime = (String) params.get("createdTime");
        PBaseInfo baseInfo = BeanUtil.queryProcessData(mMerchant, createdTime, 0, 0, 0);
        mApiManager.queryProcessData(baseInfo).compose(new TransformerHelper<ResponseEntity<List<ForecastProcessPlan>>>().get())
                .subscribe(new ApiSubscribe<List<ForecastProcessPlan>>() {
                    @Override
                    protected void onResult(List<ForecastProcessPlan> result) {
                        try {
                            String listStr = GsonUtil.getGson().toJson(result);
                            saveDbRequestData(TYPE_PROCESS_STORAGE_QUERY_PROCESS_DATA, listStr);
                            listener.onReceive(TYPE_PROCESS_STORAGE_QUERY_PROCESS_DATA);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        try {
                            listener.onError(TYPE_PROCESS_STORAGE_QUERY_PROCESS_DATA_FAILED, failString);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 调拨验收-未验收商品列表
     */
    private void queryNotAcceptData(Map params, final OnRequestDataListener listener) {
        int page = (int) params.get("page");
        int pageSize = (int) params.get("pageSize");
        int defaultPageSize = (int) params.get("defaultPageSize");
        String date = (String) params.get("date");
        String status = (String) params.get("status");
        String inWarehouseUuid = (String) params.get("inWarehouseUuid");
        PBaseInfo pBaseInfo = BeanUtil.queryNotAcceptData(mMerchant, page, pageSize, defaultPageSize, date, status, inWarehouseUuid);
        mApiManager.queryNotAcceptData(pBaseInfo).compose(new TransformerHelper<ResponseEntity<List<AllocateRecord>>>().get())
                .subscribe(new ApiSubscribe<List<AllocateRecord>>() {
                    @Override
                    protected void onResult(List<AllocateRecord> result) {
                        try {
                            String listStr = GsonUtil.getGson().toJson(result);
                            saveDbRequestData(TYPE_ALLOCATE_ACCEPT_QUERY_NOT_ACCEPT_DATA, listStr);
                            listener.onReceive(TYPE_ALLOCATE_ACCEPT_QUERY_NOT_ACCEPT_DATA);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        try {
                            listener.onError(TYPE_ALLOCATE_ACCEPT_QUERY_NOT_ACCEPT_DATA_FAILED, failString);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 调拨验收-仓库验收列表
     */
    private void getWareHouseCheck(final OnRequestDataListener listener) {
        PBaseInfo wareHouseCheck = BeanUtil.getWareHouseCheck(mMerchant);
        mApiManager.getDropDownWareHouse(wareHouseCheck).compose(new TransformerHelper<ResponseEntity<List<Warehouse>>>().get())
                .subscribe(new ApiSubscribe<List<Warehouse>>() {
                    @Override
                    protected void onResult(List<Warehouse> result) {
                        try {
                            String listStr = GsonUtil.getGson().toJson(result);
                            saveDbRequestData(TYPE_ALLOCATE_ACCEPT_WARE_HOUSE_CHECK, listStr);
                            listener.onReceive(TYPE_ALLOCATE_ACCEPT_WARE_HOUSE_CHECK);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        try {
                            listener.onError(TYPE_ALLOCATE_ACCEPT_WARE_HOUSE_CHECK_FAILED, failString);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 扫描库存标签
     */
    private void scanByTraceCode(Map params, final OnRequestDataListener listener) {
        String traceCode = (String) params.get("traceCode");
        String warehouseUU = (String) params.get("warehouseUU");
        PBaseInfo pBaseInfo = BeanUtil.scanByTraceCode(mMerchant, traceCode, warehouseUU);
        mApiManager.scanByTraceCode(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<List<Stock>>>().get())
                .subscribe(new ApiSubscribe<List<Stock>>() {
                    @Override
                    protected void onResult(List<Stock> result) {
                        try {
                            String listStr = GsonUtil.getGson().toJson(result);
                            saveDbRequestData(TYPE_SCAN_BY_TRACE_CODE, listStr);
                            listener.onReceive(TYPE_SCAN_BY_TRACE_CODE);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        try {
                            listener.onError(TYPE_SCAN_BY_TRACE_CODE_FAILED, failString);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }

    /**
     * 出库，调拨，盘点-撤销
     */
    private void cancelSimilar(Map params, final OnRequestDataListener listener) {
        int type = (int) params.get("type");
        String uuid = (String) params.get("uuid");
        PBaseInfo pBaseInfo = BeanUtil.cancelSimilar(mMerchant, uuid);
        Observable<ResponseEntity<String>> observable = null;
        if (type == Common.DbType.TYPE_STORE_OUT) {
            //出库
            observable = mApiManager.cancelStockOut(pBaseInfo);
        } else if (type == Common.DbType.TYPE_ALLOCATE) {
            //调拨
            observable = mApiManager.cancelAllocate(pBaseInfo);
        }
        if (observable == null) {
            return;
        }
        observable.compose(new TransformerHelper<ResponseEntity<String>>().get())
                .subscribe(new ApiSubscribe<String>() {
                    @Override
                    protected void onResult(String result) {
                        try {
                            String listStr = GsonUtil.getGson().toJson(result);
                            saveDbRequestData(TYPE_SIMILAR_CANCEL, listStr);
                            listener.onReceive(TYPE_SIMILAR_CANCEL);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        try {
                            listener.onError(TYPE_SIMILAR_CANCEL_FAILED, failString);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                });
    }

    /**
     * 出库，调拨，盘点 - 查找库存记录
     */
    private void queryStock(Map params, final OnRequestDataListener listener) {
        int page = (int) params.get(Common.PAGE_STRING);
        int pageSize = (int) params.get(Common.PAGE_SIZE_STRING);
        int defaultPageSize = (int) params.get(Common.DEFAULT_PAGE_SIZE_STRING);
        String basketCode = (String) params.get("basketCode");
        PBaseInfo pBaseInfo = BeanUtil.queryStock(mMerchant, page, pageSize, defaultPageSize, basketCode);
        mApiManager.queryStock(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<PageData<Stock>>>().get())
                .subscribe(new ApiSubscribe<PageData<Stock>>() {
                    @Override
                    protected void onResult(PageData<Stock> result) {
                        try {
                            String listStr = GsonUtil.getGson().toJson(result);
                            saveDbRequestData(TYPE_SIMILAR_QUERY_STOCK, listStr);
                            listener.onReceive(TYPE_SIMILAR_QUERY_STOCK);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        try {
                            listener.onError(TYPE_SIMILAR_QUERY_STOCK_FAILED, failString);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 出库，调拨，盘点 - 查找类别
     */
    private void getDropdownLeafCategory(final OnRequestDataListener listener) {
        PBaseInfo pBaseInfo = BeanUtil.getDropdownLeafCategory(mMerchant);
        mApiManager.getDropdownLeafCategory(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<List<GoodsCategory>>>().get())
                .subscribe(new ApiSubscribe<List<GoodsCategory>>() {
                    @Override
                    protected void onResult(List<GoodsCategory> result) {
                        try {
                            String listStr = GsonUtil.getGson().toJson(result);
                            saveDbRequestData(TYPE_SIMILAR_GET_DROPDOWN_LEAFCATEGORY, listStr);
                            listener.onReceive(TYPE_SIMILAR_GET_DROPDOWN_LEAFCATEGORY);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        try {
                            listener.onError(TYPE_SIMILAR_GET_DROPDOWN_LEAFCATEGORY_FAILED, failString);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 验收-撤销
     */
    private void cancelStockIn(Map params, final OnRequestDataListener listener) {
        int type_cancel = (int) params.get("type");
        String uuid = (String) params.get("uuid");
        String stockInType;
        if (type_cancel == Common.DbType.TYPE_ChECK_IN_CROSS_OUT) {
            stockInType = "crossDock";
        } else {
            stockInType = "purchaseIn";
        }
        PBaseInfo pBaseInfo = BeanUtil.cancelStockIn(mMerchant, uuid, stockInType);
        mApiManager.cancelStockIn(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<String>>().get())
                .subscribe(new ApiSubscribe<String>() {
                    @Override
                    protected void onResult(String result) {
                        //三种验收撤销成功后的操作都一样
                        try {
                            listener.onReceive(TYPE_CHECK_IN_CANCEL);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        try {
                            listener.onError(TYPE_CHECK_IN_CANCEL_FAILED, failString);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                });
    }

    /**
     * 验收-列表
     */
    private void queryPurchaseData(Map params, final OnRequestDataListener listener) {
        int page = (int) params.get(Common.PAGE_STRING);
        int pageSize = (int) params.get(Common.PAGE_SIZE_STRING);
        int defaultPageSize = (int) params.get(Common.DEFAULT_PAGE_SIZE_STRING);
        String date = (String) params.get("DATE");
        PBaseInfo pBaseInfo = BeanUtil.queryPurchaseData(mMerchant, page, pageSize, defaultPageSize, date);
        mApiManager.queryPurchaseData(pBaseInfo).compose(new TransformerHelper<ResponseEntity<List<PurchaseData>>>().get())
                .subscribe(new ApiSubscribe<List<PurchaseData>>() {
                    @Override
                    protected void onResult(List<PurchaseData> result) {
                        try {
                            String listStr = GsonUtil.getGson().toJson(result);
                            saveDbRequestData(TYPE_CHECK_IN_QUERY_PURCHASE_DATA, listStr);
                            listener.onReceive(TYPE_CHECK_IN_QUERY_PURCHASE_DATA);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        try {
                            listener.onError(TYPE_CHECK_IN_QUERY_PURCHASE_DATA_FAILED, failString);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 验收-获取下拉操作人
     */
    private void getDropdownOperator(Map params, final OnRequestDataListener listener) {
        int page = (int) params.get(Common.PAGE_STRING);
        int pageSize = (int) params.get(Common.PAGE_SIZE_STRING);
        int defaultPageSize = (int) params.get(Common.DEFAULT_PAGE_SIZE_STRING);
        PBaseInfo pBaseInfo = BeanUtil.getDropdownOperator(mMerchant, page, pageSize, defaultPageSize);
        mApiManager.getDropdownOperator(pBaseInfo).compose(new TransformerHelper<ResponseEntity<List<IdName>>>().get())
                .subscribe(new ApiSubscribe<List<IdName>>() {
                    @Override
                    protected void onResult(List<IdName> result) {
                        try {
                            String listStr = GsonUtil.getGson().toJson(result);
                            saveDbRequestData(TYPE_CHECK_IN_GET_DROPDOWN_OPERATOR, listStr);
                            listener.onReceive(TYPE_CHECK_IN_GET_DROPDOWN_OPERATOR);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        try {
                            listener.onError(TYPE_CHECK_IN_GET_DROPDOWN_OPERATOR_FAILED, failString);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 获取分拣列表（数量，重量）
     */
    private void getListSortOut(final long type, Map params, final OnRequestDataListener listener) {
        if (params == null) {
            return;
        }
        int page = (int) params.get(Common.PAGE_STRING);
        int pageSize = (int) params.get(Common.PAGE_SIZE_STRING);
        int defaultPageSize = (int) params.get(Common.DEFAULT_PAGE_SIZE_STRING);
        String deliveryTime = (String) params.get("DATE");
        PBaseInfo pBaseInfo = BeanUtil.getSourOutListService(mMerchant, page, pageSize, defaultPageSize, deliveryTime);
        Observable<ResponseEntity<List<CustomSortOutData>>> observable;
        if (type == TYPE_SORT_OUT_WEIGHT) {
            //重量
            observable = mApiManager.getsForWeigh(pBaseInfo);
        } else {
            //数量
            observable = mApiManager.getsForNotWeigh(pBaseInfo);
        }
        observable
                .compose(new TransformerHelper<ResponseEntity<List<CustomSortOutData>>>().get())
                .subscribe(new ApiSubscribe<List<CustomSortOutData>>() {

                    @Override
                    protected void onResult(List<CustomSortOutData> result) {
                        try {
                            String resultString = GsonUtil.getGson().toJson(result);
                            if (type == TYPE_SORT_OUT_WEIGHT) {
                                saveDbRequestData(TYPE_SORT_OUT_WEIGHT, resultString);
                                listener.onReceive(TYPE_SORT_OUT_WEIGHT);
                            } else {
                                saveDbRequestData(TYPE_SORT_OUT_COUNT, resultString);
                                listener.onReceive(TYPE_SORT_OUT_COUNT);
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        try {
                            if (type == TYPE_SORT_OUT_WEIGHT) {
                                listener.onError(TYPE_SORT_OUT_WEIGHT_FAILED, failString);
                            } else {
                                listener.onError(TYPE_SORT_OUT_COUNT_FAILED, failString);
                            }
                        } catch (RemoteException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 分拣撤销
     */
    private void sortOutCancel(Map params, final OnRequestDataListener listener) {
        if (params == null) {
            return;
        }
        CustomSortOutData data = (CustomSortOutData) params.get("CustomSortOutData");
        PBaseInfo pBaseInfo = BeanUtil.cancelSortOut(mMerchant, data);
        mApiManager.cancelSortOut(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<CustomSortOutData>>().get())
                .subscribe(new ApiSubscribe<CustomSortOutData>() {
                    @Override
                    protected void onResult(CustomSortOutData result) {
                        try {
                            listener.onReceive(TYPE_SORT_OUT_CANCEL);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        try {
                            listener.onError(TYPE_SORT_OUT_CANCEL_FAILED, failString);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void saveDbRequestData(long type, String data) {
        DbRequestData requestData = new DbRequestData();
        requestData.setId(type);
        requestData.setData(data);
        mDBRequestManager.insertOrReplace(requestData);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * 保存到数据库的接口类型,用type来保存主键id, 确保数据库中只有一条该type数据
     */
    /**
     * 获取分拣数据-重量
     */
    public static final long TYPE_SORT_OUT_WEIGHT = 1L;
    public static final long TYPE_SORT_OUT_WEIGHT_FAILED = 2L;
    /**
     * 获取分拣数据-数量
     */
    public static final long TYPE_SORT_OUT_COUNT = 3L;
    public static final long TYPE_SORT_OUT_COUNT_FAILED = 4L;
    /**
     * 获取分拣数据-取消
     */
    public static final long TYPE_SORT_OUT_CANCEL = 5L;
    public static final long TYPE_SORT_OUT_CANCEL_FAILED = 6L;
    /**
     * 验收-获取下拉操作者
     */
    public static final long TYPE_CHECK_IN_GET_DROPDOWN_OPERATOR = 7L;
    public static final long TYPE_CHECK_IN_GET_DROPDOWN_OPERATOR_FAILED = 8L;
    /**
     * 验收-批量查询采购信息
     */
    public static final long TYPE_CHECK_IN_QUERY_PURCHASE_DATA = 9L;
    public static final long TYPE_CHECK_IN_QUERY_PURCHASE_DATA_FAILED = 10L;
    /**
     * 验收-撤销入库
     */
    public static final long TYPE_CHECK_IN_CANCEL = 11L;
    public static final long TYPE_CHECK_IN_CANCEL_FAILED = 12L;
    /**
     * 出库，调拨，盘点-查找类别
     */
    public static final long TYPE_SIMILAR_GET_DROPDOWN_LEAFCATEGORY = 13L;
    public static final long TYPE_SIMILAR_GET_DROPDOWN_LEAFCATEGORY_FAILED = 14L;
    /**
     * 出库，调拨，盘点-查找库存记录
     */
    public static final long TYPE_SIMILAR_QUERY_STOCK = 15L;
    public static final long TYPE_SIMILAR_QUERY_STOCK_FAILED = 16L;
    /**
     * 出库，调拨，盘点-撤销
     */
    public static final long TYPE_SIMILAR_CANCEL = 17L;
    public static final long TYPE_SIMILAR_CANCEL_FAILED = 18L;
    /**
     * 扫描库存标签
     */
    public static final long TYPE_SCAN_BY_TRACE_CODE = 19L;
    public static final long TYPE_SCAN_BY_TRACE_CODE_FAILED = 20L;
    /**
     * 调拨验收-仓库验收列表
     */
    public static final long TYPE_ALLOCATE_ACCEPT_WARE_HOUSE_CHECK = 21L;
    public static final long TYPE_ALLOCATE_ACCEPT_WARE_HOUSE_CHECK_FAILED = 22L;
    /**
     * 调拨验收-商品列表
     */
    public static final long TYPE_ALLOCATE_ACCEPT_QUERY_NOT_ACCEPT_DATA = 23L;
    public static final long TYPE_ALLOCATE_ACCEPT_QUERY_NOT_ACCEPT_DATA_FAILED = 24L;
    /**
     * 调拨验收-查询加工列表
     */
    public static final long TYPE_PROCESS_STORAGE_QUERY_PROCESS_DATA = 25L;
    public static final long TYPE_PROCESS_STORAGE_QUERY_PROCESS_DATA_FAILED = 26L;
    /**
     * 调拨验收-撤销调拨验收
     */
    public static final long TYPE_ALLOCATE_ACCEPT_CANCEL = 27L;
    public static final long TYPE_ALLOCATE_ACCEPT_CANCEL_FAILED = 28L;
    /**
     * 加工入库-撤销
     */
    public static final long TYPE_PROCESS_STORAGE_CANCEL = 29L;
    public static final long TYPE_PROCESS_STORAGE_CANCEL_FAILED = 30L;

    /**
     * 注意添加新type
     */
    private long getFailedType(long type) {
        long failedType = -1L;
        if (type == TYPE_SORT_OUT_COUNT) {
            failedType = TYPE_SORT_OUT_COUNT_FAILED;
        } else if (type == TYPE_SORT_OUT_WEIGHT) {
            failedType = TYPE_SORT_OUT_WEIGHT_FAILED;
        } else if (type == TYPE_SORT_OUT_CANCEL) {
            failedType = TYPE_SORT_OUT_CANCEL_FAILED;
        } else if (type == TYPE_CHECK_IN_GET_DROPDOWN_OPERATOR) {
            failedType = TYPE_CHECK_IN_GET_DROPDOWN_OPERATOR_FAILED;
        } else if (type == TYPE_CHECK_IN_QUERY_PURCHASE_DATA) {
            failedType = TYPE_CHECK_IN_QUERY_PURCHASE_DATA_FAILED;
        } else if (type == TYPE_CHECK_IN_CANCEL) {
            failedType = TYPE_CHECK_IN_CANCEL_FAILED;
        } else if (type == TYPE_SIMILAR_GET_DROPDOWN_LEAFCATEGORY) {
            failedType = TYPE_SIMILAR_GET_DROPDOWN_LEAFCATEGORY_FAILED;
        } else if (type == TYPE_SIMILAR_QUERY_STOCK) {
            failedType = TYPE_SIMILAR_QUERY_STOCK_FAILED;
        } else if (type == TYPE_SIMILAR_CANCEL) {
            failedType = TYPE_SIMILAR_CANCEL_FAILED;
        } else if (type == TYPE_SCAN_BY_TRACE_CODE) {
            failedType = TYPE_SCAN_BY_TRACE_CODE_FAILED;
        } else if (type == TYPE_ALLOCATE_ACCEPT_WARE_HOUSE_CHECK) {
            failedType = TYPE_ALLOCATE_ACCEPT_WARE_HOUSE_CHECK_FAILED;
        } else if (type == TYPE_ALLOCATE_ACCEPT_QUERY_NOT_ACCEPT_DATA) {
            failedType = TYPE_ALLOCATE_ACCEPT_QUERY_NOT_ACCEPT_DATA_FAILED;
        } else if (type == TYPE_PROCESS_STORAGE_QUERY_PROCESS_DATA) {
            failedType = TYPE_PROCESS_STORAGE_QUERY_PROCESS_DATA_FAILED;
        } else if (type == TYPE_ALLOCATE_ACCEPT_CANCEL) {
            failedType = TYPE_ALLOCATE_ACCEPT_CANCEL_FAILED;
        } else if (type == TYPE_PROCESS_STORAGE_CANCEL) {
            failedType = TYPE_PROCESS_STORAGE_CANCEL_FAILED;
        }
        return failedType;
    }

}
