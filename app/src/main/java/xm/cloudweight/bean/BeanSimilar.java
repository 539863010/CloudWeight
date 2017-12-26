package xm.cloudweight.bean;

import android.content.Context;
import android.widget.EditText;

import com.xmzynt.storm.basic.idname.IdName;
import com.xmzynt.storm.basic.operateinfo.OperateInfo;
import com.xmzynt.storm.basic.ucn.UCN;
import com.xmzynt.storm.service.user.merchant.Merchant;
import com.xmzynt.storm.service.wms.allocate.AllocateRecord;
import com.xmzynt.storm.service.wms.inventory.InventoryRecord;
import com.xmzynt.storm.service.wms.stock.Stock;
import com.xmzynt.storm.service.wms.stockout.StockOutRecord;
import com.xmzynt.storm.service.wms.stockout.StockOutType;
import com.xmzynt.storm.service.wms.warehouse.Warehouse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import xm.cloudweight.comm.Common;
import xm.cloudweight.utils.bussiness.LocalSpUtil;
import xm.cloudweight.widget.DataSpinner;

/**
 * @author wyh
 * @Description: 创建 入库-调拨-盘点 请求体
 * @creat 2017/11/7
 */
public class BeanSimilar {

    /**
     * 出库
     *
     * @param ctx             上下文
     * @param mStock          Stock对象
     * @param mSpTypeStockOut 出库类型
     * @param countSub        出库数量（重量）
     * @param leather         扣皮数量（重量）
     * @param deduct          扣重数量（重量）
     * @return StockOutRecord
     */
    public static StockOutRecord createStoreOutRecord(Context ctx,
                                                      Stock mStock,
                                                      DataSpinner<StockOutType> mSpTypeStockOut,
                                                      BigDecimal countSub,
                                                      BigDecimal leather,
                                                      BigDecimal deduct) {
        StockOutRecord sor = new StockOutRecord();
        sor.setOrg(mStock.getOrg());
        sor.setStockOutType(mSpTypeStockOut.getSelectedItem());
        sor.setWarehouse(mStock.getWarehouse());
        sor.setGoods(mStock.getGoods());
        sor.setGoodsSpec(mStock.getGoodsSpec());
        sor.setGoodsUnit(mStock.getGoodsUnit());
        sor.setRemark(mStock.getRemark());
        sor.setTraceCode(mStock.getTraceCode());
        sor.setMaterials(mStock.getMaterials());
        sor.setPlatformBatchCode(mStock.getPlatformBatchCode());
        sor.setOutDate(new Date());
        sor.setStockUuid(mStock.getUuid());
        sor.setLeatherQty(leather);
        sor.setDeductQty(deduct);
        // 出库数量   净重（当前重量-扣皮-扣重）
        BigDecimal weightCoefficient = mStock.getWeightCoefficient();
        if (weightCoefficient != null) {
            sor.setQuantity(countSub.divide(weightCoefficient, RoundingMode.HALF_EVEN));
        } else {
            sor.setQuantity(countSub);
        }
        //  保存成功的url
//        ArrayList<String> images = new ArrayList<>();
//        images.add(imageUrl);
//        sor.setImages(images);
        //  设备号（eg. 001 /002/...） + 年月日时间到毫秒
        sor.setPlatformTraceCode(Common.getPlatformTraceCode());
        // 2017/11/7  “ 加个sdStockOutQty  随便数值 就不会报错  现在更新不了   你可以先加上测试下”
        sor.setSdStockOutQty(new BigDecimal(11));
        return sor;
    }

    /**
     * 调拨
     *
     * @param ctx            上下文
     * @param mStock         Stock
     * @param mSpWareHouseIn 调拨仓库
     * @param countSub       调拨数量
     * @param leather        扣皮数量
     * @param deduct         扣重数量
     * @return AllocateRecord
     */
    public static AllocateRecord createAllocateRecord(Context ctx,
                                                      Stock mStock,
                                                      DataSpinner<Warehouse> mSpWareHouseIn,
                                                      BigDecimal countSub,
                                                      BigDecimal leather,
                                                      BigDecimal deduct) {
        AllocateRecord ar = new AllocateRecord();
        ar.setOrg(mStock.getOrg());
        ar.setGoods(mStock.getGoods());
        ar.setGoodsUnit(mStock.getGoodsUnit());
        ar.setGoodsSpec(mStock.getGoodsSpec());
        ar.setRemark(mStock.getRemark());
        ar.setSourceStockUuid(mStock.getUuid());
        ar.setLeatherQty(leather);
        ar.setDeductQty(deduct);
        //出库仓库
        ar.setOutWarehouse(mStock.getWarehouse());
        //调入仓库    仓库列表  不能与出库仓库 重复
        Warehouse wareHouseIn = mSpWareHouseIn.getSelectedItem();
        ar.setInWarehouse(new UCN(wareHouseIn.getUuid(), wareHouseIn.getCode(), wareHouseIn.getName()));
        BigDecimal weightCoefficient = mStock.getWeightCoefficient();
        if (weightCoefficient != null) {
            ar.setAllocateQty(countSub.divide(weightCoefficient, RoundingMode.HALF_EVEN));
        } else {
            ar.setAllocateQty(countSub);
        }
        // 当前操作人   时间为当前时间
        Merchant merchant = LocalSpUtil.getMerchant(ctx);
        if (merchant != null) {
            OperateInfo info = new OperateInfo();
            info.setOperateTime(new Date());
            info.setOperator(new IdName(merchant.getUuid(), merchant.getName()));
            ar.setOperateInfo(info);
        }
        return ar;
    }

    /**
     * 盘点
     *
     * @param ctx      上下文
     * @param mStock   Stock
     * @param mEtCount 盘点数量
     * @return InventoryRecord
     */
    public static InventoryRecord createInventoryRecord(Context ctx, Stock mStock, EditText mEtCount) {
        InventoryRecord ir = new InventoryRecord();
        ir.setOrg(mStock.getOrg());
        ir.setGoods(mStock.getGoods());
        ir.setGoodsUnit(mStock.getGoodsUnit());
        ir.setWarehouse(mStock.getWarehouse());
        ir.setRemark(mStock.getRemark());
        ir.setSourceStockUuid(mStock.getUuid());
        ir.setStockQty(mStock.getAmount());
        //实际数量
        BigDecimal countSub = new BigDecimal(mEtCount.getText().toString().trim());
        BigDecimal weightCoefficient = mStock.getWeightCoefficient();
        if (weightCoefficient != null) {
            ir.setInventoryQty(countSub.divide(weightCoefficient, RoundingMode.HALF_EVEN));
        } else {
            ir.setInventoryQty(countSub);
        }
        // 当前操作人   时间为当前时间
        Merchant merchant = LocalSpUtil.getMerchant(ctx);
        if (merchant != null) {
            OperateInfo info = new OperateInfo();
            info.setOperateTime(new Date());
            info.setOperator(new IdName(merchant.getUuid(), merchant.getName()));
            ir.setOperateInfo(info);
        }
        return ir;
    }

}
