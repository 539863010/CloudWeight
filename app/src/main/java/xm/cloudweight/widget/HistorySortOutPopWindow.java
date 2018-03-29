package xm.cloudweight.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
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
import xm.cloudweight.utils.bussiness.printer.PrinterBean;
import xm.cloudweight.utils.bussiness.printer.PrinterSortOut;
import xm.cloudweight.utils.dao.bean.DbImageUpload;
import xm.cloudweight.widget.impl.OnDeleteListener;

/**
 * @author wyh
 * @Description: 分拣历史pop
 * @creat 2017/10/29
 */
public class HistorySortOutPopWindow extends PopupWindow implements View.OnClickListener {

    private View mAnchor;
    private Context mContext;
    private List<DbImageUpload> mList = new ArrayList<>();
    private List<DbImageUpload> mListSearch = new ArrayList<>();
    private HistoryAdapter mHistoryAdapter;
    private OnDeleteListener mOnDeleteListener;
    private EditText mEtHistoryGoodsName;
    private CancelDialog mCancelDialog;

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
        TextView titleOrderNum = view.findViewById(R.id.item_order_num);
        titleOrderNum.setTextColor(color);
        TextView titleSortOutNum = view.findViewById(R.id.item_sort_out_num);
        titleSortOutNum.setTextColor(color);
        TextView titleCustomer = view.findViewById(R.id.item_customer);
        titleCustomer.setTextColor(color);
        view.findViewById(R.id.item_printer_label).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.item_revocation).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.item_upload_state).setVisibility(View.GONE);

        //父布局设置不自动弹出
        //   android:focusable="true"
        //   android:focusableInTouchMode="true"
        mEtHistoryGoodsName = view.findViewById(R.id.et_history_goods_name);
        view.findViewById(R.id.iv_history_search).setOnClickListener(this);

        ListView lvHistory = view.findViewById(R.id.lv_sort_sort_history);
        mHistoryAdapter = new HistoryAdapter(mContext, mListSearch);
        lvHistory.setAdapter(mHistoryAdapter);
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
            //设置状态
            holder.setVisible(R.id.item_upload, View.GONE);
            boolean isRequestSuccess = dbSortOutData.getIsRequestSuccess();
            if (isRequestSuccess) {
                holder.setVisible(R.id.item_upload_state, View.VISIBLE);
                holder.setVisible(R.id.item_revocation, View.VISIBLE);
            } else {
                holder.setVisible(R.id.item_upload_state, View.INVISIBLE);
                holder.setVisible(R.id.item_revocation, View.INVISIBLE);
            }
            final String goodsName = data.getGoods().getName();
            holder.setText(R.id.item_goods_name, goodsName);
            String goodsUnit = data.getGoodsUnit().getName();
            BigDecimal unitCoefficient = data.getUnitCoefficient();
            String strSortOutNum;
            if (unitCoefficient != null && unitCoefficient.doubleValue() != 0) {
                strSortOutNum = BigDecimalUtil.toScaleStr(data.getStockOutQty().multiply(unitCoefficient)).concat("kg");
            } else {
                strSortOutNum = BigDecimalUtil.toScaleStr(data.getStockOutQty()).concat(goodsUnit);
            }
            String strOrderNum = BigDecimalUtil.toScaleStr(data.getGoodsQty()).concat(goodsUnit);
            holder.setText(R.id.item_order_num, strOrderNum);
            holder.setText(R.id.item_sort_out_num, strSortOutNum);

            holder.setText(R.id.item_customer, data.getCustomer().getName());
            holder.setOnClickListener(R.id.item_revocation, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mCancelDialog == null) {
                        mCancelDialog = new CancelDialog(mContext, mOnDeleteListener);
                    }
                    mCancelDialog.showCancelDialog(dbSortOutData, "分拣");
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
                    String storageMode = data.getStorageMode();
                    String period = data.getPeriod();
                    PrinterBean printerBean = PrinterBean.get(1, traceCode, customer, department, goodsName, sortOutNum, storageMode, period);
                    PrinterSortOut.printer(
                            mContext,
                            printerBean);
                }
            });
        }
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        mOnDeleteListener = onDeleteListener;
    }

}
