package xm.cloudweight.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.xmzynt.storm.service.user.merchant.Merchant;

import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.schedulers.Schedulers;
import xm.cloudweight.IRequestDataService;
import xm.cloudweight.OnRequestDataListener;
import xm.cloudweight.SortOutActivity;
import xm.cloudweight.api.ApiManager;
import xm.cloudweight.api.ApiSubscribe;
import xm.cloudweight.api.ResponseEntity;
import xm.cloudweight.api.TransformerHelper;
import xm.cloudweight.bean.CustomSortOutData;
import xm.cloudweight.bean.PBaseInfo;
import xm.cloudweight.net.RetrofitUtil;
import xm.cloudweight.utils.bussiness.BeanUtil;
import xm.cloudweight.utils.bussiness.BuglyUtil;
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
    private void getListSortOut(Map params, final OnRequestDataListener listener) throws RemoteException {
        if (params == null) {
            return;
        }
        // 格式  TYPE_COUNT, PAGE, PAGE_SIZE, DEFAULT_PAGE_SIZE, time
        final int type = (int) params.get("TYPE");
        int page = (int) params.get("PAGE");
        int pageSize = (int) params.get("PAGE_SIZE");
        int defaultPageSize = (int) params.get("DEFAULT_PAGE_SIZE");
        String deliveryTime = (String) params.get("DATE");
        PBaseInfo pBaseInfo = BeanUtil.getSourOutListService(mMerchant, page, pageSize, defaultPageSize, deliveryTime);
        Observable<ResponseEntity> observable;
        if (type == SortOutActivity.TYPE_WEIGHT) {
            //重量
            observable = mApiManager.getsForWeigh(pBaseInfo);
        } else {
            //数量
            observable = mApiManager.getsForNotWeigh(pBaseInfo);
        }
        observable.subscribeOn(Schedulers.io())
                .subscribe(new Observer<ResponseEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        try {
                            if (type == SortOutActivity.TYPE_WEIGHT) {
                                listener.onError(TYPE_SORT_OUT_WEIGHT_FAILED, "获取分拣-重量列表失败");
                            } else {
                                listener.onError(TYPE_SORT_OUT_COUNT_FAILED, "获取分拣-数量列表失败");
                            }
                            BuglyUtil.uploadCrash(e);
                        } catch (RemoteException e1) {
                            e1.printStackTrace();
                        }
                    }

                    @Override
                    public void onNext(ResponseEntity re) {
                        try {
                            String result = re.getData();
                            DbRequestData data = new DbRequestData();
                            if (type == SortOutActivity.TYPE_WEIGHT) {
                                data.setId(TYPE_SORT_OUT_WEIGHT);
                                data.setData(result);
                                mDBRequestManager.insertOrReplace(data);
                                listener.onReceive(TYPE_SORT_OUT_WEIGHT);
                            } else {
                                data.setId(TYPE_SORT_OUT_COUNT);
                                data.setData(result);
                                mDBRequestManager.insertOrReplace(data);
                                listener.onReceive(TYPE_SORT_OUT_COUNT);
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
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
                    listener.onError(type, "RequestDataService中未获取用户信息");
                }
                return;
            }
            if (type == TYPE_SORT_OUT_COUNT || type == TYPE_SORT_OUT_WEIGHT) {
                getListSortOut(params, listener);
            } else if (type == TYPE_SORT_OUT_CANCEL) {
                sortOutCancel(params, listener);
            }
        }

    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * 保存到数据库的接口类型,用type来保存主键id, 确保数据库中只有一条该type数据
     */
    //获取分拣数据-重量
    public static final long TYPE_SORT_OUT_WEIGHT = 1L;
    public static final long TYPE_SORT_OUT_WEIGHT_FAILED = 11L;
    //获取分拣数据-数量
    public static final long TYPE_SORT_OUT_COUNT = 2L;
    public static final long TYPE_SORT_OUT_COUNT_FAILED = 22L;
    //获取分拣数据-取消
    public static final long TYPE_SORT_OUT_CANCEL = 3L;
    public static final long TYPE_SORT_OUT_CANCEL_FAILED = 33L;

}
