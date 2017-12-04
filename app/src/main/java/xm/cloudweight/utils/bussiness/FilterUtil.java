package xm.cloudweight.utils.bussiness;

import android.text.TextUtils;
import android.widget.EditText;

import com.xmzynt.storm.basic.idname.IdName;
import com.xmzynt.storm.basic.ucn.UCN;
import com.xmzynt.storm.service.sort.SortOutData;
import com.xmzynt.storm.service.user.customer.Customer;
import com.xmzynt.storm.service.user.customer.CustomerLevel;
import com.xmzynt.storm.service.user.customer.MerchantCustomer;

import java.util.ArrayList;
import java.util.List;

import xm.cloudweight.widget.DataSpinner;

/**
 * @author wyh
 * @Description:
 * @creat 2017/11/11
 */
public class FilterUtil {

    public static ArrayList<SortOutData> filter(List<SortOutData> mListAll
            , DataSpinner<CustomerLevel> mSpCustomersLevel
            , DataSpinner<MerchantCustomer> mSpCustomers
            , EditText mEtGoodsNameOrId
//            , EditText mEtBasket
            , EditText mEtCustomGroup
    ) {

        //逐级删除不符合的数据
        ArrayList<SortOutData> listCustomerLevel = new ArrayList<>();
        CustomerLevel customerLevel = mSpCustomersLevel.getSelectedItem();
        if (customerLevel != null && !customerLevel.getName().equals("全部")) {
            for (SortOutData data : mListAll) {
                IdName level = data.getCustomerLevel();
                if (level != null && customerLevel.getName().equals(level.getName())) {
                    listCustomerLevel.add(data);
                }
            }
        } else {
            listCustomerLevel.addAll(mListAll);
        }

        ArrayList<SortOutData> listMerchantCustomer = new ArrayList<>();
        MerchantCustomer merchantCustomer = mSpCustomers.getSelectedItem();
        if (merchantCustomer != null && !merchantCustomer.getCustomer().getName().equals("全部")) {
            for (SortOutData data : listCustomerLevel) {
                Customer customer = merchantCustomer.getCustomer();
                IdName idName = data.getCustomer();
                if (customer != null && idName != null && customer.getName().equals(idName.getName())) {
                    listMerchantCustomer.add(data);
                }
            }
        } else {
            listMerchantCustomer.addAll(listCustomerLevel);
        }

        ArrayList<SortOutData> listGoodsNameOrId = new ArrayList<>();
        String goodsNameOrId = mEtGoodsNameOrId.getText().toString().trim();
        if (!TextUtils.isEmpty(goodsNameOrId)) {
            for (SortOutData data : listMerchantCustomer) {
                UCN goods = data.getGoods();
                if (goods != null && (goods.getName().contains(goodsNameOrId)
                        || goodsNameOrId.equals(goods.getUuid()))) {
                    listGoodsNameOrId.add(data);
                }
            }
        } else {
            listGoodsNameOrId.addAll(listMerchantCustomer);
        }

//        ArrayList<SortOutData> listBasket = new ArrayList<>();
//        String basket = mEtBasket.getText().toString().trim();
//        if (!TextUtils.isEmpty(basket)) {
//            for (SortOutData data : listGoodsNameOrId) {
//                if (basket.equals(data.getBasketCode())) {
//                    listBasket.add(data);
//                }
//            }
//        } else {
//            listBasket.addAll(listGoodsNameOrId);
//        }

        ArrayList<SortOutData> listCustomGroup = new ArrayList<>();
        String customGroup = mEtCustomGroup.getText().toString();
        if (!TextUtils.isEmpty(customGroup)) {
            for (SortOutData data : listGoodsNameOrId) {
                if (customGroup.equals(data.getCustomerGroup())) {
                    listCustomGroup.add(data);
                }
            }
        } else {
            listCustomGroup.addAll(listGoodsNameOrId);
        }
        return listCustomGroup;
    }

}
