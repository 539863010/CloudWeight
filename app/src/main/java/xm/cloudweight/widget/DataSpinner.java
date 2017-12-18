package xm.cloudweight.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.xmzynt.storm.basic.idname.IdName;
import com.xmzynt.storm.service.goods.GoodsCategory;
import com.xmzynt.storm.service.purchase.PurchaseBill;
import com.xmzynt.storm.service.user.customer.CustomerLevel;
import com.xmzynt.storm.service.user.customer.MerchantCustomer;
import com.xmzynt.storm.service.wms.stockout.StockOutType;
import com.xmzynt.storm.service.wms.warehouse.Warehouse;

import java.util.ArrayList;
import java.util.List;

import xm.cloudweight.R;

/**
 * @author wyh
 * @Description:
 * @creat 2017/10/26
 */
public class DataSpinner<T> extends android.support.v7.widget.AppCompatSpinner {

    private List<String> mListTitle = new ArrayList<>();
    private List<T> mList;
    private OnItemSelectedListener mListener;
    private boolean isFirstLoad = true;
    private LayoutInflater mInflater;
    private int mTitleColorRes = -1;

    public DataSpinner(Context context) {
        super(context);
        init();
    }

    public DataSpinner(Context context, int mode) {
        super(context, mode);
        init();
    }

    public DataSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mInflater = LayoutInflater.from(getContext());
        setMinimumWidth(getResources().getDimensionPixelSize(R.dimen._60dp));
        setDropDownVerticalOffset(getResources().getDimensionPixelOffset(R.dimen._40dp));
//        mArrayAdapter.setDropDownViewResource(R.layout.item_spinner_title);
        setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                TextView tv = (TextView) view;
                if (tv == null) {
                    return;
                }
                if (mTitleColorRes != -1) {
                    tv.setTextColor(mTitleColorRes);
                }
                //供应商显示联系人
                T t = mList.get(i);
                if (t instanceof PurchaseBill) {
                    //验收供应商列表默认显示供应商
                    PurchaseBill purchaseBill = (PurchaseBill) t;
                    IdName supplier = purchaseBill.getSupplier();
                    tv.setText((supplier != null ? supplier.getName() : ""));
                }
                //取消第一次加载
                if (isFirstLoad) {
                    isFirstLoad = false;
                    return;
                }
                if (null != mListener) {
                    mListener.onItemSelected(adapterView, view, i, l);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                if (null != mListener) {
                    mListener.onNothingSelected(adapterView);
                }
            }
        });
    }

    public void setTitleColor(int titleColorRes) {
        mTitleColorRes = getResources().getColor(titleColorRes);
    }

    public List<T> getList() {
        return mList;
    }

    public void setList(final List<T> list) {
        mList = list;
        mListTitle.clear();
        for (T t : list) {
            if (t instanceof String) {
                mListTitle.add((String) t);
            } else if (t instanceof Warehouse) {
                //仓库列表
                mListTitle.add(((Warehouse) t).getName());
            } else if (t instanceof PurchaseBill) {
                //供应商
                PurchaseBill purchaseBill = (PurchaseBill) t;
                IdName supplier = purchaseBill.getSupplier();
                mListTitle.add(purchaseBill.getBillNumber() + "  " + (supplier != null ? supplier.getName() : ""));
            } else if (t instanceof MerchantCustomer) {
                //客户列表
                mListTitle.add(((MerchantCustomer) t).getCustomer().getName());
            } else if (t instanceof CustomerLevel) {
                //客户等级
                mListTitle.add(((CustomerLevel) t).getName());
            } else if (t instanceof GoodsCategory) {
                //类别（出库，调拨，盘点）
                mListTitle.add(((GoodsCategory) t).getName());
            } else if (t instanceof StockOutType) {
                //出库类型
                mListTitle.add(((StockOutType) t).getCaption());
            } else {
                throw new RuntimeException("----请设置类型----");
            }
        }
        setAdapter(mArrayAdapter);
    }

    private ArrayAdapter mArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.item_spinner_title, mListTitle) {
        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null)
                convertView = mInflater.inflate(
                        android.R.layout.simple_list_item_1, null);
            TextView text = convertView
                    .findViewById(android.R.id.text1);
            text.setText(mListTitle.get(position));
            text.setTextSize(getResources().getDimensionPixelSize(R.dimen._8sp));
            return convertView;
        }
    };

    @Override
    public T getSelectedItem() {
        if (mList == null || mList.size() == 0) {
            return null;
        }
        return mList.get(getSelectedItemPosition());
    }

    public void setCustomItemSelectedListener(OnItemSelectedListener listener) {
        mListener = listener;
    }
}
