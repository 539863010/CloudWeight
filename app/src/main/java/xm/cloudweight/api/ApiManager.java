package xm.cloudweight.api;

import com.xmzynt.storm.service.goods.GoodsCategory;
import com.xmzynt.storm.service.purchase.PurchaseBill;
import com.xmzynt.storm.service.purchase.PurchaseData;
import com.xmzynt.storm.service.user.customer.CustomerLevel;
import com.xmzynt.storm.service.user.customer.MerchantCustomer;
import com.xmzynt.storm.service.user.merchant.Merchant;
import com.xmzynt.storm.service.user.supplier.Supplier;
import com.xmzynt.storm.service.wms.stock.Stock;
import com.xmzynt.storm.service.wms.warehouse.Warehouse;
import com.xmzynt.storm.util.query.PageData;

import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import rx.Observable;
import xm.cloudweight.bean.CustomSortOutData;
import xm.cloudweight.bean.PBaseInfo;
import xm.cloudweight.bean.PLogin;

/**
 * @author wyh
 * @Description: 接口请求api
 * @creat 2017/8/1
 */
@SuppressWarnings("AlibabaAbstractMethodOrInterfaceMethodMustUseJavadoc")
public interface ApiManager {

    String BASE_URL = "http://beta.shipments.atfresh.cn";

    /**
     * 登录
     *
     * @param p 登录请求体
     * @return Observable<ResponseEntity<Merchant>>
     */
    @POST("/mdata-controller/mdata/sso/login.do")
    Observable<ResponseEntity<Merchant>> login(@Body PLogin p);

    /**
     * 获取仓库列表
     *
     * @param p 获取仓库列表请求体
     * @return Observable<ResponseEntity<List<Warehouse>>>
     */
    @POST("/merchant/scaleApi/getDropDownWarehouse.do")
    Observable<ResponseEntity<List<Warehouse>>> getDropDownWareHouse(@Body PBaseInfo p);

    /**
     * 分页查询采购订单
     *
     * @param p 分页查询采购订单请求体
     * @return Observable<ResponseEntity<PageData<PurchaseBill>>>
     */
    @POST("/merchant/scaleApi/queryPurchaseBill.do")
    Observable<ResponseEntity<PageData<PurchaseBill>>> queryPurchaseBill(@Body PBaseInfo p);

    /**
     * 批量查询采购信息
     *
     * @param p 批量查询采购信息请求体
     * @return Observable<ResponseEntity<List<PurchaseData>>>
     */
    @POST("/merchant/scaleApi/queryPurchaseData.do")
    Observable<ResponseEntity<List<PurchaseData>>> queryPurchaseData(@Body PBaseInfo p);

    /**
     * 查询采购订单
     *
     * @param p 查询采购订单请求体
     * @return Observable<ResponseEntity<PurchaseBill>>
     */
    @POST("/merchant/scaleApi/getPurchaseBill.do")
    Observable<ResponseEntity<PurchaseBill>> getPurchaseBill(@Body PBaseInfo p);

    /**
     * 扫码获采购信息
     *
     * @param p 扫码获采购信息请求体
     * @return Observable<ResponseEntity<PurchaseData>>
     */
    @POST("/merchant/scaleApi/scanToPurchaseData.do")
    Observable<ResponseEntity<PurchaseData>> scanToPurchaseData(@Body PBaseInfo p);

    /**
     * 查询供应商列表
     *
     * @param p 查询供应商列表请求体
     * @return Observable<ResponseEntity<List<Supplier>>>
     */
    @POST("/merchant/scaleApi/getDropdownSuppliers.do")
    Observable<ResponseEntity<List<Supplier>>> getDropdownSuppliers(@Body PBaseInfo p);

    /**
     * 查询客户列表
     *
     * @param p 查询客户列表请求体
     * @return Observable<ResponseEntity<List<MerchantCustomer>>>
     */
    @POST("/merchant/scaleApi/getDropdownCustomers.do")
    Observable<ResponseEntity<List<MerchantCustomer>>> getDropdownCustomers(@Body PBaseInfo p);

    /**
     * 查询客户级别
     *
     * @param p 查询客户级别请求体
     * @return Observable<ResponseEntity<List<CustomerLevel>>>
     */
    @POST("/merchant/scaleApi/getDropdownLevels.do")
    Observable<ResponseEntity<List<CustomerLevel>>> getDropDownLevels(@Body PBaseInfo p);

