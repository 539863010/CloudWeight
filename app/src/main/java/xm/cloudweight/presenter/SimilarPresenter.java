package xm.cloudweight.presenter;

import com.xmzynt.storm.service.goods.GoodsCategory;

import java.util.List;

import xm.cloudweight.api.ApiSubscribe;
import xm.cloudweight.api.ResponseEntity;
import xm.cloudweight.api.TransformerHelper;
import xm.cloudweight.base.BaseActivity;
import xm.cloudweight.bean.PBaseInfo;
import xm.cloudweight.impl.SimilarImpl;
import xm.cloudweight.utils.bussiness.BeanUtil;

/**
 * @author wyh
 * @Description: 出库，调拨，盘点
 * @creat 2017/11/2
 */
public class SimilarPresenter {

    /**
     * 查找类别
     */
    public static void getDropdownLeafCategory(final BaseActivity aty) {
        if (!(aty instanceof SimilarImpl.OnGetDropdownLeafCategoryListener)) {
            return;
        }
        PBaseInfo pBaseInfo = BeanUtil.getDropdownLeafCategory(aty);
        aty.getApiManager().getDropdownLeafCategory(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<List<GoodsCategory>>>().get(aty))
                .subscribe(new ApiSubscribe<List<GoodsCategory>>() {
                    @Override
                    protected void onResult(List<GoodsCategory> result) {
                        ((SimilarImpl.OnGetDropdownLeafCategoryListener) aty).onGetDropdownLeafCategorySuccess(result);
                    }

                    @Override
                    protected void onResultFail(int errorType,String failString) {
                        ((SimilarImpl.OnGetDropdownLeafCategoryListener) aty).onGetDropdownLeafCategoryFailed(errorType,failString);
                    }
                });

    }

//
//    /**
//     * 出库
//     */
//    public static void stockOut(final BaseActivity aty, StockOutRecord sor,final String path) {
//        if (!(aty instanceof SimilarImpl.OnStoreOutListener)) {
//            return;
//        }
//        PBaseInfo pBaseInfo = BeanUtil.stockOut(aty, sor);
//        aty.getApiManager().stockOut(pBaseInfo)
//                .compose(new TransformerHelper<ResponseEntity<String>>().get(aty))
//                .subscribe(new ApiSubscribe<String>() {
//                    @Override
//                    protected void onResult(String result) {
//                        ((SimilarImpl.OnStoreOutListener) aty).onStoreOutSuccess(result,path);
//                    }
//
//                    @Override
//                    protected void onResultFail(String failString) {
//                        ((SimilarImpl.OnStoreOutListener) aty).onStoreOutFailed(failString);
//                    }
//                });
//    }
//
//    /**
//     * 调拨
//     */
//    public static void allocate(final BaseActivity aty, AllocateRecord ar) {
//        if (!(aty instanceof SimilarImpl.OnAllocateListener)) {
//            return;
//        }
//        PBaseInfo pBaseInfo = BeanUtil.allocate(aty, ar);
//        LogUtils.e("------调拨----", GsonUtil.getGson().toJson(pBaseInfo));
//        aty.getApiManager().allocate(pBaseInfo)
//                .compose(new TransformerHelper<ResponseEntity<String>>().get(aty))
//                .subscribe(new ApiSubscribe<String>() {
//                    @Override
//                    protected void onResult(String result) {
//                        ((SimilarImpl.OnAllocateListener) aty).onAllocateSuccess(result);
//                    }
//
//                    @Override
//                    protected void onResultFail(String failString) {
//                        ((SimilarImpl.OnAllocateListener) aty).onAllocateFailed(failString);
//                    }
//                });
//    }
//
//    /**
//     * 盘点
//     */
//    public static void inventory(final BaseActivity aty, InventoryRecord ir) {
//        if (!(aty instanceof SimilarImpl.OnInventoryListener)) {
//            return;
//        }
//        PBaseInfo pBaseInfo = BeanUtil.inventory(aty, ir);
//        aty.getApiManager().inventory(pBaseInfo)
//                .compose(new TransformerHelper<ResponseEntity<String>>().get(aty))
//                .subscribe(new ApiSubscribe<String>() {
//                    @Override
//                    protected void onResult(String result) {
//                        ((SimilarImpl.OnInventoryListener) aty).onInventorySuccess(result);
//                    }
//
//                    @Override
//                    protected void onResultFail(String failString) {
//                        ((SimilarImpl.OnInventoryListener) aty).onInventoryFailed(failString);
//                    }
//                });
//    }
}
