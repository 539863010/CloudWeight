package xm.cloudweight.impl;

import com.xmzynt.storm.service.goods.GoodsCategory;
import com.xmzynt.storm.service.wms.stock.Stock;
import com.xmzynt.storm.util.query.PageData;

import java.util.List;

/**
 * @author wyh
 * @Description: 出库，调拨，盘点接口
 * @creat 2017/11/2
 */
public class SimilarImpl {

    /**
     * 获取类别列表接口
     */
    public interface OnGetDropdownLeafCategoryListener {

        void onGetDropdownLeafCategorySuccess(List<GoodsCategory> result);

        void onGetDropdownLeafCategoryFailed(int errorType, String failString);

    }


    /**
     * 出库
     */
    public interface OnStoreOutListener {

        void onStoreOutSuccess(String result,String imagePath);

        void onStoreOutFailed(String failString);

    }

    /**
     * 调拨
     */
    public interface OnAllocateListener {

        void onAllocateSuccess(String result);

        void onAllocateFailed(String failString);

    }

    /**
     * 盘点
     */
    public interface OnInventoryListener {

        void onInventorySuccess(String result);

        void onInventoryFailed(String failString);

    }

}
