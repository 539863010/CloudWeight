package xm.cloudweight.presenter;

import com.xmzynt.storm.service.goods.GoodsCategory;

import java.util.List;

import rx.Observable;
import xm.cloudweight.api.ApiSubscribe;
import xm.cloudweight.api.ResponseEntity;
import xm.cloudweight.api.TransformerHelper;
import xm.cloudweight.base.BaseActivity;
import xm.cloudweight.bean.PBaseInfo;
import xm.cloudweight.comm.Common;
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
                    protected void onResultFail(int errorType, String failString) {
                        ((SimilarImpl.OnGetDropdownLeafCategoryListener) aty).onGetDropdownLeafCategoryFailed(errorType, failString);
                    }
                });

    }

    /**
     * 扫码获取采购信息
     */
    public static void cancelSimilar(final BaseActivity aty, String uuid, int type) {
        if (!(aty instanceof SimilarImpl.OnCancelSimilarListener)) {
            return;
        }
        PBaseInfo pBaseInfo = BeanUtil.cancelSimilar(aty, uuid);
        Observable<ResponseEntity<String>> observable = null;
        if (type == Common.DbType.TYPE_STORE_OUT) {
            //出库
            observable = aty.getApiManager().cancelStockOut(pBaseInfo);
        } else if (type == Common.DbType.TYPE_ALLOCATE) {
            //调拨
            observable = aty.getApiManager().cancelAllocate(pBaseInfo);
        }
        if (observable == null) {
            return;
        }
        observable.compose(new TransformerHelper<ResponseEntity<String>>().get(aty))
                .subscribe(new ApiSubscribe<String>() {
                    @Override
                    protected void onResult(String result) {
                        ((SimilarImpl.OnCancelSimilarListener) aty).onCancelSimilarSuccess(result);
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        ((SimilarImpl.OnCancelSimilarListener) aty).onCancelSimilarFailed(failString);
                    }

                });
    }
}
