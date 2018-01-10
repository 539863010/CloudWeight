package xm.cloudweight.impl;

import com.xmzynt.storm.basic.idname.IdName;
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
     * 撤销入库
     */
    public interface OnCancelStockInListener {

        void onCancelStockInSuccess(String result);

        void onCancelStockInFailed(String message);

    }

    /**
     * 撤销入库
     */
    public interface OnGetDropdownOperatorListener {

        void onGetDropdownOperatorSuccess(List<IdName> result);

        void onGetDropdownOperatorFailed(String message);

    }


}
