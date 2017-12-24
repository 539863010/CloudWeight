package xm.cloudweight.impl;

import com.xmzynt.storm.service.user.customer.CustomerLevel;
import com.xmzynt.storm.service.user.customer.MerchantCustomer;

import java.util.List;

import xm.cloudweight.bean.CustomSortOutData;

/**
 * @author wyh
 * @Description:
 * @creat 2017/10/31
 */
public class SortOutImpl {

    /**
     * 客户列表接口
     */
    public interface OnGetMerchantCustomerListener {

        void getMerchantCustomerSuccess(List<MerchantCustomer> list);

        void getMerchantCustomerFailed(int errorType, String message);

    }

    /**
     * 客户等级接口
     */
    public interface OnGetDropDownLevelsListener {

        void getDropDownLevelsSuccess(List<CustomerLevel> list);

        void getDropDownLevelsFailed(int errorType, String message);

    }

    /**
     * 查询分拣数据
     */
    public interface OnGetSortOutListListener {

        void getSortOutListSuccess(int type, List<CustomSortOutData> list);

        void getSortOutListFailed(int type, String message);

    }

    /**
     * 分拣
     */
    public interface OnSortOutListener {

        void onSortOutSuccess(CustomSortOutData list);

        void onSortOutFailed(String message);

    }

    /**
     * 撤销分拣
     */
    public interface OnCancelSortOutListener {

        void onCancelSortOutSuccess(CustomSortOutData list);

        void onCancelSortOutFailed(String message);

    }

}
