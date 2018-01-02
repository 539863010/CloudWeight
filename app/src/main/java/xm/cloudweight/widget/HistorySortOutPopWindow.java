package xm.cloudweight.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.xmzynt.storm.util.GsonUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import xm.cloudweight.R;
import xm.cloudweight.bean.CustomSortOutData;
import xm.cloudweight.utils.BigDecimalUtil;
import xm.cloudweight.utils.ToastUtil;
import xm.cloudweight.utils.bussiness.PrinterSortOut;
import xm.cloudweight.utils.dao.bean.DbImageUpload;

/**
 * @author wyh
 * @Description:
 * @creat 2017/10/29
 */
public class HistorySortOutPopWindow extends PopupWindow implements View.OnClickListener {

    private View mAnchor;
    private Context mContext;
    private ListView mLvHistory;
    private List<DbImageUpload> mList = new ArrayList<>();
    private List<DbImageUpload> mListSearch = new ArrayList<>();
    private HistoryAdapter mHistoryAdapter;
    private OnDeleteListener mOnDeleteListener;
    private ImageView mIvHistorySearch;
    private EditText mEtHistoryGoodsName;

    public HistorySortOutPopWindow(Context context, View anchor) {
        super(context);
        this.mAnchor = anchor;
        this.mContext = context;
        init();
    }

    private void init() {

        View view = LayoutInflater.from(mContext).inflate(R.layout.pop_sort_out_history, null);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(view);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(mContext.getResources().getColor(android.R.color.transparent)));

        view.findViewById(R.id.pop_title).setBackgroundColor(mContext.getResources().getColor(R.color.color_c5dcc0));
        int color = mContext.getResources().getColor(R.color.color_135c31);
        TextView titleName = view.findViewById(R.id.item_goods_name);
        titleName.setTextColor(color);
        TextView titleUnit = view.findViewById(R.id.item_goods_unit);
        titleUnit.setTextColor(color);
        TextView titleOrderNum = view.findViewById(R.id.item_order_num);
        titleOrderNum.setTextColor(color);
        TextView titleSortOutNum = view.findViewById(R.id.item_sort_out_num);
        titleSortOutNum.setTextColor(color);
        TextView titleCustomer = view.findViewById(R.id.item_customer);
        titleCustomer.setTextColor(color);
        view.findViewById(R.id.item_printer_label).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.item_revocation).setVisibility(View.INVISIBLE);

        mEtHistoryGoodsName = view.findViewById(R.id.et_history_goods_name);
        mIvHistorySearch = view.findViewById(R.id.iv_history_search);
        mIvHistorySearch.setOnClickListener(this);

        mLvHistory = view.findViewById(R.id.lv_sort_sort_history);
        mHistoryAdapter = new HistoryAdapter(mContext, mListSearch);
        mLvHistory.setAdapter(mHistoryAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_history_search:
                String key = mEtHistoryGoodsName.getText().toString().trim();
                mListSearch.clear();
                if (!TextUtils.isEmpty(key) && mList.size() > 0) {
                    for (DbImageUpload dbImageUpload : mList) {
                        CustomSortOutData data = GsonUtil.getGson().fromJson(dbImageUpload.getLine(), CustomSortOutData.class);
                        if (data != null && data.getGoods().getName().contains(key)) {
                            mListSearch.add(dbImageUpload);
                        }
                    }
                } else {
                    mListSearch.addAll(mList);
                    ToastUtil.showShortToast(mContext, "商品名不能为空");
                }
                mHistoryAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    public void show() {
        if (mAnchor == null) {
            throw new RuntimeException("must set anchor");
        }
        if (!isShowing()) {
            showAsDropDown(mAnchor, 0, 0);
        }
    }

    public void notify(List<DbImageUpload> daoList) {
        mList.clear();
        mList.addAll(daoList);
        mListSearch.clear();
        mListSearch.addAll(mList);
        mHistoryAdapter.notifyDataSetChanged();
    }

    private class HistoryAdapter extends CommonAdapter4Lv<DbImageUpload> {

        private HistoryAdapter(Context context, List<DbImageUpload> data) {
            super(context, R.layout.item_sort_out_history, data);
        }

        @Override
        public void doSomething(CommonHolder4Lv holder, final DbImageUpload dbSortOutData, int position) {
            final CustomSortOutData data = GsonUtil.getGson().fromJson(dbSortOutData.getLine(), CustomSortOutData.class);
            final String goodsName = data.getGoods().getName();
            holder.setText(R.id.item_goods_name, goodsName);
            String goodsUnit = data.getGoodsUnit().getName();
            holder.setText(R.id.item_goods_unit, goodsUnit);
            BigDecimal unitCoefficient = data.getUnitCoefficient();
            String strOrderNum;
            String strSortOutNum;
            if (unitCoefficient != null && unitCoefficient.doubleValue() != 0) {
                strOrderNum = BigDecimalUtil.toScaleStr(data.getCoverToKgQty()).concat("kg");
                strSortOutNum = BigDecimalUtil.toScaleStr(data.getStockOutQty().multiply(unitCoefficient)).concat("kg");
            } else {
                strOrderNum = BigDecimalUtil.toScaleStr(data.getGoodsQty().subtract(data.getStockOutQty()).subtract(data.getHasStockOutQty())).concat(goodsUnit);
                strSortOutNum = BigDecimalUtil.toScaleStr(data.getStockOutQty()).concat(goodsUnit);
            }
            holder.setText(R.id.item_order_num, strOrderNum);
            holder.setText(R.id.item_sort_out_num, strSortOutNum);

            holder.setText(R.id.item_customer, data.getCustomer().getName());
            holder.setOnClickListener(R.id.item_revocation, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnDeleteListener != null) {
                        mOnDeleteListener.delete(dbSortOutData);
                    }
                }
            });
            final String sortOutNum = strSortOutNum;
            holder.setOnClickListener(R.id.item_printer_label, new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //打印标签
                    String traceCode = data.getPlatformTraceCode();
                    String customer = data.getCustomer() != null ? data.getCustomer().getName() : "";
                    String department = data.getCustomerDepartment() != null ? data.getCustomerDepartment().getName() : "";
                    PrinterSortOut.printer(
                            mContext,
                            1,
                            PrinterSortOut.SORT_OUT_QRCODE,
                            customer,
                            department,
                            goodsName,
                            sortOutNum,
                            traceCode);
                }
            });
        }
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        mOnDeleteListener = onDeleteListener;
    }

    public interface OnDeleteListener {

        void delete(DbImageUpload data);

    }

}