    /**
     * 查询分拣数据（重量）
     *
     * @param p 查询分拣数据请求体
     * @return Observable<ResponseEntity<List<SortOutData>>>
     */
    @POST("/merchant/scaleApi/getsForWeigh.do")
    Observable<ResponseEntity<List<CustomSortOutData>>> getsForWeigh(@Body PBaseInfo p);

    /**
     * 查询分拣数据（数量）
     *
     * @param p 查询分拣数据请求体
     * @return Observable<ResponseEntity<List<SortOutData>>>
     */
    @POST("/merchant/scaleApi/getsForNotWeigh.do")
    Observable<ResponseEntity<List<CustomSortOutData>>> getsForNotWeigh(@Body PBaseInfo p);

    /**
     * 分拣
     *
     * @param p 分拣请求体
     * @return Observable<ResponseEntity<List<String>>>
     */
    @POST("/merchant/scaleApi/sortOut.do")
    Observable<ResponseEntity<List<String>>> sortOut(@Body PBaseInfo p);

    /**
     * 撤销分拣
     *
     * @param p 撤销分拣请求体
     * @return Observable<ResponseEntity<SortOutData>>
     */
    @POST("/merchant/scaleApi/cancelSortOut.do")
    Observable<ResponseEntity<CustomSortOutData>> cancelSortOut(@Body PBaseInfo p);

    /**
     * 出库，调拨，盘点- 获取类别列表
     *
     * @param p 获取类别列表请求体
     * @return Observable<ResponseEntity<List<GoodsCategory>>>
     */
    @POST("/merchant/scaleApi/getDropdownLeafCategory.do")
    Observable<ResponseEntity<List<GoodsCategory>>> getDropdownLeafCategory(@Body PBaseInfo p);

    /**
     * 入库
     *
     * @param p 入库请求体
     * @return Observable<ResponseEntity<String>>
     */
    @POST("/merchant/scaleApi/stockIn.do")
    Observable<ResponseEntity<String>> stockIn(@Body PBaseInfo p);

    /**
     * 越库
     *
     * @param p 越库请求体
     * @return Observable<ResponseEntity<List<String>>>
     */
    @POST("/merchant/scaleApi/crossDocking.do")
    Observable<ResponseEntity<List<String>>> crossDocking(@Body PBaseInfo p);

    /**
     * 越库调拨
     *
     * @param p 越库调拨请求体
     * @return Observable<ResponseEntity<List<String>>>
     */
    @POST("/merchant/scaleApi/crossAllocate.do")
    Observable<ResponseEntity<String>> crossAllocate(@Body PBaseInfo p);

    /**
     * 上传图片
     *
     * @param options     头部参数
     * @param requestBody 请求体
     * @return Observable<ResponseEntity<String>>
     */
    @POST("/mdata-controller/mdata/ossUtil/uploadPhoto.do")
    Observable<ResponseEntity<String>> uploadPhoto(@HeaderMap Map<String, Object> options,
                                                   @Body RequestBody requestBody);

    /**
     * 查询库存记录
     *
     * @param p 查询库存记录请求体
     * @return Observable<ResponseEntity<PageData<Stock>>>
     */
    @POST("/merchant/scaleApi/queryStock.do")
    Observable<ResponseEntity<PageData<Stock>>> queryStock(@Body PBaseInfo p);

    /**
     * 扫描库存标签
     *
     * @param p 扫描库存标签请求体
     * @return Observable<ResponseEntity<List<Stock>>>
     */
    @POST("/merchant/scaleApi/scanByTraceCode.do")
    Observable<ResponseEntity<List<Stock>>> scanByTraceCode(@Body PBaseInfo p);

    /**
     * 出库
     *
     * @param p 出库请求体
     * @return Observable<ResponseEntity<String>>
     */
    @POST("/merchant/scaleApi/stockOut.do")
    Observable<ResponseEntity<String>> stockOut(@Body PBaseInfo p);

    /**
     * 调拨
     *
     * @param p 调拨请求体
     * @return Observable<ResponseEntity<String>>
     */
    @POST("/merchant/scaleApi/allocate.do")
    Observable<ResponseEntity<String>> allocate(@Body PBaseInfo p);

    /**
     * 盘点
     *
     * @param p 盘点请求体
     * @return Observable<ResponseEntity<String>>
     */
    @POST("/merchant/scaleApi/inventory.do")
    Observable<ResponseEntity<String>> inventory(@Body PBaseInfo p);

    /**
     * 保存图片URL
     *
     * @param p 保存图片URL请求体
     * @return Observable<ResponseEntity<String>>
     */
    @POST("/merchant/scaleApi/saveImage.do")
    Observable<ResponseEntity<String>> saveImage(@Body PBaseInfo p);

}
