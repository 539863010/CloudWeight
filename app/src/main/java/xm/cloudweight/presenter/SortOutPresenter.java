package xm.cloudweight.presenter;

import com.xmzynt.storm.service.sort.SortOutData;
import com.xmzynt.storm.service.user.customer.CustomerLevel;
import com.xmzynt.storm.service.user.customer.MerchantCustomer;

import java.util.List;

import rx.Observable;
import xm.cloudweight.SortOutActivity;
import xm.cloudweight.api.ApiSubscribe;
import xm.cloudweight.api.ResponseEntity;
import xm.cloudweight.api.TransformerHelper;
import xm.cloudweight.base.BaseActivity;
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
                    protected void onResultFail(int errorType,String failString) {
                        ((SortOutImpl.OnGetMerchantCustomerListener) aty).getMerchantCustomerFailed(errorType,failString);
                    }

                    @Override
                    protected void onResult(List<MerchantCustomer> result) {
                        ((SortOutImpl.OnGetMerchantCustomerListener) aty).getMerchantCustomerSuccess(result);
                    }
                });
    }

    /**
     * 查询客户等级
     */
    public static void getDropDownLevels(final BaseActivity aty) {
        if (!(aty instanceof SortOutImpl.OnGetDropDownLevelsListener)) {
            return;
        }
        PBaseInfo p = BeanUtil.getDropDownLevels(aty);
        aty.getApiManager().getDropDownLevels(p)
                .compose(new TransformerHelper<ResponseEntity<List<CustomerLevel>>>().get(aty))
                .subscribe(new ApiSubscribe<List<CustomerLevel>>() {
                    @Override
                    protected void onResultFail(int errorType,String failString) {
                        ((SortOutImpl.OnGetDropDownLevelsListener) aty).getDropDownLevelsFailed(errorType,failString);
                    }

                    @Override
                    protected void onResult(List<CustomerLevel> result) {
                        ((SortOutImpl.OnGetDropDownLevelsListener) aty).getDropDownLevelsSuccess(result);
                    }
                });
    }

    /**
     * 获取查询列表
     */
    public static void getSourOutList(final BaseActivity aty, final int type, int page, int pageSize, int defaultPageSize, String deliveryTime) {
        if (!(aty instanceof SortOutImpl.OnGetSortOutListListener)) {
            return;
        }
        PBaseInfo pBaseInfo = BeanUtil.getSourOutList(aty, page, pageSize, defaultPageSize, deliveryTime);
        Observable<ResponseEntity<List<SortOutData>>> observable;
        if (type == SortOutActivity.TYPE_WEIGHT) {
            //重量
            observable = aty.getApiManager().getsForWeigh(pBaseInfo);
        } else {
            //数量
            observable = aty.getApiManager().getsForNotWeigh(pBaseInfo);
        }
        observable.compose(new TransformerHelper<ResponseEntity<List<SortOutData>>>().get(aty))
                .subscribe(new ApiSubscribe<List<SortOutData>>() {
                    @Override
                    protected void onResult(List<SortOutData> result) {
                        ((SortOutImpl.OnGetSortOutListListener) aty).getSortOutListSuccess(type, result);
                    }

                    @Override
                    protected void onResultFail(int errorType,String failString) {
                        ((SortOutImpl.OnGetSortOutListListener) aty).getSortOutListFailed(type, failString);
                    }
                });
    }

//    /**
//     * 分拣
//     */
//    public static void sortOut(final BaseActivity aty, SortOutData data) {
//        if (!(aty instanceof SortOutImpl.OnSortOutListener)) {
//            return;
//        }
//        PBaseInfo pBaseInfo = BeanUtil.sortOut(aty, data);
//        aty.getApiManager().sortOut(pBaseInfo)
//                .compose(new TransformerHelper<ResponseEntity<SortOutData>>().get(aty))
//                .subscribe(new ApiSubscribe<SortOutData>() {
//                    @Override
//                    protected void onResult(SortOutData result) {
//                        ((SortOutImpl.OnSortOutListener) aty).onSortOutSuccess(result);
//                    }
//
//                    @Override
//                    protected void onResultFail(String failString) {
//                        ((SortOutImpl.OnSortOutListener) aty).onSortOutFailed(failString);
//                    }
//                });
//
//    }

    public static void cancelSortOut(final BaseActivity aty, SortOutData data) {
        if (!(aty instanceof SortOutImpl.OnCancelSortOutListener)) {
            return;
        }
        PBaseInfo pBaseInfo = BeanUtil.cancelSortOut(aty, data);
        aty.getApiManager().cancelSortOut(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<SortOutData>>().get(aty))
                .subscribe(new ApiSubscribe<SortOutData>() {
                    @Override
                    protected void onResult(SortOutData result) {
                        ((SortOutImpl.OnCancelSortOutListener) aty).onCancelSortOutSuccess(result);
                    }

                    @Override
                    protected void onResultFail(int errorType,String failString) {
                        ((SortOutImpl.OnCancelSortOutListener) aty).onCancelSortOutFailed(failString);
                    }
                });
    }
}