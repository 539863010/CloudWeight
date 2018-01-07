package xm.cloudweight.presenter;

import com.xmzynt.storm.service.user.customer.MerchantCustomer;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import xm.cloudweight.SortOutActivity;
import xm.cloudweight.api.ApiSubscribe;
import xm.cloudweight.api.ResponseEntity;
import xm.cloudweight.api.TransformerHelper;
import xm.cloudweight.base.BaseActivity;
import xm.cloudweight.bean.CustomSortOutData;
import xm.cloudweight.bean.PBaseInfo;
import xm.cloudweight.impl.SortOutImpl;
import xm.cloudweight.utils.bussiness.BeanUtil;

/**
 * @author wyh
 * @Description:
 * @creat 2017/10/31
 */
public class SortOutPresenter {

    /**
     * 查询客户列表
     */
    public static void getDropdownCustomers(final BaseActivity aty, int page, int pageSize, int defaultPageSize) {
        if (!(aty instanceof SortOutImpl.OnGetMerchantCustomerListener)) {
            return;
        }
        PBaseInfo p = BeanUtil.getMerchantCustomer(aty, page, pageSize, defaultPageSize);
        aty.getApiManager().getDropdownCustomers(p)
                .compose(new TransformerHelper<ResponseEntity<List<MerchantCustomer>>>().get(aty))
                .subscribe(new ApiSubscribe<List<MerchantCustomer>>() {
                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        ((SortOutImpl.OnGetMerchantCustomerListener) aty).getMerchantCustomerFailed(errorType, failString);
                    }

                    @Override
                    protected void onResult(List<MerchantCustomer> result) {
                        ((SortOutImpl.OnGetMerchantCustomerListener) aty).getMerchantCustomerSuccess(result);
                    }
                });
    }

    /**
     * 获取查询列表
     */
    public static Subscription getSourOutList(final BaseActivity aty, final int type, int page, int pageSize, int defaultPageSize, String deliveryTime) {

        if (!(aty instanceof SortOutImpl.OnGetSortOutListListener)) {
            return null;
        }
        PBaseInfo pBaseInfo = BeanUtil.getSourOutList(aty, page, pageSize, defaultPageSize, deliveryTime);
        Observable<ResponseEntity<List<CustomSortOutData>>> observable;
        if (type == SortOutActivity.TYPE_WEIGHT) {
            //重量
            observable = aty.getApiManager().getsForWeigh(pBaseInfo);
        } else {
            //数量
            observable = aty.getApiManager().getsForNotWeigh(pBaseInfo);
        }
        return observable.compose(new TransformerHelper<ResponseEntity<List<CustomSortOutData>>>().get(aty))
                .subscribe(new ApiSubscribe<List<CustomSortOutData>>() {
                    @Override
                    protected void onResult(List<CustomSortOutData> result) {
                        ((SortOutImpl.OnGetSortOutListListener) aty).getSortOutListSuccess(type, result);
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        ((SortOutImpl.OnGetSortOutListListener) aty).getSortOutListFailed(type, failString);
                    }
                });
    }


    public static void cancelSortOut(final BaseActivity aty, CustomSortOutData data) {
        if (!(aty instanceof SortOutImpl.OnCancelSortOutListener)) {
            return;
        }
        PBaseInfo pBaseInfo = BeanUtil.cancelSortOut(aty, data);
        aty.getApiManager().cancelSortOut(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<CustomSortOutData>>().get(aty))
                .subscribe(new ApiSubscribe<CustomSortOutData>() {
                    @Override
                    protected void onResult(CustomSortOutData result) {
                        ((SortOutImpl.OnCancelSortOutListener) aty).onCancelSortOutSuccess(result);
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        ((SortOutImpl.OnCancelSortOutListener) aty).onCancelSortOutFailed(failString);
                    }
                });
    }
}
