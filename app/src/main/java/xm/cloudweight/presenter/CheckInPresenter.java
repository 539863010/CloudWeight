package xm.cloudweight.presenter;

import com.xmzynt.storm.service.purchase.PurchaseBill;
import com.xmzynt.storm.service.purchase.PurchaseData;
import com.xmzynt.storm.service.user.supplier.Supplier;
import com.xmzynt.storm.util.query.PageData;

import java.util.List;

import xm.cloudweight.api.ApiSubscribe;
import xm.cloudweight.api.ResponseEntity;
import xm.cloudweight.api.TransformerHelper;
import xm.cloudweight.base.BaseActivity;
import xm.cloudweight.bean.PBaseInfo;
import xm.cloudweight.impl.CheckInImpl;
import xm.cloudweight.utils.bussiness.BeanUtil;

/**
 * @author wyh
 * @Description: 验收接口
 * @creat 2017/10/31
 */
public class CheckInPresenter {

    /**
     * 获取提供商列表
     */
    public static void getListSupplier(final BaseActivity aty, int page, int pageSize, int defaultPageSize) {
        if (!(aty instanceof CheckInImpl.OnGetSuppliersListener)) {
            return;
        }
        PBaseInfo supplier = BeanUtil.getSupplier(aty, page, pageSize, defaultPageSize);
        aty.getApiManager().getDropdownSuppliers(supplier)
                .compose(new TransformerHelper<ResponseEntity<List<Supplier>>>().get(aty))
                .subscribe(new ApiSubscribe<List<Supplier>>() {
                    @Override
                    protected void onResult(List<Supplier> result) {
                        ((CheckInImpl.OnGetSuppliersListener) aty).getSuppliersSuccess(result);
                    }

                    @Override
                    protected void onResultFail(int errorType,String failString) {
                        ((CheckInImpl.OnGetSuppliersListener) aty).getSuppliersFailed(failString);
                    }
                });
    }

    /**
     * 分页查询采购订单
     */
    public static void queryPurchaseBill(final BaseActivity aty, int page, int pageSize, int defaultPageSize, String deliveryTime, String warehouseUuid) {
        if (!(aty instanceof CheckInImpl.OnQueryPurchaseBillListener)) {
            return;
        }
        PBaseInfo pBaseInfo = BeanUtil.queryPurchaseBill(aty, page, pageSize, defaultPageSize, deliveryTime, warehouseUuid);
        aty.getApiManager().queryPurchaseBill(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<PageData<PurchaseBill>>>().get(aty))
                .subscribe(new ApiSubscribe<PageData<PurchaseBill>>() {
                    @Override
                    protected void onResult(PageData<PurchaseBill> result) {
                        ((CheckInImpl.OnQueryPurchaseBillListener) aty).onQueryPurchaseBillSuccess(result);
                    }

                    @Override
                    protected void onResultFail(int errorType,String failString) {
                        ((CheckInImpl.OnQueryPurchaseBillListener) aty).onQueryPurchaseBillFailed(failString);
                    }

                });
    }

    /**
     * 查询采购订单
     */
    public static void getPurchaseBill(final BaseActivity aty, String uuid) {
        if (!(aty instanceof CheckInImpl.OnGetPurchaseBillListener)) {
            return;
        }
        PBaseInfo pBaseInfo = BeanUtil.getPurchaseBill(aty, uuid);
        aty.getApiManager().getPurchaseBill(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<PurchaseBill>>().get(aty))
                .subscribe(new ApiSubscribe<PurchaseBill>() {
                    @Override
                    protected void onResult(PurchaseBill result) {
                        ((CheckInImpl.OnGetPurchaseBillListener) aty).onGetPurchaseBillSuccess(result);
                    }

                    @Override
                    protected void onResultFail(int errorType,String failString) {
                        ((CheckInImpl.OnGetPurchaseBillListener) aty).onGetPurchaseBillFailed(failString);
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
                    protected void onResultFail(int errorType,String failString) {
                        ((CheckInImpl.OnScanToPurchaseDataListener) aty).onScanToPurchaseDataFailed(errorType,failString);
                    }

                });
    }

//    /**
//     * 入库
//     */
//    public static void stockIn(final BaseActivity aty, StockInRecord stockInRecord,final String path) {
//        if (!(aty instanceof CheckInImpl.OnStockInListener)) {
//            return;
//        }
//        if (stockInRecord == null) {
//            return;
//        }
//        PBaseInfo pBaseInfo = BeanUtil.stockIn(aty, stockInRecord);
//        aty.getApiManager().stockIn(pBaseInfo)
//                .compose(new TransformerHelper<ResponseEntity<String>>().get(aty))
//                .subscribe(new ApiSubscribe<String>() {
//                    @Override
//                    protected void onResult(String result) {
//                        ((CheckInImpl.OnStockInListener) aty).onStockInSuccess(result,path);
//                    }
//
//                    @Override
//                    protected void onResultFail(String failString) {
//                        ((CheckInImpl.OnStockInListener) aty).onStockInFailed(failString);
//                    }
//
//                });
//    }
//
//    /**
//     * 越库
//     */
//    public static void crossDocking(final BaseActivity aty, StockInRecord stockInRecord,final String path) {
//        if (!(aty instanceof CheckInImpl.OnCrossDockingListener)) {
//            return;
//        }
//        if (stockInRecord == null) {
//            return;
//        }
//        PBaseInfo pBaseInfo = BeanUtil.crossDocking(aty, stockInRecord);
//        aty.getApiManager().crossDocking(pBaseInfo)
//                .compose(new TransformerHelper<ResponseEntity<List<String>>>().get(aty))
//                .subscribe(new ApiSubscribe<List<String>>() {
//                    @Override
//                    protected void onResult(List<String> result) {
//                        ((CheckInImpl.OnCrossDockingListener) aty).onCrossDockingSuccess(result,path);
//                    }
//
//                    @Override
//                    protected void onResultFail(String failString) {
//                        ((CheckInImpl.OnCrossDockingListener) aty).onCrossDockingFailed(failString);
//                    }
//
//                });
//    }

}
