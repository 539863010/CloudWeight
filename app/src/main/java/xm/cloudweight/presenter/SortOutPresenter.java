package xm.cloudweight.presenter;

import com.xmzynt.storm.service.user.customer.MerchantCustomer;

import java.util.List;

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
                    protected void onResultFail(int errorType, String failString) {
                        ((SortOutImpl.OnGetMerchantCustomerListener) aty).getMerchantCustomerFailed(errorType, failString);
                    }

                    @Override
                    protected void onResult(List<MerchantCustomer> result) {
                        ((SortOutImpl.OnGetMerchantCustomerListener) aty).getMerchantCustomerSuccess(result);
                    }
                });
    }

}
