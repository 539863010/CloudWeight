package xm.cloudweight.impl;

import com.xmzynt.storm.service.purchase.PurchaseData;

import java.util.List;

/**
 * @author wyh
 * @Description: 验收接口回调
 * @creat 2017/10/31
 */
public class CheckInImpl {

    /**
     * 分页查询采购订单
     */
    public interface OnQueryPurchaseDataListener {

        void onQueryPurchaseDataSuccess(List<PurchaseData> data);

        void onQueryPurchaseDataFailed(String message);

    }


    /**
     * 扫码获取采购信息
     */
    public interface OnScanToPurchaseDataListener {

        void onScanToPurchaseDataSuccess(PurchaseData purchaseBill);

        void onScanToPurchaseDataFailed(int errorType, String message);

    }

    /**
     * 入库
     */
    public interface OnStockInListener {

        void onStockInSuccess(String message,String imagePath);

        void onStockInFailed(String message);

    }

    /**
     * 越库
     */
    public interface OnCrossDockingListener {

        void onCrossDockingSuccess(List<String> result,String imagePath);

        void onCrossDockingFailed(String message);

    }

}
