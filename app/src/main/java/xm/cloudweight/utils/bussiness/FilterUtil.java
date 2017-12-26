package xm.cloudweight.utils.bussiness;

import android.text.TextUtils;
import android.widget.EditText;

import com.xmzynt.storm.basic.idname.IdName;
import com.xmzynt.storm.basic.ucn.UCN;
import com.xmzynt.storm.service.user.customer.Customer;
import com.xmzynt.storm.service.user.customer.CustomerLevel;
import com.xmzynt.storm.service.user.customer.MerchantCustomer;

import java.util.ArrayList;
import java.util.List;

import xm.cloudweight.bean.CustomSortOutData;
import xm.cloudweight.widget.DataSpinner;

/**
 * @author wyh
 * @Description:
 * @creat 2017/11/11
 */
public class FilterUtil {

    public static ArrayList<CustomSortOutData> filter(List<CustomSortOutData> mListAll
            , DataSpinner<CustomerLevel> mSpCustomersLevel
            , DataSpinner<MerchantCustomer> mSpCustomers
            , EditText mEtCustomGroup
            , EditText mEtGoodsNameOrId
            , EditText mEtTag
    ) {
        for (CustomSortOutData data : mListAll) {
            data.setLabelCode(null);
        }
        //逐级删除不符合的数据
        ArrayList<CustomSortOutData> listCustomerLevel = new ArrayList<>();
        CustomerLevel customerLevel = mSpCustomersLevel.getSelectedItem();
        if (customerLevel != null && !customerLevel.getName().equals("全部")) {
            for (CustomSortOutData data : mListAll) {
                IdName level = data.getCustomerLevel();
                if (level != null && customerLevel.getName().equals(level.getName())) {
                    listCustomerLevel.add(data);
                }
            }
        } else {
            listCustomerLevel.addAll(mListAll);
        }

        ArrayList<CustomSortOutData> listMerchantCustomer = new ArrayList<>();
        MerchantCustomer merchantCustomer = mSpCustomers.getSelectedItem();
        if (merchantCustomer != null && !merchantCustomer.getCustomer().getName().equals("全部")) {
            for (CustomSortOutData data : listCustomerLevel) {
                Customer customer = merchantCustomer.getCustomer();
                IdName idName = data.getCustomer();
                if (customer != null && idName != null && customer.getName().equals(idName.getName())) {
                    listMerchantCustomer.add(data);
                }
            }
        } else {
            listMerchantCustomer.addAll(listCustomerLevel);
        }

        ArrayList<CustomSortOutData> listGoodsNameOrId = new ArrayList<>();
        String goodsNameOrId = mEtGoodsNameOrId.getText().toString().trim();
        if (!TextUtils.isEmpty(goodsNameOrId)) {
            for (CustomSortOutData data : listMerchantCustomer) {
                UCN goods = data.getGoods();
                if (goods != null && (goods.getName().contains(goodsNameOrId)
                        || goodsNameOrId.equals(goods.getUuid()))) {
                    listGoodsNameOrId.add(data);
                }
            }
        } else {
            listGoodsNameOrId.addAll(listMerchantCustomer);
        }

        ArrayList<CustomSortOutData> listTag = new ArrayList<>();
        String tag = mEtTag.getText().toString().trim();
        //库存标签码（商品编码+“-”+日期+“-”+流水号）
        if (!TextUtils.isEmpty(tag)
                && tag.contains("-")
                && tag.split("-").length == 3) {
            String goodsCode = tag.split("-")[0];
            for (CustomSortOutData data : listGoodsNameOrId) {
                String code = data.getGoods().getCode();
                if (goodsCode.equals(code)) {
                    //设置标签码， 扫描筛选后的数据上传分拣接口要设置到basketCode里面
                    data.setLabelCode(tag);
                    listTag.add(data);
                }
            }
        } else {
            listTag.addAll(listGoodsNameOrId);
        }

        ArrayList<CustomSortOutData> listCustomGroup = new ArrayList<>();
        String customGroup = mEtCustomGroup.getText().toString();
        if (!TextUtils.isEmpty(customGroup)) {
            for (CustomSortOutData data : listTag) {
                if (customGroup.equals(data.getCustomerGroup())) {
                    listCustomGroup.add(data);
                }
            }
        } else {
            listCustomGroup.addAll(listTag);
        }
        return listCustomGroup;
    }

}
