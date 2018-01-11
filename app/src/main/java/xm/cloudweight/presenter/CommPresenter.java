package xm.cloudweight.presenter;

import android.content.Context;

import com.tencent.bugly.crashreport.CrashReport;
import com.xmzynt.storm.service.user.customer.CustomerLevel;
import com.xmzynt.storm.service.user.merchant.Merchant;
import com.xmzynt.storm.service.wms.stock.Stock;
import com.xmzynt.storm.service.wms.warehouse.Warehouse;
import com.xmzynt.storm.util.query.PageData;

import java.util.List;

import xm.cloudweight.api.ApiSubscribe;
import xm.cloudweight.api.ResponseEntity;
import xm.cloudweight.api.TransformerHelper;
import xm.cloudweight.app.App;
import xm.cloudweight.base.BaseActivity;
import xm.cloudweight.bean.PBaseInfo;
import xm.cloudweight.impl.CommImpl;
import xm.cloudweight.utils.LogUtils;
import xm.cloudweight.utils.bussiness.BeanUtil;
import xm.cloudweight.utils.bussiness.LocalSpUtil;

/**
 * @author wyh
 * @Description: 通用接口
 * @creat 2017/10/31
 */
public class CommPresenter {

    /**
     * 获取仓库列表
     */
    public static void getListWareHouse(final Context ctx, Merchant merchant) {
        if (merchant == null || ctx == null) {
            return;
        }
        PBaseInfo p = BeanUtil.getWareHouse(merchant);
        App.getApiManager(ctx)
                .getDropDownWareHouse(p)
                .compose(new TransformerHelper<ResponseEntity<List<Warehouse>>>().get())
                .subscribe(new ApiSubscribe<List<Warehouse>>() {
                               @Override
                               protected void onResult(List<Warehouse> result) {
                                   LocalSpUtil.setListWareHouse(ctx, result);
                               }

                               @Override
                               protected void onResultFail(int errorType, String failString) {
                                   LogUtils.e("获取仓库列表失败", failString);
                                   CrashReport.postCatchedException(new Exception("获取仓库列表失败 : failString = " + failString));
                               }
                           }
                );
    }

    /**
     * 查询客户等级
     */
    public static void getDropDownLevels(final Context ctx, Merchant merchant) {
        PBaseInfo p = BeanUtil.getDropDownLevels(merchant);
        App.getApiManager(ctx).getDropDownLevels(p)
                .compose(new TransformerHelper<ResponseEntity<List<CustomerLevel>>>().get())
                .subscribe(new ApiSubscribe<List<CustomerLevel>>() {

                    @Override
                    protected void onResult(List<CustomerLevel> result) {
                        LocalSpUtil.setListCustomerLevel(ctx, result);
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        LogUtils.e("获取客户等级列表失败", failString);
                        CrashReport.postCatchedException(new Exception("获取客户等级列表失败 : failString = " + failString));
                    }

                });
    }

    /**
     * 查找库存记录
     */
    public static void queryStock(final BaseActivity aty, int page, int pageSize, int defaultPageSize, String basketCode) {
        if (!(aty instanceof CommImpl.OnQueryStockListener)) {
            return;
        }
        PBaseInfo pBaseInfo = BeanUtil.queryStock(aty, page, pageSize, defaultPageSize, basketCode);
        aty.getApiManager().queryStock(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<PageData<Stock>>>().get(aty))
                .subscribe(new ApiSubscribe<PageData<Stock>>() {
                    @Override
                    protected void onResult(PageData<Stock> result) {
                        ((CommImpl.OnQueryStockListener) aty).onQueryStockSuccess(result);
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        ((CommImpl.OnQueryStockListener) aty).onQueryStockFailed(errorType, failString);
                    }
                });

    }

    /**
     * 扫描库存标签
     */
    public static void scanByTraceCode(final BaseActivity aty, String traceCode, String warehouseUU) {
        if (!(aty instanceof CommImpl.OnScanByTraceCodeListener)) {
            return;
        }
        PBaseInfo pBaseInfo = BeanUtil.scanByTraceCode(aty, traceCode, warehouseUU);
        aty.getApiManager().scanByTraceCode(pBaseInfo)
                .compose(new TransformerHelper<ResponseEntity<List<Stock>>>().get(aty))
                .subscribe(new ApiSubscribe<List<Stock>>() {
                    @Override
                    protected void onResult(List<Stock> result) {
                        ((CommImpl.OnScanByTraceCodeListener) aty).onScanByTraceCodeSuccess(result);
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        ((CommImpl.OnScanByTraceCodeListener) aty).onScanByTraceCodeFailed(errorType, failString);
                    }
                });

    }
}
