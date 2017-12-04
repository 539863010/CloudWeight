package xm.cloudweight.presenter;

import android.text.TextUtils;

import com.xmzynt.storm.service.user.merchant.Merchant;
import com.xmzynt.storm.service.wms.stock.Stock;
import com.xmzynt.storm.service.wms.warehouse.Warehouse;
import com.xmzynt.storm.util.IMGType;
import com.xmzynt.storm.util.query.PageData;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import xm.cloudweight.api.ApiSubscribe;
import xm.cloudweight.api.ResponseEntity;
import xm.cloudweight.api.TransformerHelper;
import xm.cloudweight.base.BaseActivity;
import xm.cloudweight.bean.PBaseInfo;
import xm.cloudweight.impl.CommImpl;
import xm.cloudweight.impl.SimilarImpl;
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
     * 获取日期列表
     */
    public static void getListDate(final BaseActivity aty) {
        if (!(aty instanceof CommImpl.OnGetDatesListener)) {
            return;
        }
        List<String> list = new ArrayList<>();
        list.add("2017-10-31");
        if (list.size() > 0) {
            ((CommImpl.OnGetDatesListener) aty).getDatesSuccess(list);
        } else {
            ((CommImpl.OnGetDatesListener) aty).getDatesFailed("日期列表为空");
        }
    }

    /**
     * 获取仓库列表
     */
    public static void getListWareHouse(final BaseActivity aty) {
        if (!(aty instanceof CommImpl.OnGetWareHousesListener)) {
            return;
        }
        PBaseInfo p = BeanUtil.getWareHouse(aty);
        aty.getApiManager()
                .getDropDownWareHouse(p)
                .compose(new TransformerHelper<ResponseEntity<List<Warehouse>>>().get(aty))
                .subscribe(new ApiSubscribe<List<Warehouse>>() {
                               @Override
                               protected void onResult(List<Warehouse> result) {
                                   ((CommImpl.OnGetWareHousesListener) aty).getWareHousesSuccess(result);
                               }

                               @Override
                               protected void onResultFail(int errorType,String failString) {
                                   ((CommImpl.OnGetWareHousesListener) aty).getWareHousesFailed(errorType,failString);
                               }
                           }
                );
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
                    protected void onResultFail(int errorType,String failString) {
                        ((CommImpl.OnUpLoadPhotoListener) aty).onUpLoadPhotoFailed(errorType,failString);
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
                    protected void onResultFail(int errorType,String failString) {
                        ((CommImpl.OnQueryStockListener) aty).onQueryStockFailed(errorType,failString);
                    }
                });

    }
}
