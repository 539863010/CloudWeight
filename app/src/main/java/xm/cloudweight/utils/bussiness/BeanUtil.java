package xm.cloudweight.utils.bussiness;

import android.content.Context;
import android.text.TextUtils;

import com.xmzynt.storm.service.user.merchant.Merchant;
import com.xmzynt.storm.service.wms.allocate.AllocateRecord;
import com.xmzynt.storm.service.wms.inventory.InventoryRecord;
import com.xmzynt.storm.service.wms.stockin.StockInRecord;
import com.xmzynt.storm.service.wms.stockout.StockOutRecord;
import com.xmzynt.storm.util.GsonUtil;
import com.xmzynt.storm.util.query.QueryFilter;

import java.util.Map;

import xm.cloudweight.bean.CustomSortOutData;
import xm.cloudweight.bean.PBaseInfo;
import xm.cloudweight.bean.PLogin;
import xm.cloudweight.comm.Common;

/**
 * @author wyh
 * @Description:
 * @creat 2017/10/30
 */
public class BeanUtil {

    private static void setBaseInfo(Context context, PBaseInfo p) {
        Merchant merchant = LocalSpUtil.getMerchant(context);
        setMerchantInfo(p, merchant);
    }

    private static void setMerchantInfo(PBaseInfo p, Merchant merchant) {
        if (merchant != null && p != null) {
            p.setPlatform(Common.PLAT_FORM);
            p.setUserIdentity(Common.USER_IDENTITY);
            p.setSessionId(merchant.getSessionId());
            p.setUserUuid(merchant.getUuid());
            p.setUserName(merchant.getName());
        }
    }

    /**
     * 登录请求参数
     */
    public static PLogin getPLogin(String name, String pwd) {
        PLogin pLogin = new PLogin();
        pLogin.setPlatform(Common.PLAT_FORM);
        PLogin.BodyBean body = new PLogin.BodyBean();
        body.setUserName(name);
        body.setPassword(pwd);
        body.setUserIdentity(Common.USER_IDENTITY);
        body.setCaptcha("");
        body.setService("http://localhost:8080/#/index/view");
        pLogin.setBody(body);
        return pLogin;
    }

    /**
     * 获取供应商列表
     */
    public static PBaseInfo getSupplier(Context ctx, int page, int pageSize, int defaultPageSize) {
        PBaseInfo p = new PBaseInfo();
        setBaseInfo(ctx, p);
        Map<String, Object> body = p.getBody();
        QueryFilter queryFilter = new QueryFilter();
        queryFilter.setPage(page);
        queryFilter.setPageSize(pageSize);
        queryFilter.setDefaultPageSize(defaultPageSize);
        Map<String, Object> params = queryFilter.getParams();
        params.put("uuid", p.getUserUuid());
        queryFilter.setParams(params);
        body.put("queryFilter", queryFilter);
        p.setBody(body);
        return p;
    }

    /**
     * 分页查询采购订单
     */
    public static PBaseInfo queryPurchaseBill(Context ctx, int page, int pageSize, int defaultPageSize, String deliveryTime, String warehouseUuid) {
        PBaseInfo p = new PBaseInfo();
        setBaseInfo(ctx, p);
        Map<String, Object> body = p.getBody();
        QueryFilter queryFilter = new QueryFilter();
        queryFilter.setPage(page);
        queryFilter.setPageSize(pageSize);
        queryFilter.setDefaultPageSize(defaultPageSize);
        Map<String, Object> params = queryFilter.getParams();
        if (!TextUtils.isEmpty(deliveryTime)) {
            params.put("deliveryTime", deliveryTime);
        }
        if (!TextUtils.isEmpty(warehouseUuid)) {
            params.put("warehouseUuid", warehouseUuid);
        }
        //11.16更新
        params.put("initial", "initial");
        params.put("complete", "complete");
        params.put("abort", "abort");
        body.put("queryFilter", queryFilter);
        queryFilter.setParams(params);
        p.setBody(body);
        return p;
    }

    /**
     * 查询采购订单
     */
    public static PBaseInfo getPurchaseBill(Context ctx, String uuid) {
        PBaseInfo p = new PBaseInfo();
        setBaseInfo(ctx, p);
        Map<String, Object> body = p.getBody();
        body.put("uuid", uuid);
        p.setBody(body);
        return p;
    }

    /**
     * 获取仓库列表
     */
    public static PBaseInfo getWareHouse(Merchant merchant) {
        PBaseInfo pBaseInfo = new PBaseInfo();
        setMerchantInfo(pBaseInfo, merchant);
        return pBaseInfo;
    }


    /**
     * 查询客户列表
     */
    public static PBaseInfo getMerchantCustomer(Context ctx, int page, int pageSize, int defaultPageSize) {
        PBaseInfo pBaseInfo = new PBaseInfo();
        setBaseInfo(ctx, pBaseInfo);
        Map<String, Object> body = pBaseInfo.getBody();
        QueryFilter queryFilter = new QueryFilter();
        queryFilter.setPage(page);
        queryFilter.setPageSize(pageSize);
        queryFilter.setDefaultPageSize(defaultPageSize);
        body.put("queryFilter", queryFilter);
        pBaseInfo.setBody(body);
        return pBaseInfo;
    }

    /**
     * 查询客户级别
     */
    public static PBaseInfo getDropDownLevels(Merchant merchant) {
        PBaseInfo pBaseInfo = new PBaseInfo();
        setMerchantInfo(pBaseInfo, merchant);
        return pBaseInfo;
    }

