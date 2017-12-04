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
    private TextView mTvOrigin;
    private TextView mTvBatchNum;
    private TextView mTvCertificateNum;
    private TextView mTvProduceDate;
    private TextView mTvEffeiveDate;

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
        //产地
        mTvOrigin = v.findViewById(R.id.similar_origin);
        //批号
        mTvBatchNum = v.findViewById(R.id.similar_batch_num);
        //上市凭证号
        mTvCertificateNum = v.findViewById(R.id.similar_certificate_num);
        //生产日期
        mTvProduceDate = v.findViewById(R.id.similar_produce_date);
        //有效日期
        mTvEffeiveDate = v.findViewById(R.id.similar_effetive_date);

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
            mTvOrigin.setText("");
            mTvBatchNum.setText("");
            mTvCertificateNum.setText("");
            mTvProduceDate.setText("");
            mTvEffeiveDate.setText("");
            return;
        }
        UCN warehouse = stock.getWarehouse();
        mTvWareHouse.setText((warehouse != null && !TextUtils.isEmpty(warehouse.getName())) ? warehouse.getName() : "");
        String origin = !TextUtils.isEmpty(stock.getOrigin()) ? stock.getOrigin() : "";
        mTvOrigin.setText(origin);
        mTvBatchNum.setText(!TextUtils.isEmpty(stock.getBatchNumber()) ? stock.getBatchNumber() : "");
        mTvCertificateNum.setText(!TextUtils.isEmpty(stock.getListingCertificateNo()) ? stock.getListingCertificateNo() : "");
        mTvStoreInDate.setText(DateUtils.getTime(stock.getStockInDate()));
        mTvProduceDate.setText(DateUtils.getTime(stock.getProduceDate()));
        mTvEffeiveDate.setText(DateUtils.getTime(stock.getEffectiveDate()));
    }

}
