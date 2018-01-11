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

import com.xmzynt.storm.basic.ucn.UCN;
import com.xmzynt.storm.service.wms.stockin.StockInRecord;
import com.xmzynt.storm.util.GsonUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import xm.cloudweight.R;
import xm.cloudweight.comm.Common;
import xm.cloudweight.utils.BigDecimalUtil;
import xm.cloudweight.utils.ToastUtil;
import xm.cloudweight.utils.bussiness.PrinterInventory;
import xm.cloudweight.utils.bussiness.PrinterSortOut;
import xm.cloudweight.utils.dao.bean.DbImageUpload;

/**
 * @author wyh
 * @Description:
 * @creat 2017/10/29
 */
public class HistoryCheckInPopWindow extends PopupWindow implements View.OnClickListener {

    private View mAnchor;
    private Context mContext;
    private ListView mLvHistory;
    private List<DbImageUpload> mList = new ArrayList<>();
    private List<DbImageUpload> mListSearch = new ArrayList<>();
    private HistoryAdapter mHistoryAdapter;
    private OnDeleteListener mOnDeleteListener;
    private ImageView mIvHistorySearch;
    private EditText mEtHistoryGoodsName;

    public HistoryCheckInPopWindow(Context context, View anchor) {
        super(context);
        this.mAnchor = anchor;
        this.mContext = context;
        init();
    }

    private void init() {

        View view = LayoutInflater.from(mContext).inflate(R.layout.pop_check_in_history, null);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(view);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(mContext.getResources().getColor(android.R.color.transparent)));

        view.findViewById(R.id.pop_title).setBackgroundColor(mContext.getResources().getColor(R.color.color_c5dcc0));
        int color = mContext.getResources().getColor(R.color.color_135c31);
        TextView titleType = view.findViewById(R.id.ci_type);
        titleType.setTextColor(color);
        TextView titleName = view.findViewById(R.id.ci_goods);
        titleName.setTextColor(color);
        TextView titleNum = view.findViewById(R.id.ci_num);
        titleNum.setTextColor(color);
        TextView titleUnit = view.findViewById(R.id.ci_unit);
        titleUnit.setTextColor(color);
        TextView titleTime = view.findViewById(R.id.ci_operation_time);
        titleTime.setTextColor(color);
        view.findViewById(R.id.ci_print_label).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.ci_revocation).setVisibility(View.INVISIBLE);

        mEtHistoryGoodsName = view.findViewById(R.id.et_history_goods_name);
        mIvHistorySearch = view.findViewById(R.id.iv_history_search);
        mIvHistorySearch.setOnClickListener(this);

        mLvHistory = view.findViewById(R.id.lv_check_in_history);
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
                        StockInRecord data = GsonUtil.getGson().fromJson(dbImageUpload.getLine(), StockInRecord.class);
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
            super(context, R.layout.item_check_in_history, data);
        }

        @Override
        public void doSomething(CommonHolder4Lv holder, final DbImageUpload dbSortOutData, int position) {
            final StockInRecord data = GsonUtil.getGson().fromJson(dbSortOutData.getLine(), StockInRecord.class);
            //设置类型
            final int type = dbSortOutData.getType();
            if (type == Common.DbType.TYPE_ChECK_IN_STORE_IN) {
                holder.setText(R.id.ci_type, Common.DbType.STR_TYPE_ChECK_IN_STORE_IN);
            } else if (type == Common.DbType.TYPE_ChECK_IN_CROSS_OUT) {
                holder.setText(R.id.ci_type, Common.DbType.STR_TYPE_ChECK_IN_CROSS_OUT);
            } else if (type == Common.DbType.TYPE_ChECK_IN_CROSS_ALLCOCATE) {
                holder.setText(R.id.ci_type, Common.DbType.STR_TYPE_ChECK_IN_CROSS_ALLCOCATE);
            } else {
                ToastUtil.showShortToast(mContext, "类型有误");
            }
            //设置操作时间
            holder.setText(R.id.ci_operation_time, dbSortOutData.getOperatime());
            //设置商品名
            UCN goods = data.getGoods();
            final String goodsName = goods.getName();
            holder.setText(R.id.ci_goods, goodsName);
            //设置单位
            String goodsUnit = data.getGoodsUnit().getName();
            holder.setText(R.id.ci_unit, goodsUnit);
            //设置数量
            final BigDecimal quantity = data.getQuantity();
            holder.setText(R.id.ci_num, BigDecimalUtil.toScaleStr(quantity));

            holder.setOnClickListener(R.id.ci_revocation, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnDeleteListener != null) {
                        mOnDeleteListener.delete(dbSortOutData);
                    }
                }
            });
            holder.setOnClickListener(R.id.ci_print_label, new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //打印标签
                    String count = BigDecimalUtil.toScaleStr(quantity);
                    // 顾客-部门   部门可能为null
                    String customer = data.getCustomerName();
                    String department = "";
                    if (customer.contains("-")) {
                        String[] s = customer.split("-");
                        customer = s[0];
                        department = s[1];
                    }
                    if (type == Common.DbType.TYPE_ChECK_IN_CROSS_OUT) {
                        String platformTraceCode = data.getPlatformTraceCode();
                        PrinterSortOut.printer(
                                mContext,
                                1,
                                PrinterSortOut.SORT_OUT_QRCODE.concat(platformTraceCode),
                                customer,
                                department,
                                goodsName,
                                count,
                                platformTraceCode);
                    } else if (type == Common.DbType.TYPE_ChECK_IN_STORE_IN ||
                            type == Common.DbType.TYPE_ChECK_IN_CROSS_ALLCOCATE) {
                        String purchaseBatch = data.getTraceCode();
                        PrinterInventory.printer(mContext, 1, goodsName, purchaseBatch);
                    }
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