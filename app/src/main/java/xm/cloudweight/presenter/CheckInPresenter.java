package xm.cloudweight.presenter;

import com.xmzynt.storm.service.purchase.PurchaseData;

import java.util.List;

import xm.cloudweight.api.ApiSubscribe;
import xm.cloudweight.api.ResponseEntity;
import xm.cloudweight.api.TransformerHelper;
import xm.cloudweight.base.BaseActivity;
import xm.cloudweight.bean.PBaseInfo;
import xm.cloudweight.comm.Common;
import xm.cloudweight.impl.CheckInImpl;
import xm.cloudweight.utils.bussiness.BeanUtil;

/**
 * @author wyh
 * @Description: 验收接口
 * @creat 2017/10/31
 */
public class CheckInPresenter {

    /**
     * 查询查询采购信息
     */
    public static void queryPurchaseData(final BaseActivity aty, int page, int pageSize, int defaultPageSize, String deliveryTime) {
        if (!(aty instanceof CheckInImpl.OnQueryPurchaseDataListener)) {
            return;
        }
        PBaseInfo pBaseInfo = BeanUtil.queryPurchaseData(aty, page, pageSize, defaultPageSize, deliveryTime);
        aty.getApiManager().queryPurchaseData(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<List<PurchaseData>>>().get(aty))
                .subscribe(new ApiSubscribe<List<PurchaseData>>() {
                    @Override
                    protected void onResult(List<PurchaseData> result) {
                        ((CheckInImpl.OnQueryPurchaseDataListener) aty).onQueryPurchaseDataSuccess(result);
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        ((CheckInImpl.OnQueryPurchaseDataListener) aty).onQueryPurchaseDataFailed(failString);
                    }

                });
    }

    /**
     * 扫码获取采购信息
     */
    public static void scanToPurchaseData(final BaseActivity aty, String uuid) {
        if (!(aty instanceof CheckInImpl.OnScanToPurchaseDataListener)) {
            return;
        }
        PBaseInfo pBaseInfo = BeanUtil.scanToPurchaseData(aty, uuid);
        aty.getApiManager().scanToPurchaseData(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<PurchaseData>>().get(aty))
                .subscribe(new ApiSubscribe<PurchaseData>() {
                    @Override
                    protected void onResult(PurchaseData result) {
                        ((CheckInImpl.OnScanToPurchaseDataListener) aty).onScanToPurchaseDataSuccess(result);
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        ((CheckInImpl.OnScanToPurchaseDataListener) aty).onScanToPurchaseDataFailed(errorType, failString);
                    }

                });
    }

    /**
     * 扫码获取采购信息
     */
    public static void cancelStockIn(final BaseActivity aty, String uuid, int type) {
        if (!(aty instanceof CheckInImpl.OnCancelStockInListener)) {
            return;
        }
        String stockInType;
        if (type == Common.DbType.TYPE_ChECK_IN_CROSS_OUT) {
            stockInType = "crossDock";
        } else {
            stockInType = "purchaseIn";
        }
        PBaseInfo pBaseInfo = BeanUtil.cancelStockIn(aty, uuid, stockInType);
        aty.getApiManager().cancelStockIn(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<String>>().get(aty))
                .subscribe(new ApiSubscribe<String>() {
                    @Override
                    protected void onResult(String result) {
                        ((CheckInImpl.OnCancelStockInListener) aty).onCancelStockInSuccess(result);
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        ((CheckInImpl.OnCancelStockInListener) aty).onCancelStockInFailed(failString);
                    }

                });
    }

}
