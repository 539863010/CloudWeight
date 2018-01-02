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
import com.xmzynt.storm.service.wms.allocate.AllocateRecord;
import com.xmzynt.storm.service.wms.stockout.StockOutRecord;
import com.xmzynt.storm.service.wms.stockout.StockOutType;
import com.xmzynt.storm.util.GsonUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import xm.cloudweight.R;
import xm.cloudweight.comm.Common;
import xm.cloudweight.utils.BigDecimalUtil;
import xm.cloudweight.utils.ToastUtil;
import xm.cloudweight.utils.dao.bean.DbImageUpload;

/**
 * @author wyh
 * @Description:
 * @creat 2017/10/29
 */
public class HistorySimilarPopWindow extends PopupWindow implements View.OnClickListener {

    private final int mType;
    private View mAnchor;
    private Context mContext;
    private ListView mLvHistory;
    private List<DbImageUpload> mList = new ArrayList<>();
    private List<DbImageUpload> mListSearch = new ArrayList<>();
    private HistoryAdapter mHistoryAdapter;
    private OnDeleteListener mOnDeleteListener;
    private ImageView mIvHistorySearch;
    private EditText mEtHistoryGoodsName;

    public HistorySimilarPopWindow(Context context, int type, View anchor) {
        super(context);
        this.mAnchor = anchor;
        this.mType = type;
        this.mContext = context;
        init();
    }

    private void init() {

        View view = LayoutInflater.from(mContext).inflate(R.layout.pop_similar_history, null);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(view);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(mContext.getResources().getColor(android.R.color.transparent)));

        view.findViewById(R.id.pop_title).setBackgroundColor(mContext.getResources().getColor(R.color.color_c5dcc0));
        int color = mContext.getResources().getColor(R.color.color_135c31);
        TextView goodsName = view.findViewById(R.id.similar_goods_name);
        goodsName.setTextColor(color);
        TextView num = view.findViewById(R.id.similar_num);
        num.setTextColor(color);
        TextView unit = view.findViewById(R.id.similar_unit);
        unit.setTextColor(color);
        TextView operation = view.findViewById(R.id.similar_operatime);
        operation.setTextColor(color);
        view.findViewById(R.id.similar_revocation).setVisibility(View.INVISIBLE);

        if (mType == Common.SIMILAR_STOCKOUT) {
            TextView type = view.findViewById(R.id.similar_type);
            type.setTextColor(color);
            type.setVisibility(View.VISIBLE);
        } else if (mType == Common.SIMILAR_ALLOCATE) {
            TextView wareHouseOut = view.findViewById(R.id.similar_warehouse_out);
            wareHouseOut.setTextColor(color);
            wareHouseOut.setVisibility(View.VISIBLE);
            TextView wareHouseIn = view.findViewById(R.id.similar_warehouse_in);
            wareHouseIn.setTextColor(color);
            wareHouseIn.setVisibility(View.VISIBLE);
        }

        mEtHistoryGoodsName = view.findViewById(R.id.et_history_goods_name);
        mIvHistorySearch = view.findViewById(R.id.iv_history_search);
        mIvHistorySearch.setOnClickListener(this);

        mLvHistory = view.findViewById(R.id.lv_similar_history);
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
                        int type = dbImageUpload.getType();
                        String name = null;
                        if (type == Common.DbType.TYPE_STORE_OUT) {
                            StockOutRecord data = GsonUtil.getGson().fromJson(dbImageUpload.getLine(), StockOutRecord.class);
                            name = data.getGoods().getName();
                        } else if (type == Common.DbType.TYPE_ALLOCATE) {
                            AllocateRecord data = GsonUtil.getGson().fromJson(dbImageUpload.getLine(), AllocateRecord.class);
                            name = data.getGoods().getName();
                        }
                        if (!TextUtils.isEmpty(name) && name.contains(key)) {
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
            super(context, R.layout.item_similar_history, data);
        }

        @Override
        public void doSomething(CommonHolder4Lv holder, final DbImageUpload dbSortOutData, int position) {
            int type = dbSortOutData.getType();
            String goodsName = "";
            String goodsUnit = "";
            BigDecimal num = new BigDecimal(0);
            if (type == Common.DbType.TYPE_STORE_OUT) {
                StockOutRecord data = GsonUtil.getGson().fromJson(dbSortOutData.getLine(), StockOutRecord.class);
                ///调入仓库
                holder.setVisible(R.id.similar_warehouse_in, View.GONE);
                //调出仓库
                holder.setVisible(R.id.similar_warehouse_out, View.GONE);
                holder.setVisible(R.id.similar_type, View.VISIBLE);
                StockOutType stockOutType = data.getStockOutType();
                if (stockOutType != null) {
                    holder.setText(R.id.similar_type, stockOutType.getCaption());
                } else {
                    holder.setText(R.id.similar_type, "");
                }
                goodsName = data.getGoods().getName();
                goodsUnit = data.getGoodsUnit().getName();
                num = data.getQuantity();
            } else if (type == Common.DbType.TYPE_ALLOCATE) {
                AllocateRecord data = GsonUtil.getGson().fromJson(dbSortOutData.getLine(), AllocateRecord.class);
                //隐藏出库类型
                holder.setVisible(R.id.similar_type, View.GONE);
                holder.setVisible(R.id.similar_warehouse_in, View.VISIBLE);
                holder.setVisible(R.id.similar_warehouse_out, View.VISIBLE);
                //调入仓库
                UCN inWarehouse = data.getInWarehouse();
                if (inWarehouse != null) {
                    holder.setText(R.id.similar_warehouse_in, inWarehouse.getName());
                } else {
                    holder.setText(R.id.similar_warehouse_in, "");
                }
                //调出仓库
                UCN outWarehouse = data.getOutWarehouse();
                if (outWarehouse != null) {
                    holder.setText(R.id.similar_warehouse_out, outWarehouse.getName());
                } else {
                    holder.setText(R.id.similar_warehouse_out, "");
                }
                goodsName = data.getGoods().getName();
                goodsUnit = data.getGoodsUnit().getName();
                num = data.getAllocateQty();
            }
            //商品
            holder.setText(R.id.similar_goods_name, goodsName);
            //数量
            holder.setText(R.id.similar_num, BigDecimalUtil.toScaleStr(num));
            //单位
            holder.setText(R.id.similar_unit, goodsUnit);
            //操作时间
            holder.setText(R.id.similar_operatime, dbSortOutData.getOperatime());
            //撤销
            holder.setOnClickListener(R.id.similar_revocation, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnDeleteListener != null) {
                        mOnDeleteListener.delete(dbSortOutData);
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
