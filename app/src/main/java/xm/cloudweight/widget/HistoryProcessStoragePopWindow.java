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

import com.xmzynt.storm.basic.idname.IdName;
import com.xmzynt.storm.basic.ucn.UCN;
import com.xmzynt.storm.service.process.StockInData;
import com.xmzynt.storm.service.wms.stockin.StockInRecord;
import com.xmzynt.storm.util.GsonUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import xm.cloudweight.R;
import xm.cloudweight.utils.BigDecimalUtil;
import xm.cloudweight.utils.ToastUtil;
import xm.cloudweight.utils.dao.bean.DbImageUpload;
import xm.cloudweight.widget.impl.OnDeleteListener;

/**
 * @author wyh
 * @Description: 加工入库
 * @creat 2017/10/29
 */
public class HistoryProcessStoragePopWindow extends PopupWindow implements View.OnClickListener {

    private View mAnchor;
    private Context mContext;
    private List<DbImageUpload> mList = new ArrayList<>();
    private List<DbImageUpload> mListSearch = new ArrayList<>();
    private HistoryAdapter mHistoryAdapter;
    private OnDeleteListener mOnDeleteListener;
    private EditText mEtHistoryGoodsName;
    private CancelDialog mCancelDialog;

    public HistoryProcessStoragePopWindow(Context context, View anchor) {
        super(context);
        this.mAnchor = anchor;
        this.mContext = context;
        init();
    }

    private void init() {

        View view = LayoutInflater.from(mContext).inflate(R.layout.pop_process_storage_history, null);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(view);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(mContext.getResources().getColor(android.R.color.transparent)));

        view.findViewById(R.id.pop_title).setBackgroundColor(mContext.getResources().getColor(R.color.color_c5dcc0));
        int color = mContext.getResources().getColor(R.color.color_135c31);
        TextView titleName = view.findViewById(R.id.ci_goods);
        titleName.setTextColor(color);
        TextView titleSpec = view.findViewById(R.id.ci_spec);
        titleSpec.setTextColor(color);
        TextView titleUnit = view.findViewById(R.id.ci_unit);
        titleUnit.setTextColor(color);
        TextView titleProduction = view.findViewById(R.id.ci_production);
        titleProduction.setTextColor(color);
        TextView titleBad = view.findViewById(R.id.ci_bad);
        titleBad.setTextColor(color);
        TextView titlePrice = view.findViewById(R.id.ci_price);
        titlePrice.setTextColor(color);
        TextView titleOperationTime = view.findViewById(R.id.ci_operation_time);
        titleOperationTime.setTextColor(color);
        view.findViewById(R.id.ci_revocation).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.item_upload_state).setVisibility(View.GONE);

        mEtHistoryGoodsName = view.findViewById(R.id.et_history_goods_name);
        view.findViewById(R.id.iv_history_search).setOnClickListener(this);

        ListView lvHistory = view.findViewById(R.id.lv_check_in_history);
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
            super(context, R.layout.item_process_storage_history, data);
        }

        @Override
        public void doSomething(CommonHolder4Lv holder, final DbImageUpload dbSortOutData, int position) {
            final StockInData data = GsonUtil.getGson().fromJson(dbSortOutData.getLine(), StockInData.class);
            //设置状态
            holder.setVisible(R.id.item_upload, View.GONE);
            boolean isRequestSuccess = dbSortOutData.getIsRequestSuccess();
            if (isRequestSuccess) {
                holder.setVisible(R.id.item_upload_state, View.VISIBLE);
                holder.setVisible(R.id.ci_revocation, View.VISIBLE);
            } else {
                holder.setVisible(R.id.item_upload_state, View.INVISIBLE);
                holder.setVisible(R.id.ci_revocation, View.INVISIBLE);
            }
            //设置商品名
            UCN goods = data.getGoods();
            holder.setText(R.id.ci_goods, goods != null ? goods.getName() : "");
            //包装规格
            String packageSpec = data.getPackageSpec();
            holder.setText(R.id.ci_spec, !TextUtils.isEmpty(packageSpec) ? packageSpec : "");
            //单位
            IdName goodsUnit = data.getGoodsUnit();
            holder.setText(R.id.ci_unit, goodsUnit != null ? goodsUnit.getName() : "");
            //产量
            final BigDecimal quantity = data.getOutputQty();
            holder.setText(R.id.ci_production, BigDecimalUtil.toScaleStr(quantity));
            //不良
            holder.setText(R.id.ci_bad, BigDecimalUtil.toScaleStr(data.getRejectQty()));
            //入库单价
            holder.setText(R.id.ci_price, BigDecimalUtil.toScaleStr(data.getPrice()));
            //设置操作时间
            holder.setText(R.id.ci_operation_time, dbSortOutData.getOperatime());

            holder.setOnClickListener(R.id.ci_revocation, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mCancelDialog == null) {
                        mCancelDialog = new CancelDialog(mContext, mOnDeleteListener);
                    }
                    mCancelDialog.showCancelDialog(dbSortOutData, "加工入库");
                }
            });
        }
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        mOnDeleteListener = onDeleteListener;
    }

}
