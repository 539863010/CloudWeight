package xm.cloudweight.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.xmzynt.storm.basic.idname.IdName;
import com.xmzynt.storm.service.purchase.PurchaseData;
import com.xmzynt.storm.service.user.merchant.Merchant;
import com.xmzynt.storm.util.GsonUtil;

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
            }
        }

    };


    // TODO: 2018/1/22 注意添加新type
    private long getFailedType(long type) {
        long failedType = -1L;
        if (type == TYPE_SORT_OUT_COUNT) {
            failedType = TYPE_SORT_OUT_COUNT;
        } else if (type == TYPE_SORT_OUT_WEIGHT) {
            failedType = TYPE_SORT_OUT_WEIGHT_FAILED;
        } else if (type == TYPE_SORT_OUT_CANCEL) {
            failedType = TYPE_SORT_OUT_CANCEL_FAILED;
        } else if (type == TYPE_CHECK_IN_GET_DROPDOWN_OPERATOR) {
            failedType = TYPE_CHECK_IN_GET_DROPDOWN_OPERATOR_FAILED;
        } else if (type == TYPE_CHECK_IN_QUERY_PURCHASE_DATA) {
            failedType = TYPE_CHECK_IN_QUERY_PURCHASE_DATA_FAILED;
        }
        return failedType;
    }


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
    //获取分拣数据-重量
    public static final long TYPE_SORT_OUT_WEIGHT = 1L;
    public static final long TYPE_SORT_OUT_WEIGHT_FAILED = 2L;
    //获取分拣数据-数量
    public static final long TYPE_SORT_OUT_COUNT = 3L;
    public static final long TYPE_SORT_OUT_COUNT_FAILED = 4L;
    //获取分拣数据-取消
    public static final long TYPE_SORT_OUT_CANCEL = 5L;
    public static final long TYPE_SORT_OUT_CANCEL_FAILED = 6L;
    //验收-获取下拉操作者
    public static final long TYPE_CHECK_IN_GET_DROPDOWN_OPERATOR = 7L;
    public static final long TYPE_CHECK_IN_GET_DROPDOWN_OPERATOR_FAILED = 8L;
    //验收-批量查询采购信息
    public static final long TYPE_CHECK_IN_QUERY_PURCHASE_DATA = 9L;
    public static final long TYPE_CHECK_IN_QUERY_PURCHASE_DATA_FAILED = 10L;

}
