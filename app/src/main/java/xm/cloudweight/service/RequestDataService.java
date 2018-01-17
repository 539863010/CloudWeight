package xm.cloudweight.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.xmzynt.storm.service.user.merchant.Merchant;
import com.xmzynt.storm.util.GsonUtil;

import java.util.List;

import rx.Observable;
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
import xm.cloudweight.utils.bussiness.RefreshMerchantHelper;
import xm.cloudweight.utils.bussiness.SplitParamUtil;

/**
 * @author wyh
 * @description: 个个界面数据请求
 * @create 2018/1/17
 */
public class RequestDataService extends Service implements RefreshMerchantHelper.onRefreshMerchantListener {

    private RequestDataImpl mRequestDataImpl = new RequestDataImpl();
    private ApiManager mApiManager;
    /**
     * 获取验收数据
     */
    public static final int TYPE_REQUEST_DATA_CHECK_IN = 1;
    /**
     * 获取分拣数据
     */
    public static final int TYPE_REQUEST_DATA_SORT_OUT = 2;
    /**
     * 获取出库
     */
    public static final int TYPE_REQUEST_DATA_STOCK_OUT = 3;
    /**
     * 获取调拨
     */
    public static final int TYPE_REQUEST_DATA_ALLOCTE = 4;
    /**
     * 获取盘点
     */
    public static final int TYPE_REQUEST_DATA_CHECK = 5;
    private RefreshMerchantHelper mRefreshMerchantHelper;
    private Merchant mMerchant;

    @Override
    public void onCreate() {
        super.onCreate();

        mRefreshMerchantHelper = new RefreshMerchantHelper();
        mRefreshMerchantHelper.regist(this, this);

        mApiManager = RetrofitUtil.getApiManager(this);
    }

    @Override
    public void get(Merchant merchant) {
        mMerchant = merchant;
    }

    private void sortOut(String param, final OnRequestDataListener listener) throws RemoteException {
        String[] params = SplitParamUtil.split(param);
        if (params == null) {
            return;
        }
        // 格式  TYPE_COUNT, PAGE, PAGE_SIZE, DEFAULT_PAGE_SIZE, time
        final int type = Integer.parseInt(params[0]);
        int page = Integer.parseInt(params[1]);
        int pageSize = Integer.parseInt(params[2]);
        int defaultPageSize = Integer.parseInt(params[3]);
        String deliveryTime = params[4];
        PBaseInfo pBaseInfo = BeanUtil.getSourOutListService(mMerchant, page, pageSize, defaultPageSize, deliveryTime);
        Observable<ResponseEntity<List<CustomSortOutData>>> observable;
        if (type == SortOutActivity.TYPE_WEIGHT) {
            //重量
            observable = mApiManager.getsForWeigh(pBaseInfo);
        } else {
            //数量
            observable = mApiManager.getsForNotWeigh(pBaseInfo);
        }
        observable.compose(new TransformerHelper<ResponseEntity<List<CustomSortOutData>>>().get())
                .subscribe(new ApiSubscribe<List<CustomSortOutData>>() {
                    @Override
                    protected void onResult(List<CustomSortOutData> result) {
                        try {
                            listener.onReceive(type, GsonUtil.getGson().toJson(result));
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        try {
                            listener.onError(type, failString);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private class RequestDataImpl extends IRequestDataService.Stub {

        @Override
        public void onGetDataListener(int type, String params, OnRequestDataListener listener) throws RemoteException {
            // type 请求数据类型   listener 返回监听
            if (type == -1 || listener == null) {
                return;
            }
            if (mMerchant == null) {
                listener.onError(type, "RequestDataService中未获取用户信息");
                return;
            }
            switch (type) {
                case TYPE_REQUEST_DATA_CHECK_IN:

                    break;
                case TYPE_REQUEST_DATA_SORT_OUT:
                    sortOut(params, listener);
                    break;
                case TYPE_REQUEST_DATA_STOCK_OUT:

                    break;
                case TYPE_REQUEST_DATA_ALLOCTE:

                    break;
                case TYPE_REQUEST_DATA_CHECK:

                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mRequestDataImpl;
    }

}
