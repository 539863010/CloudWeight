package xm.cloudweight.impl;

import com.xmzynt.storm.service.wms.stock.Stock;
import com.xmzynt.storm.util.query.PageData;

import java.util.List;

/**
 * @author wyh
 * @Description: 通用网络请求回调接口
 * @creat 2017/10/31
 */
public class CommImpl {

    /**
     * 获取时间列表监听
     */
    public interface OnGetDatesListener {

        void getDatesSuccess(List<String> list);

        void getDatesFailed(String message);

    }

    /**
     * 上传图片
     */
    public interface OnUpLoadPhotoListener {

        void onUpLoadPhotoSuccess(String result);

        void onUpLoadPhotoFailed(int errorType, String message);

    }

    /**
     * 查询库存记录
     */
    public interface OnQueryStockListener {

        void onQueryStockSuccess(PageData<Stock> result);

        void onQueryStockFailed(int errorType, String failString);

    }

    /**
     * 查询库存记录
     */
    public interface OnScanByTraceCodeListener {

        void onScanByTraceCodeSuccess(List<Stock> result);

        void onScanByTraceCodeFailed(int errorType, String failString);

    }

}