    /**
     * 查询分拣数据
     */
    public static PBaseInfo getSourOutList(Context ctx, int page, int pageSize, int defaultPageSize, String deliveryTime) {
        PBaseInfo pBaseInfo = new PBaseInfo();
        setBaseInfo(ctx, pBaseInfo);
        Map<String, Object> body = pBaseInfo.getBody();
        QueryFilter queryFilter = new QueryFilter();
        queryFilter.setOrders(null);
        queryFilter.setPage(page);
        queryFilter.setPageSize(pageSize);
        queryFilter.setDefaultPageSize(defaultPageSize);
        Map<String, Object> params = queryFilter.getParams();
        params.put("deliveryTime", deliveryTime);
        body.put("queryFilter", queryFilter);
        pBaseInfo.setBody(body);
        return pBaseInfo;
    }

    /**
     * 分拣
     */
    public static PBaseInfo sortOut(Merchant merchant, CustomSortOutData data) {
        PBaseInfo pBaseInfo = new PBaseInfo();
        setMerchantInfo(pBaseInfo, merchant);
        Map<String, Object> body = pBaseInfo.getBody();
        body.put("sortOutData", GsonUtil.getGson().toJson(data));
        pBaseInfo.setBody(body);
        return pBaseInfo;
    }

    /**
     * 撤销分拣
     */
    public static PBaseInfo cancelSortOut(Context ctx, CustomSortOutData data) {
        PBaseInfo pBaseInfo = new PBaseInfo();
        setBaseInfo(ctx, pBaseInfo);
        Map<String, Object> body = pBaseInfo.getBody();
        body.put("sortOutData", GsonUtil.getGson().toJson(data));
        pBaseInfo.setBody(body);
        return pBaseInfo;
    }

    /**
     * 获取类别列表
     */
    public static PBaseInfo getDropdownLeafCategory(Context ctx) {
        PBaseInfo pBaseInfo = new PBaseInfo();
        setBaseInfo(ctx, pBaseInfo);
        return pBaseInfo;
    }

    /**
     * 入库
     */
    public static PBaseInfo stockIn(Merchant merchant, StockInRecord stockInRecord) {
        PBaseInfo pBaseInfo = new PBaseInfo();
        setMerchantInfo(pBaseInfo,merchant);
        Map<String, Object> body = pBaseInfo.getBody();
        body.put("stockInRecord", GsonUtil.getGson().toJson(stockInRecord));
        return pBaseInfo;
    }

    /**
     * 越库
     */
    public static PBaseInfo crossDocking(Merchant merchant, StockInRecord stockInRecord) {
        PBaseInfo pBaseInfo = new PBaseInfo();
        setMerchantInfo(pBaseInfo,merchant);
        Map<String, Object> body = pBaseInfo.getBody();
        body.put("stockInRecord", GsonUtil.getGson().toJson(stockInRecord));
        return pBaseInfo;
    }

    /**
     * 扫描获取订单信息
     */
    public static PBaseInfo scanToPurchaseData(Context ctx, String uuid) {
        PBaseInfo pBaseInfo = new PBaseInfo();
        setBaseInfo(ctx, pBaseInfo);
        Map<String, Object> body = pBaseInfo.getBody();
        body.put("uuid", uuid);
        return pBaseInfo;
    }

    /**
     * 查询库存列表
     */
    public static PBaseInfo queryStock(Context ctx, int page, int pageSize, int defaultPageSize, String basketCode) {
        PBaseInfo pBaseInfo = new PBaseInfo();
        setBaseInfo(ctx, pBaseInfo);
        Map<String, Object> body = pBaseInfo.getBody();
        QueryFilter queryFilter = new QueryFilter();
        queryFilter.setPage(page);
        queryFilter.setPageSize(pageSize);
        queryFilter.setDefaultPageSize(defaultPageSize);
        queryFilter.setOrders(null);
        Map<String, Object> params = queryFilter.getParams();
        params.put("moreThanZero", true);
        params.put("basketCode", basketCode);
        body.put("queryFilter", queryFilter);
        return pBaseInfo;
    }

    /**
     * 出库
     */
    public static PBaseInfo stockOut(Merchant merchant, StockOutRecord sor) {
        PBaseInfo pBaseInfo = new PBaseInfo();
        setMerchantInfo(pBaseInfo, merchant);
        Map<String, Object> body = pBaseInfo.getBody();
        body.put("stockOutRecord", GsonUtil.getGson().toJson(sor));
        return pBaseInfo;
    }

    /**
     * 调拨
     */
    public static PBaseInfo allocate(Merchant merchant, AllocateRecord ar) {
        PBaseInfo pBaseInfo = new PBaseInfo();
        setMerchantInfo(pBaseInfo, merchant);
        Map<String, Object> body = pBaseInfo.getBody();
        body.put("allocateRecord", GsonUtil.getGson().toJson(ar));
        return pBaseInfo;
    }

    /**
     * 盘点
     */
    public static PBaseInfo inventory(Merchant merchant, InventoryRecord ir) {
        PBaseInfo pBaseInfo = new PBaseInfo();
        setMerchantInfo(pBaseInfo, merchant);
        Map<String, Object> body = pBaseInfo.getBody();
        body.put("inventoryRecord", GsonUtil.getGson().toJson(ir));
        return pBaseInfo;
    }

    /**
     * 保存图片
     */
    public static PBaseInfo saveImage(Merchant merchant, String uuid, String imageUrl, String type) {
        PBaseInfo pBaseInfo = new PBaseInfo();
        setMerchantInfo(pBaseInfo, merchant);
        Map<String, Object> body = pBaseInfo.getBody();
        body.put("uuid", uuid);
        body.put("image", imageUrl);
        body.put("type", type);
        return pBaseInfo;
    }

}
