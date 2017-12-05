package xm.cloudweight.presenter;

import android.content.Context;
import android.text.TextUtils;

import com.tencent.bugly.crashreport.CrashReport;
import com.xmzynt.storm.service.user.customer.CustomerLevel;
import com.xmzynt.storm.service.user.merchant.Merchant;
import com.xmzynt.storm.service.wms.stock.Stock;
import com.xmzynt.storm.service.wms.warehouse.Warehouse;
import com.xmzynt.storm.util.IMGType;
import com.xmzynt.storm.util.query.PageData;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
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
import xm.cloudweight.utils.bussiness.UploadPhotoUtil;

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
     * 上传图片
     */
    public static void uploadPhoto(final BaseActivity aty, String filePath) {
        if (!(aty instanceof CommImpl.OnUpLoadPhotoListener)) {
            return;
        }
        Merchant merchant = LocalSpUtil.getMerchant(aty);
        if (merchant == null || TextUtils.isEmpty(filePath)) {
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        String uuid = merchant.getUuid();
        HashMap<String, Object> map = new HashMap<>(2);
        map.put("uuid", uuid);
        String type = IMGType.GOODS_IMG.toString();
        map.put("IMG-TYPE", type);

        RequestBody requestBody =
                RequestBody.create(MediaType.parse("multipart/form-data;boundary=android"), UploadPhotoUtil.getFileByte(filePath));

        aty.getApiManager().uploadPhoto(map, requestBody)
                .compose(new TransformerHelper<ResponseEntity<String>>().get(aty))
                .subscribe(new ApiSubscribe<String>() {
                    @Override
                    protected void onResult(String result) {
                        ((CommImpl.OnUpLoadPhotoListener) aty).onUpLoadPhotoSuccess(result);
                    }

                    @Override
                    protected void onResultFail(int errorType, String failString) {
                        ((CommImpl.OnUpLoadPhotoListener) aty).onUpLoadPhotoFailed(errorType, failString);
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
}
