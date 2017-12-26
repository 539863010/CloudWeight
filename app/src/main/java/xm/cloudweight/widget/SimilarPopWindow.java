package xm.cloudweight.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.xmzynt.storm.basic.ucn.UCN;
import com.xmzynt.storm.service.wms.stock.Stock;

import xm.cloudweight.R;
import xm.cloudweight.utils.DateUtils;

/**
 * @author wyh
 * @Description: 出库，调拨，盘点详情
 * @creat 2017/10/29
 */
public class SimilarPopWindow extends PopupWindow {

    private View mAnchor;
    private Context mContext;
    private TextView mTvWareHouse;
    private TextView mTvStoreInDate;

    public SimilarPopWindow(Context context, View anchor) {
        super(context);
        this.mAnchor = anchor;
        this.mContext = context;
        init();
    }

    private void init() {

        View v = LayoutInflater.from(mContext).inflate(R.layout.pop_similar_info, null);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setContentView(v);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(mContext.getResources().getColor(android.R.color.transparent)));

        //出库仓库
        mTvWareHouse = v.findViewById(R.id.similar_ware_house);
        //入库时间
        mTvStoreInDate = v.findViewById(R.id.similar_store_in_date);
    }

    public void show() {
        if (mAnchor == null) {
            throw new RuntimeException("must set anchor");
        }
        showAsDropDown(mAnchor, 0, 0);
    }

    public void setInfo(Stock stock) {
        if (stock == null) {
            mTvWareHouse.setText("");
            mTvStoreInDate.setText("");
            return;
        }
        UCN warehouse = stock.getWarehouse();
        mTvWareHouse.setText((warehouse != null && !TextUtils.isEmpty(warehouse.getName())) ? warehouse.getName() : "");
        mTvStoreInDate.setText(DateUtils.getTime(stock.getStockInDate()));
    }

}
